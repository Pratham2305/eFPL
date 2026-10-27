# Personal FPL Assistant — Detailed Project Plan

A personalized, data-driven Fantasy Premier League decision-support app. Java (Spring Boot) owns the product/API layer, Python owns data/ML, a local LLM turns model output into plain-English advice. Zero-cost, local-first, resume-worthy.

---

## 1. Guiding Principles (from your brief)

- **Numbers come from data/ML, not the LLM.** The LLM explains and converses; it never invents an expected-points number.
- **No recommendation in isolation.** Every suggestion is squad-aware, budget-aware, fixture-aware.
- **MVP over impressiveness.** Every technology in the stack has a job. Don't add Kafka because it looks good on a resume.
- **Zero cost.** Free FPL API, local Postgres, local LLM (Ollama), everything in Docker Compose on your machine.

---

## 2. High-Level Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│  React + TS + Vite + Tailwind (Frontend)                        │
│  Squad view · Fixtures · Transfers · Captaincy · Chat            │
└───────────────────────────┬───────────────────────────────────--┘
                             │ REST/JSON
┌───────────────────────────▼───────────────────────────────────--┐
│  Java / Spring Boot (Core API + Orchestration)                  │
│  - Team & user management        - Auth (single-user OK for v1) │
│  - FPL official API sync         - Recommendation orchestration │
│  - Business rules (budget, transfer legality, chip rules)       │
│  - Postgres access (JPA/Hibernate)                               │
│  - Calls Python ML service (REST) for predictions/scores        │
│  - Calls LLM service for natural-language explanation           │
└──────────┬───────────────────────────────────────┬───────────--─┘
           │ REST                                  │ REST
┌──────────▼───────────────────────┐   ┌────────────▼──────────────┐
│  Python / FastAPI (ML Service)   │   │  Ollama (Local LLM)        │
│  - Feature engineering           │   │  - Explanation generation  │
│  - xPoints model                 │   │  - Q&A over recommendations│
│  - Transfer/captaincy scoring    │   │  - Summarization           │
│  - Differential detection        │   └─────────────────────────--─┘
└──────────┬────────────────────--─┘
           │
┌──────────▼──────────────────────────────────────────────────────┐
│  PostgreSQL — players, teams, fixtures, gameweeks, stats,        │
│  my squad, transfers, predictions, recommendations                │
└────────────────────────────────────────────────────────────────--┘
           ▲
┌──────────┴────────────────────────────────────────────────────--┐
│  Python — Data Ingestion Pipeline (scheduled job)                │
│  Pulls official FPL API → cleans → loads into Postgres           │
└────────────────────────────────────────────────────────────────--┘
```

**Why split Java/Python this way:** Java is the system of record and the thing a client talks to — it enforces FPL rules (squad legality, budget, transfer limits) which is genuinely "business logic," not ML. Python owns everything statistical. This is a real, defensible boundary for an interview, not an artificial one.

---

## 3. Data Source

The official FPL API is free, unauthenticated, and sufficient for v1:

- `https://fantasy.premierleague.com/api/bootstrap-static/` — all players, teams, gameweeks, current prices/ownership
- `https://fantasy.premierleague.com/api/fixtures/` — fixtures + FDR (fixture difficulty rating)
- `https://fantasy.premierleague.com/api/element-summary/{player_id}/` — per-player history and upcoming fixtures
- `https://fantasy.premierleague.com/api/entry/{team_id}/event/{gw}/picks/` — **your** squad for a given gameweek (needs your team ID, no login required for public data)
- `https://fantasy.premierleague.com/api/entry/{team_id}/` — team value, bank, transfers, chips used

This alone covers requirement #1 ("integrate with FPL API to retrieve my actual team") with zero manual entry needed beyond your team ID. Supplement later (post-MVP) with something like Understat or FBref for xG/xA if the official API's underlying stats feel thin — flag this as a stretch goal, not MVP.

---

## 4. Database Schema (PostgreSQL) — Core Tables

