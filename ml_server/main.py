import time
import random
from fastapi import FastAPI
from pydantic import BaseModel, Field
from typing import List

app = FastAPI(title="ML Stub Server")

class TabRequest(BaseModel):
    title: str = Field(..., description="Название табулатуры")
    chordProgression: List[List] = Field(
        ..., description="Последовательность аккордов [[chord, duration], ...]"
    )
    signature: str = Field(..., description="Музыкальный размер")
    genreId: int = Field(..., description="ID жанра")

@app.post("/generate")
def generate_tab(req: TabRequest) -> List[List[int]]:
    # имитация долгой генерации
    time.sleep(10)

    # считаем суммарную длительность аккордов
    total_duration = sum(chord[1] for chord in req.chordProgression)

    # парсим signature
    numerator, denominator = map(int, req.signature.split("/"))

    # считаем длину табулатуры
    length = int(total_duration * 48 * numerator / denominator)

    result = []

    for _ in range(length):
        record = [
            random.randint(0, 44),  # нота_1
            random.randint(0, 6),   # эффект_1
            random.randint(0, 44),  # нота_2
            random.randint(0, 6)    # эффект_2
        ]
        result.append(record)

    return result