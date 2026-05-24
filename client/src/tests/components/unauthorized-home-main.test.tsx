import { describe, expect, beforeEach, test, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';

import UnauthorizedHomeMain from '@/components/home/unauthorized-home-main/unauthorized-home-main';

vi.mock('@/hooks/use-banner-interactions', () => ({ useBannerInteractions: () => {} }));

const switchTheme = vi.fn();

vi.mock('@/hooks/use-theme', () => ({ useTheme: () => ({ switchTheme }) }));

describe('UnauthorizedHomeMain', () => {
    beforeEach(() => {
        switchTheme.mockClear();
    });

    test('UnauthorizedHomeMain - отображает баннеры и при нажатии на кнопку смены темы меняет её', () => {
        render(
            <MemoryRouter>
                <UnauthorizedHomeMain />
            </MemoryRouter>
        );

        const btn = screen.getByRole('button', { name: /Сменить тему/i });
        fireEvent.click(btn);
        
        expect(switchTheme).toHaveBeenCalled();
        expect(screen.getByText(/Генерация соло/i)).toBeInTheDocument();
        expect(screen.getByText(/Быстрый старт/i)).toBeInTheDocument();
        expect(screen.getByText(/Личный кабинет/i)).toBeInTheDocument();
    });
});
