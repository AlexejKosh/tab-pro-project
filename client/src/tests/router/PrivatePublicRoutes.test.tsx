import { describe, test, expect, beforeEach } from 'vitest';
import { MemoryRouter, Routes, Route } from 'react-router-dom';
import { render } from '@testing-library/react';

import { PrivateRoute } from '@/router/PrivateRoute';
import { PublicRoute } from '@/router/PublicRoute';
import { useAuthStore } from '@/store/authStore';

describe('Routing guards', () => {
    beforeEach(() => {
        useAuthStore.setState({
            token: null,
            user: null,
            isAuthenticated: false,
        });
    });

    test('PrivateRoute - перенаправляет неавторизованного пользователя', () => {
        const { container } = render(
            <MemoryRouter initialEntries={["/private"]}>
                <Routes>
                    <Route path="/private" element={<PrivateRoute><div>secret</div></PrivateRoute>} />
                    <Route path="/login" element={<div>login</div>} />
                </Routes>
            </MemoryRouter>
        );

        expect(container.textContent).toContain('login');
    });

    test('PrivateRoute - показывает содержимое для авторизованного', () => {
        useAuthStore.setState({ isAuthenticated: true });

        const { container } = render(
            <MemoryRouter initialEntries={["/private"]}>
                <Routes>
                    <Route path="/private" element={<PrivateRoute><div>secret</div></PrivateRoute>} />
                    <Route path="/login" element={<div>login</div>} />
                </Routes>
            </MemoryRouter>
        );

        expect(container.textContent).toContain('secret');
    });

    test('PublicRoute - перенаправляет авторизованного пользователя на HOME', () => {
        useAuthStore.setState({ isAuthenticated: true });

        const { container } = render(
            <MemoryRouter initialEntries={["/public"]}>
                <Routes>
                    <Route path="/public" element={<PublicRoute><div>public</div></PublicRoute>} />
                    <Route path="/" element={<div>home</div>} />
                </Routes>
            </MemoryRouter>
        );

        expect(container.textContent).toContain('home');
    });

    test('PublicRoute - показывает содержимое для неавторизованного', () => {
        useAuthStore.setState({ isAuthenticated: false });

        const { container } = render(
            <MemoryRouter initialEntries={["/public"]}>
                <Routes>
                    <Route path="/public" element={<PublicRoute><div>public</div></PublicRoute>} />
                    <Route path="/" element={<div>home</div>} />
                </Routes>
            </MemoryRouter>
        );

        expect(container.textContent).toContain('public');
    });
});
