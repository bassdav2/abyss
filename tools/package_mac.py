"""Baut ein eigenständiges Apple-Silicon-.app-Bundle mit festem Java-25-Runtime."""
from pathlib import Path
import os
import platform
import shutil
import subprocess
import sys

ROOT=Path(__file__).resolve().parents[1]
if sys.platform!='darwin' or platform.machine() not in ('arm64','aarch64'):
    raise SystemExit('Dieser Paketierer ist für Apple Silicon macOS. Quellcode-Builds sind separat möglich.')
candidates=[Path(os.environ.get('JAVA_HOME','/nonexistent')),Path('/opt/homebrew/opt/openjdk@25/libexec/openjdk.jdk/Contents/Home')]
try:
    candidates.append(Path(subprocess.check_output(['/usr/libexec/java_home','-v','25'],text=True,stderr=subprocess.DEVNULL).strip()))
except (subprocess.CalledProcessError,FileNotFoundError):
    pass
JAVA=next((path for path in candidates if (path/'release').is_file() and 'JAVA_VERSION="25.' in (path/'release').read_text()),None)
if JAVA is None:
    raise SystemExit('JDK 25 nicht gefunden. JAVA_HOME auf eine Java-25-Installation setzen.')
BUILD=ROOT/'build'
env=dict(os.environ,JAVA_HOME=str(JAVA),PATH=str(JAVA/'bin')+os.pathsep+os.environ['PATH'])


def run(*args):
    subprocess.run([str(a) for a in args],cwd=ROOT,env=env,check=True)


run(ROOT/'gradlew','test','jar','copyRuntime','processResources')
modules=BUILD/'fx-modules'
modules.mkdir(exist_ok=True)
for file in modules.glob('*.jar'):
    file.unlink()
for jar in (BUILD/'runtime-libs').glob('*-mac-aarch64.jar'):
    shutil.copy2(jar,modules/jar.name)
runtime=BUILD/'mac-runtime'
if runtime.exists(): shutil.rmtree(runtime)
run(JAVA/'bin/jlink','--module-path',str(JAVA/'jmods')+os.pathsep+str(modules),
    '--add-modules','javafx.controls,javafx.media,java.desktop,java.logging,java.xml,jdk.unsupported',
    '--strip-debug','--no-man-pages','--no-header-files','--output',runtime)
run(JAVA/'bin/java','--class-path',BUILD/'classes/java/main',ROOT/'tools/MakeIcon.java',ROOT)
run('/usr/bin/iconutil','-c','icns',BUILD/'Abyss.iconset','-o',BUILD/'Abyss.icns')
staging=BUILD/'package-input'
staging.mkdir(exist_ok=True)
for file in staging.glob('*.jar'): file.unlink()
jar=max((BUILD/'libs').glob('abyss-*.jar'),key=lambda p:p.stat().st_mtime)
shutil.copy2(jar,staging/'abyss.jar')
out=ROOT/'dist'
out.mkdir(exist_ok=True)
app=out/'Abyss.app'
if app.exists(): shutil.rmtree(app)
# macOS/jpackage verlangt eine positive erste Versionskomponente. Spielstand 0.2 war Bundle 1.0.1,
# Spielversion 1.0 war Bundle 1.1.0, Spielversion 1.1 ist Bundle 1.2.0.
run(JAVA/'bin/jpackage','--type','app-image','--name','Abyss','--app-version','1.2.0',
    '--description','Vom Heck bis zur Brücke. Ein Tiefsee-Roguelite.',
    '--vendor','Abyss Project','--input',staging,'--main-jar','abyss.jar',
    '--main-class','ch.zhaw.abyss.Launcher','--runtime-image',runtime,'--dest',out,
    '--icon',BUILD/'Abyss.icns','--mac-package-identifier','ch.zhaw.abyss',
    '--java-options','--add-modules=javafx.controls,javafx.media',
    '--java-options','--enable-native-access=javafx.graphics,javafx.media')
print('ABYSS_APP_READY',app)
