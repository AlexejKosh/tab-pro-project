import pytest
import os
import sys
import base64
import importlib.util
from pathlib import Path

# Добавление корня проекта в PYTHONPATH для корректных импортов
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), "..")))

from export import export_solo_mp3 as esm
from constants import music_constants as mc
from pydub import AudioSegment


# Вспомогательная функция: создаёт короткий silent-отрезок для тестов аудио
def make_silent(ms = 200):
    return AudioSegment.silent(duration=ms, frame_rate=44100)


# Вспомогательный класс-заглушка для проверки экспорта в base64
class DummyAudio:
    def __init__(self, payload: bytes = b"test-mp3-bytes"):
        self.payload = payload

    def export(self, buffer, format="mp3", bitrate="128k"):
        buffer.write(self.payload)


# Вспомогательная функция: возвращает пример корректного solo_list
def valid_solo_list():
    return [
        [12, mc.NOTE_STATES["attack"], 0, 0],
        [12, mc.NOTE_STATES["sustain"], 0, 0],
        [12, mc.NOTE_STATES["hammer-on"], 16, mc.NOTE_STATES["attack"]],
        [0, 0, 16, mc.NOTE_STATES["sustain"]],
    ]


# Проверка конвертации аудио-объекта в base64-строку
def test_export_to_base64():
    dummy = DummyAudio(b"abc")
    result = esm.export_to_base64(dummy)

    assert result == base64.b64encode(b"abc").decode("utf-8")


# Проверка парсинга имени аккорда на тонику и лад
@pytest.mark.parametrize(
    "chord_name,expected",
    [
        ("Dbm", ("C#", "m")),
        ("C#maj7", ("C#", "maj7")),
        ("A", ("A", "")),
        (" Am7 ", ("A", "m7")),
    ],
)
def test_get_root_note_and_mode_valid(chord_name, expected):
    assert esm.get_root_note_and_mode(chord_name) == expected


# Проверка ошибок при некорректном имени аккорда
@pytest.mark.parametrize(
    "chord_name,match_text",
    [
        ("   ", "Пустое имя аккорда"),
        ("Hm", "Некорректное имя аккорда"),
        ("C#sus9", "Недопустимый тип аккорда"),
    ],
)
def test_get_root_note_and_mode_invalid(chord_name, match_text):
    with pytest.raises(ValueError, match=match_text):
        esm.get_root_note_and_mode(chord_name)


# Проверка преобразования индекса ноты в её имя
@pytest.mark.parametrize(
    "idx,expected",
    [
        (1, "E2"),
        (12, "D#3"),
        (16, "G3"),
        (47, "D6"),
    ],
)
def test_get_note_name_from_index_valid(idx, expected):
    assert esm.get_note_name_from_index(idx) == expected


# Проверка ошибки при выходе индекса ноты за допустимый диапазон
@pytest.mark.parametrize("idx", [0, 48])
def test_get_note_name_from_index_invalid(idx):
    with pytest.raises(ValueError, match="Индекс ноты вне диапазона"):
        esm.get_note_name_from_index(idx)


# Проверка построения набора нот гаммы по тональности
def test_get_scale_pitch_classes():
    assert esm.get_scale_pitch_classes(0) == [0, 2, 4, 5, 7, 9, 11]
    assert esm.get_scale_pitch_classes(5) == [5, 7, 9, 10, 0, 2, 4]


# Проверка веток поиска нижней ноты гаммы
def test_get_lower_scale_index_branches():
    scale = [0, 2, 4, 5, 7, 9, 11]

    assert esm.get_lower_scale_index(10, scale) == 9
    assert esm.get_lower_scale_index(8, scale) == 6
    assert esm.get_lower_scale_index(8, []) == 7
    assert esm.get_lower_scale_index(1, scale) == 1


# Проверка веток поиска верхней ноты гаммы
def test_get_upper_scale_index_branches():
    scale = [0, 2, 4, 5, 7, 9, 11]
    another_scale = [1, 5, 6, 8, 10]

    assert esm.get_upper_scale_index(38, scale) == 40
    assert esm.get_upper_scale_index(34, another_scale) == 35
    assert esm.get_upper_scale_index(47, scale) == 47


