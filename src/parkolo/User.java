package parkolo;

public class User {
    private int id;
    private String nev;
    private String email;
    private String rendszam;

    public User(int id, String nev, String email, String rendszam) {
        this.id = id;
        this.nev = nev;
        this.email = email;
        this.rendszam = rendszam;
    }

    public int getId() { return id; }
    public String getNev() { return nev; }
    public String getEmail() { return email; }
    public String getRendszam() { return rendszam; }

    @Override
    public String toString() {
        return nev + " (" + rendszam + ") - " + email;
    }
}
