import { describe, expect, beforeEach, test, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';

import TabsTableRow from '@/components/tabs/tabs-table-row/tabs-table-row';

const mockClick = vi.fn();

const mockTab = {
    id: 1,
    title: 'Rock Solo',
    createdAt: '2026-05-24',
    signature: '4/4',
    genreName: 'Рок',
    chordProgression: 'C G Am F Dm Em A B C G Am F Dm Em',
    genreId: 1
};

describe('TabsTableRow', () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    test('TabsTableRow - отображает данные строки', () => {
        render(
            <table>
                <tbody>
                    <TabsTableRow
                        tab={mockTab}
                        onClick={mockClick}
                        formatDate={() => '24.05.2026'}
                    />
                </tbody>
            </table>
        );

        expect(screen.getByText('Rock Solo')).toBeInTheDocument();
        expect(screen.getByText('24.05.2026')).toBeInTheDocument();
        expect(screen.getByText('Рок')).toBeInTheDocument();
    });

    test('TabsTableRow - вызывает onClick при клике на строку', () => {
        render(
            <table>
                <tbody>
                    <TabsTableRow
                        tab={mockTab}
                        onClick={mockClick}
                        formatDate={() => '24.05.2026'}
                    />
                </tbody>
            </table>
        );

        fireEvent.click(screen.getByRole('button'));

        expect(mockClick).toHaveBeenCalledWith(1);
    });

    test('TabsTableRow - вызывает onClick при нажатии Enter', () => {
        render(
            <table>
                <tbody>
                    <TabsTableRow
                        tab={mockTab}
                        onClick={mockClick}
                        formatDate={() => '24.05.2026'}
                    />
                </tbody>
            </table>
        );

        fireEvent.keyDown(screen.getByRole('button'), { key: 'Enter' });

        expect(mockClick).toHaveBeenCalledWith(1);
    });
});