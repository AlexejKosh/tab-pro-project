import pytest
from fastapi.testclient import TestClient

import main


# Fixture FastAPI TestClient:
# создаёт тестовый клиент для отправки HTTP-запросов
# в приложение без запуска реального сервера
@pytest.fixture
def client():
    return TestClient(main.app)


# Базовый корректный запрос к /generate
def base_request():
    return {
        "genreId": 1,
        "signature": "4/4",
        "musicKey": 0,
        "bpm": 120,
        "chordProgression": "C-1",
        "ip": "127.0.0.1"
    }


# Проверка успешной генерации:
# при корректном запросе должно возвращаться 200
def test_generate_success_monkeypatched(client, monkeypatch):

    # Мок ключевых функций:
    # ритм, соло, текст, mp3
    monkeypatch.setattr(main, "encode_rhythm", lambda cp, sig: [[0, 0, 0]] * 4)
    monkeypatch.setattr(main, "generate_solo", lambda chords, key, genre, temperature=0.7: [[0, 0, 0, 0]] * 4)
    monkeypatch.setattr(main, "export_solo_txt", lambda solo, sig: "TAB")
    monkeypatch.setattr(main, "export_solo_mp3", lambda r, len_r, s, sig, k, g, b: "BASE64")

    resp = client.post("/generate", json=base_request())
    data = resp.json()

    assert resp.status_code == 200
    assert data["tabData"] == "TAB"
    assert data["audioData"] == "BASE64"


# Проверка невалидного genreId:
# genreId должен быть от 1 до 3, иначе 400
def test_generate_invalid_genreId(client):
    req = base_request()
    req["genreId"] = 99

    resp = client.post("/generate", json=req)

    assert resp.status_code == 400
    assert resp.json()["detail"] == "Неверный жанр"


# Проверка невалидного размера такта:
# signature должен быть "3/4" или "4/4", иначе 400
def test_generate_invalid_signature(client):
    req = base_request()
    req["signature"] = "5/4"

    resp = client.post("/generate", json=req)

    assert resp.status_code == 400
    assert resp.json()["detail"] == "Неверное значение музыкального размера"


# Проверка некорректного BPM:
# BPM должен быть целым числом от 50 до 200, иначе 400
def test_generate_invalid_bpm(client):
    req = base_request()
    req["bpm"] = 10

    resp = client.post("/generate", json=req)

    assert resp.status_code == 400
    assert resp.json()["detail"] == "BPM должен быть целым от 50 до 200"

# Проверка некорректной музыкальной тональности:
# значение musicKey должно быть от 0 до 11, иначе 400
def test_generate_invalid_musicKey(client):
    req = base_request()
    req["musicKey"] = 99

    resp = client.post("/generate", json=req)

    assert resp.status_code == 400
    assert resp.json()["detail"] == "Неверное значение тональности"


# Проверка обработки ошибки encode_rhythm:
# сбой кодирования ритма должен возвращать 400
def test_encode_rhythm_raises_returns_400(client, monkeypatch):

    # Мок encode_rhythm для генерации исключения при обработке ритма
    monkeypatch.setattr(
        main,
        "encode_rhythm",
        lambda cp, sig: (_ for _ in ()).throw(ValueError("bad rhythm"))
    )

    resp = client.post("/generate", json=base_request())

    assert resp.status_code == 400
    assert "Некорректная последовательность аккордов" in resp.json()["detail"]

# Проверка ошибки генерации ML модели:
# сбой генерации должен возвращать 500
def test_generate_solo_raises_returns_500(client, monkeypatch):

    # Мок generate_solo для генерации исключения при генерации соло
    monkeypatch.setattr(main, "encode_rhythm", lambda cp, sig: [[0]] * 4)
    monkeypatch.setattr(
        main,
        "generate_solo",
        lambda chords, key, genre, temperature=0.7: (_ for _ in ()).throw(Exception("model"))
    )

    resp = client.post("/generate", json=base_request())

    assert resp.status_code == 500
    assert "Ошибка генерации соло" in resp.json()["detail"]


# Проверка ошибки экспорта соло в txt:
# сбой текстового экспорта должен возвращать 500
def test_export_solo_txt_raises_returns_500(client, monkeypatch):

    # Мок export_solo_txt для генерации исключения при экспорте табулатуры
    monkeypatch.setattr(main, "encode_rhythm", lambda cp, sig: [[0]] * 4)
    monkeypatch.setattr(main, "generate_solo", lambda chords, key, genre, temperature=0.7: [[0, 0, 0, 0]] * 4)
    monkeypatch.setattr(
        main,
        "export_solo_txt",
        lambda solo, sig: (_ for _ in ()).throw(Exception("txt error"))
    )

    resp = client.post("/generate", json=base_request())

    assert resp.status_code == 500
    assert "Ошибка экспорта табулатуры" in resp.json()["detail"]


# Проверка ошибки экспорта соло в mp3:
# сбой аудио-экспорта должен возвращать 500
def test_export_solo_mp3_raises_returns_500(client, monkeypatch):

    # Мок export_solo_mp3 для генерации исключения при экспорте аудио
    monkeypatch.setattr(main, "encode_rhythm", lambda cp, sig: [[0]] * 4)
    monkeypatch.setattr(main, "generate_solo", lambda chords, key, genre, temperature=0.7: [[0, 0, 0, 0]] * 4)
    monkeypatch.setattr(main, "export_solo_txt", lambda solo, sig: "TAB")
    monkeypatch.setattr(
        main,
        "export_solo_mp3",
        lambda r, s, sig, k, g, b: (_ for _ in ()).throw(Exception("mp3 error"))
    )

    resp = client.post("/generate", json=base_request())

    assert resp.status_code == 500
    assert "Ошибка экспорта аудио" in resp.json()["detail"]


def test_lifespan_loads_models(monkeypatch):
    info_calls = []

    def fake_info(msg):
        info_calls.append(msg)

    # Подмена logger.info и load_models до создания TestClient
    monkeypatch.setattr(main.logger, "info", fake_info)

    loaded = {"called": False}

    def fake_load_models():
        loaded["called"] = True

    monkeypatch.setattr(main, "load_models", fake_load_models)

    # Создаём TestClient чтобы пройти lifespan
    from fastapi.testclient import TestClient

    with TestClient(main.app):
        pass

    assert loaded["called"] is True
    assert any("Загрузка моделей" in m for m in info_calls)
    assert any("Модели успешно загружены" in m for m in info_calls)

    idx_load = next(i for i, m in enumerate(info_calls) if "Загрузка моделей" in m)
    idx_ok = next(i for i, m in enumerate(info_calls) if "Модели успешно загружены" in m)

    assert idx_load < idx_ok