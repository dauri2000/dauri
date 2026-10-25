from collections import Counter
from pathlib import Path
import csv
import hashlib
import json
import re
import zipfile

ROOT=Path(__file__).parent
OFFICIAL=json.loads((ROOT/'official-catalog-input.json').read_text())
NATIVE=json.loads((ROOT/'native-reference-input.json').read_text())
OFFICIAL_RESULTS={row['path']:row for row in json.loads((ROOT/'matcher-results.json').read_text())['rows']}
NATIVE_RESULTS={row['path']:row for row in json.loads((ROOT/'native-matcher-results.json').read_text())['rows']}
MULTI=json.loads((ROOT.parent/'fireworks/reference/multi-uuid-semantics.json').read_text())
MULTI_BY_UUID={row['uuid']:row for row in MULTI['rows']}
(ROOT/'MULTI-SEMANTICS.json').write_text(json.dumps(MULTI,ensure_ascii=False,indent=2)+'\n')
with zipfile.ZipFile(ROOT/'core-if48.jar') as jar:
    RESOURCES={name:jar.read('assets/viabedrock/data/'+name) for name in [
        'bedrock/runtime_item_states.json','bedrock/item_tags.json','custom/item_mappings.json','java/via_mappings.json']}
    RUNTIME={item['name']:item['id'] for item in json.loads(RESOURCES['bedrock/runtime_item_states.json'])}
    ITEM_MAPPINGS=json.loads(RESOURCES['custom/item_mappings.json'])
    VIA=json.loads(RESOURCES['java/via_mappings.json'])
    JAVA_ITEMS={item if item.startswith('minecraft:') else 'minecraft:'+item for item in VIA['items']}
    TAGS=json.loads(RESOURCES['bedrock/item_tags.json'])
    CLASSES={name:hashlib.sha256(jar.read(name)).hexdigest() for name in jar.namelist() if name.endswith('.class') and any(
        token in name for token in ['Crafting','Recipe','SharedCraftingGrid','InventoryRequestTracker','InventoryPackets','FurnaceContainer','SmithingContainer','BrewingPropertyStorage','CartographyRecipes','LoomContainer','LoomPatterns','WorkstationResults'])}

def targets(value):
    output=set()
    if isinstance(value,dict):
        for key,item in value.items():
            if key=='java_id':output.add(item)
            elif isinstance(item,(dict,list)):output.update(targets(item))
    elif isinstance(value,list):
        for item in value:output.update(targets(item))
    return output

def native_names(value):
    names=set();tags=set()
    if isinstance(value,list):
        for item in value:
            more,more_tags=native_names(item);names.update(more);tags.update(more_tags)
    elif isinstance(value,dict):
        for key,item in value.items():
            if key in ['itemId','reagentId','inputId','outputId'] or (key=='id' and isinstance(item,str) and item.startswith('minecraft:') and 'count' in value):names.add(item)
            if key=='itemTag':tags.add(item)
            if isinstance(item,(dict,list)):
                more,more_tags=native_names(item);names.update(more);tags.update(more_tags)
    return names,tags

def source_names(value):
    names=set();tags=set()
    if isinstance(value,list):
        for item in value:
            more,more_tags=source_names(item);names.update(more);tags.update(more_tags)
    elif isinstance(value,dict):
        for key,item in value.items():
            if key=='item' and isinstance(item,str):names.add(item)
            elif key in ['input','output','result','template','base','addition','reagent'] and isinstance(item,str) and not item.startswith('minecraft:potion_type:'):names.add(item)
            elif key=='tag' and isinstance(item,str):tags.add(item)
            if isinstance(item,(dict,list)) and key not in ['unlock','description']:
                more,more_tags=source_names(item);names.update(more);tags.update(more_tags)
    return names,tags

def station_route(tags,typ):
    if typ in ['MULTI','native_reference:MULTI']:
        return 'CraftingDataReader.read almacena UUID+networkId como MultiRecipe; la receta no contiene patrón ni resultado estático. Requiere implementación específica; no se prueba con matchShaped/matchShapeless.'
    if typ.endswith('smithing_trim') or typ.endswith('SMITHING_TRIM'):
        return 'CraftingDataReader.readSmithing → SmithingRecipe; CraftingDataTracker.matchesSmithing compara template/base/addition; SmithingContainer y WorkstationItemData.applyTrim generan la vista de resultado. Aceptación nativa no ejecutada.'
    if typ.endswith('smithing_transform') or typ.endswith('SMITHING_TRANSFORM'):
        return 'CraftingDataReader.readSmithing → SmithingRecipe; CraftingDataTracker.matchesSmithing y SmithingContainer usan template/base/addition y resultado recibido. Transacción y consumo reales pendientes.'
    if 'brewing_stand' in tags or 'brewing' in typ:
        return 'CraftingDataReader.read carga mezclas en BrewingPropertyStorage.add; el servidor ejecuta la preparación. BrewingStandContainer traduce slots y propiedades; no es un match de cuadrícula.'
    if typ.endswith('furnace') or any(tag in tags for tag in ['furnace','blast_furnace','smoker','campfire','soul_campfire']):
        return 'FurnaceContainer.acceptsRecipe exige ShapelessRecipe de un ingrediente y tag de estación; FurnaceRecipePlacement coloca entradas. Campfire/soul_campfire siguen interacción de bloque y estado nativos, sin certificación por cuadrícula.'
    if 'stonecutter' in tags:
        return 'StonecutterContainer.updateRecipes filtra ShapelessRecipe, tag stonecutter y un ingrediente; handleButtonClick selecciona y handleClick solicita networkId. El probe de forma no ejecuta esta estación.'
    if 'cartography_table' in tags:
        return 'CartographyContainer/CartographyRecipes.select mantienen operaciones específicas y sus MultiRecipe UUID; no se presupone que una receta de cuadrícula valide mapas o su metadata.'
    if 'deprecated' in tags:
        return 'Receta marcada deprecated en la referencia. Se ensaya su patrón por cobertura; no se afirma que sea desbloqueada, visible o utilizable en una sesión actual.'
    return 'CraftingDataReader → CraftingDataTracker.getRecipeMatch; InventoryContainer 2×2 o CraftingTableContainer 3×3; CraftingPlacement y CraftingOutputPlan se usan después de identificar receta. El probe solo ejecuta matchShaped/matchShapeless.'

