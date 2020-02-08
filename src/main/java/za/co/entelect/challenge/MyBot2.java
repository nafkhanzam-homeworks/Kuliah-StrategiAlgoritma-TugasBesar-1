package za.co.entelect.challenge;

import java.util.List;
import java.util.stream.Collectors;

import za.co.entelect.challenge.entities.Building;
import za.co.entelect.challenge.entities.Cell;
import za.co.entelect.challenge.entities.GameDetails;
import za.co.entelect.challenge.entities.GameState;
import za.co.entelect.challenge.entities.Missile;
import za.co.entelect.challenge.entities.Player;
import za.co.entelect.challenge.enums.BuildingType;
import za.co.entelect.challenge.enums.PlayerType;

public class MyBot2 {
    private static final String NOTHING_COMMAND = "";
    private GameState gameState;
    private GameDetails gameDetails;
    private int gameWidth;
    private int gameHeight;
    private Player myself;
    private Player opponent;
    private List<Building> buildings;
    private List<Missile> missiles;
    private Cell[][] cells;

    /**
     * Constructor
     *
     * @param gameState the game state
     **/
    public MyBot2(GameState gameState) {
        this.gameState = gameState;

        gameDetails = gameState.getGameDetails();
        gameWidth = gameDetails.mapWidth;
        gameHeight = gameDetails.mapHeight;
        myself = gameState.getPlayers().stream().filter(p -> p.playerType == PlayerType.A).findFirst().get();
        opponent = gameState.getPlayers().stream().filter(p -> p.playerType == PlayerType.B).findFirst().get();

        buildings = gameState.getGameMap().stream()
                .flatMap(c -> c.getBuildings().stream())
                .collect(Collectors.toList());

        missiles = gameState.getGameMap().stream()
                .flatMap(c -> c.getMissiles().stream())
                .collect(Collectors.toList());

        cells = new Cell[gameWidth][gameHeight];
        gameState.getGameMap().stream().flatMap(c -> c.getBuildings().stream()).forEach(b -> cells[b.getX()][b.getY()] = b);
        gameState.getGameMap().stream().flatMap(c -> c.getMissiles().stream()).forEach(b -> cells[b.getX()][b.getY()] = b);
    }

    public String run() {
        Integer row = checkEmptyEB();
        if (row != null) {
            return BuildingType.ENERGY.buildCommand(0, row);
        }

        // row = checkEmptyDB();
        // if (row != null) {
        //     return BuildingType.DEFENSE.buildCommand(7, row);
        // }

        row = checkUndefended();
        if (row != null) {
            if (cells[7][row] == null)
                BuildingType.DEFENSE.buildCommand(7, row);
            else
                BuildingType.DEFENSE.buildCommand(6, row);
        }

        Point point = rowNotFull();
        if (point != null) {
            int i = point.x, j = point.y;
            BuildingType type;
            if (i <= 0) {
                type = BuildingType.ENERGY;
            } else if (i <= 6) {
                type = BuildingType.ATTACK;
            } else {
                type = BuildingType.DEFENSE;
            }
            return type.buildCommand(i, j);
        }

        return NOTHING_COMMAND;
    }

    public Point rowNotFull() {
        int j = 7, n = 7;
        while (n-- > 0) {
            Integer i = getEmptyCol(j);
            if (i != null) {
                return new Point(i, j);
            }
            j -= 3;
            if (j < 0) {
                j += 7;
            }
        }
        return null;
    }

    public Integer checkUndefended() {
        for (int i = 0; i <= 7; ++i) {
            for (int j = 0; j <= 7; ++j) {
                if (cells[i+8][j] != null) {
                    Cell c = cells[i+8][j];
                    if (c instanceof Building) {
                        Building b = (Building)c;
                        if (b.buildingType == BuildingType.ATTACK) {
                            return j;
                        }
                    }
                }
            }
        }
        return null;
    }

    public Integer getEmptyCol(int j) {
        // check DB
        for (int i = 7; i >= 7; --i) {
            if (cells[i][j] == null) {
                return i;
            }
        }

        // check AB
        for (int i = 6; i >= 1; --i) {
            if (cells[i][j] == null) {
                return i;
            }
        }
        return null;
    }

    public Integer checkEmptyEB() {
        int x = 0;
        for (int j = 0; j < gameHeight; ++j) {
            Cell c = cells[x][j];
            if (c == null) {
                return j;
            }
        }
        return null;
    }

    public Integer checkEmptyEB2() {
        int x = 0, cnt = 0;
        Integer row = null;
        for (int j = 0; j < gameHeight; ++j) {
            Cell c = cells[x][j];
            if (c == null) {
                row = j;
            } else {
                ++cnt;
            }
        }
        return cnt > 4 ? null : row;
    }

    public Integer checkEmptyDB() {
        int x = 7;
        for (int j = 0; j < gameHeight; ++j) {
            Cell c = cells[x][j];
            if (c == null) {
                return j;
            }
        }
        return null;
    }

    private class Point {
        int x, y;
        public Point(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

}