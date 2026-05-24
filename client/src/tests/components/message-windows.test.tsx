import { describe, expect, beforeEach, test, afterEach, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import { act } from 'react';

import MessageWindow from '@/components/common/message-window/message-window';

import { useUiStore } from '@/store/uiStore';

describe('MessageWindow', () => {
    beforeEach(() => {
        vi.useFakeTimers();
        useUiStore.setState({ currentMessage: null });
    });

    afterEach(() => {
        vi.useRealTimers();
    });

    test('MessageWindow - отображает сообщение', () => {
        useUiStore.setState({
            currentMessage: {
                message: 'Успешно',
                type: 'success'
            }
        });

        render(<MessageWindow />);

        expect(screen.getByText('Успешно')).toBeInTheDocument();
    });

    test('MessageWindow - исчезает после таймаута', () => {
        useUiStore.setState({
            currentMessage: {
                message: 'Ошибка',
                type: 'error'
            }
        });

        render(<MessageWindow />);

        act(() => {
            vi.advanceTimersByTime(4000);
        });

        expect(useUiStore.getState().currentMessage).toBeNull();
    });
});