def common_pending(tags,typ):
    if 'MULTI' in typ:return 'Ejecutar receta dinámica con UUID anunciado en esa conexión, conservar NBT y validar consumo/output con respuesta del servidor.'
    if 'brewing' in typ:return 'Preparar la mezcla real; comprobar combustible, tiempo, frascos, efecto y cantidades autorizados.'
    if 'smithing' in typ.lower():return 'Aplicar template/base/addition en la estación; comprobar consumo, resultado y conservación de datos.'
    if any(tag in tags for tag in ['furnace','blast_furnace','smoker','campfire','soul_campfire']):return 'Cocinar/fundir con estación y combustible válidos; verificar tiempo, resultado y XP recibida.'
    if 'stonecutter' in tags:return 'Seleccionar y fabricar en cortapiedras; comprobar resultado, coste y transferencias reales.'
    if 'cartography_table' in tags:return 'Completar operación de cartografía y confirmar mapa, nombre/NBT, consumos y cantidades.'
    if 'deprecated' in tags:return 'Confirmar si la receta está anunciada y utilizable en el servidor; no habilitarla por existir en la referencia.'
    return 'Usar la receta realmente anunciada; comprobar preview, pickup/shift-clic, apilado, recipientes devueltos, NBT y respuesta nativa de aceptación/rechazo.'

def compact(value):
    return json.dumps(value,ensure_ascii=False,separators=(',',':'))

def make_ledger(source,results,origin):
    ledger=[]
    for index,row in enumerate(source['recipes']):
        probe=results[row['path']];typ=row.get('native_reference_type',row['type']);tags=row['tags']
        if origin=='native':
            names,tag_names=native_names(row['native_reference'])
            output=row['native_reference'].get('output',row['native_reference'].get('result'))
        else:names,tag_names=source_names(row['recipe']);output=row['recipe'].get('result',row['recipe'].get('output'))
        absent=[name for name in names if name not in RUNTIME or name not in ITEM_MAPPINGS]
        unresolved_tags=sorted(tag_names-set(TAGS))
        if probe['result']=='PASS_SOURCE_DERIVED_SHAPE_FIXTURES_ONLY':
            status='MATCH DE FORMA/CANTIDAD COMPROBADO CON FRONTERAS EXPLÍCITAS'
            tested=f"{probe['matcher_assertions']} afirmaciones del matcher real; {compact(probe.get('checks',{}))}"
        else:status='OTRA FAMILIA: SIN PRUEBA DEL MATCHER DE CUADRÍCULA';tested='No se ejecutó matchShaped/matchShapeless para esta familia; se identifica la ruta específica.'
        semantic=None
        if origin=='native' and typ=='MULTI':
            semantic=MULTI_BY_UUID.get(row['public_reference_uuid'])
            status='RECETA MULTI: SEMÁNTICA IDENTIFICADA, EJECUCIÓN NO PROBADA' if semantic and semantic['semantic_verified'] else 'RECETA MULTI: SEMÁNTICA DESCONOCIDA, SIN SUPOSICIONES'
            tested='UUID público identificado como '+semantic['semantic_constant'] if semantic else 'UUID sin semántica identificada'
        sensitivity=[]
        if typ=='USER_DATA_SHAPELESS':sensitivity.append('El tipo USER_DATA hace que la identidad/NBT del resultado sea parte crítica; pasar el matcher no valida conservar contenido, colores, texto ni datos anidados.')
        if 'observed_count_compressed_2x2_match' in probe.get('checks',{}):sensitivity.append('Observación acotada: matcher acepta layout comprimido en 2×2; sin contrato nativo de aceptación/rechazo para este layout.')
        if absent and origin=='official':sensitivity.append('Nombres de fuente pueden omitir namespace o usar alias/aux históricos; no equivalen directamente a los nombres de red. No se declara pérdida de receta por esa ausencia literal.')
        if tags==['deprecated']:sensitivity.append('Solo referencia deprecated; no activar ni mostrar sin anuncio/estado de desbloqueo nativo.')
        entry={
            'id':f'{origin}-{index:05d}','recipe_identifier':row['identifier'],'source_entry':row['path'],
            'source_type':typ,'stations':tags,'output_reference':output,'result_scope':status,
            'concrete_evidence':tested,'implementation_route':station_route(tags,typ),
            'native_item_names_checked':sorted(names),'item_names_absent_literal_runtime_or_mapping':absent,
            'ingredient_tags_checked':sorted(tag_names),'missing_tags':unresolved_tags,
            'notes':sensitivity,'live_server_pending':common_pending(tags,typ),'server_gameplay_verified':False,
            'source_entry_sha256':row.get('source_sha256',row.get('canonical_entry_sha256')),
        }
        if origin=='native':
            entry['public_reference_uuid']=row['public_reference_uuid'];entry['public_network_id_example_only']=row['public_reference_network_id']
            entry['network_id_is_not_a_production_constant']=True
        if semantic:entry['multi_semantic_reference']=semantic
        ledger.append(entry)
    return ledger

official=make_ledger(OFFICIAL,OFFICIAL_RESULTS,'official')
native=make_ledger(NATIVE,NATIVE_RESULTS,'native')
brewing=[]
for kind,values in [('potionMixes',NATIVE['public_potion_mixes']),('containerMixes',NATIVE['public_container_mixes'])]:
    for index,value in enumerate(values):
        names,tags=native_names(value)
        brewing.append({'id':f'native-{kind}-{index:03d}','reference':value,'item_names_checked':sorted(names),
            'mapping_data_present':all(name in RUNTIME and name in ITEM_MAPPINGS for name in names),
            'concrete_evidence':'Entrada pública identificada por input/reagent/output y aux cuando existen; CraftingDataReader.read → BrewingPropertyStorage.add(inputID,data,reagentID,data,outputID,data).',
            'result_scope':'DATOS Y RUTA REVISADOS; PREPARACIÓN NO EJECUTADA','live_server_pending':'Preparar mezcla real y confirmar recipientes/ingrediente/efecto/tiempo/combustible y consumo.',
            'server_gameplay_verified':False})

