import { useState } from 'react'
import PortfolioDashboard from './components/PortfolioDashboard'
import InvestmentAdvisor from './components/InvestmentAdvisor'

export default function App() {
  const [activeTab, setActiveTab] = useState('portfolio')
  const [advisorCandidates, setAdvisorCandidates] = useState(null)
  const [advisorLoading, setAdvisorLoading] = useState(false)
  const [advisorError, setAdvisorError] = useState(null)

  const handleScanNifty50 = async () => {
    setAdvisorLoading(true)
    setAdvisorError(null)

    try {
      const response = await fetch('/api/v1/advice/top-candidates')
      if (!response.ok) {
        throw new Error(`HTTP ${response.status}: ${response.statusText}`)
      }
      const data = await response.json()
      setAdvisorCandidates(data)
    } catch (err) {
      setAdvisorError(err.message)
    } finally {
      setAdvisorLoading(false)
    }
  }

  return (
    <div className="min-h-screen bg-gray-950 text-gray-100 font-mono">
      {/* Header */}
      <header className="border-b border-gray-700 bg-gray-900/50 backdrop-blur">
        <div className="px-6 py-4">
          <div className="flex items-center justify-between">
            <div>
              <h1 className="text-2xl font-bold tracking-tight text-white">⚡ Avants Trading Terminal</h1>
              <p className="text-gray-500 text-sm mt-0.5">Multi-Strategy Investment Platform</p>
            </div>
            <span className="text-xs text-emerald-400 border border-emerald-700 rounded px-2 py-1 animate-pulse">
              ● LIVE
            </span>
          </div>

          {/* Tab Navigation */}
          <nav className="flex gap-1 mt-6 border-b border-gray-700">
            <button
              onClick={() => setActiveTab('portfolio')}
              className={`px-4 py-2 text-sm font-medium transition-colors relative ${
                activeTab === 'portfolio'
                  ? 'text-white'
                  : 'text-gray-400 hover:text-gray-300'
              }`}
            >
              Portfolio Dashboard
              {activeTab === 'portfolio' && (
                <div className="absolute bottom-0 left-0 right-0 h-0.5 bg-emerald-500" />
              )}
            </button>
            <button
              onClick={() => setActiveTab('advisor')}
              className={`px-4 py-2 text-sm font-medium transition-colors relative ${
                activeTab === 'advisor'
                  ? 'text-white'
                  : 'text-gray-400 hover:text-gray-300'
              }`}
            >
              Investment Advisor
              {activeTab === 'advisor' && (
                <div className="absolute bottom-0 left-0 right-0 h-0.5 bg-emerald-500" />
              )}
            </button>
          </nav>
        </div>
      </header>

      {/* Tab Content */}
      <main className="p-6">
        {activeTab === 'portfolio' && <PortfolioDashboard />}
        {activeTab === 'advisor' && (
          <InvestmentAdvisor
            candidates={advisorCandidates}
            loading={advisorLoading}
            error={advisorError}
            onScan={handleScanNifty50}
          />
        )}
      </main>
    </div>
  )
}
