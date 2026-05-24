import { describe, test, expect, beforeEach } from 'vitest';
import { render, fireEvent } from '@testing-library/react';

import { useTheme } from '@/hooks/use-theme';

const TestComponent = () => {
    const { theme, switchTheme } = useTheme();
    return (
        <div>
            <span data-testid="theme">{theme}</span>
            <button data-testid="btn" onClick={switchTheme} />
        </div>
    );
};

describe('useTheme', () => {
    beforeEach(() => {
        localStorage.clear();
        const link = document.createElement('link');
        link.id = 'theme-style';
        document.head.appendChild(link);
    });

    test('useTheme - инициализирует тему из localStorage и меняет href', () => {
        localStorage.setItem('theme', 'dark');
        const { getByTestId } = render(<TestComponent />);

        expect(getByTestId('theme').textContent).toBe('dark');
        const link = document.getElementById('theme-style') as HTMLLinkElement;
        expect(link.href).toContain('global-dark-theme.css');
    });

    test('useTheme - switchTheme переключает тему и сохраняет в localStorage', () => {
        const { getByTestId } = render(<TestComponent />);
        const btn = getByTestId('btn');
        const themeSpan = getByTestId('theme');

        expect(themeSpan.textContent).toBe('light');
        fireEvent.click(btn);
        expect(themeSpan.textContent).toBe('dark');
        expect(localStorage.getItem('theme')).toBe('dark');
    });
});