# Проверка ошибки при некорректной длительности в строке ритма
def test_incorrect_duration():
    with pytest.raises(ValueError, match="Длительность должна содержать целые числа"):
        esm.validate_rhythm_string("C-f/g")


# Проверка сдвига высоты тона: при нулевом сдвиге должен вернуться тот же объект
def test_pitch_shift_zero_returns_same_object():
    sound = make_silent(100)
    assert esm.pitch_shift(sound, 0) is sound


# Проверка pitch_shift на ненулевом сдвиге и сохранении frame_rate
def test_pitch_shift_nonzero_keeps_frame_rate():
    sound = make_silent(100)
    shifted = esm.pitch_shift(sound, 2)

    assert shifted is not sound
    assert shifted.frame_rate == sound.frame_rate
    assert abs(len(shifted) - (len(sound) / (2.0 ** (2 / 12.0)))) <= 1


# Проверка обработки короткого аудио при fade out
def test_apply_fade_out_short_sound():
    sound = make_silent(20)
    result = esm.apply_fade_out(sound)

    assert len(result) == len(sound)


# Проверка обработки длинного аудио при fade out
def test_apply_fade_out_long_sound():
    sound = make_silent(200)
    result = esm.apply_fade_out(sound)

    assert len(result) == len(sound)


# Проверка ошибки, если не найдена папка rhythm
def test_load_genre_samples_missing_rhythm_dir(tmp_path, monkeypatch):
    genre = "rock"
    project_root = tmp_path
    solo_dir = project_root / "assets" / "audio" / genre / "solo"
    solo_dir.mkdir(parents=True)

    # Мок PROJECT_ROOT с целью направить загрузку в временную тестовую директорию
    monkeypatch.setattr(esm, "PROJECT_ROOT", project_root)

    with pytest.raises(FileNotFoundError, match="Не найдена папка"):
        esm.load_genre_samples(genre)


# Проверка ошибки, если не найдена папка solo
def test_load_genre_samples_missing_solo_dir(tmp_path, monkeypatch):
    genre = "rock"
    project_root = tmp_path
    rhythm_dir = project_root / "assets" / "audio" / genre / "rhythm"
    rhythm_dir.mkdir(parents=True)

    # Мок PROJECT_ROOT с целью проверить ветку отсутствия папки solo
    monkeypatch.setattr(esm, "PROJECT_ROOT", project_root)

    with pytest.raises(FileNotFoundError, match="Не найдена папка"):
        esm.load_genre_samples(genre)


# Проверка успешной загрузки аудио из mp3-файлов
def test_load_genre_samples_success(tmp_path, monkeypatch):
    genre = "rock"
    project_root = tmp_path
    rhythm_dir = project_root / "assets" / "audio" / genre / "rhythm"
    solo_dir = project_root / "assets" / "audio" / genre / "solo"
    rhythm_dir.mkdir(parents=True)
    solo_dir.mkdir(parents=True)

    # Мок PROJECT_ROOT с целью подменить реальную файловую систему на временную
    monkeypatch.setattr(esm, "PROJECT_ROOT", project_root)
    # Мок os.listdir с целью вернуть список тестовых файлов без обращения к диску
    monkeypatch.setattr(os, "listdir", lambda path: ["a.mp3", "b.txt", "c.MP3"])
    # Мок AudioSegment.from_mp3 с целью не читать реальные mp3-файлы
    monkeypatch.setattr(
        esm.AudioSegment,
        "from_mp3",
        lambda path: f"audio:{Path(path).name}",
    )

    rhythm_samples, solo_samples = esm.load_genre_samples(genre)

    assert rhythm_samples == {"a": "audio:a.mp3", "c": "audio:c.MP3"}
    assert solo_samples == {"a": "audio:a.mp3", "c": "audio:c.MP3"}


