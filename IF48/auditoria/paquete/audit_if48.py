"""Independent immutable IF48 artifact audit. Does not import or run any builder."""
from pathlib import Path
from zipfile import ZipFile
from io import BytesIO, StringIO
import csv
import hashlib
import json

TASK_ROOT = Path('/workspace/minecraft/analysis/end-audit-if48/final-independent')
ANALYSIS = Path('/workspace/minecraft/analysis')
SOURCE = Path('/workspace/minecraft/entregables/ViaFabricPlus-Bedrock-IF47-XP-Diagnostico.jar')
OUTPUT = Path('/workspace/minecraft/entregables/ViaFabricPlus-Bedrock-IF48.jar')
CORE = 'META-INF/jars/ViaBedrock-0.0.31-StackAnvil.jar'
BUNDLE = 'META-INF/stackanvil-if48-changes.zip'
CLASS_SHA = {
 'com/viaversion/viafabricplus/bedrock/render/BedrockFirstPersonItemRendering.class':'912d2f1fe20fa1eeb7aceef04e01350c24349754cdd27ba195b1a346f2527215',
 'net/raphimc/viabedrock/protocol/storage/ConnectedBlockShapes.class':'c983eb2dd2c98867d6f0cf966dad9648aa1165961afaec57d49bebfb689f9b96',
 'net/raphimc/viabedrock/protocol/packet/ClientPlayerPackets.class':'c833b164912b1d4cac4dc2a5f1449ce2cbfe8f353069b198023efedd63b599ae',
 'net/raphimc/viabedrock/protocol/storage/ChorusPlantConnections.class':'64b0a20b0b62837ca4021a13344f0f77499f453c2922b367955086820601f009',
 'net/raphimc/viabedrock/protocol/packet/EndCreditsBridge.class':'a7ae969f2b55691cfac4c46a277fd84b087c534ce9b37c47a3645f1e5389738c',
}
XP_SHA = {
 'net/raphimc/viabedrock/api/model/entity/Entity.class':'baf45e77323924dbbbc5498b7c404451af15b5277e458cd3b23073b00216bc57',
 'net/raphimc/viabedrock/protocol/packet/EntityPackets.class':'4f2fb74a12d763dfdddf3c7b2c9362b8662c0c70fb30fd5ba7b4326de5882f5d',
 'net/raphimc/viabedrock/protocol/storage/XpOrbDiagnostics.class':'d48304fda5c6035932c8526095913144e1c20701c28be1e97d51280e4bd4e7ac',
 'net/raphimc/viabedrock/protocol/storage/XpOrbDiagnostics$Orb.class':'8af99998eb379dc4b7716373b232a5cad6fc6f76fd9b801222e9f0ed240e28a9',
}
def sha(b): return hashlib.sha256(b).hexdigest()
def file_sha(p):
 with Path(p).open('rb') as f: return hashlib.file_digest(f,'sha256').hexdigest()
def index(z):
 names=z.namelist();assert len(names)==len(set(names)),'duplicate entries'
 assert z.testzip() is None,'CRC failure'
 return {n:sha(z.read(n)) for n in names}
def compare(before,after,changed,added):
 assert set(before)-set(after)==set(),'removed entries'
 assert set(after)-set(before)==set(added),'unexpected additions'
 actual={n for n in before if before[n]!=after[n]}
 assert actual==set(changed),f'changed scope differs: {actual}'
 return {'unchanged_entries':len(before)-len(actual),'changed_entries':sorted(actual),'added_entries':sorted(added),'removed_entries':[], 'all_other_entry_contents_identical':True,'no_duplicate_entries':True,'crc_ok':True}

