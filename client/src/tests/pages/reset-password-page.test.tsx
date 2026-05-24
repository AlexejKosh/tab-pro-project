import { describe, expect, test, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';

import ResetPasswordPage from '@/pages/reset-password-page/reset-password-page';
import { checkRecoverPasswordToken } from '@/api/authApi';

import { useUiStore } from '@/store/uiStore';

const mockNavigate = vi.fn();

vi.mock('react-router-dom', async () => {
    const actual = await vi.importActual<
        typeof import('react-router-dom')
    >('react-router-dom');

    return {
        ...actual,
        useNavigate: () => mockNavigate,
        useParams: () => ({ token: 'valid-token' })
    };
});

vi.mock('@/api/authApi', () => ({
    checkRecoverPasswordToken: vi.fn()
}));

describe('ResetPasswordPage', () => {
    beforeEach(() => {
        mockNavigate.mockClear();
        vi.clearAllMocks();

        useUiStore.setState({ setMessage: vi.fn() });
    });

    test('ResetPasswordPage - отображает страницу и показывает форму при валидном токене', async () => {
        (checkRecoverPasswordToken as any).mockResolvedValue({
            valid: true
        });

        render(
            <MemoryRouter>
                <ResetPasswordPage />
            </MemoryRouter>
        );

        await waitFor(() => {
            expect(screen.getByRole('heading', {
                name: 'Сброс пароля'
            })).toBeInTheDocument();
        });
    });

    test('ResetPasswordPage - перенаправляет при невалидном токене', async () => {
        const setMessage = vi.fn();

        useUiStore.setState({ setMessage });

        (checkRecoverPasswordToken as any).mockResolvedValue({
            valid: false
        });

        render(
            <MemoryRouter>
                <ResetPasswordPage />
            </MemoryRouter>
        );

        await waitFor(() => {
            expect(setMessage).toHaveBeenCalledWith({
                message: 'Ссылка восстановления пароля недействительна.',
                type: 'error'
            });

            expect(mockNavigate).toHaveBeenCalled();
        });
    });
});