import { useState } from 'react'

const DistanceBadge = ({ distancePct }) => {
  const isClose = Math.abs(distancePct) < 2
  const isMedium = Math.abs(distancePct) >= 2 && Math.abs(distancePct) < 5

  let colorClass = 'bg-gray-700 text-gray-300'
  if (isClose) colorClass = 'bg-emerald-900 text-emerald-300 border border-emerald-600'
  else if (isMedium) colorClass = 'bg-yellow-900 text-yellow-300 border border-yellow-600'

  return (
    <span className={`px-2 py-1 rounded text-xs font-semibold ${colorClass}`}>
      {distancePct >= 0 ? '+' : ''}{distancePct.toFixed(2)}%
    </span>
  )
}

const RecommendationCard = ({ symbol, currentPrice, ema200, distancePct }) => {
  return (
    <div className="bg-gray-800 rounded-xl border border-gray-700 p-5 hover:border-emerald-700 transition-colors">
      <div className="flex items-start justify-between mb-3">
        <div>
          <h3 className="text-white font-bold text-lg">{symbol}</h3>
          <p className="text-gray-400 text-sm">NSE</p>
        </div>
        <DistanceBadge distancePct={distancePct} />
      </div>

      <div className="space-y-2 text-sm">
        <div className="flex justify-between">
          <span className="text-gray-400">Current Price</span>
          <span className="text-white font-semibold">₹{currentPrice.toFixed(2)}</span>
        </div>
        <div className="flex justify-between">
          <span className="text-gray-400">EMA 200</span>
          <span className="text-cyan-300 font-semibold">₹{ema200.toFixed(2)}</span>
        </div>
        <div className="pt-2 border-t border-gray-700">
          <span className="text-gray-500 text-xs">Distance from EMA 200</span>
        </div>
      </div>
    </div>
  )
}

export default function InvestmentAdvisor() {
  const [candidates, setCandidates] = useState(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)

  const handleScanNifty50 = async () => {
    setLoading(true)
    setError(null)

    try {
      const response = await fetch('/api/v1/advice/top-candidates')
      if (!response.ok) {
        throw new Error(`HTTP ${response.status}: ${response.statusText}`)
      }
      const data = await response.json()
      setCandidates(data)
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="space-y-6">
      {/* Header with Scan Button */}
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-xl font-bold text-white">Investment Advisor</h2>
          <p className="text-gray-400 text-sm mt-1">
            Scan Nifty 50 stocks for EMA-based entry opportunities
          </p>
        </div>
        <button
          onClick={handleScanNifty50}
          disabled={loading}
          className="bg-emerald-600 hover:bg-emerald-700 disabled:bg-gray-700 disabled:text-gray-500 text-white font-semibold px-6 py-2.5 rounded-lg transition-colors flex items-center gap-2"
        >
          {loading ? (
            <>
              <svg className="animate-spin h-4 w-4" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
              </svg>
              Scanning...
            </>
          ) : (
            <>
              <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
              </svg>
              Scan Nifty 50
            </>
          )}
        </button>
      </div>

      {/* Loading State */}
      {loading && (
        <div className="bg-gray-800 rounded-xl border border-gray-700 p-10 text-center">
          <div className="flex flex-col items-center gap-4">
            <svg className="animate-spin h-12 w-12 text-emerald-500" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
              <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
              <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
            </svg>
            <div>
              <p className="text-white font-semibold">Analyzing Nifty 50 stocks...</p>
              <p className="text-gray-400 text-sm mt-1">Fetching historical data and calculating indicators (~20s)</p>
            </div>
          </div>
        </div>
      )}

      {/* Error State */}
      {error && (
        <div className="bg-red-900/20 border border-red-700 rounded-xl p-5">
          <div className="flex items-start gap-3">
            <svg xmlns="http://www.w3.org/2000/svg" className="h-6 w-6 text-red-400 flex-shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
            <div>
              <p className="text-red-400 font-semibold">Failed to scan stocks</p>
              <p className="text-red-300 text-sm mt-1">{error}</p>
            </div>
          </div>
        </div>
      )}

      {/* Results */}
      {candidates && !loading && (
        <div>
          <div className="flex items-center justify-between mb-4">
            <h3 className="text-sm text-gray-400 uppercase tracking-widest">
              Top Candidates ({candidates.length})
            </h3>
            <p className="text-xs text-gray-500">
              Green = Within 2% of EMA 200 · Amber = 2-5% from EMA 200
            </p>
          </div>

          {candidates.length === 0 ? (
            <div className="bg-gray-800 rounded-xl border border-gray-700 p-10 text-center">
              <p className="text-gray-400">No candidates found matching the criteria</p>
            </div>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
              {candidates.map(candidate => (
                <RecommendationCard
                  key={candidate.symbol}
                  symbol={candidate.symbol}
                  currentPrice={candidate.currentPrice}
                  ema200={candidate.ema200}
                  distancePct={candidate.distancePct}
                />
              ))}
            </div>
          )}
        </div>
      )}

      {/* Empty State */}
      {!candidates && !loading && !error && (
        <div className="bg-gray-800 rounded-xl border border-gray-700 p-10 text-center">
          <svg xmlns="http://www.w3.org/2000/svg" className="h-16 w-16 text-gray-600 mx-auto mb-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
          </svg>
          <p className="text-gray-400 font-semibold mb-2">Ready to analyze market opportunities</p>
          <p className="text-gray-500 text-sm">Click "Scan Nifty 50" to find stocks near their EMA 200 levels</p>
        </div>
      )}
    </div>
  )
}
