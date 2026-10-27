export interface PickDto {
  playerId: number;
  webName: string;
  teamShortName: string;
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
  overallRank: number;
  overallPoints: number;
  gameweekPoints: number;
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
  awayTeamName: string;
  awayTeamShortName: string;
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
