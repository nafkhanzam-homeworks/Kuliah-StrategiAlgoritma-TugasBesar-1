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

public class MyBot4 {
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
    public MyBot4(GameState gameState) {
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

        if ((res = createEB(0)) != null) {
            return res;
        }

        if ((res = createAB()) != null) {
            return res;
        }

        return NOTHING_COMMAND;
    }

    public String createDB() {
        if (myself.energy < 30) {
            return null;
        }
        int x = 6;
        for (int j = 0; j <= 7; ++j) {
            if (getBuilding(x, j) == null) {
                return BuildingType.DEFENSE.buildCommand(x, j);
            }
        }
        return null;
    }

    public String createAB() {
        for (int i = 6; i >= 1; --i) {
            for (int j = 0; j <= 7; ++j) {
                if (getBuilding(i, j) == null) {
                    return BuildingType.ATTACK.buildCommand(i, j);
                }
            }
        }
        return null;
    }

    public String createEB(int x) {
        for (int j = 0; j <= 7; ++j) {
            if (getBuilding(x, j) == null) {
                return BuildingType.ENERGY.buildCommand(x, j);
            }
        }
        return null;
    }

    public String ironcurtain() {
        if (myself.energy >= gameDetails.ironCurtainStats.price + 30 && myself.ironCurtainAvailable
                && !myself.isIronCurtainActive) {
            return "0,0,5";
        }
        return null;
    }

    public String tesla() {
        if (myself.energy < gameDetails.buildingsStats.get(BuildingType.TESLA).price) {
            return null;
        }
        int x = 7;
        for (int j = 1; j <= 6; j += 5) {
            if (getBuilding(x, j) == null) {
                return BuildingType.TESLA.buildCommand(x, j);
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