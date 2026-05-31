import { useEffect, useState } from "react";
import { getGenreById } from "@/api/genreApi";
import { base64ToAudioUrl } from "@/utils/audio";
import { exportTabToDocx } from "@/utils/exportTabToDocx";

interface Props {
    title: string;
    genreId: number;
    tempo: number;
    signature: string;
    keyIndex: number;
    chords: string;
    tabData: string;
    audioData: string;
}

const KEYS = [
    "C/Am",
    "Db/Bbm",
    "D/Bm",
    "Eb/Cm",
    "E/C#m",
    "F/Dm",
    "F#/Ebm",
    "G/Em",
    "Ab/Fm",
    "A/F#m",
    "Bb/Gm",
    "B/G#m"
];

export default function TabInfo({
    title,
    genreId,
    tempo,
    signature,
    keyIndex,
    chords,
    tabData,
    audioData,
}: Props) {

    const [genreName, setGenreName] =
        useState<string>("Загрузка...");

    const [audioUrl, setAudioUrl] =
        useState<string | null>(null);

    const [isExporting, setIsExporting] = useState(false);

    useEffect(() => {
        const loadGenre = async () => {
            try {
                const genre = await getGenreById(genreId);
                const map: Record<string, string> = {
                    rock: "рок",
                    metal: "метал",
                    blues: "блюз",
                };
                const name = genre.name || "";
                const russian = map[name.toLowerCase()] || name;
                setGenreName(russian);
            } catch {
                setGenreName("Неизвестно");
            }
        };
        loadGenre();
    }, [genreId]);

    useEffect(() => {
        if (!audioData) {
            setAudioUrl(null);

            return;
        }

        const url = base64ToAudioUrl(audioData);
        setAudioUrl(url);

        return () => {
            if (url && url.startsWith("blob:")) {
                URL.revokeObjectURL(url);
            }
        };

    }, [audioData]);

    const handleDownload = async () => {
        try {
            setIsExporting(true);

            await exportTabToDocx({
                title,
                rhythm: chords,
                tabData,
            });
        } finally {
            setIsExporting(false);
        }
    };

    return (
        <>
            <h1 className="main-title main-title--usual-page">
                {title}
            </h1>
            <p className="main-text">
                <b>Жанр:</b> {genreName};{" "}
                <b>темп:</b> {tempo} BPM;{" "}
                <b>размер:</b> {signature};{" "}
                <b>тональность:</b>{" "}
                {KEYS[keyIndex]}
            </p>
            <p className="main-text">
                Аккорды:
            </p>
            <textarea
                className="generate-textarea generate-textarea--chords"
                value={chords}
                readOnly
            />
            <p className="main-text">
                Сгенерированная табулатура:
            </p>
            <textarea
                className="generate-textarea generate-textarea--tabs"
                value={tabData}
                readOnly
            />
            <button
                type="button"
                className="download-button"
                onClick={handleDownload}
                disabled={isExporting}
            >
                {isExporting ? "Формирование документа..." : "Скачать DOCX"}
            </button>
            <p className="main-text">
                Аудио представление:
            </p>
            {audioUrl ? (
                <audio controls className="audio-player">
                    <source src={audioUrl} type="audio/mpeg" />
                    Ваш браузер не поддерживает
                    элемент audio.
                </audio>
            ) : (
                <p className="main-text">Аудио недоступно.</p>
            )}
        </>
    );
}