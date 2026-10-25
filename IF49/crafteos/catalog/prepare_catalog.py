from pathlib import Path
from collections import Counter
import hashlib
import json
import zipfile

ROOT=Path(__file__).parent
SOURCE=ROOT/'official-samples/behavior_pack/recipes'
rows=[]
for path in sorted(SOURCE.glob('*.json')):
    raw=path.read_bytes()
    data=json.loads(raw.decode('utf-8-sig'))
    types=[key for key in data if key.startswith('minecraft:recipe_')]
    assert len(types)==1,(path,types)
    typ=types[0]
    recipe=data[typ]
    row={'path':path.name,'source_sha256':hashlib.sha256(raw).hexdigest(),
         'identifier':recipe['description']['identifier'],'format_version':data.get('format_version'),
         'type':typ,'tags':recipe.get('tags',[]),'recipe':recipe,
         'static_matcher_applicable':typ in ['minecraft:recipe_shaped','minecraft:recipe_shapeless']}
    rows.append(row)
assert len({row['path'] for row in rows})==len(rows)
with zipfile.ZipFile(ROOT/'core-if48.jar') as jar:
    tags=json.loads(jar.read('assets/viabedrock/data/bedrock/item_tags.json'))
    class_paths=[
        'net/raphimc/viabedrock/protocol/storage/CraftingDataTracker.class',
        'net/raphimc/viabedrock/protocol/model/recipe/ShapedRecipe.class',
        'net/raphimc/viabedrock/protocol/model/recipe/ShapelessRecipe.class',
        'net/raphimc/viabedrock/protocol/model/recipe/ItemDescriptor.class',
        'net/raphimc/viabedrock/protocol/types/recipe/CraftingDataReader.class',
    ]
    class_hashes={path:hashlib.sha256(jar.read(path)).hexdigest() for path in class_paths}
    tag_sha=hashlib.sha256(jar.read('assets/viabedrock/data/bedrock/item_tags.json')).hexdigest()
data={
    'official_repository':'Mojang/bedrock-samples',
    'official_commit':'46ba6ea985fb5a92d79a9419198f10dda14c199d',
    'official_sample_version':'1.26.50.4',
    'not_a_capture_of_user_server_recipes':True,
    'files':len(rows),'type_counts':dict(Counter(row['type'] for row in rows)),
    'tag_counts':dict(Counter(tag for row in rows for tag in row['tags'])),
    'core_item_tag_sha256':tag_sha,'core_item_tags':tags,
    'core_class_sha256':class_hashes,
    'recipes':rows,
}
(ROOT/'official-catalog-input.json').write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n')
print(json.dumps({'files':len(rows),'type_counts':data['type_counts'],'tag_counts':data['tag_counts'],'core_class_sha256':class_hashes},indent=2))
