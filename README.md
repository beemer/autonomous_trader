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
│  │  - Manages "The Bible"   │─────▶│  - Reads the manifest  │  │
│  │  - Defines Universe      │      │  - Fetches live data   │  │
│  │  - Sets Strategy Rules   │      │  - Asks Claude 4.6     │  │
│  │  - Sets Risk Parameters  │      │    for trade approval  │  │
│  │                          │      │  - Executes trades via │  │
│  └──────────────────────────┘      │    Zerodha Kite API    │  │
│             │                      └────────────────────────┘  │
│             ▼                                                   │
│    trading_manifest.json                                        │
│       ("The Bible")                                             │
└─────────────────────────────────────────────────────────────────┘
```

---

## Component 1: The Governor (This Repository)

**Role:** The Librarian / Strategic Brain

The Governor is a **Spring Boot (Java 21)** application that acts as the single source of truth for the trading system. It owns and manages `trading_manifest.json` — referred to internally as **"The Bible"**.

### Responsibilities

| Responsibility | Description |
|---|---|
| **Universe Management** | Defines which instruments are eligible for trading (e.g., Nifty 50 symbols on NSE) |
| **Strategy Definition** | Encodes the technical strategy rules (e.g., 9 EMA > 200 EMA + MACD Breakout) |
| **Risk Governance** | Sets hard limits: max capital per trade, max open positions, stop-loss %, target % |
| **Manifest Publishing** | Writes the final `trading_manifest.json` that OpenClaw reads |

### Key Classes

```
src/main/java/com/avants/autonomoustrader/
├── AutonomousTraderApplication.java   # Spring Boot entry point
├── model/
│   └── TradingManifest.java           # POJO model for The Bible
└── service/
    └── GovernorService.java           # Load / save / summarize the manifest
```

---

## Component 2: The Executioner (OpenClaw)

**Role:** The Autonomous Trading Agent

OpenClaw is a **separate autonomous agent** that operates independently from the Governor. It is the action arm of the system — it reads the strategy defined by the Governor and executes trades in the real market.

### Responsibilities

| Responsibility | Description |
|---|---|
| **Manifest Consumption** | Reads `trading_manifest.json` to understand the current Universe and Strategy |
| **Live Data Fetching** | Fetches real-time OHLCV data for all symbols in the Universe |
| **AI-Powered Approval** | Sends market context + strategy rules to **Claude 4.6** and asks: *"Should I trade this?"* |
| **Trade Execution** | Places, monitors, and exits trades via the **Zerodha Kite Connect API** |

### OpenClaw Decision Flow

```
Read trading_manifest.json
        │
        ▼
Fetch live market data (OHLCV) for each symbol
        │
        ▼
Compute indicators (EMA 9, EMA 200, MACD)
        │
        ▼
Check entry conditions from manifest
        │
        ▼
If conditions met → Ask Claude 4.6 for approval
        │
        ├── APPROVED → Place order via Kite Connect
        │
        └── REJECTED → Log reason, skip trade
```

---

## "The Bible" — trading_manifest.json

This JSON file is the **contract** between the Governor and the Executioner. The Governor writes it; OpenClaw reads it.

```json
{
  "manifest_version": "1.0.0",
  "last_updated": "2026-02-28T12:00:00",
  "universe": {
    "name": "Nifty 50",
    "exchange": "NSE",
    "symbols": ["RELIANCE", "TCS", "HDFCBANK", "..."]
  },
  "technical_strategy": {
    "name": "EMA Crossover + MACD Breakout",
    "description": "Enter long when 9 EMA is above 200 EMA and MACD line crosses above signal line.",
    "indicators": [...],
    "entry_conditions": [
      "EMA_9 > EMA_200",
      "MACD_LINE crosses_above MACD_SIGNAL",
      "VOLUME > 1.5x 20-period average volume"
    ],
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

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.2 |
| Build Tool | Maven |
| JSON Serialization | Jackson Databind |
| Broker API | Zerodha Kite Connect (`com.zerodhatech.kiteconnect`) |
| AI Approval Engine | Anthropic Claude 4.6 (used by OpenClaw) |

---

## Getting Started

### Prerequisites

- Java 21+
- Maven 3.8+
- Zerodha Kite Connect API credentials

### Build & Run

```bash
# Clone the repository
git clone https://github.com/your-org/autonomous_trader.git
cd autonomous_trader

# Build the project
mvn clean install

# Run the Governor
mvn spring-boot:run
```

### Updating The Bible

Modify `trading_manifest.json` directly, or use the `GovernorService` API to programmatically update the Universe, Strategy, or Risk Parameters. OpenClaw will pick up the changes on its next cycle.

---

## Project Structure

```
autonomous_trader/
├── pom.xml                          # Maven build file (Java 21, Spring Boot 3.2)
├── trading_manifest.json            # "The Bible" — read by OpenClaw
├── README.md
└── src/
    ├── main/
    │   ├── java/com/avants/autonomoustrader/
    │   │   ├── AutonomousTraderApplication.java
    │   │   ├── model/
    │   │   │   └── TradingManifest.java
    │   │   └── service/
    │   │       └── GovernorService.java
    │   └── resources/
    │       └── application.properties
    └── test/
        └── java/com/avants/autonomoustrader/
            └── AutonomousTraderApplicationTests.java
```

---

## Roadmap

- [ ] REST API endpoints to update the manifest via HTTP
- [ ] Manifest versioning and audit log
- [ ] Multi-strategy support (different strategies per symbol group)
- [ ] OpenClaw integration tests
- [ ] Dashboard UI for live monitoring

---

## License

Proprietary — Avants Technologies. All rights reserved.