all_native_names=set();all_native_tags=set()
for row in native:
    all_native_names.update(row['native_item_names_checked']);all_native_tags.update(row['ingredient_tags_checked'])
for row in brewing:all_native_names.update(row['item_names_checked'])
item_ledger=[]
for name in sorted(all_native_names):
    dest=targets(ITEM_MAPPINGS.get(name,{}))
    item_ledger.append({'identifier':name,'runtime_declared':name in RUNTIME,'mapping_declared':name in ITEM_MAPPINGS,
                        'java_targets':sorted(dest),'all_targets_exist_in_included_java_registry':all(value in JAVA_ITEMS for value in dest),
                        'scope':'RESOURCE_DECLARATION_ONLY_NOT_ITEM_USE_OR_NBT_RESULT_TEST'})
assert len(item_ledger)==1307
assert all(row['runtime_declared'] and row['mapping_declared'] and row['all_targets_exist_in_included_java_registry'] for row in item_ledger)
assert len(all_native_tags)==17 and all(tag in TAGS for tag in all_native_tags)
(ROOT/'native-item-mapping-ledger.json').write_text(json.dumps({'items':item_ledger,'tags':sorted(all_native_tags),'resources_sha256':{k:hashlib.sha256(v).hexdigest() for k,v in RESOURCES.items()},'not_a_recipe_execution_report':True},ensure_ascii=False,indent=2)+'\n')

def write_ledger(name,rows,description):
    (ROOT/(name+'.json')).write_text(json.dumps({'scope':description,'rows':rows},ensure_ascii=False,indent=2)+'\n')
    with (ROOT/(name+'.csv')).open('w',newline='') as file:
        fields=sorted({key for row in rows for key in row})
        writer=csv.DictWriter(file,fieldnames=fields);writer.writeheader()
        for row in rows:writer.writerow({key:compact(value) if isinstance(value,(list,dict)) else value for key,value in row.items()})

write_ledger('CATALOGO-OFICIAL',official,'1892 official JSON entries;1655 static shape fixtures; other stations explicitly not shape-tested; not actual server recipe list')
write_ledger('CATALOGO-NATIVO-PUBLICO',native,'4768 public reference recipe entries protocol2193;4741 static shape fixtures;14Multi+13smithing not shape-tested; not user session')
write_ledger('MEZCLAS-NATIVAS-PUBLICAS',brewing,'210 potion mixes and2container mixes; route/data only, no brewing gameplay')

route_rows=[
 ('Datos recibidos','CraftingDataReader.read','Lee bloques shaped/shapeless/Multi/userdata/chemistry/smithing y mezclas; readUnlocking conserva la política de desbloqueo. No captura de CRAFTING_DATA del usuario.'),
 ('Actualización del libro','InventoryPackets CRAFTING_DATA/UNLOCKED_RECIPES; CraftingDataTracker.updateCraftingDataList/sendJavaUpdateRecipes','UUID/redID anunciados se guardan por conexión; las entradas públicas son ejemplos, no constantes para producción.'),
 ('Libro y ghost recipe','CraftingDataTracker.recipeBookDisplays/writeRecipeDisplay/sendJavaGhostRecipe','Solo se incluyen estaciones/tipos soportados y desbloqueados; mapping/serializer presente no implementa receta especial.'),
 ('Cuadrícula 2×2','CraftingDataTracker.getRecipeMatch; InventoryContainer; SharedCraftingGrid','Grid side2; HUD28–31. El bulk prueba el método de matching con fixtures; no la transacción completa del inventario.'),
 ('Mesa 3×3','CraftingDataTracker.getRecipeMatch; CraftingTableContainer','Grid side3; slots nativos32–40, preview50. Preservar correcciones compartidas anteriores.'),
 ('Con forma','CraftingDataTracker.matchShaped/matchPattern','Offsets/mirror habilitado en fixture, huecos, material ajeno y cantidades. La bandera mirror real viene del anuncio nativo y no se capturó aquí.'),
 ('Sin forma','CraftingDataTracker.matchShapeless/assignIngredient; ShapelessRecipe.expandIngredients','Asignación de ingredientes, orden invertido, cantidades, inputs ausentes/extra y stacks64; no validación de UUID/resultNBT.'),
 ('USER_DATA_SHAPELESS','CraftingDataTracker.getRecipeMatch','Usa matcher shapeless;1456 entradas públicas cubiertas por forma. La conservación de datos del resultado requiere auditoría específica.'),
 ('Descriptores','ItemDescriptor.InvalidDescriptor/ItemNameDescriptor/ItemTagDescriptor/MolangDescriptor; NetworkItemDescriptorType','El predicate del bulk aisla nombre/aux/tag; no sustituye una prueba de matchesItem en conexión real. MOLANG no aparece en los fixtures de estas fuentes.'),
 ('Autocolocación','InventoryPackets.applyRecipePlacement; CraftingPlacement.plan/actions/apply','Ruta por displayId, estación, disponibilidad, netId y solicitudes pendientes; auditoría general del agente principal.'),
 ('Clic resultado y shift','CraftingTableContainer.handleClick; CraftingOutputPlan.pickup/plan','Pickup/QuickMove, capacidades de stack, consumo y destino se gestionan después del match; pasar forma no los certifica.'),
 ('ACK, rechazo y cierre','InventoryRequestTracker y SharedCraftingGrid','Continuación rápida debe esperar confirmaciones y conservar slots/redIDs; auditoría general del agente principal.'),
 ('Cortapiedras','StonecutterContainer.updateRecipes/handleButtonClick/handleClick','Un ingrediente, tag stonecutter, selección por receta recibida.354 entradas públicas frente356 fuentes oficiales; versiones/fuentes pueden diferir.'),
 ('Hornos','FurnaceContainer.acceptsRecipe/handleRecipePlacement; FurnaceRecipePlacement','Acepta ShapelessRecipe de1ingrediente con tag de horno correcto; servidor cocina.134furnace/62blast/9smoker entradas públicas.'),
 ('Fogatas','Interacción de bloque y actualizaciones nativas','9campfire y9soul_campfire entradas públicas; no equivalen a una UI de crafting de mesa.'),
 ('Herrería','CraftingDataReader.readSmithing; CraftingDataTracker.matchesSmithing; SmithingContainer; WorkstationItemData','12transform+1trim públicos; template/base/addition y datos resultantes, no ensayados como transacción por el bulk.'),
 ('Pociones','CraftingDataReader.read; BrewingPropertyStorage.add/write; BrewingStandContainer','210mezclas+2cambios de recipiente públicos; se revisan datos/ruta, no preparación en servidor.'),
 ('Cartografía','CartographyRecipes.select; CartographyContainer.resultActions','Cuatro UUID dedicados en cartografía; código específico distinto del matching genérico y de mapas visuales.'),
 ('Telar','LoomContainer.refreshInput/resultActions; LoomPatterns.result','Patrones/colores/NBT se tratan en ruta propia; no certificados por tener serializer Java de banner.'),
 ('Yunque y afiladora','AnvilContainer; GrindstoneContainer; WorkstationResults','Reparación/ench/coste son rutas de estación; conservar IF46. No son todas recetas de cuadrícula.'),
 ('Crafter automático','CrafterContainer; TOGGLE_CRAFTER_SLOT_REQUEST','Ejecución, redstone e inventario nativos; esta auditoría de matcher no hace funcionar el bloque automático.'),
 ('Dinámicas MULTI','MultiRecipe; familia por UUID anunciado','14UUID públicos,13semánticas identificadas y1desconocida. El matcher genérico no contiene patrón ni resultado para estas entradas.'),
]
routes=[]
for index,(family,methods,limit) in enumerate(route_rows):routes.append({'id':f'path-{index:02d}','family':family,'actual_methods':methods,'scope_and_limit':limit,'live_gameplay_verified':False})
write_ledger('RUTAS-Y-ESTACIONES',routes,'Actual shipped code paths reviewed; not end-to-end gameplay certification')

