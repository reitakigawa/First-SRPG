package com.example.srpg.domain;

import java.util.List;

public record ScenarioConfig(
        MapConfig map,
        int maxTurns,
        List<UnitConfig> playerUnits,
        List<UnitConfig> enemyUnits
) {
}
