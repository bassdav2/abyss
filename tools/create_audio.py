"""Originale synthetische Unterwasser-Atmosphäre und Spielsignale; keine Fremd-Samples."""
from pathlib import Path
import wave
import numpy as np

OUT = Path(__file__).resolve().parents[1] / 'src/main/resources/audio'
OUT.mkdir(parents=True, exist_ok=True)
RATE = 44100
rng = np.random.default_rng(73419)


def write(name, samples):
    samples = np.asarray(samples)
    peak = max(1.0, float(np.max(np.abs(samples))))
    data = (np.clip(samples / peak, -.98, .98) * 32767).astype('<i2')
    with wave.open(str(OUT / (name + '.wav')), 'wb') as f:
        f.setnchannels(1 if samples.ndim == 1 else samples.shape[1])
        f.setsampwidth(2)
        f.setframerate(RATE)
        f.writeframes(data.tobytes())


def time(seconds):
    return np.arange(round(seconds * RATE)) / RATE


def fade(signal, seconds=.01):
    n = min(int(seconds * RATE), len(signal) // 2)
    signal[:n] *= np.linspace(0, 1, n)
    signal[-n:] *= np.linspace(1, 0, n)
    return signal


for name, length, freq, decay, noise in [
    ('hit', .18, 118, 28, .42), ('hurt', .3, 66, 17, .26),
    ('swing', .17, 220, 25, .45), ('dash', .28, 160, 15, .27),
    ('jump', .18, 220, 19, .08), ('shot', .18, 640, 33, .20),
    ('down', .44, 72, 12, .35), ('click', .07, 940, 70, .05),
]:
    t = time(length)
    env = np.exp(-t * decay)
    signal = (.36 * np.sin(2 * np.pi * (freq * t - freq * .8 * t ** 2)) + noise * rng.normal(0, .5, len(t))) * env
    write(name, fade(signal))

for name, tones in [('upgrade', [440, 554.37, 659.25]), ('clear', [329.63, 440, 659.25]),
                    ('victory', [220, 293.66, 349.23, 440, 587.33]), ('defeat', [220, 174.61, 130.81])]:
    t = time(2.4)
    result = np.zeros(len(t))
    for i, hz in enumerate(tones):
        delta = np.maximum(0, t - i * .13)
        gate = t >= i * .13
        result += .12 * (np.sin(2 * np.pi * hz * delta) + .2 * np.sin(2 * np.pi * 2 * hz * delta)) * np.exp(-delta * 2.7) * gate
    write(name, fade(result, .025))

t = time(.85)
write('pulse', fade((.45 * np.sin(2 * np.pi * (110 * t - 47 * t ** 2)) +
                    .12 * rng.normal(0, .5, len(t))) * np.exp(-t * 5)))
t = time(.3)
write('warning', fade(np.sin(2 * np.pi * 730 * t) * np.sin(2 * np.pi * 9 * t) ** 2 * .1, .02))

# Periodischer FFT-Rauschanteil: keine sprunghafte Naht am Loop-Punkt.
length = 16
t = time(length)
freqs = np.fft.rfftfreq(len(t), 1 / RATE)
shape = np.zeros_like(freqs)
valid = (freqs > 26) & (freqs < 1600)
shape[valid] = 1 / np.sqrt(freqs[valid])
spectrum = (rng.normal(size=len(freqs)) + 1j * rng.normal(size=len(freqs))) * shape
noise = np.fft.irfft(spectrum, n=len(t))
noise /= np.max(np.abs(noise))
pad = .12 * np.sin(2 * np.pi * 55 * t) + .055 * np.sin(2 * np.pi * 82.5 * t)
pad += .035 * np.sin(2 * np.pi * 110 * t) * (.6 + .4 * np.sin(2 * np.pi * t / 8))
pad += noise * .18
sonar = np.zeros(len(t))
for onset in [2, 10]:
    dt = t - onset
    gate = dt >= 0
    sonar += .045 * np.sin(2 * np.pi * 880 * dt) * np.exp(-np.maximum(dt, 0) * 2.5) * gate
write('ambience', np.stack([pad + sonar, np.roll(pad, 713) + sonar * .75], axis=1))

t = time(32)
music = np.zeros(len(t))
notes = [146.832, 174.614, 164.814, 130.813, 146.832, 220, 196, 164.814]
for i, hz in enumerate(notes):
    dt = t - i * 4
    positive = np.maximum(dt, 0)
    env = (1 - np.exp(-positive * 2)) * np.exp(-positive * .45) * (dt >= 0)
    music += .075 * env * (np.sin(2 * np.pi * hz * dt) + .28 * np.sin(2 * np.pi * hz * 2.003 * dt))
music = fade(music, .4)
write('music', np.stack([music, np.roll(music, 370)], axis=1))
print(f'Created {len(list(OUT.glob("*.wav")))} original WAV assets.')

# Erweiterung: eigene Motive pro Sektion und eine rhythmische Boss-Variation.
# Periodische Hüllkurven und Frequenzen vermeiden Klicks an der Loop-Grenze.
for name,root,pace,color in [('music_engine',73.416, .8, .0),('music_command',65.406,1.6,.35),('music_boss',73.416,.4,.65)]:
    duration=25.6
    t=time(duration)
    track=np.zeros(len(t))
    for i,ratio in enumerate([1,1.5,2,2.4]):
        hz=round(root*ratio*duration)/duration
        track += (.040/(i+1))*np.sin(2*np.pi*hz*t)*(.62+.38*np.sin(2*np.pi*t/duration+i)**2)
    for beat in np.arange(0,duration,pace):
        offset=(t-beat)%duration
        track+=.12*np.sin(2*np.pi*(48*offset+3*(1-np.exp(-offset*25))))*np.exp(-offset*12)
        if name=='music_boss':
            track+=.027*np.sin(2*np.pi*440*offset)*np.exp(-offset*30)
    for i,note in enumerate([2,2.4,3,2.25,2,3,2.6667,2.25]):
        delta=(t-i*3.2)%duration
        hz=round(root*note*duration)/duration
        track+=(.035+color*.015)*np.sin(2*np.pi*hz*delta)*np.exp(-delta*1.3)*(1-np.exp(-delta*20))
    write(name,np.stack([track,np.roll(track,500)],axis=1))
print('Expanded audio library:',len(list(OUT.glob('*.wav'))))
