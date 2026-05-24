import { describe, test, expect } from 'vitest';
import { render } from '@testing-library/react';

import { useDocumentTitle } from '@/hooks/use-document-title';

const TestComponent = ({ title }: { title: string }) => {
    useDocumentTitle(title);
    return <div />;
};

describe('useDocumentTitle', () => {
    test('useDocumentTitle - устанавливает document.title', () => {
        render(<TestComponent title="My Title" />);
        expect(document.title).toBe('My Title');
    });
});