| Table | Purpose |
|---|---|
| `teams` | EPL clubs (id, name, short_name, strength ratings) |
| `players` | FPL players (id, team_id, position, current_price, status/injury flag) |
| `gameweeks` | GW id, deadline, is_current, is_next |
| `fixtures` | GW, home/away team, FDR home/away, kickoff time |
| `player_gameweek_stats` | Per-player, per-GW actuals: minutes, goals, assists, clean sheets, bonus, points, ownership snapshot |
| `my_team` | Snapshot of your squad per GW: 15 players, XI, bench order, captain, vice-captain |
| `my_team_state` | Bank, team value, free transfers, chips remaining, overall rank, mini-league rank(s) |
| `transfers_log` | Historical transfers you made (for "don't overreact" tracking and future ML features) |
| `predictions` | player_id, gw, model_version, expected_points, floor, ceiling, start_probability |
| `recommendations` | Generated recommendation records (type: transfer/captain/chip, payload JSON, reasoning, confidence, created_at) — lets you look back at what the app told you and whether it was right |

Keeping `predictions` and `recommendations` as their own tables (not just computed on the fly) is what lets you later build a "how good was my model" evaluation view — a nice resume talking point (model monitoring).

---

## 5. ML / Data Layer (Python)

### 5.1 Feature engineering (per player, per upcoming GW)
- Rolling form (points/xG/xA over last 3/5 GWs)
- Season-long per-90 rates
- Fixture difficulty (own + next N GWs)
- Home/away
- Minutes trend (starts vs. sub appearances, last 5 GWs)
- Price trend / ownership trend
- Team-level attacking/defensive strength

### 5.2 Models (in order of build priority — this is also your ML roadmap)

1. **Expected Minutes / Start Probability** — classification (logistic regression or gradient-boosted trees) predicting P(60+ mins). This is the highest-leverage, easiest-to-justify model because your own domain doc says minutes matter more than anything else.
2. **Expected Points (xPoints)** — regression model (XGBoost/LightGBM) predicting next-GW fantasy points per player, conditioned on start probability, fixture, form, underlying stats. Output floor/ceiling via quantile regression or a simple residual-based interval, not just a point estimate.
3. **Transfer Scoring** — not a separate trained model initially; a **scoring function** built on top of #2: `transfer_value = (incoming xPoints over horizon) − (outgoing xPoints over horizon) − hit_cost − opportunity_cost`. This is honest engineering: don't force an ML model where a well-designed formula is more correct and more explainable.
4. **Captaincy Ranking** — ranks current squad by `xPoints × start_probability`, weighted by ceiling for differential captaincy vs. floor for safe captaincy, exposed as a toggle tied to risk tolerance.
5. **Differential Detection** — filters players below an ownership threshold, ranks by `xPoints − ownership-adjusted expectation`, i.e. players whose model output meaningfully exceeds what their ownership implies the market expects.

