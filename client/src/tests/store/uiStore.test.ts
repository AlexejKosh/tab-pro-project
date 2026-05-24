import { describe, test, expect, beforeEach } from 'vitest';

import { useUiStore } from '@/store/uiStore';

describe('useUiStore', () => {
    beforeEach(() => {
        useUiStore.setState({ currentMessage: null });
    });

    test('useUiStore - setMessage и clearMessage работают корректно', () => {
        const msg = {
            message: 'Hi',
            type: 'info',
        } as any;
        useUiStore.getState().setMessage(msg);
        expect(useUiStore.getState().currentMessage).toEqual(msg);

        useUiStore.getState().clearMessage();
        expect(useUiStore.getState().currentMessage).toBeNull();
    });
});
