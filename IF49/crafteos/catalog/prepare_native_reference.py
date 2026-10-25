from pathlib import Path
import hashlib
import json

ROOT=Path(__file__).parent
SOURCE=ROOT.parent/'fireworks/reference/kaooot-1.26.50-recipes.json'
source=json.loads(SOURCE.read_text())
official=json.loads((ROOT/'official-catalog-input.json').read_text())
rows=[]
names={0:'SHAPED',1:'SHAPELESS',2:'MULTI',3:'USER_DATA_SHAPELESS',6:'SMITHING_TRANSFORM',7:'SMITHING_TRIM'}

def descriptor(value):
    amount=value.get('count',1)
    if value['type']=='name':return {'item':value['itemId'],'data':value.get('auxValue',0),'count':amount}
    if value['type']=='item_tag':return {'tag':value['itemTag'],'count':amount}
    raise ValueError(value)

for index,native in enumerate(source['recipes']):
    native_type=native['type'];typ={0:'minecraft:recipe_shaped',1:'minecraft:recipe_shapeless',3:'minecraft:recipe_shapeless'}.get(native_type,'native_reference:'+names.get(native_type,str(native_type)))
    identifier=native.get('id',native.get('uuid','entry-'+str(index)))
    recipe={'description':{'identifier':identifier},'tags':[native['block']] if native.get('block') else []}
    if native_type==0:
        recipe['pattern']=native['shape'];recipe['key']={key:descriptor(value) for key,value in native['input'].items()}
    elif native_type in [1,3]:recipe['ingredients']=[descriptor(value) for value in native['input']]
    recipe['result']=native.get('output',native.get('result',[]))
    rows.append({'path':f'{index:05d}-{names.get(native_type,str(native_type))}',
                 'identifier':identifier,'type':typ,'native_reference_type':names.get(native_type,str(native_type)),
                 'native_reference_json_type':native_type,
                 'public_reference_uuid':native.get('uuid'),'public_reference_network_id':native.get('netId'),
                 'tags':recipe['tags'],'recipe':recipe,
                 'static_matcher_applicable':native_type in [0,1,3],
                 'native_reference':native,
                 'canonical_entry_sha256':hashlib.sha256(json.dumps(native,sort_keys=True,separators=(',',':')).encode()).hexdigest()})
data={'source':'Kaooot public Bedrock1.26.50 reference dump; NOT user server capture',
      'public_repository_commit':'3e50b56363888330800588a0bd6798c508416659',
      'source_sha256':hashlib.sha256(SOURCE.read_bytes()).hexdigest(),
      'source_protocol_number':source['version'],
      'type_number_boundary':'JSON author types0=shaped,1=shapeless,2=multi,3=user-data,6=smithingtransform,7=trim; not Bedrock RecipeType wire enum values',
      'uuid_and_network_ids_are_reference_only_not_production_constants':True,
      'core_item_tags':official['core_item_tags'],'core_class_sha256':official['core_class_sha256'],
      'recipes':rows,'public_potion_mixes':source['potionMixes'],'public_container_mixes':source['containerMixes']}
(ROOT/'native-reference-input.json').write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n')
print('Prepared',len(rows),'public native-reference entries;',sum(row['static_matcher_applicable'] for row in rows),'static matcher fixtures; sourceSHA',data['source_sha256'])
