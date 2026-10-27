// Premier League official alternative SVG badges
export const CLUB_CODE_BY_SHORT_NAME: Record<string, number> = {
  ARS: 3,
  AVL: 7,
  BOU: 91,
  BRE: 94,
  BHA: 36,
  CHE: 8,
  COV: 9,
  CRY: 31,
  EVE: 11,
  FUL: 54,
  HUL: 88,
  IPS: 40,
  LEE: 2,
  LIV: 14,
  MCI: 43,
  MUN: 1,
  NEW: 4,
  NFO: 17,
  SUN: 56,
  TOT: 6,
  WOL: 39,
  WHU: 21,
  SOU: 20,
  LEI: 13,
};

export const CLUB_CODE_BY_NAME: Record<string, number> = {
  arsenal: 3,
  'aston villa': 7,
  bournemouth: 91,
  'afc bournemouth': 91,
  brentford: 94,
  brighton: 36,
  'brighton & hove albion': 36,
  'brighton and hove albion': 36,
  chelsea: 8,
  'coventry city': 9,
  coventry: 9,
  'crystal palace': 31,
  everton: 11,
  fulham: 54,
  'hull city': 88,
  hull: 88,
  'ipswich town': 40,
  ipswich: 40,
  'leeds united': 2,
  leeds: 2,
  liverpool: 14,
  'manchester city': 43,
  'man city': 43,
  'manchester united': 1,
  'man utd': 1,
  'newcastle united': 4,
  newcastle: 4,
  'nottingham forest': 17,
  'nott\'m forest': 17,
  sunderland: 56,
  'tottenham hotspur': 6,
  tottenham: 6,
  spurs: 6,
  wolverhampton: 39,
  wolves: 39,
  'west ham united': 21,
  'west ham': 21,
  southampton: 20,
  'leicester city': 13,
  leicester: 13,
};

export function getClubCode(identifier: string | number | undefined | null): number | null {
  if (identifier == null) return null;
  if (typeof identifier === 'number') return identifier;

  const trimmed = String(identifier).trim();
  if (/^\d+$/.test(trimmed)) {
    return parseInt(trimmed, 10);
  }

  const upper = trimmed.toUpperCase();
  if (CLUB_CODE_BY_SHORT_NAME[upper]) {
    return CLUB_CODE_BY_SHORT_NAME[upper];
  }

  const lower = trimmed.toLowerCase();
  if (CLUB_CODE_BY_NAME[lower]) {
    return CLUB_CODE_BY_NAME[lower];
  }

  return null;
}

export function getClubBadgeUrl(identifier: string | number | undefined | null): string {
  const code = getClubCode(identifier);
  if (code) {
    return `https://resources.premierleague.com/premierleague25/badges-alt/${code}.svg`;
  }
  // Fallback to Premier League lion crest if code is unrecognized
  return 'https://resources.premierleague.com/premierleague25/badges-alt/3.svg';
}
