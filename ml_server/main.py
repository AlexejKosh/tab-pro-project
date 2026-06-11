import logging

from fastapi import FastAPI, HTTPException
from pydantic import BaseModel, Field
from contextlib import asynccontextmanager

from preprocessing.encode_rhythm import encode_rhythm
from core.generate_solo import generate_solo, load_models
from export.export_solo_txt import export_solo_txt
from export.export_solo_mp3 import export_solo_mp3
from constants.music_constants import GENRES, ALLOWED_SIGNATURES

# Настройка логирования
logging.basicConfig(
    level=logging.INFO,
    format="[%(asctime)s] %(levelname)s | %(message)s"
)

logger = logging.getLogger(__name__)

@asynccontextmanager
async def lifespan(app: FastAPI):

    logger.info("Загрузка моделей")
    load_models()
    logger.info("Модели успешно загружены")
    yield

app = FastAPI(
    title="ML Server",
    lifespan=lifespan
)

class GenerateTabRequest(BaseModel):
    # ID жанра:
    # 1 - blues
    # 2 - metal
    # 3 - rock
    genreId: int = Field(..., description="ID жанра (1..3)")

    # Музыкальный размер
    signature: str = Field(..., description="Музыкальный размер (3/4, 4/4)")

    # Индекс тональности
    musicKey: int = Field(..., description="Тональность (0..11)")

    # Темп композиции
    bpm: int = Field(..., description="Темп (50..200)")

    # Последовательность аккордов
    chordProgression: str = Field(..., description="Последовательность аккордов")

    ip: str = Field(..., description="IP-адресс клиента")


class MlServerGenerateResponse(BaseModel):
    tabData: str
    audioData: str


@app.post("/generate", response_model=MlServerGenerateResponse)
def generate_tab(req: GenerateTabRequest):

    logger.info(
        f"[{req.ip}] Запрос на генерацию | "
        f"genreId={req.genreId}, bpm={req.bpm}, key={req.musicKey}, signature={req.signature}"
    )

    # Проверка genreId
    try:
        genre = GENRES[req.genreId - 1]
    except Exception:
        logger.warning(f"[{req.ip}] Некорректный genreId: {req.genreId}")
        raise HTTPException(status_code=400, detail="Неверный жанр")

    # Проверка signature
    if not isinstance(req.signature, str) or req.signature not in [f"{i}/4" for i in ALLOWED_SIGNATURES]:
        logger.warning(f"[{req.ip}] Некорректный signature: {req.signature}")
        raise HTTPException(status_code=400, detail="Неверное значение музыкального размера")

    # Проверка bpm
    if not isinstance(req.bpm, int) or not 50 <= req.bpm <= 200:
        logger.warning(f"[{req.ip}] Некорректный bpm: {req.bpm}")
        raise HTTPException(status_code=400, detail="BPM должен быть целым от 50 до 200")

    # Проверка musicKey
    if not isinstance(req.musicKey, int) or not 0 <= req.musicKey <= 11:
        logger.warning(f"[{req.ip}] Некорректный musicKey: {req.musicKey}")
        raise HTTPException(status_code=400, detail="Неверное значение тональности")

    signature = int(req.signature[0])

    # Кодирование ритма в формат для модели
    try:
        logger.info(f"[{req.ip}] Кодирование ритма")
        chords = encode_rhythm(req.chordProgression, signature)
    except Exception as e:
        logger.warning(f"[{req.ip}] Ошибка encode_rhythm: {e}")
        raise HTTPException(status_code=400, detail=f"Некорректная последовательность аккордов: {e}")

    # Генерация соло
    try:
        logger.info(f"[{req.ip}] Генерация соло")
        solo = generate_solo(chords, req.musicKey, genre)
    except Exception as e:
        logger.error(f"[{req.ip}] Ошибка generate_solo: {e}")
        raise HTTPException(status_code=500, detail=f"Ошибка генерации соло: {e}")

    # Экспорт табулатуры в текст
    try:
        logger.info(f"[{req.ip}] Экспорт табулатуры")
        tab_text = export_solo_txt(solo, signature)
    except Exception as e:
        logger.error(f"[{req.ip}] Ошибка export_solo_txt: {e}")
        raise HTTPException(status_code=500, detail=f"Ошибка экспорта табулатуры: {e}")

    # Генерация аудио и конвертация в base64
    try:
        logger.info(f"[{req.ip}] Генерация mp3")
        audio_b64 = export_solo_mp3(
            req.chordProgression,
            len(chords),
            solo,
            signature,
            req.musicKey,
            genre,
            req.bpm
        )
    except Exception as e:
        logger.error(f"[{req.ip}] Ошибка export_solo_mp3: {e}")
        raise HTTPException(status_code=500, detail=f"Ошибка экспорта аудио: {e}")

    logger.info(f"[{req.ip}] Генерация успешно завершена")

    return MlServerGenerateResponse(
        tabData=tab_text,
        audioData=audio_b64
    )