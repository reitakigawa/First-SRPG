package com.example.srpg.service;

import com.example.srpg.domain.BattleUnit;
import com.example.srpg.domain.MapConfig;
import com.example.srpg.domain.Position;
import com.example.srpg.domain.ScenarioConfig;
import com.example.srpg.domain.Team;
import com.example.srpg.domain.UnitConfig;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
public class SrpgEngineService {

    public BattleResult simulate(ScenarioConfig scenario) {
        List<BattleUnit> units = createUnits(scenario.playerUnits(), scenario.enemyUnits());
        List<String> logs = new ArrayList<>();

        Team phase = Team.PLAYER;
        int turnNumber = 1;
        String result = "ongoing";

        while ("ongoing".equals(result) && turnNumber <= scenario.maxTurns()) {
            logs.add("--- Turn " + turnNumber + " / " + phase + " phase ---");
            result = runPhase(units, scenario.map(), phase, logs);

            if (!"ongoing".equals(result)) {
                break;
            }

            resetWait(units, phase);
            if (phase == Team.PLAYER) {
                phase = Team.ENEMY;
            } else {
                phase = Team.PLAYER;
                turnNumber++;
            }
        }

        if ("ongoing".equals(result)) {
            result = "turn_limit";
            logs.add("Battle stopped at turn limit (" + scenario.maxTurns() + ")");
        } else {
            logs.add("Battle result: " + result);
        }

        return new BattleResult(result, turnNumber, logs);
    }

    private List<BattleUnit> createUnits(List<UnitConfig> playerUnits, List<UnitConfig> enemyUnits) {
        List<BattleUnit> units = new ArrayList<>();
        playerUnits.forEach(unit -> units.add(new BattleUnit(unit)));
        enemyUnits.forEach(unit -> units.add(new BattleUnit(unit)));
        return units;
    }

    private String runPhase(List<BattleUnit> units, MapConfig map, Team phase, List<String> logs) {
        for (BattleUnit unit : unitsByTeam(units, phase)) {
            if (unit.isWaited()) {
                continue;
            }

            takeAction(unit, units, map, logs);
            unit.setWaited(true);

            String result = checkResult(units);
            if (!"ongoing".equals(result)) {
                return result;
            }
        }

        return "ongoing";
    }

    private void takeAction(BattleUnit actor, List<BattleUnit> units, MapConfig map, List<String> logs) {
        BattleUnit target = nearestEnemy(actor, units);
        if (target == null) {
            logs.add(actor.getName() + " waits");
            return;
        }

        if (distance(actor.getPosition(), target.getPosition()) > actor.getRange()) {
            Position nextPosition = stepToward(actor.getPosition(), target.getPosition(), actor.getMove(), map, units, actor.getId());
            moveUnit(actor, nextPosition, map, units, logs);
        }

        if (distance(actor.getPosition(), target.getPosition()) <= actor.getRange()) {
            attack(actor, target, logs);
        } else {
            logs.add(actor.getName() + " waits");
        }
    }

    private boolean moveUnit(BattleUnit unit, Position to, MapConfig map, List<BattleUnit> units, List<String> logs) {
        if (!unit.isAlive()) return false;
        if (!inBounds(map, to)) return false;
        if (isOccupied(units, to, unit.getId())) return false;

        int dist = distance(unit.getPosition(), to);
        if (dist == 0 || dist > unit.getMove()) return false;

        Position from = unit.getPosition();
        unit.setPosition(to);
        logs.add(unit.getName() + " moved (" + from.x() + "," + from.y() + ") -> (" + to.x() + "," + to.y() + ")");
        return true;
    }

    private boolean attack(BattleUnit attacker, BattleUnit defender, List<String> logs) {
        if (!attacker.isAlive() || !defender.isAlive()) return false;
        if (distance(attacker.getPosition(), defender.getPosition()) > attacker.getRange()) return false;

        int damage = Math.max(1, attacker.getAttack() - defender.getDefense());
        int newHp = Math.max(0, defender.getHp() - damage);
        defender.setHp(newHp);

        logs.add(attacker.getName() + " attacked " + defender.getName() + " for " + damage + " damage (HP " + newHp + "/" + defender.getMaxHp() + ")");

        if (!defender.isAlive()) {
            logs.add(defender.getName() + " was defeated");
        }

        return true;
    }

    private BattleUnit nearestEnemy(BattleUnit actor, List<BattleUnit> units) {
        Team enemyTeam = actor.getTeam() == Team.PLAYER ? Team.ENEMY : Team.PLAYER;
        return unitsByTeam(units, enemyTeam).stream()
                .min(Comparator.comparingInt(unit -> distance(actor.getPosition(), unit.getPosition())))
                .orElse(null);
    }

    private Position stepToward(Position from, Position to, int maxStep, MapConfig map, List<BattleUnit> units, String actorId) {
        Position cursor = from;

        for (int i = 0; i < maxStep; i++) {
            int dx = to.x() - cursor.x();
            int dy = to.y() - cursor.y();
            Position next = cursor;

            if (Math.abs(dx) >= Math.abs(dy) && dx != 0) {
                next = new Position(cursor.x() + (dx > 0 ? 1 : -1), cursor.y());
            } else if (dy != 0) {
                next = new Position(cursor.x(), cursor.y() + (dy > 0 ? 1 : -1));
            }

            if (!inBounds(map, next) || isOccupied(units, next, actorId)) {
                break;
            }

            cursor = next;
            if (Objects.equals(cursor, to)) {
                break;
            }
        }

        return cursor;
    }

    private String checkResult(List<BattleUnit> units) {
        boolean hasPlayers = units.stream().anyMatch(u -> u.isAlive() && u.getTeam() == Team.PLAYER);
        boolean hasEnemies = units.stream().anyMatch(u -> u.isAlive() && u.getTeam() == Team.ENEMY);

        if (!hasEnemies) return "victory";
        if (!hasPlayers) return "defeat";
        return "ongoing";
    }

    private void resetWait(List<BattleUnit> units, Team team) {
        unitsByTeam(units, team).forEach(unit -> unit.setWaited(false));
    }

    private List<BattleUnit> unitsByTeam(List<BattleUnit> units, Team team) {
        return units.stream().filter(u -> u.isAlive() && u.getTeam() == team).toList();
    }

    private boolean isOccupied(List<BattleUnit> units, Position position, String exceptUnitId) {
        return units.stream()
                .anyMatch(u -> u.isAlive()
                        && !u.getId().equals(exceptUnitId)
                        && u.getPosition().x() == position.x()
                        && u.getPosition().y() == position.y());
    }

    private boolean inBounds(MapConfig map, Position pos) {
        return pos.x() >= 0 && pos.y() >= 0 && pos.x() < map.width() && pos.y() < map.height();
    }

    private int distance(Position a, Position b) {
        return Math.abs(a.x() - b.x()) + Math.abs(a.y() - b.y());
    }
}
