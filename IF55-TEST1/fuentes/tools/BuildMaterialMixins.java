import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;
import java.util.zip.ZipFile;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

/** Emits the two source-equivalent Mixins without inventing Minecraft stubs. */
public final class BuildMaterialMixins implements Opcodes {
    private static final String STACK = "Lnet/minecraft/world/item/ItemStack;";
    private static final String COMPARE = "(" + STACK + STACK + ")Z";
    private static final String ITEM_STACK = "net/minecraft/world/item/ItemStack";
    private static final String HELPER = "com/viaversion/viafabricplus/bedrock/compat/BedrockMaterialComparison";
    private static final String MIXIN_ROOT = "com/viaversion/viafabricplus/bedrock/injection/mixin/compat/easyplacefix/";
    private static final String EXPECTED_EPF = "c20e5c42aaf47a260c058f8230a04b861d4266e7e8ec44713cb72da8ac1d067f";

    private record Spec(String mixin, String target, String method, String descriptor, boolean isStatic) {}
    private static final List<Spec> SPECS = List.of(
        new Spec("MixinEasyPlaceFixMaterialSearch", "org/uiop/easyplacefix/util/PlacementInventory", "getSlotWithStack", "(" + STACK + "Lnet/minecraft/world/entity/player/Inventory;)I", true),
        new Spec("MixinEasyPlaceFixMaterialRow", "org/uiop/easyplacefix/materials/MaterialSnapshot$Row", "matches", "(" + STACK + ")Z", false)
    );

    private static String sha(byte[] bytes) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }

    private static byte[] emit(Spec spec) {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        cw.visit(V25, ACC_PUBLIC | ACC_SUPER | ACC_ABSTRACT, MIXIN_ROOT + spec.mixin, null, "java/lang/Object", null);
        cw.visitAnnotation("Lorg/spongepowered/asm/mixin/Pseudo;", false).visitEnd();
        AnnotationVisitor mixin = cw.visitAnnotation("Lorg/spongepowered/asm/mixin/Mixin;", false);
        AnnotationVisitor targets = mixin.visitArray("targets");
        targets.visit(null, spec.target.replace('/', '.'));
        targets.visitEnd();
        mixin.visit("remap", false);
        mixin.visitEnd();
        MethodVisitor ctor = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        ctor.visitCode();
        ctor.visitVarInsn(ALOAD, 0);
        ctor.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        ctor.visitInsn(RETURN);
        ctor.visitMaxs(0, 0);
        ctor.visitEnd();
        MethodVisitor mv = cw.visitMethod(ACC_PRIVATE | (spec.isStatic ? ACC_STATIC : 0), "bedrock$compareMaterial", COMPARE, null, null);
        AnnotationVisitor redirect = mv.visitAnnotation("Lorg/spongepowered/asm/mixin/injection/Redirect;", true);
        AnnotationVisitor methods = redirect.visitArray("method");
        methods.visit(null, spec.method + spec.descriptor);
        methods.visitEnd();
        AnnotationVisitor at = redirect.visitAnnotation("at", "Lorg/spongepowered/asm/mixin/injection/At;");
        at.visit("value", "INVOKE");
        at.visit("target", "L" + ITEM_STACK + ";isSameItemSameComponents" + COMPARE);
        at.visit("remap", false);
        at.visitEnd();
        redirect.visit("remap", false);
        redirect.visit("require", 1);
        redirect.visit("allow", 1);
        redirect.visitEnd();
        mv.visitCode();
        int first = spec.isStatic ? 0 : 1;
        mv.visitVarInsn(ALOAD, first);
        mv.visitVarInsn(ALOAD, first + 1);
        mv.visitMethodInsn(INVOKESTATIC, ITEM_STACK, "isSameItem", COMPARE, false);
        Label sameItem = new Label();
        mv.visitJumpInsn(IFNE, sameItem);
        mv.visitInsn(ICONST_0);
        mv.visitInsn(IRETURN);
        mv.visitLabel(sameItem);
        mv.visitFrame(F_SAME, 0, null, 0, null);
        mv.visitVarInsn(ALOAD, first);
        mv.visitMethodInsn(INVOKESTATIC, HELPER, "normalizeCopy", "(" + STACK + ")" + STACK, false);
        mv.visitVarInsn(ALOAD, first + 1);
        mv.visitMethodInsn(INVOKESTATIC, HELPER, "normalizeCopy", "(" + STACK + ")" + STACK, false);
        mv.visitMethodInsn(INVOKESTATIC, ITEM_STACK, "isSameItemSameComponents", COMPARE, false);
        mv.visitInsn(IRETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
        cw.visitEnd();
        return cw.toByteArray();
    }

    public static void main(String[] args) throws Exception {
        require(args.length == 2, "Usage: BuildMaterialMixins EXACT_EPF_JAR OUTPUT_CLASSES");
        Path jar = Path.of(args[0]);
        require(sha(Files.readAllBytes(jar)).equals(EXPECTED_EPF), "Unexpected EasyPlaceFix bytes");
        try (ZipFile zip = new ZipFile(jar.toFile())) {
            for (Spec spec : SPECS) {
                byte[] targetBytes = zip.getInputStream(zip.getEntry(spec.target + ".class")).readAllBytes();
                ClassNode node = new ClassNode();
                new ClassReader(targetBytes).accept(node, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
                MethodNode target = node.methods.stream().filter(m -> m.name.equals(spec.method) && m.desc.equals(spec.descriptor)).findFirst().orElseThrow();
                require(((target.access & ACC_STATIC) != 0) == spec.isStatic, "Target staticness changed");
                int calls = 0;
                for (var insn : target.instructions) {
                    if (insn instanceof MethodInsnNode method && method.getOpcode() == INVOKESTATIC && method.owner.equals(ITEM_STACK) && method.name.equals("isSameItemSameComponents") && method.desc.equals(COMPARE)) calls++;
                }
                require(calls == 1, "Expected exactly one comparison target, found " + calls);
                byte[] emitted = emit(spec);
                ClassNode candidate = new ClassNode();
                new ClassReader(emitted).accept(candidate, 0);
                require(candidate.version == V25 && candidate.fields.isEmpty() && candidate.methods.size() == 2, "Unexpected output class structure");
                MethodNode handler = candidate.methods.stream().filter(m -> m.name.equals("bedrock$compareMaterial")).findFirst().orElseThrow();
                int normalizers = 0, compares = 0, identities = 0;
                for (var insn : handler.instructions) {
                    if (insn instanceof MethodInsnNode method) {
                        require(method.getOpcode() == INVOKESTATIC, "Unexpected invocation in comparison handler");
                        if (method.owner.equals(HELPER) && method.name.equals("normalizeCopy")) normalizers++;
                        else if (method.owner.equals(ITEM_STACK) && method.name.equals("isSameItemSameComponents")) compares++;
                        else if (method.owner.equals(ITEM_STACK) && method.name.equals("isSameItem")) identities++;
                        else throw new IllegalStateException("Unexpected comparison dependency");
                    }
                }
                require(normalizers == 2 && compares == 1 && identities == 1, "Incorrect comparison wrapper");
                Path output = Path.of(args[1]).resolve(candidate.name + ".class");
                Files.createDirectories(output.getParent());
                Files.write(output, emitted);
                System.out.println(spec.mixin + " target_sha256=" + sha(targetBytes) + " callsites=" + calls + " class_sha256=" + sha(emitted));
            }
        }
    }
}
