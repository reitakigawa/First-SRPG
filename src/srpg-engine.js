/**
 * Minimal SRPG core loop prototype based on requirements.
 * Run: node src/srpg-engine.js
 */

const fs = require('fs');
const path = require('path');

function loadScenario(filePath) {
  const json = fs.readFileSync(filePath, 'utf-8');
  const scenario = JSON.parse(json);

  if (!scenario.map || !Number.isInteger(scenario.map.width) || !Number.isInteger(scenario.map.height)) {
    throw new Error('Invalid scenario: map.width/map.height are required integers.');
  }

  return scenario;
}

function manhattan(a, b) {
  return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
}

function inBounds(map, pos) {
  return pos.x >= 0 && pos.y >= 0 && pos.x < map.width && pos.y < map.height;
}

class Unit {
  constructor(data) {
    this.id = data.id;
    this.team = data.team; // "player" | "enemy"
    this.name = data.name;
    this.hp = data.hp;
    this.maxHp = data.hp;
    this.attack = data.attack;
    this.defense = data.defense;
    this.move = data.move;
    this.range = data.range;
    this.pos = { x: data.position[0], y: data.position[1] };
    this.waited = false;
  }

  get alive() {
    return this.hp > 0;
  }
}

class SRPGEngine {
  constructor(scenario) {
    this.map = scenario.map;
    this.turnNumber = 1;
    this.phase = 'player';
    this.units = [...scenario.playerUnits, ...scenario.enemyUnits].map((u) => new Unit(u));
    this.log = [];
  }

  unitsByTeam(team) {
    return this.units.filter((u) => u.alive && u.team === team);
  }

  occupied(pos, exceptId = null) {
    return this.units.some((u) => u.alive && u.id !== exceptId && u.pos.x === pos.x && u.pos.y === pos.y);
  }

  moveUnit(unit, to) {
    if (!unit.alive) return false;
    if (!inBounds(this.map, to)) return false;
    if (this.occupied(to, unit.id)) return false;
    const dist = manhattan(unit.pos, to);
    if (dist === 0 || dist > unit.move) return false;

    this.log.push(`${unit.name} moved (${unit.pos.x},${unit.pos.y}) -> (${to.x},${to.y})`);
    unit.pos = { ...to };
    return true;
  }

  attack(attacker, defender) {
    if (!attacker.alive || !defender.alive) return false;
    const dist = manhattan(attacker.pos, defender.pos);
    if (dist > attacker.range) return false;

    const damage = Math.max(1, attacker.attack - defender.defense);
    defender.hp = Math.max(0, defender.hp - damage);

    this.log.push(
      `${attacker.name} attacked ${defender.name} for ${damage} damage (HP ${defender.hp}/${defender.maxHp})`
    );

    if (!defender.alive) {
      this.log.push(`${defender.name} was defeated`);
    }

    return true;
  }

  nearestEnemy(unit) {
    const enemyTeam = unit.team === 'player' ? 'enemy' : 'player';
    const enemies = this.unitsByTeam(enemyTeam);
    if (enemies.length === 0) return null;

    return enemies.reduce((nearest, current) => {
      if (!nearest) return current;
      return manhattan(unit.pos, current.pos) < manhattan(unit.pos, nearest.pos) ? current : nearest;
    }, null);
  }

  stepToward(from, to, maxStep) {
    // Greedy shortest move (no pathfinding for this prototype).
    let cursor = { ...from };
    for (let i = 0; i < maxStep; i += 1) {
      const dx = to.x - cursor.x;
      const dy = to.y - cursor.y;
      const next = { ...cursor };

      if (Math.abs(dx) >= Math.abs(dy) && dx !== 0) {
        next.x += dx > 0 ? 1 : -1;
      } else if (dy !== 0) {
        next.y += dy > 0 ? 1 : -1;
      }

      if (!inBounds(this.map, next) || this.occupied(next)) {
        break;
      }

      cursor = next;
      if (cursor.x === to.x && cursor.y === to.y) break;
    }

    return cursor;
  }

  enemyAction(enemy) {
    const target = this.nearestEnemy(enemy);
    if (!target) return;

    if (manhattan(enemy.pos, target.pos) > enemy.range) {
      const nextPos = this.stepToward(enemy.pos, target.pos, enemy.move);
      this.moveUnit(enemy, nextPos);
    }

    if (manhattan(enemy.pos, target.pos) <= enemy.range) {
      this.attack(enemy, target);
    } else {
      this.log.push(`${enemy.name} waits`);
    }

    enemy.waited = true;
  }

  playerAction(player) {
    const target = this.nearestEnemy(player);
    if (!target) return;

    // Simple auto behavior for prototype:
    // move into range if needed, then attack once.
    if (manhattan(player.pos, target.pos) > player.range) {
      const nextPos = this.stepToward(player.pos, target.pos, player.move);
      this.moveUnit(player, nextPos);
    }

    if (manhattan(player.pos, target.pos) <= player.range) {
      this.attack(player, target);
    } else {
      this.log.push(`${player.name} waits`);
    }

    player.waited = true;
  }

  checkResult() {
    const players = this.unitsByTeam('player');
    const enemies = this.unitsByTeam('enemy');

    if (enemies.length === 0) return 'victory';
    if (players.length === 0) return 'defeat';
    return 'ongoing';
  }

  runTurn() {
    this.log.push(`--- Turn ${this.turnNumber} / ${this.phase} phase ---`);

    const actingTeam = this.phase;
    for (const unit of this.unitsByTeam(actingTeam)) {
      if (unit.waited) continue;

      if (actingTeam === 'player') {
        this.playerAction(unit);
      } else {
        this.enemyAction(unit);
      }

      const result = this.checkResult();
      if (result !== 'ongoing') return result;
    }

    // End phase.
    for (const unit of this.unitsByTeam(actingTeam)) {
      unit.waited = false;
    }

    if (this.phase === 'player') {
      this.phase = 'enemy';
    } else {
      this.phase = 'player';
      this.turnNumber += 1;
    }

    return 'ongoing';
  }

  run(maxTurns = 20) {
    let result = 'ongoing';
    while (result === 'ongoing' && this.turnNumber <= maxTurns) {
      result = this.runTurn();
    }

    if (result === 'ongoing') {
      this.log.push(`Battle stopped at turn limit (${maxTurns})`);
      return 'turn_limit';
    }

    this.log.push(`Battle result: ${result}`);
    return result;
  }
}

function main() {
  const scenarioPath = path.join(__dirname, '..', 'config', 'stage1.json');
  const scenario = loadScenario(scenarioPath);
  const engine = new SRPGEngine(scenario);

  const result = engine.run(scenario.maxTurns || 20);
  console.log(engine.log.join('\n'));
  console.log(`\nFinal result: ${result}`);
}

if (require.main === module) {
  main();
}

module.exports = {
  loadScenario,
  SRPGEngine,
  Unit,
  manhattan,
  inBounds,
};
