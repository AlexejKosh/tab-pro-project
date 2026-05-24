import { describe, expect, beforeEach, test } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';

import ForgotPasswordPage from '@/pages/forgot-password-page/forgot-password-page';

describe('ForgotPasswordPage', () => {
    beforeEach(() => {
        document.title = '';
    });

    test('ForgotPasswordPage - отображает форму восстановления и устанавливает заголовок документа', () => {
        render(
            <MemoryRouter>
                <ForgotPasswordPage />
            </MemoryRouter>
        );

        expect(screen.getByText(/Восстановление пароля/i)).toBeInTheDocument();
        expect(document.title).toBe('Восстановление пароля');
    });
});
