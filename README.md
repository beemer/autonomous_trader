# Avants Autonomous Trader

> A two-component autonomous trading system: a **Strategic Governor** (The Librarian) and an **Autonomous Executioner** (OpenClaw).

---

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                     AUTONOMOUS TRADER SYSTEM                    │
│                                                                 │
│  ┌──────────────────────────┐      ┌────────────────────────┐  │
│  │   THE GOVERNOR           │      │   THE EXECUTIONER      │  │
│  │   (Java Spring Boot App) │      │   (OpenClaw Agent)     │  │
│  │                          │      │                        │  │
│  │  - Manages strategy &    │─────▶│  - Reads strategy &    │  │
│  │    positions files       │      │    positions files     │  │
│  │  - Sets Strategy Rules   │      │  - Asks Claude 4.6     │  │
│  │  - Sets Risk Parameters  │      │    for trade approval  │  │
│  │  - Syncs live portfolio  │      │  - Executes trades via │  │
│  │    from Zerodha every 60s│      │    Zerodha Kite API    │  │
│  │  - React dashboard UI    │      └────────────────────────┘  │
│  └──────────────────────────┘                                   │
│             │                                                   │
│             ▼                                                   │
│    strategy.json + positions.json                               │
│       (strategy rules + live portfolio)                         │
└─────────────────────────────────────────────────────────────────┘
```

---

## Component 1: The Governor (This Repository)

**Role:** The Librarian / Strategic Brain

The Governor is a **Spring Boot 3 (Java 21)** application that acts as the single source of truth for the trading system. It owns and manages `strategy.json` (universe, strategy rules, risk parameters) and `positions.json` (live portfolio), and continuously syncs live portfolio data from Zerodha Kite Connect.

### Responsibilities

| Responsibility | Description |
|---|---|
| **Universe Management** | Defines which instruments are eligible for trading (e.g., Nifty 50 symbols on NSE) |
| **Strategy Definition** | Encodes the technical strategy rules (e.g., 9 EMA > 200 EMA + MACD Breakout) |
| **Risk Governance** | Sets hard limits: max capital per trade, max open positions, stop-loss %, target % |
| **Live Portfolio Sync** | Fetches holdings and net positions from Zerodha every 60 seconds via Java 21 Virtual Threads |
| **Strategy Publishing** | Writes `strategy.json` and `positions.json` that OpenClaw reads |
| **REST API** | Exposes `/api/dashboard` and `/api/portfolio` endpoints for the frontend and external consumers |

### Key Classes

```
src/main/java/com/avants/autonomoustrader/
├── AutonomousTraderApplication.java       # Spring Boot entry point (@EnableScheduling)
├── config/
│   ├── AsyncConfig.java                   # Java 21 Virtual Thread executor bean
│   └── KiteConfig.java                    # KiteConnect bean (KITE_API_KEY, KITE_ACCESS_TOKEN)
├── controller/
│   └── DashboardController.java           # GET /api/dashboard, GET /api/portfolio
├── dto/
│   ├── DashboardDto.java                  # DashboardResponse, PerformanceStats, Holding, StrategyViewer
│   └── KiteDto.java                       # LivePortfolio, HoldingDto, PositionDto
├── model/
│   ├── TradingStrategy.java              # POJO model for strategy.json
│   └── TradingPositions.java             # POJO model for positions.json
└── service/
    ├── GovernorService.java               # Load / save / summarize strategy and positions
    └── KiteSyncService.java               # Scheduled sync: holdings + positions every 60s
```

### Frontend

A **React (Vite)** dashboard lives in `frontend/` and connects to the Governor's REST API:

```
frontend/src/
├── App.jsx
└── components/
    └── Dashboard.jsx    # Live portfolio viewer + strategy display
```

---

## Component 2: The Executioner (OpenClaw)

**Role:** The Autonomous Trading Agent

OpenClaw is a **separate autonomous agent** that operates independently from the Governor. It is the action arm of the system — it reads the strategy defined by the Governor and executes trades in the real market.

### Responsibilities

| Responsibility | Description |
|---|---|
| **File Consumption** | Reads `strategy.json` and `positions.json` to understand the current Universe, Strategy, and live portfolio |
| **Live Data Fetching** | Fetches real-time OHLCV data for all symbols in the Universe |
| **AI-Powered Approval** | Sends market context + strategy rules to **Claude 4.6** and asks: *"Should I trade this?"* |
| **Trade Execution** | Places, monitors, and exits trades via the **Zerodha Kite Connect API** |

### OpenClaw Decision Flow

```
Read strategy.json + positions.json
        │
        ▼
Fetch live market data (OHLCV) for each symbol
        │
        ▼
Compute indicators (EMA 9, EMA 200, MACD)
        │
        ▼
Check entry conditions from strategy.json
        │
        ▼
If conditions met → Ask Claude 4.6 for approval
        │
        ├── APPROVED → Place order via Kite Connect
        │
        └── REJECTED → Log reason, skip trade
