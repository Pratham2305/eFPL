import React from 'react';
import { ChipsAvailable } from '../types';

interface ChipBadgesProps {
  chips: ChipsAvailable | undefined;
}

const ChipBadges: React.FC<ChipBadgesProps> = ({ chips }) => {
  if (!chips) return null;

  const items = [
    { label: 'WC', active: chips.wildcard, full: 'Wildcard' },
    { label: 'FH', active: chips.freeHit, full: 'Free Hit' },
    { label: 'BB', active: chips.benchBoost, full: 'Bench Boost' },
    { label: 'TC', active: chips.tripleCaptain, full: 'Triple Captain' },
  ];

  return (
    <div style={{ display: 'flex', gap: '0.3rem' }}>
      {items.map((c) => (
        <span
          key={c.label}
          title={`${c.full}: ${c.active ? 'Available' : 'Used'}`}
          style={{
            padding: '0.15rem 0.4rem',
            borderRadius: 4,
            fontSize: '0.7rem',
            fontWeight: 700,
            background: c.active ? 'rgba(0, 255, 135, 0.2)' : 'rgba(255, 255, 255, 0.05)',
            color: c.active ? 'var(--accent-primary)' : 'var(--text-muted)',
            border: c.active ? '1px solid rgba(0, 255, 135, 0.4)' : '1px solid rgba(255, 255, 255, 0.05)',
            opacity: c.active ? 1 : 0.4,
          }}
        >
          {c.label}
        </span>
      ))}
    </div>
  );
};

export default ChipBadges;
