/// <reference types="vitest" />
import '@testing-library/jest-dom';

// Global test setup can go here (e.g., mocking matchMedia, global fetch, etc.)
// Example: provide a basic window.matchMedia mock if needed by components
if (typeof window !== 'undefined' && !window.matchMedia) {
	// @ts-ignore
	window.matchMedia = () => ({
		matches: false,
		addListener: () => {},
		removeListener: () => {},
		addEventListener: () => {},
		removeEventListener: () => {},
		dispatchEvent: () => false
	});
}
