"""
FitAI – Backend proxy para Gemini API (opcional / uso futuro)
=============================================================
NOTA: La app Android llama directamente a Gemini API desde GeminiService.kt.
Este servidor es un proxy de referencia pensado para uso futuro (nube, multi-usuario).

Arrancar el servidor:
    pip install -r requirements.txt
    cp .env.example .env          # rellenar GEMINI_API_KEY
    uvicorn main:app --reload --port 8000
"""

import os
import httpx
from fastapi import FastAPI, HTTPException, Request
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import StreamingResponse
from pydantic import BaseModel
from dotenv import load_dotenv

load_dotenv()

GEMINI_API_KEY = os.getenv("GEMINI_API_KEY", "")
GEMINI_BASE_URL = (
    "https://generativelanguage.googleapis.com/v1beta/models"
    "/gemini-2.5-flash:streamGenerateContent"
)

app = FastAPI(
    title="FitAI Backend",
    description="Proxy opcional para la API de Google Gemini usada por FitAI.",
    version="1.0.0",
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)


# ── Modelos ──────────────────────────────────────────────────────────────────

class GeminiRequest(BaseModel):
    """Cuerpo de la petición que reenvía la app Android."""
    systemInstruction: dict | None = None
    contents: list[dict]
    generationConfig: dict | None = None


# ── Endpoints ────────────────────────────────────────────────────────────────

@app.get("/health")
async def health_check():
    """Comprueba que el servidor está en marcha."""
    return {"status": "ok", "service": "FitAI Backend"}


@app.post("/gemini/stream")
async def proxy_gemini_stream(body: GeminiRequest):
    """
    Reenvía la petición a Gemini API con streaming SSE y devuelve
    los chunks al cliente (app Android) en tiempo real.
    """
    if not GEMINI_API_KEY:
        raise HTTPException(status_code=500, detail="GEMINI_API_KEY no configurada.")

    url = f"{GEMINI_BASE_URL}?key={GEMINI_API_KEY}&alt=sse"

    async def stream_response():
        async with httpx.AsyncClient(timeout=60.0) as client:
            async with client.stream(
                "POST",
                url,
                json=body.model_dump(exclude_none=True),
                headers={"Content-Type": "application/json"},
            ) as response:
                if response.status_code != 200:
                    error = await response.aread()
                    yield f"data: {{\"error\": \"{error.decode()}\"}}\n\n"
                    return
                async for chunk in response.aiter_bytes():
                    yield chunk

    return StreamingResponse(stream_response(), media_type="text/event-stream")


@app.post("/gemini/generate")
async def proxy_gemini_generate(body: GeminiRequest):
    """
    Reenvía la petición a Gemini API sin streaming y devuelve
    la respuesta completa en JSON (útil para testing o clientes simples).
    """
    if not GEMINI_API_KEY:
        raise HTTPException(status_code=500, detail="GEMINI_API_KEY no configurada.")

    # Endpoint sin SSE
    url_no_stream = (
        "https://generativelanguage.googleapis.com/v1beta/models"
        f"/gemini-2.5-flash:generateContent?key={GEMINI_API_KEY}"
    )

    async with httpx.AsyncClient(timeout=60.0) as client:
        response = await client.post(
            url_no_stream,
            json=body.model_dump(exclude_none=True),
            headers={"Content-Type": "application/json"},
        )

    if response.status_code != 200:
        raise HTTPException(status_code=response.status_code, detail=response.text)

    return response.json()

