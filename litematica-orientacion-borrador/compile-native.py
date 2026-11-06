#!/usr/bin/env python3
"""Compile the draft against genuine supplied classes; never creates a mod JAR."""
from pathlib import Path
import hashlib
import json
import os
import subprocess
import sys
import zipfile

root=Path(__file__).resolve().parent
jdk=Path(os.environ.get('TASK_JAVA_HOME','/workspace/litematica-work/tools/jdk-25.0.5.0.3'))
classpath_file=Path(os.environ.get('TASK_CLASSPATH_FILE','/workspace/litematica-work/runtime/new-inputs/compilation-classpath.txt'))
classpath=classpath_file.read_text().strip()
extras=Path(os.environ.get('TASK_MIXINEXTRAS_JAR','/workspace/litematica-work/runtime/new-inputs/lib/mixinextras-fabric-0.5.5.jar'))
if extras.exists():
    if str(extras) not in classpath.split(os.pathsep):
        classpath += os.pathsep+str(extras)
    with zipfile.ZipFile(extras) as archive:
        extras_version=json.loads(archive.read('fabric.mod.json'))['version']
else:
    raise SystemExit('Missing genuine MixinExtras dependency: '+str(extras))
sources=sorted((root/'src/main/java').rglob('*.java'))+sorted((root/'minecraft-facing/src/main/java').rglob('*.java'))
output=root/'build/native-classes'
output.mkdir(parents=True,exist_ok=True)
verification=root/'verification'
verification.mkdir(exist_ok=True)
command=[str(jdk/'bin/javac'),'-proc:none','-Xlint:all','-cp',classpath,'-d',str(output)]+[str(p) for p in sources]
result=subprocess.run(command,capture_output=True,text=True)
log=result.stdout+result.stderr
(verification/'native-compile.txt').write_text(log)
receipt={
    'status':'PASS_COMPILE_ONLY' if result.returncode==0 else 'BLOCKED_COMPILE',
    'exit_code':result.returncode,
    'source_count':len(sources),
    'command':command,
    'genuine_classpath_file':str(classpath_file),
    'genuine_classpath_file_sha256':hashlib.sha256(classpath_file.read_bytes()).hexdigest(),
    'mixinextras':{'file':str(extras),'version':extras_version,
                   'sha256':hashlib.sha256(extras.read_bytes()).hexdigest()},
    'mixin_transformed':False,'game_or_BDS_tested':False,'jar_packaged':False,
    'log_file':'verification/native-compile.txt',
    'log_sha256':hashlib.sha256(log.encode()).hexdigest(),
    'sources':[{'path':str(p.relative_to(root)),'sha256':hashlib.sha256(p.read_bytes()).hexdigest()} for p in sources],
}
(verification/'native-compile.json').write_text(json.dumps(receipt,indent=2)+'\n')
print(receipt['status'], 'sources='+str(len(sources)))
print(log,end='')
sys.exit(result.returncode)
