import TabsTableRow from "@/components/tabs/tabs-table-row/tabs-table-row";

import type { TabSummaryResponse } from "@/types/tab";

interface Props {
        tabs: Array<TabSummaryResponse & { genreName: string }>;
        onRowClick: (id: number) => void;
        formatDate: (isoDate: string) => string;
}

export default function TabsTable({ tabs, onRowClick, formatDate }: Props) {

    return (
        <table className="tabs-table">
            <thead>
                <tr>
                    <th className="title-cell">Название</th>
                    <th className="date-cell">Дата</th>
                    <th className="signature-cell">Размер</th>
                    <th className="genre-cell">Жанр</th>
                    <th className="chords-cell">Аккорды</th>
                </tr>
            </thead>
            <tbody>
                {tabs.map((tab) => (
                    <TabsTableRow
                        key={tab.id}
                        tab={tab}
                        onClick={onRowClick}
                        formatDate={formatDate}
                    />
                ))}
            </tbody>
        </table>
    );
};