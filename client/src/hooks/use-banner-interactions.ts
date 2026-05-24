import { useEffect } from "react";

export const useBannerInteractions = (
    rootRef: React.RefObject<HTMLElement | null>
) => {
    useEffect(() => {
        const root = rootRef.current;
        if (!root) {
            return;
        }

        const interactiveSections =
            root.querySelectorAll<HTMLElement>(".banner");
        const intervals: number[] = [];
        const cleanupFunctions: Array<() => void> = [];
        interactiveSections.forEach((section) => {
            const container = section.querySelector(
                ".banner-particles"
            ) as HTMLDivElement | null;
            let interval: number | undefined;

            const handleMouseMove = (e: Event) => {
                const event = e as MouseEvent;
                const rect = section.getBoundingClientRect();
                const x = (event.clientX - rect.left) / rect.width;
                const y = (event.clientY - rect.top) / rect.height;
                const moveX = (0.5 - x) * 25;
                const moveY = (0.5 - y) * 25;
                section.style.setProperty("--move-x", `${moveX}px`);
                section.style.setProperty("--move-y", `${moveY}px`);
            };

            const createParticle = () => {
                if (!container) {
                    return;
                }

                const particle = document.createElement("div");
                particle.classList.add("particle");
                particle.style.left = `${Math.random() * 100}%`;
                const size = Math.random() * 4 + 2;
                particle.style.width = `${size}px`;
                particle.style.height = `${size}px`;
                const duration = Math.random() * 3 + 2;
                particle.style.animationDuration = `${duration}s`;
                container.appendChild(particle);
                window.setTimeout(() => {
                    particle.remove();
                }, duration * 1000);
            };

            const handleMouseEnter = () => {
                interval = window.setInterval(createParticle, 60);
                intervals.push(interval);
            };

            const handleMouseLeave = () => {
                if (interval) {
                    clearInterval(interval);
                }

                section.style.setProperty("--move-x", "0px");
                section.style.setProperty("--move-y", "0px");
            };

            section.addEventListener("mousemove", handleMouseMove);
            section.addEventListener("mouseenter", handleMouseEnter);
            section.addEventListener("mouseleave", handleMouseLeave);
            cleanupFunctions.push(() => {
                section.removeEventListener("mousemove", handleMouseMove);
                section.removeEventListener("mouseenter", handleMouseEnter);
                section.removeEventListener("mouseleave", handleMouseLeave);

                if (interval) {
                    clearInterval(interval);
                }
            });
        });

        return () => {
            cleanupFunctions.forEach((fn) => fn());
            intervals.forEach((id) => {
                clearInterval(id);
                clearTimeout(id);
            });
        };
    }, [rootRef]);
};