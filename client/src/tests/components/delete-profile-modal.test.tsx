import { describe, expect, beforeEach, test, vi } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';

import DeleteProfileModal from '@/components/profile/delete-profile-modal/delete-profile-modal';

import { useAuthStore } from '@/store/authStore';
import { useUiStore } from '@/store/uiStore';

const mockNavigate = vi.fn();

vi.mock('react-router-dom', async () => {
    const actual = await vi.importActual('react-router-dom');
    return {
        ...actual,
        useNavigate: () => mockNavigate
    };
});

vi.mock('@/api/userApi', () => ({ deleteCurrentUser: vi.fn() }));

describe('DeleteProfileModal', () => {
    let deleteCurrentUser: any;

    beforeEach(async () => {
        useAuthStore.setState({ logout: vi.fn() });
        useUiStore.setState({ setMessage: vi.fn() });

        const mod = await vi.importMock('@/api/userApi');
        deleteCurrentUser = mod.deleteCurrentUser;

        deleteCurrentUser.mockClear();
        mockNavigate.mockClear();
    });

    test('DeleteProfileModal - клик по оверлею вызывает onClose', () => {
        const onClose = vi.fn();

        render(<DeleteProfileModal isOpen={true} onClose={onClose} />);

        const overlay = document.querySelector('.modal-overlay') as HTMLElement;
        if (!overlay) throw new Error('overlay not found');

        fireEvent.click(overlay);

        expect(onClose).toHaveBeenCalled();
    });

    test('DeleteProfileModal - успешное удаление вызывает API, logout и навигацию', async () => {
        deleteCurrentUser.mockResolvedValue({});

        const logout = vi.fn();
        useAuthStore.setState({ logout });

        render(<DeleteProfileModal isOpen={true} onClose={vi.fn()} />);

        const btn = screen.getByRole('button', {
            name: /Удалить профиль/i
        });

        fireEvent.click(btn);

        await waitFor(() => {
            expect(deleteCurrentUser).toHaveBeenCalled();
            expect(logout).toHaveBeenCalled();
            expect(mockNavigate).toHaveBeenCalled();
        });
    });

    test('DeleteProfileModal - неудачное удаление показывает сообщение об ошибке', async () => {
        deleteCurrentUser.mockRejectedValue({
            response: {
                data: {
                    message: 'Fail'
                }
            }
        });

        const setMessage = vi.fn();
        useUiStore.setState({ setMessage });

        render(<DeleteProfileModal isOpen={true} onClose={vi.fn()} />);

        const btn = screen.getByRole('button', {
            name: /Удалить профиль/i
        });

        fireEvent.click(btn);

        await waitFor(() => {
            expect(setMessage).toHaveBeenCalledWith({
                message: 'Fail',
                type: 'error'
            });
        });
    });
});