Stretch (post-MVP, only if it's adding real value): goal/assist/clean-sheet probability as separate classifiers feeding into #2, rather than #2 predicting points directly.

### 5.3 Why this is "meaningful ML," not resume-padding
Your doc explicitly warns against ML for its own sake. The defensible line for an interview: *"I use ML where the target is genuinely probabilistic and hard to hand-write rules for (minutes, points), and deterministic scoring formulas where the logic is well-defined (transfer value, hit-worthiness)."* Say that in your README.

---

## 6. LLM Layer

- **Model:** run via Ollama locally (e.g. a Llama 3.1 8B or similar instruct model — pick based on what your machine can run comfortably).
- **Strict contract:** the LLM receives structured JSON (predictions + recommendation objects) as context and is prompted to *only* narrate/explain that JSON — never to generate its own numbers. This is enforceable with prompt design + a lightweight post-check (e.g., flag if the LLM's response contains numbers not present in the input JSON).
- **Two use modes:**
  1. **Explanation mode** — turns a `recommendations` row into the RECOMMENDATION / WHY / RISKS / ALTERNATIVES format from your spec.
  2. **Chat mode** — lets you ask ad hoc questions ("why not captain X instead?"), answered by re-fetching relevant predictions from Postgres via the Java API and feeding them to the LLM as context (retrieval, not fine-tuning).

---

## 7. API Surface (Java, sketch)

```
GET  /api/team/current            → squad, XI, bench, captain, budget, chips
POST /api/team/sync                → pulls latest from official FPL API for your team id
GET  /api/fixtures?teamId=&range= → fixture difficulty over N gameweeks
GET  /api/recommendations/transfers
GET  /api/recommendations/captain
GET  /api/recommendations/chips
GET  /api/analysis/weaknesses
GET  /api/analysis/differentials
POST /api/chat                     → natural-language Q&A over current state
```

Python ML service exposes internal-only endpoints Java calls server-to-server (`/predict/points`, `/score/transfers`, `/rank/captains`, `/detect/differentials`) — not exposed to the frontend directly, keeping the frontend's contract entirely with Java.

---

## 8. Repository Structure

```
fpl-assistant/
├── frontend/            # React + TS + Vite + Tailwind
├── backend-java/        # Spring Boot: api, service, repository, domain, dto
├── ml-service/          # Python FastAPI: ingestion/, features/, models/, api/
├── ollama/               # model config / prompt templates
├── db/                   # Flyway/Liquibase migrations
├── docker-compose.yml
└── docs/                 # architecture notes, model cards
```

---

## 9. Phased Roadmap

**Phase 0 — Scaffolding (few days)**
Repo structure, Docker Compose (Postgres + empty Java + empty Python services), CI skeleton (lint/test on push).

**Phase 1 — Data ingestion**
Python job pulling bootstrap-static + fixtures + your team's picks into Postgres. This alone gives you a working "view my squad" feature once Java reads it.

**Phase 2 — Core team management (Java + frontend)**
Squad/XI/bench/captain/budget/chips views. Read-only at first; this is your functional MVP milestone #1.

**Phase 3 — Fixture analysis**
FDR views, multi-GW fixture run detection, blank/double GW flags — pure data/aggregation, no ML yet, but delivers real value fast.

**Phase 4 — xPoints + minutes model (Python)**
Train on historical `player_gameweek_stats`, store in `predictions`. Validate with a backtest (predict GW N using data through GW N−1, compare to actual) — a legitimate ML evaluation story for your resume.

**Phase 5 — Recommendation engine**
Transfer scoring, captaincy ranking, weakness analysis, differentials — built on Phase 4's predictions.

**Phase 6 — LLM explanation layer**
Ollama integration, structured-JSON-in/explanation-out, then chat mode.

**Phase 7 — Chip strategy**
Requires forward-looking fixture/BGW/DGW modeling across a horizon — naturally the most complex feature, sequenced last on purpose.

**Phase 8 — Polish**
Docker Compose end-to-end, API docs (OpenAPI/Swagger on the Java side), tests across all three services, README with architecture diagram and model evaluation numbers.

---

## 10. Testing & Engineering Practices

- **Java:** JUnit for service/business-rule logic (transfer legality, budget checks), integration tests against a test Postgres (Testcontainers).
- **Python:** pytest for feature engineering and scoring functions; a small backtest script as a "test" that model performance stays above a baseline.
- **CI:** GitHub Actions running both test suites on push — free, and a genuine CI/CD story.
- **Docker Compose:** one command (`docker compose up`) brings up Postgres, Java, Python ML service, Ollama, frontend — this is your demo story for interviews.

---

## 11. Risks & Mitigations

| Risk | Mitigation |
|---|---|
| Historical stats needed for training aren't in the official API in enough depth | Start model training once you've collected a few GWs yourself via ingestion; use prior-season data if available, or lean more on the minutes-classifier + formula-based scoring first, add xPoints once enough data exists |
| Local LLM inventing numbers | Strict JSON-grounded prompting + a validation check before displaying LLM output |
| Scope creep (chips, differentials, chat all at once) | Roadmap above sequences by value/complexity — resist building everything before Phase 2 ships |
| Over-engineering the MVP | Single-user, no auth beyond a config value for your FPL team ID, no Kubernetes — Docker Compose is enough until it demonstrably isn't |

---

## 12. Resume Framing (once built)

*"Built a full-stack FPL decision-support system: Spring Boot API + PostgreSQL for team/domain logic, a Python/FastAPI ML service (XGBoost) predicting player expected points and start probability with backtested evaluation, a rules-based transfer/captaincy scoring engine on top of those predictions, and a local LLM (Ollama) layer that explains recommendations in natural language without generating its own numbers — fully containerized with Docker Compose, zero paid infrastructure."*

That sentence alone demonstrates: backend engineering, ML with real evaluation, system design/service boundaries, and LLM integration done responsibly (grounded, not hallucinated) — which is exactly the resume-worthy story you're after.
