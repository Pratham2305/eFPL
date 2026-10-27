import React, { useState, useEffect } from 'react';
import { Layers, ShieldCheck, Zap } from 'lucide-react';

interface ChipStrategyViewProps {
  currentGw: number;
}

const ChipStrategyView: React.FC<ChipStrategyViewProps> = ({ currentGw }) => {
  const [strategy, setStrategy] = useState<any>(null);
  const [loading, setLoading] = useState<boolean>(true);

  useEffect(() => {
    fetchChipStrategy();
  }, [currentGw]);

  const fetchChipStrategy = async () => {
    setLoading(true);
    try {
      const res = await fetch('/api/recommendations/chips?horizon=8');
      if (res.ok) {
        const data = await res.json();
        setStrategy(data.chip_strategy);
      }
    } catch (err) {
      console.error('Failed to load chip strategy:', err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ maxWidth: 950, margin: '0 auto' }}>
      {/* Header */}
      <div className="glass-panel" style={{
        padding: '1.25rem 1.5rem',
        marginBottom: '1.5rem',
        background: 'linear-gradient(135deg, rgba(56,0,60,0.6) 0%, rgba(18,24,41,0.9) 100%)',
        borderColor: 'rgba(251, 191, 36, 0.3)'
      }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.8rem' }}>
          <Layers size={24} color="var(--accent-amber)" />
          <div>
            <h2 style={{ fontSize: '1.2rem', fontWeight: 800 }}>Forward-Looking Chip Deployment Strategy</h2>
            <p style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>
              Evaluates Blank Gameweeks (BGW), Double Gameweeks (DGW), and fixture swings over an 8-GW horizon
            </p>
          </div>
        </div>
      </div>

      {loading && <div style={{ textAlign: 'center', padding: '3rem', color: 'var(--text-secondary)' }}>Analyzing fixture swings & chip windows...</div>}

      {!loading && strategy && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
          {/* Status Bar */}
          <div className="glass-panel" style={{ padding: '1rem 1.25rem', display: 'flex', gap: '1.5rem', flexWrap: 'wrap' }}>
            {Object.entries(strategy.chips_available || {}).map(([chip, available]) => (
              <div key={chip} style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                <ShieldCheck size={16} color={available ? 'var(--accent-primary)' : 'var(--text-muted)'} />
                <span style={{ fontSize: '0.85rem', fontWeight: 600, textTransform: 'capitalize' }}>
                  {chip.replace('_', ' ')}:
                </span>
                <span style={{
                  fontSize: '0.8rem',
                  fontWeight: 700,
                  color: available ? 'var(--accent-primary)' : 'var(--text-muted)'
                }}>
                  {available ? 'AVAILABLE' : 'USED'}
                </span>
              </div>
            ))}
          </div>

          {/* Recommendations Timeline */}
          <div className="glass-panel" style={{ padding: '1.25rem' }}>
            <h3 style={{ fontSize: '1.05rem', fontWeight: 700, marginBottom: '1rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              <Zap size={18} color="var(--accent-amber)" />
              Recommended Chip Roadmap
            </h3>

            {(!strategy.recommendations || strategy.recommendations.length === 0) ? (
              <div style={{ padding: '1.5rem', textAlign: 'center', color: 'var(--text-secondary)', fontSize: '0.9rem' }}>
                No immediate chip deployment recommended for current horizon. Save chips for upcoming Blank/Double Gameweek swings.
              </div>
            ) : (
              <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                {strategy.recommendations.map((item: any, idx: number) => (
                  <div key={idx} style={{
                    padding: '1rem',
                    borderRadius: 'var(--radius-sm)',
                    background: 'rgba(0,0,0,0.3)',
                    borderLeft: '4px solid var(--accent-amber)',
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center',
                    gap: '1rem'
                  }}>
                    <div>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '0.8rem', marginBottom: 4 }}>
                        <span style={{
                          padding: '0.2rem 0.6rem',
                          borderRadius: 4,
                          background: 'rgba(251, 191, 36, 0.2)',
                          color: 'var(--accent-amber)',
                          fontWeight: 800,
                          fontSize: '0.85rem'
                        }}>
                          {item.chip}
                        </span>
                        <span style={{ fontWeight: 700, fontSize: '0.95rem' }}>Gameweek {item.recommended_gw}</span>
                      </div>
                      <p style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>{item.reasoning}</p>
                    </div>

                    <div style={{ textAlign: 'right', minWidth: 90 }}>
                      <div style={{ fontSize: '0.7rem', color: 'var(--text-muted)' }}>CONFIDENCE</div>
                      <div style={{ fontWeight: 800, fontSize: '1rem', color: 'var(--accent-primary)' }}>
                        {(item.confidence * 100).toFixed(0)}%
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
};

export default ChipStrategyView;
