"""Zusätzliche eigene Klänge für ABYSS 1.0: Treffer, Explosionen, Beute, Boss und ein Motiv für das
Forschungsdeck. Reine Synthese mit NumPy, keine Fremd-Samples. Bestehende Dateien bleiben unverändert."""
from pathlib import Path
import wave
import numpy as np

OUT = Path(__file__).resolve().parents[1] / 'src/main/resources/audio'
RATE = 44100
rng = np.random.default_rng(2026)


def write(name, samples):
    samples = np.asarray(samples, dtype=float)
    peak = max(1e-6, float(np.max(np.abs(samples))))
    data = (np.clip(samples / peak * .9, -.98, .98) * 32767).astype('<i2')
    with wave.open(str(OUT / (name + '.wav')), 'wb') as f:
        f.setnchannels(1 if samples.ndim == 1 else samples.shape[1])
        f.setsampwidth(2)
        f.setframerate(RATE)
        f.writeframes(data.tobytes())


def time(seconds):
    return np.arange(round(seconds * RATE)) / RATE


def fade(signal, seconds=.008):
    n = min(int(seconds * RATE), len(signal) // 2)
    signal[:n] *= np.linspace(0, 1, n)
    signal[-n:] *= np.linspace(1, 0, n)
    return signal


def lowpass(signal, alpha):
    out = np.zeros_like(signal)
    acc = 0.0
    for i, v in enumerate(signal):
        acc += alpha * (v - acc)
        out[i] = acc
    return out


def square(freq, t):
    return np.sign(np.sin(2 * np.pi * freq * t))


t = time(.25)
write('crit', fade(.5 * np.sin(2 * np.pi * 150 * t) * np.exp(-t * 30) + .35 * np.sin(2 * np.pi * 1320 * t) * np.exp(-t * 14)
                   + .25 * rng.normal(0, .5, len(t)) * np.exp(-t * 60)))
t = time(.35)
clang = sum(a * np.sin(2 * np.pi * f * t) * np.exp(-t * d) for f, a, d in [(523, .4, 10), (1320, .3, 14), (2090, .22, 18), (3150, .1, 24)])
write('block', fade(clang + .15 * rng.normal(0, .5, len(t)) * np.exp(-t * 80)))
t = time(1.0)
rumble = lowpass(rng.normal(0, 1, len(t)), .05) * np.exp(-t * 3.2)
boom = .8 * np.sin(2 * np.pi * (70 * t - 28 * t ** 2)) * np.exp(-t * 4)
write('explosion', fade(rumble * 1.6 + boom + .3 * rng.normal(0, .5, len(t)) * np.exp(-t * 25), .02))
t = time(.32)
buzz = .35 * square(92, t) * (rng.random(len(t)) > .35) * np.exp(-t * 7) + .25 * np.sin(2 * np.pi * 1800 * t) * np.exp(-t * 20)
write('zap', fade(lowpass(buzz, .35)))
t = time(.7)
shimmer = sum(.12 * np.sin(2 * np.pi * f * t) * np.exp(-t * (4 + i)) for i, f in enumerate([2093, 2637, 3136, 4186]))
write('freeze', fade(shimmer + .08 * rng.normal(0, .5, len(t)) * np.exp(-t * 9)))
t = time(.42)
blips = np.zeros(len(t))
for i in range(5):
    dt = t - i * .07
    gate = dt >= 0
    blips += .25 * np.sin(2 * np.pi * (300 + i * 120) * dt) * np.exp(-np.maximum(dt, 0) * 30) * gate
write('spawn', fade(blips))
t = time(.3)
write('harpoon', fade(.6 * np.sin(2 * np.pi * (180 * t - 120 * t ** 2)) * np.exp(-t * 22) + .3 * lowpass(rng.normal(0, 1, len(t)), .2) * np.exp(-t * 9)))
t = time(.14)
write('land', fade(.6 * np.sin(2 * np.pi * 62 * t) * np.exp(-t * 35) + .25 * lowpass(rng.normal(0, 1, len(t)), .1) * np.exp(-t * 40)))
t = time(.22)
crack = lowpass(rng.normal(0, 1, len(t)), .45) * np.exp(-t * 22)
for onset in (0, .045):
    dt = t - onset
    crack += .5 * rng.normal(0, 1, len(t)) * np.exp(-np.maximum(dt, 0) * 200) * (dt >= 0)
write('crate', fade(crack))
t = time(.09)
write('pickup', fade(.3 * square(np.where(t < .04, 988, 1480), t) * np.exp(-t * 18)))
t = time(.32)
arp = np.zeros(len(t))
for i, f in enumerate([784, 988, 1319]):
    dt = t - i * .07
    arp += .22 * np.sin(2 * np.pi * f * dt) * np.exp(-np.maximum(dt, 0) * 12) * (dt >= 0)
write('core', fade(arp))
t = time(.8)
hiss = lowpass(rng.normal(0, 1, len(t)), .3) * np.exp(-t * 4) * .5
clunk = .7 * np.sin(2 * np.pi * 55 * (t - .55)) * np.exp(-np.maximum(t - .55, 0) * 18) * (t >= .55)
write('door', fade(hiss + clunk, .02))
t = time(1.4)
mod = np.sin(2 * np.pi * 31 * t) * 3
growl = np.sin(2 * np.pi * (58 * t) + mod) * (1 - np.exp(-t * 8)) * np.exp(-t * 1.6)
write('roar', fade(.7 * growl + .35 * lowpass(rng.normal(0, 1, len(t)), .08) * np.exp(-t * 2), .03))
t = time(.8)
curse = sum(.15 * np.sin(2 * np.pi * (f * t - f * .25 * t ** 2)) * np.exp(-t * 3) for f in (311, 330, 466))
write('curse', fade(curse))

# Forschungsdeck: gläserne Glockentöne über einem langsamen Puls, nahtlos loopbar.
duration = 25.6
t = time(duration)
track = np.zeros(len(t))
root = 55.0
for i, ratio in enumerate([1, 1.5, 2.25]):
    hz = round(root * ratio * duration) / duration
    track += (.05 / (i + 1)) * np.sin(2 * np.pi * hz * t) * (.55 + .45 * np.sin(2 * np.pi * t / duration * 2 + i) ** 2)
for i, note in enumerate([4, 4.5, 6, 5.333, 4, 6.75, 6, 4.5]):
    delta = (t - i * 3.2) % duration
    hz = round(root * note * duration) / duration
    track += .05 * np.sin(2 * np.pi * hz * delta) * np.exp(-delta * .9) * (1 - np.exp(-delta * 40))
    track += .018 * np.sin(2 * np.pi * hz * 2.76 * delta) * np.exp(-delta * 2.2)
for beat in np.arange(0, duration, 1.6):
    offset = (t - beat) % duration
    track += .06 * np.sin(2 * np.pi * 42 * offset) * np.exp(-offset * 10)
write('music_research', np.stack([track, np.roll(track, 700)], axis=1))
print('Zusätzliche Klänge erzeugt:', sorted(p.stem for p in OUT.glob('*.wav')))
