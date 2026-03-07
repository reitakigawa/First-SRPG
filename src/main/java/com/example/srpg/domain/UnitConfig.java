package com.example.srpg.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UnitConfig(
        String id,
        Team team,
        String name,
        int hp,
        int attack,
        int defense,
        int move,
        int range,
        @JsonProperty("position") int[] position
) {
    public Position toPosition() {
        return new Position(position[0], position[1]);
    }
}
