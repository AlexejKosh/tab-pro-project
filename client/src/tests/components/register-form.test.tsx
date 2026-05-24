import { describe, expect, beforeEach, test, vi } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';

import RegisterForm from '@/components/auth/register-form/register-form';

import { useAuthStore } from '@/store/authStore';
import { useUiStore } from '@/store/uiStore';

const mockNavigate = vi.fn();

vi.mock('react-router-dom', async () => {
    const actual = await vi.importActual<
        typeof import('react-router-dom')
    >('react-router-dom');

    return {
        ...actual,
        useNavigate: () => mockNavigate
    };
});

vi.mock('@/api/authApi', () => ({
    register: vi.fn()
}));

describe('RegisterForm', () => {
    let registerApi: any;

    beforeEach(async () => {
        mockNavigate.mockClear();

        useAuthStore.setState({
            login: vi.fn(),
            loadCurrentUser: vi.fn()
        });

        useUiStore.setState({ setMessage: vi.fn() });

        const mod = await vi.importMock<
            typeof import('@/api/authApi')
        >('@/api/authApi');

        registerApi = mod.register;

        vi.clearAllMocks();
    });

    const renderComponent = () => {
        render(
            <MemoryRouter>
                <RegisterForm />
            </MemoryRouter>
        );
    };

    test('RegisterForm - не отправляет форму, если поля пустые', async () => {
        const setMessage = vi.fn();

        useUiStore.setState({ setMessage });

        renderComponent();

        fireEvent.click(
            screen.getByRole('button', {
                name: /Зарегистрироваться/i
            })
        );

        await waitFor(() => {
            expect(registerApi).not.toHaveBeenCalled();
            expect(setMessage).not.toHaveBeenCalled();
        });
    });

    test('RegisterForm - показывает ошибку, если пароли не совпадают', async () => {
        const setMessage = vi.fn();

        useUiStore.setState({ setMessage });

        renderComponent();

        fireEvent.change(
            screen.getByPlaceholderText(/^Имя пользователя$/i),
            {
                target: {
                    value: 'u'
                }
            }
        );

        fireEvent.change(
            screen.getByPlaceholderText(/^E-mail$/i),
            {
                target: {
                    value: 'a@b.com'
                }
            }
        );

        fireEvent.change(
            screen.getByPlaceholderText(/^Пароль$/i),
            {
                target: {
                    value: 'p1'
                }
            }
        );

        fireEvent.change(
            screen.getByPlaceholderText(/^Повторите пароль$/i),
            {
                target: {
                    value: 'p2'
                }
            }
        );

        fireEvent.click(
            screen.getByRole('button', {
                name: /Зарегистрироваться/i
            })
        );

        await waitFor(() => {
            expect(setMessage).toHaveBeenCalledWith({
                message: 'Пароли не совпадают.',
                type: 'error'
            });
            expect(registerApi).not.toHaveBeenCalled();
        });
    });

    test('RegisterForm - успешно регистрирует, логинит и перенаправляет', async () => {
        const authLogin = vi.fn();
        const loadCurrentUser = vi.fn();
        const setMessage = vi.fn();

        useAuthStore.setState({
            login: authLogin,
            loadCurrentUser
        });

        useUiStore.setState({ setMessage });

        registerApi.mockResolvedValue({ token: 'tok-1' });

        renderComponent();

        fireEvent.change(
            screen.getByPlaceholderText(/^Имя пользователя$/i),
            {
                target: {
                    value: 'u'
                }
            }
        );

        fireEvent.change(
            screen.getByPlaceholderText(/^E-mail$/i),
            {
                target: {
                    value: 'a@b.com'
                }
            }
        );

        fireEvent.change(
            screen.getByPlaceholderText(/^Пароль$/i),
            {
                target: {
                    value: 'p'
                }
            }
        );

        fireEvent.change(
            screen.getByPlaceholderText(/^Повторите пароль$/i),
            {
                target: {
                    value: 'p'
                }
            }
        );

        fireEvent.click(
            screen.getByRole('button', {
                name: /Зарегистрироваться/i
            })
        );

        await waitFor(() => {
            expect(registerApi).toHaveBeenCalledWith({
                username: 'u',
                email: 'a@b.com',
                password: 'p'
            });
            expect(authLogin).toHaveBeenCalledWith('tok-1');
            expect(loadCurrentUser).toHaveBeenCalled();
            expect(setMessage).toHaveBeenCalledWith({
                message: 'Регистрация прошла успешно.',
                type: 'success'
            });
            expect(mockNavigate).toHaveBeenCalled();
        });
    });
});