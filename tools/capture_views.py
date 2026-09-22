"""Rendert echte JavaFX-Screens mit isolierten QA-Speicherständen.

Dies ist eine Layoutprüfung, kein Nachweis manueller Bedienung. Keine OS-Eingabeautomatisierung.
"""
from pathlib import Path
import os
import subprocess
import sys

ROOT=Path(__file__).resolve().parents[1]
OUT=ROOT/'docs/qa/screens'
OUT.mkdir(parents=True,exist_ok=True)
JAVA='/opt/homebrew/opt/openjdk@25/libexec/openjdk.jdk/Contents/Home'
env=dict(os.environ,JAVA_HOME=JAVA,PATH=JAVA+'/bin:'+os.environ['PATH'])


def fixture(folder,depth):
    folder.mkdir(parents=True,exist_ok=True)
    folder.joinpath('profile.properties').write_text('version=1\nruns=7\nwins=1\nbestRoom=12\nbestCycle=1\nkills=124\nunlocked=PULSE,ARC,AEGIS\nvolume=0\nmusic=0\n')
    folder.joinpath('checkpoint.properties').write_text(
        f'version=1\nseed=73419\ncycle=0\ndepth={depth}\nbranch=0\nmodule=PULSE\nexplorer=false\n'
        'health=140\nenergy=120\nsalvage=45\nkills=23\nelapsed=420\n'
        'upgrade.SERVO=2\nupgrade.MEDICAL=2\nupgrade.RECOVERY=1\nupgrade.PLATING=1\nupgrade.CAPACITOR=1\n'
        'route='+','.join('0' for _ in range(depth+1))+'\n')


views=[('title',None,None),('loadout','loadout',None),('archive','archive',None),('settings','settings',None),
       ('help','help',None),('pause','pause',0),('route','route',3),('reward','reward',3),
       ('reactor',None,6),('boss',None,11)]
if len(sys.argv)>1:
    chosen=set(sys.argv[1:]);views=[v for v in views if v[0] in chosen]
for name,screen,depth in views:
    save=ROOT/'build/qa-views'/name
    if depth is not None:fixture(save,depth)
    args=[f'--capture={OUT/name}.png','--exit=true',f'--after={4.7 if name=="reactor" else 1.2}']
    if depth is not None:args.append('--resume=true')
    if screen:args.append('--qa-screen='+screen)
    result=subprocess.run([str(ROOT/'gradlew'),'run','-Psilent','-PqaSave='+str(save),'--args='+' '.join(args)],
                          cwd=ROOT,env=env,text=True,stdout=subprocess.PIPE,stderr=subprocess.STDOUT)
    (OUT/(name+'.log')).write_text(result.stdout)
    if result.returncode:print(result.stdout);raise SystemExit(result.returncode)
    if not (OUT/(name+'.png')).is_file():raise RuntimeError('Capture missing: '+name)
    print('CAPTURED',name,flush=True)
