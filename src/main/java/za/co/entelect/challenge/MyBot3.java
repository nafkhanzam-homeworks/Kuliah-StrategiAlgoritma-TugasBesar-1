package za.co.entelect.challenge;

import java.util.Arrays;
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

public class MyBot3 {
    private static final String NOTHING_COMMAND = "";
    private GameState gameState;
    private GameDetails gameDetails;
    private int gameWidth;
    private int gameHeight;
    private Player myself;
    private Player opponent;
    private Cell[][] cells;

    /**
     * Constructor
     *
     * @param gameState the game state
     **/
    public MyBot3(GameState gameState) {
        this.gameState = gameState;

        gameDetails = gameState.getGameDetails();
        gameWidth = gameDetails.mapWidth;
        gameHeight = gameDetails.mapHeight;
        myself = gameState.getPlayers().stream().filter(p -> p.playerType == PlayerType.A).findFirst().get();
        opponent = gameState.getPlayers().stream().filter(p -> p.playerType == PlayerType.B).findFirst().get();

        cells = new Cell[gameWidth][gameHeight];
        gameState.getGameMap().stream().flatMap(c -> c.getBuildings().stream())
                .forEach(b -> cells[b.getX()][b.getY()] = b);
        gameState.getGameMap().stream().flatMap(c -> c.getMissiles().stream())
                .forEach(b -> cells[b.getX()][b.getY()] = b);
    }

    public String run() {
        String res = null;

        if ((res = tesla()) != null) {
            return res;
        }

        if ((res = ironcurtain()) != null) {
            return res;
        }

        if ((res = createABandDB()) != null) {
            return res;
        }

        if ((res = createEBStrat()) != null) {
            return res;
        }

        return NOTHING_COMMAND;
    }

    public String createEBStrat() {
        for (int j = 3; j <= 4; ++j) {
            for (int i = 0; i <= 3; i++) {
                if (cells[i][j] == null) {
                    return BuildingType.ENERGY.buildCommand(i, j);
                }
            }
        }
        for (int j = 3; j <= 4; ++j) {
            for (int i = 6; i <= 7; i++) {
                if (cells[i][j] == null) {
                    return BuildingType.DEFENSE.buildCommand(i, j);
                }
            }
        }
        for (int j = 3; j <= 4; ++j) {
            for (int i = 4; i <= 5; i++) {
                if (cells[i][j] == null) {
                    return BuildingType.ENERGY.buildCommand(i, j);
                }
            }
        }
        return null;
    }

    public String ironcurtain() {
        if (myself.energy >= 130 && myself.ironCurtainAvailable && !myself.isIronCurtainActive) {
            return "0,0,5";
        }
        return null;
    }

    public String tesla() {
        if (myself.energy >= 330) {
            if (getBuilding(7, 5) == null)
                return BuildingType.TESLA.buildCommand(7, 5);
            else if (getBuilding(7, 2) == null)
                return BuildingType.TESLA.buildCommand(7, 2);
        }
        return null;
    }

    public String createABandDB() {
        if (myself.energy < 30) {
            return null;
        }
        Pair[] count = new Pair[8];
        for (int j = 0; j <= 7; ++j) {
            count[j] = new Pair(0, 0);
            if (j == 3 || j == 4) {
                continue;
            }
            for (int i = 0; i <= 7; ++i) {
                if (isType(i + 8, j, BuildingType.ATTACK)) {
                    ++count[j].b;
                }
            }
        }
        Arrays.sort(count, (a, b) -> b.b - a.b);
        for (int a = 0; a <= 7; ++a) {
            Pair p = count[a];
            for (int i = 7; i >= 0; --i) {
                if (cells[i][p.a] == null) {
                    return (i == 7 ? BuildingType.DEFENSE : BuildingType.ATTACK).buildCommand(i, p.a);
                }
            }
        }
        return null;
    }

    public boolean isType(int x, int y, BuildingType type) {
        Building b = getBuilding(x, y);
        return b != null && b.buildingType == type;
    }

    public Building getBuilding(int x, int y) {
        Cell c = cells[x][y];
        if (c instanceof Building) {
            return (Building) c;
        }
        return null;
    }

    public class Pair {
        int a, b;

        public Pair(int a, int b) {
            this.a = a;
            this.b = b;
        }
    }

}