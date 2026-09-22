"""Fasst vorhandene Prüfprotokolle zusammen und prüft die Ressourcen im App-JAR."""
from pathlib import Path
from datetime import datetime,timezone
import hashlib,json,re,statistics,subprocess,xml.etree.ElementTree as ET,zipfile
ROOT=Path(__file__).resolve().parents[1]
counts={'tests':0,'failures':0,'errors':0,'skipped':0}
suites=[];campaigns=[]
for path in sorted((ROOT/'build/test-results/test').glob('TEST-*.xml')):
    element=ET.parse(path).getroot()
    for key in counts:counts[key]+=int(element.attrib.get(key,0))
    suites.append({'name':element.attrib['name'],'tests':int(element.attrib['tests'])})
    output=element.findtext('system-out') or ''
    for line in output.splitlines():
        if line.startswith('CAMPAIGN '):campaigns.append(dict(re.findall(r'(\w+)=([^ ]+)',line)))
assert counts['tests']>0 and counts['failures']==0 and counts['errors']==0
assert len(campaigns)==144
assert all(c['won']=='true' for c in campaigns)
ui=(ROOT/'docs/qa/ui-smoke.txt').read_text().splitlines()
assert ui[-1]=='PASS'
jar=ROOT/'dist/Abyss.app/Contents/app/abyss.jar'
manifest=json.loads((ROOT/'art-source/asset-manifest.json').read_text())
with zipfile.ZipFile(jar) as archive:
    for asset in manifest['assets']:
        assert hashlib.sha256(archive.read(asset['path'])).hexdigest()==asset['sha256'],asset['path']
subprocess.run(['codesign','--verify','--deep','--strict',str(ROOT/'dist/Abyss.app')],check=True)
report={'verifiedUtc':datetime.now(timezone.utc).isoformat(),'junit':counts,'suites':suites,
        'campaigns':{'runs':len(campaigns),'wins':sum(c['won']=='true' for c in campaigns),
                     'note':'InputFrame bot; not a human playtime or usability measurement'},
        'javafxComponentChecks':sum(line.startswith('PASS ') for line in ui),
        'bundleResourceHashesMatched':len(manifest['assets']),
        'bundleJarSha256':hashlib.sha256(jar.read_bytes()).hexdigest(),
        'macCodeSignature':'ad-hoc signature verifies; not notarized',
        'humanOsInputAndListeningTest':'not performed; desktop locked',
        'pdf':'21 pages; rendered and visually inspected'}
(ROOT/'docs/qa/test-summary.json').write_text(json.dumps(report,indent=2)+'\n')
print(json.dumps(report,indent=2))
