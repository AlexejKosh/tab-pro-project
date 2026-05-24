import { describe, expect, beforeEach, test, vi } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';

import TabInfo from '@/components/tabs/tab-info/tab-info';
import * as genreApi from '@/api/genreApi';
import * as audioUtils from '@/utils/audio';

vi.mock('@/api/genreApi');
vi.mock('@/utils/audio');

describe('TabInfo', () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    test('TabInfo - отображает информацию таба', async () => {
        vi.mocked(genreApi.getGenreById).mockResolvedValue({
            id: 1,
            name: 'rock'
        });

        vi.mocked(audioUtils.base64ToAudioUrl).mockReturnValue('blob:test');

        render(
            <TabInfo
                title="My Solo"
                genreId={1}
                tempo={120}
                signature="4/4"
                keyIndex={0}
                chords="C G Am F"
                tabData="5-7-8"
                audioData="base64data"
            />
        );

        expect(screen.getByText('My Solo')).toBeInTheDocument();
        expect(screen.getByDisplayValue('C G Am F')).toBeInTheDocument();
        expect(screen.getByDisplayValue('5-7-8')).toBeInTheDocument();

        expect(await screen.findByText(/рок/i)).toBeInTheDocument();
    });

    test('TabInfo - показывает неизвестный жанр при ошибке API', async () => {
        vi.mocked(genreApi.getGenreById).mockRejectedValue(new Error());

        render(
            <TabInfo
                title="My Solo"
                genreId={1}
                tempo={120}
                signature="4/4"
                keyIndex={0}
                chords="C"
                tabData="5"
                audioData=""
            />
        );

        expect(await screen.findByText(/неизвестно/i)).toBeInTheDocument();
    });

    test('TabInfo - отображает аудио плеер', async () => {
        vi.mocked(genreApi.getGenreById).mockResolvedValue({
            id: 1,
            name: 'rock'
        });

        vi.mocked(audioUtils.base64ToAudioUrl).mockReturnValue('blob:test');

        render(
            <TabInfo
                title="Solo"
                genreId={1}
                tempo={120}
                signature="4/4"
                keyIndex={0}
                chords="C"
                tabData="5"
                audioData="base64"
            />
        );

        await waitFor(() => {
            expect(document.querySelector('audio')).toBeInTheDocument();
        });
    });

    test('TabInfo - показывает сообщение о недоступности аудио', async () => {
        vi.mocked(genreApi.getGenreById).mockResolvedValue({
            id: 1,
            name: 'rock'
        });

        render(
            <TabInfo
                title="Solo"
                genreId={1}
                tempo={120}
                signature="4/4"
                keyIndex={0}
                chords="C"
                tabData="5"
                audioData=""
            />
        );

        expect(await screen.findByText(/аудио недоступно/i)).toBeInTheDocument();
    });
});