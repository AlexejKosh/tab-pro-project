import { describe, expect, beforeEach, test, vi } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';

import ForgotPasswordForm from '@/components/auth/forgot-password-form/forgot-password-form';

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
    sendRecoverPasswordMail: vi.fn()
}));

describe('ForgotPasswordForm', () => {
    let sendRecoverPasswordMail: any;

    beforeEach(async () => {
        mockNavigate.mockClear();

        useUiStore.setState({ setMessage: vi.fn() });

        const mod = await vi.importMock<
            typeof import('@/api/authApi')
        >('@/api/authApi');

        sendRecoverPasswordMail = mod.sendRecoverPasswordMail;

        vi.clearAllMocks();
    });

    const renderComponent = () => {
        render(
            <MemoryRouter>
                <ForgotPasswordForm />
            </MemoryRouter>
        );
    };

    test('ForgotPasswordForm - не отправляет форму, если email пустой', async () => {
        const setMessage = vi.fn();

        useUiStore.setState({ setMessage });

        renderComponent();

        fireEvent.click(
            screen.getByRole('button', {
                name: /Восстановить пароль/i
            })
        );

        await waitFor(() => {
            expect(sendRecoverPasswordMail).not.toHaveBeenCalled();
            expect(setMessage).not.toHaveBeenCalled();
            expect(mockNavigate).not.toHaveBeenCalled();
        });
    });

    test('ForgotPasswordForm - успешно отправляет письмо и перенаправляет', async () => {
        const setMessage = vi.fn();

        useUiStore.setState({ setMessage });

        sendRecoverPasswordMail.mockResolvedValue({
            message: 'ok'
        });

        renderComponent();

        fireEvent.change(
            screen.getByPlaceholderText(/^E-mail$/i),
            {
                target: {
                    value: 'a@b.com'
                }
            }
        );

        fireEvent.click(
            screen.getByRole('button', {
                name: /Восстановить пароль/i
            })
        );

        await waitFor(() => {
            expect(sendRecoverPasswordMail).toHaveBeenCalledWith({
                email: 'a@b.com'
            });

            expect(setMessage).toHaveBeenCalledWith({
                message: 'ok',
                type: 'success'
            });

            expect(mockNavigate).toHaveBeenCalled();
        });
    });

    test('ForgotPasswordForm - показывает ошибку при неудачной отправке', async () => {
        const setMessage = vi.fn();

        useUiStore.setState({ setMessage });

        sendRecoverPasswordMail.mockRejectedValue({
            response: {
                data: {
                    message: 'Fail'
                }
            }
        });

        renderComponent();

        fireEvent.change(
            screen.getByPlaceholderText(/^E-mail$/i),
            {
                target: {
                    value: 'a@b.com'
                }
            }
        );

        fireEvent.click(
            screen.getByRole('button', {
                name: /Восстановить пароль/i
            })
        );

        await waitFor(() => {
            expect(setMessage).toHaveBeenCalledWith({
                message: 'Fail',
                type: 'error'
            });

            expect(mockNavigate).not.toHaveBeenCalled();
        });
    });
});