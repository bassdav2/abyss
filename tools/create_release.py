"""Lokale Auslieferung mit echten Asset-Dateien, nicht Git-LFS-Zeigern."""
from pathlib import Path
import hashlib,json,subprocess,zipfile
ROOT=Path(__file__).resolve().parents[1]
OUT=ROOT/'release';OUT.mkdir(exist_ok=True)
APP=ROOT/'dist/Abyss.app'
assert (APP/'Contents/MacOS/Abyss').is_file()
assert (ROOT/'build/docs/javadoc/index.html').is_file()
source=OUT/'Abyss_v0.2_Source_2026-09-22.zip'
mac=OUT/'Abyss_v0.2_macOS_AppleSilicon_2026-09-22.zip'
if mac.exists():mac.unlink()
subprocess.run(['/usr/bin/ditto','-c','-k','--sequesterRsrc','--keepParent',str(APP),str(mac)],check=True)
tracked=subprocess.check_output(['git','ls-files','-z'],cwd=ROOT).decode().split('\0')
with zipfile.ZipFile(source,'w',zipfile.ZIP_DEFLATED,compresslevel=6) as archive:
    for relative in sorted(filter(None,tracked)):
        path=ROOT/relative
        if path.is_file():archive.write(path,'abyss-source/'+relative)
    for base,prefix in [(ROOT/'build/docs/javadoc','abyss-source/api'),(ROOT/'build/test-results/test','abyss-source/verification/junit')]:
        for path in sorted(base.rglob('*')):
            if path.is_file() and ('binary' not in path.parts):archive.write(path,prefix+'/'+str(path.relative_to(base)))
    archive.writestr('abyss-source/SOURCE_PACKAGE.txt','ABYSS local source package\nActual resource bytes are included. Git LFS is not needed to unpack this ZIP.\nJDK 25 and first-build network access are required.\nAPI documentation: api/index.html\n')
with zipfile.ZipFile(source) as archive:
    assert archive.testzip() is None
    assert archive.read('abyss-source/src/main/resources/art/rooms/aft.png').startswith(b'\x89PNG\r\n\x1a\n')
    assert b'<html' in archive.read('abyss-source/api/index.html').lower()
with zipfile.ZipFile(mac) as archive:
    assert archive.testzip() is None
    assert 'Abyss.app/Contents/MacOS/Abyss' in archive.namelist()
rows=[]
for path in [mac,source]:rows.append({'file':path.name,'bytes':path.stat().st_size,'sha256':hashlib.sha256(path.read_bytes()).hexdigest()})
(OUT/'SHA256SUMS.txt').write_text(''.join(row['sha256']+'  '+row['file']+'\n' for row in rows))
(OUT/'manifest.json').write_text(json.dumps({'sourceCommit':subprocess.check_output(['git','rev-parse','HEAD'],cwd=ROOT,text=True).strip(),'archives':rows},indent=2)+'\n')
print(json.dumps(rows,indent=2))
