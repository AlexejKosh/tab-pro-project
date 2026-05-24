import { describe, expect, beforeEach, test } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';

import RegisterPage from '@/pages/register-page/register-page';

describe('RegisterPage', () => {
    beforeEach(() => {
        document.title = '';
    });

    test('RegisterPage - отображает форму регистрации и устанавливает заголовок документа', () => {
        render(
            <MemoryRouter>
                <RegisterPage />
            </MemoryRouter>
        );

        expect(screen.getByText(/Регистрация/i)).toBeInTheDocument();
        expect(document.title).toBe('Регистрация');
    });
});
