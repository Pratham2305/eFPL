# API Documentation — Personal FPL Assistant

Complete documentation for all REST API endpoints provided by the **Java Spring Boot Core API** (Port `8080`) and the **Python FastAPI ML Service** (Port `8000`).

---

## ☕ Java Spring Boot API (`http://localhost:8080/api`)

The Java backend serves as the main system of record, handling team/squad state, FPL API synchronization, rule validation, player directory browsing, season performance history, and proxying recommendation algorithms to the Python ML service.

---

### 1. Team, Gameweek & Squad Endpoints

#### `GET /api/gameweeks`
* **Summary**: Returns all 38 Premier League gameweeks with deadline times, average entry scores, highest scores, real-time lifecycle phase (`FINISHED`, `LIVE`, `UPCOMING`, `FUTURE`), and `isDefaultHome` indicator.
* **HTTP Method**: `GET`
* **Response `200 OK`**:
```json
[
  {
    "id": 8,
    "name": "Gameweek 8",
    "deadlineTime": "2024-10-19T10:00:00",
    "averageEntryScore": 45,
    "highestScore": 128,
    "isCurrent": true,
    "isNext": false,
    "isPrevious": false,
    "finished": true,
    "dataChecked": true,
    "phase": "FINISHED",
    "isDefaultHome": false
  },
  {
    "id": 9,
    "name": "Gameweek 9",
    "deadlineTime": "2024-10-25T17:30:00",
    "averageEntryScore": null,
    "highestScore": null,
    "isCurrent": false,
    "isNext": true,
    "isPrevious": false,
    "finished": false,
    "dataChecked": false,
    "phase": "UPCOMING",
    "isDefaultHome": true
  }
]
```

---

#### `GET /api/team/current`
* **Summary**: Get squad for default home gameweek (or specified query parameter `gameweekId`), with starting XI, bench, budget, rank, chips, raw and effective points, auto-subs, and points reconciliation.
* **HTTP Method**: `GET`
* **Query Parameters**:
  * `gameweekId` (optional, integer): Gameweek ID to inspect. Defaults to the calculated default home gameweek.
* **Response `200 OK`**:
```json
{
  "gameweekId": 8,
  "currentGameweekId": 8,
  "nextGameweekId": 9,
  "phase": "FINISHED",
  "isProjected": false,
  "overallRank": 2228443,
  "overallPoints": 501,
  "gameweekPoints": 46,
  "gameweekNetPoints": 46,
  "transfersCost": 0,
  "pointsOnBench": 3,
  "liveTotalPoints": 46,
  "reconciledGameweekPoints": 46,
  "pointsDiscrepancy": false,
  "averageEntryScore": 45,
  "highestScore": 128,
  "bank": 14,
  "teamValue": 1015,
  "freeTransfers": 1,
  "chips": {
    "wildcard": true,
    "freeHit": true,
    "benchBoost": true,
    "tripleCaptain": false
  },
  "startingXI": [
    {
      "playerId": 411,
      "webName": "Haaland",
      "teamShortName": "MCI",
      "teamCode": 43,
      "position": "FWD",
      "squadPosition": 1,
      "multiplier": 2,
      "isCaptain": true,
      "isViceCaptain": false,
      "nowCost": 154,
      "form": 8.5,
      "totalPoints": 75,
      "rawPoints": 2,
      "effectivePoints": 4,
      "minutes": 90,
      "goals": 0,
      "assists": 0,
      "cleanSheets": 0,
      "bonus": 0,
      "bps": 4,
      "isAutoSubIn": false,
      "isAutoSubOut": false
    }
  ],
  "bench": [ ... ]
}
```

---

#### `GET /api/team/gameweek/{gw}`
* **Summary**: Direct path-variable retrieval of squad, actuals, or projected lineup for any gameweek `gw`.
* **HTTP Method**: `GET`
* **Response `200 OK`**: Same schema as `GET /api/team/current`.

