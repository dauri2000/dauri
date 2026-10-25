# ABI y lifecycle comprobados para IF49

El JAR completo IF48 contiene exactamente la misma clase de pantalla que IF34 (SHA256 `8ada62b5d544a9c43773817edbf3c69f357a55cc833d5bbc89571d548d3fc742`). `class-comparison.json` fija las nueve clases de UI leídas para el análisis.

Todas las llamadas y campos externos del helper nuevo ya aparecen en el bytecode original de esa clase: `ItemStack.matches(ItemStack,ItemStack):boolean`, `Slot.getItem():ItemStack`, `AbstractContainerMenu.getCarried():ItemStack`, `slots:NonNullList` y sus `size/get`, `List.size/get`, `Component.getString():String`, `Objects.equals`, `NativeUiImages.revision():long`, y los campos heredados `font/width/height/menu/title`. No se introduce una nueva API externa de Minecraft.

El propio `BedrockNativeContainerScreen.nativeViewport()` de IF48 usa `NativeUiViewport.currentJavaScale()`. El mixin existente `MixinNativeUiViewport` modifica los dos argumentos de `Screen.init(II)` y `resize(II)` para usar `nativeViewport().width()/height()`. Por tanto el fingerprint compara las mismas unidades y deja la ruta original si cambian. El parche no altera ese mixin ni el cálculo de escala.

Referencias adicionales de fuente26.3:

- NeoForge commit `090aba227fefe4bf0f5eaf5834c68851498c1040`, `patches/net/minecraft/client/gui/screens/Screen.java.patch`: contexto de `init(int,int)` fija width/height y llama al `init()` protegido cuando la pantalla aún no está inicializada.
- El mismo commit, `patches/net/minecraft/client/gui/Gui.java.patch`: `setScreen` instala la pantalla y llama `added`; el parche parcial no muestra por sí solo todo el cuerpo de inicialización de vanilla.
- Fabric API rama26.3, `fabric-screen-api-v1/.../ScreenMixin.java`: inyecciones HEAD/TAIL sobre `init(II)V`. `GuiMixin.java` intercepta `Gui.setScreen` y advierte que se debe llamar en el hilo de render.

Las referencias se conservan para análisis, no son una ejecución del cliente. No se obtuvo un cliente26.3 completo ni el motor de transformación Mixin.
