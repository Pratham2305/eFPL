import React from 'react';
import { Search, X, RotateCcw } from 'lucide-react';
import { PlayerMetaDto } from '../../types';
import ClubBadge from '../ClubBadge';

export interface FilterState {
  search: string;
  positions: number[]; // element_type 1, 2, 3, 4
  teams: number[];
  minPrice: string;
  maxPrice: string;
  status: string; // 'all', 'a', 'd_i'
  minOwnership: string;
  maxOwnership: string;
  inSquad: 'all' | 'true' | 'false';
}

interface PlayerFiltersProps {
  filters: FilterState;
  meta: PlayerMetaDto | null;
  onFilterChange: <K extends keyof FilterState>(key: K, value: FilterState[K]) => void;
  onReset: () => void;
}

export const PlayerFilters: React.FC<PlayerFiltersProps> = ({
  filters,
  meta,
  onFilterChange,
  onReset
}) => {
  const togglePosition = (posType: number) => {
    if (posType === 0) {
      onFilterChange('positions', []);
      return;
    }
    const current = [...filters.positions];
    const idx = current.indexOf(posType);
    if (idx >= 0) {
      current.splice(idx, 1);
    } else {
      current.push(posType);
    }
    onFilterChange('positions', current);
  };

  const toggleTeam = (teamId: number) => {
    const current = [...filters.teams];
    const idx = current.indexOf(teamId);
    if (idx >= 0) {
      current.splice(idx, 1);
    } else {
      current.push(teamId);
    }
    onFilterChange('teams', current);
  };

  const hasActiveFilters =
    filters.search !== '' ||
    filters.positions.length > 0 ||
    filters.teams.length > 0 ||
    filters.minPrice !== '' ||
    filters.maxPrice !== '' ||
    filters.status !== 'all' ||
    filters.minOwnership !== '' ||
    filters.maxOwnership !== '' ||
    filters.inSquad !== 'all';

  return (
    <div className="glass-panel" style={{ padding: '1.2rem', marginBottom: '1.2rem', display: 'flex', flexDirection: 'column', gap: '1rem' }}>
      {/* Row 1: Search & Positions & In-Squad Toggle */}
      <div style={{ display: 'flex', gap: '1rem', flexWrap: 'wrap', alignItems: 'center', justifyContent: 'space-between' }}>
        {/* Search */}
        <div style={{ position: 'relative', flex: '1 1 280px', minWidth: 240 }}>
          <Search size={16} style={{ position: 'absolute', left: 12, top: 11, color: 'var(--text-muted)' }} />
          <input
            type="text"
            placeholder="Search by name, club (e.g. Saka, Ødegaard, Arsenal)..."
            value={filters.search}
            onChange={(e) => onFilterChange('search', e.target.value)}
            style={{
              width: '100%',
              padding: '0.55rem 2.2rem 0.55rem 2.3rem',
              borderRadius: 'var(--radius-sm, 6px)',
              background: 'rgba(0, 0, 0, 0.45)',
              border: '1px solid var(--border-color)',
              color: '#fff',
              fontSize: '0.85rem'
            }}
          />
          {filters.search && (
            <button
              onClick={() => onFilterChange('search', '')}
              style={{
                position: 'absolute',
                right: 10,
                top: 10,
                background: 'none',
                border: 'none',
                color: 'var(--text-muted)',
                cursor: 'pointer'
              }}
            >
              <X size={15} />
            </button>
          )}
        </div>

        {/* Position Chips */}
        <div style={{ display: 'flex', gap: '0.4rem', alignItems: 'center', flexWrap: 'wrap' }}>
          <span style={{ fontSize: '0.78rem', color: 'var(--text-secondary)', marginRight: '0.2rem' }}>POS:</span>
          <button
            onClick={() => togglePosition(0)}
            className={`btn ${filters.positions.length === 0 ? 'btn-primary' : 'btn-secondary'}`}
            style={{ padding: '0.3rem 0.65rem', fontSize: '0.75rem' }}
          >
            ALL
          </button>
          {[
            { type: 1, label: 'GKP' },
            { type: 2, label: 'DEF' },
            { type: 3, label: 'MID' },
            { type: 4, label: 'FWD' }
          ].map((pos) => {
            const isSelected = filters.positions.includes(pos.type);
            return (
              <button
                key={pos.type}
                onClick={() => togglePosition(pos.type)}
                className={`btn ${isSelected ? 'btn-primary' : 'btn-secondary'}`}
                style={{ padding: '0.3rem 0.65rem', fontSize: '0.75rem' }}
              >
                {pos.label}
              </button>
            );
          })}
        </div>

        {/* Squad Filter Toggle */}
        <div style={{ display: 'flex', gap: '0.35rem', alignItems: 'center' }}>
          <span style={{ fontSize: '0.78rem', color: 'var(--text-secondary)' }}>SQUAD:</span>
          <select
            value={filters.inSquad}
            onChange={(e) => onFilterChange('inSquad', e.target.value as FilterState['inSquad'])}
            style={{
              background: 'rgba(0, 0, 0, 0.4)',
              border: '1px solid var(--border-color)',
              color: '#fff',
              borderRadius: 6,
              padding: '0.35rem 0.6rem',
              fontSize: '0.8rem'
            }}
          >
            <option value="all">All PL Players</option>
            <option value="true">In My Squad Only</option>
            <option value="false">Not In Squad</option>
          </select>
        </div>
      </div>

      {/* Row 2: Club Badge Filter Bar (scrollable horizontally) */}
      {meta?.teams && (
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', overflowX: 'auto', paddingBottom: '0.3rem' }}>
          <span style={{ fontSize: '0.75rem', color: 'var(--text-secondary)', flexShrink: 0 }}>CLUBS:</span>
          {meta.teams.map((t) => {
            const isSelected = filters.teams.includes(t.id);
            return (
              <button
                key={t.id}
                onClick={() => toggleTeam(t.id)}
                title={t.name}
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  gap: '0.3rem',
                  padding: '0.25rem 0.55rem',
                  borderRadius: 14,
                  background: isSelected ? 'rgba(0, 255, 135, 0.18)' : 'rgba(255, 255, 255, 0.05)',
                  border: isSelected ? '1px solid var(--accent-primary)' : '1px solid rgba(255, 255, 255, 0.1)',
                  color: isSelected ? '#fff' : 'var(--text-secondary)',
                  cursor: 'pointer',
                  flexShrink: 0,
                  fontSize: '0.74rem',
                  fontWeight: isSelected ? 700 : 500
                }}
              >
                <ClubBadge team={t.shortName} size={15} />
                <span>{t.shortName}</span>
              </button>
            );
          })}
        </div>
      )}

      {/* Row 3: Price, Status, Ownership, & Reset button */}
      <div style={{ display: 'flex', gap: '1.2rem', flexWrap: 'wrap', alignItems: 'center', justifyContent: 'space-between', borderTop: '1px solid rgba(255, 255, 255, 0.06)', paddingTop: '0.8rem' }}>
        <div style={{ display: 'flex', gap: '1rem', flexWrap: 'wrap', alignItems: 'center' }}>
          {/* Price Range */}
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', fontSize: '0.8rem' }}>
            <span style={{ color: 'var(--text-secondary)' }}>Price (£m):</span>
            <input
              type="number"
              step="0.5"
              min="3.5"
              max="16.0"
              placeholder="Min"
              value={filters.minPrice}
              onChange={(e) => onFilterChange('minPrice', e.target.value)}
              style={{
                width: 60,
                padding: '0.25rem 0.4rem',
                borderRadius: 4,
                background: 'rgba(0,0,0,0.4)',
                border: '1px solid var(--border-color)',
                color: '#fff',
                fontSize: '0.8rem'
              }}
            />
            <span style={{ color: 'var(--text-muted)' }}>–</span>
            <input
              type="number"
              step="0.5"
              min="3.5"
              max="16.0"
              placeholder="Max"
              value={filters.maxPrice}
              onChange={(e) => onFilterChange('maxPrice', e.target.value)}
              style={{
                width: 60,
                padding: '0.25rem 0.4rem',
                borderRadius: 4,
                background: 'rgba(0,0,0,0.4)',
                border: '1px solid var(--border-color)',
                color: '#fff',
                fontSize: '0.8rem'
              }}
            />
          </div>

          {/* Availability / Status */}
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', fontSize: '0.8rem' }}>
            <span style={{ color: 'var(--text-secondary)' }}>Status:</span>
            <select
              value={filters.status}
              onChange={(e) => onFilterChange('status', e.target.value)}
              style={{
                background: 'rgba(0, 0, 0, 0.4)',
                border: '1px solid var(--border-color)',
                color: '#fff',
                borderRadius: 6,
                padding: '0.25rem 0.5rem',
                fontSize: '0.8rem'
              }}
            >
              <option value="all">All</option>
              <option value="a">Available (Fit)</option>
              <option value="d_i">Doubtful / Injured</option>
            </select>
          </div>

          {/* Ownership */}
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', fontSize: '0.8rem' }}>
            <span style={{ color: 'var(--text-secondary)' }}>Ownership %:</span>
            <input
              type="number"
              min="0"
              max="100"
              placeholder="Min %"
              value={filters.minOwnership}
              onChange={(e) => onFilterChange('minOwnership', e.target.value)}
              style={{
                width: 65,
                padding: '0.25rem 0.4rem',
                borderRadius: 4,
                background: 'rgba(0,0,0,0.4)',
                border: '1px solid var(--border-color)',
                color: '#fff',
                fontSize: '0.8rem'
              }}
            />
            <span style={{ color: 'var(--text-muted)' }}>–</span>
            <input
              type="number"
              min="0"
              max="100"
              placeholder="Max %"
              value={filters.maxOwnership}
              onChange={(e) => onFilterChange('maxOwnership', e.target.value)}
              style={{
                width: 65,
                padding: '0.25rem 0.4rem',
                borderRadius: 4,
                background: 'rgba(0,0,0,0.4)',
                border: '1px solid var(--border-color)',
                color: '#fff',
                fontSize: '0.8rem'
              }}
            />
          </div>
        </div>

        {/* Reset */}
        {hasActiveFilters && (
          <button
            onClick={onReset}
            style={{
              background: 'none',
              border: 'none',
              color: 'var(--accent-cyan)',
              cursor: 'pointer',
              display: 'flex',
              alignItems: 'center',
              gap: '0.3rem',
              fontSize: '0.78rem',
              padding: '0.2rem 0.5rem'
            }}
          >
            <RotateCcw size={13} /> Reset Filters
          </button>
        )}
      </div>
    </div>
  );
};

export default PlayerFilters;
