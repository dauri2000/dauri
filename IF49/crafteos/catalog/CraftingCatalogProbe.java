package net.raphimc.viabedrock.protocol.storage;

import com.viaversion.viaversion.libs.gson.*;
import net.raphimc.viabedrock.protocol.model.BedrockItem;
import net.raphimc.viabedrock.protocol.model.recipe.*;
import java.nio.file.*;
import java.util.*;
import java.util.function.BiPredicate;

/**
 * Runs shipped IF48 shape/assignment algorithms on each official static recipe.
 * The predicate is an explicit name/aux/tag fixture boundary: IDs are symbolic,
 * not the IDs or recipe UUIDs announced by the user's server. No game classes.
 */
public final class CraftingCatalogProbe {
    static final Map<String,Integer> IDS = new LinkedHashMap<>();
    static final Map<Integer,String> NAMES = new HashMap<>();
    static final Map<String,Set<String>> TAGS = new HashMap<>();
    static final int NOISE=63000;
    static int assertions;
    static String current;

    static int id(String name) {
        return IDS.computeIfAbsent(name,key->{int id=IDS.size()+1;NAMES.put(id,key);return id;});
    }
    static void require(boolean condition,String check) {
        assertions++;
        if(!condition)throw new AssertionError(current+": "+check);
    }
    static ItemDescriptor descriptor(JsonObject json) {
        int amount=json.has("count")?json.get("count").getAsInt():1;
        if(json.has("item"))return new ItemDescriptor.ItemNameDescriptor(json.get("item").getAsString(),json.has("data")?json.get("data").getAsInt():0,amount);
        if(json.has("tag"))return new ItemDescriptor.ItemTagDescriptor(json.get("tag").getAsString(),amount);
        throw new IllegalArgumentException("Unrepresented descriptor: "+json);
    }
    static boolean matches(ItemDescriptor descriptor,BedrockItem item) {
        if(descriptor instanceof ItemDescriptor.InvalidDescriptor)return item.isEmpty();
        if(item.isEmpty()||item.amount()<descriptor.amount())return false;
        if(descriptor instanceof ItemDescriptor.ItemNameDescriptor name)
            return Objects.equals(NAMES.get(item.identifier()),name.fullName())&&ItemDescriptor.matchesAuxValue(name.auxValue(),item);
        if(descriptor instanceof ItemDescriptor.ItemTagDescriptor tag)
            return NAMES.containsKey(item.identifier())&&TAGS.getOrDefault(tag.itemTag(),Set.of()).contains(NAMES.get(item.identifier()));
        throw new IllegalArgumentException("Unrepresented descriptor type: "+descriptor);
    }
    static BedrockItem sample(ItemDescriptor descriptor) {
        if(descriptor instanceof ItemDescriptor.InvalidDescriptor)return BedrockItem.empty();
        if(descriptor instanceof ItemDescriptor.ItemNameDescriptor name)
            return new BedrockItem(id(name.fullName()),(short)(name.auxValue()<0||name.auxValue()==32767?0:name.auxValue()),(byte)name.amount());
        if(descriptor instanceof ItemDescriptor.ItemTagDescriptor tag) {
            var members=TAGS.get(tag.itemTag());
            if(members==null||members.isEmpty())throw new IllegalArgumentException("No bundled members for "+tag.itemTag());
            return new BedrockItem(id(members.stream().sorted().findFirst().orElseThrow()),(short)0,(byte)tag.amount());
        }
        throw new IllegalArgumentException("No sample "+descriptor);
    }
    static BedrockItem[] copy(BedrockItem[] grid) {
        return Arrays.stream(grid).map(BedrockItem::copy).toArray(BedrockItem[]::new);
    }
    static void checkResult(BedrockItem[] before,BedrockItem[] after,int[] result,int expectedCost,String check) {
        require(Arrays.equals(before,after),check+" mutated inputs");
        require(result!=null,check+" unexpectedly rejected source-derived grid");
        require(Arrays.stream(result).sum()==expectedCost,check+" incorrect required-quantity total");
    }
    static BedrockItem[] shapedGrid(ItemDescriptor[][] pattern,int size,int ox,int oy,boolean mirror,int quantity) {
        BedrockItem[] grid=BedrockItem.emptyArray(size*size);
        int width=pattern[0].length;
        for(int y=0;y<pattern.length;y++)for(int x=0;x<width;x++) {
            BedrockItem item=sample(pattern[y][mirror?width-1-x:x]);
            if(!item.isEmpty()&&quantity>0)item.setAmount(quantity);
            grid[(y+oy)*size+x+ox]=item;
        }
        return grid;
    }
    static JsonObject testShaped(JsonObject row) {
        JsonObject source=row.getAsJsonObject("recipe");
        JsonArray strings=source.getAsJsonArray("pattern");
        int height=strings.size(),width=0;
        for(JsonElement line:strings)width=Math.max(width,line.getAsString().length());
        ItemDescriptor[][] pattern=new ItemDescriptor[height][width];
        int cost=0,paddedRows=0;
        for(int y=0;y<height;y++) {
            String line=strings.get(y).getAsString();
            if(line.length()<width){line=line+" ".repeat(width-line.length());paddedRows++;}
            for(int x=0;x<width;x++) {
                char c=line.charAt(x);
                pattern[y][x]=c==' '?new ItemDescriptor.InvalidDescriptor():descriptor(source.getAsJsonObject("key").getAsJsonObject(String.valueOf(c)));
                if(c!=' ')cost+=pattern[y][x].amount();
            }
        }
        ShapedRecipe recipe=new ShapedRecipe(current,UUID.nameUUIDFromBytes(current.getBytes(java.nio.charset.StandardCharsets.UTF_8)),"fixture",0,pattern,List.of(new BedrockItem(62000)),true);
        int accepted=0;
        for(int size:new int[]{2,3}) {
            if(width>size||height>size) {
                require(CraftingDataTracker.matchShaped(recipe,BedrockItem.emptyArray(size*size),size,CraftingCatalogProbe::matches)==null,"oversize pattern not rejected in "+size+"x"+size);
                continue;
            }
            for(int y=0;y<=size-height;y++)for(int x=0;x<=size-width;x++)for(boolean mirror:new boolean[]{false,true}) {
                BedrockItem[] grid=shapedGrid(pattern,size,x,y,mirror,0),before=copy(grid);
                checkResult(before,grid,CraftingDataTracker.matchShaped(recipe,grid,size,CraftingCatalogProbe::matches),cost,"shape/offset/mirror "+size+"x"+size);
                accepted++;
            }
        }
        BedrockItem[] correct=shapedGrid(pattern,3,0,0,false,0);
        int nonempty=0;while(correct[nonempty].isEmpty())nonempty++;
        BedrockItem[] missing=copy(correct);missing[nonempty]=BedrockItem.empty();
        require(CraftingDataTracker.matchShaped(recipe,missing,3,CraftingCatalogProbe::matches)==null,"missing ingredient accepted");
        BedrockItem[] wrong=copy(correct);wrong[nonempty]=new BedrockItem(NOISE);
        require(CraftingDataTracker.matchShaped(recipe,wrong,3,CraftingCatalogProbe::matches)==null,"unrelated ingredient accepted");
        int empty=0;while(empty<correct.length&&!correct[empty].isEmpty())empty++;
        if(empty<correct.length) {
            BedrockItem[] extra=copy(correct);extra[empty]=new BedrockItem(NOISE);
            require(CraftingDataTracker.matchShaped(recipe,extra,3,CraftingCatalogProbe::matches)==null,"extraneous occupied cell accepted");
        }
        BedrockItem[] stacked=shapedGrid(pattern,3,0,0,false,64),before=copy(stacked);
        checkResult(before,stacked,CraftingDataTracker.matchShaped(recipe,stacked,3,CraftingCatalogProbe::matches),cost,"stack64 shape");
        JsonObject result=new JsonObject();result.addProperty("source_shape_width",width);result.addProperty("source_shape_height",height);
        result.addProperty("required_units",cost);result.addProperty("accepted_fixture_offsets_and_mirrors",accepted);
        result.addProperty("source_rows_right_padded_to_matrix",paddedRows);
        result.addProperty("fits_2x2_by_source_shape",width<=2&&height<=2);
        result.addProperty("mirror_contract_boundary","model mirrored=true supplied by fixture; no native per-recipe mirror flag captured");
        return result;
    }
    static JsonObject testShapeless(JsonObject row) {
        List<ItemDescriptor> ingredients=new ArrayList<>();
        for(JsonElement raw:row.getAsJsonObject("recipe").getAsJsonArray("ingredients"))ingredients.add(descriptor(raw.getAsJsonObject()));
        int cost=ingredients.stream().mapToInt(ItemDescriptor::amount).sum();
        List<ItemDescriptor> expanded=ShapelessRecipe.expandIngredients(ingredients,9);
        require(expanded!=null,"official static source cannot expand into nine cells");
        int accepted=0;
        for(int size:new int[]{2,3}) {
            if(expanded.size()>size*size)continue;
            for(boolean reverse:new boolean[]{false,true}) {
                BedrockItem[] grid=BedrockItem.emptyArray(size*size);
                for(int i=0;i<expanded.size();i++)grid[reverse?size*size-1-i:i]=sample(expanded.get(i));
                BedrockItem[] before=copy(grid);
                checkResult(before,grid,CraftingDataTracker.matchShapeless(ingredients,grid,CraftingCatalogProbe::matches),cost,"shapeless expanded/order "+size+"x"+size);
                accepted++;
            }
        }
        BedrockItem[] grid=BedrockItem.emptyArray(9);
        for(int i=0;i<expanded.size();i++)grid[i]=sample(expanded.get(i));
        BedrockItem[] missing=copy(grid);missing[0]=BedrockItem.empty();
        require(CraftingDataTracker.matchShapeless(ingredients,missing,CraftingCatalogProbe::matches)==null,"shapeless missing ingredient accepted");
        BedrockItem[] wrong=copy(grid);wrong[0]=new BedrockItem(NOISE);
        require(CraftingDataTracker.matchShapeless(ingredients,wrong,CraftingCatalogProbe::matches)==null,"shapeless unrelated ingredient accepted");
        if(expanded.size()<9) {
            BedrockItem[] extra=copy(grid);extra[expanded.size()]=new BedrockItem(NOISE);
            require(CraftingDataTracker.matchShapeless(ingredients,extra,CraftingCatalogProbe::matches)==null,"shapeless extraneous cell accepted");
        }
        BedrockItem[] stacked=copy(grid);for(BedrockItem item:stacked)if(!item.isEmpty())item.setAmount(64);
        BedrockItem[] before=copy(stacked);
        checkResult(before,stacked,CraftingDataTracker.matchShapeless(ingredients,stacked,CraftingCatalogProbe::matches),cost,"stack64 shapeless");
        JsonObject result=new JsonObject();result.addProperty("required_units",cost);
        result.addProperty("source_descriptor_entries",ingredients.size());result.addProperty("accepted_fixture_orders",accepted);
        result.addProperty("fits_2x2_when_expanded",expanded.size()<=4);
        if(cost>ingredients.size()&&ingredients.size()<=4) {
            BedrockItem[] compressed=BedrockItem.emptyArray(4);
            for(int i=0;i<ingredients.size();i++)compressed[i]=sample(ingredients.get(i));
            int[] observed=CraftingDataTracker.matchShapeless(ingredients,compressed,CraftingCatalogProbe::matches);
            result.addProperty("observed_count_compressed_2x2_match",observed!=null);
            result.addProperty("compressed_grid_contract","observation only; no assertion that server accepts/rejects this layout");
        }
        return result;
    }
    public static void main(String[] args) throws Exception {
        JsonObject input=JsonParser.parseString(Files.readString(Path.of(args[0]))).getAsJsonObject();
        for(var e:input.getAsJsonObject("core_item_tags").entrySet()) {
            Set<String> names=new TreeSet<>();for(JsonElement item:e.getValue().getAsJsonArray())names.add(item.getAsString());TAGS.put(e.getKey(),names);
        }
        JsonArray rows=new JsonArray();int tested=0,unmatched=0;
        for(JsonElement element:input.getAsJsonArray("recipes")) {
            JsonObject source=element.getAsJsonObject(),out=new JsonObject();current=source.get("identifier").getAsString();
            out.addProperty("path",source.get("path").getAsString());out.addProperty("identifier",current);out.add("type",source.get("type"));
            if(!source.get("static_matcher_applicable").getAsBoolean()) {
                out.addProperty("result","NOT_RUN_OTHER_STATION_FAMILY");out.addProperty("matcher_assertions",0);unmatched++;
            } else {
                int start=assertions;
                try {
                    JsonObject checks=source.get("type").getAsString().endsWith("_shaped")?testShaped(source):testShapeless(source);
                    out.addProperty("result","PASS_SOURCE_DERIVED_SHAPE_FIXTURES_ONLY");out.add("checks",checks);tested++;
                } catch(Throwable failure) {
                    out.addProperty("result","FIXTURE_CHECK_FAILED");out.addProperty("failure",failure.toString());
                }
                out.addProperty("matcher_assertions",assertions-start);
            }
            out.addProperty("server_accepted",false);rows.add(out);
        }
        JsonObject report=new JsonObject();report.addProperty("baseline","IF48 shipped core actual matcher/model methods");
        report.addProperty("boundary","Official static source adapted into real recipe models; symbolic name/aux/tag predicate using shipped tag-membership JSON; not actual ItemDescriptor.matchesItem runtime/native aliases; synthetic recipe UUIDs not native recipe IDs; mirrored=true fixture, not observed native per-recipe flag");
        report.addProperty("server_crafting_data_capture",false);report.addProperty("live_gameplay_executed",false);
        report.addProperty("recipes_passed_shape_fixtures",tested);report.addProperty("other_station_recipes_not_shape_tested",unmatched);report.addProperty("assertions",assertions);report.add("rows",rows);
        Files.writeString(Path.of(args[1]),new GsonBuilder().setPrettyPrinting().create().toJson(report)+"\n");
        System.out.println("Source recipe files="+rows.size()+", static matcher rows passed="+tested+", other station rows="+unmatched+", actual-matcher assertions="+assertions);
        for(JsonElement e:rows)if(e.getAsJsonObject().get("result").getAsString().equals("FIXTURE_CHECK_FAILED"))System.out.println(e);
    }
}
