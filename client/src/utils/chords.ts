const NOTES = [
    "C",
    "C#",
    "D",
    "D#",
    "E",
    "F",
    "F#",
    "G",
    "G#",
    "A",
    "A#",
    "B",
];

const MODE_INTERVALS: Record<string, number[]> = {
    "": [4, 3],
    m: [3, 4],
    dim: [3, 3],
    aug: [4, 4],
    sus2: [2, 5],
    sus4: [5, 2],
    "5": [7],
    "6": [4, 3, 2],
    m6: [3, 4, 2],
    "7": [4, 3, 3],
    m7: [3, 4, 3],
    maj7: [4, 3, 4],
    mmaj7: [3, 4, 4],
};

const FLAT_TO_SHARP: Record<string, string> = {
    Db: "C#",
    Eb: "D#",
    Gb: "F#",
    Ab: "G#",
    Bb: "A#",
};

const NOTE_PATTERN = NOTES.sort((a, b) => b.length - a.length).join("|");
const MODE_PATTERN = Object.keys(MODE_INTERVALS)
    .filter((mode) => mode)
    .sort((a, b) => b.length - a.length)
    .join("|");

const CHORD_PATTERN = `(?:${NOTE_PATTERN})(?:${MODE_PATTERN})?-(?:\\d+(?:\\/\\d+)?)`;
const FULL_PATTERN = new RegExp(`^${CHORD_PATTERN}(?:,${CHORD_PATTERN})*$`);

export const normalizeChordProgression = (value: string): string => {
    let rhythmString = value.replace(/\s+/g, "");

    for (const [flat, sharp] of Object.entries(FLAT_TO_SHARP)) {
        rhythmString = rhythmString.split(flat).join(sharp);
    }

    return rhythmString;
};

export const isValidChordProgression = (value: string): boolean => {
    return FULL_PATTERN.test(normalizeChordProgression(value));
};