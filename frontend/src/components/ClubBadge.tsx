import React, { useState } from 'react';
import { getClubBadgeUrl } from '../utils/clubBadges';

interface ClubBadgeProps {
  team: string | number | undefined | null;
  size?: number;
  className?: string;
  style?: React.CSSProperties;
  alt?: string;
  fallbackText?: string;
}

export const ClubBadge: React.FC<ClubBadgeProps> = ({
  team,
  size = 20,
  className = '',
  style = {},
  alt,
  fallbackText,
}) => {
  const [hasError, setHasError] = useState(false);
  const badgeUrl = getClubBadgeUrl(team);
  const label = alt || (typeof team === 'string' ? team : `Club ${team || ''}`);

  if (hasError) {
    return (
      <span
        className={`club-badge-fallback ${className}`}
        style={{
          display: 'inline-flex',
          alignItems: 'center',
          justifyContent: 'center',
          width: size,
          height: size,
          borderRadius: 4,
          background: 'rgba(255, 255, 255, 0.1)',
          fontSize: Math.max(9, Math.floor(size * 0.45)),
          fontWeight: 700,
          color: 'var(--text-secondary)',
          lineHeight: 1,
          ...style,
        }}
        title={label}
      >
        {fallbackText || (typeof team === 'string' && team.length <= 4 ? team : '⚽')}
      </span>
    );
  }

  return (
    <img
      src={badgeUrl}
      alt={label}
      title={label}
      width={size}
      height={size}
      className={`club-badge ${className}`}
      loading="lazy"
      onError={() => setHasError(true)}
      style={{
        width: size,
        height: size,
        objectFit: 'contain',
        display: 'inline-block',
        verticalAlign: 'middle',
        filter: 'drop-shadow(0 1px 2px rgba(0, 0, 0, 0.4))',
        ...style,
      }}
    />
  );
};

export default ClubBadge;