---

#### `GET /api/team/history`
* **Summary**: Complete season performance history containing executive KPI summary cards, captaincy efficiency metrics, interactive chart feeds, per-gameweek sortable log, chips timeline, and deterministic narrative insights.
* **HTTP Method**: `GET`
* **Response `200 OK`**:
```json
{
  "summary": {
    "totalPoints": 501,
    "currentOverallRank": 2228443,
    "bestGameweekRank": 1245000,
    "bestGameweekRankEvent": 3,
    "averageGameweekScore": 62.6,
    "averageLeagueScore": 54.2,
    "scoreVersusLeagueAvg": 8.4,
    "totalTransfers": 6,
    "totalTransferCost": 0,
    "totalBenchPoints": 38,
    "teamValue": 101.5,
    "bank": 1.4,
    "captainEfficiency": {
      "totalCaptainPoints": 118,
      "averageCaptainPoints": 14.8,
      "optimalCaptainHits": 6,
      "totalGameweeks": 8
    }
  },
  "gameweeks": [
    {
      "gameweekId": 8,
      "gameweekName": "GW 8",
      "points": 46,
      "netPoints": 46,
      "averageScore": 45,
      "highestScore": 128,
      "rank": 5259632,
      "overallRank": 2228443,
      "overallPoints": 501,
      "transfers": 1,
      "transferCost": 0,
      "pointsOnBench": 3,
      "bank": 1.4,
      "teamValue": 101.5,
      "chipUsed": null,
      "captainName": "Haaland",
      "captainPoints": 4
    }
  ],
  "deterministicInsights": [
    "🌟 Best Gameweek: GW 3 with rank 1,245,000.",
    "📊 Average Score: 62.6 pts/GW vs league average of 54.2 (+8.4 pts/GW difference).",
    "👑 Captaincy Impact: Captain picks earned 118 points (avg 14.8 pts/GW), scoring as the highest starter in 6 of 8 gameweeks.",
    "🪑 Bench Management: 38 total points left on bench, with a season high of 12 points in GW 5.",
    "🔄 Transfer Discipline: 6 transfers made with zero points lost to transfer hits."
  ],
  "chipsTimeline": [
    {
      "gameweekId": 3,
      "chip": "3xc",
      "chipName": "Triple Captain",
      "gameweekPoints": 88
    }
  ]
}
```

---

#### `POST /api/team/sync`
* **Summary**: Triggers idempotent full data sync from the official FPL API: teams, players, gameweeks, fixtures, entry history, per-GW picks, live player stats, auto-subs, and transfer logs.
* **HTTP Method**: `POST`
* **Query Parameters**:
  * `teamId` (optional, integer): FPL entry team ID. Defaults to configured team ID.
  * `forceHistory` (optional, boolean, default: `false`): If `true`, re-downloads and overwrites settled historical gameweeks.
* **Example**: `POST /api/team/sync?teamId=3500002&forceHistory=true`
* **Response `200 OK`**:
```json
{
  "status": "OK",
  "message": "Full sync completed successfully",
  "playersUpdated": 669,
  "fixturesUpdated": 380,
  "gameweeksUpdated": 38,
  "historyGameweeksUpdated": 8,
  "picksUpdated": 120,
  "statsUpdated": 4200,
  "transfersUpdated": 6
}
```

---

### 2. Player Directory Endpoints