# Проверка валидации строки ритма на некорректных значениях
@pytest.mark.parametrize(
    "value,match_text",
    [
        (None, "rhythm_string должен быть непустой строкой"),
        ("   ", "rhythm_string должен быть непустой строкой"),
        (" , ", "Список аккордов пуст"),
        ("C1", "Некорректный формат аккорда"),
        ("H-1", "Некорректное имя аккорда"),
        ("C-a", "Некорректная длительность"),
        ("C-1/0", "Длительность должна быть положительной"),
        ("C-0", "Некорректная длительность аккорда"),
    ],
)
def test_validate_rhythm_string_invalid(value, match_text):
    with pytest.raises(ValueError, match=match_text):
        esm.validate_rhythm_string(value)


# Проверка валидации корректной строки ритма
def test_validate_rhythm_string_valid():
    result = esm.validate_rhythm_string("Db-1, Eb-1/2, Am-2")

    assert result == [("Db", 1), ("Eb", 0.5), ("Am", 2)]


# Проверка валидации корректного solo_list
def test_validate_solo_list_valid():
    solo = valid_solo_list()
    esm.validate_solo_list(solo, len(solo))


# Проверка валидации solo_list на разных некорректных входах
@pytest.mark.parametrize(
    "solo_list,rhythm_length,match_text",
    [
        ("not-list", 1, "solo_list должен быть списком"),
        ([[0, 0, 0, 0]], 2, "Длина solo_list должна совпадать с rhythm"),
        ([[0, 0, 0, 0, 0]], 1, "Каждая строка solo_list должна быть списком из 4 чисел"),
        ([[0, 0, 0, "x"]], 1, "Все значения solo_list должны быть целыми числами"),
        ([[48, 0, 0, 0]], 1, "Высота ноты должна быть в диапазоне"),
        ([[0, 7, 0, 0]], 1, "Состояние ноты должно быть в диапазоне"),
        ([[12, 1, 0, 3]], 1, "Если pitch равен 0, state тоже должен быть 0"),
        ([[12, 6, 0, 0]], 1, "Нота не может начинаться со state 6")
    ],
)
def test_validate_solo_list_invalid(monkeypatch, solo_list, rhythm_length, match_text):
    with pytest.raises(ValueError, match=match_text):
        esm.validate_solo_list(solo_list, rhythm_length)


# Проверка ограничения на максимальную длину solo_list
def test_validate_solo_list_length_limit(monkeypatch):
    # Мок GLOBAL_LENGHT_LIMIT с целью искусственно занизить лимит длины
    monkeypatch.setattr(esm, "GLOBAL_LENGHT_LIMIT", 2)
    solo = [[0, 0, 0, 0], [0, 0, 0, 0], [0, 0, 0, 0]]

    with pytest.raises(ValueError, match="Длина solo_list не должна быть больше"):
        esm.validate_solo_list(solo, 3)


# Проверка ошибки, если для аккорда нет аудио в кэше
def test_make_chord_clip_missing_chord():
    with pytest.raises(FileNotFoundError, match="Нет аудио для аккорда"):
        esm.make_chord_clip("C", 1, 4, 120, {})


# Проверка обрезки короткого аудио без лишних преобразований
def test_make_chord_clip_shorter_audio(monkeypatch):
    # Мок apply_fade_out с целью убрать влияние fade out на тест
    monkeypatch.setattr(esm, "apply_fade_out", lambda sound: sound)

    rhythm_samples = {"C": make_silent(500)}
    clip = esm.make_chord_clip("C", 1, 4, 120, rhythm_samples)

    assert len(clip) == 500


# Проверка обрезки длинного аудио до нужной длительности
def test_make_chord_clip_truncates_audio(monkeypatch):
    # Мок apply_fade_out с целью не менять итоговую длину клипа
    monkeypatch.setattr(esm, "apply_fade_out", lambda sound: sound)

    rhythm_samples = {"C": make_silent(3000)}
    clip = esm.make_chord_clip("C", 1, 4, 120, rhythm_samples)

    assert len(clip) == 2100


# Проверка пасстру режима для техник attack и slide
@pytest.mark.parametrize("state", [mc.NOTE_STATES["attack"], mc.NOTE_STATES["slide"]])
def test_build_technique_clip_passthrough_states(state):
    sound = make_silent(200)
    assert esm.build_technique_clip(sound, state, 12, 0) is sound


