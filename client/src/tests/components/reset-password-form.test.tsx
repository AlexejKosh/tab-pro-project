import { describe, expect, test, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';

import ResetPasswordForm from '@/components/auth/reset-password-form/reset-password-form';

const mockNavigate = vi.fn();
const mockRecoverPassword = vi.fn();
const mockSetMessage = vi.fn();

vi.mock('react-router-dom', async () => {
    const actual = await vi.importActual<typeof import('react-router-dom')>(
        'react-router-dom'
    );

    return {
        ...actual,
        useNavigate: () => mockNavigate
    };
});

vi.mock('@/api/authApi', () => ({
    recoverPassword: (...args: any[]) => mockRecoverPassword(...args)
}));

vi.mock('@/store/uiStore', () => ({
    useUiStore: (selector: any) =>
        selector({ setMessage: mockSetMessage })
}));

describe('ResetPasswordForm', () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    const renderComponent = () => {
        render(<ResetPasswordForm token="test-token" />);
    };

    test('ResetPasswordForm - показывает ошибку, если поля пустые', async () => {
        renderComponent();

        fireEvent.submit(
            screen.getByRole('button', {
                name: /сбросить пароль/i
            }).closest('form')!
        );

        await waitFor(() => {
            expect(mockSetMessage).toHaveBeenCalledWith({
                message: 'Пожалуйста, заполните все поля.',
                type: 'error'
            });
            expect(mockRecoverPassword).not.toHaveBeenCalled();
        });
    });

    test('ResetPasswordForm - показывает ошибку, если пароли не совпадают', async () => {
        renderComponent();

        fireEvent.change(screen.getByPlaceholderText('Пароль'), {
            target: { value: '123' }
        });

        fireEvent.change(screen.getByPlaceholderText('Повторите пароль'), {
            target: { value: '456' }
        });

        fireEvent.submit(
            screen.getByRole('button', {
                name: /сбросить пароль/i
            }).closest('form')!
        );

        await waitFor(() => {
            expect(mockSetMessage).toHaveBeenCalledWith({
                message: 'Пароли не совпадают.',
                type: 'error'
            });
            expect(mockRecoverPassword).not.toHaveBeenCalled();
        });
    });

    test('ResetPasswordForm - успешно сбрасывает пароль', async () => {
        mockRecoverPassword.mockResolvedValue({});

        renderComponent();

        fireEvent.change(screen.getByPlaceholderText('Пароль'), {
            target: { value: '123' }
        });

        fireEvent.change(screen.getByPlaceholderText('Повторите пароль'), {
            target: { value: '123' }
        });

        fireEvent.submit(
            screen.getByRole('button', {
                name: /сбросить пароль/i
            }).closest('form')!
        );

        await waitFor(() => {
            expect(mockRecoverPassword).toHaveBeenCalledWith('test-token', {
                password1: '123',
                password2: '123'
            });
            expect(mockSetMessage).toHaveBeenCalledWith({
                message: 'Пароль успешно сброшен.',
                type: 'success'
            });
            expect(mockNavigate).toHaveBeenCalled();
        });
    });

    test('ResetPasswordForm - показывает ошибку API при неудаче', async () => {
        mockRecoverPassword.mockRejectedValue({
            response: { data: { message: 'Fail' } }
        });

        renderComponent();

        fireEvent.change(screen.getByPlaceholderText('Пароль'), {
            target: { value: '123' }
        });

        fireEvent.change(screen.getByPlaceholderText('Повторите пароль'), {
            target: { value: '123' }
        });

        fireEvent.submit(
            screen.getByRole('button', {
                name: /сбросить пароль/i
            }).closest('form')!
        );

        await waitFor(() => {
            expect(mockSetMessage).toHaveBeenCalledWith({
                message: 'Fail',
                type: 'error'
            });
        });
    });
});