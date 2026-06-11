import os
import sys

import numpy as np
import pytest
import torch
import torch.nn as nn

# Добавление корня проекта в PYTHONPATH
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), "..")))

from core import generate_solo as gs


@pytest.fixture(autouse=True)
def force_cpu(monkeypatch):
    # Мок DEVICE с целью не зависеть от наличия CUDA на машине запуска
    monkeypatch.setattr(gs, "DEVICE", "cpu", raising=False)


# Проверка построения признаков аккордов, включая сдвиг по тональности и флаги позиций
def test_build_chord_features_valid():
    chords = []
    for i in range(50):
        chord = np.zeros(gs.CHORD_DIM, dtype=np.float32)
        chord[0 if i % 2 == 0 else 7] = 1.0
        chords.append(chord.tolist())

    features = gs.build_chord_features(chords, key=1)

    assert len(features) == 50
    assert features[0].shape[0] == gs.INPUT_DIM

    abs_part = features[0][:gs.CHORD_DIM]
    rel_part = features[0][gs.CHORD_DIM:gs.CHORD_DIM * 2]
    flags = features[0][-3:]

    assert np.array_equal(abs_part, np.asarray(chords[0], dtype=np.float32))
    assert np.array_equal(rel_part, np.roll(np.asarray(chords[0], dtype=np.float32), -1))
    assert np.array_equal(flags, np.array([1.0, 0.0, 0.0], dtype=np.float32))

    middle_flags = features[1][-3:]
    end_flags = features[2][-3:]
    last_flags = features[-1][-3:]

    assert np.array_equal(middle_flags, np.array([0.0, 1.0, 0.0], dtype=np.float32))
    assert np.array_equal(end_flags, np.array([0.0, 0.0, 1.0], dtype=np.float32))
    assert np.array_equal(last_flags, np.array([0.0, 0.0, 1.0], dtype=np.float32))


# Проверка обработки пустой последовательности аккордов
def test_build_chord_features_empty():
    assert gs.build_chord_features([], key=0) == []


# Проверка позиционного кодирования на корректную форму и добавление значений
def test_positional_encoding_forward():
    pe = gs.PositionalEncoding(d_model=4, max_len=5)
    x = torch.zeros((1, 3, 4), dtype=torch.float32)

    out = pe(x)

    assert out.shape == x.shape
    assert torch.allclose(out, pe.pe[:, :3])


# Проверка сборки модели и формы выходов encode/decode
def test_solo_transformer_encode_decode_shapes():
    model = gs.SoloTransformer()

    features = torch.zeros((1, 2, gs.INPUT_DIM), dtype=torch.float32)
    encoded = model.encode(features)

    y = torch.tensor(
        [[[gs.NOTE_BOS, gs.STATE_BOS, 1, 1],
          [2, 3, 4, 5]]],
        dtype=torch.long
    )
    decoded = model.decode(y)

    assert encoded.shape == (1, 2, gs.D_MODEL)
    assert decoded.shape == (1, 2, gs.D_MODEL)


# Проверка sample на защиту от температуры 0 и корректный вызов multinomial
def test_sample_temperature_clamp(monkeypatch):
    captured = {}

    def fake_multinomial(probs, num_samples):
        captured["probs"] = probs.detach().clone()
        return torch.tensor([2])

    # Мок torch.multinomial с целью сделать выбор детерминированным
    monkeypatch.setattr(torch, "multinomial", fake_multinomial)

    idx = gs.sample(torch.tensor([1.0, 2.0, 3.0]), temperature=0.0)

    assert idx == 2
    assert captured["probs"].shape == (3,)
    assert torch.isfinite(captured["probs"]).all()
    assert torch.isclose(captured["probs"].sum(), torch.tensor(1.0))


# Проверка sample на очистку NaN и бесконечностей
def test_sample_sanitizes_nan_and_inf(monkeypatch):
    def fake_softmax(logits, dim=-1):
        return torch.tensor([float("nan"), float("inf"), float("-inf")])

    captured = {}

    def fake_multinomial(probs, num_samples):
        captured["probs"] = probs.detach().clone()
        return torch.tensor([0])

    # Мок torch.softmax с целью вернуть некорректные значения и проверить очистку
    monkeypatch.setattr(torch, "softmax", fake_softmax)
    # Мок torch.multinomial с целью не зависеть от реального семплирования
    monkeypatch.setattr(torch, "multinomial", fake_multinomial)

    idx = gs.sample(torch.tensor([1.0, 2.0, 3.0]), temperature=1.0)

    assert idx == 0
    assert torch.isfinite(captured["probs"]).all()


