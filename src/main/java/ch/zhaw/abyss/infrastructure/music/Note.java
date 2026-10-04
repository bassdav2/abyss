package ch.zhaw.abyss.infrastructure.music;

/**
 * Ein Notenereignis im Takt der Partitur.
 *
 * @param beat Beginn in Schlägen ab Stückanfang
 * @param length Dauer in Schlägen
 * @param pitch MIDI-Tonhöhe (beim Schlagzeug ohne Bedeutung)
 * @param velocity Anschlagstärke 0 bis 1
 */
record Note(double beat, double length, double pitch, double velocity) {
    Note shifted(double beats) {
        return new Note(beat + beats, length, pitch, velocity);
    }

    Note transposed(double semitones) {
        return new Note(beat, length, pitch + semitones, velocity);
    }
}
