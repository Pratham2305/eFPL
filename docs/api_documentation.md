# API Documentation — Personal FPL Assistant

Complete documentation for all REST API endpoints provided by the **Java Spring Boot Core API** (Port `8080`) and the **Python FastAPI ML Service** (Port `8000`).

---

## ☕ Java Spring Boot API (`http://localhost:8080/api`)

The Java backend serves as the main system of record, handling team/squad state, FPL API synchronization, rule validation, and proxying recommendation algorithms to the Python ML service.

---

### 1. Team & Squad Endpoints

#### `GET /api/team/current`
* **Summary**: Get current squad, starting XI, bench, budget, rank, and chips.
* **HTTP Method**: `GET`
* **Response `200 OK`**:
```json
{
  "gameweekId": 2,
  "overallRank": 912695,
  "overallPoints": 162,
  "gameweekPoints": 121,
  "bank": 0,
  "teamValue": 1001,
  "freeTransfers": 1,
  "chips": {
    "wildcard": true,
    "freeHit": true,
    "benchBoost": true,
    "tripleCaptain": true
  },
  "startingXI": [
    {
      "playerId": 1,
      "webName": "Raya",
      "teamShortName": "ARS",
      "position": "GK",
      "squadPosition": 1,
      "multiplier": 1,
      "isCaptain": false,
      "isViceCaptain": false,
      "nowCost": 60,
      "form": 6.00,
      "totalPoints": 12,
      "status": "a",
      "news": ""
    },
    {
      "playerId": 426,
      "webName": "B.Fernandes",
      "teamShortName": "MUN",
      "position": "MID",
      "squadPosition": 6,
      "multiplier": 2,
      "isCaptain": true,
      "isViceCaptain": false,
      "nowCost": 120,
      "form": 12.50,
      "totalPoints": 25,
      "status": "a",
      "news": ""
    }
  ],
  "bench": [ ... ]
}
```

---

#### `POST /api/team/sync`
* **Summary**: Triggers full data sync from the official FPL API (teams, players, gameweeks, fixtures, and user squad picks).
* **HTTP Method**: `POST`
* **Query Parameters**:
  * `teamId` (optional, integer): FPL entry team ID. Defaults to configured team ID or sample team ID 1.
* **Example**: `POST /api/team/sync?teamId=12345`
* **Response `200 OK`**:
```json
{
  "status": "OK",
  "message": "Full sync completed successfully",
  "playersUpdated": 652,
  "fixturesUpdated": 380,
  "gameweeksUpdated": 38
}
```

---

### 2. Fixture Endpoints

#### `GET /api/fixtures`
* **Summary**: Returns Premier League fixtures with Fixture Difficulty Rating (FDR 1-5).
* **HTTP Method**: `GET`
* **Query Parameters**:
  * `gameweekId` (optional, integer): Filter by specific gameweek (e.g. `1`).
  * `teamId` (optional, integer): Filter fixtures for a specific Premier League club (e.g. `2`).
  * `rangeEnd` (optional, integer): End gameweek for multi-GW range (e.g. `rangeEnd=5`).
* **Example**: `GET /api/fixtures?gameweekId=1&rangeEnd=5`
* **Response `200 OK`**:
```json
[
  {
    "id": 1,
    "gameweekId": 1,
    "homeTeamName": "Arsenal",
    "homeTeamShortName": "ARS",
    "awayTeamName": "Wolverhampton Wanderers",
    "awayTeamShortName": "WOL",
    "homeDifficulty": 2,
    "awayDifficulty": 4,
    "kickoffTime": "2026-08-17T14:00:00",
    "finished": true,
    "homeScore": 2,
    "awayScore": 0
  }
]
```

---

### 3. ML Recommendations & Analytics Endpoints

#### `GET /api/recommendations/transfers`
* **Summary**: Ranks transfer-in / transfer-out player pairs by net predicted xPoints gain over a multi-GW horizon.
* **Query Parameters**: `horizon` (default: 3).
* **Response `200 OK`**:
```json
{
  "status": "success",
  "horizon": 3,
  "recommendations": [
    {
      "transfer_out": { "id": 182, "web_name": "Amenda", "now_cost": 40, "xpoints_horizon": 2.5 },
      "transfer_in": { "id": 4, "web_name": "Gabriel", "now_cost": 60, "xpoints_horizon": 14.2 },
      "net_points_gain": 11.7,
      "cost_delta": 2.0,
      "horizon_gws": 3,
      "reasoning": "Replacing Amenda (2.5 xPts) with Gabriel (14.2 xPts) yields +11.7 net points over next 3 GWs."
    }
  ]
}
```

