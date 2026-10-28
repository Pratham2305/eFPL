import { useState, useEffect } from 'react';
import Header from './components/Header';
import PitchView from './components/PitchView';
import GameweekSelector from './components/GameweekSelector';
import FixtureMatrix from './components/FixtureMatrix';
import PlayerExplorer from './components/PlayerExplorer';
import RecommendationsView from './components/RecommendationsView';
import ChipStrategyView from './components/ChipStrategyView';
import HistoryView from './components/history/HistoryView';
import AiChatModal from './components/AiChatModal';
import { TeamCurrentResponse, GameweekDto } from './types';
import { LayoutGrid, Calendar, Users, Sparkles, Layers, MessageSquare, AlertCircle, TrendingUp } from 'lucide-react';

export function App() {
  const [team, setTeam] = useState<TeamCurrentResponse | null>(null);
  const [gameweeks, setGameweeks] = useState<GameweekDto[]>([]);
  const [selectedGw, setSelectedGw] = useState<number | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [isSyncing, setIsSyncing] = useState<boolean>(false);
  const [teamId, setTeamId] = useState<string>(() => localStorage.getItem('fpl_team_id') || '3500002');
  const [activeTab, setActiveTab] = useState<'pitch' | 'recommendations' | 'chips' | 'fixtures' | 'directory' | 'history'>('pitch');
  const [isChatOpen, setIsChatOpen] = useState<boolean>(false);
  const [chatInitialQuery, setChatInitialQuery] = useState<string | null>(null);

  useEffect(() => {
    initialLoad();
  }, []);

  const initialLoad = async () => {
    setLoading(true);
    setError(null);
    try {
      // 1. Fetch gameweeks to know phases and default home
      let defaultGwId: number | null = null;
      try {
        const gwRes = await fetch('/api/gameweeks');
        if (gwRes.ok) {
          const gwList: GameweekDto[] = await gwRes.json();
          setGameweeks(gwList);
          const defaultGw = gwList.find((g) => g.isDefaultHome);
          if (defaultGw) {
            defaultGwId = defaultGw.id;
          }
        }
      } catch (e) {
        console.warn('Could not load gameweek phases:', e);
      }

      // 2. Fetch squad for default or current gameweek
      await fetchTeamData(defaultGwId);
    } finally {
      setLoading(false);
    }
  };

  const fetchTeamData = async (gwId?: number | null) => {
    setError(null);
    try {
      const url = gwId ? `/api/team/gameweek/${gwId}` : '/api/team/current';
      const res = await fetch(url);
      if (!res.ok) {
        if (res.status === 404) {
          setError('No squad data found yet. Click "Sync FPL Data" above to pull your live team.');
        } else {
          throw new Error(`Failed to load team data (${res.status})`);
        }
        setTeam(null);
      } else {
        const data: TeamCurrentResponse = await res.json();
        setTeam(data);
        setSelectedGw(data.gameweekId);
      }
    } catch (err: any) {
      setError(err.message || 'An error occurred loading your team.');
    }
  };

  const handleSelectGameweek = async (gwId: number) => {
    setSelectedGw(gwId);
    await fetchTeamData(gwId);
  };

  const handleJumpFromHistory = async (gwId: number) => {
    setActiveTab('pitch');
    setSelectedGw(gwId);
    await fetchTeamData(gwId);
  };

  const handleSync = async () => {
    setIsSyncing(true);
    setError(null);
    try {
      const parsedId = parseInt(teamId, 10);
      const url = parsedId && !isNaN(parsedId) ? `/api/team/sync?teamId=${parsedId}&forceHistory=true` : '/api/team/sync?forceHistory=true';
      if (parsedId && !isNaN(parsedId)) {
        localStorage.setItem('fpl_team_id', String(parsedId));
      }
      const res = await fetch(url, { method: 'POST' });
      if (!res.ok) throw new Error('FPL Sync failed');
      await initialLoad();
    } catch (err: any) {
      setError(err.message || 'Sync failed');
    } finally {
      setIsSyncing(false);
    }
  };

  const handleOpenChatWithQuery = (prompt: string) => {
    setChatInitialQuery(prompt);
    setIsChatOpen(true);
  };

  return (
    <div style={{ minHeight: '100vh', display: 'flex', flexDirection: 'column', position: 'relative' }}>
      {/* App Header */}
      <Header
        team={team}
        onSync={handleSync}
        isSyncing={isSyncing}
        teamId={teamId}
        setTeamId={setTeamId}
      />

      {/* Main Container */}
      <main style={{ flex: 1, padding: '1.5rem 2rem', maxWidth: 1200, width: '100%', margin: '0 auto' }}>
        {/* Navigation Tabs */}
        <div style={{ display: 'flex', justifyContent: 'center', marginBottom: '1.5rem' }}>
          <div className="nav-tabs" style={{ flexWrap: 'wrap', justifyContent: 'center' }}>
            <button
              id="tab-pitch"
              className={`tab-btn ${activeTab === 'pitch' ? 'active' : ''}`}
              onClick={() => setActiveTab('pitch')}
            >
              <LayoutGrid size={16} /> My Squad
            </button>
            <button
              id="tab-history"
              className={`tab-btn ${activeTab === 'history' ? 'active' : ''}`}
              onClick={() => setActiveTab('history')}
            >
              <TrendingUp size={16} /> Performance History
            </button>
            <button
              id="tab-recommendations"
              className={`tab-btn ${activeTab === 'recommendations' ? 'active' : ''}`}
              onClick={() => setActiveTab('recommendations')}
            >
              <Sparkles size={16} /> AI Recommendations
            </button>
            <button
              id="tab-chips"
              className={`tab-btn ${activeTab === 'chips' ? 'active' : ''}`}
              onClick={() => setActiveTab('chips')}
            >
              <Layers size={16} /> Chip Strategy
            </button>
            <button
              id="tab-fixtures"
              className={`tab-btn ${activeTab === 'fixtures' ? 'active' : ''}`}
              onClick={() => setActiveTab('fixtures')}
            >
              <Calendar size={16} /> Fixture Matrix
            </button>
            <button
              id="tab-directory"
              className={`tab-btn ${activeTab === 'directory' ? 'active' : ''}`}
              onClick={() => setActiveTab('directory')}
            >
              <Users size={16} /> Player Directory
            </button>
          </div>
        </div>

        {/* Error / Alert Banner */}
        {error && (
          <div className="glass-panel" style={{
            padding: '1rem 1.5rem',
            marginBottom: '1.5rem',
            background: 'rgba(255, 23, 68, 0.1)',
            borderColor: 'rgba(255, 23, 68, 0.3)',
            display: 'flex',
            alignItems: 'center',
            gap: '0.8rem'
          }}>
            <AlertCircle size={20} color="#ff1744" />
            <span style={{ color: '#f8fafc', fontSize: '0.9rem' }}>{error}</span>
          </div>
        )}

        {/* Loading Spinner */}
        {loading && (
          <div style={{ textAlign: 'center', padding: '4rem 0', color: 'var(--text-secondary)' }}>
            <div className="pulse" style={{ fontSize: '2rem', marginBottom: '0.5rem' }}>⚽</div>
            Loading squad intelligence...
          </div>
        )}

        {/* Tab 1: Pitch View */}
        {!loading && activeTab === 'pitch' && (
          team ? (
            <div>
              {gameweeks.length > 0 && selectedGw && (
                <GameweekSelector
                  gameweeks={gameweeks}
                  selectedGw={selectedGw}
                  onSelectGw={handleSelectGameweek}
                />
              )}
              <PitchView team={team} />
            </div>
          ) : (
            <div className="glass-panel" style={{ textAlign: 'center', padding: '3rem 1.5rem', maxWidth: 600, margin: '2rem auto' }}>
              <div style={{ fontSize: '3rem', marginBottom: '1rem' }}>🏆</div>
              <h3 style={{ fontSize: '1.2rem', marginBottom: '0.5rem' }}>Welcome to FPL Assistant</h3>
              <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem', marginBottom: '1.5rem' }}>
                Pull your official FPL team and Premier League fixtures to inspect your squad and FDR runs.
              </p>
              <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', gap: '0.8rem', marginBottom: '1rem' }}>
                <span style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>FPL Team ID:</span>
                <input
                  type="number"
                  value={teamId}
                  onChange={(e) => setTeamId(e.target.value)}
                  placeholder="3500002"
                  style={{
                    background: 'rgba(0, 0, 0, 0.4)',
                    border: '1px solid var(--border-color)',
                    borderRadius: 'var(--radius-sm)',
                    padding: '0.5rem 0.8rem',
                    color: 'var(--text-primary)',
                    fontSize: '0.9rem',
                    width: '130px'
                  }}
                />
              </div>
              <button id="initial-sync-btn" className="btn btn-primary" onClick={handleSync} disabled={isSyncing}>
                {isSyncing ? 'Syncing Official API...' : 'Sync FPL Data Now'}
              </button>
            </div>
          )
        )}

        {/* Tab 2: Performance History View */}
        {!loading && activeTab === 'history' && (
          <HistoryView onSelectGameweek={handleJumpFromHistory} />
        )}

        {/* Tab 3: AI Recommendations View */}
        {!loading && activeTab === 'recommendations' && (
          <RecommendationsView currentGw={team?.gameweekId || 1} />
        )}

        {/* Tab 4: Chip Strategy View */}
        {!loading && activeTab === 'chips' && (
          <ChipStrategyView currentGw={team?.gameweekId || 1} />
        )}

        {/* Tab 5: Fixture Matrix */}
        {!loading && activeTab === 'fixtures' && (
          <FixtureMatrix currentGw={team?.gameweekId || 1} />
        )}

        {/* Tab 6: Player Directory */}
        {!loading && activeTab === 'directory' && (
          <PlayerExplorer onAskAi={handleOpenChatWithQuery} />
        )}
      </main>

      {/* Floating AI Chat Assistant Trigger Button */}
      <button
        id="ai-chat-btn"
        className="btn btn-primary"
        onClick={() => {
          setChatInitialQuery(null);
          setIsChatOpen(!isChatOpen);
        }}
        style={{
          position: 'fixed',
          bottom: 24,
          right: 24,
          borderRadius: 30,
          padding: '0.8rem 1.2rem',
          boxShadow: '0 8px 30px rgba(0, 255, 135, 0.4)',
          zIndex: 999
        }}
      >
        <MessageSquare size={18} />
        Ask AI Assistant
      </button>

      {/* AI Chat Modal */}
      <AiChatModal
        isOpen={isChatOpen}
        onClose={() => {
          setIsChatOpen(false);
          setChatInitialQuery(null);
        }}
        initialQuery={chatInitialQuery}
      />

      {/* Footer */}
      <footer style={{
        padding: '1rem 2rem',
        borderTop: '1px solid var(--border-color)',
        textAlign: 'center',
        fontSize: '0.8rem',
        color: 'var(--text-muted)'
      }}>
        Personal FPL Assistant — Zero-cost, local-first decision support system.
      </footer>
    </div>
  );
}

export default App;
