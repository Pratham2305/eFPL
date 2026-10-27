package com.fplassistant.service.chat;

import com.fplassistant.model.Player;
import com.fplassistant.repository.PlayerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlayerResolverTest {

    @Mock
    private PlayerRepository playerRepository;

    private PlayerResolver resolver;
    private Player salah;
    private Player haaland;
    private Player foden;

    @BeforeEach
    void setUp() {
        resolver = new PlayerResolver(playerRepository);

        salah = new Player();
        salah.setId(101);
        salah.setWebName("Salah");
        salah.setFirstName("Mohamed");
        salah.setSecondName("Salah");

        haaland = new Player();
        haaland.setId(102);
        haaland.setWebName("Haaland");
        haaland.setFirstName("Erling");
        haaland.setSecondName("Haaland");

        foden = new Player();
        foden.setId(103);
        foden.setWebName("Foden");
        foden.setFirstName("Phil");
        foden.setSecondName("Foden");

        when(playerRepository.findAll()).thenReturn(List.of(salah, haaland, foden));
    }

    @Test
    void testResolveSinglePlayerWithAlias() {
        List<Player> resolved = resolver.resolvePlayers("should i captain mo salah?", null);
        assertThat(resolved).hasSize(1);
        assertThat(resolved.get(0).getWebName()).isEqualTo("Salah");
    }

    @Test
    void testResolveMultiplePlayersInCompare() {
        List<Player> resolved = resolver.resolvePlayers("compare salah and haaland", null);
        assertThat(resolved).hasSize(2);
        assertThat(resolved.stream().map(Player::getWebName)).containsExactly("Salah", "Haaland");
    }

    @Test
    void testResolvePronounFromRecentEntities() {
        // "why not him instead?" should resolve to Erling Haaland from recent context
        List<Player> resolved = resolver.resolvePlayers("why not him instead?", List.of("Erling Haaland is a great pick"));
        assertThat(resolved).hasSize(1);
        assertThat(resolved.get(0).getWebName()).isEqualTo("Haaland");
    }
}
