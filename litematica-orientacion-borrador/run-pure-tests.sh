#!/usr/bin/env bash
set -euo pipefail
TASK_ROOT="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
TASK_JDK="${TASK_JAVA_HOME:-/workspace/litematica-work/tools/jdk-25.0.5.0.3}"
mkdir -p "$TASK_ROOT/classes" "$TASK_ROOT/verification"
mapfile -t TASK_SOURCES < <(rg --files "$TASK_ROOT/src/main/java" "$TASK_ROOT/src/test/java" -g '*.java' | sort)
"$TASK_JDK/bin/javac" -Xlint:all -d "$TASK_ROOT/classes" "${TASK_SOURCES[@]}" > "$TASK_ROOT/verification/compile.txt" 2>&1
{
    "$TASK_JDK/bin/java" -Xverify:all -cp "$TASK_ROOT/classes" org.uiop.easyplacefix.rotation.RotationLeaseControllerTest
    "$TASK_JDK/bin/java" -Xverify:all -cp "$TASK_ROOT/classes" org.uiop.easyplacefix.rotation.ClientContextTrackerTest
    "$TASK_JDK/bin/java" -Xverify:all -cp "$TASK_ROOT/classes" org.uiop.easyplacefix.rotation.TaskAdmissionTest
    "$TASK_JDK/bin/java" -Xverify:all -cp "$TASK_ROOT/classes" org.uiop.easyplacefix.placement.LegalSupportSelectorTest
    "$TASK_JDK/bin/java" -Xverify:all -cp "$TASK_ROOT/classes" org.uiop.easyplacefix.placement.NativeActionQueueTest
} | tee "$TASK_ROOT/verification/result.txt"
python3 - "$TASK_ROOT" <<'PY'
import hashlib, json, pathlib, subprocess, sys
root = pathlib.Path(sys.argv[1])
paths = sorted(root.glob('src/**/*.java')) + [root/'run-pure-tests.sh']
result = {
    'scope': 'Pure JDK controller and per-packet state only',
    'status': 'PASS',
    'minecraft_facing_compiled': False,
    'mixin_transformation_tested': False,
    'bedrock_placement_tested': False,
    'command': str(root/'run-pure-tests.sh'),
    'result': (root/'verification/result.txt').read_text(),
    'files': [{'path': str(p.relative_to(root)), 'sha256': hashlib.sha256(p.read_bytes()).hexdigest()} for p in paths],
}
(root/'verification/receipt.json').write_text(json.dumps(result, indent=2)+'\n')
PY
