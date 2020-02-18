package za.co.entelect.challenge;

import za.co.entelect.challenge.entities.Building;
import za.co.entelect.challenge.entities.Cell;
import za.co.entelect.challenge.entities.GameDetails;
import za.co.entelect.challenge.entities.GameState;
import za.co.entelect.challenge.entities.Missile;
import za.co.entelect.challenge.entities.Player;
import za.co.entelect.challenge.enums.BuildingType;
import za.co.entelect.challenge.enums.PlayerType;

public class MyBot {
    private static final String NOTHING_COMMAND = "";
    private GameState gameState;
    private GameDetails gameDetails;
    private int gameWidth;
    private int gameHeight;
    private Player myself;
    private Player opponent;
    private Cell[][] cells, missiles;

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

        cells = new Cell[gameWidth][gameHeight];
        missiles = new Cell[gameWidth][gameHeight];
        gameState.getGameMap().stream().flatMap(c -> c.getBuildings().stream())
                .forEach(b -> cells[b.getX()][b.getY()] = b);
        gameState.getGameMap().stream().flatMap(c -> c.getMissiles().stream())
                .forEach(b -> missiles[b.getX()][b.getY()] = b);
    }

    /**
     * Fungsi run menghasilkan command apa yang akan dilakukan selanjutnya
     * bergantung pada state permainan saat round tertentu.
     * 
     * @return Sebuah string command.
     */
    public String run() {
        String res = null;
        int rowE = -1;

        /**
         * Mengecek apakah ada row yang membutuhkan defense
         */
        if ((rowE = isEmergency()) != -1) {
            if ((res = createDB(rowE)) != null) {
                return res;
            }
        }

        /**
         * Mengecek apakah tesla cocok untuk digunakan sekarang
         */
        Pair p = tesla();
        if (p != null) {
            return BuildingType.TESLA.buildCommand(p.a, p.b);
        }

        /**
         * Mengecek apakah iron curtain cocok untuk digunakan sekarang
         */
        if (activateICNow() && (res = buildIC()) != null) {
            return res;
        }

        /**
         * Mengecek apakah energy building sudah terbuat semua di kolom 0
         */
        if ((res = createEB(0)) != null) {
            return res;
        }

        /**
         * Membuat attack building di baris yang memiliki energy building musuh
         * terbanyak.
         */
        if ((res = createAB()) != null) {
            return res;
        }

        return NOTHING_COMMAND;
    }

    /**
     * Fungsi isEmergency menghasilkan apakah perlu dibuatnya attack building di
     * round dan state tertentu.
     * 
     * @return Integer row yang membutuhkan pertahanan.
     */
    public int isEmergency() {
        int maxCnt = 0, row = -1;
        for (int j = 0; j <= 7; ++j) {
            int cnt = 0;
            for (int i = 0; i <= 7; ++i) {
                Building b = getBuilding(i + 8, j);
                if (b != null && b.buildingType == BuildingType.ATTACK) {
                    ++cnt;
                }
            }
            int defCnt = 0;
            for (int i = 6; i <= 7; ++i) {
                Building b = getBuilding(i, j);
                if (b != null && b.buildingType == BuildingType.DEFENSE) {
                    ++defCnt;
                }
            }
            int v = cnt - 2 * defCnt - 1;
            if (v > maxCnt && defCnt < 2) {
                maxCnt = v;
                row = j;
            }
        }
        return row;
    }

    /**
     * Membuat Defense Building di row yang ditentukan.
     * 
     * @param row
     * @return command build defense
     */
    public String createDB(int row) {
        if (myself.energy < 30) {
            return null;
        }
        for (int i = 6; i <= 7; ++i) {
            if (isAvailable(i, row)) {
                return BuildingType.DEFENSE.buildCommand(i, row);
            }
        }
        return null;
    }

    /**
     * Membuat Attack Building di row dan kolom yang sesuai dengan strategi
     * algoritma greedy.
     * 
     * @return Command build attack building.
     */
    public String createAB() {
        for (int i = 5; i >= 1; --i) {
            Integer j = findEBRow(i);
            if (j == null) {
                continue;
            }
            if (isAvailable(i, j)) {
                return BuildingType.ATTACK.buildCommand(i, j);
            }
        }
        for (int i = 6; i <= 7; ++i) {
            Integer j = findEBRow(i);
            if (j == null) {
                continue;
            }
            if (isAvailable(i, j)) {
                return BuildingType.ATTACK.buildCommand(i, j);
            }
        }
        return null;
    }

    /**
     * Mencari row musuh yang mempunyai energy building paling banyak
     * 
     * @param col
     * @return Integer row yang mempunyai energy building musuh paling banyak
     */
    public Integer findEBRow(int col) {
        int maxCnt = 0, row = 0;
        for (int j = 0; j <= 7; ++j) {
            int cnt = 0;
            for (int i = 0; i <= 7; ++i) {
                Building b = getBuilding(i + 8, j);
                if (b != null && b.buildingType == BuildingType.ENERGY) {
                    ++cnt;
                }
            }
            if (cnt > maxCnt && isAvailable(col, j)) {
                maxCnt = cnt;
                row = j;
            }
        }

        if (!isAvailable(col, row)) {
            row = 0;
            while (row <= 7 && !isAvailable(col, row)) {
                ++row;
            }
            if (row > 7) {
                return null;
            }
        }
        return row;
    }

    /**
     * Membuat energy building di kolom tertentu dengan urutan dari row 0 sampai 7
     * 
     * @param x
     * @return
     */
    public String createEB(int x) {
        for (int j = 0; j <= 7; ++j) {
            if (isAvailable(x, j)) {
                return BuildingType.ENERGY.buildCommand(x, j);
            }
        }
        return null;
    }

    /**
     * Menghitung attack building musuh di row tertentu
     * 
     * @param row
     * @return
     */
    public int countEnemyAB(int row) {
        int res = 0;
        for (int i = 0; i <= 7; ++i) {
            Building b = getBuilding(i, row);
            if (b != null && b.buildingType == BuildingType.ATTACK) {
                ++res;
            }
        }
        return res;
    }

    /**
     * Memasang Iron Curtain apabila energy mencukupi dan iron curtain dapat
     * digunakan.
     * 
     * @return
     */
    public String buildIC() {
        if (myself.energy >= gameDetails.ironCurtainStats.price + 30 && myself.ironCurtainAvailable
                && !myself.isIronCurtainActive) {
            return "0,0,5";
        }
        return null;
    }

    /**
     * Mendapatkan point pada cell yang cocok untuk ditempati tesla apabila energy
     * mencukupi.
     * 
     * @return
     */
    public Pair tesla() {
        if (countMyBuilding(BuildingType.TESLA) >= 2) {
            return null;
        }
        int price = gameDetails.buildingsStats.get(BuildingType.TESLA).price;
        if (myself.energy < price) {
            return null;
        }
        // kalo ada dua tesla return false
        // pasti bikin klo health lawan <= 20
        // duit nya cukup ato engga, kalo cukup next, klo engga false
        if (opponent.health <= 20) {
            for (int i = 6; i <= 7; ++i) {
                for (int j = 0; j <= 7; ++j) {
                    Building b = getBuilding(i, j);
                    if (b == null) {
                        return new Pair(i, j);
                    }
                }
            }
        }
        // kalo ada defence yang belakangnya kosong, build , kalo engga ada ada skema
        // buat bikin defence yang belakangnya kosong
        int i = 6;
        for (int j = 0; j <= 7; ++j) {
            Building b = getBuilding(i, j);
            if (b == null) {
                return new Pair(i, j);
            }
        }

        return null;
    }

    /**
     * State yang cocok untuk mengaktifkan iron curtain
     *
     * @return
     */
    public boolean activateICNow() {
        Integer teslaRow = enemyHasTesla();
        if (teslaRow != null && opponent.energy >= 100) { // kalo ada tesla (atau turn sealnjutanya ada tesla) && turn
                                                          // selanjutnya ada energy >=100
            return true;
        }

        for (int i = 0; i <= 7; i++) {
            int tHealth = getDBHealth(i); // itung total health
            int tMissile = countMissile67(i); // itung banyak missile
            if (tMissile * 5 > tHealth) {
                return true;
            }
        }
        return false;
    }

    /**
     * Mendapatkan total Defense Building Health pada row kita
     * 
     * @param row
     * @return
     */
    public int getDBHealth(int row) {
        int res = 0;
        for (int i = 6; i <= 7; ++i) {
            Building b = getBuilding(i, row);
            if (b != null && b.buildingType != BuildingType.DEFENSE) {
                res += b.health;
            }
        }
        return res;
    }

    /**
     * Menghitung total building jenis tertentu yang ada di bagian kita.
     * 
     * @param type
     * @return
     */
    public int countMyBuilding(BuildingType type) {
        int res = 0;
        for (int i = 0; i <= 7; ++i) {
            for (int j = 0; j <= 7; ++j) {
                Building b = getBuilding(i, j);
                if (b != null && b.buildingType == type) {
                    ++res;
                }
            }
        }
        return res;
    }

    /**
     * Mengecek row musuh mana yang terdapat tesla
     * 
     * @return
     */
    public Integer enemyHasTesla() {
        for (int i = 0; i <= 7; ++i) {
            for (int j = 0; j <= 7; ++j) {
                Building b = getBuilding(i + 8, j);
                if (b != null && b.buildingType == BuildingType.TESLA && b.constructionTimeLeft <= 1) {
                    return j;
                }
            }
        }
        return null;
    }

    /**
     * Menghitung jumlah missile yang ada di kolom 6 dan 7 musuh
     * 
     * @param row
     * @return
     */
    public int countMissile67(int row) {
        int res = 0;
        for (int i = 0; i <= 1; ++i) {
            Missile m = getMissile(i + 8, row);
            if (m != null) {
                ++res;
            }
        }
        return res;
    }

    /**
     * Mengecek apakah building di koordinat tertentu merupakan type tertentu
     * 
     * @param x
     * @param y
     * @param type
     * @return
     */
    public boolean isType(int x, int y, BuildingType type) {
        Building b = getBuilding(x, y);
        return b != null && b.buildingType == type;
    }

    /**
     * Mendapatkan Building di koordinat tertentu
     * 
     * @param x
     * @param y
     * @return
     */
    public Building getBuilding(int x, int y) {
        Cell c = cells[x][y];
        if (c instanceof Building) {
            return (Building) c;
        }
        return null;
    }

    /**
     * Mengecek apakah koordinat tertentu dapat ditempati building baru
     * 
     * @param x
     * @param y
     * @return
     */
    public boolean isAvailable(int x, int y) {
        return cells[x][y] == null;
    }

    /**
     * Mendapatkan missile di koordinat tertentu.
     * 
     * @param x
     * @param y
     * @return
     */
    public Missile getMissile(int x, int y) {
        Cell c = missiles[x][y];
        if (c instanceof Missile) {
            return (Missile) c;
        }
        return null;
    }

    /**
     * Class Pair digunakan untuk menentukan koordinat pada cell.
     */
    public class Pair {
        int a, b;

        public Pair(int a, int b) {
            this.a = a;
            this.b = b;
        }
    }

}
