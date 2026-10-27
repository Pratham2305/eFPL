import React from 'react';
import { ChevronUp, ChevronDown } from 'lucide-react';
import { PlayerSummaryDto } from '../../types';
import ClubBadge from '../ClubBadge';

export type SortField =
  | 'total_points'
  | 'expected_points'
  | 'form'
  | 'now_cost'
  | 'selected_by_percent'
  | 'goals_scored'
  | 'assists'
  | 'clean_sheets'
  | 'minutes'
  | 'ict_index'
  | 'expected_goals'
  | 'expected_assists'
  | 'expected_goal_involvements'
  | 'web_name';

interface PlayerTableProps {
  players: PlayerSummaryDto[];
  sortBy: string;
  sortDir: 'asc' | 'desc';
  onSort: (field: SortField) => void;
  onSelectPlayer: (id: number) => void;
  loading: boolean;
}

export const PlayerTable: React.FC<PlayerTableProps> = ({
  players,
  sortBy,
  sortDir,
  onSort,
  onSelectPlayer,
  loading
}) => {
  const renderSortArrow = (field: SortField) => {
    if (sortBy !== field) {
      return <span style={{ opacity: 0.2, marginLeft: 4 }}>⇅</span>;
    }
    return sortDir === 'asc' ? (
      <ChevronUp size={14} style={{ display: 'inline', marginLeft: 2, color: 'var(--accent-cyan)' }} />
    ) : (
      <ChevronDown size={14} style={{ display: 'inline', marginLeft: 2, color: 'var(--accent-cyan)' }} />
    );
  };

  const fdrColor = (diff: number | null) => {
    if (diff === null) return { bg: 'rgba(255, 255, 255, 0.08)', text: '#94a3b8' };
    switch (diff) {
      case 1:
      case 2:
        return { bg: 'rgba(16, 185, 129, 0.2)', text: '#34d399', border: 'rgba(16, 185, 129, 0.4)' };
      case 3:
        return { bg: 'rgba(234, 179, 8, 0.2)', text: '#fde047', border: 'rgba(234, 179, 8, 0.4)' };
      case 4:
      case 5:
        return { bg: 'rgba(239, 68, 68, 0.2)', text: '#f87171', border: 'rgba(239, 68, 68, 0.4)' };
      default:
        return { bg: 'rgba(255, 255, 255, 0.08)', text: '#fff', border: 'rgba(255, 255, 255, 0.15)' };
    }
  };

  return (
    <div className="glass-panel" style={{ overflowX: 'auto', padding: '0', position: 'relative' }}>
      {loading && (
        <div style={{
          position: 'absolute',
          top: 0,
          left: 0,
          right: 0,
          bottom: 0,
          background: 'rgba(15, 23, 42, 0.65)',
          backdropFilter: 'blur(3px)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          zIndex: 10,
          color: 'var(--accent-cyan)'
        }}>
          <div className="pulse" style={{ fontSize: '1.2rem', fontWeight: 600 }}>Filtering players...</div>
        </div>
      )}

      <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '0.84rem' }}>
        <thead>
          <tr style={{ borderBottom: '1px solid var(--border-color)', color: 'var(--text-secondary)', background: 'rgba(0, 0, 0, 0.25)' }}>
            <th
              onClick={() => onSort('web_name')}
              style={{ padding: '0.8rem 1rem', cursor: 'pointer', userSelect: 'none', minWidth: 170 }}
            >
              PLAYER {renderSortArrow('web_name')}
            </th>
            <th style={{ padding: '0.8rem 0.6rem' }}>CLUB</th>
            <th style={{ padding: '0.8rem 0.6rem' }}>POS</th>
            <th
              onClick={() => onSort('now_cost')}
              style={{ padding: '0.8rem 0.6rem', cursor: 'pointer', userSelect: 'none' }}
            >
              PRICE {renderSortArrow('now_cost')}
            </th>
            <th style={{ padding: '0.8rem 0.6rem', minWidth: 110 }}>NEXT FIXTURE</th>
            <th
              onClick={() => onSort('expected_points')}
              style={{ padding: '0.8rem 0.6rem', cursor: 'pointer', userSelect: 'none', color: 'var(--accent-cyan)' }}
              title="Next GW Expected Points (ML Model)"
            >
              xP NEXT {renderSortArrow('expected_points')}
            </th>
            <th
              onClick={() => onSort('total_points')}
              style={{ padding: '0.8rem 0.6rem', cursor: 'pointer', userSelect: 'none' }}
            >
              PTS {renderSortArrow('total_points')}
            </th>
            <th
              onClick={() => onSort('form')}
              style={{ padding: '0.8rem 0.6rem', cursor: 'pointer', userSelect: 'none' }}
            >
              FORM {renderSortArrow('form')}
            </th>
            <th
              onClick={() => onSort('selected_by_percent')}
              style={{ padding: '0.8rem 0.6rem', cursor: 'pointer', userSelect: 'none' }}
            >
              OWN% {renderSortArrow('selected_by_percent')}
            </th>
            <th
              onClick={() => onSort('goals_scored')}
              style={{ padding: '0.8rem 0.5rem', cursor: 'pointer', userSelect: 'none' }}
            >
              G {renderSortArrow('goals_scored')}
            </th>
            <th
              onClick={() => onSort('assists')}
              style={{ padding: '0.8rem 0.5rem', cursor: 'pointer', userSelect: 'none' }}
            >
              A {renderSortArrow('assists')}
            </th>
            <th
              onClick={() => onSort('clean_sheets')}
              style={{ padding: '0.8rem 0.5rem', cursor: 'pointer', userSelect: 'none' }}
            >
              CS {renderSortArrow('clean_sheets')}
            </th>
            <th
              onClick={() => onSort('expected_goal_involvements')}
              style={{ padding: '0.8rem 0.6rem', cursor: 'pointer', userSelect: 'none' }}
              title="Expected Goal Involvements"
            >
              xGI {renderSortArrow('expected_goal_involvements')}
            </th>
            <th
              onClick={() => onSort('ict_index')}
              style={{ padding: '0.8rem 0.6rem', cursor: 'pointer', userSelect: 'none' }}
            >
              ICT {renderSortArrow('ict_index')}
            </th>
          </tr>
        </thead>
        <tbody>
          {players.length === 0 && !loading && (
            <tr>
              <td colSpan={14} style={{ padding: '3rem 1rem', textAlign: 'center', color: 'var(--text-muted)' }}>
                No Premier League players found matching the current filters.
              </td>
            </tr>
          )}

          {players.map((player) => {
            const fdr = fdrColor(player.nextOpponentDifficulty);
            return (
              <tr
                key={player.id}
                onClick={() => onSelectPlayer(player.id)}
                className="player-table-row"
                style={{
                  borderBottom: '1px solid rgba(255, 255, 255, 0.04)',
                  cursor: 'pointer',
                  transition: 'background-color 0.15s ease'
                }}
              >
                {/* Player Name & Badges */}
                <td style={{ padding: '0.7rem 1rem' }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                    <div>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '0.35rem' }}>
                        <span style={{ fontWeight: 700, color: '#fff', fontSize: '0.9rem' }}>
                          {player.webName}
                        </span>
                        {player.inSquad && (
                          <span
                            title={player.isStarting ? 'In your Starting XI' : 'On your Bench'}
                            style={{
                              fontSize: '0.66rem',
                              padding: '1px 5px',
                              borderRadius: 4,
                              background: 'rgba(0, 255, 135, 0.2)',
                              color: 'var(--accent-primary)',
                              fontWeight: 700
                            }}
                          >
                            {player.isCaptain ? 'C' : player.isViceCaptain ? 'V' : player.isStarting ? 'XI' : 'SUB'}
                          </span>
                        )}
                        {player.status !== 'a' && (
                          <span
                            title={player.news || 'Flagged'}
                            style={{
                              width: 8,
                              height: 8,
                              borderRadius: '50%',
                              backgroundColor: player.status === 'd' ? '#fbbf24' : '#f87171',
                              display: 'inline-block'
                            }}
                          />
                        )}
                      </div>
                      <div style={{ fontSize: '0.72rem', color: 'var(--text-muted)', marginTop: 1 }}>
                        {player.firstName} {player.secondName}
                      </div>
                    </div>
                  </div>
                </td>

                {/* Club */}
                <td style={{ padding: '0.7rem 0.6rem' }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '0.35rem' }}>
                    <ClubBadge team={player.teamShortName} size={18} alt={player.teamShortName} />
                    <span style={{ fontWeight: 600, color: 'var(--text-secondary)', fontSize: '0.8rem' }}>
                      {player.teamShortName}
                    </span>
                  </div>
                </td>

                {/* Pos */}
                <td style={{ padding: '0.7rem 0.6rem' }}>
                  <span style={{
                    padding: '2px 6px',
                    borderRadius: 4,
                    background: 'rgba(255, 255, 255, 0.08)',
                    fontSize: '0.74rem',
                    fontWeight: 600,
                    color: 'var(--text-secondary)'
                  }}>
                    {player.positionName}
                  </span>
                </td>

                {/* Price */}
                <td style={{ padding: '0.7rem 0.6rem', fontWeight: 600 }}>
                  <div>£{(player.nowCost / 10).toFixed(1)}m</div>
                  {player.costChangeEvent !== 0 && (
                    <div style={{
                      fontSize: '0.65rem',
                      color: player.costChangeEvent > 0 ? '#34d399' : '#f87171'
                    }}>
                      {player.costChangeEvent > 0 ? `+£${player.costChangeEvent / 10}m` : `-£${Math.abs(player.costChangeEvent) / 10}m`}
                    </div>
                  )}
                </td>

                {/* Next Fixture */}
                <td style={{ padding: '0.7rem 0.6rem' }}>
                  {player.isBlankGameweek ? (
                    <span style={{ fontSize: '0.72rem', padding: '2px 6px', borderRadius: 4, background: 'rgba(239, 68, 68, 0.2)', color: '#f87171', fontWeight: 700 }}>
                      BLANK
                    </span>
                  ) : (
                    <span style={{
                      fontSize: '0.75rem',
                      padding: '2px 7px',
                      borderRadius: 4,
                      background: fdr.bg,
                      color: fdr.text,
                      fontWeight: 700,
                      display: 'inline-block',
                      border: fdr.border ? `1px solid ${fdr.border}` : undefined
                    }}>
                      {player.nextOpponentShortName}
                      {player.isDoubleGameweek && <span style={{ marginLeft: 3, color: '#34d399' }}>*</span>}
                    </span>
                  )}
                </td>

                {/* xP Next (Model Projection) */}
                <td style={{ padding: '0.7rem 0.6rem', fontWeight: 700 }}>
                  {player.xpNext !== null ? (
                    <span style={{ color: 'var(--accent-primary)', fontSize: '0.88rem' }}>
                      {player.xpNext}
                    </span>
                  ) : (
                    <span style={{ color: 'var(--text-muted)' }}>—</span>
                  )}
                </td>

                {/* Points */}
                <td style={{ padding: '0.7rem 0.6rem', fontWeight: 700, color: '#fff' }}>
                  {player.totalPoints}
                </td>

                {/* Form */}
                <td style={{ padding: '0.7rem 0.6rem', color: 'var(--accent-primary)', fontWeight: 600 }}>
                  {player.form ?? '—'}
                </td>

                {/* Ownership */}
                <td style={{ padding: '0.7rem 0.6rem', color: 'var(--text-secondary)' }}>
                  {player.selectedByPercent ? `${player.selectedByPercent}%` : '—'}
                </td>

                {/* Stats */}
                <td style={{ padding: '0.7rem 0.5rem', color: 'var(--text-secondary)' }}>
                  {player.goalsScored}
                </td>
                <td style={{ padding: '0.7rem 0.5rem', color: 'var(--text-secondary)' }}>
                  {player.assists}
                </td>
                <td style={{ padding: '0.7rem 0.5rem', color: 'var(--text-secondary)' }}>
                  {player.cleanSheets}
                </td>
                <td style={{ padding: '0.7rem 0.6rem', color: 'var(--text-secondary)' }}>
                  {player.expectedGoalInvolvements ?? '—'}
                </td>
                <td style={{ padding: '0.7rem 0.6rem', color: 'var(--text-secondary)' }}>
                  {player.ictIndex ?? '—'}
                </td>
              </tr>
            );
          })}
        </tbody>
      </table>
    </div>
  );
};

export default PlayerTable;
