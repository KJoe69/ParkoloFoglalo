package parkolo;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MainFrame extends JFrame {
    private boolean isAdminMode = false;
    private JPanel mapGridPanel;
    private JComboBox<User> userComboBox;
    private JLabel roleStatusLabel;
    private JPanel adminSidePanel;
    private DefaultTableModel userTableModel;
    private JTable userTable;

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public MainFrame(boolean startAsAdmin) {
        this.isAdminMode = startAsAdmin;
        setTitle("Parkoló Foglaló Rendszer - " + (isAdminMode ? "ADMINISZTRÁTOR" : "FELHASZNÁLÓ"));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1150, 740);
        setLocationRelativeTo(null);
        initComponents();
        refreshAllData();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));

        // --- FELSO SAV: Magasabb, 2 soros elrendezes, hogy minden gomb kenyelmesen latszodjon ---
        JPanel topBar = new JPanel(new GridLayout(2, 1, 5, 8));
        topBar.setBorder(new EmptyBorder(12, 15, 12, 15));
        topBar.setBackground(new Color(44, 62, 80));

        // 1. sor: Szerepkor kijelzes balra, Frissites es Szerepkor valto gombok jobbra
        JPanel topRow1 = new JPanel(new BorderLayout(10, 0));
        topRow1.setOpaque(false);

        roleStatusLabel = new JLabel();
        roleStatusLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        roleStatusLabel.setForeground(Color.WHITE);
        topRow1.add(roleStatusLabel, BorderLayout.WEST);

        JPanel rightButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightButtons.setOpaque(false);

        JButton btnRefresh = new JButton("Frissítés");
        btnRefresh.setForeground(Color.BLACK);
        btnRefresh.addActionListener(e -> refreshAllData());
        rightButtons.add(btnRefresh);

        JButton btnSwitchRole = new JButton("Szerepkör váltása (Admin <-> Felhasználó)");
        btnSwitchRole.setBackground(new Color(243, 156, 18));
        btnSwitchRole.setForeground(Color.BLACK);
        btnSwitchRole.setFocusPainted(false);
        btnSwitchRole.addActionListener(e -> toggleRole());
        rightButtons.add(btnSwitchRole);

        topRow1.add(rightButtons, BorderLayout.EAST);

        // 2. sor: Aktiv felhasznalo kivalaszto es Uj felhasznalo gomb
        JPanel topRow2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        topRow2.setOpaque(false);

        JLabel lblActiveUser = new JLabel("Aktív felhasználó (foglaláshoz):");
        lblActiveUser.setForeground(Color.WHITE);
        lblActiveUser.setFont(new Font("SansSerif", Font.BOLD, 13));
        topRow2.add(lblActiveUser);

        userComboBox = new JComboBox<>();
        userComboBox.setPreferredSize(new Dimension(320, 30));
        topRow2.add(userComboBox);

        JButton btnQuickNewUser = new JButton("+ Új felhasználó");
        btnQuickNewUser.setForeground(Color.BLACK);
        btnQuickNewUser.setPreferredSize(new Dimension(140, 30));
        btnQuickNewUser.addActionListener(e -> showAddUserDialog());
        topRow2.add(btnQuickNewUser);

        topBar.add(topRow1);
        topBar.add(topRow2);
        add(topBar, BorderLayout.NORTH);

        // --- KOZEPSO RESZ: Vizualis Parkoloterkep ---
        JPanel centerContainer = new JPanel(new BorderLayout(5, 5));
        centerContainer.setBorder(new EmptyBorder(5, 10, 10, 10));

        JPanel legendPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        legendPanel.add(createLegendBadge("SZABAD (Kattints a foglaláshoz)", new Color(46, 204, 113)));
        legendPanel.add(createLegendBadge("FOGLALT (Kattints a felszabadításhoz / díjszámításhoz)", new Color(231, 76, 60)));
        centerContainer.add(legendPanel, BorderLayout.NORTH);

        mapGridPanel = new JPanel(new GridLayout(0, 4, 12, 12));
        mapGridPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        JScrollPane mapScroll = new JScrollPane(mapGridPanel);
        mapScroll.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(149, 165, 166), 1),
                "Vizuális Parkolótérkép",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("SansSerif", Font.BOLD, 14)
        ));
        centerContainer.add(mapScroll, BorderLayout.CENTER);
        add(centerContainer, BorderLayout.CENTER);

        // --- JOBB OLDALI PANEL: Adminisztracios funkciok (Csak Admin modban lathato) ---
        adminSidePanel = createAdminPanel();
        add(adminSidePanel, BorderLayout.EAST);

        updateRoleUI();
    }

    private JPanel createLegendBadge(String text, Color bg) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
        JLabel box = new JLabel("   ");
        box.setOpaque(true);
        box.setBackground(bg);
        box.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        p.add(box);
        p.add(new JLabel(text));
        return p;
    }

    private JPanel createAdminPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setPreferredSize(new Dimension(380, 0));
        panel.setBorder(new EmptyBorder(5, 5, 10, 10));

        // 1. Parkolohely hozzaadasa / torlese
        JPanel spotAdminPanel = new JPanel(new GridBagLayout());
        spotAdminPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(52, 152, 219), 1),
                "Admin: Parkolóhely hozzáadása",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("SansSerif", Font.BOLD, 13)
        ));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField tfSor = new JTextField("C", 6);
        JSpinner spHely = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
        JSpinner spOradij = new JSpinner(new SpinnerNumberModel(500, 100, 10000, 50));

        gbc.gridx = 0; gbc.gridy = 0; spotAdminPanel.add(new JLabel("Sor (pl. A, B, C):"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; spotAdminPanel.add(tfSor, gbc);

        gbc.gridx = 0; gbc.gridy = 1; spotAdminPanel.add(new JLabel("Hely száma:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; spotAdminPanel.add(spHely, gbc);

        gbc.gridx = 0; gbc.gridy = 2; spotAdminPanel.add(new JLabel("Óradíj (Ft/óra):"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; spotAdminPanel.add(spOradij, gbc);

        JButton btnAddSpot = new JButton("Parkolóhely hozzáadása");
        btnAddSpot.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnAddSpot.setForeground(Color.BLACK); // Fekete felirat a keresnek megfeleloen
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        spotAdminPanel.add(btnAddSpot, gbc);

        btnAddSpot.addActionListener(e -> {
            String sor = tfSor.getText().trim().toUpperCase();
            int hely = (Integer) spHely.getValue();
            int oradij = (Integer) spOradij.getValue();
            if (sor.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Kérlek, add meg a sort!", "Hiányzó adat", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                DatabaseManager.addParkingSpot(sor, hely, oradij);
                refreshAllData();
                JOptionPane.showMessageDialog(this, "A(z) " + sor + "-" + hely + " parkolóhely sikeresen létrehozva!");
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Hiba (valószínűleg már létezik ez a sor/hely):\n" + ex.getMessage(),
                        "Adatbázis hiba", JOptionPane.ERROR_MESSAGE);
            }
        });

        panel.add(spotAdminPanel, BorderLayout.NORTH);

        // 2. Felhasznalok kezelese
        JPanel userAdminPanel = new JPanel(new BorderLayout(5, 5));
        userAdminPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(52, 152, 219), 1),
                "Admin: Felhasználók kezelése",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("SansSerif", Font.BOLD, 13)
        ));

        userTableModel = new DefaultTableModel(new Object[]{"ID", "Név", "Email", "Rendszám"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        userTable = new JTable(userTableModel);
        userTable.getColumnModel().getColumn(0).setPreferredWidth(35);
        userAdminPanel.add(new JScrollPane(userTable), BorderLayout.CENTER);

        JPanel userBtnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnAddUser = new JButton("Új felhasználó");
        btnAddUser.setForeground(Color.BLACK);
        btnAddUser.addActionListener(e -> showAddUserDialog());

        JButton btnDeleteUser = new JButton("Kijelölt törlése");
        btnDeleteUser.setForeground(Color.BLACK);
        btnDeleteUser.addActionListener(e -> deleteSelectedUser());

        userBtnPanel.add(btnAddUser);
        userBtnPanel.add(btnDeleteUser);
        userAdminPanel.add(userBtnPanel, BorderLayout.SOUTH);

        panel.add(userAdminPanel, BorderLayout.CENTER);
        return panel;
    }

    private void toggleRole() {
        isAdminMode = !isAdminMode;
        updateRoleUI();
        refreshAllData();
    }

    private void updateRoleUI() {
        setTitle("Parkoló Foglaló Rendszer - " + (isAdminMode ? "ADMINISZTRÁTOR MÓD" : "FELHASZNÁLÓI MÓD"));
        roleStatusLabel.setText(isAdminMode ? "Szerepkör: ADMINISZTRÁTOR" : "Szerepkör: FELHASZNÁLÓ");
        adminSidePanel.setVisible(isAdminMode);
        revalidate();
        repaint();
    }

    private void refreshAllData() {
        try {
            // 1. Felhasznalok betoltese
            List<User> users = DatabaseManager.getAllUsers();
            User previouslySelected = (User) userComboBox.getSelectedItem();
            userComboBox.removeAllItems();
            userTableModel.setRowCount(0);

            for (User u : users) {
                userComboBox.addItem(u);
                if (previouslySelected != null && previouslySelected.getId() == u.getId()) {
                    userComboBox.setSelectedItem(u);
                }
                userTableModel.addRow(new Object[]{u.getId(), u.getNev(), u.getEmail(), u.getRendszam()});
            }

            // 2. Parkolohelyek terkepenek frissitese
            List<ParkingSpot> spots = DatabaseManager.getAllParkingSpots();
            mapGridPanel.removeAll();

            for (ParkingSpot spot : spots) {
                mapGridPanel.add(createSpotCard(spot));
            }

            mapGridPanel.revalidate();
            mapGridPanel.repaint();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Adatbázis hiba a frissítés során:\n" + ex.getMessage(),
                    "Hiba", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel createSpotCard(ParkingSpot spot) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setPreferredSize(new Dimension(170, 145));
        Color bgColor = spot.isFoglalt() ? new Color(231, 76, 60) : new Color(46, 204, 113);
        card.setBackground(bgColor);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.DARK_GRAY, 2),
                new EmptyBorder(8, 8, 8, 8)
        ));

        // Fejlec: Sor-Hely es Oradij
        JLabel titleLbl = new JLabel(spot.getKod() + " (" + spot.getOradij() + " Ft/ó)", SwingConstants.CENTER);
        titleLbl.setFont(new Font("SansSerif", Font.BOLD, 16));
        titleLbl.setForeground(Color.WHITE);
        card.add(titleLbl, BorderLayout.NORTH);

        // Kozep: Statusz es foglalo adatai
        String infoHtml;
        if (spot.isFoglalt()) {
            String ido = spot.getFoglalasKezdete() != null ? spot.getFoglalasKezdete().format(TIME_FMT) : "-";
            infoHtml = "<html><div style='text-align:center; color:white;'>" +
                    "<b>FOGLALT</b><br>" +
                    "Rendszám: <b>" + (spot.getRendszam() != null ? spot.getRendszam() : "-") + "</b><br>" +
                    "<small>" + (spot.getFelhasznaloNev() != null ? spot.getFelhasznaloNev() : "") + "</small><br>" +
                    "<small>Kezdet: " + ido + "</small>" +
                    "</div></html>";
        } else {
            infoHtml = "<html><div style='text-align:center; color:white;'>" +
                    "<b>SZABAD</b><br><br>" +
                    "Kattints a foglaláshoz!" +
                    "</div></html>";
        }
        JLabel centerLbl = new JLabel(infoHtml, SwingConstants.CENTER);
        card.add(centerLbl, BorderLayout.CENTER);

        // Gombok alul: Foglalas/Felszabaditas + Admin modban Torles gomb
        JPanel bottomBtns = new JPanel(new GridLayout(1, isAdminMode ? 2 : 1, 4, 0));
        bottomBtns.setOpaque(false);

        JButton actionBtn = new JButton(spot.isFoglalt() ? "Felszabadít" : "Lefoglal");
        actionBtn.setFont(new Font("SansSerif", Font.BOLD, 11));
        actionBtn.setForeground(Color.BLACK);
        actionBtn.addActionListener(e -> handleSpotClick(spot));
        bottomBtns.add(actionBtn);

        if (isAdminMode) {
            JButton delBtn = new JButton("Töröl");
            delBtn.setFont(new Font("SansSerif", Font.PLAIN, 11));
            delBtn.setForeground(Color.BLACK);
            delBtn.addActionListener(e -> handleDeleteSpot(spot));
            bottomBtns.add(delBtn);
        }

        card.add(bottomBtns, BorderLayout.SOUTH);
        return card;
    }

    private void handleSpotClick(ParkingSpot spot) {
        try {
            if (!spot.isFoglalt()) {
                // Foglalas inditasa
                User activeUser = (User) userComboBox.getSelectedItem();
                if (activeUser == null) {
                    JOptionPane.showMessageDialog(this,
                            "Nincs kiválasztva felhasználó! Kérlek, hozz létre vagy válassz ki egy felhasználót a felső sávban.",
                            "Nincs felhasználó", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                int confirm = JOptionPane.showConfirmDialog(this,
                        "Lefoglalod a(z) " + spot.getKod() + " parkolóhelyet?\n" +
                        "Felhasználó: " + activeUser.getNev() + " (" + activeUser.getRendszam() + ")\n" +
                        "Óradíj: " + spot.getOradij() + " Ft/óra",
                        "Foglalás megerősítése", JOptionPane.YES_NO_OPTION);

                if (confirm == JOptionPane.YES_OPTION) {
                    DatabaseManager.bookSpot(spot.getId(), activeUser.getId());
                    refreshAllData();
                }
            } else {
                // Felszabaditas es dijszamitas
                int confirm = JOptionPane.showConfirmDialog(this,
                        "Felszabadítod a(z) " + spot.getKod() + " parkolóhelyet és lezárod a parkolást?\n" +
                        "Foglaló: " + spot.getFelhasznaloNev() + " (" + spot.getRendszam() + ")",
                        "Parkolás lezárása és díjszámítás", JOptionPane.YES_NO_OPTION);

                if (confirm == JOptionPane.YES_OPTION) {
                    DatabaseManager.FeeCalculation calc = DatabaseManager.releaseSpot(spot);
                    refreshAllData();
                    String msg = String.format(
                            "Parkolóhely (%s) sikeresen felszabadítva!\n\n" +
                            "Foglaló: %s (%s)\n" +
                            "Foglalás kezdete: %s\n" +
                            "Felszabadítás ideje: %s\n" +
                            "Eltelt idő: %d perc\n" +
                            "Óradíj: %d Ft/óra\n\n" +
                            "FIZETENDŐ ÖSSZEG: %d Ft",
                            spot.getKod(),
                            spot.getFelhasznaloNev(), spot.getRendszam(),
                            calc.kezdet.format(TIME_FMT),
                            calc.veg.format(TIME_FMT),
                            calc.minutes,
                            calc.oradij,
                            calc.fizetendoForint
                    );
                    JOptionPane.showMessageDialog(this, msg, "Parkolási Díj Összesítő", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Adatbázis hiba:\n" + ex.getMessage(), "Hiba", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleDeleteSpot(ParkingSpot spot) {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Biztosan törlöd a(z) " + spot.getKod() + " parkolóhelyet az adatbázisból?",
                "Parkolóhely törlése", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                DatabaseManager.deleteParkingSpot(spot.getId());
                refreshAllData();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Hiba a törlés során:\n" + ex.getMessage(), "Hiba", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showAddUserDialog() {
        JTextField tfNev = new JTextField(15);
        JTextField tfEmail = new JTextField(15);
        JTextField tfRendszam = new JTextField(10);

        JPanel p = new JPanel(new GridLayout(3, 2, 5, 5));
        p.add(new JLabel("Név:")); p.add(tfNev);
        p.add(new JLabel("Email:")); p.add(tfEmail);
        p.add(new JLabel("Rendszám:")); p.add(tfRendszam);

        int res = JOptionPane.showConfirmDialog(this, p, "Új felhasználó regisztrálása", JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION) {
            String nev = tfNev.getText().trim();
            String email = tfEmail.getText().trim();
            String rendszam = tfRendszam.getText().trim().toUpperCase();
            if (nev.isEmpty() || email.isEmpty() || rendszam.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Minden mező kitöltése kötelező!", "Hiányzó adatok", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                DatabaseManager.addUser(nev, email, rendszam);
                refreshAllData();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Hiba a felhasználó mentésekor (pl. már létező rendszám):\n" + ex.getMessage(),
                        "Adatbázis hiba", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteSelectedUser() {
        int row = userTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Válassz ki egy felhasználót a táblázatból a törléshez!", "Nincs kijelölés", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int userId = (Integer) userTableModel.getValueAt(row, 0);
        String nev = (String) userTableModel.getValueAt(row, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Biztosan törlöd a következő felhasználót: " + nev + "?\n(Az általa foglalt helyek automatikusan felszabadulnak.)",
                "Felhasználó törlése", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                DatabaseManager.deleteUser(userId);
                refreshAllData();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Hiba a törlés során:\n" + ex.getMessage(), "Hiba", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}