serializer_rows=[]
for index,name in enumerate(VIA['recipe_serializers']):
    serializer_rows.append({'index':index,'java_serializer':name,
        'registry_presence':True,
        'scope':'Identifier in bundled Java26.3 recipe_serializers; registry presence does not mean Bedrock translation or dynamic result implementation exists.',
        'native_counterpart_claimed':False})
(ROOT/'JAVA-SERIALIZERS.json').write_text(json.dumps({'rows':serializer_rows,'count':len(serializer_rows)},indent=2)+'\n')
bedrock_types=[
 ('SHAPELESS',0,'CraftingDataReader.readShapeless; CraftingDataTracker.matchShapeless','Static shape fixtures executed; server packet decode/acceptance not executed.'),
 ('SHAPED',1,'CraftingDataReader.readShaped; CraftingDataTracker.matchShaped','Static shape fixtures executed; native mirror/unlock/output state not captured.'),
 ('UNKNOWN_1',2,'RecipeType enum only; no explicit read or generic match branch identified','No support claim; do not map JSON author type2 to this constant.'),
 ('UNKNOWN_2',3,'RecipeType enum only; no explicit read or generic match branch identified','No support claim; public JSON type3 means USER_DATA in its own format.'),
 ('MULTI',4,'CraftingDataReader.read constructs MultiRecipe(UUID)','No pattern/result in this model. Requires family-specific implementation.'),
 ('USER_DATA_SHAPELESS',5,'CraftingDataReader.readShapeless; CraftingDataTracker.matchShapeless','1456 public reference entries shape-tested; output metadata not certified.'),
 ('SHAPELESS_CHEMISTRY',6,'CraftingDataReader.readShapeless reads family; getRecipeMatch generic switch does not select it','Vanilla reference does not constitute Education recipe coverage.'),
 ('SHAPED_CHEMISTRY',7,'CraftingDataReader.readShaped reads family; getRecipeMatch generic switch does not select it','Vanilla reference does not constitute Education recipe coverage.'),
 ('SMITHING_TRANSFORM',8,'CraftingDataReader.readSmithing(transform=true); matchesSmithing','12 public entries inventoried, actual station transaction not executed.'),
 ('SMITHING_TRIM',9,'CraftingDataReader.readSmithing(transform=false); matchesSmithing','1 public generic trim entry inventoried; all combinations not generated/tested.'),
]
(ROOT/'BEDROCK-RECIPE-TYPES.json').write_text(json.dumps({'scope':'Numeric constants of bundled RecipeType model; not the numeric type scheme of the public JSON dump. Packet reader consumes its family-specific sections.','rows':[{'name':name,'model_value':value,'actual_path':path,'scope_and_limit':limit} for name,value,path,limit in bedrock_types]},indent=2)+'\n')
descriptors=[
 ('INVALID',0,'InvalidDescriptor.matchesItem accepts only empty item; matrix spaces represented with this real descriptor.',False),
 ('ITEM_NAME',1,'ItemNameDescriptor.matchesItem reads ItemRewriter name→ID and matchesAuxValue; fixture uses explicit symbolic name/aux predicate instead.',False),
 ('MOLANG',2,'MolangDescriptor.matchesItem uses MolangTagQuery.parse and item tags; not present in supplied static reference fixtures.',False),
 ('ITEM_TAG',3,'ItemTagDescriptor.matchesItem queries bundled tag membership through runtime name mapping; fixture uses supplied real membership JSON with symbolic names.',False),
]
(ROOT/'INGREDIENT-DESCRIPTORS.json').write_text(json.dumps({'scope':'Actual descriptor classes inventoried; bulk predicate boundary is not a live matchesItem test.','rows':[{'name':name,'model_value':value,'actual_path':path,'live_descriptor_resolution_tested':live} for name,value,path,live in descriptors]},indent=2)+'\n')

