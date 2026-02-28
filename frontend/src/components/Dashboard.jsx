import { useEffect, useState } from 'react'

const KITE_LOGIN_URL = 'http://localhost:8080/api/auth/login'

const StatCard = ({ label, value }) => {
  const isPositive = value >= 0
  return (
    <div className="bg-gray-800 rounded-xl p-5 flex flex-col gap-1 border border-gray-700">
      <span className="text-gray-400 text-sm uppercase tracking-widest">{label}</span>
      <span className={`text-2xl font-bold ${isPositive ? 'text-emerald-400' : 'text-red-400'}`}>
        {isPositive ? '+' : ''}{value.toFixed(2)}%
      </span>
    </div>
  )
}

const StrategyMatchBadge = ({ match }) => {
  const colors = {
    'STRONG MATCH': 'bg-emerald-900 text-emerald-300 border border-emerald-600',
    'PARTIAL MATCH': 'bg-yellow-900 text-yellow-300 border border-yellow-600',
    'NO MATCH': 'bg-red-900 text-red-300 border border-red-600',
  }
  return (
    <span className={`px-2 py-0.5 rounded text-xs font-semibold ${colors[match] ?? 'bg-gray-700 text-gray-300'}`}>
      {match}
    </span>
  )
}

export default function Dashboard() {
  const [data, setData] = useState(null)
  const [error, setError] = useState(null)

  useEffect(() => {
    fetch('/api/dashboard')
      .then(res => {
        if (res.status === 401) {
          window.location.href = KITE_LOGIN_URL
          return
        }
        if (!res.ok) throw new Error(`HTTP ${res.status}`)
        return res.json()
      })
      .then(json => { if (json) setData(json) })
      .catch(err => setError(err.message))
  }, [])

  if (error) {
    return (
      <div className="min-h-screen bg-gray-950 flex items-center justify-center text-red-400">
        Failed to load dashboard: {error}
      </div>
    )
  }

  if (!data) {
    return (
      <div className="min-h-screen bg-gray-950 flex items-center justify-center text-gray-400 animate-pulse">
        Loading Trading Terminal…
      </div>
    )
  }

  const { performance, holdings, strategy } = data

  return (
    <div className="min-h-screen bg-gray-950 text-gray-100 font-mono p-6 space-y-8">

      {/* Header */}
      <header className="flex items-center justify-between border-b border-gray-700 pb-4">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-white">⚡ Avants Trading Terminal</h1>
          <p className="text-gray-500 text-sm mt-0.5">Strategy Governor Dashboard</p>
        </div>
        <span className="text-xs text-emerald-400 border border-emerald-700 rounded px-2 py-1 animate-pulse">
          ● LIVE
        </span>
      </header>

      {/* Performance Stats */}
      <section>
        <h2 className="text-xs text-gray-500 uppercase tracking-widest mb-3">Performance</h2>
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
          <StatCard label="Daily" value={performance.dailyPct} />
          <StatCard label="Weekly" value={performance.weeklyPct} />
          <StatCard label="Monthly" value={performance.monthlyPct} />
        </div>
      </section>

      {/* Holdings Table */}
      <section>
        <h2 className="text-xs text-gray-500 uppercase tracking-widest mb-3">Holdings</h2>
        <div className="overflow-x-auto rounded-xl border border-gray-700">
          <table className="w-full text-sm">
            <thead className="bg-gray-800 text-gray-400 uppercase text-xs tracking-widest">
              <tr>
                <th className="px-4 py-3 text-left">Symbol</th>
                <th className="px-4 py-3 text-right">P&amp;L (₹)</th>
                <th className="px-4 py-3 text-right">P&amp;L %</th>
                <th className="px-4 py-3 text-center">Strategy Match</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-800">
              {holdings.map(h => (
                <tr key={h.symbol} className="hover:bg-gray-800/50 transition-colors">
                  <td className="px-4 py-3 font-semibold text-white">{h.symbol}</td>
                  <td className={`px-4 py-3 text-right ${h.pnl >= 0 ? 'text-emerald-400' : 'text-red-400'}`}>
                    {h.pnl >= 0 ? '+' : ''}₹{h.pnl.toLocaleString('en-IN', { minimumFractionDigits: 2 })}
                  </td>
                  <td className={`px-4 py-3 text-right ${h.pnlPct >= 0 ? 'text-emerald-400' : 'text-red-400'}`}>
                    {h.pnlPct >= 0 ? '+' : ''}{h.pnlPct.toFixed(2)}%
                  </td>
                  <td className="px-4 py-3 text-center">
                    <StrategyMatchBadge match={h.strategyMatch} />
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>

      {/* Strategy Viewer */}
      <section>
        <h2 className="text-xs text-gray-500 uppercase tracking-widest mb-3">Strategy Viewer</h2>
        <div className="bg-gray-800 rounded-xl border border-gray-700 p-5 space-y-5">
          <div>
            <p className="text-white font-bold text-lg">{strategy.name}</p>
            <p className="text-gray-400 text-sm mt-1">{strategy.description}</p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            {/* Indicators */}
            <div>
              <h3 className="text-xs text-gray-500 uppercase tracking-widest mb-2">Indicators</h3>
              <ul className="space-y-1">
                {strategy.indicators.map((ind, i) => (
                  <li key={i} className="text-sm bg-gray-900 rounded px-3 py-1.5 text-cyan-300">
                    {ind.type}({ind.period}) <span className="text-gray-500">· {ind.source}</span>
                  </li>
                ))}
              </ul>
            </div>

            {/* Entry Conditions */}
            <div>
              <h3 className="text-xs text-emerald-500 uppercase tracking-widest mb-2">Entry Conditions</h3>
              <ul className="space-y-1">
                {strategy.entryConditions.map((r, i) => (
                  <li key={i} className="text-sm bg-gray-900 rounded px-3 py-1.5 text-emerald-300">
                    ✓ {r.condition}
                  </li>
                ))}
              </ul>
            </div>

            {/* Exit Conditions */}
            <div>
              <h3 className="text-xs text-red-500 uppercase tracking-widest mb-2">Exit Conditions</h3>
              <ul className="space-y-1">
                {strategy.exitConditions.map((r, i) => (
                  <li key={i} className="text-sm bg-gray-900 rounded px-3 py-1.5 text-red-300">
                    ✗ {r.condition}
                  </li>
                ))}
              </ul>
            </div>
          </div>
        </div>
      </section>

    </div>
  )
}
