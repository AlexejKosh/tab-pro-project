import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";

import { getTabs } from "@/api/tabApi";
import { getGenreById } from "@/api/genreApi";
import TabsTable from "@/components/tabs/tabs-table/tabs-table";
import { Link } from "react-router-dom";

import { useDocumentTitle } from "@/hooks/use-document-title";

import type { TabSummaryResponse } from "@/types/tab";
import type { Genre } from "@/types/genre";

type SortOption = "date" | "title" | "genre";

interface SavedTabViewModel extends TabSummaryResponse {
    genreName: string;
}

const GENRE_LABELS: Record<string, string> = {
    Rock: "Рок",
    Metal: "Метал",
    Blues: "Блюз",
};

const formatDate = (isoDate: string): string => {
    const date = new Date(isoDate);

    return new Intl.DateTimeFormat("ru-RU").format(date);
};

const getRussianGenreName = (englishName: string): string => {
    return GENRE_LABELS[englishName] ?? englishName;
};

export default function SavedTabsPage() {
    useDocumentTitle("Сохранённые табулатуры");

    const [tabs, setTabs] = useState<SavedTabViewModel[]>([]);
    const [search, setSearch] = useState("");
    const [sortBy, setSortBy] = useState<SortOption>("date");
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    const navigate = useNavigate();

    useEffect(() => {
        const loadTabs = async () => {
            try {
                setIsLoading(true);
                setError(null);
                const tabsResponse = await getTabs();
                const uniqueGenreIds = [...new Set(tabsResponse.map((tab) => tab.genreId))];
                const genres = await Promise.all(
                    uniqueGenreIds.map(async (id) => {
                        const genre = (await getGenreById(id)) as Genre;
                        return genre;
                    })
                );
                const genreMap = new Map<number, string>();
                genres.forEach((genre) => {
                    genreMap.set(genre.id, getRussianGenreName(genre.name));
                });
                const enrichedTabs: SavedTabViewModel[] = tabsResponse.map((tab) => ({
                    ...tab,
                    genreName: genreMap.get(tab.genreId) ?? "Неизвестно",
                }));
                setTabs(enrichedTabs);
            } catch {
                setError("Не удалось загрузить сохранённые табулатуры.");
            } finally {
                setIsLoading(false);
            }
        };
        void loadTabs();
    }, []);

    const visibleTabs = useMemo(() => {
        const normalizedSearch = search.trim().toLowerCase();
        const filtered = tabs.filter((tab) =>
            tab.title.toLowerCase().includes(normalizedSearch)
        );

        const sorted = [...filtered].sort((a, b) => {
            if (sortBy === "date") {
                return new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime();
            }

            if (sortBy === "title") {
                return a.title.localeCompare(b.title, "ru");
            }

            return a.genreName.localeCompare(b.genreName, "ru");
        });

        return sorted;
    }, [tabs, search, sortBy]);

    return (
        <main className="main main--usual-page">
            <Link to="/" className="form-link">
                На главную
            </Link>
            <h1 className="main-title main-title--usual-page">
                Сохранённые табулатуры
            </h1>
            <textarea
                className="generate-textarea generate-textarea--search"
                placeholder="Поиск..."
                value={search}
                onChange={(event) => setSearch(event.target.value)}
            />
            <div className="tabs-sort">
                <span className="main-text">
                    <b>Сортировать: </b>
                </span>

                <select
                    className="tabs-sort-select"
                    value={sortBy}
                    onChange={(event) => setSortBy(event.target.value as SortOption)}
                >
                    <option value="date">по дате</option>
                    <option value="title">по названию</option>
                    <option value="genre">по жанру</option>
                </select>
            </div>
            {isLoading && <p className="main-text">Загрузка табулатур...</p>}
            {!isLoading && error && <p className="main-text">{error}</p>}
            {!isLoading && !error && (
                <TabsTable
                    tabs={visibleTabs}
                    onRowClick={(id) => navigate(`/tabs/${id}`)}
                    formatDate={formatDate}
                />
            )}
        </main>
    );
};