def md_table(name,rows,title):
    pages=ROOT/'pages';pages.mkdir(exist_ok=True)
    index=f'# {title}\n\nUna fila por entrada; páginas de hasta350recetas. Pasar la forma no certifica NBT, transacciones ni aceptación del servidor.\n\n'
    index+='| Familia | Entradas | Páginas |\n|---|---:|---|\n'
    groups={}
    for row in rows:groups.setdefault(row['source_type'],[]).append(row)
    for family,entries in groups.items():
        chunks=[entries[start:start+350] for start in range(0,len(entries),350)]
        slug=re.sub('[^a-zA-Z0-9_-]+','-',family)
        links=[]
        for page_number,chunk in enumerate(chunks,1):
            file=f'{name}-{slug}-{page_number:02d}.md'
            links.append(f'[{page_number}](pages/{file})')
            text=f'# {title}: {family}, página{page_number}/{len(chunks)}\n\n[Índice completo](../{name}.md). Estas{len(chunk)}filas comprueban únicamente el alcance indicado.\n\n'
            text+='| ID | Receta | Estación | Resultado limitado | Afirmaciones | Resultado de referencia |\n|---|---|---|---|---:|---|\n'
            for row in chunk:
                result=OFFICIAL_RESULTS[row['source_entry']] if name=='CATALOGO-OFICIAL' else NATIVE_RESULTS[row['source_entry']]
                output=compact(row['output_reference'])
                if len(output)>170:output=output[:167]+'…'
                values=[row['id'],row['recipe_identifier'],','.join(row['stations']),row['result_scope'],result['matcher_assertions'],output]
                text+='| '+' | '.join(str(value).replace('|','/') for value in values)+' |\n'
            text+='\nEl JSON/CSV completo conserva evidencia, tags/nombres comprobados, ruta, sensibilidad de datos, pendientes y hash por entrada.\n'
            text=text.replace('página'+str(page_number),'página '+str(page_number)).replace('Estas'+str(len(chunk))+'filas','Estas '+str(len(chunk))+' filas')
            (pages/file).write_text(text)
        index+=f'| {family} | {len(entries)} | '+', '.join(links)+' |\n'
    index+=f'\n[JSON completo]({name}.json) · [CSV completo]({name}.csv). Las fuentes se solapan; no sumar ambos catálogos como recetas únicas.\n'
    (ROOT/(name+'.md')).write_text(index.replace('hasta350recetas','hasta 350 recetas'))
md_table('CATALOGO-OFICIAL',official,'Recetas oficiales, una por una (1892 entradas)')
md_table('CATALOGO-NATIVO-PUBLICO',native,'Recetas de referencia pública Bedrock2193, una por una (4768 entradas)')

official_probe=json.loads((ROOT/'matcher-results.json').read_text());native_probe=json.loads((ROOT/'native-matcher-results.json').read_text())
verification={
 'baseline':'IF48 full mod; core extracted without mutation','core_sha256':hashlib.sha256((ROOT/'core-if48.jar').read_bytes()).hexdigest(),
 'jdk':'25.0.5.0.2','jvm_flags':['-Xverify:all'],'production_modified':False,'server_capture_from_user':False,
 'official_source_commit':OFFICIAL['official_commit'],'official_recipe_rows':len(official),
 'official_static_matcher_rows_passed':official_probe['recipes_passed_shape_fixtures'],'official_matcher_assertions':official_probe['assertions'],
 'public_native_source_commit':NATIVE['public_repository_commit'],'public_native_source_protocol':2193,
 'public_native_source_sha256':NATIVE['source_sha256'],'public_native_recipe_rows':len(native),
 'public_native_static_matcher_rows_passed':native_probe['recipes_passed_shape_fixtures'],'public_native_matcher_assertions':native_probe['assertions'],
 'public_brewing_rows':len(brewing),'public_native_item_names_declared_and_java_targets_present':len(item_ledger),
 'public_native_ingredient_tags_declared':len(all_native_tags),'public_multi_uuids':14,'multi_semantics_known':13,'multi_semantics_unknown':1,
 'java_recipe_serializer_identifiers':len(serializer_rows),'source_sets_overlap_do_not_add_as_unique_recipes':True,
 'controlled_matcher_boundary':native_probe['boundary'],
 'does_not_validate':['Actual wire CraftingDataReader decoding','Native connection ItemDescriptor.matchesItem/alias resolver','Recipe unlock/display/placement','Output item/NBT/remainders','Native request acceptance/ACK/rollback','Whole workstation behavior','Client renderer/Mixin/BDS'],
 'test_adaptation_notes':['50 official shaped patterns have rows with unequal string lengths; fixture right-pads to rectangular matrix. This is not reported as an addon defect.',
                          'Noise item initially had no symbolic name and fixture tag lookup threw; fixed test-only predicate to reject unknown fixture ID before final execution. No production changed.',
                          'Two official counted-descriptor layouts also match compressed2x2; observed only, no claim that Bedrock accepts/rejects these layouts.'],
 'protected_actual_class_hashes':CLASSES,
}
(ROOT/'verification.json').write_text(json.dumps(verification,ensure_ascii=False,indent=2)+'\n')
(ROOT/'SOURCE-MANIFEST.json').write_text(json.dumps({
    'raw_third_party_source_not_embedded_in_delivery':True,
    'official':{'repository':'Mojang/bedrock-samples','commit':OFFICIAL['official_commit'],
                'directory_url':'https://github.com/Mojang/bedrock-samples/tree/'+OFFICIAL['official_commit']+'/behavior_pack/recipes',
                'raw_file_url_template':'https://raw.githubusercontent.com/Mojang/bedrock-samples/'+OFFICIAL['official_commit']+'/behavior_pack/recipes/{path}',
                'files':[{'path':row['path'],'sha256':row['source_sha256']} for row in OFFICIAL['recipes']]},
    'public_native':{'repository':'Kaooot/bedrock-network-data','commit':NATIVE['public_repository_commit'],
                     'url':'https://raw.githubusercontent.com/Kaooot/bedrock-network-data/'+NATIVE['public_repository_commit']+'/release/1.26.50/recipes.json',
                     'sha256':NATIVE['source_sha256'],'protocol':2193},
    'multi_semantics_sources':[{'uuid':row['uuid'],'source':row['semantic_source']} for row in MULTI['rows']],
    'generated_test_input_hashes':{name:hashlib.sha256((ROOT/name).read_bytes()).hexdigest() for name in ['official-catalog-input.json','native-reference-input.json']},
    'production_core_sha256':verification['core_sha256'],
},ensure_ascii=False,indent=2)+'\n')

