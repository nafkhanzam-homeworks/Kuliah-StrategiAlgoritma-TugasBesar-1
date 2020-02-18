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

public class TemplateBot {
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
    public TemplateBot(GameState gameState) {
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
        return NOTHING_COMMAND;
    }

}