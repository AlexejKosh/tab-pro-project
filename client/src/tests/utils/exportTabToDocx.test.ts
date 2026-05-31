import { describe, test, expect, vi, beforeEach } from 'vitest';

vi.mock('docx', async () => {
    class PageBreak {}

    class TextRun {
        text?: string;
        children?: any[];
        font?: string;
        size?: number;
        constructor(opts: any) {
            this.text = opts?.text;
            this.children = opts?.children;
            this.font = opts?.font;
            this.size = opts?.size;
        }
    }

    class Paragraph {
        options: any;
        constructor(opts: any) {
            this.options = opts || {};
        }
    }

    class Footer {
        options: any;
        constructor(opts: any) {
            this.options = opts;
        }
    }

    class Document {
        sections: any[];
        constructor(opts: any) {
            this.sections = opts?.sections || [];
        }
    }

    const toBlob = vi.fn(async (doc: any) => ({ fake: 'blob', doc }));

    return {
        AlignmentType: { LEFT: 'LEFT', CENTER: 'CENTER' },
        Document,
        Footer,
        PageBreak,
        Paragraph,
        TextRun,
        Packer: { toBlob },
        PageNumber: { CURRENT: 'CURRENT_PAGE' },
    };
});

vi.mock('file-saver', () => {
    const saveAs = vi.fn();
    return { saveAs };
});

import { exportTabToDocx } from '@/utils/exportTabToDocx';
import { Packer } from 'docx';
import { saveAs } from 'file-saver';

beforeEach(() => {
    vi.clearAllMocks();
});

describe('exportTabToDocx', () => {
    test('exportTabToDocx вызывает Packer.toBlob и saveAs с корректным именем файла', async () => {
        const input = {
            title: 'Моя/Тест:Песня?',
            rhythm: 'a, b, c',
            tabData: [
                'HEADER LINE',
                'ignored',
                'barsline#',
                'e| 0123456789',
                'B| 0123456789',
                'G| 0123456789',
                'D| 0123456789',
                'A| 0123456789',
                'E| 0123456789',
            ].join('\n'),
        };

        const result = await exportTabToDocx(input as any);

        expect((Packer.toBlob as any)).toHaveBeenCalledTimes(1);
        expect((saveAs as any)).toHaveBeenCalledTimes(1);

        const calledBlob = (saveAs as any).mock.calls[0][0];
        const calledName = (saveAs as any).mock.calls[0][1];

        expect(calledBlob).toBeDefined();
        expect(calledName).toBe('Табулатура - Моя_Тест_Песня_.docx');
    });

    test('exportTabToDocx формирует заголовок с конвертированными аккордами в верхнем регистре и форматированным текстом', async () => {
        const input = {
            title: 'Title',
            rhythm: ' am,  c ,d',
            tabData: [
                'HDR',
                'x',
                'bars-#',
                'e| abcdefgh',
                'B| abcdefgh',
                'G| abcdefgh',
                'D| abcdefgh',
                'A| abcdefgh',
                'E| abcdefgh',
            ].join('\n'),
        };

        await exportTabToDocx(input as any);

        const docPassed = (Packer.toBlob as any).mock.calls[0][0];

        expect(docPassed).toBeTruthy();
        expect(docPassed.sections).toBeInstanceOf(Array);

        const children = docPassed.sections[0].children;
        const titleParagraph = children[0];
        const chordsParagraph = children[1];
        const titleText = titleParagraph.options.children[0].text;
        const chordsText = chordsParagraph.options.children[0].text;

        expect(titleText).toContain('Название: Title.');
        expect(chordsText).toContain('Аккорды: Am, C, D.');
    });
});
