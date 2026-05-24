export interface GenerateTabRequest {
    genreId: number;
    signature: string;
    musicKey: number;
    bpm: number;
    chordProgression: string;
}

export interface GenerateResponse {
    genreId: number;
    signature: string;
    musicKey: number;
    bpm: number;
    chordProgression: string;
    tabData: string;
    audioData: string;
}

export interface SaveTabRequest {
    title: string;
    genreId: number;
    signature: string;
    musicKey: number;
    bpm: number;
    chordProgression: string;
    tabData: string;
    audioData: string;
}

export interface TabResponse {
    id: number;
    genreId: number;
    title: string;
    signature: string;
    musicKey: number;
    bpm: number;
    chordProgression: string;
    tabData: string;
    audioData: string;
    createdAt: string;
}

export interface TabSummaryResponse {
    id: number;
    title: string;
    signature: string;
    genreId: number;
    chordProgression: string;
    createdAt: string;
}