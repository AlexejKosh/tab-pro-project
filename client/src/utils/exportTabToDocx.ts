import {
    AlignmentType,
    Document,
    Footer,
    PageBreak,
    PageNumber,
    Paragraph,
    Packer,
    TextRun,
} from "docx";
import { saveAs } from "file-saver";

const LENGTH_LIMIT = 70;
const HEIGHT_LIMIT = 44;

interface ExportTabToDocxInput {
    title: string;
    rhythm: string;
    tabData: string;
}

function convertRhythm(rhythm: string): string {
    const chords = rhythm
        .replace(/\s+/g, "")
        .split(",")
        .filter(Boolean)
        .map((chord) => chord.charAt(0).toUpperCase() + chord.slice(1));

    return chords.join(", ");
}

function sanitizeFileName(fileName: string): string {
    return fileName.replace(/[\\/:*?"<>|]+/g, "_").trim();
}

function buildPages(title: string, rhythm: string, tabData: string): string[] {
    const tabLayout: Record<string, string[]> = {
        header: [
            `Название: ${title}.`,
            `Аккорды: ${convertRhythm(rhythm)}.`,
        ],
        bars: ["   "],
        string_e: ["e| "],
        string_b: ["B| "],
        string_g: ["G| "],
        string_d: ["D| "],
        string_a: ["A| "],
        string_E: ["E| "],
    };

    const tabLines = tabData.split("\n");

    for (let lineIndex = 0; lineIndex < tabLines.length; lineIndex += 1) {
        const line = tabLines[lineIndex];

        if (lineIndex === 0) {
            tabLayout.header.push(line);
        } else if ((lineIndex - 1) % 8 === 0) {
            continue;
        } else if ((lineIndex - 2) % 8 === 0) {
            tabLayout.bars.push(line.trim().slice(0, -1));
        } else {
            const stringKey = {
                1: "string_e",
                2: "string_b",
                3: "string_g",
                4: "string_d",
                5: "string_a",
                6: "string_E",
            }[(lineIndex - 2) % 8] as keyof typeof tabLayout;

            tabLayout[stringKey].push(line.slice(3));
        }
    }

    const segmentLengths: number[] = [];
    let currentSegmentLength = 0;

    for (const stringFragment of tabLayout.string_e) {
        for (let charIndex = 0; charIndex < stringFragment.length; charIndex += 1) {
            if (charIndex !== stringFragment.length - 1) {
                if (currentSegmentLength !== LENGTH_LIMIT) {
                    currentSegmentLength += 1;
                } else {
                    segmentLengths.push(currentSegmentLength);
                    currentSegmentLength = 1;
                }
            } else {
                if (currentSegmentLength === LENGTH_LIMIT) {
                    segmentLengths.push(currentSegmentLength);
                    currentSegmentLength = 1;
                } else if (LENGTH_LIMIT - currentSegmentLength < 8) {
                    currentSegmentLength += 1;
                    segmentLengths.push(currentSegmentLength);
                    currentSegmentLength = 0;
                } else {
                    currentSegmentLength += 1;
                }
            }
        }
    }

    if (currentSegmentLength) {
        segmentLengths.push(currentSegmentLength);
    }

    for (let barIndex = 0; barIndex < tabLayout.bars.length; barIndex += 1) {
        tabLayout.bars[barIndex] += " ".repeat(
            tabLayout.string_e[barIndex].length - tabLayout.bars[barIndex].length,
        );
    }

    for (const key of [
        "bars",
        "string_e",
        "string_b",
        "string_g",
        "string_d",
        "string_a",
        "string_E",
    ] as const) {
        tabLayout[key] = [tabLayout[key].join("")];
    }

    const pages: string[] = [""];

    const [titleLine, chordsLine, signatureLine] = tabLayout.header.join("\n").split("\n");

    for (const infoLine of [titleLine, chordsLine]) {
        let line = infoLine;

        while (line.length > 0) {
            pages[0] += line.slice(0, LENGTH_LIMIT) + "\n";
            line = line.slice(LENGTH_LIMIT);
        }
    }

    pages[0] += signatureLine + "\n\n";

    const maxSegmentsOnFirstPage =
        Math.floor((HEIGHT_LIMIT - pages[0].split("\n").length + 1) / 7);

    for (let segmentIndex = 0; segmentIndex < segmentLengths.length; segmentIndex += 1) {
        const segmentLength = segmentLengths[segmentIndex];

        const pageIndex =
            segmentIndex < maxSegmentsOnFirstPage
                ? 0
                : Math.floor((segmentIndex - maxSegmentsOnFirstPage) / 6) + 1;

        if (pages.length <= pageIndex) {
            pages.push("");
        }

        for (const key of [
            "bars",
            "string_e",
            "string_b",
            "string_g",
            "string_d",
            "string_a",
            "string_E",
        ] as const) {
            pages[pageIndex] += tabLayout[key][0].slice(0, segmentLength) + "\n";
            tabLayout[key][0] = tabLayout[key][0].slice(segmentLength);
        }
    }

    return pages;
}

function createLineParagraph(line: string): Paragraph {
    return new Paragraph({
        alignment: AlignmentType.LEFT,
        spacing: {
            before: 0,
            after: 0,
            line: 276,
        },
        indent: {
            firstLine: 0,
        },
        children: [
            new TextRun({
                text: line,
                font: "Consolas",
                size: 24, // 12 pt
            }),
        ],
    });
}

export async function exportTabToDocx({
    title,
    rhythm,
    tabData,
}: ExportTabToDocxInput): Promise<void> {
    const pages = buildPages(title, rhythm, tabData);

    const children: Paragraph[] = [];

    for (let pageIndex = 0; pageIndex < pages.length; pageIndex += 1) {
        const lines = pages[pageIndex].split("\n");

        for (const line of lines) {
            children.push(createLineParagraph(line));
        }

        if (pageIndex < pages.length - 1) {
            children.push(
                new Paragraph({
                    children: [new PageBreak()],
                }),
            );
        }
    }

    const fileName = `Табулатура - ${sanitizeFileName(title)}.docx`;

    const doc = new Document({
        sections: [
            {
                properties: {
                    page: {
                        size: {
                            width: 11906,
                            height: 16838,
                        },
                        margin: {
                            top: 1134,
                            right: 850,
                            bottom: 1250,
                            left: 1701,
                            header: 0,
                            footer: 600,
                            gutter: 0
                        }
                    },
                },
                footers: {
                    default: new Footer({
                        children: [
                            new Paragraph({
                                alignment: AlignmentType.CENTER,
                                children: [
                                    new TextRun({
                                        children: [PageNumber.CURRENT],
                                        font: "Consolas",
                                        size: 24,
                                    }),
                                ],
                            }),
                        ],
                    }),
                },
                children,
            },
        ],
    });

    const blob = await Packer.toBlob(doc);
    saveAs(blob, fileName);
}