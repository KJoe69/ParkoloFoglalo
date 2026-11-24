package parkolo;

import javax.swing.*;
import java.sql.SQLException;

public class App {
    public static void main(String[] args) {
        // 1. Modern Swing megjelenes beallitasa
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        // 2. Adatbazis tablak automatikus letrehozasa es ellenorzese
        try {
            DatabaseManager.initDatabase();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null,
                    "Nem sikerült csatlakozni a MySQL adatbázishoz vagy létrehozni a táblákat!\n\n" +
                    "Ellenőrizd a következőket:\n" +
                    "1. Fut-e a MySQL 8.4.7 szerver a localhost:3306 porton (root / jelszó nélkül)?\n" +
                    "2. Létrehoztad-e a 'parkolo' adatbázist?\n" +
                    "3. Bemásoltad-e a mysql-connector-j.jar fájlt a 'lib' mappába?\n\n" +
                    "Részletes hibaüzenet:\n" + ex.getMessage(),
                    "Adatbázis Kapcsolódási Hiba", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        // 3. Egyszeru szerepkor-valaszto a program inditasakor
        SwingUtilities.invokeLater(() -> {
            String[] options = {"Felhasználói felület", "Adminisztrátori felület", "Kilépés"};
            int choice = JOptionPane.showOptionDialog(
                    null,
                    "Üdvözlünk a Parkoló Foglaló Alkalmazásban!\n" +
                    "Kérlek, válaszd ki, milyen szerepkörben szeretnéd indítani a programot:\n" +
                    "(A szerepkört később a főablakban is bármikor átválthatod.)",
                    "Szerepkör választása",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[0]
            );

            if (choice == 0 || choice == 1) {
                boolean startAsAdmin = (choice == 1);
                MainFrame frame = new MainFrame(startAsAdmin);
                frame.setVisible(true);
            } else {
                System.exit(0);
            }
        });
    }
}