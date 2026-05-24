import { defineConfig } from "vitest/config";
import react from "@vitejs/plugin-react";
import path from "path";

export default defineConfig({
    plugins: [react()],
    resolve: {
        alias: {
            "@": path.resolve(__dirname, "./src")
        }
    },
    test: {
        globals: true,
        environment: 'jsdom',
        setupFiles: 'src/tests/setup.ts',
        include: ['src/**/*.{test,spec}.{js,ts,tsx}'],
        coverage: {
            provider: "v8",
            reporter: ["text", "html"],
            include: [
                "src/api/**/*.ts",
                "src/components/**/*.tsx",
                "src/hooks/**/*.ts",
                "src/pages/**/*.tsx",
                "src/store/**/*.ts",
                "src/utils/**/*.ts",
                "src/router/**/*.ts"
            ],
            exclude: [
                "src/tests/**",
                "src/main.tsx",
                "src/App.tsx"
            ]
        }
    }
});