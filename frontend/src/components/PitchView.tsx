import React from 'react';
import { TeamCurrentResponse } from '../types';
import PlayerCard from './PlayerCard';

interface PitchViewProps {
  team: TeamCurrentResponse;
}

const PitchView: React.FC<PitchViewProps> = ({ team }) => {
  const { startingXI, bench } = team;

  // Group starting XI by position
  const gks = startingXI.filter((p) => p.position === 'GK');
  const defs = startingXI.filter((p) => p.position === 'DEF');
  const mids = startingXI.filter((p) => p.position === 'MID');
  const fwds = startingXI.filter((p) => p.position === 'FWD');

  return (
    <div style={{ maxWidth: 900, margin: '0 auto' }}>
      {/* Tactical Pitch Header */}
      <div style={{
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
        marginBottom: '1rem'
      }}>
        <h2 style={{ fontSize: '1.2rem', fontWeight: 700, display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <span>📋</span> Gameweek {team.gameweekId} Squad XI
        </h2>
        <span style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>
          Formation: {defs.length}-{mids.length}-{fwds.length}
        </span>
      </div>

      {/* Pitch Layout */}
      <div className="pitch-container">
        {/* Pitch Lines */}
        <div className="pitch-line pitch-center-line" />
        <div className="pitch-line pitch-circle" />

        {/* GK Row */}
        <div className="pitch-row">
          {gks.map((player) => (
            <PlayerCard key={player.playerId} player={player} />
          ))}
        </div>

        {/* DEF Row */}
        <div className="pitch-row">
          {defs.map((player) => (
            <PlayerCard key={player.playerId} player={player} />
          ))}
        </div>

        {/* MID Row */}
        <div className="pitch-row">
          {mids.map((player) => (
            <PlayerCard key={player.playerId} player={player} />
          ))}
        </div>

        {/* FWD Row */}
        <div className="pitch-row" style={{ marginBottom: 0 }}>
          {fwds.map((player) => (
            <PlayerCard key={player.playerId} player={player} />
          ))}
        </div>
      </div>

      {/* Bench Shelf */}
      <div className="bench-shelf">
        <div style={{
          fontSize: '0.8rem',
          fontWeight: 700,
          color: 'var(--text-secondary)',
          marginBottom: '0.8rem',
          textTransform: 'uppercase',
          letterSpacing: '0.05em'
        }}>
          🪑 Substitute Bench
        </div>

        <div style={{
          display: 'flex',
          justifyContent: 'center',
          gap: '1rem',
          flexWrap: 'wrap'
        }}>
          {bench.map((player, idx) => (
            <div key={player.playerId} style={{ display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
              <span style={{ fontSize: '0.65rem', color: 'var(--text-muted)', marginBottom: 2 }}>
                {idx === 0 ? 'GK Bench' : `Bench #${idx}`}
              </span>
              <PlayerCard player={player} isBench />
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};

export default PitchView;
