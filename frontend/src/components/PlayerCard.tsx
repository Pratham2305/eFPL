import React from 'react';
import { PickDto } from '../types';
import { ShieldAlert, Activity, ArrowRightLeft } from 'lucide-react';
import ClubBadge from './ClubBadge';

interface PlayerCardProps {
  player: PickDto;
  isBench?: boolean;
  phase?: string;
}

const PlayerCard: React.FC<PlayerCardProps> = ({ player, isBench = false, phase = 'FINISHED' }) => {
  const priceFormatted = (player.nowCost / 10).toFixed(1);
  const isDoubtful = player.status === 'd' || player.status === 'i';
  const showMatchPoints = phase === 'FINISHED' || phase === 'LIVE';

  const effectivePts = player.effectivePoints ?? ((player.rawPoints ?? 0) * (player.multiplier || 1));
  const rawPts = player.rawPoints ?? 0;
  const isCaptain = player.isCaptain || (player.multiplier && player.multiplier > 1);
  const isTripleCaptain = player.multiplier === 3;

  return (
    <div
      className="glass-panel"
      style={{
        width: isBench ? 132 : 144,
        padding: '0.6rem 0.5rem',
        textAlign: 'center',
        position: 'relative',
        background: player.isAutoSubOut
          ? 'rgba(15, 23, 42, 0.55)'
          : isBench
          ? 'rgba(15, 23, 42, 0.92)'
          : 'rgba(22, 30, 49, 0.88)',
        opacity: player.isAutoSubOut ? 0.65 : 1,
        border: isCaptain ? '1px solid var(--accent-amber)' : '1px solid var(--border-color)',
        boxShadow: isCaptain ? '0 0 16px rgba(251, 191, 36, 0.3)' : 'none',
        transition: 'transform 0.2s ease',
      }}
    >
      {/* Captain / Vice Captain Badge */}
      {isCaptain && (
        <span
          title={isTripleCaptain ? 'Triple Captain (3x points)' : 'Captain (2x points)'}
          style={{
            position: 'absolute',
            top: -8,
            right: -8,
            width: isTripleCaptain ? 26 : 22,
            height: 22,
            borderRadius: 11,
            background: 'var(--accent-amber)',
            color: '#000',
            fontWeight: 800,
            fontSize: '0.7rem',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            boxShadow: '0 2px 6px rgba(0,0,0,0.5)',
            zIndex: 2
          }}
        >
          {isTripleCaptain ? 'TC' : 'C'}
        </span>
      )}

      {player.isViceCaptain && !isCaptain && (
        <span
          title="Vice Captain"
          style={{
            position: 'absolute',
            top: -8,
            right: -8,
            width: 22,
            height: 22,
            borderRadius: '50%',
            background: '#94a3b8',
            color: '#000',
            fontWeight: 800,
            fontSize: '0.75rem',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            zIndex: 2
          }}
        >
          V
        </span>
      )}

      {/* Auto-Sub Badge Indicator */}
      {player.isAutoSubIn && (
        <div
          title={`Automatically subbed in for ${player.subbedWithPlayerName || 'starter'}`}
          style={{
            position: 'absolute',
            top: -7,
            left: -6,
            background: '#10b981',
            color: '#fff',
            borderRadius: 10,
            padding: '1px 6px',
            fontSize: '0.62rem',
            fontWeight: 700,
            display: 'flex',
            alignItems: 'center',
            gap: 2,
            boxShadow: '0 2px 5px rgba(0,0,0,0.4)',
            zIndex: 2
          }}
        >
          <ArrowRightLeft size={10} /> In
        </div>
      )}

      {player.isAutoSubOut && (
        <div
          title={`Subbed out for ${player.subbedWithPlayerName || 'sub'}`}
          style={{
            position: 'absolute',
            top: -7,
            left: -6,
            background: '#64748b',
            color: '#e2e8f0',
            borderRadius: 10,
            padding: '1px 6px',
            fontSize: '0.62rem',
            fontWeight: 700,
            display: 'flex',
            alignItems: 'center',
            gap: 2,
            boxShadow: '0 2px 5px rgba(0,0,0,0.4)',
            zIndex: 2
          }}
        >
          <ArrowRightLeft size={10} /> Out
        </div>
      )}

      {/* Official Club SVG Crest */}
      <div style={{
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
        height: 34,
        marginBottom: '0.25rem',
      }}>
        <ClubBadge
          team={player.teamCode || player.teamShortName}
          size={30}
          alt={player.teamShortName}
        />
      </div>

      {/* Web Name */}
      <div style={{
        fontWeight: 700,
        fontSize: '0.85rem',
        whiteSpace: 'nowrap',
        overflow: 'hidden',
        textOverflow: 'ellipsis',
        color: 'var(--text-primary)'
      }}>
        {player.webName}
      </div>

      {/* Team & Position */}
      <div style={{
        display: 'flex',
        justifyContent: 'center',
        gap: '0.35rem',
        alignItems: 'center',
        marginTop: 2,
        marginBottom: 4
      }}>
        <span style={{ fontSize: '0.7rem', color: 'var(--accent-cyan)', fontWeight: 600 }}>
          {player.teamShortName}
        </span>
        <span style={{
          fontSize: '0.62rem',
          padding: '1px 4px',
          borderRadius: 3,
          background: 'rgba(255, 255, 255, 0.1)',
          color: 'var(--text-secondary)'
        }}>
          {player.position}
        </span>
      </div>

      {/* Gameweek Points Display */}
      {showMatchPoints ? (
        <div style={{
          margin: '4px 0',
          padding: '3px 4px',
          borderRadius: 4,
          background: isCaptain
            ? 'rgba(251, 191, 36, 0.15)'
            : 'rgba(0, 255, 135, 0.1)',
          border: isCaptain
            ? '1px solid rgba(251, 191, 36, 0.3)'
            : '1px solid rgba(0, 255, 135, 0.2)',
        }}>
          <div style={{
            fontSize: '1rem',
            fontWeight: 800,
            color: isCaptain ? 'var(--accent-amber)' : 'var(--accent-primary)',
            lineHeight: 1.1
          }}>
            {effectivePts} <span style={{ fontSize: '0.7rem', fontWeight: 600 }}>pts</span>
          </div>
          {player.multiplier > 1 && (
            <div style={{ fontSize: '0.65rem', color: 'var(--text-muted)' }}>
              ({rawPts} × {player.multiplier})
            </div>
          )}
        </div>
      ) : (
        /* Projected / xPoints Display for upcoming */
        <div style={{
          margin: '4px 0',
          padding: '3px 4px',
          borderRadius: 4,
          background: 'rgba(235, 255, 0, 0.08)',
          border: '1px solid rgba(235, 255, 0, 0.2)',
        }}>
          <div style={{ fontSize: '0.85rem', fontWeight: 800, color: '#e2f00d' }}>
            {player.expectedPoints ? player.expectedPoints.toFixed(1) : player.form || '0.0'} <span style={{ fontSize: '0.65rem' }}>xP</span>
          </div>
        </div>
      )}

      {/* Mini Match Stats (minutes, goals, assists, bonus) if available */}
      {showMatchPoints && (player.minutes !== undefined && player.minutes > 0) && (
        <div style={{
          display: 'flex',
          justifyContent: 'center',
          gap: '4px',
          alignItems: 'center',
          fontSize: '0.65rem',
          color: 'var(--text-secondary)',
          margin: '2px 0 4px 0',
          flexWrap: 'wrap'
        }}>
          <span>{player.minutes}'</span>
          {player.goals ? <span title={`${player.goals} goal(s)`}>⚽{player.goals}</span> : null}
          {player.assists ? <span title={`${player.assists} assist(s)`}>🎯{player.assists}</span> : null}
          {player.cleanSheets ? <span title="Clean Sheet">🧤CS</span> : null}
          {player.bonus ? <span title={`${player.bonus} bonus pts`}>⭐{player.bonus}</span> : null}
        </div>
      )}

      {/* Form & Price */}
      <div style={{
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
        paddingTop: 3,
        borderTop: '1px solid rgba(255,255,255,0.06)',
        fontSize: '0.7rem',
        color: 'var(--text-muted)'
      }}>
        <span style={{ display: 'flex', alignItems: 'center', gap: 2 }}>
          <Activity size={10} color="var(--accent-primary)" />
          {player.form || '0.0'}
        </span>
        <span style={{ fontWeight: 600, color: '#f1f5f9' }}>
          £{priceFormatted}m
        </span>
      </div>

      {/* Status Warning if injured/doubtful */}
      {isDoubtful && (
        <div style={{
          marginTop: 3,
          fontSize: '0.63rem',
          color: '#ff1744',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          gap: 2
        }} title={player.news || 'Doubtful for next round'}>
          <ShieldAlert size={10} />
          {player.status === 'i' ? 'Injured' : 'Doubtful'}
        </div>
      )}
    </div>
  );
};

export default PlayerCard;
