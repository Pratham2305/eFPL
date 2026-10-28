import React from 'react';
import { TeamCurrentResponse } from '../types';
import { Award, TrendingUp, AlertTriangle, Zap, Armchair } from 'lucide-react';

interface GameweekSummaryBarProps {
  team: TeamCurrentResponse;
}

const GameweekSummaryBar: React.FC<GameweekSummaryBarProps> = ({ team }) => {
  const isFinished = team.phase === 'FINISHED';
  const isLive = team.phase === 'LIVE';
  const isProjected = team.isProjected;

  const hitsCost = team.transfersCost || 0;
  const netPoints = team.gameweekNetPoints !== undefined ? team.gameweekNetPoints : (team.gameweekPoints - hitsCost);

  return (
    <div style={{ marginBottom: '1.2rem' }}>
      {/* Projected Notice */}
      {isProjected && (
        <div style={{
          display: 'flex',
          alignItems: 'center',
          gap: '0.6rem',
          padding: '0.6rem 1rem',
          background: 'rgba(235, 255, 0, 0.08)',
          border: '1px solid rgba(235, 255, 0, 0.25)',
          borderRadius: 'var(--radius-md)',
          color: '#e2f00d',
          fontSize: '0.85rem',
          marginBottom: '0.8rem'
        }}>
          <Zap size={16} />
          <span>
            <strong>Projected Squad:</strong> This pre-deadline lineup carries over your latest settled picks and confirmed transfers. Official picks unlock at the gameweek deadline.
          </span>
        </div>
      )}

      {/* Points Discrepancy Note */}
      {team.pointsDiscrepancy && (
        <div style={{
          display: 'flex',
          alignItems: 'center',
          gap: '0.6rem',
          padding: '0.5rem 1rem',
          background: 'rgba(245, 158, 11, 0.1)',
          border: '1px solid rgba(245, 158, 11, 0.3)',
          borderRadius: 'var(--radius-md)',
          color: 'var(--accent-amber)',
          fontSize: '0.8rem',
          marginBottom: '0.8rem'
        }}>
          <AlertTriangle size={14} />
          <span>Notice: Reconciled starter sum ({team.reconciledGameweekPoints} pts) differs slightly from official event total ({team.gameweekPoints} pts), likely due to auto-subs or live adjustments.</span>
        </div>
      )}

      {/* Summary KPI Strip */}
      <div style={{
        display: 'grid',
        gridTemplateColumns: 'repeat(auto-fit, minmax(130px, 1fr))',
        gap: '0.75rem',
      }}>
        {/* GW Points */}
        <div className="glass-panel" style={{ padding: '0.6rem 0.8rem', textAlign: 'center' }}>
          <div style={{ fontSize: '0.7rem', color: 'var(--text-muted)', textTransform: 'uppercase' }}>
            {isFinished ? 'GW Points' : isLive ? 'Live Points' : 'Projected xP'}
          </div>
          <div style={{ fontSize: '1.4rem', fontWeight: 800, color: 'var(--accent-primary)', marginTop: 2 }}>
            {isFinished || isLive ? (
              <span>
                {netPoints}
                {hitsCost > 0 && (
                  <span style={{ fontSize: '0.75rem', color: '#ff1744', fontWeight: 600, marginLeft: 4 }}>
                    ({team.gameweekPoints} - {hitsCost})
                  </span>
                )}
              </span>
            ) : (
              <span>
                {team.startingXI.reduce((acc, p) => acc + (p.expectedPoints || p.form || 0), 0).toFixed(1)}
              </span>
            )}
          </div>
        </div>

        {/* GW Rank */}
        {(isFinished || isLive) && team.overallRank && (
          <div className="glass-panel" style={{ padding: '0.6rem 0.8rem', textAlign: 'center' }}>
            <div style={{ fontSize: '0.7rem', color: 'var(--text-muted)', textTransform: 'uppercase', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 4 }}>
              <TrendingUp size={12} /> Overall Rank
            </div>
            <div style={{ fontSize: '1.2rem', fontWeight: 700, color: 'var(--text-primary)', marginTop: 2 }}>
              {team.overallRank.toLocaleString()}
            </div>
          </div>
        )}

        {/* Bench Points */}
        {(isFinished || isLive) && (
          <div className="glass-panel" style={{ padding: '0.6rem 0.8rem', textAlign: 'center' }}>
            <div style={{ fontSize: '0.7rem', color: 'var(--text-muted)', textTransform: 'uppercase', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 4 }}>
              <Armchair size={12} /> Bench Points
            </div>
            <div style={{ fontSize: '1.2rem', fontWeight: 700, color: 'var(--text-secondary)', marginTop: 2 }}>
              {team.pointsOnBench ?? 0} pts
            </div>
          </div>
        )}

        {/* Average Entry Score */}
        {team.averageEntryScore !== undefined && team.averageEntryScore !== null && team.averageEntryScore > 0 && (
          <div className="glass-panel" style={{ padding: '0.6rem 0.8rem', textAlign: 'center' }}>
            <div style={{ fontSize: '0.7rem', color: 'var(--text-muted)', textTransform: 'uppercase' }}>
              League Avg
            </div>
            <div style={{ fontSize: '1.2rem', fontWeight: 700, color: 'var(--text-secondary)', marginTop: 2 }}>
              {team.averageEntryScore} pts
            </div>
          </div>
        )}

        {/* Highest Score */}
        {team.highestScore !== undefined && team.highestScore !== null && team.highestScore > 0 && (
          <div className="glass-panel" style={{ padding: '0.6rem 0.8rem', textAlign: 'center' }}>
            <div style={{ fontSize: '0.7rem', color: 'var(--text-muted)', textTransform: 'uppercase' }}>
              Top Score
            </div>
            <div style={{ fontSize: '1.2rem', fontWeight: 700, color: 'var(--accent-amber)', marginTop: 2 }}>
              {team.highestScore} pts
            </div>
          </div>
        )}

        {/* Active Chip */}
        {team.activeChip && (
          <div className="glass-panel" style={{
            padding: '0.6rem 0.8rem',
            textAlign: 'center',
            borderColor: 'var(--accent-primary)',
            background: 'rgba(0, 255, 135, 0.08)'
          }}>
            <div style={{ fontSize: '0.7rem', color: 'var(--accent-primary)', textTransform: 'uppercase', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 4 }}>
              <Award size={12} /> Chip Played
            </div>
            <div style={{ fontSize: '1rem', fontWeight: 800, color: 'var(--accent-primary)', marginTop: 2 }}>
              {team.activeChip.toUpperCase()}
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default GameweekSummaryBar;
