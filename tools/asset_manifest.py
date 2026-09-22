"""Inventar und Prüfsummen; liest Bilder, verändert keine Grafiken."""
from pathlib import Path
import hashlib,json,struct,wave
ROOT=Path(__file__).resolve().parents[1]
RESOURCE=ROOT/'src/main/resources'
items=[]
for path in sorted(RESOURCE.rglob('*')):
    if not path.is_file():continue
    data=path.read_bytes()
    item={'path':str(path.relative_to(RESOURCE)),'bytes':len(data),'sha256':hashlib.sha256(data).hexdigest()}
    if path.suffix=='.png':
        item['width'],item['height']=struct.unpack('>II',data[16:24])
        if '/rooms/' in str(path):item['source']='OpenAI Imagegen; docs/ART_PROMPTS.md'
        elif '/actors/' in str(path):item['source']='Blender; tools/render_actors.py'
        else:item['source']='Existing approved ABYSS concept image; docs/CONCEPT-PROMPTS.md'
    if path.suffix=='.wav':
        with wave.open(str(path)) as audio:
            item.update(channels=audio.getnchannels(),sampleRate=audio.getframerate(),sampleWidth=audio.getsampwidth(),duration=audio.getnframes()/audio.getframerate())
        item['source']='Original synthesis; tools/create_audio.py'
    items.append(item)
report={'format':1,'logicalStage':[1600,900],'floorY':620,'actorFootAnchor':[.5,.944],'assets':items}
(ROOT/'art-source/asset-manifest.json').write_text(json.dumps(report,indent=2,ensure_ascii=False)+'\n')
print('MANIFEST',len(items),'assets',sum(i['bytes'] for i in items),'bytes')