readme='''# Revisión de todos los crafteos: catálogo y alcance real

Se revisó cada entrada de dos fuentes y se ejecutaron los métodos reales de matching del core IF48 sobre sus recetas estáticas. **Esto no certifica todos los crafteos dentro del servidor.** El caso comunicado de cohete de vuelo2 es una receta dinámica: tener miles de patrones correctos no descarta ese fallo.

| Fuente | Entradas inventariadas | Recetas probadas por forma/cantidad | Afirmaciones del matcher real |
|---|---:|---:|---:|
| Muestras oficiales Bedrock1.26.50.4 |1892|1655 (1064 shaped +591 shapeless)|38567|
| Referencia pública Bedrock1.26.50, protocolo2193 |4768|4741 (1217 shaped +2068 shapeless +1456 USER_DATA)|97852|

Las fuentes se solapan. No se suman como6660recetas únicas ni como una certificación de gameplay. El dump público no pertenece a la conexión del usuario: sus UUID/netIDs solo identifican la evidencia y no deben fijarse en producción.

## Una fila por entrada

- [Catálogo oficial:1892filas](CATALOGO-OFICIAL.md), con [JSON](CATALOGO-OFICIAL.json) y [CSV](CATALOGO-OFICIAL.csv).
- [Catálogo nativo público:4768filas](CATALOGO-NATIVO-PUBLICO.md), con [JSON](CATALOGO-NATIVO-PUBLICO.json) y [CSV](CATALOGO-NATIVO-PUBLICO.csv).
- [212mezclas públicas de pociones/recipientes](MEZCLAS-NATIVAS-PUBLICAS.json), [rutas y estaciones](RUTAS-Y-ESTACIONES.json), [22serializers Java](JAVA-SERIALIZERS.json), [1307nombres de ítem](native-item-mapping-ledger.json).

Cada fila conserva tipo, estación, resultado de referencia, prueba concreta o ausencia de prueba, ruta y comprobación pendiente. Los resultados USER_DATA requieren conservar NBT; que coincidan los ingredientes no prueba contenido de shulker, colores, texto, encantamientos o datos de un cohete.

## Qué ejecuta el ensayo

`CraftingCatalogProbe.java` llama a `CraftingDataTracker.matchShaped`, `matchShapeless` y `ShapelessRecipe.expandIngredients` del JAR real IF48, sin reescribir sus algoritmos. Usa los modelos reales ShapedRecipe/ShapelessRecipe/ItemDescriptor/BedrockItem.

Por receta estática se comprueban formas 2×2/3×3 cuando caben, offsets, espejo habilitado en el modelo de prueba, orden inverso sin forma, ingrediente ausente, material ajeno, celda extra, stacks de64, suma de unidades requeridas e inmutabilidad de los inputs. Un patrón mayor que2×2 se rechaza en esa dimensión. Se conservan resultados individuales y cantidad de afirmaciones por fila.

La frontera del predicado de ingrediente es explícita: usa nombres/aux y miembros de tags del recurso real incluido, con IDs simbólicos. **No ejecuta ItemDescriptor.matchesItem conectado a un servidor ni su resolución de aliases.** Los nombres de fuentes JSON pueden ser históricos, no llevar namespace o codificar aux dentro del nombre; su ausencia literal en registros no prueba que la receta desaparezca.

Los12tags de ingredientes empleados en las formas públicas tienen miembros incluidos. Considerando también herrería/dinámicas hay17tags y1307nombres nativos de ítem: todos tienen declaración runtime/mapping y destinos existentes en el registro Java incluido. Este último control solo verifica datos, no sus usos ni NBT.

Cincuenta patrones oficiales de escalera tienen filas de distinta longitud; se rellenan espacios a la derecha para formar la matriz del fixture. No se cambia la fuente ni se presenta como fallo del addon. Dos recetas oficiales, sopa de remolacha y lingote de netherita, también coinciden con cantidades comprimidas en dos celdas del matcher2×2. Se registra la observación; no se atribuye a Bedrock una aceptación/rechazo que no se midió.

## Familias que el bulk de cuadrícula no certifica

La muestra oficial contiene152recetas de horno,72de pociones y13de herrería fuera del matcher de cuadrícula. La referencia pública contiene14MULTI y13de herrería fuera del bulk, además de210mezclas de pociones y2cambios de recipiente separados. No figuran como pruebas de forma aprobadas.

De14UUID MULTI públicos se identificaron13por fuentes fijadas de PMMP/PowerNukkitX: reparación, fuegos artificiales, clonado de libros, operaciones de mapas, patrones/duplicado de estandartes y vasija decorada. `00000000-0000-0000-0000-0000000000c8` queda desconocido: no se inventa que sea decoración de escudo. FIREWORKS usa un UUID común; no se deducen tres UUID separados de los nombres Java de cohete/estrella/desvanecimiento.

El core genérico guarda MultiRecipe como UUID sin patrón/resultado y su matcher normal solo trata SHAPED/SHAPELESS/USER_DATA_SHAPELESS. La familia necesita lógica específica que preserve datos y use el networkId realmente anunciado. La auditoría de fuegos artificiales es la que reproduce vuelo2/3 sin preview y pérdida de NBT de estrella al usar solo resultado estático. Este catálogo no presenta esa corrección como entregada mientras su revisión siga en curso.

El registro Java incluido enumera22serializers, incluidos crafting_dye/imbue/transmute y especiales. Su presencia **no prueba** que exista conversión Bedrock ni que esas recetas se ejecuten. Lo mismo se aplica a las constantes UNKNOWN de RecipeType y a las familias de química: se documenta su lector, sin fingir que las fuentes vanilla aquí contienen todas las recetas Education.

## Fuentes fijadas

- [Mojang/bedrock-samples](https://github.com/Mojang/bedrock-samples/tree/46ba6ea985fb5a92d79a9419198f10dda14c199d/behavior_pack/recipes), commit46ba6ea985fb5a92d79a9419198f10dda14c199d, versión1.26.50.4.1892archivos originales conSHA256 por entrada.
- [Kaooot/bedrock-network-data](https://raw.githubusercontent.com/Kaooot/bedrock-network-data/3e50b56363888330800588a0bd6798c508416659/release/1.26.50/recipes.json), commit3e50b56363888330800588a0bd6798c508416659. SHA256 `8b25b59c2ecff6942f81df48be9fe1e69ca85083170036844f0e45816bff8514`. El tipo numérico de ese JSON es del formato del autor:0shaped,1shapeless,2multi,3userdata,6transform,7trim. No se confunde con los valores de RecipeType del paquete de red.
- Semánticas MULTI: [MULTI-SEMANTICS.json](MULTI-SEMANTICS.json), con URL y commit de las constantes identificadas.
- Base IF48 y clases exactas fijadas en `verification.json`; no se modificó producción ni se publicaron archivos durante esta auditoría.

## Reproducción

El classpath guardado coloca core-if48.jar antes de los JAR históricos. Se usa JDK25.0.5.0.2 con `-Xverify:all`. Los IDs/UUID del modelo de prueba son fixtures y nunca se empaquetan como constantes runtime.

```sh
javac -cp "$(cat classpath.txt)" -d test-classes CraftingCatalogProbe.java
java -Xverify:all -cp "test-classes:$(cat classpath.txt)" net.raphimc.viabedrock.protocol.storage.CraftingCatalogProbe official-catalog-input.json matcher-results.json
java -Xverify:all -cp "test-classes:$(cat classpath.txt)" net.raphimc.viabedrock.protocol.storage.CraftingCatalogProbe native-reference-input.json native-matcher-results.json
python build_coverage_ledger.py
```

No distribuir test-classes como clases cargables del mod. Fuentes de prueba y resultados son evidencia separada. El cierre/clic rápido, autocolocación, slots compartidos, recipientes devueltos y ACK/rechazo se auditan en las rutas de inventario; no quedan certificados por esta prueba de formas.

Los paquetes JSON originales y los fixtures que los contienen no se incluyen en la entrega: [SOURCE-MANIFEST.json](SOURCE-MANIFEST.json) fija URLs y hashes para recuperarlos. `prepare_catalog.py` y `prepare_native_reference.py` generan los inputs locales del ensayo a partir de esas fuentes; `CraftingCatalogProbe.java` produce los resultados derivados publicados.
'''
# Expand counts only in prose; preserve identifiers, hashes, versions and URLs.
replacements={
 'Bedrock1.26':'Bedrock 1.26','protocolo2193':'protocolo 2193','vuelo2':'vuelo 2','vuelo2/3':'vuelo 2/3',
 'como6660recetas':'como 6660 recetas','1892filas':'1892 filas','4768filas':'4768 filas','212mezclas':'212 mezclas',
 '22serializers':'22 serializers','1307nombres':'1307 nombres','stacks de64':'stacks de 64','que2×2':'que 2×2',
 'matcher2×2':'matcher 2×2','Los12tags':'Los 12 tags','hay17tags':'hay 17 tags','y1307nombres':'y 1307 nombres',
 'contiene152recetas':'contiene 152 recetas','72de':'72 de','13de':'13 de','contiene14MULTI':'contiene 14 MULTI',
 'de210mezclas':'de 210 mezclas','y2cambios':'y 2 cambios','De14UUID':'De 14 UUID','identificaron13por':'identificaron 13 por',
 'enumera22serializers':'enumera 22 serializers','commit46ba':'commit 46ba','commit3e50':'commit 3e50',
 'versión1.26':'versión 1.26','1.26.50.4.1892archivos':'1.26.50.4. 1892 archivos','conSHA256':'con SHA256',
 'autor:0shaped,1shapeless,2multi,3userdata,6transform,7trim':'autor: 0 shaped, 1 shapeless, 2 multi, 3 userdata, 6 transform, 7 trim',
 'JDK25':'JDK 25','354 entradas públicas frente356':'354 entradas públicas frente 356',
 'de1ingrediente':'de 1 ingrediente','134furnace/62blast/9smoker':'134 furnace / 62 blast / 9 smoker',
 '9campfire y9soul_campfire':'9 campfire y 9 soul_campfire','12transform+1trim':'12 transform + 1 trim',
 '210mezclas+2cambios':'210 mezclas + 2 cambios','14UUID':'14 UUID','13semánticas':'13 semánticas','y1desconocida':'y 1 desconocida',
 'y1307 nombres':'y 1307 nombres','horno,72':'horno, 72','y13 de':'y 13 de','enumera22':'enumera 22',
}
for old,new in replacements.items():readme=readme.replace(old,new)
(ROOT/'ALCANCE-Y-REPRODUCCION.md').write_text(readme)

