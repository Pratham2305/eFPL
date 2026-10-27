export interface PickDto {
  playerId: number;
  webName: string;
  teamShortName: string;
  teamCode?: number;
  badgeUrl?: string;
  position: 'GK' | 'DEF' | 'MID' | 'FWD' | string;
  squadPosition: number;
  multiplier: number;
  isCaptain: boolean;
  isViceCaptain: boolean;
  nowCost: number; // in tenths e.g. 100 = £10.0m
  form: number;
  totalPoints: number;
  status: string; // 'a', 'd', 'i', 's', 'u', 'n'
  news: string | null;
}

export interface ChipsAvailable {
  wildcard: boolean;
  freeHit: boolean;
  benchBoost: boolean;
  tripleCaptain: boolean;
}

export interface TeamCurrentResponse {
  gameweekId: number;
  currentGameweekId?: number;
  nextGameweekId?: number;
  overallRank: number;
  overallPoints: number;
  gameweekPoints: number;
  liveTotalPoints?: number;
  bank: number; // in tenths
  teamValue: number; // in tenths
  freeTransfers: number;
  chips: ChipsAvailable;
  startingXI: PickDto[];
  bench: PickDto[];
}

export interface FixtureDto {
  id: number;
  gameweekId: number | null;
  homeTeamName: string;
  homeTeamShortName: string;
  homeTeamCode?: number;
  homeBadgeUrl?: string;
  awayTeamName: string;
  awayTeamShortName: string;
  awayTeamCode?: number;
  awayBadgeUrl?: string;
  homeDifficulty: number; // FDR 1-5
  awayDifficulty: number; // FDR 1-5
  kickoffTime: string;
  finished: boolean;
  homeScore: number | null;
  awayScore: number | null;
}

export interface SyncResponse {
  status: string;
  message: string;
  playersUpdated: number;
  fixturesUpdated: number;
  gameweeksUpdated: number;
}

export interface ChatRequest {
  query: string;
  conversationId?: string | null;
}

export interface ChatResponse {
  query: string;
  reply: string;
  intent: string;
  source: 'LLM' | 'DETERMINISTIC_FALLBACK' | string;
  conversationId: string;
  dataAsOf: string;
  warning?: string | null;
  grounded: boolean;
}

export interface ChatHealth {
  ollamaReachable: boolean;
  modelAvailable: boolean;
  model: string;
  predictionsAvailable: boolean;
  dataAsOf: string;
}

// ─── Player Directory Types ─────────────────────────────────────

export interface PlayerSummaryDto {
  id: number;
  webName: string;
  firstName: string;
  secondName: string;
  teamId: number;
  teamName: string;
  teamShortName: string;
  elementType: number;
  positionName: string;
  nowCost: number; // in tenths
  costChangeStart: number;
  costChangeEvent: number;
  status: string;
  chanceOfPlayingNext: number | null;
  chanceOfPlayingThisRound: number | null;
  news: string | null;
  newsAdded: string | null;
  form: number | null;
  totalPoints: number;
  pointsPerGame: number | null;
  selectedByPercent: number | null;
  minutes: number;
  starts: number;
  goalsScored: number;
  assists: number;
  cleanSheets: number;
  goalsConceded: number;
  ownGoals: number;
  penaltiesSaved: number;
  penaltiesMissed: number;
  yellowCards: number;
  redCards: number;
  saves: number;
  bonus: number;
  bps: number;
  influence: number | null;
  creativity: number | null;
  threat: number | null;
  ictIndex: number | null;
  expectedGoals: number | null;
  expectedAssists: number | null;
  expectedGoalInvolvements: number | null;
  expectedGoalsConceded: number | null;
  transfersInEvent: number;
  transfersOutEvent: number;
  epNext: number | null;
  xpNext: number | null;
  inSquad: boolean;
  isStarting: boolean;
  isCaptain: boolean;
  isViceCaptain: boolean;
  nextOpponentShortName: string | null;
  nextOpponentDifficulty: number | null;
  isDoubleGameweek: boolean;
  isBlankGameweek: boolean;
}

export interface PlayerPageResponse {
  content: PlayerSummaryDto[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  hasNext: boolean;
  hasPrevious: boolean;
}

export interface UpcomingFixtureDto {
  fixtureId: number;
  gameweekId: number;
  gameweekName: string;
  opponentTeamId: number;
  opponentName: string;
  opponentShortName: string;
  isHome: boolean;
  difficulty: number;
  kickoffTime: string;
  isDoubleGameweek: boolean;
  isBlankGameweek: boolean;
}

export interface HistoricalStatDto {
  gameweekId: number;
  opponentShortName: string | null;
  wasHome: boolean | null;
  minutes: number;
  totalPoints: number;
  goalsScored: number;
  assists: number;
  cleanSheets: number;
  goalsConceded: number;
  bonus: number;
  bps: number;
  expectedGoals: number | null;
  expectedAssists: number | null;
  expectedGoalInvolvements: number | null;
}

export interface GameweekPredictionDto {
  gameweekId: number;
  expectedPoints: number;
  floor: number | null;
  ceiling: number | null;
  startProbability: number | null;
}

export interface PlayerDetailDto {
  summary: PlayerSummaryDto;
  xp3: number | null;
  xp6: number | null;
  upcomingFixtures: UpcomingFixtureDto[];
  historicalStats: HistoricalStatDto[];
  predictionsByGameweek: GameweekPredictionDto[];
}

export interface PlayerMetaDto {
  teams: { id: number; name: string; shortName: string }[];
  positions: { elementType: number; shortName: string; singularName: string }[];
  priceRange: { min: number; max: number };
  ownershipRange: { min: number; max: number };
  totalPlayers: number;
  currentGameweek: number | null;
  nextGameweek: number | null;
}
