import { describe, expect, beforeEach, test, vi } from 'vitest';
import { render, screen, waitFor, fireEvent, } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";

import SavedTabsPage from "@/pages/saved-tabs-page/saved-tabs-page";
import * as tabApi from "@/api/tabApi";
import * as genreApi from "@/api/genreApi";

vi.mock("@/api/tabApi");
vi.mock("@/api/genreApi");

const mockNavigate = vi.fn();

vi.mock(
    "react-router-dom",
    async () => {
        const actual =
            await vi.importActual(
                "react-router-dom"
            );

        return {
            ...actual,
            useNavigate: () => mockNavigate,
        };
    }
);

describe('SavedTabsPage', () => {
        beforeEach(() => {
            vi.clearAllMocks();
        });

        test('SavedTabsPage - отображает список табулатур', async () => {
                vi.mocked(tabApi.getTabs)
                    .mockResolvedValue([
                        {
                            id: 1,
                            title: "Solo",
                            genreId: 1,
                            signature: "4/4",
                            createdAt: "2026-05-24",
                            chordProgression: "C G",
                        },
                    ]);

                vi.mocked(
                    genreApi.getGenreById
                )
                    .mockResolvedValue({
                        id: 1,
                        name: "Rock",
                    });

                render(
                    <MemoryRouter>
                        <SavedTabsPage />
                    </MemoryRouter>
                );

                await waitFor(() => {
                    expect(screen.getByText("Solo"))
                        .toBeInTheDocument();
                });
            }
        );

        test('SavedTabsPage - показывает индикатор загрузки', () => {
                vi.mocked(
                    tabApi.getTabs
                )
                    .mockImplementation(
                        () => new Promise(() => {})
                    );

                render(
                    <MemoryRouter>
                        <SavedTabsPage />
                    </MemoryRouter>
                );

                expect(
                    screen.getByText(/загрузка/i)
                )
                    .toBeInTheDocument();
            }
        );

        test('SavedTabsPage - показывает ошибку при загрузке', async () => {
                vi.mocked(
                    tabApi.getTabs
                )
                    .mockRejectedValue(
                        new Error()
                    );

                render(
                    <MemoryRouter>
                        <SavedTabsPage />
                    </MemoryRouter>
                );

                await waitFor(() => {
                    expect(
                        screen.getByText(
                            /не удалось загрузить/i
                        )
                    )
                        .toBeInTheDocument();
                });
            }
        );

        test('SavedTabsPage - фильтрует табулатуры', async () => {
                vi.mocked(
                    tabApi.getTabs
                )
                    .mockResolvedValue([
                        {
                            id: 1,
                            title: "Rock Solo",
                            genreId: 1,
                            signature: "4/4",
                            createdAt: "2026-05-24",
                            chordProgression: "C G",
                        },
                    ]);

                vi.mocked(
                    genreApi.getGenreById
                )
                    .mockResolvedValue({
                        id: 1,
                        name: "Rock",
                    });

                render(
                    <MemoryRouter>
                        <SavedTabsPage />
                    </MemoryRouter>
                );

                await waitFor(() => {
                    expect(
                        screen.getByText("Rock Solo")
                    )
                        .toBeInTheDocument();
                });

                fireEvent.change(
                    screen.getByPlaceholderText("Поиск..."),
                    {
                        target: { value: "Jazz" },
                    }
                );

                expect(screen.queryByText("Rock Solo"))
                    .not.toBeInTheDocument();
            }
        );
    }
);