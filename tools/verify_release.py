"""Entpackt beide lokalen Archive und prüft Build, Ressourcen und nativen App-Start."""
from datetime import datetime, timezone
from pathlib import Path
import hashlib
import json
import os
import re
import subprocess
import tempfile
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
RELEASE = ROOT / 'release'
manifest = json.loads((RELEASE / 'manifest.json').read_text())
work = Path(tempfile.mkdtemp(prefix='release-v02-check-', dir=ROOT / 'build'))
java = Path('/opt/homebrew/opt/openjdk@25/libexec/openjdk.jdk/Contents/Home')
env = dict(os.environ, JAVA_HOME=str(java), PATH=str(java / 'bin') + os.pathsep + os.environ['PATH'])


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


for row in manifest['archives']:
    path = RELEASE / row['file']
    assert digest(path) == row['sha256'], f'Archiv-Prüfsumme falsch: {path}'
    subprocess.run(['/usr/bin/ditto', '-x', '-k', str(path), str(work)], check=True)

source = work / 'abyss-source'
assert not (source / 'build').exists(), 'Projekt-Build muss frisch sein'
log = work / 'fresh-source-build.log'
with log.open('w') as output:
    subprocess.run([str(source / 'gradlew'), 'test', 'checkJavaFormat', 'javadoc'],
                   cwd=source, env=env, stdout=output, stderr=subprocess.STDOUT, check=True)
tests = {key: 0 for key in ('tests', 'failures', 'errors', 'skipped')}
xml_files = list((source / 'build/test-results/test').glob('TEST-*.xml'))
assert xml_files, 'JUnit-Berichte fehlen'
for path in xml_files:
    suite = ET.parse(path).getroot()
    for key in tests:
        tests[key] += int(suite.get(key, '0'))
assert tests['tests'] == 53 and tests['failures'] == tests['errors'] == tests['skipped'] == 0

paths = [p for folder in ('src', 'gradle') for p in (ROOT / folder).rglob('*') if p.is_file()]
paths += [ROOT / name for name in ('build.gradle', 'settings.gradle', 'gradle.properties',
                                 'gradlew', 'gradlew.bat', 'run.command')]
rows = []
for path in sorted(paths):
    relative = path.relative_to(ROOT)
    assert (source / relative).is_file(), f'Quelldatei fehlt: {relative}'
    assert digest(path) == digest(source / relative), f'Quelldatei weicht ab: {relative}'
    rows.append({'path': relative.as_posix(), 'sha256': digest(path)})

app = work / 'Abyss.app'
with (work / 'codesign.log').open('w') as output:
    subprocess.run(['/usr/bin/codesign', '--verify', '--deep', '--strict', str(app)],
                   stdout=output, stderr=subprocess.STDOUT, check=True)
jar = app / 'Contents/app/abyss.jar'
assert digest(jar) == digest(ROOT / 'dist/Abyss.app/Contents/app/abyss.jar')
app_env = dict(env, JAVA_TOOL_OPTIONS=f'-Dabyss.saveDir={work}/isolated-save -Dabyss.silent=true')
launch_log = work / 'native-launch.log'
with launch_log.open('w') as output:
    subprocess.run([str(app / 'Contents/MacOS/Abyss'), '--start=true',
                    f'--capture={work}/unpacked-app.png', '--exit=true', '--after=3'],
                   cwd=work, env=app_env, stdout=output, stderr=subprocess.STDOUT,
                   timeout=40, check=True)
capture = work / 'unpacked-app.png'
assert capture.read_bytes().startswith(b'\x89PNG\r\n\x1a\n')
match = re.search(r'frames=(\d+)', launch_log.read_text())
assert match and int(match.group(1)) > 0
report = {
    'verifiedUtc': datetime.now(timezone.utc).isoformat(),
    'sourceCommitTested': manifest['sourceCommit'],
    'freshUnzipBuild': {
        'tasks': ['test', 'checkJavaFormat', 'javadoc'],
        'allTasksExecuted': 'UP-TO-DATE' not in log.read_text(),
        'junit': tests,
        'dependencyCache': 'existing local Gradle cache reused; project build directory was absent',
    },
    'sourceFilesMatch': len(rows),
    'sourceFilesDigest': hashlib.sha256(json.dumps(rows, sort_keys=True).encode()).hexdigest(),
    'sourceFiles': rows,
    'unpackedMacApp': {
        'nativeLaunch': True,
        'capturedFrames': int(match.group(1)),
        'bundleJarMatchesOriginal': True,
        'bundleJarSha256': digest(jar),
        'adHocSignatureVerified': True,
    },
    'scope': 'Local Apple Silicon Mac only. No human input or listening test.',
}
(ROOT / 'docs/qa/release-check.json').write_text(json.dumps(report, indent=2) + '\n')
print(json.dumps({'result': 'PASS', 'junit': tests, 'sourceFilesMatch': len(rows),
                  'evidenceDirectory': str(work)}, indent=2))
