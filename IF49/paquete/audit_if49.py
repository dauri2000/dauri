"""Independent, read-only review of immutable IF49 bytes. No builder imported."""
from collections import Counter
from io import BytesIO
from pathlib import Path, PurePosixPath
from zipfile import ZipFile
import hashlib
import json
import re

ROOT=Path('/workspace/minecraft/analysis/chest-lag-if49/final-independent')
ANALYSIS=Path('/workspace/minecraft/analysis')
PACKAGE=ANALYSIS/'chest-lag-if49/package'
SOURCE=Path('/workspace/minecraft/entregables/ViaFabricPlus-Bedrock-IF48.jar')
OUTPUT=Path('/workspace/minecraft/entregables/ViaFabricPlus-Bedrock-IF49.jar')
CORE='META-INF/jars/ViaBedrock-0.0.31-StackAnvil.jar'
BUNDLE='META-INF/stackanvil-if49-changes.zip'
SOURCE_SHA='cce72ee547155865c5196b0262deec9766bb9267741a755eaa72bbad74ae758a'
OUTPUT_SHA='93ad74e03ef84237bd4ea376d5a2cb8837d730308e0f5305562f5652af63df5a'
RUNTIME_SHA={
 'com/viaversion/viafabricplus/bedrock/screen/container/BedrockNativeContainerScreen.class':'902b8b6b4a99ffd887bc21f8f0364e14cb5ed2494e5912842c631ca3c009e57d',
 'net/raphimc/viabedrock/api/resourcepack/ui/JsonUiProgram$Session.class':'630a70475327fbcccd8ba4202fd047f4edf1d4d2c1463e87d30c116870191e67',
 'net/raphimc/viabedrock/protocol/storage/CraftingDataTracker.class':'b3dee566f527d0465a853f877a368b0151a6cf3fb72d00e3f6dc722d5054d153',
 'net/raphimc/viabedrock/api/resourcepack/ui/UiVariableSnapshotMap$EntryIterator.class':'a4d55ed8fbb1226d8518510ccd9aacfb079859634c1ef33f06cc55ca0043f5c0',
 'net/raphimc/viabedrock/api/resourcepack/ui/UiVariableSnapshotMap$EntryView.class':'5d42566fd797f63976808874c7577632361fd1729bc6b392af91261c55aef315',
 'net/raphimc/viabedrock/api/resourcepack/ui/UiVariableSnapshotMap$LiveEntry.class':'deb55a79bc75834dac0e12dc5de755a196bd311f5d7579ce9d0c91630f8ac858',
 'net/raphimc/viabedrock/api/resourcepack/ui/UiVariableSnapshotMap.class':'6e45e00989950c68a3c4294901577339631001eb5671cb104788f8e10630c8ec',
 'net/raphimc/viabedrock/protocol/storage/FireworkMultiRecipes.class':'27f6da94517f6f5a2467a60be89c10926716648186b98d607e60a0b69b9a2126',
}
ROOT_REPLACEMENTS={next(name for name in RUNTIME_SHA if name.startswith('com/'))}
CORE_REPLACEMENTS={
 'net/raphimc/viabedrock/api/resourcepack/ui/JsonUiProgram$Session.class',
 'net/raphimc/viabedrock/protocol/storage/CraftingDataTracker.class',
}
CORE_ADDITIONS=set(RUNTIME_SHA)-ROOT_REPLACEMENTS-CORE_REPLACEMENTS
AUTO_NAMES=['package_if49.py','build_final.py','build-inputs.json','candidate-scope.json','protected-inputs.json','ESTADO.md']
OMITTED_CATALOG={
 'CATALOGO-OFICIAL.json','CATALOGO-OFICIAL.csv','CATALOGO-NATIVO-PUBLICO.json','CATALOGO-NATIVO-PUBLICO.csv',
 'matcher-results.json','native-matcher-results.json',
}

def sha(data):return hashlib.sha256(data).hexdigest()
def file_sha(path):
    with Path(path).open('rb') as file:return hashlib.file_digest(file,'sha256').hexdigest()
def zip_index(archive):
    names=archive.namelist()
    assert len(names)==len(set(names)),'Duplicate ZIP entries'
    assert archive.testzip() is None,'ZIP CRC failure'
    return {name:sha(archive.read(name)) for name in names}
def exact_scope(before,after,changed,added):
    assert set(before)-set(after)==set(),'Unexpected removed entries'
    assert set(after)-set(before)==added,'Unexpected added entries'
    actual={name for name in before if before[name]!=after[name]}
    assert actual==changed,(actual,changed)
    return {'entry_count_before':len(before),'entry_count_after':len(after),'changed_entries':sorted(actual),
            'added_entries':sorted(added),'removed_entries':[],'unchanged_entries':len(before)-len(actual),
            'all_other_entry_contents_identical':True,'no_duplicates':True,'crc_ok':True}