# Проверка обработки hammer-on
def test_build_technique_clip_hammer_on(monkeypatch):
    sound = make_silent(200)
    # Мок get_lower_scale_index с целью принудительно выбрать тестовый индекс
    monkeypatch.setattr(esm, "get_lower_scale_index", lambda pitch_index, scale: 1)
    # Мок audio_cache с целью подложить готовый тестовый сэмпл
    monkeypatch.setattr(esm, "audio_cache", {"E2": make_silent(120)})

    result = esm.build_technique_clip(sound, mc.NOTE_STATES["hammer-on"], 12, 0)

    assert isinstance(result, AudioSegment)
    assert len(result) > 0


# Проверка обработки pull-off
def test_build_technique_clip_pull_off(monkeypatch):
    sound = make_silent(200)
    # Мок get_upper_scale_index с целью принудительно выбрать тестовый индекс
    monkeypatch.setattr(esm, "get_upper_scale_index", lambda pitch_index, scale: 2)
    # Мок audio_cache с целью подложить готовый тестовый сэмпл
    monkeypatch.setattr(esm, "audio_cache", {"F3": make_silent(120)})

    result = esm.build_technique_clip(sound, mc.NOTE_STATES["pull-off"], 12, 0)

    assert isinstance(result, AudioSegment)
    assert len(result) > 0


# Проверка обработки bend
def test_build_technique_clip_bend(monkeypatch):
    sound = make_silent(200)
    calls = []

    # Мок get_upper_scale_index с целью увести логику в нужную ветку
    monkeypatch.setattr(esm, "get_upper_scale_index", lambda pitch_index, scale: pitch_index + 2)
    # Мок pitch_shift с целью отследить факт вызова без реального изменения аудио
    monkeypatch.setattr(esm, "pitch_shift", lambda chunk, semitones: calls.append(semitones) or chunk)

    result = esm.build_technique_clip(sound, mc.NOTE_STATES["bend"], 12, 0)

    assert isinstance(result, AudioSegment)
    assert len(calls) > 0


# Проверка возврата исходного звука для неизвестного состояния
def test_build_technique_clip_default_return():
    sound = make_silent(200)
    result = esm.build_technique_clip(sound, 0, 12, 0)

    assert result is sound


# Проверка входной валидации export_solo_mp3 по ключевым параметрам
@pytest.mark.parametrize(
    "kwargs,match_text",
    [
        ({"genre": "pop"}, "genre должен быть одним из"),
        ({"signature": 5}, "signature должен быть 3 или 4"),
        ({"bpm": "fast"}, "bpm должен быть целым числом"),
        ({"bpm": 10}, "bpm должен быть целым числом"),
        ({"key": "0"}, "key должен быть числом"),
        ({"key": 99}, "key должен быть числом"),
    ],
)
def test_export_solo_mp3_input_validation(monkeypatch, kwargs, match_text):
    base = dict(
        rhythm_string="C-1",
        expected_length=1,
        solo_list=[[0, 0, 0, 0]],
        signature=4,
        key=0,
        genre="rock",
        bpm=120,
    )
    base.update(kwargs)

    with pytest.raises(ValueError, match=match_text):
        esm.export_solo_mp3(**base)


# Проверка успешного экспорта для жанра rock
def test_export_solo_mp3_success_rock(monkeypatch):
    rhythm_string = "Db-1, Eb-1/2"
    solo_list = valid_solo_list()

    # Мок load_genre_samples с целью не читать реальные аудиофайлы с диска
    monkeypatch.setattr(esm, "load_genre_samples", lambda genre: (
        {"C#": make_silent(500), "D#": make_silent(500)},
        {"D#3": make_silent(500), "G3": make_silent(500)},
    ))
    # Мок make_chord_clip с целью вернуть короткий тестовый аудиофрагмент
    monkeypatch.setattr(esm, "make_chord_clip", lambda *args, **kwargs: make_silent(120))
    # Мок build_technique_clip с целью убрать зависимость от внутренней обработки техник
    monkeypatch.setattr(esm, "build_technique_clip", lambda sound, state, pitch_index, key: sound)
    # Мок export_to_base64 с целью проверить только итоговую связку, а не кодирование MP3
    monkeypatch.setattr(esm, "export_to_base64", lambda audio: "ENCODED")

    result = esm.export_solo_mp3(
        rhythm_string=rhythm_string,
        expected_length=len(solo_list),
        solo_list=solo_list,
        signature=4,
        key=0,
        genre="rock",
        bpm=120,
    )

    assert result == "ENCODED"


