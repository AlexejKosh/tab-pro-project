import { describe, test, expect, beforeEach, afterEach, vi } from 'vitest';
import { useRef } from 'react';
import { render, fireEvent, act } from '@testing-library/react';

import { useBannerInteractions } from '@/hooks/use-banner-interactions';

const TestComponent = () => {
    const ref = useRef<HTMLDivElement | null>(null);
    useBannerInteractions(ref as any);
    return (
        <div ref={ref}>
            <section className="banner">
                <div className="banner-particles"></div>
            </section>
        </div>
    );
};

describe('useBannerInteractions', () => {
    beforeEach(() => {
        vi.useFakeTimers();
    });

    afterEach(() => {
        vi.useRealTimers();
        document.body.innerHTML = '';
        vi.restoreAllMocks();
    });

    test('useBannerInteractions - обрабатывает mousemove, mouseenter и создает частицы', () => {
        const { container } = render(<TestComponent />);
        const section = container.querySelector('.banner') as HTMLElement;
        const particles = section.querySelector('.banner-particles') as HTMLElement;

        fireEvent.mouseMove(section, {
            clientX: 10,
            clientY: 10,
        });
        expect(section.style.getPropertyValue('--move-x')).toBeDefined();

        fireEvent.mouseEnter(section);
        act(() => {
            vi.advanceTimersByTime(200);
        });

        const particle = particles.querySelector('.particle');
        expect(particle).not.toBeNull();

        fireEvent.mouseLeave(section);
        expect(section.style.getPropertyValue('--move-x')).toBe('0px');
        expect(section.style.getPropertyValue('--move-y')).toBe('0px');
    });
});
