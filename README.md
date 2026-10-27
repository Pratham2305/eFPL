# Personal FPL Assistant ⚽

> A personalized, data-driven Fantasy Premier League decision-support system. Java (Spring Boot) owns the product/API layer, Python owns data/ML, and a local LLM (Ollama) explains model outputs in plain-English advice. Zero-cost, local-first, containerized.

---

## 🌟 Resume Framing

> *"Built a full-stack FPL decision-support system: Spring Boot API + PostgreSQL for team/domain logic, a Python/FastAPI ML service predicting player expected points and start probability with backtested evaluation, a rules-based transfer/captaincy scoring engine on top of those predictions, and a local LLM (Ollama) layer that explains recommendations in natural language without generating its own numbers — fully containerized with Docker Compose, zero paid infrastructure."*

---

## 📐 System Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│  React + TS + Vite (Frontend)                                  │
│  Squad Pitch · Fixture FDR Matrix · AI Recs · Chip Strategy · Chat │
└───────────────────────────┬─────────────────────────────────────┘
                             │ REST/JSON
┌───────────────────────────▼─────────────────────────────────────┐
│  Java / Spring Boot 3.3 (Core API & Orchestration)             │
│  - Squad & User State Management   - FPL Official API Sync       │
│  - Business & Budget Rules        - Recommendation Proxy        │
│  - PostgreSQL Access (JPA)        - Grounded LLM Chat Service   │
└──────────┬───────────────────────────────────────┬──────────────┘
           │ REST                                  │ REST
┌──────────▼───────────────────────┐   ┌────────────▼──────────────┐
│  Python / FastAPI (ML Service)   │   │  Ollama (Local LLM)        │
│  - Feature Engineering Pipeline  │   │  - Grounded Explanations   │
│  - Start Probability Classifier  │   │  - Natural Language Q&A    │
│  - xPoints Model & Backtesting   │   └───────────────────────────┘
│  - Transfer/Captain/Chip Scorer  │
└──────────┬───────────────────────┘
           │
┌──────────▼──────────────────────────────────────────────────────┐
│  PostgreSQL 16 Database — players, teams, fixtures, gameweeks,  │
│  my_team, player_stats, predictions, recommendations           │
└─────────────────────────────────────────────────────────────────┘
```

---

## ✨ Features

- **🏆 Tactical Pitch View**: Interactive 4-4-2 / 3-5-2 pitch visualization for starting XI and substitute bench with captain (C) / vice-captain (V) badges, price (£m), form, and injury status.
- **📅 Fixture Difficulty Rating (FDR) Matrix**: Multi-gameweek fixture runs (3, 5, 8 GW horizon) for all 20 Premier League clubs with color-coded difficulty badges (1-5) and Blank/Double GW flags.
- **🤖 ML Expected Points (xPoints) Model**: Predicts expected points, floor (10th percentile), and ceiling (90th percentile) conditioned on P(start), position, form, underlying xGI, and fixture difficulty. Includes backtest evaluation script (MAE & RMSE).
- **📈 Squad-Aware Transfer Scorer**: Ranks transfer-in / transfer-out pairs by net expected points gain over a 3-GW horizon ($\text{xPts}_{\text{in}} - \text{xPts}_{\text{out}} - \text{hit\_cost}$), enforcing FPL budget and club limit constraints (max 3 per club).
- **👑 Risk-Adjusted Captaincy Ranker**: Ranks squad starters for Safe (high floor) vs. Differential (high ceiling) captain choices.
- **🛡️ Squad Weakness & Differential Detector**: Highlights low-xPoints/injured starters needing transfer attention and low-ownership (<12%) target gems.
- **🚀 Chip Strategy Roadmap**: Forward-looking 8-GW timeline planning optimal deployment windows for Wildcard, Free Hit, Bench Boost, and Triple Captain.
- **💬 Grounded AI Chat Assistant**: Interactive AI assistant powered by local Ollama (Llama 3.1 8B) to answer manager Q&A without hallucinating numbers.

---

## 🛠️ Technology Stack

- **Frontend**: React 18, TypeScript, Vite, Vanilla CSS Design System, Lucide Icons
- **Backend**: Java 21, Spring Boot 3.3, Spring Data JPA, Flyway Database Migrations
- **ML / Data Service**: Python 3.12, FastAPI, SQLAlchemy 2.0, Pandas, NumPy, Psycopg2
- **Database**: PostgreSQL 16
- **Local LLM**: Ollama (Llama 3.1 8B)
- **Containerization**: Docker Compose

---

## 🚀 Quick Start with Docker Compose

Ensure Docker Desktop is running on your system, then run:

```bash
# 1. Clone the repository
git clone https://github.com/your-username/eFPL.git
cd eFPL

# 2. Copy environment file and configure your FPL Team ID
cp .env.example .env
# Edit .env and set FPL_TEAM_ID=your_id

# 3. Start all 5 microservices
docker compose up -d --build
```

Access the services:
- **Frontend Dashboard**: `http://localhost:3000` (or `http://localhost:8080` when bundled with Spring Boot)
- **Java Spring Boot API**: `http://localhost:8080/api/team/current`
- **Python ML Service**: `http://localhost:8000/docs` (Swagger UI)
- **Ollama LLM**: `http://localhost:11434`

For full endpoint definitions, input parameters, and sample JSON payloads, see the [API Documentation](file:///Users/macp1/Documents/Projects/eFPL/docs/api_documentation.md).

---

## 🧪 Local Testing & Verification

### Java Backend
```bash
cd backend-java
./mvnw clean test-compile
./mvnw clean package -DskipTests
```

### Python ML Service
```bash
cd ml-service
python3 -m venv venv
./venv/bin/pip install -r requirements.txt pytest
./venv/bin/pytest
```

---

## 📊 Guiding Principles

1. **Numbers come from data/ML, not the LLM.** The LLM narrates and explains structured model outputs; it never invents an expected-points number.
2. **No recommendation in isolation.** Every suggestion is squad-aware, budget-aware, and fixture-aware.
3. **MVP over impressiveness.** Every technology in the stack has a clear boundary and job.
4. **Zero cost.** Free official FPL API, local PostgreSQL, local LLM (Ollama), containerized via Docker Compose.
