import { describe, expect, beforeEach, test, vi } from 'vitest';
import { render } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';

import MainLayout from '@/components/layout/MainLayout';

import { useAuthStore } from '@/store/authStore';

vi.mock('@/hooks/use-theme', () => ({
    useTheme: vi.fn()
}));

vi.mock('react-router-dom', async () => {
    const actual = await vi.importActual('react-router-dom');

    return {
        ...actual,
        Outlet: () => <div>Outlet Content</div>
    };
});

describe('MainLayout', () => {
    beforeEach(() => {
        useAuthStore.setState({
            isAuthenticated: false,
            user: null,
            loadCurrentUser: vi.fn()
        });
    });

    test('MainLayout - отображает компоненты макета', () => {
        const { getByText } = render(
            <MemoryRouter>
                <MainLayout />
            </MemoryRouter>
        );

        expect(getByText('Outlet Content')).toBeInTheDocument();
    });

    test('MainLayout - загружает текущего пользователя', () => {
        const load = vi.fn();

        useAuthStore.setState({
            isAuthenticated: true,
            user: null,
            loadCurrentUser: load
        });

        render(
            <MemoryRouter>
                <MainLayout />
            </MemoryRouter>
        );

        expect(load).toHaveBeenCalled();
    });
});