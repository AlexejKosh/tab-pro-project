import { describe, expect, beforeEach, test, vi } from 'vitest';
import { render, screen, waitFor } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";

import TabPage from "@/pages/tab-page/tab-page";

import * as tabApi from "@/api/tabApi";

vi.mock("@/api/tabApi");

const mockNavigate = vi.fn();

vi.mock("react-router-dom", async () => {

    const actual =
        await vi.importActual("react-router-dom");

    return {
        ...actual,
        useNavigate: () => mockNavigate,
        useParams: () => ({
            id:"1"
        })
    };

});

describe('TabPage', () => {

    beforeEach(() => {
        vi.clearAllMocks();
    });

    test('TabPage - показывает индикатор загрузки изначально', () => {

        vi.mocked(tabApi.getTabById)
            .mockImplementation(
                ()=>new Promise(()=>{})
            );

        render(
            <MemoryRouter>
                <TabPage/>
            </MemoryRouter>
        );

        expect(
            screen.getByText(/загрузка/i)
        ).toBeInTheDocument();

    });

    test('TabPage - отображает загруженную табулатуру', async () => {
        vi.mocked(tabApi.getTabById)
            .mockResolvedValue({
                id: 1,
                title: "Solo",
                genreId: 1,
                bpm: 120,
                signature: "4/4",
                musicKey: 0,
                chordProgression: "C-1, G-1",
                tabData: "5-7",
                audioData: "base64",
                createdAt: "2026-05-24T12:00:00"
            });

        render(
            <MemoryRouter>
                <TabPage/>
            </MemoryRouter>
        );

        await waitFor(()=>{
            expect(
                screen.getByText("Solo")
            ).toBeInTheDocument();
        });
    });

    test('TabPage - перенаправляет при ошибке API', async () => {
        vi.mocked(tabApi.getTabById)
            .mockRejectedValue(
                new Error()
            );
        render(
            <MemoryRouter>
                <TabPage/>
            </MemoryRouter>
        );

        await waitFor(()=>{
            expect(
                mockNavigate
            ).toHaveBeenCalledWith(
                "/tabs"
            );
        });
    });
});