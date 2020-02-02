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

/**
 * MyBot for Strategi 2 di https://docs.google.com/document/d/1VNVTHrTE3ypElwrV3azMEXb0rAkKn6-F3arCZ_5nR-g/edit
 */
public class MyBot {
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
    public MyBot(GameState gameState) {
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
        final int front = gameWidth/2-1;
        // Check if every row is safe.
        RowStatus rowStatus = hasRowNotDefended();
        if (rowStatus != null) {
            int y = rowStatus.row;
            if (rowStatus.status == RowStatusEnum.NEED_ATTACK) {
                for (int i = front-2; i >= 0; --i) {
                    if (cells[i][y] == null) {
                        return BuildingType.ATTACK.buildCommand(i, y);
                    }
                }
            } else {
                for (int i = front; i >= front-1; --i) {
                    if (cells[i][y] == null) {
                        return BuildingType.DEFENSE.buildCommand(i, y);
                    }
                }
            }
        }

        // Check if EB is created all row.
        Integer row = checkEmptyEB();
        if (row != null) {
            return BuildingType.ENERGY.buildCommand(0, row);
        }

        // Check if a DB is dying.
        row = checkDBDying();
        if (row != null) {
            for (int i = front; i >= front-1; --i) {
                if (cells[i][row] == null) {
                    return BuildingType.DEFENSE.buildCommand(i, row);
                }
            }
        }

        return NOTHING_COMMAND;
    }

    public Integer checkDBDying() {
        int front = gameWidth/2-1;
        for (int j = 0; j < gameHeight; ++j) {
            Cell cf = cells[front][j];
            if (cf instanceof Building) {
                Building b = (Building)cf;
                if (b.health <= 15 || b.buildingType != BuildingType.DEFENSE) {
                    return j;
                }
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

    public RowStatus hasRowNotDefended() {
        for (int j = 0; j < gameHeight; ++j) {
            int AB = 0, DB = 0;
            int eAB = 0;
            for (int i = 0; i < gameWidth; ++i) {
                Cell c = cells[i][j];
                if (c instanceof Building) {
                    Building b = (Building)c;
                    if (b.buildingType == BuildingType.ATTACK || b.buildingType == BuildingType.TESLA) {
                        if (b.isPlayers(PlayerType.A)) {
                            ++AB;
                        } else {
                            ++eAB;
                        }
                    } else if (b.buildingType == BuildingType.DEFENSE) {
                        ++DB;
                    }
                }
            }
            boolean needAB = AB < eAB, needDB = DB == 0;
            if (eAB > 0 && (needAB || needDB)) {
                return new RowStatus(j, needDB ? RowStatusEnum.NEED_DEFEND : RowStatusEnum.NEED_ATTACK);
            }
        }
        return null;
    }

    private enum RowStatusEnum {
        NEED_DEFEND,
        NEED_ATTACK,
    }

    private class RowStatus {
        public int row;
        public RowStatusEnum status;
        public RowStatus(int row, RowStatusEnum status) {
            this.row = row;
            this.status = status;
        }
    }
}