# Проверка успешного экспорта для жанра metal
def test_export_solo_mp3_success_metal(monkeypatch):
    rhythm_string = "C-1"
    solo_list = [[12, mc.NOTE_STATES["attack"], 0, 0]]

    # Мок load_genre_samples с целью не обращаться к реальному набору сэмплов
    monkeypatch.setattr(esm, "load_genre_samples", lambda genre: (
        {"C": make_silent(500)},
        {"D#3": make_silent(500)},
    ))
    # Мок make_chord_clip с целью подменить генерацию аккорда тестовым звуком
    monkeypatch.setattr(esm, "make_chord_clip", lambda *args, **kwargs: make_silent(120))
    # Мок build_technique_clip с целью упростить сборку итогового трека
    monkeypatch.setattr(esm, "build_technique_clip", lambda sound, state, pitch_index, key: sound)
    # Мок export_to_base64 с целью проверить итог без реального base64-кодирования
    monkeypatch.setattr(esm, "export_to_base64", lambda audio: "ENCODED")
    # Мок low_pass_filter с целью не усложнять тест фильтрацией
    monkeypatch.setattr(esm.AudioSegment, "low_pass_filter", lambda self, cutoff: self)

    result = esm.export_solo_mp3(
        rhythm_string=rhythm_string,
        expected_length=len(solo_list),
        solo_list=solo_list,
        signature=4,
        key=0,
        genre="metal",
        bpm=120,
    )

    assert result == "ENCODED"


# Проверка ошибки при отсутствии аудио для ноты в кэше
def test_export_solo_mp3_missing_audio_in_cache(monkeypatch):
    rhythm_string = "C-1"
    solo_list = [
        [12, mc.NOTE_STATES["attack"], 0, 0],
        [0, 0, 0, 0],
    ]

    # Мок load_genre_samples с целью оставить кэш нот пустым
    monkeypatch.setattr(esm, "load_genre_samples", lambda genre: (
        {"C": make_silent(500)},
        {},
    ))
    # Мок make_chord_clip с целью не зависеть от формирования аккордов
    monkeypatch.setattr(esm, "make_chord_clip", lambda *args, **kwargs: make_silent(120))
    # Мок build_technique_clip с целью не задействовать логику техник
    monkeypatch.setattr(esm, "build_technique_clip", lambda sound, state, pitch_index, key: sound)

    with pytest.raises(FileNotFoundError, match="Нет аудио для ноты"):
        esm.export_solo_mp3(
            rhythm_string=rhythm_string,
            expected_length=len(solo_list),
            solo_list=solo_list,
            signature=4,
            key=0,
            genre="rock",
            bpm=120,
        )


# Проверка ошибки при отсутствии ffmpeg или ffprobe во время импорта модуля
def test_import_raises_when_ffmpeg_missing(monkeypatch):
    module_path = Path(esm.__file__)

    # Мок Path.exists с целью симулировать отсутствие ffmpeg/ffprobe в системе
    monkeypatch.setattr(Path, "exists", lambda self: False)

    spec = importlib.util.spec_from_file_location("temp_export_solo_mp3_missing_bins", module_path)
    assert spec is not None
    assert spec.loader is not None

    temp_module = importlib.util.module_from_spec(spec)

    with pytest.raises(FileNotFoundError, match="ffmpeg или ffprobe не найдены"):
        spec.loader.exec_module(temp_module)