# Проверка удаления состояний у пауз
def test_fix_pause_state():
    note = [0, 5, 0, 6]

    gs.fix_pause_state(note)

    assert note == [0, 0, 0, 0]


# Проверка исправления состояния первой ноты соло
def test_fix_first_note_state():
    note = [12, 6, 16, 0]

    gs.fix_first_note_state(note)

    assert note == [12, 1, 16, 1]


# Проверка исправления состояния при повторении ноты и при её изменении
def test_fix_continue_state():
    same_curr = [12, 0, 16, 0]
    same_prev = [12, 1, 16, 1]

    gs.fix_continue_state(same_curr, same_prev)
    assert same_curr == [12, 6, 16, 6]

    diff_curr = [13, 6, 17, 6]
    diff_prev = [12, 1, 16, 1]

    gs.fix_continue_state(diff_curr, diff_prev)
    assert diff_curr == [13, 1, 17, 1]


# Проверка постобработки пустого соло
def test_edit_solo_empty():
    assert gs.edit_solo([]) == []


# Проверка постобработки соло: первая нота, продолжение и смена нот
def test_edit_solo_rewrites_states():
    solo = [
        [12, 0, 16, 6],
        [12, 0, 16, 0],
        [13, 6, 17, 6],
    ]

    out = gs.edit_solo(solo)

    assert out[0] == [12, 1, 16, 1]
    assert out[1] == [12, 6, 16, 6]
    assert out[2] == [13, 1, 17, 1]


class FakeWindowModel:
    def __init__(self):
        self.decode_inputs = []
        self.transformer_inputs = []

    def decode(self, generated):
        self.decode_inputs.append(generated.detach().clone())
        return torch.zeros((generated.size(0), generated.size(1), gs.D_MODEL), dtype=torch.float32)

    def transformer(self, src, tgt, tgt_mask):
        self.transformer_inputs.append((src.shape, tgt.shape, tgt_mask.shape))
        return torch.zeros((src.size(0), tgt.size(1), gs.D_MODEL), dtype=torch.float32)

    def note1_head(self, last):
        return torch.zeros((last.size(0), gs.NOTE_CLASSES), dtype=torch.float32)

    def state1_head(self, last):
        return torch.zeros((last.size(0), gs.STATE_CLASSES), dtype=torch.float32)

    def note2_head(self, last):
        return torch.zeros((last.size(0), gs.NOTE_CLASSES), dtype=torch.float32)

    def state2_head(self, last):
        return torch.zeros((last.size(0), gs.STATE_CLASSES), dtype=torch.float32)


# Проверка генерации одного окна с warm-start и несколькими шагами
def test_generate_window_with_warm_tokens(monkeypatch):
    model = FakeWindowModel()
    memory = torch.zeros((1, 5, gs.D_MODEL), dtype=torch.float32)

    sample_values = iter([1, 2, 3, 4, 5, 6, 7, 8])

    # Мок sample с целью сделать генерацию полностью детерминированной
    monkeypatch.setattr(gs, "sample", lambda logits, temperature=0.7: next(sample_values))

    out = gs.generate_window(
        model=model,
        memory=memory,
        warm_tokens=[[9, 9, 9, 9]],
        new_tokens_count=2,
        temperature=0.7,
    )

    assert model.decode_inputs[0].shape == (1, 2, 4)
    assert len(out) == 2
    assert out == [[1, 2, 3, 4], [5, 6, 7, 8]]


# Проверка генерации окна без новых токенов
def test_generate_window_zero_new_tokens():
    model = FakeWindowModel()
    memory = torch.zeros((1, 3, gs.D_MODEL), dtype=torch.float32)

    out = gs.generate_window(
        model=model,
        memory=memory,
        warm_tokens=[],
        new_tokens_count=0,
        temperature=0.7,
    )

    assert out == []


class FakeSoloModel:
    def __init__(self):
        self.loaded = None
        self.device = None
        self.eval_called = False
        self.encoded_shapes = []

    def to(self, device):
        self.device = device
        return self

    def load_state_dict(self, state):
        self.loaded = state

    def eval(self):
        self.eval_called = True

    def encode(self, features):
        self.encoded_shapes.append(tuple(features.shape))
        return torch.zeros((1, features.size(1), gs.D_MODEL), dtype=torch.float32)


# Проверка generate_solo на пустом входе
def test_generate_solo_empty_input(monkeypatch):
    model = FakeSoloModel()

    # Мок SoloTransformer с целью не создавать реальную модель
    monkeypatch.setattr(gs, "MODELS", {"rock": model})
    # Мок build_chord_features с целью вернуть пустую последовательность
    monkeypatch.setattr(gs, "build_chord_features", lambda chords, key: [])

    result = gs.generate_solo(chords=[], key=0, genre="rock", temperature=0.7)

    assert result == []


