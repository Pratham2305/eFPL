"""Async HTTP client for the official FPL API."""

import logging
from typing import Any

import httpx

from app.core.config import settings

logger = logging.getLogger(__name__)

BASE_URL = settings.fpl_api_base_url

# The FPL API requires a user-agent header
HEADERS = {
    "User-Agent": "FPL-Assistant/0.1"
}


async def fetch_bootstrap_static() -> dict[str, Any]:
    """GET /api/bootstrap-static/ — all players, teams, gameweeks."""
    url = f"{BASE_URL}/bootstrap-static/"
    logger.info("Fetching bootstrap-static from %s", url)
    async with httpx.AsyncClient(headers=HEADERS, timeout=30.0) as client:
        resp = await client.get(url)
        resp.raise_for_status()
        return resp.json()


async def fetch_fixtures() -> list[dict[str, Any]]:
    """GET /api/fixtures/ — all fixtures."""
    url = f"{BASE_URL}/fixtures/"
    logger.info("Fetching fixtures from %s", url)
    async with httpx.AsyncClient(headers=HEADERS, timeout=30.0) as client:
        resp = await client.get(url)
        resp.raise_for_status()
        return resp.json()


async def fetch_element_summary(player_id: int) -> dict[str, Any]:
    """GET /api/element-summary/{player_id}/ — per-player history."""
    url = f"{BASE_URL}/element-summary/{player_id}/"
    async with httpx.AsyncClient(headers=HEADERS, timeout=30.0) as client:
        resp = await client.get(url)
        resp.raise_for_status()
        return resp.json()


async def fetch_entry(team_id: int) -> dict[str, Any]:
    """GET /api/entry/{team_id}/ — your team overview."""
    url = f"{BASE_URL}/entry/{team_id}/"
    logger.info("Fetching entry for team %d from %s", team_id, url)
    async with httpx.AsyncClient(headers=HEADERS, timeout=30.0) as client:
        resp = await client.get(url)
        resp.raise_for_status()
        return resp.json()


async def fetch_picks(team_id: int, gameweek: int) -> dict[str, Any]:
    """GET /api/entry/{team_id}/event/{gw}/picks/ — your picks for a GW."""
    url = f"{BASE_URL}/entry/{team_id}/event/{gameweek}/picks/"
    logger.info("Fetching picks for team %d GW %d from %s", team_id, gameweek, url)
    async with httpx.AsyncClient(headers=HEADERS, timeout=30.0) as client:
        resp = await client.get(url)
        resp.raise_for_status()
        return resp.json()