# Проверка ошибки, когда pitch и state у первой ноты не согласованы
def test_validate_solo_list_invalid_pitch_state_mismatch_first_voice():
    solo_list = [[12, 0, 0, 0]]

    with pytest.raises(ValueError, match="Если pitch равен 0, state тоже должен быть 0, и наоборот"):
        esm.validate_solo_list(solo_list, 1)


# Проверка ошибки, когда вторая нота начинается со state = 6
def test_validate_solo_list_invalid_state_6_second_voice():
    solo_list = [[12, 1, 16, 6]]

    with pytest.raises(ValueError, match="Нота не может начинаться со state 6"):
        esm.validate_solo_list(solo_list, 1)


# Проверка ветки hammer-on, когда готового сэмпла для нижней ноты нет
def test_build_technique_clip_hammer_on_fallback_to_pitch_shift(monkeypatch):
    sound = make_silent(200)
    calls = []

    # Мок get_lower_scale_index с целью вывести логику в ветку без аудио в кэше
    monkeypatch.setattr(esm, "get_lower_scale_index", lambda pitch_index, scale: 1)
    # Мок audio_cache с целью симулировать отсутствие готового сэмпла
    monkeypatch.setattr(esm, "audio_cache", {})
    # Мок pitch_shift с целью проверить, что именно он вызывается в fallback-ветке
    monkeypatch.setattr(esm, "pitch_shift", lambda chunk, semitones: calls.append(semitones) or chunk)

    result = esm.build_technique_clip(sound, mc.NOTE_STATES["hammer-on"], 12, 0)

    assert isinstance(result, AudioSegment)
    assert calls == [-11]


# Проверка ветки pull-off, когда готовый сэмпл для верхней ноты есть в кэше
def test_build_technique_clip_pull_off_uses_cached_sample(monkeypatch):
    sound = make_silent(200)
    calls = []

    # Мок get_upper_scale_index с целью выбрать конкретную верхнюю ноту
    monkeypatch.setattr(esm, "get_upper_scale_index", lambda pitch_index, scale: 2)
    # Мок audio_cache с целью подложить готовый сэмпл для верхней ноты
    monkeypatch.setattr(esm, "audio_cache", {"F2": make_silent(120)})
    # Мок pitch_shift с целью убедиться, что fallback-ветка не используется
    monkeypatch.setattr(esm, "pitch_shift", lambda chunk, semitones: calls.append(semitones) or chunk)

    result = esm.build_technique_clip(sound, mc.NOTE_STATES["pull-off"], 12, 0)

    assert isinstance(result, AudioSegment)
    assert len(result) > 0
    assert calls == []


# Проверка ветки bend, когда остаток звука слишком короткий и цикл даже не запускается
def test_build_technique_clip_bend_returns_when_rest_empty(monkeypatch):
    sound = make_silent(50)
    calls = []

    # Мок get_upper_scale_index с целью стабилизировать расчёт бенда
    monkeypatch.setattr(esm, "get_upper_scale_index", lambda pitch_index, scale: 14)
    # Мок pitch_shift с целью проверить, что при пустом остатке он не вызывается
    monkeypatch.setattr(esm, "pitch_shift", lambda chunk, semitones: calls.append(semitones) or chunk)

    result = esm.build_technique_clip(sound, mc.NOTE_STATES["bend"], 12, 0)

    assert len(result) == len(sound)
    assert calls == []


# Проверка ветки bend, когда остаток звука короче числа сегментов и срабатывает break
def test_build_technique_clip_bend_breaks_when_rest_too_short(monkeypatch):
    sound = make_silent(83)
    calls = []

    # Мок get_upper_scale_index с целью стабилизировать расчёт бенда
    monkeypatch.setattr(esm, "get_upper_scale_index", lambda pitch_index, scale: 14)
    # Мок pitch_shift с целью посчитать количество сегментов, в которые реально пошёл bend
    monkeypatch.setattr(esm, "pitch_shift", lambda chunk, semitones: calls.append(semitones) or chunk)

    result = esm.build_technique_clip(sound, mc.NOTE_STATES["bend"], 12, 0)

    assert isinstance(result, AudioSegment)
    assert len(calls) == 3