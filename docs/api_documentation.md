# API Documentation — Personal FPL Assistant

Complete documentation for all REST API endpoints provided by the **Java Spring Boot Core API** (Port `8080`) and the **Python FastAPI ML Service** (Port `8000`).

---

## ☕ Java Spring Boot API (`http://localhost:8080/api`)

The Java backend serves as the main system of record, handling team/squad state, FPL API synchronization, rule validation, player directory browsing, and proxying recommendation algorithms to the Python ML service.

---

### 1. Team & Squad Endpoints

#### `GET /api/team/current`
* **Summary**: Get current squad, starting XI, bench, budget, rank, and chips.
* **HTTP Method**: `GET`
* **Response `200 OK`**:
```json
{
  "gameweekId": 8,
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
  "startingXI": [ ... ],
  "bench": [ ... ]
}
```

---

#### `POST /api/team/sync`
* **Summary**: Triggers full data sync from the official FPL API (teams, players, gameweeks, fixtures, and user squad picks).
* **HTTP Method**: `POST`
* **Query Parameters**:
  * `teamId` (optional, integer): FPL entry team ID. Defaults to configured team ID.
* **Example**: `POST /api/team/sync?teamId=3500002`
* **Response `200 OK`**:
```json
{
  "status": "OK",
  "message": "Full sync completed successfully",
  "playersUpdated": 669,
  "fixturesUpdated": 380,
  "gameweeksUpdated": 38
}
```

---

### 2. Player Directory Endpoints

#### `GET /api/players`
* **Summary**: Search, filter, sort, and paginate through ALL ~700 Premier League players. Includes ML model expected points (`xpNext`), official FPL projection (`epNext`), next opponent with FDR coloring, and squad status flags.
* **HTTP Method**: `GET`
* **Query Parameters**:
  * `search` (optional, string): Case-insensitive search on player names (`webName`, `firstName`, `secondName`) with Scandinavian/European accent normalization (e.g. `odegaard` matches `Ødegaard`).
  * `positions` (optional, comma-separated integers): Element type (1: GKP, 2: DEF, 3: MID, 4: FWD). E.g. `positions=3,4`.
  * `teams` (optional, comma-separated integers): Premier League team IDs. E.g. `teams=1,14`.
  * `minPrice` / `maxPrice` (optional, double): Price filters in millions of pounds (e.g. `minPrice=5.0&maxPrice=10.5`).
  * `status` (optional, string): Availability status filter. `a` (available), `d,i,s,u` (flagged / injured / suspended).
  * `minOwnership` / `maxOwnership` (optional, double): Ownership percentage bounds (0.0 to 100.0).
  * `inSquad` (optional, boolean): `true` to filter players in current user squad, `false` to filter players not in squad.
  * `sortBy` (optional, string): Sort field (`total_points`, `expected_points`, `form`, `now_cost`, `selected_by_percent`, `goals_scored`, `assists`, `clean_sheets`, `minutes`, `ict_index`, `expected_goals`, `expected_assists`, `expected_goal_involvements`, `web_name`). Defaults to `total_points`.
  * `sortDir` (optional, string): `asc` or `desc`. Defaults to `desc`. Missing/null values are always placed last regardless of direction, with deterministic tie-breaking by player ID.
  * `page` (optional, integer): Zero-based page index (defaults to `0`).
  * `size` (optional, integer): Page size (defaults to `25`, max `100`).
