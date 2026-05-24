import { describe, expect, beforeEach, test, vi } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';

import LoginForm from '@/components/auth/login-form/login-form';

import { useAuthStore } from '@/store/authStore';
import { useUiStore } from '@/store/uiStore';

const mockNavigate = vi.fn();

vi.mock('react-router-dom', async () => {
    const actual = await vi.importActual<typeof import('react-router-dom')>('react-router-dom');

    return {
        ...actual,
        useNavigate: () => mockNavigate
    };
});

vi.mock('@/api/authApi', () => ({
login: vi.fn()
}));

describe('LoginForm', () => {
    let loginApi: any;

    beforeEach(async () => {
        mockNavigate.mockClear();

        useAuthStore.setState({
            login: vi.fn(),
            loadCurrentUser: vi.fn()
        });

        useUiStore.setState({ setMessage: vi.fn() });

        const mod = await vi.importMock<typeof import('@/api/authApi')>('@/api/authApi');

        loginApi = mod.login;
        vi.clearAllMocks();
    });

    const renderComponent = () => {
        return render(
            <MemoryRouter>
                <LoginForm />
            </MemoryRouter>
        );
    };

    test('LoginForm - успешный вход обновляет store и выполняет навигацию', async () => {
        loginApi.mockResolvedValue({ token: 'token-123' });

        const authLogin = vi.fn();
        const loadCurrentUser = vi.fn();
        const setMessage = vi.fn();

        useAuthStore.setState({
            login: authLogin,
            loadCurrentUser
        });

        useUiStore.setState({ setMessage });

        renderComponent();

        const usernameInput = screen.getByPlaceholderText(/Имя пользователя\/e-mail/i);
        const passwordInput = screen.getByPlaceholderText(/Пароль/i);
        const submitButton = screen.getByRole('button', { name: /Войти/i });

        fireEvent.change(usernameInput, {
            target: { value: 'user' }
        });

        fireEvent.change(passwordInput, {
            target: { value: 'pass' }
        });

        fireEvent.click(submitButton);

        await waitFor(() => {
            expect(loginApi).toHaveBeenCalledWith({
                usernameOrEmail: 'user',
                password: 'pass'
            });
            expect(authLogin).toHaveBeenCalledWith('token-123');
            expect(loadCurrentUser).toHaveBeenCalled();
            expect(setMessage).toHaveBeenCalledWith({
                message: 'Успешный вход',
                type: 'success'
            });
            expect(mockNavigate).toHaveBeenCalled();
        });
    });

    test('LoginForm - неудачный вход показывает сообщение об ошибке', async () => {
        loginApi.mockRejectedValue({
            response: {
                data: {
                    message: 'Bad creds'
                }
            }
        });

        const setMessage = vi.fn();

        useUiStore.setState({ setMessage });

        renderComponent();

        const usernameInput = screen.getByPlaceholderText(/Имя пользователя\/e-mail/i);
        const passwordInput = screen.getByPlaceholderText(/Пароль/i);
        const submitButton = screen.getByRole('button', { name: /Войти/i });

        fireEvent.change(usernameInput, {
            target: { value: 'user' }
        });

        fireEvent.change(passwordInput, {
            target: { value: 'wrong' }
        });

        fireEvent.click(submitButton);

        await waitFor(() => {
            expect(loginApi).toHaveBeenCalledWith({
                usernameOrEmail: 'user',
                password: 'wrong'
            });
            expect(setMessage).toHaveBeenCalledWith({
                message: 'Bad creds',
                type: 'error'
            });
            expect(mockNavigate).not.toHaveBeenCalled();
        });
    });
});