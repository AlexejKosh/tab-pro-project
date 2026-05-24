import { describe, expect, beforeEach, test } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';

import LoginPage from '@/pages/login-page/login-page';

describe('LoginPage', () => {
    beforeEach(() => {
        document.title = '';
    });

    test('LoginPage - отображает форму входа и устанавливает заголовок документа', () => {
        render(
            <MemoryRouter>
                <LoginPage />
            </MemoryRouter>
        );

        expect(screen.getByText(/Вход в аккаунт/i)).toBeInTheDocument();
        expect(document.title).toBe('Вход');
    });
});
