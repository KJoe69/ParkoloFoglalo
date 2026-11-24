package parkolo;

import java.sql.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private static final String URL = "jdbc:mysql://localhost:3306/parkolo?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Europe/Budapest&characterEncoding=utf8";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static void initDatabase() throws SQLException {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            // 1. Felhasznalok tabla
            String createUsers = "CREATE TABLE IF NOT EXISTS felhasznalok (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "nev VARCHAR(100) NOT NULL, " +
                    "email VARCHAR(100) NOT NULL, " +
                    "rendszam VARCHAR(20) NOT NULL UNIQUE" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_hungarian_ci;";
            stmt.executeUpdate(createUsers);

            // 2. Parkolohelyek tabla
            String createSpots = "CREATE TABLE IF NOT EXISTS parkolohelyek (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "sor VARCHAR(10) NOT NULL, " +
                    "hely INT NOT NULL, " +
                    "oradij INT NOT NULL, " +
                    "UNIQUE KEY unique_hely (sor, hely)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_hungarian_ci;";
            stmt.executeUpdate(createSpots);

            // 3. Foglalasok tabla (csak foglaltsag vagy szabad statusz + kezdeti idopont a dijszamitashoz)
            String createBookings = "CREATE TABLE IF NOT EXISTS foglalasok (" +
                    "parkolohely_id INT PRIMARY KEY, " +
                    "statusz ENUM('SZABAD', 'FOGLALT') NOT NULL DEFAULT 'SZABAD', " +
                    "felhasznalo_id INT NULL, " +
                    "foglalas_kezdete DATETIME NULL, " +
                    "FOREIGN KEY (parkolohely_id) REFERENCES parkolohelyek(id) ON DELETE CASCADE, " +
                    "FOREIGN KEY (felhasznalo_id) REFERENCES felhasznalok(id) ON DELETE SET NULL" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_hungarian_ci;";
            stmt.executeUpdate(createBookings);

            // Ha ures az adatbazis, toltsunk fel nehany alapertelmezett tesztadatot
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM parkolohelyek");
            if (rs.next() && rs.getInt(1) == 0) {
                seedInitialData();
            }
        }
    }

    private static void seedInitialData() throws SQLException {
        addUser("Kovács János", "kovacs.janos@email.hu", "ABC-123");
        addUser("Nagy Éva", "nagy.eva@email.hu", "XYZ-987");
        addUser("Szabó Péter", "szabo.peter@email.hu", "AA-BB-111");

        String[] sorok = {"A", "B"};
        for (String sor : sorok) {
            int dij = sor.equals("A") ? 600 : 450;
            for (int h = 1; h <= 4; h++) {
                addParkingSpot(sor, h, dij);
            }
        }
    }

    // --- FELHASZNALOK KEZELESE ---
    public static List<User> getAllUsers() throws SQLException {
        List<User> list = new ArrayList<>();
        String sql = "SELECT id, nev, email, rendszam FROM felhasznalok ORDER BY nev";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new User(
                        rs.getInt("id"),
                        rs.getString("nev"),
                        rs.getString("email"),
                        rs.getString("rendszam")
                ));
            }
        }
        return list;
    }

    public static void addUser(String nev, String email, String rendszam) throws SQLException {
        String sql = "INSERT INTO felhasznalok (nev, email, rendszam) VALUES (?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nev.trim());
            ps.setString(2, email.trim());
            ps.setString(3, rendszam.trim().toUpperCase());
            ps.executeUpdate();
        }
    }

    public static void deleteUser(int userId) throws SQLException {
        // Eloszor felszabaditjuk a felhasznalo altal esetleg lefoglalt helyeket
        String freeSql = "UPDATE foglalasok SET statusz = 'SZABAD', felhasznalo_id = NULL, foglalas_kezdete = NULL WHERE felhasznalo_id = ?";
        String delSql = "DELETE FROM felhasznalok WHERE id = ?";
        try (Connection conn = getConnection()) {
            try (PreparedStatement ps1 = conn.prepareStatement(freeSql)) {
                ps1.setInt(1, userId);
                ps1.executeUpdate();
            }
            try (PreparedStatement ps2 = conn.prepareStatement(delSql)) {
                ps2.setInt(1, userId);
                ps2.executeUpdate();
            }
        }
    }

    // --- PARKOLOHELYEK ES FOGLALASOK KEZELESE ---
    public static List<ParkingSpot> getAllParkingSpots() throws SQLException {
        List<ParkingSpot> spots = new ArrayList<>();
        String sql = "SELECT p.id, p.sor, p.hely, p.oradij, " +
                     "f.statusz, f.felhasznalo_id, f.foglalas_kezdete, " +
                     "u.nev AS felhasznalo_nev, u.rendszam " +
                     "FROM parkolohelyek p " +
                     "LEFT JOIN foglalasok f ON p.id = f.parkolohely_id " +
                     "LEFT JOIN felhasznalok u ON f.felhasznalo_id = u.id " +
                     "ORDER BY p.sor ASC, p.hely ASC";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String statusz = rs.getString("statusz");
                boolean foglalt = "FOGLALT".equalsIgnoreCase(statusz);
                int fId = rs.getInt("felhasznalo_id");
                Integer felhasznaloId = rs.wasNull() ? null : fId;
                Timestamp ts = rs.getTimestamp("foglalas_kezdete");
                LocalDateTime kezdet = (ts != null) ? ts.toLocalDateTime() : null;

                spots.add(new ParkingSpot(
                        rs.getInt("id"),
                        rs.getString("sor"),
                        rs.getInt("hely"),
                        rs.getInt("oradij"),
                        foglalt,
                        felhasznaloId,
                        rs.getString("felhasznalo_nev"),
                        rs.getString("rendszam"),
                        kezdet
                ));
            }
        }
        return spots;
    }

    public static void addParkingSpot(String sor, int hely, int oradij) throws SQLException {
        String insertSpot = "INSERT INTO parkolohelyek (sor, hely, oradij) VALUES (?, ?, ?)";
        String insertBooking = "INSERT INTO foglalasok (parkolohely_id, statusz, felhasznalo_id, foglalas_kezdete) VALUES (?, 'SZABAD', NULL, NULL)";
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement psSpot = conn.prepareStatement(insertSpot, Statement.RETURN_GENERATED_KEYS)) {
                psSpot.setString(1, sor.trim().toUpperCase());
                psSpot.setInt(2, hely);
                psSpot.setInt(3, oradij);
                psSpot.executeUpdate();

                try (ResultSet keys = psSpot.getGeneratedKeys()) {
                    if (keys.next()) {
                        int newId = keys.getInt(1);
                        try (PreparedStatement psBook = conn.prepareStatement(insertBooking)) {
                            psBook.setInt(1, newId);
                            psBook.executeUpdate();
                        }
                    }
                }
                conn.commit();
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public static void deleteParkingSpot(int spotId) throws SQLException {
        String sql = "DELETE FROM parkolohelyek WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, spotId);
            ps.executeUpdate();
        }
    }

    public static void bookSpot(int spotId, int userId) throws SQLException {
        String sql = "UPDATE foglalasok SET statusz = 'FOGLALT', felhasznalo_id = ?, foglalas_kezdete = ? WHERE parkolohely_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            ps.setInt(3, spotId);
            ps.executeUpdate();
        }
    }

    public static class FeeCalculation {
        public final long minutes;
        public final int oradij;
        public final int fizetendoForint;
        public final LocalDateTime kezdet;
        public final LocalDateTime veg;

        public FeeCalculation(long minutes, int oradij, int fizetendoForint, LocalDateTime kezdet, LocalDateTime veg) {
            this.minutes = minutes;
            this.oradij = oradij;
            this.fizetendoForint = fizetendoForint;
            this.kezdet = kezdet;
            this.veg = veg;
        }
    }

    public static FeeCalculation releaseSpot(ParkingSpot spot) throws SQLException {
        LocalDateTime most = LocalDateTime.now();
        LocalDateTime kezdet = spot.getFoglalasKezdete() != null ? spot.getFoglalasKezdete() : most;
        long minutes = Math.max(1, Duration.between(kezdet, most).toMinutes());
        // Percalapu aranyos dijszamitas (minimum 1 perc), vagy felfele kerekites
        int fizetendo = (int) Math.ceil((minutes / 60.0) * spot.getOradij());

        String sql = "UPDATE foglalasok SET statusz = 'SZABAD', felhasznalo_id = NULL, foglalas_kezdete = NULL WHERE parkolohely_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, spot.getId());
            ps.executeUpdate();
        }
        return new FeeCalculation(minutes, spot.getOradij(), fizetendo, kezdet, most);
    }
}
