import React, { useState, useEffect } from 'react';
import { Sparkles, ArrowRight, ShieldAlert, Award, TrendingUp, Cpu } from 'lucide-react';

interface RecommendationViewProps {
  currentGw: number;
}

const RecommendationsView: React.FC<RecommendationViewProps> = ({ currentGw }) => {
  const [transfers, setTransfers] = useState<any[]>([]);
  const [captains, setCaptains] = useState<{ safe: any[]; differential: any[] }>({ safe: [], differential: [] });
  const [weaknesses, setWeaknesses] = useState<any[]>([]);
  const [differentials, setDifferentials] = useState<any[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [isGenerating, setIsGenerating] = useState<boolean>(false);
  const [activeCapTab, setActiveCapTab] = useState<'safe' | 'differential'>('safe');

  useEffect(() => {
    fetchAllRecommendations();
  }, [currentGw]);

  const fetchAllRecommendations = async () => {
    setLoading(true);
    try {
      const [tRes, cRes, wRes, dRes] = await Promise.all([
        fetch('/api/recommendations/transfers?horizon=3'),
        fetch('/api/recommendations/captain'),
        fetch('/api/analysis/weaknesses'),
        fetch('/api/analysis/differentials?maxOwnership=12.0'),
      ]);

      if (tRes.ok) {
        const d = await tRes.json();
        setTransfers(d.recommendations || []);
      }
      if (cRes.ok) {
        const d = await cRes.json();
        setCaptains(d.captaincy_rankings || { safe: [], differential: [] });
      }
      if (wRes.ok) {
        const d = await wRes.json();
        setWeaknesses(d.weaknesses || []);
      }
      if (dRes.ok) {
        const d = await dRes.json();
        setDifferentials(d.differentials || []);
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

  return (
    <div style={{ maxWidth: 1050, margin: '0 auto' }}>
      {/* Top Banner */}
      <div className="glass-panel" style={{
        padding: '1.25rem 1.5rem',
        marginBottom: '1.5rem',
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
        background: 'linear-gradient(135deg, rgba(56,0,60,0.6) 0%, rgba(18,24,41,0.9) 100%)',
        borderColor: 'rgba(0, 255, 135, 0.2)'
      }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.8rem' }}>
          <Sparkles size={24} color="var(--accent-primary)" />
          <div>
            <h2 style={{ fontSize: '1.2rem', fontWeight: 800 }}>ML Decision Engine & AI Assistant</h2>
            <p style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>
              Squad-aware transfer optimization, risk-adjusted captaincy, and differential detection
            </p>
          </div>
        </div>

        <button
          id="run-ml-model-btn"
          className="btn btn-primary"
          onClick={handleRunModel}
          disabled={isGenerating}
        >
          <Cpu size={16} className={isGenerating ? 'pulse' : ''} />
          {isGenerating ? 'Running xPoints Model...' : 'Re-Run ML Models'}
        </button>
      </div>

      {loading && (
        <div style={{ textAlign: 'center', padding: '3rem', color: 'var(--text-secondary)' }}>
          Analyzing squad xPoints & running transfer scoring algorithms...
        </div>
      )}

      {!loading && (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(480px, 1fr))', gap: '1.5rem' }}>
          
          {/* Section 1: Transfer Recommendations */}
          <div className="glass-panel" style={{ padding: '1.25rem' }}>
            <h3 style={{ fontSize: '1.05rem', fontWeight: 700, marginBottom: '1rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              <TrendingUp size={18} color="var(--accent-primary)" />
              Top Transfer Recommendations (3-GW Horizon)
            </h3>

            {transfers.length === 0 ? (
              <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>No positive transfer options found or run ML models first.</p>
            ) : (
              <div style={{ display: 'flex', flexDirection: 'column', gap: '0.8rem' }}>
                {transfers.slice(0, 4).map((t, idx) => (
                  <div key={idx} style={{
                    padding: '0.8rem',
                    borderRadius: 'var(--radius-sm)',
                    background: 'rgba(0,0,0,0.3)',
                    border: '1px solid var(--border-color)'
                  }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 4 }}>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem', fontSize: '0.9rem', fontWeight: 700 }}>
                        <span style={{ color: '#ff1744' }}>OUT: {t.transfer_out.web_name}</span>
                        <ArrowRight size={14} color="var(--text-muted)" />
                        <span style={{ color: 'var(--accent-primary)' }}>IN: {t.transfer_in.web_name}</span>
                      </div>
                      <span style={{
                        padding: '0.15rem 0.5rem',
                        borderRadius: 4,
                        background: 'rgba(0,255,135,0.15)',
                        color: 'var(--accent-primary)',
                        fontWeight: 700,
                        fontSize: '0.8rem'
                      }}>
                        +{t.net_points_gain} net xPts
                      </span>
                    </div>
                    <div style={{ fontSize: '0.75rem', color: 'var(--text-secondary)' }}>
                      {t.reasoning}
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>

          {/* Section 2: Captaincy Choice Ranker */}
          <div className="glass-panel" style={{ padding: '1.25rem' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
              <h3 style={{ fontSize: '1.05rem', fontWeight: 700, display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                <Award size={18} color="var(--accent-amber)" />
                Captaincy Rankings
              </h3>

              <div style={{ display: 'flex', gap: 4, background: 'rgba(0,0,0,0.3)', padding: 2, borderRadius: 6 }}>
                <button
                  onClick={() => setActiveCapTab('safe')}
                  style={{
                    padding: '0.2rem 0.6rem',
                    fontSize: '0.75rem',
                    fontWeight: 600,
                    borderRadius: 4,
                    border: 'none',
                    cursor: 'pointer',
                    background: activeCapTab === 'safe' ? 'var(--bg-card)' : 'transparent',
                    color: activeCapTab === 'safe' ? 'var(--accent-primary)' : 'var(--text-muted)'
                  }}
                >
                  Safe (Floor)
                </button>
                <button
                  onClick={() => setActiveCapTab('differential')}
                  style={{
                    padding: '0.2rem 0.6rem',
                    fontSize: '0.75rem',
                    fontWeight: 600,
                    borderRadius: 4,
                    border: 'none',
                    cursor: 'pointer',
                    background: activeCapTab === 'differential' ? 'var(--bg-card)' : 'transparent',
                    color: activeCapTab === 'differential' ? 'var(--accent-amber)' : 'var(--text-muted)'
                  }}
                >
                  Differential (Ceiling)
                </button>
              </div>
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.6rem' }}>
              {(captains[activeCapTab] || []).map((c: any, idx: number) => (
                <div key={c.player_id} style={{
                  padding: '0.6rem 0.8rem',
                  borderRadius: 'var(--radius-sm)',
                  background: idx === 0 ? 'rgba(251,191,36,0.1)' : 'rgba(0,0,0,0.2)',
                  border: idx === 0 ? '1px solid rgba(251,191,36,0.3)' : '1px solid var(--border-color)',
                  display: 'flex',
                  justifyContent: 'space-between',
                  alignItems: 'center'
                }}>
                  <div>
                    <span style={{ fontWeight: 700, fontSize: '0.9rem', marginRight: 6 }}>
                      {idx === 0 ? '👑 ' : idx === 1 ? '🥈 ' : '🥉 '}{c.web_name}
                    </span>
                    <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>({c.team_short})</span>
                  </div>

                  <div style={{ display: 'flex', gap: '0.8rem', fontSize: '0.8rem' }}>
                    <span>Floor: <strong>{c.floor}</strong></span>
                    <span style={{ color: 'var(--accent-primary)' }}>xP: <strong>{c.expected_points}</strong></span>
                    <span>Ceiling: <strong>{c.ceiling}</strong></span>
                  </div>
                </div>
              ))}
            </div>
          </div>

          {/* Section 3: Squad Weaknesses */}
          <div className="glass-panel" style={{ padding: '1.25rem' }}>
            <h3 style={{ fontSize: '1.05rem', fontWeight: 700, marginBottom: '1rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              <ShieldAlert size={18} color="#ff1744" />
              Squad Weaknesses & Red Flags
            </h3>

            {weaknesses.length === 0 ? (
              <p style={{ fontSize: '0.85rem', color: 'var(--accent-primary)' }}>✅ Your starting XI has strong expected points across all positions!</p>
            ) : (
              <div style={{ display: 'flex', flexDirection: 'column', gap: '0.6rem' }}>
                {weaknesses.map((w: any) => (
                  <div key={w.player_id} style={{
                    padding: '0.6rem 0.8rem',
                    borderRadius: 'var(--radius-sm)',
                    background: 'rgba(255,23,68,0.08)',
                    border: '1px solid rgba(255,23,68,0.2)',
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center'
                  }}>
                    <div>
                      <span style={{ fontWeight: 700, fontSize: '0.85rem' }}>{w.web_name} ({w.position_name})</span>
                      <div style={{ fontSize: '0.72rem', color: '#ff1744' }}>{w.issue}</div>
                    </div>
                    <span style={{ fontWeight: 700, fontSize: '0.85rem', color: 'var(--text-muted)' }}>
                      {w.expected_points} xPts
                    </span>
                  </div>
                ))}
              </div>
            )}
          </div>

          {/* Section 4: Differential Targets */}
          <div className="glass-panel" style={{ padding: '1.25rem' }}>
            <h3 style={{ fontSize: '1.05rem', fontWeight: 700, marginBottom: '1rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              <Sparkles size={18} color="var(--accent-cyan)" />
              Differential Target Gems (&lt;12% Ownership)
            </h3>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.6rem' }}>
              {differentials.slice(0, 4).map((d: any) => (
                <div key={d.player_id} style={{
                  padding: '0.6rem 0.8rem',
                  borderRadius: 'var(--radius-sm)',
                  background: 'rgba(56,189,248,0.08)',
                  border: '1px solid rgba(56,189,248,0.2)',
                  display: 'flex',
                  justifyContent: 'space-between',
                  alignItems: 'center'
                }}>
                  <div>
                    <span style={{ fontWeight: 700, fontSize: '0.85rem' }}>{d.web_name} ({d.position_name})</span>
                    <div style={{ fontSize: '0.72rem', color: 'var(--text-muted)' }}>
                      {d.team_short} · {d.ownership_percent}% owned · £{(d.now_cost / 10).toFixed(1)}m
                    </div>
                  </div>
                  <div style={{ textAlign: 'right' }}>
                    <div style={{ fontWeight: 700, fontSize: '0.85rem', color: 'var(--accent-cyan)' }}>{d.expected_points} xPts</div>
                    <div style={{ fontSize: '0.7rem', color: 'var(--text-muted)' }}>Ceiling {d.ceiling}</div>
                  </div>
                </div>
              ))}
            </div>
          </div>

        </div>
      )}
    </div>
  );
};

export default RecommendationsView;
