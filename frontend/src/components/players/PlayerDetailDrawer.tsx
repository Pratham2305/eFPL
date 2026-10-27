import React, { useEffect, useState } from 'react';
import { X, Sparkles, AlertTriangle, TrendingUp, Calendar, Activity } from 'lucide-react';
import { PlayerDetailDto } from '../../types';
import ClubBadge from '../ClubBadge';

interface PlayerDetailDrawerProps {
  playerId: number | null;
  onClose: () => void;
  onAskAi: (prompt: string) => void;
}

export const PlayerDetailDrawer: React.FC<PlayerDetailDrawerProps> = ({
  playerId,
  onClose,
  onAskAi
}) => {
  const [detail, setDetail] = useState<PlayerDetailDto | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!playerId) {
      setDetail(null);
      return;
    }
    const fetchDetail = async () => {
      setLoading(true);
      setError(null);
      try {
        const res = await fetch(`/api/players/${playerId}`);
        if (!res.ok) throw new Error(`Failed to load player details (${res.status})`);
        const data: PlayerDetailDto = await res.json();
        setDetail(data);
      } catch (err: any) {
        setError(err.message || 'Error loading player details');
      } finally {
        setLoading(false);
      }
    };
    fetchDetail();
  }, [playerId]);

  // Handle ESC key
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'Escape') onClose();
    };
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [onClose]);

  if (!playerId) return null;

  const fdrColor = (diff: number) => {
    switch (diff) {
      case 1:
      case 2:
        return { bg: 'rgba(16, 185, 129, 0.2)', border: 'rgba(16, 185, 129, 0.4)', text: '#34d399' };
      case 3:
        return { bg: 'rgba(234, 179, 8, 0.2)', border: 'rgba(234, 179, 8, 0.4)', text: '#fde047' };
      case 4:
      case 5:
        return { bg: 'rgba(239, 68, 68, 0.2)', border: 'rgba(239, 68, 68, 0.4)', text: '#f87171' };
      default:
        return { bg: 'rgba(255, 255, 255, 0.1)', border: 'rgba(255, 255, 255, 0.2)', text: '#fff' };
    }
  };

  const p = detail?.summary;

  return (
    <div style={{
      position: 'fixed',
      top: 0,
      left: 0,
      width: '100vw',
      height: '100vh',
      backgroundColor: 'rgba(0, 0, 0, 0.7)',
      backdropFilter: 'blur(6px)',
      zIndex: 1050,
      display: 'flex',
      justifyContent: 'flex-end',
      animation: 'fadeIn 0.2s ease-out'
    }}>
      {/* Click outside backdrop to close */}
      <div style={{ flex: 1 }} onClick={onClose} />

      {/* Drawer Container */}
      <div style={{
        width: 580,
        maxWidth: '100vw',
        height: '100%',
        background: 'rgba(15, 23, 42, 0.98)',
        borderLeft: '1px solid rgba(56, 189, 248, 0.3)',
        boxShadow: '-10px 0 30px rgba(0, 0, 0, 0.8)',
        display: 'flex',
        flexDirection: 'column',
        overflowY: 'auto',
        color: '#f8fafc'
      }}>
        {/* Header */}
        <div style={{
          padding: '1.2rem 1.5rem',
          borderBottom: '1px solid rgba(255, 255, 255, 0.08)',
          background: 'linear-gradient(135deg, rgba(56, 189, 248, 0.12), rgba(168, 85, 247, 0.08))',
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'flex-start'
        }}>
          {p ? (
            <div style={{ display: 'flex', gap: '0.9rem', alignItems: 'center' }}>
              <ClubBadge team={p.teamShortName} size={42} alt={p.teamName} />
              <div>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', flexWrap: 'wrap' }}>
                  <h2 style={{ fontSize: '1.3rem', fontWeight: 700, margin: 0, color: '#fff' }}>
                    {p.webName}
                  </h2>
                  <span style={{
                    padding: '2px 8px',
                    borderRadius: 4,
                    background: 'rgba(56, 189, 248, 0.2)',
                    color: 'var(--accent-cyan)',
                    fontSize: '0.75rem',
                    fontWeight: 700
                  }}>
                    {p.positionName}
                  </span>
                  {p.inSquad && (
                    <span style={{
                      padding: '2px 8px',
                      borderRadius: 4,
                      background: 'rgba(0, 255, 135, 0.2)',
                      color: 'var(--accent-primary)',
                      fontSize: '0.75rem',
                      fontWeight: 700
                    }}>
                      In Squad {p.isStarting ? '(Starting XI)' : '(Bench)'} {p.isCaptain ? '★ (C)' : p.isViceCaptain ? '★ (V)' : ''}
                    </span>
                  )}
                </div>
                <div style={{ fontSize: '0.84rem', color: 'var(--text-secondary)', marginTop: 2 }}>
                  {p.firstName} {p.secondName} • {p.teamName}
                </div>
              </div>
            </div>
          ) : (
            <div style={{ color: 'var(--text-secondary)' }}>Loading player profile...</div>
          )}

          <button
            onClick={onClose}
            style={{ background: 'none', border: 'none', color: '#94a3b8', cursor: 'pointer', padding: '0.2rem' }}
          >
            <X size={22} />
          </button>
        </div>

        {/* Content */}
        {loading && (
          <div style={{ textAlign: 'center', padding: '4rem 1rem', color: 'var(--text-secondary)' }}>
            <div className="pulse" style={{ fontSize: '2rem', marginBottom: '0.5rem' }}>⚽</div>
            Loading full player history & predictions...
          </div>
        )}

        {error && (
          <div style={{ padding: '2rem', textAlign: 'center', color: '#f87171' }}>
            <AlertTriangle size={28} style={{ marginBottom: '0.5rem' }} />
            <div>{error}</div>
          </div>
        )}

        {p && detail && !loading && (
          <div style={{ padding: '1.2rem 1.5rem', display: 'flex', flexDirection: 'column', gap: '1.3rem' }}>
            {/* Status Warning Banner if not 100% available */}
            {p.status !== 'a' && (
              <div style={{
                padding: '0.8rem 1rem',
                borderRadius: 8,
                background: 'rgba(239, 68, 68, 0.15)',
                border: '1px solid rgba(239, 68, 68, 0.35)',
                display: 'flex',
                gap: '0.7rem',
                alignItems: 'flex-start',
                fontSize: '0.84rem'
              }}>
                <AlertTriangle size={18} color="#f87171" style={{ flexShrink: 0, marginTop: 2 }} />
                <div>
                  <div style={{ fontWeight: 700, color: '#fca5a5' }}>
                    {p.chanceOfPlayingNext !== null ? `${p.chanceOfPlayingNext}% Chance of Playing` : 'Player Flagged'}
                  </div>
                  <div style={{ color: '#cbd5e1', marginTop: 2 }}>{p.news || 'Injured or doubtful'}</div>
                  {p.newsAdded && (
                    <div style={{ fontSize: '0.72rem', color: '#94a3b8', marginTop: 4 }}>
                      Flag added: {new Date(p.newsAdded).toLocaleDateString()}
                    </div>
                  )}
                </div>
              </div>
            )}

            {/* Quick Action: Ask AI */}
            <div style={{
              display: 'flex',
              justifyContent: 'space-between',
              alignItems: 'center',
              padding: '0.9rem 1.2rem',
              borderRadius: 10,
              background: 'linear-gradient(90deg, rgba(56, 189, 248, 0.15), rgba(168, 85, 247, 0.12))',
              border: '1px solid rgba(56, 189, 248, 0.3)'
            }}>
              <div>
                <div style={{ fontWeight: 700, fontSize: '0.9rem', color: '#fff', display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
                  <Sparkles size={16} color="var(--accent-cyan)" />
                  Consult Grounded AI Assistant
                </div>
                <div style={{ fontSize: '0.76rem', color: 'var(--text-secondary)', marginTop: 2 }}>
                  Analyze role, fixture run, and transfer fit against your squad
                </div>
              </div>
              <button
                className="btn btn-primary"
                onClick={() => {
                  onClose();
                  onAskAi(`Tell me about ${p.webName} and whether I should consider transferring him into my squad.`);
                }}
                style={{ padding: '0.45rem 0.9rem', fontSize: '0.8rem', whiteSpace: 'nowrap' }}
              >
                Ask About {p.webName}
              </button>
            </div>

            {/* Key Metrics Header Grid */}
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '0.6rem' }}>
              <div className="glass-panel" style={{ padding: '0.75rem', textAlign: 'center' }}>
                <div style={{ fontSize: '0.72rem', color: 'var(--text-secondary)' }}>PRICE</div>
                <div style={{ fontSize: '1.15rem', fontWeight: 800, color: '#fff', marginTop: 3 }}>
                  £{(p.nowCost / 10).toFixed(1)}m
                </div>
                <div style={{ fontSize: '0.68rem', color: p.costChangeEvent > 0 ? '#34d399' : p.costChangeEvent < 0 ? '#f87171' : 'var(--text-muted)' }}>
                  {p.costChangeEvent > 0 ? `+£${p.costChangeEvent / 10}m this GW` : p.costChangeEvent < 0 ? `-£${Math.abs(p.costChangeEvent) / 10}m` : 'No change'}
                </div>
              </div>

              <div className="glass-panel" style={{ padding: '0.75rem', textAlign: 'center' }}>
                <div style={{ fontSize: '0.72rem', color: 'var(--text-secondary)' }}>TOTAL PTS</div>
                <div style={{ fontSize: '1.15rem', fontWeight: 800, color: '#fff', marginTop: 3 }}>
                  {p.totalPoints}
                </div>
                <div style={{ fontSize: '0.68rem', color: 'var(--text-muted)' }}>
                  {p.pointsPerGame ? `${p.pointsPerGame} PPG` : '—'}
                </div>
              </div>

              <div className="glass-panel" style={{ padding: '0.75rem', textAlign: 'center' }}>
                <div style={{ fontSize: '0.72rem', color: 'var(--text-secondary)' }}>FORM</div>
                <div style={{ fontSize: '1.15rem', fontWeight: 800, color: 'var(--accent-primary)', marginTop: 3 }}>
                  {p.form ?? '—'}
                </div>
                <div style={{ fontSize: '0.68rem', color: 'var(--text-muted)' }}>Last 30 days</div>
              </div>

              <div className="glass-panel" style={{ padding: '0.75rem', textAlign: 'center' }}>
                <div style={{ fontSize: '0.72rem', color: 'var(--text-secondary)' }}>OWNERSHIP</div>
                <div style={{ fontSize: '1.15rem', fontWeight: 800, color: '#38bdf8', marginTop: 3 }}>
                  {p.selectedByPercent ? `${p.selectedByPercent}%` : '—'}
                </div>
                <div style={{ fontSize: '0.68rem', color: 'var(--text-muted)' }}>All Managers</div>
              </div>
            </div>

            {/* Projections & Expected Points (ML vs FPL) */}
            <div className="glass-panel" style={{ padding: '1rem' }}>
              <div style={{ fontSize: '0.85rem', fontWeight: 700, display: 'flex', alignItems: 'center', gap: '0.4rem', marginBottom: '0.8rem', color: 'var(--accent-cyan)' }}>
                <TrendingUp size={16} />
                Expected Points Projections
              </div>
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '0.8rem', marginBottom: '0.7rem' }}>
                <div style={{ background: 'rgba(0, 0, 0, 0.3)', padding: '0.65rem', borderRadius: 8, textAlign: 'center' }}>
                  <div style={{ fontSize: '0.7rem', color: 'var(--text-secondary)' }}>NEXT GW xP</div>
                  <div style={{ fontSize: '1.25rem', fontWeight: 800, color: p.xpNext !== null ? 'var(--accent-primary)' : 'var(--text-muted)', marginTop: 2 }}>
                    {p.xpNext !== null ? `${p.xpNext}` : '—'}
                  </div>
                  <div style={{ fontSize: '0.68rem', color: 'var(--text-muted)' }}>ML Model</div>
                </div>

                <div style={{ background: 'rgba(0, 0, 0, 0.3)', padding: '0.65rem', borderRadius: 8, textAlign: 'center' }}>
                  <div style={{ fontSize: '0.7rem', color: 'var(--text-secondary)' }}>NEXT 3 GWs xP</div>
                  <div style={{ fontSize: '1.25rem', fontWeight: 800, color: detail.xp3 !== null ? 'var(--accent-cyan)' : 'var(--text-muted)', marginTop: 2 }}>
                    {detail.xp3 !== null ? `${detail.xp3}` : '—'}
                  </div>
                  <div style={{ fontSize: '0.68rem', color: 'var(--text-muted)' }}>3-GW Cumulative</div>
                </div>

                <div style={{ background: 'rgba(0, 0, 0, 0.3)', padding: '0.65rem', borderRadius: 8, textAlign: 'center' }}>
                  <div style={{ fontSize: '0.7rem', color: 'var(--text-secondary)' }}>NEXT 6 GWs xP</div>
                  <div style={{ fontSize: '1.25rem', fontWeight: 800, color: detail.xp6 !== null ? '#c084fc' : 'var(--text-muted)', marginTop: 2 }}>
                    {detail.xp6 !== null ? `${detail.xp6}` : '—'}
                  </div>
                  <div style={{ fontSize: '0.68rem', color: 'var(--text-muted)' }}>6-GW Horizon</div>
                </div>
              </div>

              {p.epNext !== null && (
                <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', borderTop: '1px solid rgba(255,255,255,0.06)', paddingTop: '0.5rem', display: 'flex', justifyContent: 'space-between' }}>
                  <span>Official FPL algorithm projection (ep_next):</span>
                  <span style={{ color: '#fff', fontWeight: 600 }}>{p.epNext} pts</span>
                </div>
              )}
            </div>

            {/* Upcoming 6 Fixtures */}
            <div className="glass-panel" style={{ padding: '1rem' }}>
              <div style={{ fontSize: '0.85rem', fontWeight: 700, display: 'flex', alignItems: 'center', gap: '0.4rem', marginBottom: '0.8rem', color: '#fff' }}>
                <Calendar size={16} color="var(--accent-cyan)" />
                Next 6 Premier League Fixtures
              </div>
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(6, 1fr)', gap: '0.4rem' }}>
                {detail.upcomingFixtures.map((f, i) => {
                  if (f.isBlankGameweek) {
                    return (
                      <div key={i} style={{
                        padding: '0.5rem 0.2rem',
                        borderRadius: 6,
                        textAlign: 'center',
                        background: 'rgba(0,0,0,0.3)',
                        border: '1px dashed rgba(255,255,255,0.15)'
                      }}>
                        <div style={{ fontSize: '0.68rem', color: 'var(--text-muted)' }}>GW{f.gameweekId}</div>
                        <div style={{ fontSize: '0.72rem', fontWeight: 700, color: '#f87171', margin: '4px 0' }}>BLANK</div>
                        <div style={{ fontSize: '0.62rem', color: 'var(--text-muted)' }}>No Match</div>
                      </div>
                    );
                  }
                  const styling = fdrColor(f.difficulty);
                  return (
                    <div key={f.fixtureId || i} style={{
                      padding: '0.5rem 0.2rem',
                      borderRadius: 6,
                      textAlign: 'center',
                      background: styling.bg,
                      border: `1px solid ${styling.border}`
                    }}>
                      <div style={{ fontSize: '0.68rem', color: 'var(--text-secondary)' }}>GW{f.gameweekId}</div>
                      <div style={{ fontSize: '0.78rem', fontWeight: 800, color: styling.text, margin: '2px 0' }}>
                        {f.opponentShortName}
                      </div>
                      <div style={{ fontSize: '0.66rem', color: 'var(--text-muted)' }}>
                        ({f.isHome ? 'H' : 'A'}) {f.isDoubleGameweek && <span style={{ color: '#34d399' }}>• DGW</span>}
                      </div>
                    </div>
                  );
                })}
              </div>
            </div>

            {/* Performance & Advanced Stats Breakdown */}
            <div className="glass-panel" style={{ padding: '1rem' }}>
              <div style={{ fontSize: '0.85rem', fontWeight: 700, display: 'flex', alignItems: 'center', gap: '0.4rem', marginBottom: '0.8rem', color: '#fff' }}>
                <Activity size={16} color="var(--accent-cyan)" />
                Season Statistics & Underlying Metrics
              </div>
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '0.7rem', fontSize: '0.8rem' }}>
                <div>
                  <span style={{ color: 'var(--text-secondary)' }}>Minutes:</span> <strong>{p.minutes}</strong> ({p.starts} starts)
                </div>
                <div>
                  <span style={{ color: 'var(--text-secondary)' }}>Goals:</span> <strong>{p.goalsScored}</strong>
                </div>
                <div>
                  <span style={{ color: 'var(--text-secondary)' }}>Assists:</span> <strong>{p.assists}</strong>
                </div>
                <div>
                  <span style={{ color: 'var(--text-secondary)' }}>Clean Sheets:</span> <strong>{p.cleanSheets}</strong>
                </div>
                <div>
                  <span style={{ color: 'var(--text-secondary)' }}>Expected Goals (xG):</span> <strong>{p.expectedGoals ?? '—'}</strong>
                </div>
                <div>
                  <span style={{ color: 'var(--text-secondary)' }}>Expected Assists (xA):</span> <strong>{p.expectedAssists ?? '—'}</strong>
                </div>
                <div>
                  <span style={{ color: 'var(--text-secondary)' }}>xGI:</span> <strong>{p.expectedGoalInvolvements ?? '—'}</strong>
                </div>
                <div>
                  <span style={{ color: 'var(--text-secondary)' }}>xGC:</span> <strong>{p.expectedGoalsConceded ?? '—'}</strong>
                </div>
                <div>
                  <span style={{ color: 'var(--text-secondary)' }}>ICT Index:</span> <strong>{p.ictIndex ?? '—'}</strong>
                </div>
                <div>
                  <span style={{ color: 'var(--text-secondary)' }}>Bonus Pts:</span> <strong>{p.bonus}</strong> (BPS {p.bps})
                </div>
                <div>
                  <span style={{ color: 'var(--text-secondary)' }}>Saves:</span> <strong>{p.saves}</strong>
                </div>
                <div>
                  <span style={{ color: 'var(--text-secondary)' }}>Cards:</span> <strong>{p.yellowCards}Y / {p.redCards}R</strong>
                </div>
              </div>
            </div>

            {/* Recent Match History */}
            {detail.historicalStats.length > 0 && (
              <div className="glass-panel" style={{ padding: '1rem' }}>
                <div style={{ fontSize: '0.85rem', fontWeight: 700, marginBottom: '0.6rem', color: '#fff' }}>
                  Recent Matches
                </div>
                <div style={{ overflowX: 'auto' }}>
                  <table style={{ width: '100%', fontSize: '0.78rem', borderCollapse: 'collapse', textAlign: 'left' }}>
                    <thead>
                      <tr style={{ borderBottom: '1px solid rgba(255,255,255,0.08)', color: 'var(--text-secondary)' }}>
                        <th style={{ padding: '0.35rem 0.5rem' }}>GW</th>
                        <th style={{ padding: '0.35rem 0.5rem' }}>OPP</th>
                        <th style={{ padding: '0.35rem 0.5rem' }}>MIN</th>
                        <th style={{ padding: '0.35rem 0.5rem' }}>PTS</th>
                        <th style={{ padding: '0.35rem 0.5rem' }}>G</th>
                        <th style={{ padding: '0.35rem 0.5rem' }}>A</th>
                        <th style={{ padding: '0.35rem 0.5rem' }}>CS</th>
                        <th style={{ padding: '0.35rem 0.5rem' }}>xG</th>
                        <th style={{ padding: '0.35rem 0.5rem' }}>xA</th>
                      </tr>
                    </thead>
                    <tbody>
                      {detail.historicalStats.slice(0, 6).map((h, idx) => (
                        <tr key={idx} style={{ borderBottom: '1px solid rgba(255,255,255,0.04)' }}>
                          <td style={{ padding: '0.35rem 0.5rem', fontWeight: 600 }}>GW{h.gameweekId}</td>
                          <td style={{ padding: '0.35rem 0.5rem' }}>
                            {h.opponentShortName ? `${h.opponentShortName} (${h.wasHome ? 'H' : 'A'})` : '—'}
                          </td>
                          <td style={{ padding: '0.35rem 0.5rem' }}>{h.minutes}'</td>
                          <td style={{ padding: '0.35rem 0.5rem', fontWeight: 700, color: 'var(--accent-primary)' }}>
                            {h.totalPoints}
                          </td>
                          <td style={{ padding: '0.35rem 0.5rem' }}>{h.goalsScored}</td>
                          <td style={{ padding: '0.35rem 0.5rem' }}>{h.assists}</td>
                          <td style={{ padding: '0.35rem 0.5rem' }}>{h.cleanSheets}</td>
                          <td style={{ padding: '0.35rem 0.5rem' }}>{h.expectedGoals ?? '—'}</td>
                          <td style={{ padding: '0.35rem 0.5rem' }}>{h.expectedAssists ?? '—'}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  );
};

export default PlayerDetailDrawer;