dynamic='# Recetas dinámicas: cobertura y huecos\n\nNo se deduce soporte por encontrar un serializer o UUID. Los netIDs se anuncian por conexión; los de la referencia pública no son constantes de producción.\n\n'
dynamic+='| UUID | Semántica de fuente fijada | Ruta y alcance actual | Comprobación pendiente |\n|---|---|---|---|\n'
for item in MULTI['rows']:
    semantic=item['semantic_constant']
    if 'CARTOGRAPHY' in semantic:
        scope='CartographyRecipes.select contiene UUID dedicado; no certificado por el matcher general.'
        pending='Operación real de mapa, consumo, nombre y NBT.'
    elif semantic=='TYPE_FIREWORKS':
        scope='Fallo de cohetes Flight2/3 reproducido. Corrección en revisión para cohetes Flight1/2/3 con/sin estrellas; creación de estrellas y fade siguen fuera de esa corrección.'
        pending='Validar helper, acciones y aceptación nativa; probar estrella nueva, forma/color/efectos y fade por separado.'
    elif semantic=='TYPE_BANNER_ADD_PATTERN':
        scope='LoomContainer/LoomPatterns tienen ruta propia de telar; no asumir mismo soporte en mesa por UUID.'
        pending='Conservar patrones/colores al fabricar y confirmar resultado servidor.'
    elif item['semantic_verified']:
        scope='Semántica identificada; getRecipeMatch genérico no evalúa MULTI. Familia no corregida por el cambio de cohetes.'
        pending='Implementación específica, resultado/NBT/remainders y transacción aceptada.'
    else:
        scope='Desconocida. No atribuirla a escudo ni otra familia sin evidencia.'
        pending='Identificar contrato nativo antes de generar solicitudes o resultados.'
    dynamic+=f'| `{item["uuid"]}` | {semantic} | {scope} | {pending} |\n'