def main():
    assert SOURCE.stat().st_size==215635312 and file_sha(SOURCE)==SOURCE_SHA
    assert OUTPUT.stat().st_size==216017725 and file_sha(OUTPUT)==OUTPUT_SHA
    config=json.loads((PACKAGE/'build-inputs.json').read_text())
    protected=json.loads((PACKAGE/'protected-inputs.json').read_text())
    assert config['source_sha256']==SOURCE_SHA and config['source_size_bytes']==215635312
    assert config['version']=='1.1.1-StackAnvil-InteractionFix49.1'
    assert config['xp_fix_claimed'] is False and config['preserve_if47_diagnostics'] is True
    assert set(config['root_replacements'])==ROOT_REPLACEMENTS
    assert set(config['core_replacements'])==CORE_REPLACEMENTS
    assert set(config['core_additions'])==CORE_ADDITIONS
    for group in ['root_replacements','core_replacements','core_additions']:
        for name,value in config[group].items():
            assert value['sha256']==RUNTIME_SHA[name]==file_sha(value['path']),name
    with ZipFile(SOURCE) as old,ZipFile(OUTPUT) as new:
        old_root=zip_index(old);new_root=zip_index(new)
        root_scope=exact_scope(old_root,new_root,ROOT_REPLACEMENTS|{CORE,'fabric.mod.json'},{BUNDLE})
        old_meta=json.loads(old.read('fabric.mod.json'));new_meta=json.loads(new.read('fabric.mod.json'))
        assert new_meta=={**old_meta,'name':'ViaFabricPlus Bedrock - IF49','version':'1.1.1-StackAnvil-InteractionFix49.1'}
        with ZipFile(BytesIO(old.read(CORE))) as old_core_zip,ZipFile(BytesIO(new.read(CORE))) as new_core_zip:
            old_core=zip_index(old_core_zip);new_core=zip_index(new_core_zip)
            core_scope=exact_scope(old_core,new_core,CORE_REPLACEMENTS,CORE_ADDITIONS)
            assert sha(old.read(CORE))==protected['core_jar_sha256']=='70f2edc538a9902ad43ddb7e8043de94297da999b68b8a2120029560dc0ecad3'
            for name,expected in RUNTIME_SHA.items():assert (new_core if name.startswith('net/') else new_root)[name]==expected,name
            assert len(protected['core_sha256s'])==26 and len(protected['root_sha256s'])==11
            for name,expected in protected['core_sha256s'].items():assert old_core[name]==new_core[name]==expected,name
            for name,expected in protected['root_sha256s'].items():assert old_root[name]==new_root[name]==expected,name
            previous=json.loads((ANALYSIS/'end-audit-if48/final-independent/verification.json').read_text())
            inherited_five=previous['exact_five_runtime_class_sha256s']
            inherited_xp=previous['exact_four_xp_observer_sha256s_preserved']
            for name,expected in {**inherited_five,**inherited_xp}.items():
                assert (new_core if name.startswith('net/') else new_root)[name]==expected,name
        with ZipFile(BytesIO(new.read(BUNDLE))) as bundle:
            source_hashes=zip_index(bundle)
            chosen={str(Path(path).relative_to(ANALYSIS)):expected for path,expected in config['source_file_sha256s'].items()}
            source_list={str(Path(path).relative_to(ANALYSIS)) for path in config['source_files']}
            automatic={str((PACKAGE/name).relative_to(ANALYSIS)) for name in AUTO_NAMES}
            assert len(chosen)==len(source_list)==131 and set(chosen)==source_list
            assert len(automatic)==6 and not set(chosen)&automatic
            assert set(source_hashes)==set(chosen)|automatic and len(source_hashes)==137
            for name,expected in chosen.items():assert source_hashes[name]==expected==file_sha(ANALYSIS/name),name
            for name in automatic:assert source_hashes[name]==file_sha(ANALYSIS/name),name
            forbidden_parts={'reference','references','attachments','video','videos','world','worlds','saves','.git',
                             'decompiled','actual-source','boundary-src','boundary-classes','test-classes','tool-classes','official-samples'}
            for name in source_hashes:
                path=PurePosixPath(name)
                assert path.suffix in {'.java','.py','.md','.txt','.json','.csv'},name
                assert not path.is_absolute() and '..' not in path.parts,name
                assert not set(path.parts)&forbidden_parts,name
                assert not re.search(r'(?:latest\.log|\.log(?:\.gz)?$|\.jfr$|\.mp4$|\.litematic$|\.nbt$|\.7z(?:\.\d+)?$)',name,re.I),name
                text=bundle.read(name).decode('utf-8')
                assert '\0' not in text,name
                assert not (path.suffix=='.java' and re.search(r'^\s*package\s+net\.minecraft(?:\.|\s*;)',text,re.M)),name
            prefix='crafting-all-if49/catalog/'
            local_pages={str(path.relative_to(ANALYSIS)) for path in (ANALYSIS/'crafting-all-if49/catalog/pages').glob('*.md')}
            embedded_pages={name for name in source_hashes if name.startswith(prefix+'pages/')}
            assert local_pages==embedded_pages and len(local_pages)==29
            page_counts=Counter();largest_page=0
            for name in embedded_pages:
                text=bundle.read(name).decode();largest_page=max(largest_page,len(bundle.read(name)))
                ids=re.findall(r'^\| ((official|native)-\d+) \|',text,re.M)
                assert len(ids)<=350,name
                for entry,origin in ids:page_counts[origin]+=1
            assert page_counts=={'official':1892,'native':4768} and largest_page<400000,page_counts
            catalog_manifest=json.loads(bundle.read(prefix+'manifest.json'))
            assert catalog_manifest['no_live_gameplay_certification'] is True
            missing=[]
            for entry in catalog_manifest['files']:
                archive_name=prefix+entry['path'];local=ANALYSIS/archive_name
                assert file_sha(local)==entry['sha256'] and local.stat().st_size==entry['bytes'],archive_name
                if archive_name in source_hashes:assert source_hashes[archive_name]==entry['sha256'],archive_name
                else:
                    assert entry['path'] in OMITTED_CATALOG and entry['bytes']>1024**2,entry
                    missing.append(entry)
            assert {entry['path'] for entry in missing}==OMITTED_CATALOG and len(missing)==6
            assert not any(name in source_hashes for name in ['official-catalog-input.json','native-reference-input.json'])
            readme_name='chest-lag-if49/delivery/README.md'
            readme=bundle.read(readme_name).decode()
            for phrase in ['Cofres','Cohetes','uno, dos o tres','Produce tres cohetes','1, 2 o 3',
                           'no son FPS','No se probó cada receta dentro de un servidor real',
                           'No se declara corregido todo el crafteo','Crear estrellas','desvanecimiento',
                           'La XP lenta, los mapas','dragón','continúan pendientes','[IF47-XP]',
                           'No se ejecutaron Minecraft gráfico','un solo addon Bedrock IF','InteractionFix49.1']:
                assert phrase in readme,phrase
            delivery_sha=source_hashes[readme_name]
    assert file_sha(OUTPUT)==OUTPUT_SHA and file_sha(SOURCE)==SOURCE_SHA
    result={
      'status':'PASS_INDEPENDENT_IMMUTABLE_ARTIFACT_REVIEW',
      'artifact':str(OUTPUT),'artifact_sha256':OUTPUT_SHA,'artifact_size_bytes':216017725,
      'baseline_sha256':SOURCE_SHA,'production_rebuilt':False,'builder_imported_or_executed':False,
      'functional_or_historical_tests_rerun':False,'checks_performed':'CRC, duplicates, exact entry deltas, whole-entry hashes, eight frozen runtime hashes,37 protected entries, source allowlist/local hashes, catalog pagination and delivery scope text',
      'root':root_scope,'core':core_scope,'exact_runtime_sha256s':RUNTIME_SHA,
      'protected_root_sha256s':protected['root_sha256s'],'protected_core_sha256s':protected['core_sha256s'],
      'protected_count':37,'if48_five_runtime_entries_retained':inherited_five,'if47_four_xp_observers_retained':inherited_xp,
      'fabric_only_name_version_changed':True,'fabric_version':new_meta['version'],
      'new_source_bundle':{'entry':BUNDLE,'entries':137,'explicit_files':131,'automatic_build_files':6,
                           'exact_allowlist_matches_and_current_local_hashes_match':True,'no_private_media_logs_worlds_jfr_or_raw_reference_packs_added':True,
                           'no_loadable_test_or_minecraft_classes_added':True,'crc_ok':True,'no_duplicates':True,
                           'sha256s':dict(sorted(source_hashes.items()))},
      'catalog':{'embedded_pages':29,'official_page_rows':page_counts['official'],'native_page_rows':page_counts['native'],
                 'max_rows_per_page':350,'largest_page_bytes':largest_page,'omitted_large_derived_files_have_full_hashes_in_embedded_manifest':missing,
                 'separate_publication_not_verified_in_this_review':True},
      'delivery_readme_sha256':delivery_sha,
      'delivery_scope_review':'Three implementation changes: reuse initial native container preparation, resolver snapshot-copy reduction, dynamic rockets Flight1/2/3 with optional existing stars. Preserves prior corrections/XP diagnostics. Explicitly does not certify all dynamic recipes, star creation/fade, XP, maps, dragon, user FPS or live BDS gameplay.',
      'xp_fix_claimed':False,'live_minecraft_mixin_bds_executed':False,
      'review_source_sha256':file_sha(Path(__file__)),
    }
    (ROOT/'verification.json').write_text(json.dumps(result,ensure_ascii=False,indent=2)+'\n')
    print(json.dumps({key:result[key] for key in ['status','artifact_sha256','artifact_size_bytes','protected_count','delivery_readme_sha256']},indent=2))

if __name__=='__main__':main()
