import { describe, expect, beforeEach, test, vi } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';

import ChangePasswordForm from '@/components/profile/change-password-form/change-password-form';

import { useUiStore } from '@/store/uiStore';

vi.mock('@/api/userApi', () => ({
    changePassword: vi.fn()
}));

describe('ChangePasswordForm', () => {
    let changePassword: any;

    beforeEach(async () => {
        useUiStore.setState({ setMessage: vi.fn() });

        const mod = await vi.importMock<
            typeof import('@/api/userApi')
        >('@/api/userApi');

        changePassword = mod.changePassword;

        vi.clearAllMocks();
    });

    test('ChangePasswordForm - не отправляет форму, если поля пустые', async () => {
        const setMessage = vi.fn();

        useUiStore.setState({ setMessage });

        render(<ChangePasswordForm />);

        fireEvent.click(
            screen.getByRole('button', {
                name: /Изменить пароль/i
            })
        );

        await waitFor(() => {
            expect(changePassword).not.toHaveBeenCalled();
            expect(setMessage).not.toHaveBeenCalled();
        });
    });

    test('ChangePasswordForm - показывает ошибку, если пароли не совпадают', async () => {
        const setMessage = vi.fn();

        useUiStore.setState({ setMessage });

        render(<ChangePasswordForm />);

        fireEvent.change(
            screen.getByPlaceholderText(/Текущий пароль/i),
            {
                target: {
                    value: 'old'
                }
            }
        );

        fireEvent.change(
            screen.getByPlaceholderText(/Новый пароль/i),
            {
                target: {
                    value: 'new1'
                }
            }
        );

        fireEvent.change(
            screen.getByPlaceholderText(/Повторите пароль/i),
            {
                target: {
                    value: 'new2'
                }
            }
        );

        fireEvent.click(
            screen.getByRole('button', {
                name: /Изменить пароль/i
            })
        );

        await waitFor(() => {
            expect(setMessage).toHaveBeenCalledWith({
                message: 'Пароли не совпадают.',
                type: 'error'
            });

            expect(changePassword).not.toHaveBeenCalled();
        });
    });

    test('ChangePasswordForm - успешно меняет пароль и показывает уведомление', async () => {
        const setMessage = vi.fn();

        useUiStore.setState({ setMessage });

        changePassword.mockResolvedValue({});

        render(<ChangePasswordForm />);

        fireEvent.change(
            screen.getByPlaceholderText(/Текущий пароль/i),
            {
                target: {
                    value: 'old'
                }
            }
        );

        fireEvent.change(
            screen.getByPlaceholderText(/Новый пароль/i),
            {
                target: {
                    value: 'new'
                }
            }
        );

        fireEvent.change(
            screen.getByPlaceholderText(/Повторите пароль/i),
            {
                target: {
                    value: 'new'
                }
            }
        );

        fireEvent.click(
            screen.getByRole('button', {
                name: /Изменить пароль/i
            })
        );

        await waitFor(() => {
            expect(changePassword).toHaveBeenCalledWith({
                oldPassword: 'old',
                newPassword: 'new'
            });

            expect(setMessage).toHaveBeenCalledWith({
                message: 'Пароль успешно изменён.',
                type: 'success'
            });
        });
    });

    test('ChangePasswordForm - показывает сообщение об ошибке от API', async () => {
        const setMessage = vi.fn();

        useUiStore.setState({ setMessage });

        changePassword.mockRejectedValue({
            response: {
                data: {
                    message: 'Wrong password'
                }
            }
        });

        render(<ChangePasswordForm />);

        fireEvent.change(
            screen.getByPlaceholderText(/Текущий пароль/i),
            {
                target: {
                    value: 'old'
                }
            }
        );

        fireEvent.change(
            screen.getByPlaceholderText(/Новый пароль/i),
            {
                target: {
                    value: 'new'
                }
            }
        );

        fireEvent.change(
            screen.getByPlaceholderText(/Повторите пароль/i),
            {
                target: {
                    value: 'new'
                }
            }
        );

        fireEvent.click(
            screen.getByRole('button', {
                name: /Изменить пароль/i
            })
        );

        await waitFor(() => {
            expect(setMessage).toHaveBeenCalledWith({
                message: 'Wrong password',
                type: 'error'
            });
        });
    });
});