#### `GET /api/players`
* **Summary**: Search, filter, sort, and paginate through ALL ~700 Premier League players. Includes ML model expected points (`xpNext`), official FPL projection (`epNext`), next opponent with FDR coloring, and squad status flags.
* **HTTP Method**: `GET`
* **Query Parameters**:
  * `search` (optional, string): Case-insensitive search on player names (`webName`, `firstName`, `secondName`) with Scandinavian/European accent normalization.
  * `positions` (optional, comma-separated integers): Element type (1: GKP, 2: DEF, 3: MID, 4: FWD). E.g. `positions=3,4`.
  * `teams` (optional, comma-separated integers): Premier League team IDs. E.g. `teams=1,14`.
  * `minPrice` / `maxPrice` (optional, double): Price filters in millions of pounds (e.g. `minPrice=5.0&maxPrice=10.5`).
  * `status` (optional, string): Availability status filter. `a` (available), `d,i,s,u` (flagged / injured / suspended).
  * `minOwnership` / `maxOwnership` (optional, double): Ownership percentage bounds (0.0 to 100.0).
  * `inSquad` (optional, boolean): `true` to filter players in current user squad, `false` to filter players not in squad.
  * `sortBy` (optional, string): Sort field (`total_points`, `expected_points`, `form`, `now_cost`, `selected_by_percent`, `goals_scored`, `assists`, `clean_sheets`, `minutes`, `ict_index`, `expected_goals`, `expected_assists`, `expected_goal_involvements`, `web_name`). Defaults to `total_points`.
  * `sortDir` (optional, string): `asc` or `desc`. Defaults to `desc`.
  * `page` (optional, integer): Zero-based page index (defaults to `0`).
  * `size` (optional, integer): Page size (defaults to `25`, max `100`).
* **Response `200 OK`**:
```json
{
  "content": [ ... ],
  "page": 0,
  "size": 25,
  "totalElements": 669,
  "totalPages": 27,
  "hasNext": true,
  "hasPrevious": false
}
```

---

#### `GET /api/players/{id}`
* **Summary**: Complete player inspection detail, including summary stats, next 6 upcoming fixtures with FDR, multi-GW cumulative expected points (`xp3`, `xp6`), and recent per-match statistics.
* **HTTP Method**: `GET`
* **Response `200 OK`**: Full player details.

---

#### `GET /api/players/meta`
* **Summary**: Returns filter metadata: all 20 Premier League clubs, 4 element positions, min/max price range, min/max ownership range, total active player count, and current/next gameweeks.
* **HTTP Method**: `GET`

---

### 3. Fixture Endpoints

#### `GET /api/fixtures`
* **Summary**: Returns Premier League fixtures with Fixture Difficulty Rating (FDR 1-5).
* **HTTP Method**: `GET`
* **Query Parameters**:
  * `gameweekId` (optional, integer): Filter by specific gameweek.
  * `teamId` (optional, integer): Filter fixtures for a specific Premier League club.
  * `rangeEnd` (optional, integer): End gameweek for multi-GW range.

---

### 4. ML Recommendations & Analytics Endpoints

#### `GET /api/recommendations/transfers`
* **Summary**: Ranks transfer-in / transfer-out player pairs by net predicted xPoints gain over a multi-GW horizon.
* **Query Parameters**: `horizon` (default: 3).

---

#### `GET /api/recommendations/captain`
* **Summary**: Ranks starting XI players for Safe Captain (high floor) vs. Differential Captain (high ceiling).

---

#### `GET /api/recommendations/chips`
* **Summary**: Forward-looking 8-GW roadmap evaluating optimal deployment windows for Wildcard, Free Hit, Bench Boost, and Triple Captain.
* **Query Parameters**: `horizon` (default: 8).

---

#### `GET /api/analysis/weaknesses`
* **Summary**: Returns starting XI players flagged for low expected points (<3.0) or injury/status doubt.

---

#### `GET /api/analysis/differentials`
* **Summary**: Identifies non-owned targets with low ownership (<12%) and high predicted expected points.

---

#### `POST /api/predictions/generate`
* **Summary**: Triggers feature engineering and xPoints prediction model generation.

---

### 5. AI Chat Assistant Endpoint

#### `POST /api/chat`
* **Summary**: Natural language manager Q&A chat grounded in current squad state, predictions, and recommendations.

---

## 🐍 Python FastAPI ML Service (`http://localhost:8000`)

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
