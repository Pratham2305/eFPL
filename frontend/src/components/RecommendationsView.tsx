import React, { useState, useEffect } from 'react';
import { Sparkles, ArrowRight, ShieldAlert, Award, TrendingUp, Cpu, RefreshCw, Info } from 'lucide-react';
import ClubBadge from './ClubBadge';

interface RecommendationViewProps {
  currentGw: number;
}

interface PredictionStatus {
  modelVersion: string;
  generatedAt: string | null;
  gwCoverage: number[];
  playerCount: number;
}

const RecommendationsView: React.FC<RecommendationViewProps> = ({ currentGw }) => {
  const [transfersData, setTransfersData] = useState<any>({ recommendations: [] });
  const [captainsData, setCaptainsData] = useState<any>({ rankings: [], outside_squad: [] });
  const [weaknessData, setWeaknessData] = useState<any>({ findings: [], squadHealth: null });
  const [differentialsData, setDifferentialsData] = useState<any>({ differentials: [], threshold_used: 12.0 });
  const [predictionStatus, setPredictionStatus] = useState<PredictionStatus | null>(null);

  const [loading, setLoading] = useState<boolean>(true);
  const [isGenerating, setIsGenerating] = useState<boolean>(false);
  const [riskProfile, setRiskProfile] = useState<'safe' | 'balanced' | 'aggressive'>('balanced');
  const [transferHorizon, setTransferHorizon] = useState<number>(3);

  useEffect(() => {
    fetchAllRecommendations();
  }, [currentGw, riskProfile, transferHorizon]);

  const fetchAllRecommendations = async () => {
    setLoading(true);
    try {
      const [statusRes, tRes, cRes, wRes, dRes] = await Promise.all([
        fetch('/api/predictions/status'),
        fetch(`/api/recommendations/transfers?horizon=${transferHorizon}&limit=10`),
        fetch(`/api/recommendations/captain?riskProfile=${riskProfile}`),
        fetch('/api/analysis/weaknesses'),
        fetch('/api/analysis/differentials'),
      ]);

      if (statusRes.ok) {
        const s = await statusRes.json();
        setPredictionStatus(s);
      }
      if (tRes.ok) {
        const d = await tRes.json();
        setTransfersData(d);
      }
      if (cRes.ok) {
        const d = await cRes.json();
        setCaptainsData(d);
      }
      if (wRes.ok) {
        const d = await wRes.json();
        setWeaknessData(d);
      }
      if (dRes.ok) {
        const d = await dRes.json();
        setDifferentialsData(d);
      }
    } catch (err) {
      console.error('Error fetching recommendations:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleRunModel = async () => {
    setIsGenerating(true);
    try {
      await fetch('/api/predictions/generate', { method: 'POST' });
      await fetchAllRecommendations();
    } catch (err) {
      console.error('Failed to generate predictions:', err);
    } finally {
      setIsGenerating(false);
    }
  };

  const getVerdictBadge = (verdict: string) => {
    switch (verdict) {
      case 'RECOMMENDED':
        return { label: 'RECOMMENDED', bg: 'rgba(0, 255, 135, 0.15)', text: 'var(--accent-primary)', border: 'rgba(0, 255, 135, 0.3)' };
      case 'CONSIDER':
        return { label: 'CONSIDER', bg: 'rgba(251, 191, 36, 0.15)', text: 'var(--accent-amber)', border: 'rgba(251, 191, 36, 0.3)' };
      case 'SIDEWAYS':
        return { label: 'SIDEWAYS', bg: 'rgba(56, 189, 248, 0.15)', text: 'var(--accent-cyan)', border: 'rgba(56, 189, 248, 0.3)' };
      case 'NOT_WORTH_HIT':
        return { label: 'NOT WORTH HIT', bg: 'rgba(255, 23, 68, 0.15)', text: '#ff1744', border: 'rgba(255, 23, 68, 0.3)' };
      default:
        return { label: verdict, bg: 'rgba(255,255,255,0.1)', text: 'var(--text-secondary)', border: 'var(--border-color)' };
    }
  };

  const getSeverityBadge = (severity: string) => {
    switch (severity) {
      case 'CRITICAL':
        return { bg: 'rgba(255, 23, 68, 0.15)', text: '#ff1744', border: 'rgba(255, 23, 68, 0.3)' };
      case 'MODERATE':
        return { bg: 'rgba(251, 191, 36, 0.15)', text: 'var(--accent-amber)', border: 'rgba(251, 191, 36, 0.3)' };
      case 'MINOR':
        return { bg: 'rgba(56, 189, 248, 0.15)', text: 'var(--accent-cyan)', border: 'rgba(56, 189, 248, 0.3)' };
      case 'OPPORTUNITY':
        return { bg: 'rgba(0, 255, 135, 0.15)', text: 'var(--accent-primary)', border: 'rgba(0, 255, 135, 0.3)' };
      default:
        return { bg: 'rgba(255, 255, 255, 0.1)', text: 'var(--text-muted)', border: 'var(--border-color)' };
    }
  };

  const formatGeneratedAt = (iso: string | null) => {
    if (!iso) return 'Just now';
    try {
      const d = new Date(iso);
      return d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) + ' (' + d.toLocaleDateString() + ')';
    } catch {
      return iso;
    }
  };

  const transfers = transfersData?.recommendations || [];
  const captainRankings = captainsData?.rankings || [];
  const viceCaptain = captainsData?.vice_captain_suggestion;
  const outsideSquadCaptains = captainsData?.outside_squad || [];
  const findings = weaknessData?.findings || [];
  const squadHealth = weaknessData?.squadHealth;
  const differentials = differentialsData?.differentials || [];

  return (
    <div style={{ maxWidth: 1100, margin: '0 auto', display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
      
      {/* Top Banner & Prediction Status */}
      <div className="glass-panel" style={{
        padding: '1.25rem 1.5rem',
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
        background: 'linear-gradient(135deg, rgba(56,0,60,0.65) 0%, rgba(18,24,41,0.95) 100%)',
        borderColor: 'rgba(0, 255, 135, 0.25)',
        flexWrap: 'wrap',
        gap: '1rem'
      }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.85rem' }}>
          <Sparkles size={28} color="var(--accent-primary)" />
          <div>
            <h2 style={{ fontSize: '1.25rem', fontWeight: 800 }}>Decision Engine & Recommendations</h2>
            <div style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', display: 'flex', alignItems: 'center', gap: '0.5rem', marginTop: 3 }}>
              <span>Squad-aware optimizations for GW {currentGw}</span>
              {predictionStatus?.modelVersion && (
                <>
                  <span>•</span>
                  <span style={{
                    padding: '0.1rem 0.5rem',
                    borderRadius: 4,
                    background: 'rgba(0, 255, 135, 0.12)',
                    color: 'var(--accent-primary)',
                    fontWeight: 600,
                    fontSize: '0.75rem'
                  }}>
                    Model: {predictionStatus.modelVersion}
                  </span>
                  <span>•</span>
                  <span style={{ color: 'var(--text-muted)' }}>
                    Generated {formatGeneratedAt(predictionStatus.generatedAt)}
                  </span>
                </>
              )}
            </div>
          </div>
        </div>

        <button
          id="run-ml-model-btn"
          className="btn btn-primary"
          onClick={handleRunModel}
          disabled={isGenerating}
        >
          <Cpu size={16} className={isGenerating ? 'pulse' : ''} />
          {isGenerating ? 'Recalculating xPoints...' : 'Refresh Predictions'}
        </button>
      </div>

      {loading && (
        <div style={{ textAlign: 'center', padding: '3.5rem', color: 'var(--text-secondary)' }}>
          <RefreshCw size={24} className="pulse" style={{ margin: '0 auto 1rem', display: 'block' }} />
          Calculating squad xPoints, evaluating transfer alternatives & captaincy rankings...
        </div>
      )}

      {!loading && (
        <>
          {/* Section 1: Transfer Recommendations */}
          <div className="glass-panel" style={{ padding: '1.5rem' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.2rem', flexWrap: 'wrap', gap: '0.8rem' }}>
              <div>
                <h3 style={{ fontSize: '1.15rem', fontWeight: 700, display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                  <TrendingUp size={20} color="var(--accent-primary)" />
                  Transfer Alternatives & Upgrades
                </h3>
                <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', marginTop: 2 }}>
                  Available Bank: £{transfersData.bank || 0.0}m • Free Transfers: {transfersData.free_transfers || 1}
                </div>
              </div>

              {/* Horizon Toggle */}
              <div style={{ display: 'flex', gap: 4, background: 'rgba(0,0,0,0.3)', padding: 3, borderRadius: 6 }}>
                {[1, 3, 6].map((hz) => (
                  <button
                    key={hz}
                    onClick={() => setTransferHorizon(hz)}
                    style={{
                      padding: '0.25rem 0.65rem',
                      fontSize: '0.75rem',
                      fontWeight: 600,
                      borderRadius: 4,
                      border: 'none',
                      cursor: 'pointer',
                      background: transferHorizon === hz ? 'var(--bg-card)' : 'transparent',
                      color: transferHorizon === hz ? 'var(--accent-primary)' : 'var(--text-muted)'
                    }}
                  >
                    {hz}-GW Horizon
                  </button>
                ))}
              </div>
            </div>

            {/* Hold Advice Alert if holding is optimal */}
            {transfersData.holdAdvice && (
              <div style={{
                padding: '0.8rem 1rem',
                borderRadius: 'var(--radius-sm)',
                background: 'rgba(56, 189, 248, 0.08)',
                border: '1px solid rgba(56, 189, 248, 0.25)',
                marginBottom: '1rem',
                display: 'flex',
                alignItems: 'center',
                gap: '0.75rem',
                fontSize: '0.82rem',
                color: 'var(--text-primary)'
              }}>
                <Info size={18} color="var(--accent-cyan)" />
                <span>{transfersData.holdAdvice}</span>
              </div>
            )}

            {/* Transfer List */}
            {transfers.length === 0 ? (
              <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>No transfer candidates found. Try refreshing predictions.</p>
            ) : (
              <div style={{ display: 'flex', flexDirection: 'column', gap: '0.85rem' }}>
                {transfers.map((t: any, idx: number) => {
                  const badge = getVerdictBadge(t.verdict);
                  return (
                    <div key={idx} style={{
                      padding: '1rem',
                      borderRadius: 'var(--radius-sm)',
                      background: 'rgba(0,0,0,0.3)',
                      border: '1px solid var(--border-color)',
                      display: 'flex',
                      flexDirection: 'column',
                      gap: '0.6rem'
                    }}>
                      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '0.5rem' }}>
                        
                        {/* Out / In Players */}
                        <div style={{ display: 'flex', alignItems: 'center', gap: '0.8rem', fontSize: '0.92rem', fontWeight: 700 }}>
                          <span style={{ color: '#ff4d6d', display: 'flex', alignItems: 'center', gap: 6 }}>
                            <ClubBadge team={t.transfer_out?.team_short} size={18} />
                            OUT: {t.transfer_out?.web_name}
                            <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)', fontWeight: 400 }}>
                              ({t.transfer_out?.position}, £{t.transfer_out?.selling_price}m)
                            </span>
                          </span>

                          <ArrowRight size={16} color="var(--text-muted)" />

                          <span style={{ color: 'var(--accent-primary)', display: 'flex', alignItems: 'center', gap: 6 }}>
                            <ClubBadge team={t.transfer_in?.team_short} size={18} />
                            IN: {t.transfer_in?.web_name}
                            <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)', fontWeight: 400 }}>
                              ({t.transfer_in?.position}, £{t.transfer_in?.price}m)
                            </span>
                          </span>
                        </div>

                        {/* Verdict & Net Gain */}
                        <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem' }}>
                          <span style={{
                            padding: '0.2rem 0.55rem',
                            borderRadius: 4,
                            background: badge.bg,
                            color: badge.text,
                            border: `1px solid ${badge.border}`,
                            fontWeight: 700,
                            fontSize: '0.75rem',
                            letterSpacing: '0.04em'
                          }}>
                            {badge.label}
                          </span>

                          <span style={{
                            padding: '0.2rem 0.55rem',
                            borderRadius: 4,
                            background: t.net_value >= 0 ? 'rgba(0,255,135,0.12)' : 'rgba(255,255,255,0.06)',
                            color: t.net_value >= 0 ? 'var(--accent-primary)' : 'var(--text-secondary)',
                            fontWeight: 700,
                            fontSize: '0.82rem'
                          }}>
                            {t.net_value > 0 ? `+${t.net_value}` : t.net_value} net xPts ({transferHorizon} GWs)
                          </span>
                        </div>
                      </div>

                      {/* Multi-horizon gains & Bank Info */}
                      <div style={{
                        display: 'flex',
                        gap: '1.2rem',
                        fontSize: '0.76rem',
                        color: 'var(--text-muted)',
                        paddingTop: 4,
                        borderTop: '1px solid rgba(255,255,255,0.05)',
                        flexWrap: 'wrap'
                      }}>
                        <span>1-GW Gain: <strong style={{ color: t.xp_gain_1 >= 0 ? 'var(--accent-primary)' : '#ff4d6d' }}>{t.xp_gain_1 > 0 ? `+${t.xp_gain_1}` : t.xp_gain_1}</strong></span>
                        <span>3-GW Gain: <strong style={{ color: t.xp_gain_3 >= 0 ? 'var(--accent-primary)' : '#ff4d6d' }}>{t.xp_gain_3 > 0 ? `+${t.xp_gain_3}` : t.xp_gain_3}</strong></span>
                        <span>6-GW Gain: <strong style={{ color: t.xp_gain_6 >= 0 ? 'var(--accent-primary)' : '#ff4d6d' }}>{t.xp_gain_6 > 0 ? `+${t.xp_gain_6}` : t.xp_gain_6}</strong></span>
                        <span>Bank After: <strong>£{t.bank_after}m</strong></span>
                        {t.hit_cost > 0 && <span style={{ color: '#ff1744' }}>Hit penalty: -{t.hit_cost} pts</span>}
                      </div>

                      {/* Reasons */}
                      {t.reasons && t.reasons.length > 0 && (
                        <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.4rem', marginTop: 2 }}>
                          {t.reasons.map((r: any, rIdx: number) => (
                            <span key={rIdx} style={{
                              padding: '0.15rem 0.45rem',
                              borderRadius: 4,
                              background: 'rgba(255,255,255,0.05)',
                              border: '1px solid rgba(255,255,255,0.08)',
                              fontSize: '0.72rem',
                              color: 'var(--text-secondary)'
                            }}>
                              {r.detail}
                            </span>
                          ))}
                        </div>
                      )}
                    </div>
                  );
                })}
              </div>
            )}
          </div>

          {/* Section 2: Captaincy Choice Ranker */}
          <div className="glass-panel" style={{ padding: '1.5rem' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.2rem', flexWrap: 'wrap', gap: '0.8rem' }}>
              <div>
                <h3 style={{ fontSize: '1.15rem', fontWeight: 700, display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                  <Award size={20} color="var(--accent-amber)" />
                  Starting XI Captaincy Rankings
                </h3>
                <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', marginTop: 2 }}>
                  All 11 starters evaluated by risk-adjusted expected points
                </div>
              </div>

              {/* Risk Profile Selector */}
              <div style={{ display: 'flex', gap: 4, background: 'rgba(0,0,0,0.3)', padding: 3, borderRadius: 6 }}>
                {(['safe', 'balanced', 'aggressive'] as const).map((prof) => (
                  <button
                    key={prof}
                    onClick={() => setRiskProfile(prof)}
                    style={{
                      padding: '0.25rem 0.65rem',
                      fontSize: '0.75rem',
                      fontWeight: 600,
                      borderRadius: 4,
                      border: 'none',
                      cursor: 'pointer',
                      textTransform: 'capitalize',
                      background: riskProfile === prof ? 'var(--bg-card)' : 'transparent',
                      color: riskProfile === prof ? 'var(--accent-amber)' : 'var(--text-muted)'
                    }}
                  >
                    {prof}
                  </button>
                ))}
              </div>
            </div>

            {/* Vice Captain Banner */}
            {viceCaptain && (
              <div style={{
                padding: '0.75rem 1rem',
                borderRadius: 'var(--radius-sm)',
                background: 'rgba(251, 191, 36, 0.08)',
                border: '1px solid rgba(251, 191, 36, 0.25)',
                marginBottom: '1rem',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
                flexWrap: 'wrap',
                gap: '0.5rem',
                fontSize: '0.82rem'
              }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                  <span style={{ fontSize: '1.1rem' }}>🥈</span>
                  <span>
                    <strong>Recommended Vice-Captain:</strong> {viceCaptain.web_name} ({viceCaptain.team_short}) vs {viceCaptain.opponent} ({viceCaptain.is_home ? 'H' : 'A'})
                  </span>
                </div>
                <span style={{ color: 'var(--accent-amber)', fontWeight: 600 }}>
                  Reliable floor: {viceCaptain.floor} pts ({Math.round(viceCaptain.start_probability * 100)}% start)
                </span>
              </div>
            )}

            {/* Starting XI Captain Rankings Table */}
            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.55rem' }}>
              {captainRankings.map((c: any) => {
                const isTop = c.rank === 1;
                return (
                  <div key={c.player_id} style={{
                    padding: '0.75rem 1rem',
                    borderRadius: 'var(--radius-sm)',
                    background: isTop ? 'rgba(251,191,36,0.12)' : 'rgba(0,0,0,0.22)',
                    border: isTop ? '1px solid rgba(251,191,36,0.4)' : '1px solid var(--border-color)',
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center',
                    flexWrap: 'wrap',
                    gap: '0.6rem'
                  }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                      <span style={{ fontWeight: 800, fontSize: '0.9rem', color: isTop ? 'var(--accent-amber)' : 'var(--text-muted)', minWidth: 24 }}>
                        {isTop ? '👑' : `#${c.rank}`}
                      </span>
                      <ClubBadge team={c.team_short} size={18} />
                      <div>
                        <div style={{ fontWeight: 700, fontSize: '0.9rem', display: 'flex', alignItems: 'center', gap: 6 }}>
                          <span>{c.web_name}</span>
                          <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>({c.position})</span>
                          {c.is_current_captain && (
                            <span style={{ fontSize: '0.68rem', padding: '0.1rem 0.35rem', borderRadius: 3, background: 'rgba(251,191,36,0.2)', color: 'var(--accent-amber)' }}>
                              Current (C)
                            </span>
                          )}
                          {c.rotation_risk && (
                            <span style={{ fontSize: '0.68rem', padding: '0.1rem 0.35rem', borderRadius: 3, background: 'rgba(255,23,68,0.2)', color: '#ff1744' }}>
                              Rotation Risk
                            </span>
                          )}
                        </div>
                        <div style={{ fontSize: '0.74rem', color: 'var(--text-secondary)', marginTop: 2 }}>
                          vs {c.opponent} ({c.is_home ? 'H' : 'A'}) • FDR {c.fdr} • {c.reason}
                        </div>
                      </div>
                    </div>

                    <div style={{ display: 'flex', gap: '1rem', fontSize: '0.8rem', alignItems: 'center' }}>
                      <span style={{ color: 'var(--text-muted)' }}>Floor: <strong>{c.floor}</strong></span>
                      <span style={{ color: 'var(--accent-primary)', fontSize: '0.9rem' }}>xP: <strong>{c.expected_points}</strong></span>
                      <span style={{ color: 'var(--text-muted)' }}>Ceiling: <strong>{c.ceiling}</strong></span>
                      <span style={{
                        padding: '0.2rem 0.5rem',
                        borderRadius: 4,
                        background: isTop ? 'rgba(251,191,36,0.25)' : 'rgba(255,255,255,0.08)',
                        color: isTop ? 'var(--accent-amber)' : 'var(--text-primary)',
                        fontWeight: 700,
                        fontSize: '0.78rem'
                      }}>
                        Score: {c.score}
                      </span>
                    </div>
                  </div>
                );
              })}
            </div>

            {/* Outside Squad Captain Picks */}
            {outsideSquadCaptains.length > 0 && (
              <div style={{ marginTop: '1.2rem', paddingTop: '1rem', borderTop: '1px solid rgba(255,255,255,0.08)' }}>
                <h4 style={{ fontSize: '0.82rem', fontWeight: 700, color: 'var(--text-muted)', marginBottom: '0.6rem', textTransform: 'uppercase', letterSpacing: '0.04em' }}>
                  League Top Alternatives (Outside Your Squad)
                </h4>
                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: '0.6rem' }}>
                  {outsideSquadCaptains.map((o: any) => (
                    <div key={o.player_id} style={{
                      padding: '0.6rem 0.8rem',
                      borderRadius: 'var(--radius-sm)',
                      background: 'rgba(0,0,0,0.2)',
                      border: '1px solid var(--border-color)',
                      display: 'flex',
                      justifyContent: 'space-between',
                      alignItems: 'center'
                    }}>
                      <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                        <ClubBadge team={o.team_short} size={16} />
                        <div>
                          <div style={{ fontWeight: 700, fontSize: '0.82rem' }}>{o.web_name}</div>
                          <div style={{ fontSize: '0.7rem', color: 'var(--text-muted)' }}>vs {o.opponent} ({o.is_home ? 'H' : 'A'}) • {o.ownership_percent}% owned</div>
                        </div>
                      </div>
                      <span style={{ fontWeight: 700, color: 'var(--accent-primary)', fontSize: '0.85rem' }}>
                        {o.expected_points} xPts
                      </span>
                    </div>
                  ))}
                </div>
              </div>
            )}
          </div>

          {/* Section 3: Squad Weaknesses & Relative Nitpicking */}
          <div className="glass-panel" style={{ padding: '1.5rem' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.2rem', flexWrap: 'wrap', gap: '0.8rem' }}>
              <div>
                <h3 style={{ fontSize: '1.15rem', fontWeight: 700, display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                  <ShieldAlert size={20} color="#ff4d6d" />
                  Squad Weaknesses & Structural Findings
                </h3>
                <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', marginTop: 2 }}>
                  Relative benchmarking against position and price bracket peers
                </div>
              </div>

              {/* Squad Health Gauge */}
              {squadHealth && (
                <div style={{
                  display: 'flex',
                  alignItems: 'center',
                  gap: '0.8rem',
                  padding: '0.4rem 0.8rem',
                  borderRadius: 'var(--radius-sm)',
                  background: 'rgba(0,0,0,0.3)',
                  border: '1px solid var(--border-color)'
                }}>
                  <span style={{ fontSize: '0.78rem', color: 'var(--text-muted)' }}>Squad Health:</span>
                  <span style={{
                    fontWeight: 800,
                    fontSize: '1rem',
                    color: squadHealth.score >= 80 ? 'var(--accent-primary)' : squadHealth.score >= 65 ? 'var(--accent-amber)' : '#ff4d6d'
                  }}>
                    {squadHealth.score} / 100
                  </span>
                </div>
              )}
            </div>

            {/* Health Summary */}
            {squadHealth?.summary && (
              <div style={{
                padding: '0.75rem 1rem',
                borderRadius: 'var(--radius-sm)',
                background: 'rgba(255,255,255,0.03)',
                border: '1px solid rgba(255,255,255,0.06)',
                marginBottom: '1rem',
                fontSize: '0.82rem',
                color: 'var(--text-secondary)'
              }}>
                {squadHealth.summary}
              </div>
            )}

            {/* Findings List */}
            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.85rem' }}>
              {findings.map((f: any, idx: number) => {
                const sev = getSeverityBadge(f.severity);
                return (
                  <div key={idx} style={{
                    padding: '1rem',
                    borderRadius: 'var(--radius-sm)',
                    background: 'rgba(0,0,0,0.25)',
                    border: '1px solid var(--border-color)',
                    display: 'flex',
                    flexDirection: 'column',
                    gap: '0.5rem'
                  }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '0.4rem' }}>
                      <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                        <span style={{
                          padding: '0.15rem 0.5rem',
                          borderRadius: 4,
                          background: sev.bg,
                          color: sev.text,
                          border: `1px solid ${sev.border}`,
                          fontWeight: 700,
                          fontSize: '0.7rem'
                        }}>
                          {f.severity}
                        </span>
                        <span style={{ fontWeight: 700, fontSize: '0.9rem' }}>
                          {f.title}
                        </span>
                      </div>

                      <span style={{ fontSize: '0.72rem', color: 'var(--text-muted)' }}>
                        Priority Impact: {f.score}/100
                      </span>
                    </div>

                    {/* Evidence Badges */}
                    {f.evidence && f.evidence.length > 0 && (
                      <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.5rem', marginTop: 2 }}>
                        {f.evidence.map((ev: any, evIdx: number) => (
                          <span key={evIdx} style={{
                            padding: '0.2rem 0.5rem',
                            borderRadius: 4,
                            background: 'rgba(255,255,255,0.04)',
                            border: '1px solid rgba(255,255,255,0.07)',
                            fontSize: '0.74rem',
                            color: 'var(--text-secondary)'
                          }}>
                            <strong>{ev.label}:</strong> {ev.value}
                          </span>
                        ))}
                      </div>
                    )}

                    {/* Actionable Recommendation */}
                    {f.recommendation && (
                      <div style={{ fontSize: '0.78rem', color: 'var(--text-secondary)', fontStyle: 'italic', marginTop: 2 }}>
                        💡 {f.recommendation}
                      </div>
                    )}

                    {/* Suggested Alternatives */}
                    {f.suggestedAlternatives && f.suggestedAlternatives.length > 0 && (
                      <div style={{ marginTop: 4, paddingTop: '0.5rem', borderTop: '1px solid rgba(255,255,255,0.05)' }}>
                        <span style={{ fontSize: '0.72rem', color: 'var(--text-muted)', fontWeight: 600 }}>
                          Viable Bracket Alternatives:
                        </span>
                        <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.5rem', marginTop: 4 }}>
                          {f.suggestedAlternatives.map((alt: any) => (
                            <span key={alt.player_id} style={{
                              display: 'inline-flex',
                              alignItems: 'center',
                              gap: 5,
                              padding: '0.25rem 0.55rem',
                              borderRadius: 4,
                              background: 'rgba(0, 255, 135, 0.08)',
                              border: '1px solid rgba(0, 255, 135, 0.2)',
                              fontSize: '0.76rem',
                              color: 'var(--text-primary)'
                            }}>
                              <ClubBadge team={alt.team_short} size={14} />
                              <strong>{alt.web_name}</strong>
                              <span style={{ color: 'var(--text-muted)' }}>(£{alt.now_cost}m)</span>
                              <span style={{ color: 'var(--accent-primary)', fontWeight: 700 }}>+{alt.expected_points} xP</span>
                            </span>
                          ))}
                        </div>
                      </div>
                    )}
                  </div>
                );
              })}
            </div>
          </div>

          {/* Section 4: Differential Targets */}
          <div className="glass-panel" style={{ padding: '1.5rem' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.2rem', flexWrap: 'wrap', gap: '0.8rem' }}>
              <div>
                <h3 style={{ fontSize: '1.15rem', fontWeight: 700, display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                  <Sparkles size={20} color="var(--accent-cyan)" />
                  Differential Targets & Market Inefficiencies
                </h3>
                <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', marginTop: 2 }}>
                  High expected points players with low ownership for rapid rank climb
                </div>
              </div>

              <span style={{
                padding: '0.2rem 0.6rem',
                borderRadius: 4,
                background: 'rgba(56, 189, 248, 0.15)',
                color: 'var(--accent-cyan)',
                border: '1px solid rgba(56, 189, 248, 0.3)',
                fontWeight: 600,
                fontSize: '0.75rem'
              }}>
                Ownership &lt; {differentialsData.threshold_used || 12}%
              </span>
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: '0.85rem' }}>
              {differentials.map((d: any) => (
                <div key={d.player_id} style={{
                  padding: '0.85rem 1rem',
                  borderRadius: 'var(--radius-sm)',
                  background: 'rgba(56,189,248,0.06)',
                  border: '1px solid rgba(56,189,248,0.2)',
                  display: 'flex',
                  justifyContent: 'space-between',
                  alignItems: 'center'
                }}>
                  <div>
                    <div style={{ fontWeight: 700, fontSize: '0.9rem', display: 'flex', alignItems: 'center', gap: 6 }}>
                      <span>{d.web_name}</span>
                      <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>({d.position_name})</span>
                    </div>
                    <div style={{ fontSize: '0.74rem', color: 'var(--text-muted)', display: 'flex', alignItems: 'center', gap: 5, marginTop: 3 }}>
                      <ClubBadge team={d.team_short} size={14} alt={d.team_short} />
                      <span>{d.team_short} • {d.ownership_percent}% owned • £{d.now_cost}m</span>
                    </div>
                  </div>
                  <div style={{ textAlign: 'right' }}>
                    <div style={{ fontWeight: 800, fontSize: '0.92rem', color: 'var(--accent-cyan)' }}>
                      {d.expected_points} xPts
                    </div>
                    <div style={{ fontSize: '0.7rem', color: 'var(--text-muted)' }}>
                      Ceiling: {d.ceiling}
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </>
      )}
    </div>
  );
};

export default RecommendationsView;
