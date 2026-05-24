import { describe, expect, test, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';

import GenerateForm from '@/components/tabs/generate-form/generate-form';

const genres = [
    { id: 1, name: 'Rock' },
    { id: 2, name: 'Metal' }
];

describe('GenerateForm', () => {
    test('GenerateForm - отображает поля и обрабатывает ввод пользователя', () => {
        const onGenreChange = vi.fn();
        const onSignatureChange = vi.fn();
        const onTonalityChange = vi.fn();
        const onBpmChange = vi.fn();
        const onChordProgressionChange = vi.fn();
        const onSubmit = vi.fn((e) => e.preventDefault());

        render(
            <GenerateForm
                genres={genres}
                loading={false}
                genreId={null}
                signature="4/4"
                tonality={0}
                bpm={120}
                chordProgression=""
                onGenreChange={onGenreChange}
                onSignatureChange={onSignatureChange}
                onTonalityChange={onTonalityChange}
                onBpmChange={onBpmChange}
                onChordProgressionChange={onChordProgressionChange}
                onSubmit={onSubmit}
            />
        );

        fireEvent.click(screen.getByLabelText('Рок'));
        expect(onGenreChange).toHaveBeenCalledWith(1);

        fireEvent.click(screen.getByLabelText('3/4'));
        expect(onSignatureChange).toHaveBeenCalledWith('3/4');

        fireEvent.change(screen.getByRole('combobox'), {
            target: { value: '7' }
        });
        expect(onTonalityChange).toHaveBeenCalledWith(7);

        fireEvent.change(screen.getByRole('slider'), {
            target: { value: '150' }
        });
        expect(onBpmChange).toHaveBeenCalledWith(150);

        fireEvent.change(screen.getByPlaceholderText('Введите ритм-партию...'), {
            target: { value: 'C-1/2,G7-1/4,Am-1' }
        });
        expect(onChordProgressionChange).toHaveBeenCalledWith('C-1/2,G7-1/4,Am-1');

        fireEvent.click(screen.getByRole('button', { name: 'Сгенерировать' }));
        expect(onSubmit).toHaveBeenCalled();
    });

    test('GenerateForm - показывает состояние загрузки и блокирует элементы управления', () => {
        render(
            <GenerateForm
                genres={genres}
                loading={true}
                genreId={null}
                signature="4/4"
                tonality={0}
                bpm={120}
                chordProgression=""
                onGenreChange={vi.fn()}
                onSignatureChange={vi.fn()}
                onTonalityChange={vi.fn()}
                onBpmChange={vi.fn()}
                onChordProgressionChange={vi.fn()}
                onSubmit={vi.fn()}
            />
        );

        expect(screen.getByRole('button', { name: 'Генерация...' })).toBeDisabled();
        expect(screen.getByLabelText('Рок')).toBeDisabled();
        expect(screen.getByRole('slider')).toBeDisabled();
        expect(screen.getByPlaceholderText('Введите ритм-партию...')).toBeDisabled();
    });
});