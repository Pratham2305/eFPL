import React from 'react';
import { RefreshCw, Trophy, Wallet, ArrowRightLeft } from 'lucide-react';
import { TeamCurrentResponse } from '../types';
import ChipBadges from './ChipBadges';

interface HeaderProps {
  team: TeamCurrentResponse | null;
  onSync: () => void;
  isSyncing: boolean;
  teamId: string;
  setTeamId: (id: string) => void;
}

const Header: React.FC<HeaderProps> = ({ team, onSync, isSyncing, teamId, setTeamId }) => {
  const bankFormatted = team ? (team.bank / 10).toFixed(1) : '0.0';
  const valueFormatted = team ? (team.teamValue / 10).toFixed(1) : '0.0';

  return (
    <header className="app-header glass-panel">
      <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
        <div style={{
          width: 44,
          height: 44,
          borderRadius: 12,
          background: 'linear-gradient(135deg, #00ff87 0%, #38003c 100%)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          fontSize: '1.4rem'
        }}>
          ⚽
        </div>
        <div>
          <h1 style={{ fontSize: '1.4rem', fontWeight: 800, background: 'linear-gradient(90deg, #fff, #94a3b8)', WebkitBackgroundClip: 'text', WebkitTextFillColor: 'transparent' }}>
            FPL Manager Assistant
          </h1>
          <p style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>
            Data-Driven ML Decisions & Intelligence
          </p>
        </div>
      </div>

      {/* Team Metrics & Stat Pills */}
      {team && (
        <div style={{ display: 'flex', alignItems: 'center', gap: '1.2rem', flexWrap: 'wrap' }}>
          <div className="glass-panel" style={{ padding: '0.5rem 1rem', display: 'flex', alignItems: 'center', gap: '0.6rem' }}>
            <Trophy size={18} color="var(--accent-amber)" />
            <div>
              <div style={{ fontSize: '0.7rem', color: 'var(--text-muted)' }}>OVERALL RANK</div>
              <div style={{ fontWeight: 700, fontSize: '0.95rem' }}>#{team.overallRank?.toLocaleString() || 'N/A'}</div>
            </div>
          </div>

          <div className="glass-panel" style={{ padding: '0.5rem 1rem', display: 'flex', alignItems: 'center', gap: '0.6rem' }}>
            <Wallet size={18} color="var(--accent-primary)" />
            <div>
              <div style={{ fontSize: '0.7rem', color: 'var(--text-muted)' }}>BANK / VALUE</div>
              <div style={{ fontWeight: 700, fontSize: '0.95rem' }}>£{bankFormatted}m / £{valueFormatted}m</div>
            </div>
          </div>

          <div className="glass-panel" style={{ padding: '0.5rem 1rem', display: 'flex', alignItems: 'center', gap: '0.6rem' }}>
            <ArrowRightLeft size={18} color="var(--accent-cyan)" />
            <div>
              <div style={{ fontSize: '0.7rem', color: 'var(--text-muted)' }}>FREE TRANSFERS</div>
              <div style={{ fontWeight: 700, fontSize: '0.95rem' }}>{team.freeTransfers} Free</div>
            </div>
          </div>

          <div className="glass-panel" style={{ padding: '0.5rem 1rem' }}>
            <div style={{ fontSize: '0.7rem', color: 'var(--text-muted)', marginBottom: 2 }}>CHIPS REMAINING</div>
            <ChipBadges chips={team.chips} />
          </div>
        </div>
      )}

      {/* Team ID & Sync Button */}
      <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem', flexWrap: 'wrap' }}>
        <div style={{
          display: 'flex',
          alignItems: 'center',
          background: 'rgba(0, 0, 0, 0.35)',
          border: '1px solid var(--border-color)',
          borderRadius: 'var(--radius-sm)',
          padding: '0.4rem 0.7rem',
          gap: '0.5rem'
        }}>
          <span style={{ fontSize: '0.75rem', color: 'var(--accent-cyan)', fontWeight: 700 }}>TEAM ID:</span>
          <input
            type="number"
            value={teamId}
            onChange={(e) => setTeamId(e.target.value)}
            placeholder="e.g. 3500002"
            style={{
              background: 'transparent',
              border: 'none',
              outline: 'none',
              color: 'var(--text-primary)',
              fontSize: '0.85rem',
              fontWeight: 600,
              width: '95px'
            }}
          />
        </div>
        <button
          id="sync-btn"
          className="btn btn-primary"
          onClick={onSync}
          disabled={isSyncing}
        >
          <RefreshCw size={16} className={isSyncing ? 'pulse' : ''} />
          {isSyncing ? 'Syncing...' : 'Sync FPL Data'}
        </button>
      </div>
    </header>
  );
};

export default Header;