dynamic+='\nLos14UUID proceden del dump público2193;13semánticas se documentan en constantes PMMP/PowerNukkitX fijadas por commit. No son una captura de la sesión del usuario.\n'
dynamic=dynamic.replace('Flight2/3','Flight 2/3').replace('Flight1/2/3','Flight 1/2/3').replace('Los14UUID','Los 14 UUID').replace('público2193','público 2193').replace(';13semánticas','; 13 semánticas')
(ROOT/'RECETAS-DINAMICAS.md').write_text(dynamic)

short='''# Revisión de crafteos

Se comprobó la forma y cantidad de cada receta estática de dos fuentes con los métodos reales del core IF48. **Esto no certifica todas las recetas dentro del servidor ni sus resultados con NBT.**

| Fuente | Entradas catalogadas | Matching estático comprobado | Afirmaciones |
|---|---:|---:|---:|
| Muestras oficiales Bedrock1.26.50.4 |1892|1655|38567|
| Referencia pública Bedrock2193 |4768|4741|97852|

Las fuentes se solapan; no se suman como recetas únicas. La referencia pública no es la lista de la conexión del usuario. Las pruebas usan un predicado explícito de nombre/aux/tag con IDs simbólicos; no ejecutan resolución nativa, resultado/NBT, clics, ACK ni aceptación de servidor.

## Lo que sigue necesitando revisión específica

- Cohetes de vuelo2/3: fallo reproducido; corrección de cohetes1/2/3 con/sin estrellas en revisión. Crear una estrella nueva y añadir fade siguen siendo acciones distintas pendientes.
- Dinámicas MULTI:14UUID públicos,13semánticas identificadas y1desconocida. Clonar libros, reparar objetos, duplicar estandartes, mapas y vasijas no quedan corregidos por arreglar cohetes.
- USER_DATA:1456entradas pasan forma; eso no valida conservar contenido de shulkers, colores, texto o datos del resultado.
- Estaciones:13recetas públicas de herrería y212mezclas de pociones tienen datos/rutas identificados; el bulk no ejecuta sus transacciones. Tampoco certifica horno/cortapiedras/telar por probar un patrón.

## Cada receta, una por una

- [Catálogo oficial](CATALOGO-OFICIAL.md):1892filas por familia, páginas de hasta350recetas; [CSV](CATALOGO-OFICIAL.csv) y [JSON](CATALOGO-OFICIAL.json).
- [Catálogo nativo público](CATALOGO-NATIVO-PUBLICO.md):4768filas por familia; [CSV](CATALOGO-NATIVO-PUBLICO.csv) y [JSON](CATALOGO-NATIVO-PUBLICO.json).
- [Dinámicas y huecos](RECETAS-DINAMICAS.md), [mezclas de pociones](MEZCLAS-NATIVAS-PUBLICAS.json), [rutas y estaciones](RUTAS-Y-ESTACIONES.json).
- [Alcance, fuentes y reproducción](ALCANCE-Y-REPRODUCCION.md), [verificación](verification.json), [1307nombres de ítem](native-item-mapping-ledger.json), [22serializers Java](JAVA-SERIALIZERS.json), [tipos de receta Bedrock](BEDROCK-RECIPE-TYPES.json) y [descriptores](INGREDIENT-DESCRIPTORS.json).

Cada fila distingue la evidencia concreta de la prueba pendiente en servidor. No se modificó producción ni se publicaron archivos en esta subauditoría.
'''
short_replacements={'Bedrock1.26':'Bedrock 1.26','Bedrock2193':'Bedrock 2193','vuelo2/3':'vuelo 2/3','cohetes1/2/3':'cohetes 1/2/3','MULTI:14UUID':'MULTI: 14 UUID','públicos,13semánticas':'públicos, 13 semánticas','y1desconocida':'y 1 desconocida','USER_DATA:1456entradas':'USER_DATA: 1456 entradas','Estaciones:13recetas':'Estaciones: 13 recetas','y212mezclas':'y 212 mezclas','1892filas':'1892 filas','4768filas':'4768 filas','hasta350recetas':'hasta 350 recetas','1307nombres':'1307 nombres','22serializers':'22 serializers'}
for old,new in short_replacements.items():short=short.replace(old,new)
(ROOT/'README.md').write_text(short)
artifacts=['README.md','ALCANCE-Y-REPRODUCCION.md','RECETAS-DINAMICAS.md','MULTI-SEMANTICS.json','SOURCE-MANIFEST.json','verification.json','CATALOGO-OFICIAL.md','CATALOGO-OFICIAL.json','CATALOGO-OFICIAL.csv',
    'CATALOGO-NATIVO-PUBLICO.md','CATALOGO-NATIVO-PUBLICO.json','CATALOGO-NATIVO-PUBLICO.csv',
    'MEZCLAS-NATIVAS-PUBLICAS.json','MEZCLAS-NATIVAS-PUBLICAS.csv','RUTAS-Y-ESTACIONES.json','RUTAS-Y-ESTACIONES.csv',
    'native-item-mapping-ledger.json','JAVA-SERIALIZERS.json','BEDROCK-RECIPE-TYPES.json','INGREDIENT-DESCRIPTORS.json','matcher-results.json','native-matcher-results.json',
    'matcher-checks.txt','native-matcher-checks.txt','CraftingCatalogProbe.java','prepare_catalog.py','prepare_native_reference.py','build_coverage_ledger.py']
artifacts += [str(path.relative_to(ROOT)) for path in sorted((ROOT/'pages').glob('*.md'))]
(ROOT/'manifest.json').write_text(json.dumps({'no_production_mutation':True,'no_live_gameplay_certification':True,
    'files':[{'path':name,'bytes':(ROOT/name).stat().st_size,'sha256':hashlib.sha256((ROOT/name).read_bytes()).hexdigest()} for name in artifacts]},indent=2)+'\n')
print(json.dumps({'official':len(official),'native_reference':len(native),'brewing':len(brewing),'item_names':len(item_ledger),'native_tags':len(all_native_tags),'java_serializers':len(serializer_rows),'source_sets_overlap':True}))
