package com.example.srpg.domain;

public class BattleUnit {
    private final String id;
    private final Team team;
    private final String name;
    private final int maxHp;
    private final int attack;
    private final int defense;
    private final int move;
    private final int range;

    private int hp;
    private Position position;
    private boolean waited;

    public BattleUnit(UnitConfig config) {
        this.id = config.id();
        this.team = config.team();
        this.name = config.name();
        this.maxHp = config.hp();
        this.attack = config.attack();
        this.defense = config.defense();
        this.move = config.move();
        this.range = config.range();
        this.hp = config.hp();
        this.position = config.toPosition();
        this.waited = false;
    }

    public boolean isAlive() {
        return hp > 0;
    }

    public String getId() { return id; }
    public Team getTeam() { return team; }
    public String getName() { return name; }
    public int getMaxHp() { return maxHp; }
    public int getAttack() { return attack; }
    public int getDefense() { return defense; }
    public int getMove() { return move; }
    public int getRange() { return range; }
    public int getHp() { return hp; }
    public void setHp(int hp) { this.hp = hp; }
    public Position getPosition() { return position; }
    public void setPosition(Position position) { this.position = position; }
    public boolean isWaited() { return waited; }
    public void setWaited(boolean waited) { this.waited = waited; }
}
