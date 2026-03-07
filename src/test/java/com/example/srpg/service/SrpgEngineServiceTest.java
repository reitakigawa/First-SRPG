package com.example.srpg.service;

import com.example.srpg.domain.MapConfig;
import com.example.srpg.domain.ScenarioConfig;
import com.example.srpg.domain.Team;
import com.example.srpg.domain.UnitConfig;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SrpgEngineServiceTest {

    private final SrpgEngineService srpgEngineService = new SrpgEngineService();

    @Test
    void simulateShouldReturnVictoryForSampleLikeScenario() {
        ScenarioConfig scenario = new ScenarioConfig(
                new MapConfig(10, 10),
                20,
                List.of(
                        new UnitConfig("p1", Team.PLAYER, "Knight", 24, 9, 4, 3, 1, new int[]{1, 1}),
                        new UnitConfig("p2", Team.PLAYER, "Archer", 18, 7, 2, 3, 2, new int[]{2, 1})
                ),
                List.of(
                        new UnitConfig("e1", Team.ENEMY, "Bandit A", 16, 6, 2, 3, 1, new int[]{7, 7}),
                        new UnitConfig("e2", Team.ENEMY, "Bandit B", 16, 6, 2, 3, 1, new int[]{8, 7})
                )
        );

        BattleResult result = srpgEngineService.simulate(scenario);

        assertEquals("victory", result.result());
        assertTrue(result.logs().stream().anyMatch(log -> log.contains("attacked")));
    }

    @Test
    void simulateShouldRespectMinimumDamageRule() {
        ScenarioConfig scenario = new ScenarioConfig(
                new MapConfig(3, 3),
                5,
                List.of(new UnitConfig("p1", Team.PLAYER, "Weak", 5, 1, 10, 1, 1, new int[]{0, 0})),
                List.of(new UnitConfig("e1", Team.ENEMY, "Tank", 2, 1, 99, 1, 1, new int[]{0, 1}))
        );

        BattleResult result = srpgEngineService.simulate(scenario);

        assertEquals("victory", result.result());
        assertTrue(result.logs().stream().anyMatch(log -> log.contains("for 1 damage")));
    }
}
