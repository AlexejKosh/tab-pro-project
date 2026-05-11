import pytest
import os
import sys

# Добавление корня проекта в PYTHONPATH (для корректных импортов)
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), "..")))

from preprocessing import encode_rhythm as er


# Возвращает индексы активных нот (значений 1) в бинарном представлении аккорда
def ones_in_chord(chord):
    return [i for i, v in enumerate(chord) if v == 1]


# Проверка кодирования аккорда с целой длительностью:
# аккорд "C-1" в размере 4/4 должен занимать 48 единиц
# и содержать ноты C, E, G (0, 4, 7)
def test_encode_rhythm_integer_duration():
    seq = er.encode_rhythm("C-1", 4)

    assert isinstance(seq, list)
    assert len(seq) == 48
    assert ones_in_chord(seq[0]) == [0, 4, 7]


# Проверка обработки дробной длительности и нескольких аккордов подряд:
# "C-1/2" в размере 4/4 должен занимать 24 единицы, а "Am-1" - 48 единиц
# и содержать ноты Am (0, 4, 9)
def test_encode_rhythm_fractional_and_multiple():
    seq = er.encode_rhythm("C-1/2,Am-1", 4)

    assert len(seq) == 24 + 48
    assert ones_in_chord(seq[0]) == [0, 4, 7]
    assert set(ones_in_chord(seq[24])) == {0, 4, 9}


# Проверка автоматической замены бемолей на диезы:
# "Db-1" должен кодироваться так же, как "C#-1"
# то есть содержать ноты C#, E, G# (1, 5, 8)
def test_encode_rhythm_flats():
    seq = er.encode_rhythm("Db-1", 4)

    assert ones_in_chord(seq[0]) == [1, 5, 8]


# Проверка транспонирования аккорда на заданное количество полутонов:
# "C-1" с tune_difference=2 должен кодироваться так же, как "D-1"
# то есть содержать ноты D, F#, A (2, 6, 9)
def test_encode_rhythm_tune_shift():
    seq2 = er.encode_rhythm("C-1", 3, tune_difference=2)

    assert ones_in_chord(seq2[0]) == [2, 6, 9]


# Проверка ошибки при неверном формате аккорда:
# аккорд "C1" без дефиса должен вызывать ValueError
def test_encode_rhythm_missing_separator():
    with pytest.raises(ValueError, match="Некорректный формат аккордов"):
        er.encode_rhythm("C1", 4)


# Проверка ошибки при недопустимом знаменателе дробной длительности:
# аккорд "C-1/5" с недопустимым знаменателем должен вызывать ValueError
def test_encode_rhythm_invalid_fraction():
    with pytest.raises(ValueError, match="Недопустимый знаменатель"):
        er.encode_rhythm("C-1/5", 4)


# Проверка ошибки при неподдерживаемом размере такта:
# размер такта 5/4 не поддерживается и должен вызывать ValueError
def test_encode_rhythm_unsupported_signature():
    with pytest.raises(ValueError, match="signature должен быть 3 или 4"):
        er.encode_rhythm("C-1", 5)

# Проверка ошибки при слишком большой последовательности аккордов:
# Если превышает 36 такта, должна вызываться ошибка ValueError
def test_encode_rhythm_too_long_sequence():
    long_sequence = ",".join([f"C-1"] * 37)
    with pytest.raises(ValueError, match="Слишком длинная последовательность аккордов"):
        er.encode_rhythm(long_sequence, 4)

# Проверка защитной ветки обработки некорректного аккорда:
# даже если строка прошла regex-проверку, неверный аккорд должен вызывать ValueError
def test_encode_rhythm_invalid_chord_after_regex(monkeypatch):
    # Мок regex-проверки, чтобы пропустить неверный аккорд дальше в код
    monkeypatch.setattr(er.re, "fullmatch", lambda *args, **kwargs: True)

    with pytest.raises(ValueError, match="Некорректный аккорд"):
        er.encode_rhythm("H-1", 4)