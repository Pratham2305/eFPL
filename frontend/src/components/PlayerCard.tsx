import React from 'react';
import { PickDto } from '../types';
import { ShieldAlert, Activity } from 'lucide-react';
import ClubBadge from './ClubBadge';

interface PlayerCardProps {
  player: PickDto;
  isBench?: boolean;
}

const PlayerCard: React.FC<PlayerCardProps> = ({ player, isBench = false }) => {
  const priceFormatted = (player.nowCost / 10).toFixed(1);
  const isDoubtful = player.status === 'd' || player.status === 'i';

  return (
    <div
      className="glass-panel"
      style={{
        width: isBench ? 130 : 140,
        padding: '0.6rem',
        textAlign: 'center',
        position: 'relative',
        background: isBench ? 'rgba(15, 23, 42, 0.9)' : 'rgba(22, 30, 49, 0.85)',
        border: player.isCaptain ? '1px solid var(--accent-amber)' : '1px solid var(--border-color)',
        boxShadow: player.isCaptain ? '0 0 15px rgba(251, 191, 36, 0.25)' : 'none',
        transition: 'transform 0.2s ease',
      }}
    >
      {/* Captain / Vice Captain Badge */}
      {player.isCaptain && (
        <span style={{
          position: 'absolute',
          top: -8,
          right: -8,
          width: 22,
          height: 22,
          borderRadius: '50%',
          background: 'var(--accent-amber)',
          color: '#000',
          fontWeight: 800,
          fontSize: '0.75rem',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          boxShadow: '0 2px 6px rgba(0,0,0,0.5)'
        }}>
          C
        </span>
      )}

      {player.isViceCaptain && (
        <span style={{
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
          justifyContent: 'center'
        }}>
          V
        </span>
      )}

      {/* Official Club SVG Crest */}
      <div style={{
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
        height: 36,
        marginBottom: '0.3rem',
      }}>
        <ClubBadge
          team={player.teamCode || player.teamShortName}
          size={32}
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
        gap: '0.4rem',
        alignItems: 'center',
        marginTop: 2,
        marginBottom: 4
      }}>
        <span style={{ fontSize: '0.7rem', color: 'var(--accent-cyan)', fontWeight: 600 }}>
          {player.teamShortName}
        </span>
        <span style={{
          fontSize: '0.65rem',
          padding: '1px 5px',
          borderRadius: 3,
          background: 'rgba(255, 255, 255, 0.1)',
          color: 'var(--text-secondary)'
        }}>
          {player.position}
        </span>
      </div>

      {/* Form & Price */}
      <div style={{
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
        paddingTop: 4,
        borderTop: '1px solid rgba(255,255,255,0.06)',
        fontSize: '0.72rem',
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
          marginTop: 4,
          fontSize: '0.65rem',
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
