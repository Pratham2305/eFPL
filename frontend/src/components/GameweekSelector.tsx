import React from 'react';
import { GameweekDto } from '../types';
import { ChevronLeft, ChevronRight, CircleDot, CheckCircle2, Clock } from 'lucide-react';

interface GameweekSelectorProps {
  gameweeks: GameweekDto[];
  selectedGw: number;
  onSelectGw: (gw: number) => void;
}

const GameweekSelector: React.FC<GameweekSelectorProps> = ({
  gameweeks,
  selectedGw,
  onSelectGw
}) => {
  if (!gameweeks || gameweeks.length === 0) return null;

  const currentIndex = gameweeks.findIndex((g) => g.id === selectedGw);
  const currentObj = gameweeks[currentIndex] || gameweeks[0];

  const handlePrev = () => {
    if (currentIndex > 0) {
      onSelectGw(gameweeks[currentIndex - 1].id);
    }
  };

  const handleNext = () => {
    if (currentIndex < gameweeks.length - 1) {
      onSelectGw(gameweeks[currentIndex + 1].id);
    }
  };

  const getPhaseBadge = (phase?: string) => {
    switch (phase) {
      case 'LIVE':
        return (
          <span style={{
            display: 'inline-flex',
            alignItems: 'center',
            gap: 4,
            padding: '2px 8px',
            borderRadius: 12,
            background: 'rgba(0, 255, 135, 0.15)',
            color: 'var(--accent-primary)',
            fontSize: '0.75rem',
            fontWeight: 700,
            border: '1px solid rgba(0, 255, 135, 0.4)'
          }}>
            <CircleDot size={12} className="pulse" /> LIVE MATCHES
          </span>
        );
      case 'FINISHED':
        return (
          <span style={{
            display: 'inline-flex',
            alignItems: 'center',
            gap: 4,
            padding: '2px 8px',
            borderRadius: 12,
            background: 'rgba(56, 189, 248, 0.15)',
            color: '#38bdf8',
            fontSize: '0.75rem',
            fontWeight: 600,
            border: '1px solid rgba(56, 189, 248, 0.3)'
          }}>
            <CheckCircle2 size={12} /> Finished
          </span>
        );
      case 'UPCOMING':
        return (
          <span style={{
            display: 'inline-flex',
            alignItems: 'center',
            gap: 4,
            padding: '2px 8px',
            borderRadius: 12,
            background: 'rgba(235, 255, 0, 0.15)',
            color: '#e2f00d',
            fontSize: '0.75rem',
            fontWeight: 700,
            border: '1px solid rgba(235, 255, 0, 0.4)'
          }}>
            <Clock size={12} /> Upcoming (Projected)
          </span>
        );
      default:
        return (
          <span style={{
            padding: '2px 8px',
            borderRadius: 12,
            background: 'rgba(255, 255, 255, 0.08)',
            color: 'var(--text-muted)',
            fontSize: '0.75rem'
          }}>
            Future
          </span>
        );
    }
  };

  return (
    <div style={{
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'space-between',
      gap: '1rem',
      flexWrap: 'wrap',
      marginBottom: '1rem',
      padding: '0.6rem 1rem',
      background: 'rgba(15, 23, 42, 0.7)',
      borderRadius: 'var(--radius-md)',
      border: '1px solid var(--border-color)'
    }}>
      {/* Prev / Next controls */}
      <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
        <button
          className="btn"
          style={{ padding: '0.35rem 0.6rem', fontSize: '0.8rem' }}
          onClick={handlePrev}
          disabled={currentIndex <= 0}
          title="Previous Gameweek"
        >
          <ChevronLeft size={16} />
        </button>

        <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem' }}>
          <select
            value={selectedGw}
            onChange={(e) => onSelectGw(Number(e.target.value))}
            style={{
              background: 'rgba(0, 0, 0, 0.5)',
              border: '1px solid var(--border-color)',
              color: 'var(--text-primary)',
              borderRadius: 'var(--radius-sm)',
              padding: '0.35rem 0.75rem',
              fontWeight: 700,
              fontSize: '0.95rem',
              cursor: 'pointer'
            }}
          >
            {gameweeks.map((gw) => (
              <option key={gw.id} value={gw.id}>
                {gw.name} {gw.phase === 'LIVE' ? '🔴 [LIVE]' : gw.phase === 'FINISHED' ? '✓' : gw.isDefaultHome ? '★ (Next)' : ''}
              </option>
            ))}
          </select>

          {getPhaseBadge(currentObj?.phase)}
        </div>

        <button
          className="btn"
          style={{ padding: '0.35rem 0.6rem', fontSize: '0.8rem' }}
          onClick={handleNext}
          disabled={currentIndex >= gameweeks.length - 1}
          title="Next Gameweek"
        >
          <ChevronRight size={16} />
        </button>
      </div>

      {/* Deadline / Checked note */}
      <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
        {currentObj?.deadlineTime && (
          <span>
            Deadline: {new Date(currentObj.deadlineTime).toLocaleDateString(undefined, {
              weekday: 'short', month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit'
            })}
          </span>
        )}
      </div>
    </div>
  );
};

export default GameweekSelector;
