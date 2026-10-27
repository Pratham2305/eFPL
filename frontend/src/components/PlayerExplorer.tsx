import React, { useState, useEffect, useCallback, useRef } from 'react';
import { Users, RefreshCw } from 'lucide-react';
import { PlayerPageResponse, PlayerMetaDto } from '../types';
import PlayerFilters, { FilterState } from './players/PlayerFilters';
import PlayerTable, { SortField } from './players/PlayerTable';
import Pagination from './players/Pagination';
import PlayerDetailDrawer from './players/PlayerDetailDrawer';

interface PlayerExplorerProps {
  onAskAi?: (prompt: string) => void;
}

const DEFAULT_FILTERS: FilterState = {
  search: '',
  positions: [],
  teams: [],
  minPrice: '',
  maxPrice: '',
  status: 'all',
  minOwnership: '',
  maxOwnership: '',
  inSquad: 'all'
};

export const PlayerExplorer: React.FC<PlayerExplorerProps> = ({ onAskAi }) => {
  const [filters, setFilters] = useState<FilterState>(DEFAULT_FILTERS);
  const [debouncedSearch, setDebouncedSearch] = useState('');
  const [sortBy, setSortBy] = useState<string>('total_points');
  const [sortDir, setSortDir] = useState<'asc' | 'desc'>('desc');
  const [page, setPage] = useState<number>(0);
  const [size, setSize] = useState<number>(25);

  const [data, setData] = useState<PlayerPageResponse | null>(null);
  const [meta, setMeta] = useState<PlayerMetaDto | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  const [selectedPlayerId, setSelectedPlayerId] = useState<number | null>(null);

  // Debounce search input
  const searchTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  useEffect(() => {
    if (searchTimerRef.current) clearTimeout(searchTimerRef.current);
    searchTimerRef.current = setTimeout(() => {
      setDebouncedSearch(filters.search);
      setPage(0);
    }, 280);
    return () => {
      if (searchTimerRef.current) clearTimeout(searchTimerRef.current);
    };
  }, [filters.search]);

  // Load Meta on Mount
  useEffect(() => {
    fetch('/api/players/meta')
      .then((res) => (res.ok ? res.json() : null))
      .then((metaData: PlayerMetaDto | null) => {
        if (metaData) setMeta(metaData);
      })
      .catch((err) => console.warn('Could not load player metadata:', err));
  }, []);

  // Fetch Players
  const fetchPlayers = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const params = new URLSearchParams();
      if (debouncedSearch.trim()) params.append('search', debouncedSearch.trim());
      if (filters.positions.length > 0) {
        params.append('positions', filters.positions.join(','));
      }
      if (filters.teams.length > 0) {
        params.append('teams', filters.teams.join(','));
      }
      if (filters.minPrice !== '') params.append('minPrice', filters.minPrice);
      if (filters.maxPrice !== '') params.append('maxPrice', filters.maxPrice);

      if (filters.status === 'a') {
        params.append('status', 'a');
      } else if (filters.status === 'd_i') {
        params.append('status', 'd,i,s,u');
      }

      if (filters.minOwnership !== '') params.append('minOwnership', filters.minOwnership);
      if (filters.maxOwnership !== '') params.append('maxOwnership', filters.maxOwnership);

      if (filters.inSquad === 'true') params.append('inSquad', 'true');
      if (filters.inSquad === 'false') params.append('inSquad', 'false');

      params.append('sortBy', sortBy);
      params.append('sortDir', sortDir);
      params.append('page', String(page));
      params.append('size', String(size));

      const res = await fetch(`/api/players?${params.toString()}`);
      if (!res.ok) {
        throw new Error(`Failed to load players (${res.status})`);
      }
      const pageResponse: PlayerPageResponse = await res.json();
      setData(pageResponse);
    } catch (err: any) {
      setError(err.message || 'Error loading players');
    } finally {
      setLoading(false);
    }
  }, [
    debouncedSearch,
    filters.positions,
    filters.teams,
    filters.minPrice,
    filters.maxPrice,
    filters.status,
    filters.minOwnership,
    filters.maxOwnership,
    filters.inSquad,
    sortBy,
    sortDir,
    page,
    size
  ]);

  useEffect(() => {
    fetchPlayers();
  }, [fetchPlayers]);

  const handleFilterChange = <K extends keyof FilterState>(key: K, value: FilterState[K]) => {
    setFilters((prev) => ({ ...prev, [key]: value }));
    setPage(0);
  };

  const handleResetFilters = () => {
    setFilters(DEFAULT_FILTERS);
    setPage(0);
  };

  const handleSort = (field: SortField) => {
    if (sortBy === field) {
      setSortDir((prev) => (prev === 'asc' ? 'desc' : 'asc'));
    } else {
      setSortBy(field);
      setSortDir(field === 'web_name' || field === 'now_cost' ? 'asc' : 'desc');
    }
    setPage(0);
  };

  return (
    <div style={{ maxWidth: 1200, margin: '0 auto', display: 'flex', flexDirection: 'column', gap: '1rem' }}>
      {/* Title & Header */}
      <div style={{
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
        flexWrap: 'wrap',
        gap: '0.8rem'
      }}>
        <div>
          <h2 style={{ fontSize: '1.25rem', fontWeight: 700, display: 'flex', alignItems: 'center', gap: '0.5rem', margin: 0 }}>
            <Users size={22} color="var(--accent-cyan)" />
            Premier League Player Directory
          </h2>
          <p style={{ fontSize: '0.82rem', color: 'var(--text-secondary)', margin: '4px 0 0' }}>
            Comprehensive filterable browser of all Premier League players with model expected points and FDR fixtures
          </p>
        </div>

        <button
          className="btn btn-secondary"
          onClick={fetchPlayers}
          disabled={loading}
          style={{ padding: '0.4rem 0.8rem', fontSize: '0.78rem', display: 'flex', alignItems: 'center', gap: '0.35rem' }}
        >
          <RefreshCw size={13} className={loading ? 'spin' : ''} />
          Refresh
        </button>
      </div>

      {/* Filter Bar */}
      <PlayerFilters
        filters={filters}
        meta={meta}
        onFilterChange={handleFilterChange}
        onReset={handleResetFilters}
      />

      {/* Error notification if any */}
      {error && (
        <div className="glass-panel" style={{ padding: '1rem', color: '#f87171', background: 'rgba(239,68,68,0.1)' }}>
          {error}
        </div>
      )}

      {/* Results Table */}
      <PlayerTable
        players={data?.content || []}
        sortBy={sortBy}
        sortDir={sortDir}
        onSort={handleSort}
        onSelectPlayer={(id) => setSelectedPlayerId(id)}
        loading={loading}
      />

      {/* Pagination Footer */}
      {data && (
        <Pagination
          page={data.page}
          size={data.size}
          totalElements={data.totalElements}
          totalPages={data.totalPages}
          hasNext={data.hasNext}
          hasPrevious={data.hasPrevious}
          onPageChange={(p) => setPage(p)}
          onSizeChange={(s) => {
            setSize(s);
            setPage(0);
          }}
        />
      )}

      {/* Single Player Inspection Drawer */}
      <PlayerDetailDrawer
        playerId={selectedPlayerId}
        onClose={() => setSelectedPlayerId(null)}
        onAskAi={(prompt) => {
          if (onAskAi) onAskAi(prompt);
        }}
      />
    </div>
  );
};

export default PlayerExplorer;
