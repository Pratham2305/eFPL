import React, { useState } from 'react';
import { Search, UserCheck } from 'lucide-react';
import { PickDto } from '../types';

interface PlayerExplorerProps {
  startingXI: PickDto[];
  bench: PickDto[];
}

const PlayerExplorer: React.FC<PlayerExplorerProps> = ({ startingXI, bench }) => {
  const [searchTerm, setSearchTerm] = useState('');
  const [posFilter, setPosFilter] = useState<string>('ALL');

  const allSquad = [...startingXI, ...bench];

  const filtered = allSquad.filter((p) => {
    const matchesSearch = p.webName.toLowerCase().includes(searchTerm.toLowerCase()) ||
                          p.teamShortName.toLowerCase().includes(searchTerm.toLowerCase());
    const matchesPos = posFilter === 'ALL' || p.position === posFilter;
    return matchesSearch && matchesPos;
  });

  return (
    <div style={{ maxWidth: 900, margin: '0 auto' }}>
      {/* Header */}
      <div style={{
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
        marginBottom: '1.2rem',
        flexWrap: 'wrap',
        gap: '1rem'
      }}>
        <div>
          <h2 style={{ fontSize: '1.2rem', fontWeight: 700, display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <UserCheck size={20} color="var(--accent-cyan)" />
            Squad Player Directory
          </h2>
          <p style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>
            Search and inspect current squad performance and price metrics
          </p>
        </div>

        {/* Filter Controls */}
        <div style={{ display: 'flex', gap: '0.6rem', alignItems: 'center' }}>
          <div style={{ position: 'relative' }}>
            <Search size={14} style={{ position: 'absolute', left: 10, top: 10, color: 'var(--text-muted)' }} />
            <input
              type="text"
              placeholder="Search player or team..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              style={{
                padding: '0.5rem 0.6rem 0.5rem 2rem',
                borderRadius: 'var(--radius-sm)',
                background: 'rgba(0,0,0,0.4)',
                border: '1px solid var(--border-color)',
                color: '#fff',
                fontSize: '0.85rem'
              }}
            />
          </div>

          {['ALL', 'GK', 'DEF', 'MID', 'FWD'].map((pos) => (
            <button
              key={pos}
              onClick={() => setPosFilter(pos)}
              className={`btn ${posFilter === pos ? 'btn-primary' : 'btn-secondary'}`}
              style={{ padding: '0.3rem 0.6rem', fontSize: '0.75rem' }}
            >
              {pos}
            </button>
          ))}
        </div>
      </div>

      {/* Table */}
      <div className="glass-panel" style={{ overflowX: 'auto', padding: '1rem' }}>
        <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '0.88rem' }}>
          <thead>
            <tr style={{ borderBottom: '1px solid var(--border-color)', color: 'var(--text-secondary)' }}>
              <th style={{ padding: '0.6rem' }}>PLAYER</th>
              <th style={{ padding: '0.6rem' }}>POS</th>
              <th style={{ padding: '0.6rem' }}>CLUB</th>
              <th style={{ padding: '0.6rem' }}>PRICE</th>
              <th style={{ padding: '0.6rem' }}>FORM</th>
              <th style={{ padding: '0.6rem' }}>TOTAL PTS</th>
              <th style={{ padding: '0.6rem' }}>STATUS</th>
            </tr>
          </thead>
          <tbody>
            {filtered.map((player) => (
              <tr key={player.playerId} style={{ borderBottom: '1px solid rgba(255,255,255,0.04)' }}>
                <td style={{ padding: '0.6rem', fontWeight: 700 }}>
                  {player.webName} {player.isCaptain ? '(C)' : player.isViceCaptain ? '(V)' : ''}
                </td>
                <td style={{ padding: '0.6rem' }}>
                  <span style={{ padding: '2px 6px', borderRadius: 4, background: 'rgba(255,255,255,0.08)', fontSize: '0.75rem' }}>
                    {player.position}
                  </span>
                </td>
                <td style={{ padding: '0.6rem', color: 'var(--accent-cyan)' }}>{player.teamShortName}</td>
                <td style={{ padding: '0.6rem' }}>£{(player.nowCost / 10).toFixed(1)}m</td>
                <td style={{ padding: '0.6rem', color: 'var(--accent-primary)', fontWeight: 600 }}>{player.form}</td>
                <td style={{ padding: '0.6rem', fontWeight: 700 }}>{player.totalPoints}</td>
                <td style={{ padding: '0.6rem' }}>
                  <span className={`status-badge ${player.status === 'a' ? 'status-available' : 'status-injured'}`}>
                    {player.status === 'a' ? 'Available' : player.news || 'Doubtful'}
                  </span>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default PlayerExplorer;
