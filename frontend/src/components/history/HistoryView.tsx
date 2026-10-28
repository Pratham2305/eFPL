import React, { useState, useEffect } from 'react';
import { PerformanceHistoryResponse, GameweekHistoryDto } from '../../types';
import {
  Trophy, TrendingUp, Award, Armchair, ArrowUpDown,
  Sparkles, CheckCircle2, ChevronRight
} from 'lucide-react';
import {
  ResponsiveContainer,
  ComposedChart,
  LineChart,
  Line,
  Bar,
  XAxis,
  YAxis,
  Tooltip,
  Legend,
  CartesianGrid
} from 'recharts';

interface HistoryViewProps {
  onSelectGameweek: (gwId: number) => void;
}

const HistoryView: React.FC<HistoryViewProps> = ({ onSelectGameweek }) => {
  const [data, setData] = useState<PerformanceHistoryResponse | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [sortField, setSortField] = useState<keyof GameweekHistoryDto>('gameweekId');
  const [sortAsc, setSortAsc] = useState<boolean>(false);

  useEffect(() => {
    fetchHistory();
  }, []);

  const fetchHistory = async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await fetch('/api/team/history');
      if (!res.ok) {
        throw new Error(`Failed to load history data (${res.status})`);
      }
      const json: PerformanceHistoryResponse = await res.json();
      setData(json);
    } catch (err: any) {
      setError(err.message || 'Error loading season history.');
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div style={{ textAlign: 'center', padding: '4rem 0', color: 'var(--text-secondary)' }}>
        <div className="pulse" style={{ fontSize: '2rem', marginBottom: '0.5rem' }}>📈</div>
        Loading season performance history...
      </div>
    );
  }

  if (error || !data || data.gameweeks.length === 0) {
    return (
      <div className="glass-panel" style={{ textAlign: 'center', padding: '3rem 1.5rem', maxWidth: 650, margin: '2rem auto' }}>
        <Trophy size={48} color="var(--accent-amber)" style={{ margin: '0 auto 1rem auto' }} />
        <h3 style={{ fontSize: '1.2rem', marginBottom: '0.5rem' }}>No Performance History Found</h3>
        <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem', marginBottom: '1.5rem' }}>
          {error || 'Sync your official FPL team to backfill and analyze historical gameweek results.'}
        </p>
        <button className="btn btn-primary" onClick={fetchHistory}>
          Retry History Fetch
        </button>
      </div>
    );
  }

  const { summary, gameweeks, deterministicInsights, chipsTimeline } = data;

  // Sorting
  const sortedGameweeks = [...gameweeks].sort((a, b) => {
    const valA = a[sortField];
    const valB = b[sortField];
    if (valA === valB) return 0;
    if (valA === null || valA === undefined) return 1;
    if (valB === null || valB === undefined) return -1;
    if (typeof valA === 'string' && typeof valB === 'string') {
      return sortAsc ? valA.localeCompare(valB) : valB.localeCompare(valA);
    }
    return sortAsc ? (valA < valB ? -1 : 1) : (valA > valB ? -1 : 1);
  });

  const handleSort = (field: keyof GameweekHistoryDto) => {
    if (sortField === field) {
      setSortAsc(!sortAsc);
    } else {
      setSortField(field);
      setSortAsc(false); // default descending for stats
    }
  };

  // Prepare chart data (sorted chronologically)
  const chartData = [...gameweeks]
    .sort((a, b) => a.gameweekId - b.gameweekId)
    .map((g) => ({
      name: `GW ${g.gameweekId}`,
      gameweekId: g.gameweekId,
      netPoints: g.netPoints,
      grossPoints: g.points,
      averageScore: g.averageScore,
      overallRank: g.overallRank,
      captain: g.captainName,
      chip: g.chipUsed
    }));

  return (
    <div style={{ maxWidth: 1100, margin: '0 auto', display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
      {/* ─── 1. Season Summary KPI Cards ─────────────────────────────────── */}
      <div style={{
        display: 'grid',
        gridTemplateColumns: 'repeat(auto-fit, minmax(160px, 1fr))',
        gap: '0.8rem'
      }}>
        <div className="glass-panel" style={{ padding: '0.8rem 1rem' }}>
          <div style={{ fontSize: '0.72rem', color: 'var(--text-muted)', textTransform: 'uppercase', display: 'flex', alignItems: 'center', gap: 4 }}>
            <Trophy size={14} color="var(--accent-amber)" /> Total Points
          </div>
          <div style={{ fontSize: '1.6rem', fontWeight: 800, color: 'var(--accent-primary)', marginTop: 4 }}>
            {summary.totalPoints}
          </div>
          <div style={{ fontSize: '0.75rem', color: 'var(--text-secondary)', marginTop: 2 }}>
            Overall net cumulative
          </div>
        </div>

        <div className="glass-panel" style={{ padding: '0.8rem 1rem' }}>
          <div style={{ fontSize: '0.72rem', color: 'var(--text-muted)', textTransform: 'uppercase', display: 'flex', alignItems: 'center', gap: 4 }}>
            <TrendingUp size={14} color="#38bdf8" /> Current Rank
          </div>
          <div style={{ fontSize: '1.6rem', fontWeight: 800, color: '#f8fafc', marginTop: 4 }}>
            {summary.currentOverallRank ? summary.currentOverallRank.toLocaleString() : 'N/A'}
          </div>
          <div style={{ fontSize: '0.75rem', color: 'var(--text-secondary)', marginTop: 2 }}>
            Global rank
          </div>
        </div>

        <div className="glass-panel" style={{ padding: '0.8rem 1rem' }}>
          <div style={{ fontSize: '0.72rem', color: 'var(--text-muted)', textTransform: 'uppercase', display: 'flex', alignItems: 'center', gap: 4 }}>
            <Award size={14} color="var(--accent-cyan)" /> Best GW Rank
          </div>
          <div style={{ fontSize: '1.4rem', fontWeight: 800, color: 'var(--accent-cyan)', marginTop: 4 }}>
            {summary.bestGameweekRank ? summary.bestGameweekRank.toLocaleString() : 'N/A'}
          </div>
          <div style={{ fontSize: '0.75rem', color: 'var(--text-secondary)', marginTop: 2 }}>
            In Gameweek {summary.bestGameweekRankEvent || 1}
          </div>
        </div>

        <div className="glass-panel" style={{ padding: '0.8rem 1rem' }}>
          <div style={{ fontSize: '0.72rem', color: 'var(--text-muted)', textTransform: 'uppercase' }}>
            Avg Score vs League
          </div>
          <div style={{ fontSize: '1.4rem', fontWeight: 800, color: summary.scoreVersusLeagueAvg >= 0 ? 'var(--accent-primary)' : '#ff1744', marginTop: 4 }}>
            {summary.averageGameweekScore} <span style={{ fontSize: '0.85rem' }}>({summary.scoreVersusLeagueAvg >= 0 ? `+${summary.scoreVersusLeagueAvg}` : summary.scoreVersusLeagueAvg})</span>
          </div>
          <div style={{ fontSize: '0.75rem', color: 'var(--text-secondary)', marginTop: 2 }}>
            League avg: {summary.averageLeagueScore}
          </div>
        </div>

        <div className="glass-panel" style={{ padding: '0.8rem 1rem' }}>
          <div style={{ fontSize: '0.72rem', color: 'var(--text-muted)', textTransform: 'uppercase', display: 'flex', alignItems: 'center', gap: 4 }}>
            <Armchair size={14} color="#a855f7" /> Bench Points
          </div>
          <div style={{ fontSize: '1.6rem', fontWeight: 800, color: '#f8fafc', marginTop: 4 }}>
            {summary.totalBenchPoints} pts
          </div>
          <div style={{ fontSize: '0.75rem', color: 'var(--text-secondary)', marginTop: 2 }}>
            Left on bench
          </div>
        </div>

        <div className="glass-panel" style={{ padding: '0.8rem 1rem' }}>
          <div style={{ fontSize: '0.72rem', color: 'var(--text-muted)', textTransform: 'uppercase' }}>
            Squad Value / Bank
          </div>
          <div style={{ fontSize: '1.4rem', fontWeight: 800, color: 'var(--accent-primary)', marginTop: 4 }}>
            £{summary.teamValue.toFixed(1)}m
          </div>
          <div style={{ fontSize: '0.75rem', color: 'var(--text-secondary)', marginTop: 2 }}>
            Bank: £{summary.bank.toFixed(1)}m
          </div>
        </div>
      </div>

      {/* ─── 2. Captaincy Efficiency & Chips Strip ───────────────────────── */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))', gap: '1rem' }}>
        {/* Captaincy Card */}
        <div className="glass-panel" style={{ padding: '1rem 1.2rem' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.6rem' }}>
            <h4 style={{ fontSize: '0.95rem', fontWeight: 700, display: 'flex', alignItems: 'center', gap: 6, margin: 0 }}>
              <span>👑</span> Captaincy Efficiency
            </h4>
            <span style={{ fontSize: '0.75rem', color: 'var(--accent-amber)', fontWeight: 700 }}>
              {summary.captainEfficiency.totalCaptainPoints} total pts
            </span>
          </div>
          <p style={{ fontSize: '0.82rem', color: 'var(--text-secondary)', margin: '0 0 0.8rem 0' }}>
            Your captain pick averaged <strong>{summary.captainEfficiency.averageCaptainPoints} pts/GW</strong> and was the highest-scoring starter in <strong>{summary.captainEfficiency.optimalCaptainHits} of {summary.captainEfficiency.totalGameweeks}</strong> gameweeks.
          </p>
          <div style={{
            height: 6,
            background: 'rgba(255, 255, 255, 0.1)',
            borderRadius: 3,
            overflow: 'hidden'
          }}>
            <div style={{
              width: `${Math.min(100, Math.round((summary.captainEfficiency.optimalCaptainHits / Math.max(1, summary.captainEfficiency.totalGameweeks)) * 100))}%`,
              height: '100%',
              background: 'linear-gradient(90deg, var(--accent-amber), var(--accent-primary))'
            }} />
          </div>
          <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.7rem', color: 'var(--text-muted)', marginTop: 4 }}>
            <span>Success Rate</span>
            <span>{Math.round((summary.captainEfficiency.optimalCaptainHits / Math.max(1, summary.captainEfficiency.totalGameweeks)) * 100)}% optimal hit</span>
          </div>
        </div>

        {/* Chips Timeline Card */}
        <div className="glass-panel" style={{ padding: '1rem 1.2rem' }}>
          <h4 style={{ fontSize: '0.95rem', fontWeight: 700, display: 'flex', alignItems: 'center', gap: 6, margin: '0 0 0.6rem 0' }}>
            <span>⚡</span> Chips Played Timeline
          </h4>
          {chipsTimeline && chipsTimeline.length > 0 ? (
            <div style={{ display: 'flex', gap: '0.8rem', flexWrap: 'wrap' }}>
              {chipsTimeline.map((c, i) => (
                <div key={i} style={{
                  display: 'flex',
                  alignItems: 'center',
                  gap: 8,
                  padding: '6px 12px',
                  borderRadius: 'var(--radius-sm)',
                  background: 'rgba(0, 255, 135, 0.12)',
                  border: '1px solid rgba(0, 255, 135, 0.3)'
                }}>
                  <CheckCircle2 size={14} color="var(--accent-primary)" />
                  <div>
                    <div style={{ fontSize: '0.8rem', fontWeight: 700, color: '#f8fafc' }}>
                      {c.chipName}
                    </div>
                    <div style={{ fontSize: '0.7rem', color: 'var(--text-muted)' }}>
                      GW {c.gameweekId} ({c.gameweekPoints} pts)
                    </div>
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <p style={{ fontSize: '0.82rem', color: 'var(--text-muted)', margin: 0 }}>
              No chips played yet this season. All chips are currently available!
            </p>
          )}
        </div>
      </div>

      {/* ─── 3. Deterministic Performance Insights ───────────────────────── */}
      {deterministicInsights && deterministicInsights.length > 0 && (
        <div className="glass-panel" style={{ padding: '1rem 1.25rem', background: 'rgba(22, 30, 49, 0.7)' }}>
          <h4 style={{ fontSize: '0.95rem', fontWeight: 700, display: 'flex', alignItems: 'center', gap: 6, margin: '0 0 0.75rem 0' }}>
            <Sparkles size={16} color="var(--accent-primary)" /> Season Performance Insights
          </h4>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: '0.6rem' }}>
            {deterministicInsights.map((insight, idx) => (
              <div key={idx} style={{
                fontSize: '0.82rem',
                color: 'var(--text-secondary)',
                padding: '0.5rem 0.8rem',
                borderRadius: 'var(--radius-sm)',
                background: 'rgba(255, 255, 255, 0.03)',
                borderLeft: '3px solid var(--accent-primary)',
                lineHeight: 1.4
              }}>
                {insight}
              </div>
            ))}
          </div>
        </div>
      )}

      {/* ─── 4. Recharts Interactive Visualizations ──────────────────────── */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(450px, 1fr))', gap: '1rem' }}>
        {/* Chart 1: Points vs League Average */}
        <div className="glass-panel" style={{ padding: '1.2rem', minHeight: 320 }}>
          <h4 style={{ fontSize: '0.95rem', fontWeight: 700, marginBottom: '1rem' }}>
            📊 Gameweek Score vs League Average
          </h4>
          <div style={{ width: '100%', height: 250 }}>
            <ResponsiveContainer width="100%" height="100%">
              <ComposedChart data={chartData} margin={{ top: 10, right: 20, left: -10, bottom: 0 }}>
                <CartesianGrid strokeDasharray="3 3" stroke="rgba(255, 255, 255, 0.07)" />
                <XAxis dataKey="name" stroke="#94a3b8" fontSize={11} />
                <YAxis stroke="#94a3b8" fontSize={11} />
                <Tooltip
                  contentStyle={{ background: '#0f172a', border: '1px solid #334155', borderRadius: 8, fontSize: '0.8rem' }}
                  labelStyle={{ color: '#f8fafc', fontWeight: 700 }}
                />
                <Legend wrapperStyle={{ fontSize: '0.75rem', paddingTop: 6 }} />
                <Bar dataKey="netPoints" name="Your Net Points" fill="#00ff87" radius={[4, 4, 0, 0]} />
                <Line type="monotone" dataKey="averageScore" name="League Average" stroke="#38bdf8" strokeWidth={2.5} dot={{ r: 3 }} />
              </ComposedChart>
            </ResponsiveContainer>
          </div>
        </div>

        {/* Chart 2: Overall Rank History */}
        <div className="glass-panel" style={{ padding: '1.2rem', minHeight: 320 }}>
          <h4 style={{ fontSize: '0.95rem', fontWeight: 700, marginBottom: '1rem' }}>
            📈 Overall Rank Progression
          </h4>
          <div style={{ width: '100%', height: 250 }}>
            <ResponsiveContainer width="100%" height="100%">
              <LineChart data={chartData} margin={{ top: 10, right: 20, left: 10, bottom: 0 }}>
                <CartesianGrid strokeDasharray="3 3" stroke="rgba(255, 255, 255, 0.07)" />
                <XAxis dataKey="name" stroke="#94a3b8" fontSize={11} />
                <YAxis
                  reversed
                  stroke="#94a3b8"
                  fontSize={11}
                  tickFormatter={(val) => val >= 1000000 ? `${(val / 1000000).toFixed(1)}M` : `${Math.round(val / 1000)}k`}
                />
                <Tooltip
                  contentStyle={{ background: '#0f172a', border: '1px solid #334155', borderRadius: 8, fontSize: '0.8rem' }}
                  labelStyle={{ color: '#f8fafc', fontWeight: 700 }}
                  formatter={(value: any) => [value ? Number(value).toLocaleString() : 'N/A', 'Overall Rank']}
                />
                <Legend wrapperStyle={{ fontSize: '0.75rem', paddingTop: 6 }} />
                <Line type="monotone" dataKey="overallRank" name="Overall Rank (lower is better)" stroke="#f59e0b" strokeWidth={2.5} dot={{ r: 4 }} />
              </LineChart>
            </ResponsiveContainer>
          </div>
        </div>
      </div>

      {/* ─── 5. Per-Gameweek Sortable Data Table ──────────────────────────── */}
      <div className="glass-panel" style={{ padding: '1.2rem', overflowX: 'auto' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem', flexWrap: 'wrap', gap: '0.5rem' }}>
          <div>
            <h4 style={{ fontSize: '1rem', fontWeight: 700, margin: 0 }}>
              📅 Gameweek History Log
            </h4>
            <span style={{ fontSize: '0.78rem', color: 'var(--text-muted)' }}>
              Click any row to jump directly to that Gameweek's pitch lineup on the squad view.
            </span>
          </div>
          <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>
            Showing {sortedGameweeks.length} settled gameweeks
          </div>
        </div>

        <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '0.82rem', textAlign: 'left' }}>
          <thead>
            <tr style={{ borderBottom: '1px solid var(--border-color)', color: 'var(--text-muted)' }}>
              <th onClick={() => handleSort('gameweekId')} style={{ padding: '8px 10px', cursor: 'pointer' }}>
                GW <ArrowUpDown size={12} style={{ verticalAlign: 'middle' }} />
              </th>
              <th onClick={() => handleSort('points')} style={{ padding: '8px 10px', cursor: 'pointer' }}>
                Points (Gross) <ArrowUpDown size={12} style={{ verticalAlign: 'middle' }} />
              </th>
              <th onClick={() => handleSort('transferCost')} style={{ padding: '8px 10px', cursor: 'pointer' }}>
                Hits Cost <ArrowUpDown size={12} style={{ verticalAlign: 'middle' }} />
              </th>
              <th onClick={() => handleSort('netPoints')} style={{ padding: '8px 10px', cursor: 'pointer' }}>
                Net Pts <ArrowUpDown size={12} style={{ verticalAlign: 'middle' }} />
              </th>
              <th onClick={() => handleSort('averageScore')} style={{ padding: '8px 10px', cursor: 'pointer' }}>
                League Avg <ArrowUpDown size={12} style={{ verticalAlign: 'middle' }} />
              </th>
              <th onClick={() => handleSort('rank')} style={{ padding: '8px 10px', cursor: 'pointer' }}>
                GW Rank <ArrowUpDown size={12} style={{ verticalAlign: 'middle' }} />
              </th>
              <th onClick={() => handleSort('overallRank')} style={{ padding: '8px 10px', cursor: 'pointer' }}>
                Overall Rank <ArrowUpDown size={12} style={{ verticalAlign: 'middle' }} />
              </th>
              <th onClick={() => handleSort('captainName')} style={{ padding: '8px 10px', cursor: 'pointer' }}>
                Captain <ArrowUpDown size={12} style={{ verticalAlign: 'middle' }} />
              </th>
              <th onClick={() => handleSort('pointsOnBench')} style={{ padding: '8px 10px', cursor: 'pointer' }}>
                Bench Pts <ArrowUpDown size={12} style={{ verticalAlign: 'middle' }} />
              </th>
              <th style={{ padding: '8px 10px' }}>
                Chip
              </th>
              <th style={{ padding: '8px 10px', textAlign: 'center' }}>
                Action
              </th>
            </tr>
          </thead>
          <tbody>
            {sortedGameweeks.map((row) => {
              const diff = (row.averageScore !== null && row.averageScore !== undefined) ? (row.netPoints - row.averageScore) : null;
              return (
                <tr
                  key={row.gameweekId}
                  onClick={() => onSelectGameweek(row.gameweekId)}
                  style={{
                    borderBottom: '1px solid rgba(255, 255, 255, 0.05)',
                    cursor: 'pointer',
                    transition: 'background 0.15s ease'
                  }}
                  className="table-row-hover"
                >
                  <td style={{ padding: '10px', fontWeight: 700, color: 'var(--text-primary)' }}>
                    GW {row.gameweekId}
                  </td>
                  <td style={{ padding: '10px' }}>
                    {row.points}
                  </td>
                  <td style={{ padding: '10px', color: row.transferCost > 0 ? '#ff1744' : 'var(--text-muted)' }}>
                    {row.transferCost > 0 ? `-${row.transferCost}` : '0'}
                  </td>
                  <td style={{ padding: '10px', fontWeight: 800, color: 'var(--accent-primary)' }}>
                    {row.netPoints}
                  </td>
                  <td style={{ padding: '10px' }}>
                    {row.averageScore ?? '—'}
                    {diff !== null && (
                      <span style={{
                        fontSize: '0.72rem',
                        marginLeft: 6,
                        color: diff >= 0 ? '#10b981' : '#ff1744',
                        fontWeight: 600
                      }}>
                        ({diff >= 0 ? `+${diff}` : diff})
                      </span>
                    )}
                  </td>
                  <td style={{ padding: '10px' }}>
                    {row.rank ? row.rank.toLocaleString() : '—'}
                  </td>
                  <td style={{ padding: '10px', fontWeight: 600 }}>
                    {row.overallRank ? row.overallRank.toLocaleString() : '—'}
                  </td>
                  <td style={{ padding: '10px' }}>
                    <span style={{ fontWeight: 600, color: 'var(--text-primary)' }}>{row.captainName}</span>
                    <span style={{ fontSize: '0.72rem', color: 'var(--accent-amber)', marginLeft: 4 }}>
                      ({row.captainPoints} pts)
                    </span>
                  </td>
                  <td style={{ padding: '10px', color: 'var(--text-secondary)' }}>
                    {row.pointsOnBench}
                  </td>
                  <td style={{ padding: '10px' }}>
                    {row.chipUsed ? (
                      <span style={{
                        padding: '2px 6px',
                        borderRadius: 4,
                        background: 'rgba(0, 255, 135, 0.15)',
                        color: 'var(--accent-primary)',
                        fontSize: '0.72rem',
                        fontWeight: 700
                      }}>
                        {row.chipUsed.toUpperCase()}
                      </span>
                    ) : (
                      <span style={{ color: 'var(--text-muted)' }}>—</span>
                    )}
                  </td>
                  <td style={{ padding: '10px', textAlign: 'center' }}>
                    <button
                      className="btn"
                      style={{ padding: '3px 8px', fontSize: '0.72rem', display: 'inline-flex', alignItems: 'center', gap: 2 }}
                      onClick={(e) => {
                        e.stopPropagation();
                        onSelectGameweek(row.gameweekId);
                      }}
                    >
                      View Squad <ChevronRight size={12} />
                    </button>
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default HistoryView;