```

---

## The Contract — `strategy.json` + `positions.json`

These two files are the **contract** between the Governor and the Executioner. The Governor writes them; OpenClaw reads them.

**`strategy.json`** — universe, technical strategy rules, and risk parameters:

```json
{
  "strategy_version": "1.0.0",
  "last_updated": "2026-02-28T12:00:00",
  "universe": {
    "name": "Nifty 50",
    "exchange": "NSE",
    "symbols": ["RELIANCE", "TCS", "HDFCBANK", "..."]
  },
  "technical_strategy": {
    "name": "EMA Crossover + MACD Breakout",
    "indicators": [...],
    "entry_conditions": ["EMA_9 > EMA_200", "MACD_LINE crosses_above MACD_SIGNAL", "..."],
    "exit_conditions": [...]
  },
  "risk_parameters": {
    "max_capital_per_trade_pct": 5.0,
    "max_open_positions": 5,
    "stop_loss_pct": 1.5,
    "target_pct": 3.0
  }
}
```

**`positions.json`** — live portfolio synced every 60s from Zerodha:

```json
{
  "last_updated": "2026-02-28T12:00:00",
  "live_portfolio": {
    "holdings": [...],
    "positions": [...]
  }
}
```

---

## REST API

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/dashboard` | Dashboard data: performance stats, holdings with strategy match, strategy viewer |
| `GET` | `/api/portfolio` | Live portfolio (holdings + net positions) from last Kite sync; `204` if not yet synced |

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3 |
| Concurrency | Java 21 Virtual Threads (`Executors.newVirtualThreadPerTaskExecutor()`) |
| Build Tool | Maven |
| JSON Serialization | Jackson Databind |
| Broker API | Zerodha Kite Connect (`com.zerodhatech.kiteconnect`) |
| Frontend | React + Vite |
| AI Approval Engine | Anthropic Claude 4.6 (used by OpenClaw) |

---

## Getting Started

### Prerequisites

- Java 21+
- Maven 3.8+
- Node.js 18+ (for frontend)
- Zerodha Kite Connect API credentials

### Build & Run

```bash
# Set Kite credentials (required at startup)
export KITE_API_KEY=your_api_key
export KITE_ACCESS_TOKEN=your_access_token

# Build the project
mvn clean install

# Run the Governor backend (http://localhost:8080)
mvn spring-boot:run

# Run the React frontend (separate terminal)
cd frontend
npm install
npm run dev
```

### Configuration

Key settings in `src/main/resources/application.properties`:

| Property | Default | Purpose |
|---|---|---|
| `server.port` | `8080` | HTTP port |
| `spring.threads.virtual.enabled` | `true` | Java 21 Virtual Threads |
| `trading.strategy.path` | `strategy.json` | Path to the strategy file |
| `trading.positions.path` | `positions.json` | Path to the positions file |
| `kite.api-key` | `${KITE_API_KEY}` | Zerodha API key |
| `kite.access-token` | `${KITE_ACCESS_TOKEN}` | Zerodha access token |

---

## Project Structure

```
autonomous_trader/
├── pom.xml                              # Maven build (Java 21, Spring Boot 3)
├── strategy.json                        # Strategy rules + universe — read by OpenClaw
├── positions.json                       # Live portfolio — updated every 60s
├── README.md
├── PROJECT_CONTEXT.md                   # Agent/contributor onboarding guide
├── .junie/
│   └── guidelines.md                    # Agent behaviour rules
├── frontend/                            # React (Vite) dashboard
│   └── src/
│       ├── App.jsx
│       └── components/
│           └── Dashboard.jsx
└── src/
    ├── main/
    │   ├── java/com/avants/autonomoustrader/
    │   │   ├── AutonomousTraderApplication.java
    │   │   ├── config/
    │   │   │   ├── AsyncConfig.java
    │   │   │   └── KiteConfig.java
    │   │   ├── controller/
    │   │   │   └── DashboardController.java
    │   │   ├── dto/
    │   │   │   ├── DashboardDto.java
    │   │   │   └── KiteDto.java
    │   │   ├── model/
    │   │   │   └── TradingManifest.java
    │   │   └── service/
    │   │       ├── GovernorService.java
    │   │       └── KiteSyncService.java
    │   └── resources/
    │       └── application.properties
    └── test/
        └── java/com/avants/autonomoustrader/
            └── AutonomousTraderApplicationTests.java
```

---

## Roadmap

- [x] Spring Boot 3 + Java 21 scaffold
- [x] `strategy.json` (Nifty 50 + EMA/MACD strategy) + `positions.json` (live portfolio)
- [x] Java 21 Virtual Thread executor
- [x] Zerodha KiteConnect integration (live holdings + positions sync every 60s)
- [x] REST API: `/api/dashboard`, `/api/portfolio`
- [x] React frontend dashboard
- [ ] Full CRUD REST API for strategy management
- [ ] Strategy versioning and audit log
- [ ] Multi-strategy support (different strategies per symbol group)
- [ ] OpenClaw Executioner (separate repo)
- [ ] Claude integration in Executioner
- [ ] Dashboard UI for live trade monitoring

---

## License

Proprietary — Avants Technologies. All rights reserved.
