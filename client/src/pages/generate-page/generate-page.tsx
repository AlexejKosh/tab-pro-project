import { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";

import { getGenres } from "@/api/genreApi";
import { saveTab } from "@/api/tabApi";
import { useGenerationStore } from "@/store/generationStore";
import { useUiStore } from "@/store/uiStore";
import { useAuthStore } from "@/store/authStore";
import { isValidChordProgression, normalizeChordProgression } from "@/utils/chords";
import GenerateForm from "@/components/tabs/generate-form/generate-form";
import TabInfo from "@/components/tabs/tab-info/tab-info";

import { useDocumentTitle } from "@/hooks/use-document-title";

import type { Genre } from "@/types/genre";
import { extractErrorMessage } from "@/utils/error";

type FormState = {
    genreId: number | null;
    signature: "3/4" | "4/4";
    tonality: number;
    bpm: number;
    chordProgression: string;
};

const DEFAULT_FORM: FormState = {
    genreId: null,
    signature: "4/4",
    tonality: 0,
    bpm: 120,
    chordProgression: "",
};

export default function GeneratePage() {
    useDocumentTitle("Генерация соло");

    const navigate = useNavigate();
    const { lastRequest, result, isGenerating, startGeneration, setResult, reset } =
        useGenerationStore();
    const { setMessage } = useUiStore();

    const [genres, setGenres] = useState<Genre[]>([]);
    const [genresLoading, setGenresLoading] = useState(true);
    const [form, setForm] = useState<FormState>(DEFAULT_FORM);
    const [tabTitle, setTabTitle] = useState("");
    const isAuthenticated =
        useAuthStore(state => state.isAuthenticated);

    useEffect(() => {
        const loadGenres = async () => {
            try {
                setGenresLoading(true);
                const data = await getGenres();
                setGenres(data);
            } catch {
                setMessage({
                    message: "Не удалось загрузить список жанров.",
                    type: "error",
                });
            } finally {
                setGenresLoading(false);
            }
        };
        void loadGenres();
    }, [setMessage]);

    useEffect(() => {
        if (genres.length === 0) {
            return;
        }

        setForm((current) => {
            if (current.genreId !== null) {
                return current;
            }

            return {
                ...current,
                genreId: lastRequest?.genreId ?? genres[0].id,
                signature: (lastRequest?.signature as "3/4" | "4/4") ?? "4/4",
                tonality: lastRequest?.musicKey ?? 0,
                bpm: lastRequest?.bpm ?? 120,
                chordProgression: lastRequest?.chordProgression ?? "",
            };
        });
    }, [genres, lastRequest]);

    const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
        e.preventDefault();

        if (isGenerating) {
            return;
        }

        if (form.genreId === null) {
            setMessage({
                message: "Выберите жанр.",
                type: "error",
            });

            return;
        }

        if (!form.chordProgression.trim()) {
            setMessage({
                message: "Введите последовательность аккордов.",
                type: "error",
            });

            return;
        }

        const normalizedChords = normalizeChordProgression(form.chordProgression);

        if (!isValidChordProgression(form.chordProgression)) {
            setMessage({
                message:
                "Некорректный формат аккордов. Пример: C-1/2,G7-1/4,Am-1",
                type: "error",
            });

            return;
        }

        try {
            await startGeneration({
                genreId: form.genreId,
                signature: form.signature,
                musicKey: form.tonality,
                bpm: form.bpm,
                chordProgression: normalizedChords,
            });
            setMessage({
                message: "Соло успешно сгенерировано.",
                type: "success",
            });
        } catch (err: any) {
            setMessage({
                message: extractErrorMessage(
                    err,
                    "Не удалось сгенерировать табулатуру.",
                ),
                type: "error",
            });
        }
    };

    const handleTryAgain = () => {
        setResult(null);
    };

    const handleSave = async (e: React.FormEvent<HTMLFormElement>) => {
        e.preventDefault();

        if (!result) {
            return;
        }

        const title = tabTitle.trim();

        if (!title) {
            setMessage({
                message: "Введите название табулатуры.",
                type: "error",
            });

            return;
        }

        try {
            await saveTab({
                title,
                genreId: result.genreId,
                signature: result.signature,
                musicKey: result.musicKey,
                bpm: result.bpm,
                chordProgression: result.chordProgression,
                tabData: result.tabData,
                audioData: result.audioData,
            });
            setMessage({
                message: "Табулатура успешно сохранена.",
                type: "success",
            });
            setTabTitle("");
            reset();
            navigate("/tabs", { replace: true });
        } catch (err: any) {
            setMessage({
                message: extractErrorMessage(
                    err,
                    "Не удалось сохранить табулатуру.",
                ),
                type: "error",
            });
        }
    };

    if (result) {
        return (
            <main className="main main--usual-page">
                <Link to="/" className="form-link">
                    На главную
                </Link>

                <form className="generate-form" onSubmit={handleSave}>
                    <TabInfo
                        title="Сгенерированное соло"
                        genreId={result.genreId}
                        tempo={result.bpm}
                        signature={result.signature}
                        keyIndex={result.musicKey}
                        chords={result.chordProgression}
                        tabData={result.tabData}
                        audioData={result.audioData}
                    />

                    <div className="bottom-generate-section">
                        <button
                            className="generate-button generate-button--try-again"
                            type="button"
                            onClick={handleTryAgain}
                        >
                            Попробовать снова
                        </button>
                        {isAuthenticated && (
                            <>
                                <textarea
                                    className="generate-textarea generate-textarea--name"
                                    placeholder="Введите название..."
                                    value={tabTitle}
                                    onChange={(e) => setTabTitle(e.target.value)}
                                />
                                <button className="generate-button generate-button--save" type="submit">
                                    Сохранить
                                </button>
                            </>
                        )}
                    </div>
                </form>
            </main>
        );
    }

    return (
        <main className="main main--usual-page">
            <Link to="/" className="form-link">
                На главную
            </Link>

            <GenerateForm
                genres={genres}
                loading={genresLoading || isGenerating}
                genreId={form.genreId}
                signature={form.signature}
                tonality={form.tonality}
                bpm={form.bpm}
                chordProgression={form.chordProgression}
                onGenreChange={(genreId) =>
                    setForm((current) => ({ ...current, genreId }))
                }
                onSignatureChange={(signature) =>
                    setForm((current) => ({
                        ...current,
                        signature: signature as "3/4" | "4/4",
                    }))
                }
                onTonalityChange={(tonality) =>
                    setForm((current) => ({ ...current, tonality }))
                }
                onBpmChange={(bpm) => setForm((current) => ({ ...current, bpm }))}
                onChordProgressionChange={(value) =>
                    setForm((current) => ({ ...current, chordProgression: value }))
                }
                onSubmit={handleSubmit}
            />
        </main>
    );
};