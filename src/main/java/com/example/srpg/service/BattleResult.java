package com.example.srpg.service;

import java.util.List;

public record BattleResult(String result, int turnNumber, List<String> logs) {
}