def main():
 assert SOURCE.stat().st_size==215327204 and file_sha(SOURCE)=='8045b1222551c574a389bc8ec6c63f84c12bba3aa7c7d616480fe1762fdac15a'
 assert OUTPUT.stat().st_size==215635312 and file_sha(OUTPUT)=='cce72ee547155865c5196b0262deec9766bb9267741a755eaa72bbad74ae758a'
 config=json.loads((ANALYSIS/'end-audit-if48/package/build-inputs.json').read_text())
 previous_report=json.loads(Path('/workspace/minecraft/entregables/IF48-verificacion.json').read_text())
 with ZipFile(SOURCE) as before,ZipFile(OUTPUT) as after:
  b=index(before);a=index(after)
  root_scope=compare(b,a,{CORE,'fabric.mod.json','com/viaversion/viafabricplus/bedrock/render/BedrockFirstPersonItemRendering.class'},{BUNDLE})
  oldmeta=json.loads(before.read('fabric.mod.json'));newmeta=json.loads(after.read('fabric.mod.json'))
  expectedmeta={**oldmeta,'name':'ViaFabricPlus Bedrock - IF48','version':'1.1.1-StackAnvil-InteractionFix48.1'}
  assert newmeta==expectedmeta,'metadata unexpected delta'
  with ZipFile(BytesIO(before.read(CORE))) as oldcore,ZipFile(BytesIO(after.read(CORE))) as newcore:
   bc=index(oldcore);ac=index(newcore)
   core_scope=compare(bc,ac,{'net/raphimc/viabedrock/protocol/storage/ConnectedBlockShapes.class','net/raphimc/viabedrock/protocol/packet/ClientPlayerPackets.class'},{'net/raphimc/viabedrock/protocol/storage/ChorusPlantConnections.class','net/raphimc/viabedrock/protocol/packet/EndCreditsBridge.class'})
   for name,expected in CLASS_SHA.items():assert (ac if name.startswith('net/') else a)[name]==expected,name
   for name,expected in XP_SHA.items():assert bc[name]==ac[name]==expected,name
   protected_core={}
   for name,expected in previous_report['protected_core_class_sha256s'].items():
    assert bc[name]==ac[name]==expected,name;protected_core[name]=expected
   protected_root={}
   for name,expected in previous_report['protected_root_entry_sha256s'].items():
    assert b[name]==a[name]==expected,name;protected_root[name]=expected
  with ZipFile(BytesIO(after.read(BUNDLE))) as bundle:
   bundle_entries=index(bundle)
   selected={str(Path(path).relative_to(ANALYSIS)):expected for path,expected in config['source_file_sha256s'].items()}
   automatic={str((ANALYSIS/'end-audit-if48/package'/name).relative_to(ANALYSIS)) for name in ['package_if48.py','build_final.py','build-inputs.json','ESTADO.md']}
   assert len(selected)==88 and len(automatic)==4 and not set(selected)&automatic
   assert set(bundle_entries)==set(selected)|automatic and len(bundle_entries)==92
   for name,expected in selected.items():assert bundle_entries[name]==expected==file_sha(ANALYSIS/name),name
   for name in automatic:assert bundle_entries[name]==file_sha(ANALYSIS/name),name
   for name in bundle_entries:
    path=Path(name)
    assert path.suffix in {'.java','.py','.md','.txt','.json','.csv'},name
    assert not path.is_absolute() and '..' not in path.parts,name
    assert not set(path.parts)&{'reference','references','video','actual-source','boundary-src','boundary-classes','test-classes','tool-classes','decompiled','.git','attachments'},name
    assert path.name not in {'latest.log','latest.log.gz'} and not path.name.lower().startswith('latest'),name
   report_md='end-audit-if48/report/REVISION-UNO-POR-UNO.md'
   assert bundle_entries[report_md]=='c10b51fbf050ab804954df3472d422b5cad8ea644bbab41928ff2e3da75b6f12'
   report_json=json.loads(bundle.read('end-audit-if48/report/REVISION-UNO-POR-UNO.json'))
   report_csv=list(csv.DictReader(StringIO(bundle.read('end-audit-if48/report/REVISION-UNO-POR-UNO.csv').decode())))
   assert len(report_json['rows'])==len(report_csv)==246
   assert [x['id'] for x in report_json['rows']]==[x['id'] for x in report_csv]
   assert report_json['all_live_server_checks_pending'] is True and report_json['no_global_gameplay_pass'] is True
   readme_name='end-audit-if48/delivery/README.md'
   delivery_sha=bundle_entries[readme_name]
   readme=bundle.read(readme_name).decode()
   for text in ['El combate completo del dragón todavía no está certificado','sigue sin corregir','No se ejecutaron el cliente gráfico','[IF47-XP]','un solo addon Bedrock IF','1.1.1-StackAnvil-InteractionFix48.1']:
    assert text in readme,text
   evidence={name:expected for name,expected in sorted(bundle_entries.items())}
 result={
  'status':'PASS independent immutable final artifact review',
  'artifact':str(OUTPUT),'artifact_sha256':file_sha(OUTPUT),'artifact_size_bytes':OUTPUT.stat().st_size,
  'baseline_sha256':file_sha(SOURCE),'production_rebuilt':False,'builder_imported_or_executed':False,'historical_tests_rerun':False,
  'root':root_scope,'core':core_scope,'root_entries':len(a),'core_entries':len(ac),
  'exact_five_runtime_class_sha256s':CLASS_SHA,'exact_four_xp_observer_sha256s_preserved':XP_SHA,
  'protected_core_sha256s':protected_core,'protected_root_sha256s':protected_root,
  'fabric_only_name_version_changed':True,'fabric_version':newmeta['version'],
  'new_source_bundle':{'entry':BUNDLE,'entries':92,'selected_files':88,'automatic_build_files':4,'all_embedded_source_hashes_match_allowlist_and_local_files':True,'no_private_worlds_media_logs_test_classes_or_full_reference_sources_added':True,'crc_ok':True,'no_duplicates':True,'sha256s':evidence},
  'report_rows':246,'report_json_csv_ids_equal':True,'report_md_sha256':'c10b51fbf050ab804954df3472d422b5cad8ea644bbab41928ff2e3da75b6f12',
  'delivery_readme_sha256':delivery_sha,'readme_manual_scope_review':'Three changes limited to bow first-person, chorus Java connections, End credits acknowledgement. Explicitly no complete dragon/End certification; XP diagnostic unchanged, maps unresolved, no graphical client/Mixin/Bedrock execution claimed.',
  'xp_fix_claimed':False,'live_minecraft_graphics_or_server_executed':False,
 }
 TASK_ROOT.mkdir(exist_ok=True)
 (TASK_ROOT/'verification.json').write_text(json.dumps(result,ensure_ascii=False,indent=2)+'\n')
 print(json.dumps({k:result[k] for k in ['status','artifact_sha256','artifact_size_bytes','root_entries','core_entries','report_rows','delivery_readme_sha256']},indent=2))
if __name__=='__main__':main()
