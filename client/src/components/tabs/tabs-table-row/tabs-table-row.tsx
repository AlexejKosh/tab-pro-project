
import type { TabSummaryResponse } from "@/types/tab";

interface Props {
    tab: TabSummaryResponse & { genreName: string };
    onClick: (id: number) => void;
    formatDate: (isoDate: string) => string;
}

export default function TabsTableRow({ tab, onClick, formatDate }: Props) {
    const shortenChordProgression = (progression: string): string => {
        const MAX_LENGTH = 35;

        if (progression.length <= MAX_LENGTH) {
            return progression;
        }

        return `${progression.slice(0, MAX_LENGTH)}...`;
    };

    return (
        <tr
            className="tabs-table-row"
            onClick={() => onClick(tab.id)}
            tabIndex={0}
            role="button"
            onKeyDown={(event) => {
                if (event.key === "Enter" || event.key === " ") {
                    event.preventDefault();
                    onClick(tab.id);
                }
            }}
            >
            <td className="title-cell">{tab.title}</td>
            <td className="date-cell">{formatDate(tab.createdAt)}</td>
            <td className="signature-cell">{tab.signature}</td>
            <td className="genre-cell">{tab.genreName}</td>
            <td className="chords-cell">{shortenChordProgression(tab.chordProgression)}</td>
        </tr>
    );
};