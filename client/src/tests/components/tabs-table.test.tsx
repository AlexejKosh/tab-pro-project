import { describe, expect, test, vi } from 'vitest';
import { render, screen } from '@testing-library/react';

import TabsTable from '@/components/tabs/tabs-table/tabs-table';

const tabs = [
    {
        id: 1,
        title: 'Solo',
        createdAt: '2026-05-24',
        signature: '4/4',
        genreName: 'Рок',
        chordProgression: 'C G',
        genreId: 1
    }
];

describe('TabsTable', () => {
    test('TabsTable - отображает заголовки таблицы', () => {
        render(
            <TabsTable
                tabs={tabs}
                onRowClick={vi.fn()}
                formatDate={() => '24.05.2026'}
            />
        );

        expect(screen.getByText('Название')).toBeInTheDocument();
        expect(screen.getByText('Дата')).toBeInTheDocument();
    });

    test('TabsTable - отображает строки данных', () => {
        render(
            <TabsTable
                tabs={tabs}
                onRowClick={vi.fn()}
                formatDate={() => '24.05.2026'}
            />
        );

        expect(screen.getByText('Solo')).toBeInTheDocument();
    });
});