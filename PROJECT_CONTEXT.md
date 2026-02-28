# Project Context — Avants Autonomous Trader

> Quick-reference for AI agents and new contributors. Read this first before exploring the codebase.

---

## What This Project Is

A **two-component autonomous trading system** for Indian equity markets (NSE/BSE via Zerodha Kite Connect):

| Component | Name | Tech | Role |
|---|---|---|---|
| This repo | **The Governor** (The Librarian) | Java 21 + Spring Boot | Owns strategy, universe, and risk rules. Publishes `trading_manifest.json`. |
| Separate repo | **The Executioner** (OpenClaw) | Autonomous agent | Reads the manifest, fetches live data, asks Claude for trade approval, executes via Kite API. |

---

## The Bible — `trading_manifest.json`

The single most important file in this repo. It is the contract between The Governor and The Executioner.

| Section | Purpose |
|---|---|
| `universe` | List of eligible trading instruments (e.g., Nifty 50 symbols on NSE) |
| `technicalStrategy` | Strategy rules: EMA crossover (9 EMA > 200 EMA), MACD breakout conditions |
| `riskParameters` | Hard limits: max capital per trade, max open positions, stop-loss %, target % |
| `metadata` | Version, last-updated timestamp, active flag |

---

## Project Structure

```
autonomous_trader/
├── pom.xml                                          # Maven build (Java 21, Spring Boot, Jackson, Kite SDK)
├── trading_manifest.json                            # "The Bible" — read by OpenClaw at runtime
├── README.md                                        # Full architecture documentation
├── PROJECT_CONTEXT.md                               # This file — agent/contributor onboarding
├── .junie/
│   └── guidelines.md                                # Agent behaviour rules (approved actions, versioning policy, git workflow)
└── src/
    ├── main/
    │   ├── java/com/avants/autonomoustrader/
    │   │   ├── AutonomousTraderApplication.java      # Spring Boot entry point (@SpringBootApplication)
    │   │   ├── model/
    │   │   │   └── TradingManifest.java              # POJO model for The Bible (Universe, Strategy, Risk)
    │   │   └── service/
    │   │       └── GovernorService.java              # Load / save / summarize trading_manifest.json
    │   └── resources/
    │       └── application.properties               # App config: port 8080, manifest file path
    └── test/
        └── java/com/avants/autonomoustrader/
            └── AutonomousTraderApplicationTests.java # Spring context load test
```

---

## Key Dependencies (`pom.xml`)

| Dependency | Purpose |
|---|---|
| `org.springframework.boot:spring-boot-starter` | Core Spring Boot framework |
| `com.fasterxml.jackson.core:jackson-databind` | JSON serialization/deserialization of the manifest |
| `com.zerodhatech.kiteconnect:kiteconnect` | Zerodha Kite Connect Java SDK (used by Executioner; included here for shared model compatibility) |

> **Policy:** Always use the latest stable release of all dependencies. See `.junie/guidelines.md`.

---

## How The System Works (End-to-End Flow)

```
1. Human operator updates strategy/risk rules via The Governor (this app)
        ↓
2. GovernorService writes updated trading_manifest.json ("The Bible")
        ↓
3. OpenClaw (The Executioner) reads trading_manifest.json on each cycle
        ↓
4. OpenClaw fetches live market data for instruments in `universe`
        ↓
5. OpenClaw sends data + strategy rules to Claude (AI) for trade approval
        ↓
6. If Claude approves → OpenClaw executes trade via Zerodha Kite Connect API
```

---

## Running The Governor Locally

```bash
# Build
mvn clean package

# Run
mvn spring-boot:run

# App starts on http://localhost:8080
```

---

## Branch & PR Conventions

- Never commit directly to `main`.
- Branch naming: `feat/<description>`, `fix/<description>`, `chore/<description>`.
- Always open a PR via `gh pr create --base main`.
- See `.junie/guidelines.md` for full agent workflow rules.

---

## Current Status (as of 2026-02-28)

| Item | Status |
|---|---|
| Governor Spring Boot scaffold | ✅ Complete (`feat/initial-setup`) |
| `trading_manifest.json` (Nifty 50 + EMA/MACD strategy) | ✅ Complete |
| Governor REST API (CRUD for manifest) | 🔲 Not started |
| OpenClaw Executioner | 🔲 Separate repo, not started |
| Claude integration in Executioner | 🔲 Not started |
| Zerodha Kite live order execution | 🔲 Not started |
