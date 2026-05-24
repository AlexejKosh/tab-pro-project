import { describe, expect, beforeEach, test } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';

import NotFoundPage from '@/pages/not-found-page/not-found-page';

describe('NotFoundPage', () => {
    beforeEach(() => {
        document.title = '';
    });

    test('NotFoundPage - отображает сообщение 404 и ссылку на главную', () => {
        render(
            <MemoryRouter>
                <NotFoundPage />
            </MemoryRouter>
        );

        expect(screen.getByText(/Ошибка 404/i)).toBeInTheDocument();
        expect(screen.getByText(/Данная страница не найдена/i)).toBeInTheDocument();
        expect(screen.getByRole('link', { name: /На главную/i })).toBeInTheDocument();
        expect(document.title).toBe('Страница не найдена');
    });
});
