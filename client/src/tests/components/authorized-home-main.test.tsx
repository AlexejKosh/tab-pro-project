import { describe, expect, beforeEach, test, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';

import AuthorizedHomeMain from '@/components/home/authorized-home-main/authorized-home-main';

vi.mock('@/hooks/use-banner-interactions', () => ({ useBannerInteractions: () => {} }));

const switchTheme = vi.fn();

vi.mock('@/hooks/use-theme', () => ({
    useTheme: () => ({
        switchTheme
    })
}));

describe('AuthorizedHomeMain', () => {
    beforeEach(() => {
        switchTheme.mockClear();
    });

    test('AuthorizedHomeMain – вызывает смену темы и отображает баннеры', () => {
        render(
            <MemoryRouter>
                <AuthorizedHomeMain />
            </MemoryRouter>
        );

        const button = screen.getByRole('button', {
            name: /Сменить тему/i
        });

        fireEvent.click(button);
        expect(switchTheme).toHaveBeenCalled();
        expect(screen.getByText(/Генерация/)).toBeInTheDocument();
        expect(screen.getByText(/Сохранённые/)).toBeInTheDocument();
    });
});