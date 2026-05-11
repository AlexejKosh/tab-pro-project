import pytest
import os
import sys

# Добавление корня проекта в PYTHONPATH (для корректных импортов)
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), "..")))

from export import export_solo_txt as est
from constants import music_constants as mc


# Проверка распознавания паузы
def test_recognize_string_pause():
    assert est.recognize_string(0, mc.NOTE_STATES["attack"]) is None


# Проверка распознавания нот на разных струнах и разных техниках
@pytest.mark.parametrize(
    "pitch,state,expected",
    [
        (1, mc.NOTE_STATES["attack"], ("E", "0")),
        (9, mc.NOTE_STATES["attack"], ("A", "3")),
        (16, mc.NOTE_STATES["attack"], ("D", "5")),
        (23, mc.NOTE_STATES["attack"], ("G", "7")),
        (30, mc.NOTE_STATES["attack"], ("B", "10")),
        (36, mc.NOTE_STATES["attack"], ("e", "10")),
        (2, mc.NOTE_STATES["slide"], ("E", "/1")),
        (3, mc.NOTE_STATES["hammer-on"], ("E", "h2")),
        (4, mc.NOTE_STATES["pull-off"], ("E", "3p")),
        (5, mc.NOTE_STATES["bend"], ("E", "4b")),
    ],
)
def test_recognize_string_mappings(pitch, state, expected):
    assert est.recognize_string(pitch, state) == expected


# Проверка добавления одной ноты: активна только первая нота
def test_add_note_to_tab_one_note_1():
    strings = est.EMPTY_STRINGS.copy()

    est.add_note_to_tab(strings, ("A", "6"), None)

    assert strings["A"] == "A| -6-"
    assert strings["e"] == "e| ---"
    assert strings["B"] == "B| ---"
    assert strings["G"] == "G| ---"
    assert strings["D"] == "D| ---"
    assert strings["E"] == "E| ---"


# Проверка добавления одной ноты: активна только вторая нота
def test_add_note_to_tab_one_note_2():
    strings = est.EMPTY_STRINGS.copy()

    est.add_note_to_tab(strings, None, ("G", "7"))

    assert strings["G"] == "G| -7-"
    assert strings["e"] == "e| ---"
    assert strings["B"] == "B| ---"
    assert strings["D"] == "D| ---"
    assert strings["A"] == "A| ---"
    assert strings["E"] == "E| ---"


# Проверка добавления двух нот на разных струнах
def test_add_note_to_tab_two_notes_different_strings():
    strings = est.EMPTY_STRINGS.copy()

    est.add_note_to_tab(strings, ("A", "6"), ("D", "5"))

    assert strings["A"] == "A| -6-"
    assert strings["D"] == "D| -5-"
    assert strings["e"] == "e| ---"
    assert strings["B"] == "B| ---"
    assert strings["G"] == "G| ---"
    assert strings["E"] == "E| ---"


# Проверка добавления двух нот на одной струне через "+"
def test_add_note_to_tab_two_notes_same_string():
    strings = est.EMPTY_STRINGS.copy()

    est.add_note_to_tab(strings, ("A", "6"), ("A", "8"))

    assert strings["A"] == "A| -6+8-"
    assert strings["e"] == "e| -----"
    assert strings["B"] == "B| -----"
    assert strings["G"] == "G| -----"
    assert strings["D"] == "D| -----"
    assert strings["E"] == "E| -----"


# Проверка ошибки при неподдерживаемом размере такта
def test_export_solo_txt_unsupported_signature():
    with pytest.raises(ValueError, match="signature должен быть 3 или 4"):
        est.export_solo_txt([], 5)


# Проверка полного основного сценария:
# - первая нота
# - повторяющаяся нота с sustain
# - переход в новый такт
# - продолжение ноты из прошлого такта
def test_export_solo_txt_full_flow():
    solo = [
    [12, mc.NOTE_STATES["attack"], 0, 0],
    [12, mc.NOTE_STATES["sustain"], 0, 0],
    [12, mc.NOTE_STATES["sustain"], 0, 0],
    [12, mc.NOTE_STATES["sustain"], 0, 0],
    [12, mc.NOTE_STATES["attack"], 16, mc.NOTE_STATES["attack"]],
    *([[12, mc.NOTE_STATES["sustain"], 16, mc.NOTE_STATES["sustain"]]] * 59),
]

    final_tab = est.export_solo_txt(solo, 3)

    assert isinstance(final_tab, str)
    assert final_tab.startswith("Музыкальный размер: 3/4.")
    assert final_tab.count("Такт ") == 2
    assert "(" in final_tab
    assert ")" in final_tab