package parkolo;

import java.time.LocalDateTime;

public class ParkingSpot {
    private int id;
    private String sor;
    private int hely;
    private int oradij;
    private boolean foglalt;
    private Integer felhasznaloId;
    private String felhasznaloNev;
    private String rendszam;
    private LocalDateTime foglalasKezdete;

    public ParkingSpot(int id, String sor, int hely, int oradij, boolean foglalt,
                       Integer felhasznaloId, String felhasznaloNev, String rendszam, LocalDateTime foglalasKezdete) {
        this.id = id;
        this.sor = sor;
        this.hely = hely;
        this.oradij = oradij;
        this.foglalt = foglalt;
        this.felhasznaloId = felhasznaloId;
        this.felhasznaloNev = felhasznaloNev;
        this.rendszam = rendszam;
        this.foglalasKezdete = foglalasKezdete;
    }

    public int getId() { return id; }
    public String getSor() { return sor; }
    public int getHely() { return hely; }
    public int getOradij() { return oradij; }
    public boolean isFoglalt() { return foglalt; }
    public Integer getFelhasznaloId() { return felhasznaloId; }
    public String getFelhasznaloNev() { return felhasznaloNev; }
    public String getRendszam() { return rendszam; }
    public LocalDateTime getFoglalasKezdete() { return foglalasKezdete; }

    public String getKod() {
        return sor + "-" + hely;
    }
}