# Проверка generate_solo на ветку, где второй шаг окна уже не требуется
def test_generate_solo_break_on_negative_new_tokens(monkeypatch):
    model = FakeSoloModel()
    calls = []

    features = [np.zeros(gs.INPUT_DIM, dtype=np.float32).tolist() for _ in range(180)]

    # Мок SoloTransformer с целью подменить реальную модель заглушкой
    monkeypatch.setattr(gs, "MODELS", {"rock": model})
    # Мок build_chord_features с целью подать уже подготовленные тестовые признаки
    monkeypatch.setattr(gs, "build_chord_features", lambda chords, key: features)

    def fake_generate_window(model, memory, warm_tokens, new_tokens_count, temperature):
        calls.append((len(warm_tokens), new_tokens_count, tuple(memory.shape)))
        return [[1, 0, 0, 0] for _ in range(new_tokens_count)]

    # Мок generate_window с целью проверить только логику окон
    monkeypatch.setattr(gs, "generate_window", fake_generate_window)
    # Мок edit_solo с целью не менять результат постобработкой
    monkeypatch.setattr(gs, "edit_solo", lambda solo: solo)

    result = gs.generate_solo(chords=list(range(180)), key=0, genre="rock", temperature=0.7)

    assert len(result) == 180
    assert len(calls) == 1
    assert calls[0] == (0, 180, (1, 180, gs.D_MODEL))


# Проверка generate_solo на два окна с перекрытием
def test_generate_solo_two_windows(monkeypatch):
    model = FakeSoloModel()
    calls = []

    features = [np.zeros(gs.INPUT_DIM, dtype=np.float32).tolist() for _ in range(200)]

    # Мок SoloTransformer с целью подменить реальную модель заглушкой
    monkeypatch.setattr(gs, "MODELS", {"rock": model})
    # Мок build_chord_features с целью вернуть длинную последовательность тестовых признаков
    monkeypatch.setattr(gs, "build_chord_features", lambda chords, key: features)

    def fake_generate_window(model, memory, warm_tokens, new_tokens_count, temperature):
        calls.append((len(warm_tokens), new_tokens_count, tuple(memory.shape)))
        return [[new_tokens_count, 0, 0, 0] for _ in range(new_tokens_count)]

    # Мок generate_window с целью проверить разбиение на окна и перекрытие
    monkeypatch.setattr(gs, "generate_window", fake_generate_window)
    # Мок edit_solo с целью не вмешиваться в итоговую последовательность
    monkeypatch.setattr(gs, "edit_solo", lambda solo: solo)

    result = gs.generate_solo(chords=list(range(200)), key=0, genre="rock", temperature=0.7)

    assert len(result) == 200
    assert len(calls) == 2
    assert calls[0] == (0, 192, (1, 192, gs.D_MODEL))
    assert calls[1] == (48, 8, (1, 56, gs.D_MODEL))
    assert result[0] == [192, 0, 0, 0]
    assert result[192] == [8, 0, 0, 0]


def test_load_models_loads_all_genres(monkeypatch):
    # Записываем вызовы torch.load
    load_paths = []
    loaded_states = []

    def fake_torch_load(path, map_location=None):
        load_paths.append(path)
        state = {"state_for": path}
        loaded_states.append(state)
        return state

    monkeypatch.setattr(torch, "load", fake_torch_load)

    created = []

    class FakeModel:
        def __init__(self):
            self.device = None
            self.loaded = None
            self.eval_called = False

        def to(self, device):
            self.device = device
            return self

        def load_state_dict(self, state):
            self.loaded = state

        def eval(self):
            self.eval_called = True

    def fake_constructor():
        m = FakeModel()
        created.append(m)
        return m

    # Подменяем конструктор модели и сбрасываем MODELS
    monkeypatch.setattr(gs, "SoloTransformer", fake_constructor)
    monkeypatch.setattr(gs, "MODELS", {})

    # Вызов функции загрузки моделей
    gs.load_models()

    # Проверяем, что создано по модели на жанр и они корректно загружены
    assert len(created) == len(gs.GENRES)

    for i, genre in enumerate(gs.GENRES):
        assert load_paths[i].endswith(f"{genre}_transformer.pt")
        assert gs.MODELS[genre] is created[i]
        assert created[i].device == gs.DEVICE
        assert created[i].loaded == loaded_states[i]
        assert created[i].eval_called