* **Response `200 OK`**:
```json
{
  "content": [
    {
      "id": 411,
      "webName": "Haaland",
      "firstName": "Erling",
      "secondName": "Haaland",
      "teamId": 15,
      "teamName": "Man City",
      "teamShortName": "MCI",
      "elementType": 4,
      "positionName": "FWD",
      "nowCost": 157,
      "costChangeStart": 2,
      "costChangeEvent": 0,
      "status": "a",
      "chanceOfPlayingNext": 100,
      "chanceOfPlayingThisRound": 100,
      "news": "",
      "newsAdded": "2026-10-19T13:00:10.543876",
      "form": 9.00,
      "totalPoints": 66,
      "pointsPerGame": 8.20,
      "selectedByPercent": 78.10,
      "minutes": 709,
      "starts": 8,
      "goalsScored": 9,
      "assists": 0,
      "cleanSheets": 3,
      "ictIndex": 78.30,
      "expectedGoals": 6.09,
      "expectedAssists": 0.77,
      "expectedGoalInvolvements": 6.86,
      "epNext": 9.00,
      "xpNext": 7.82,
      "inSquad": true,
      "isStarting": true,
      "isCaptain": false,
      "isViceCaptain": true,
      "nextOpponentShortName": "BHA (H)",
      "nextOpponentDifficulty": 3,
      "isBlankGameweek": false,
      "isDoubleGameweek": false
    }
  ],
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
* **Summary**: Returns complete player inspection detail, including summary stats, next 6 upcoming Premier League fixtures with FDR difficulty, multi-GW cumulative expected points (`xp3`, `xp6`), and recent per-match statistics.
* **HTTP Method**: `GET`
* **Response `200 OK`**:
```json
{
  "summary": { ... },
  "upcomingFixtures": [
    {
      "fixtureId": 88,
      "gameweekId": 9,
      "gameweekName": "Gameweek 9",
      "opponentTeamId": 5,
      "opponentName": "Brighton",
      "opponentShortName": "BHA",
      "difficulty": 3,
      "kickoffTime": "2026-10-31T15:00:00",
      "isHome": true,
      "isBlankGameweek": false,
      "isDoubleGameweek": false
    }
  ],
  "historicalStats": [
    {
      "gameweekId": 8,
      "opponentTeamId": 20,
      "opponentShortName": "SUN",
      "wasHome": false,
      "totalPoints": 13,
      "minutes": 90,
      "goalsScored": 2,
      "assists": 0,
      "cleanSheets": 1,
      "expectedGoals": 1.45,
      "expectedAssists": 0.12
    }
  ],
  "upcomingPredictions": [ ... ],
  "xp3": 21.4,
  "xp6": 42.8
}
```
* **Response `404 Not Found`**: Returns RFC 7807 problem details if player ID does not exist.

---

#### `GET /api/players/meta`
* **Summary**: Returns filter metadata: all 20 Premier League clubs, 4 element positions, min/max price range, min/max ownership range, total active player count, and current/next gameweeks.
* **HTTP Method**: `GET`
* **Response `200 OK`**:
```json
{
  "teams": [
    { "id": 1, "name": "Arsenal", "shortName": "ARS" },
    { "id": 15, "name": "Man City", "shortName": "MCI" }
  ],
  "positions": [
    { "elementType": 1, "shortName": "GKP", "singularName": "Goalkeeper" },
    { "elementType": 2, "shortName": "DEF", "singularName": "Defender" },
    { "elementType": 3, "shortName": "MID", "singularName": "Midfielder" },
    { "elementType": 4, "shortName": "FWD", "singularName": "Forward" }
  ],
  "priceRange": { "min": 3.9, "max": 15.7 },
  "ownershipRange": { "min": 0.0, "max": 78.1 },
  "totalPlayers": 669,
  "currentGameweek": 8,
  "nextGameweek": 9
}
```

---

### 3. Fixture Endpoints

#### `GET /api/fixtures`
* **Summary**: Returns Premier League fixtures with Fixture Difficulty Rating (FDR 1-5).
* **HTTP Method**: `GET`
* **Query Parameters**:
  * `gameweekId` (optional, integer): Filter by specific gameweek (e.g. `1`).
  * `teamId` (optional, integer): Filter fixtures for a specific Premier League club (e.g. `2`).
  * `rangeEnd` (optional, integer): End gameweek for multi-GW range (e.g. `rangeEnd=5`).
* **Example**: `GET /api/fixtures?gameweekId=1&rangeEnd=5`

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
* **Query Parameters**: `maxOwnership` (default: 12.0).

---

#### `POST /api/predictions/generate`
* **Summary**: Triggers feature engineering and xPoints prediction model generation.
* **Query Parameters**: `targetGw` (optional, integer).

---

### 5. AI Chat Assistant Endpoint

#### `POST /api/chat`
* **Summary**: Natural language manager Q&A chat grounded in current squad state, predictions, and recommendations.
* **Request Body**:
```json
{
  "query": "Tell me about Haaland and whether I should consider transferring him into my squad."
}
```

---

## 🐍 Python FastAPI ML Service (`http://localhost:8000`)

The Python ML service handles data ingestion, feature engineering, machine learning predictions, and recommendation algorithms.

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