---

#### `GET /api/recommendations/captain`
* **Summary**: Ranks starting XI players for Safe Captain (high floor) vs. Differential Captain (high ceiling).
* **Response `200 OK`**:
```json
{
  "status": "success",
  "captaincy_rankings": {
    "safe": [
      {
        "player_id": 426,
        "web_name": "B.Fernandes",
        "team_short": "MUN",
        "expected_points": 7.45,
        "floor": 3.73,
        "ceiling": 16.41,
        "start_probability": 0.95,
        "ownership_percent": 24.5,
        "safe_score": 5.66
      }
    ],
    "differential": [ ... ]
  }
}
```

---

#### `GET /api/recommendations/chips`
* **Summary**: Forward-looking 8-GW roadmap evaluating optimal deployment windows for Wildcard, Free Hit, Bench Boost, and Triple Captain.
* **Query Parameters**: `horizon` (default: 8).
* **Response `200 OK`**:
```json
{
  "status": "success",
  "chip_strategy": {
    "horizon_gws": 8,
    "chips_available": {
      "wildcard": true,
      "free_hit": true,
      "bench_boost": true,
      "triple_captain": true
    },
    "recommendations": [
      {
        "chip": "Triple Captain",
        "recommended_gw": 34,
        "confidence": 0.95,
        "reasoning": "GW34 offers exceptional return for B.Fernandes (9.2 xPts in a Double Gameweek)."
      }
    ]
  }
}
```

---

#### `GET /api/analysis/weaknesses`
* **Summary**: Returns starting XI players flagged for low expected points (<3.0) or injury/status doubt.
* **Response `200 OK`**:
```json
{
  "status": "success",
  "weaknesses": [
    {
      "player_id": 182,
      "web_name": "Amenda",
      "team_short": "COV",
      "position_name": "DEF",
      "expected_points": 1.25,
      "chance_of_playing": 50,
      "issue": "Injured / Doubtful",
      "now_cost": 40
    }
  ]
}
```

---

#### `GET /api/analysis/differentials`
* **Summary**: Identifies non-owned targets with low ownership (<12%) and high predicted expected points.
* **Query Parameters**: `maxOwnership` (default: 12.0).

---

#### `POST /api/predictions/generate`
* **Summary**: Triggers feature engineering and xPoints prediction model generation.
* **Query Parameters**: `targetGw` (optional, integer).
* **Response `200 OK`**: `{"status": "success", "predictions_saved": 561}`

---

### 4. AI Chat Assistant Endpoint

#### `POST /api/chat`
* **Summary**: Natural language manager Q&A chat grounded in current squad state, predictions, and recommendations.
* **Request Body**:
```json
{
  "query": "Why should I captain Bruno Fernandes over Isak?"
}
```
* **Response `200 OK`**:
```json
{
  "query": "Why should I captain Bruno Fernandes over Isak?",
  "reply": "🤖 **FPL Assistant Analysis**:\nBruno Fernandes is the top safe captain choice with 7.45 xPoints and a high floor of 3.73, playing at Home with an FDR 2 fixture.",
  "grounded": true
}
```

---

## 🐍 Python FastAPI ML Service (`http://localhost:8000`)

The Python ML service handles heavy data ingestion, feature engineering, machine learning predictions, and recommendation algorithms.

| Endpoint | Method | Description |
|---|---|---|
| `/health` | `GET` | Service healthcheck (`{"status": "ok"}`) |
| `/api/v1/ingest/full` | `POST` | Ingestion pipeline fetching `bootstrap-static` & `fixtures` into database |
| `/api/v1/ingest/history` | `POST` | Background worker fetching per-player historical gameweek stats |
| `/api/v1/predict/generate` | `POST` | Runs feature engineering pipeline & xPoints prediction model |
| `/api/v1/evaluation/backtest` | `GET` | Runs model backtest evaluation computing MAE & RMSE |
| `/api/v1/recommendations/transfers` | `GET` | Scored transfer optimization algorithm |
| `/api/v1/recommendations/captains` | `GET` | Safe vs. differential captaincy ranker |
| `/api/v1/recommendations/weaknesses` | `GET` | Squad weakness detector |
| `/api/v1/recommendations/differentials` | `GET` | Low-ownership differential target detector |
| `/api/v1/recommendations/chips` | `GET` | Forward-looking 8-GW chip strategist |
