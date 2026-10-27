# Architecture Documentation

## Overview

The Personal FPL Assistant is built as a multi-service system:
1. **Frontend (React + TS + Vite + Tailwind)**: User Interface.
2. **Java / Spring Boot**: System of record, FPL rule enforcement, user squad management, recommendation orchestration.
3. **Python / FastAPI**: Data ingestion, feature engineering, ML models (xPoints, start probability, transfer scoring).
4. **PostgreSQL**: Central data store for teams, players, fixtures, gameweeks, picks, predictions, recommendations.
5. **Ollama**: Local LLM for explaining recommendations and answering ad-hoc user queries without hallucinating numbers.

```
┌────────────────────────────────────────────────────────┐
│  React Frontend                                        │
└──────────────────────────┬─────────────────────────────┘
                           │ REST
┌──────────────────────────▼─────────────────────────────┐
│  Java Spring Boot API                                  │
└───────┬──────────────────────┬─────────────────┬───────┘
        │ REST                 │ REST            │ JPA
┌───────▼────────┐      ┌──────▼──────┐   ┌──────▼──────┐
│  Python ML     │      │   Ollama    │   │ PostgreSQL  │
└───────┬────────┘      └─────────────┘   └─────────────┘
        │
┌───────▼────────┐
│   FPL API      │
└────────────────┘
```
