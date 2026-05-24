import { describe, expect, test } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';

import ProjectInfo from '@/components/about/project-info/project-info';

describe('ProjectInfo', () => {
    
    test('ProjectInfo - отображает заголовки и описание', () => {
        render(
            <MemoryRouter>
                <ProjectInfo />
            </MemoryRouter>
        );

        expect(screen.getByRole('link', { name: /На главную/i })).toBeInTheDocument();
        expect(screen.getByText(/О платформе/i)).toBeInTheDocument();
        expect(screen.getByText(/Процесс генерации/i)).toBeInTheDocument();
    });
});
