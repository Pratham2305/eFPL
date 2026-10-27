import React, { useState, useEffect } from 'react';
import { FixtureDto } from '../types';
import { Calendar, Filter } from 'lucide-react';

interface FixtureMatrixProps {
  currentGw: number;
}

interface TeamRun {
  teamName: string;
  shortName: string;
  fixtures: { [gw: number]: { opponentShort: string; isHome: boolean; difficulty: number }[] };
}

const FixtureMatrix: React.FC<FixtureMatrixProps> = ({ currentGw }) => {
  const [fixtures, setFixtures] = useState<FixtureDto[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [gwRange, setGwRange] = useState<number>(5);

  const startGw = currentGw || 1;
  const endGw = startGw + gwRange - 1;
  const gws = Array.from({ length: gwRange }, (_, i) => startGw + i);

  useEffect(() => {
    fetchFixtures();
  }, [startGw, endGw]);

  const fetchFixtures = async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await fetch(`/api/fixtures?gameweekId=${startGw}&rangeEnd=${endGw}`);
      if (!res.ok) throw new Error('Failed to load fixtures');
      const data = await res.json();
      setFixtures(data);
    } catch (err: any) {
      setError(err.message || 'Error fetching fixtures');
    } finally {
      setLoading(false);
    }
  };

  // Group fixtures by Team
  const teamRuns: { [shortName: string]: TeamRun } = {};

  fixtures.forEach((fix) => {
    if (!fix.gameweekId) return;

    // Home Team
    if (!teamRuns[fix.homeTeamShortName]) {
      teamRuns[fix.homeTeamShortName] = {
        teamName: fix.homeTeamName,
        shortName: fix.homeTeamShortName,
        fixtures: {},
      };
    }
    if (!teamRuns[fix.homeTeamShortName].fixtures[fix.gameweekId]) {
      teamRuns[fix.homeTeamShortName].fixtures[fix.gameweekId] = [];
    }
    teamRuns[fix.homeTeamShortName].fixtures[fix.gameweekId].push({
      opponentShort: fix.awayTeamShortName,
      isHome: true,
      difficulty: fix.homeDifficulty || 3,
    });

    // Away Team
    if (!teamRuns[fix.awayTeamShortName]) {
      teamRuns[fix.awayTeamShortName] = {
        teamName: fix.awayTeamName,
        shortName: fix.awayTeamShortName,
        fixtures: {},
      };
    }
    if (!teamRuns[fix.awayTeamShortName].fixtures[fix.gameweekId]) {
      teamRuns[fix.awayTeamShortName].fixtures[fix.gameweekId] = [];
    }
    teamRuns[fix.awayTeamShortName].fixtures[fix.gameweekId].push({
      opponentShort: fix.homeTeamShortName,
      isHome: false,
      difficulty: fix.awayDifficulty || 3,
    });
  });

  const sortedTeams = Object.values(teamRuns).sort((a, b) => a.shortName.localeCompare(b.shortName));

  return (
    <div style={{ maxWidth: 1000, margin: '0 auto' }}>
      {/* Header & Controls */}
      <div style={{
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
        marginBottom: '1.2rem',
        flexWrap: 'wrap',
        gap: '1rem'
      }}>
        <div>
          <h2 style={{ fontSize: '1.2rem', fontWeight: 700, display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <Calendar size={20} color="var(--accent-primary)" />
            Fixture Difficulty Rating (FDR) Matrix
          </h2>
          <p style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>
            Upcoming fixture runs for all Premier League teams (GW {startGw} - GW {endGw})
          </p>
        </div>

        {/* GW Range Selector */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <Filter size={16} color="var(--text-muted)" />
          <span style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>Horizon:</span>
          {[3, 5, 8].map((num) => (
            <button
              key={num}
              onClick={() => setGwRange(num)}
              className={`btn ${gwRange === num ? 'btn-primary' : 'btn-secondary'}`}
              style={{ padding: '0.3rem 0.7rem', fontSize: '0.8rem' }}
            >
              {num} GWs
            </button>
          ))}
        </div>
      </div>

      {loading && <div style={{ textAlign: 'center', padding: '2rem', color: 'var(--text-secondary)' }}>Loading fixture matrix...</div>}
      {error && <div style={{ color: '#ff1744', padding: '1rem', background: 'rgba(255,23,68,0.1)', borderRadius: 8 }}>{error}</div>}

      {/* Fixture Matrix Table */}
      {!loading && !error && (
        <div className="glass-panel" style={{ overflowX: 'auto', padding: '1rem' }}>
          <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
            <thead>
              <tr style={{ borderBottom: '1px solid var(--border-color)' }}>
                <th style={{ padding: '0.75rem 1rem', fontSize: '0.85rem', color: 'var(--text-secondary)' }}>CLUB</th>
                {gws.map((gw) => (
                  <th key={gw} style={{ padding: '0.75rem 0.5rem', textAlign: 'center', fontSize: '0.85rem', color: gw === currentGw ? 'var(--accent-primary)' : 'var(--text-secondary)' }}>
                    GW{gw}
                  </th>
                ))}
              </tr>
            </thead>
            <tbody>
              {sortedTeams.map((team) => (
                <tr key={team.shortName} style={{ borderBottom: '1px solid rgba(255,255,255,0.04)' }}>
                  <td style={{ padding: '0.6rem 1rem', fontWeight: 700, fontSize: '0.9rem' }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                      <span style={{ fontSize: '1.1rem' }}>🛡️</span>
                      {team.shortName}
                    </div>
                  </td>

                  {gws.map((gw) => {
                    const matchArr = team.fixtures[gw] || [];

                    if (matchArr.length === 0) {
                      // Blank Gameweek (BGW)
                      return (
                        <td key={gw} style={{ padding: '0.4rem', textAlign: 'center' }}>
                          <span style={{
                            padding: '0.2rem 0.5rem',
                            borderRadius: 4,
                            fontSize: '0.7rem',
                            background: 'rgba(255,255,255,0.05)',
                            color: 'var(--text-muted)'
                          }}>
                            BLANK
                          </span>
                        </td>
                      );
                    }

                    return (
                      <td key={gw} style={{ padding: '0.4rem', textAlign: 'center' }}>
                        <div style={{ display: 'flex', flexDirection: 'column', gap: 3, alignItems: 'center' }}>
                          {matchArr.map((m, idx) => (
                            <div
                              key={idx}
                              className={`fdr-badge fdr-${m.difficulty}`}
                              title={`${m.opponentShort} (${m.isHome ? 'Home' : 'Away'}) - FDR ${m.difficulty}`}
                            >
                              {m.opponentShort}({m.isHome ? 'H' : 'A'})
                            </div>
                          ))}
                        </div>
                      </td>
                    );
                  })}
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
};

export default FixtureMatrix;
