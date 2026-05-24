import { describe, expect, beforeEach, test } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';

import AboutPage from '@/pages/about-page/about-page';

describe('AboutPage', () => {
    beforeEach(() => {
        document.title = '';
    });

    test('AboutPage - отображает информацию о проекте и устанавливает заголовок документа', () => {
        render(
            <MemoryRouter>
                <AboutPage />
            </MemoryRouter>
        );

        expect(screen.getByText(/О проекте/i)).toBeInTheDocument();
        expect(screen.getByRole('link', { name: /На главную/i })).toBeInTheDocument();
        expect(document.title).toBe('О проекте');
    });
});
