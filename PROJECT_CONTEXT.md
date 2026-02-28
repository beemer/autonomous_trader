# Project Context — Avants Autonomous Trader

> Quick-reference for AI agents and new contributors. Read this first before exploring the codebase.

---

## What This Project Is

A **two-component autonomous trading system** for Indian equity markets (NSE/BSE via Zerodha Kite Connect):

| Component | Name | Tech | Role |
|---|---|---|---|
| This repo | **The Governor** (The Librarian) | Java 21 + Spring Boot 3 + React frontend | Owns strategy, universe, risk rules, live portfolio sync. Publishes `trading_manifest.json`. |
| Separate repo | **The Executioner** (OpenClaw) | Autonomous agent | Reads the manifest, fetches live data, asks Claude for trade approval, executes via Kite API. |

---

## The Bible — `trading_manifest.json`

The single most important file in this repo. It is the contract between The Governor and The Executioner.

| Section | Purpose |
|---|---|
| `universe` | List of eligible trading instruments (e.g., Nifty 50 symbols on NSE) |
| `technicalStrategy` | Strategy rules: EMA crossover (9 EMA > 200 EMA), MACD breakout conditions |
| `riskParameters` | Hard limits: max capital per trade, max open positions, stop-loss %, target % |
| `livePortfolio` | Live holdings and net positions synced every minute from Zerodha Kite |
| `metadata` | Version, last-updated timestamp, active flag |

---

## Project Structure

```
autonomous_trader/
├── pom.xml                                              # Maven build (Java 21, Spring Boot 3, Jackson, Kite SDK)
├── trading_manifest.json                                # "The Bible" — read by OpenClaw at runtime
├── README.md                                            # Full architecture documentation
├── PROJECT_CONTEXT.md                                   # This file — agent/contributor onboarding
├── .junie/
│   └── guidelines.md                                    # Agent behaviour rules (approved actions, versioning policy, git workflow)
├── frontend/                                            # React (Vite) dashboard UI
│   └── src/
│       ├── App.jsx                                      # Root component
│       └── components/
│           └── Dashboard.jsx                            # Live portfolio + strategy viewer
└── src/
    ├── main/
    │   ├── java/com/avants/autonomoustrader/
    │   │   ├── AutonomousTraderApplication.java          # Spring Boot entry point (@SpringBootApplication, @EnableScheduling)
    │   │   ├── config/
    │   │   │   ├── AsyncConfig.java                      # Java 21 Virtual Thread executor bean
    │   │   │   └── KiteConfig.java                       # KiteConnect bean (reads KITE_API_KEY, KITE_ACCESS_TOKEN)
    │   │   ├── controller/
    │   │   │   └── DashboardController.java              # REST: GET /api/dashboard, GET /api/portfolio
    │   │   ├── dto/
    │   │   │   ├── DashboardDto.java                     # Records: DashboardResponse, PerformanceStats, Holding, StrategyViewer
    │   │   │   └── KiteDto.java                          # Records: LivePortfolio, HoldingDto, PositionDto
    │   │   ├── model/
    │   │   │   └── TradingManifest.java                  # POJO model for The Bible (Universe, Strategy, Risk, LivePortfolio)
    │   │   └── service/
    │   │       ├── GovernorService.java                  # Load / save / summarize trading_manifest.json
    │   │       └── KiteSyncService.java                  # Scheduled: fetches holdings + positions from Kite every 60s via Virtual Threads
    │   └── resources/
    │       └── application.properties                    # Port 8080, manifest path, Kite credentials, logging level
    └── test/
        └── java/com/avants/autonomoustrader/
            └── AutonomousTraderApplicationTests.java     # Spring context load test
```

---

## Key Dependencies (`pom.xml`)

| Dependency | Purpose |
|---|---|
| `org.springframework.boot:spring-boot-starter-web` | Spring MVC REST API |
| `org.springframework.boot:spring-boot-starter` | Core Spring Boot framework |
| `com.fasterxml.jackson.core:jackson-databind` | JSON serialization/deserialization of the manifest |
| `com.zerodhatech.kiteconnect:kiteconnect` | Zerodha Kite Connect Java SDK — live holdings, positions |

> **Policy:** Always use the latest stable release of all dependencies. See `.junie/guidelines.md`.

---

## Key Configuration (`application.properties`)

| Property | Default | Purpose |
|---|---|---|
| `server.port` | `8080` | HTTP port |
| `spring.threads.virtual.enabled` | `true` | Enables Java 21 Virtual Threads |
| `trading.manifest.path` | `trading_manifest.json` | Path to The Bible |
| `kite.api-key` | `${KITE_API_KEY}` | Zerodha API key (set via env var) |
| `kite.access-token` | `${KITE_ACCESS_TOKEN}` | Zerodha access token (set via env var) |

---

## REST API

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/dashboard` | Returns dashboard data: performance stats, holdings with strategy match, strategy viewer |
| `GET` | `/api/portfolio` | Returns live portfolio (holdings + net positions) from last Kite sync; 204 if not yet synced |

---

## How The System Works (End-to-End Flow)

```
1. Human operator updates strategy/risk rules via The Governor (this app)
        ↓
2. GovernorService writes updated trading_manifest.json ("The Bible")
        ↓
3. KiteSyncService fetches live holdings + positions from Zerodha every 60s
   (parallel CompletableFuture on Java 21 Virtual Threads)
        ↓
4. Live portfolio is written back into trading_manifest.json
        ↓
5. OpenClaw (The Executioner) reads trading_manifest.json on each cycle
        ↓
6. OpenClaw fetches live market data for instruments in `universe`
        ↓
7. OpenClaw sends data + strategy rules to Claude (AI) for trade approval
        ↓
8. If Claude approves → OpenClaw executes trade via Zerodha Kite Connect API
```

---

## Running The Governor Locally

```bash
# Set Kite credentials
export KITE_API_KEY=your_api_key
export KITE_ACCESS_TOKEN=your_access_token

# Build and run backend
mvn clean package
mvn spring-boot:run

# Backend starts on http://localhost:8080

# Run frontend (separate terminal)
cd frontend
npm install
npm run dev
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
| Governor Spring Boot scaffold | ✅ Complete |
| `trading_manifest.json` (Nifty 50 + EMA/MACD strategy) | ✅ Complete |
| Java 21 Virtual Thread executor (`AsyncConfig`) | ✅ Complete |
| Zerodha KiteConnect config (`KiteConfig`) | ✅ Complete |
| Live portfolio sync every 60s (`KiteSyncService`) | ✅ Complete |
| REST API: `/api/dashboard`, `/api/portfolio` (`DashboardController`) | ✅ Complete |
| DTOs: `KiteDto`, `DashboardDto` | ✅ Complete |
| React frontend dashboard (`frontend/`) | ✅ Complete |
| Governor REST API (full CRUD for manifest) | 🔲 Not started |
| OpenClaw Executioner | 🔲 Separate repo, not started |
| Claude integration in Executioner | 🔲 Not started |
| Zerodha Kite live order execution | 🔲 Not started |
