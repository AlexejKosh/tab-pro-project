import { describe, expect, test, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';

import GeneratePage from '@/pages/generate-page/generate-page';
import { getGenres } from '@/api/genreApi';
import { saveTab } from '@/api/tabApi';

const mocks = vi.hoisted(() => ({
    navigate: vi.fn(),
    setMessage: vi.fn(),
    setResult: vi.fn(),
    reset: vi.fn(),
    startGeneration: vi.fn(),
    authState: {
        isAuthenticated: false,
    },
    generationState: {
        lastRequest: null as null | {
            genreId: number;
            signature: '3/4' | '4/4';
            musicKey: number;
            bpm: number;
            chordProgression: string;
        },
        result: null as null | {
            genreId: number;
            signature: '3/4' | '4/4';
            musicKey: number;
            bpm: number;
            chordProgression: string;
            tabData: string;
            audioData: string;
        },
        isGenerating: false,
        startGeneration: vi.fn(),
        setResult: vi.fn(),
        reset: vi.fn(),
    },
}));

vi.mock('@/api/genreApi', () => ({
    getGenres: vi.fn(),
}));

vi.mock('@/api/tabApi', () => ({
    saveTab: vi.fn(),
}));

vi.mock('@/hooks/use-document-title', () => ({
    useDocumentTitle: vi.fn(),
}));

vi.mock('@/components/tabs/tab-info/tab-info', () => ({
    default: ({ title }: { title: string }) => (
        <div data-testid="tab-info">{title}</div>
    ),
}));

vi.mock('@/store/generationStore', () => ({
    useGenerationStore: () => mocks.generationState,
}));

vi.mock('@/store/uiStore', () => ({
    useUiStore: () => ({
        setMessage: mocks.setMessage,
    }),
}));

vi.mock('@/store/authStore', () => ({
    useAuthStore: (selector: (state: typeof mocks.authState) => unknown) =>
        selector(mocks.authState),
}));

vi.mock('react-router-dom', async () => {
    const actual = await vi.importActual<typeof import('react-router-dom')>(
        'react-router-dom'
    );

    return {
        ...actual,
        useNavigate: () => mocks.navigate,
    };
});

describe('GeneratePage', () => {
    beforeEach(() => {
        vi.clearAllMocks();

        mocks.authState.isAuthenticated = false;
        mocks.generationState.lastRequest = null;
        mocks.generationState.result = null;
        mocks.generationState.isGenerating = false;

        mocks.generationState.startGeneration = vi.fn();
        mocks.generationState.setResult = vi.fn();
        mocks.generationState.reset = vi.fn();
    });

    test('GeneratePage - загружает жанры и отображает форму генерации', async () => {
        const user = userEvent.setup();

        vi.mocked(getGenres).mockResolvedValue([
            { id: 1, name: 'Rock' },
        ]);

        render(
            <MemoryRouter>
                <GeneratePage />
            </MemoryRouter>
        );

        await waitFor(() => {
            expect(getGenres).toHaveBeenCalled();
        });

        expect(
            screen.getByRole('button', {
                name: 'Сгенерировать'
            })
        ).toBeEnabled();
        expect(screen.getByLabelText('Рок')).toBeInTheDocument();
    });

    test('GeneratePage - показывает ошибку при неудачной загрузке жанров', async () => {
        vi.mocked(getGenres).mockRejectedValue(new Error('fail'));

        render(
            <MemoryRouter>
                <GeneratePage />
            </MemoryRouter>
        );

        await waitFor(() => {
            expect(mocks.setMessage).toHaveBeenCalledWith({
                message: 'Не удалось загрузить список жанров.',
                type: 'error',
            });
        });
    });

    test('GeneratePage - показывает ошибку валидации при пустой последовательности аккордов', async () => {
        const user = userEvent.setup();

        vi.mocked(getGenres).mockResolvedValue([
            { id: 1, name: 'Rock' },
        ]);

        render(
            <MemoryRouter>
                <GeneratePage />
            </MemoryRouter>
        );

        await waitFor(() => {
            expect(
                screen.getByRole('button', {
                    name: 'Сгенерировать'
                })
            ).toBeEnabled();
        });

        await user.click(screen.getByText('Рок'));
        await user.click(
            screen.getByRole('button', {
                name: 'Сгенерировать'
            })
        );

        expect(mocks.setMessage).toHaveBeenCalledWith({
            message: 'Введите последовательность аккордов.',
            type: 'error',
        });
        expect(mocks.generationState.startGeneration).not.toHaveBeenCalled();
    });

    test('GeneratePage - показывает ошибку валидации при некорректном формате аккордов', async () => {
        const user = userEvent.setup();

        vi.mocked(getGenres).mockResolvedValue([
            { id: 1, name: 'Rock' },
        ]);

        render(
            <MemoryRouter>
                <GeneratePage />
            </MemoryRouter>
        );

        await waitFor(() => {
            expect(
                screen.getByRole('button', {
                    name: 'Сгенерировать'
                })
            ).toBeEnabled();
        });

        await user.type(
            screen.getByPlaceholderText('Введите ритм-партию...'),
            'abc'
        );

        await user.click(
            screen.getByRole('button', {
                name: 'Сгенерировать'
            })
        );

        expect(mocks.setMessage).toHaveBeenCalledWith({
            message: 'Некорректный формат аккордов. Пример: C-1/2,G7-1/4,Am-1',
            type: 'error',
        });
        expect(mocks.generationState.startGeneration).not.toHaveBeenCalled();
    });

    test('GeneratePage - запускает генерацию при корректной отправке', async () => {
        const user = userEvent.setup();

        vi.mocked(getGenres).mockResolvedValue([
            { id: 1, name: 'Rock' },
        ]);

        mocks.generationState.startGeneration = vi.fn().mockResolvedValue(undefined);

        render(
            <MemoryRouter>
                <GeneratePage />
            </MemoryRouter>
        );

        await waitFor(() => {
            expect(
                screen.getByRole('button', {
                    name: 'Сгенерировать'
                })
            ).toBeEnabled();
        });

        await user.type(
            screen.getByPlaceholderText('Введите ритм-партию...'),
            'C-1/2,G7-1/4,Am-1'
        );

        await user.click(
            screen.getByRole('button', {
                name: 'Сгенерировать'
            })
        );

        await waitFor(() => {
            expect(mocks.generationState.startGeneration).toHaveBeenCalledWith({
                genreId: 1,
                signature: '4/4',
                musicKey: 0,
                bpm: 120,
                chordProgression: 'C-1/2,G7-1/4,Am-1',
            });
        });

        expect(mocks.setMessage).toHaveBeenCalledWith({
            message: 'Соло успешно сгенерировано.',
            type: 'success',
        });
    });

    test('GeneratePage - отображает результат и позволяет сохранить табулатуру', async () => {
        const user = userEvent.setup();

        mocks.authState.isAuthenticated = true;
        mocks.generationState.result = {
            genreId: 1,
            signature: '4/4',
            musicKey: 0,
            bpm: 120,
            chordProgression: 'C-1/2,G7-1/4,Am-1',
            tabData: '5-7-8',
            audioData: 'base64',
        };

        vi.mocked(saveTab).mockResolvedValue(undefined);

        render(
            <MemoryRouter>
                <GeneratePage />
            </MemoryRouter>
        );

        expect(screen.getByTestId('tab-info')).toHaveTextContent('Сгенерированное соло');
        expect(
            screen.getByRole('button', {
                name: 'Попробовать снова'
            })
        ).toBeInTheDocument();
        expect(screen.getByPlaceholderText('Введите название...')).toBeInTheDocument();
        expect(
            screen.getByRole('button', {
                name: 'Сохранить'
            })
        ).toBeInTheDocument();

        await user.click(
            screen.getByRole('button', {
                name: 'Попробовать снова'
            })
        );

        await waitFor(() => {
            expect(mocks.generationState.setResult).toHaveBeenCalledWith(null);
        });

        await user.click(
            screen.getByRole('button', {
                name: 'Сохранить'
            })
        );

        await waitFor(() => {
            expect(mocks.setMessage).toHaveBeenCalledWith({
                message: 'Введите название табулатуры.',
                type: 'error',
            });
        });
    });

    test('GeneratePage - успешно сохраняет сгенерированную табулатуру', async () => {
        const user = userEvent.setup();

        mocks.authState.isAuthenticated = true;
        mocks.generationState.result = {
            genreId: 1,
            signature: '4/4',
            musicKey: 0,
            bpm: 120,
            chordProgression: 'C-1/2,G7-1/4,Am-1',
            tabData: '5-7-8',
            audioData: 'base64',
        };

        vi.mocked(saveTab).mockResolvedValue(undefined);

        render(
            <MemoryRouter>
                <GeneratePage />
            </MemoryRouter>
        );

        await user.type(
            screen.getByPlaceholderText('Введите название...'),
            'Мое соло'
        );

        await user.click(
            screen.getByRole('button', {
                name: 'Сохранить'
            })
        );

        await waitFor(() => {
            expect(saveTab).toHaveBeenCalledWith({
                title: 'Мое соло',
                genreId: 1,
                signature: '4/4',
                musicKey: 0,
                bpm: 120,
                chordProgression: 'C-1/2,G7-1/4,Am-1',
                tabData: '5-7-8',
                audioData: 'base64',
            });
        });

        expect(mocks.setMessage).toHaveBeenCalledWith({
            message: 'Табулатура успешно сохранена.',
            type: 'success',
        });
        expect(mocks.generationState.reset).toHaveBeenCalled();
        expect(mocks.navigate).toHaveBeenCalledWith('/tabs', { replace: true });
    });
});