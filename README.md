# Parkolófoglaló alkalmazás

Java Swing grafikus felületű parkolókezelő alkalmazás MySQL-adatbázissal. A program parkolóhelyeket és felhasználókat kezel, foglaláskor pedig nyilvántartja a parkolás kezdetét és a felszabadításkor kiszámítja a fizetendő díjat.

## Belépési pont

A program belépési pontja:

```text
src/parkolo/App.java
```

Az `App` osztály `main` metódusa beállítja a Swing megjelenését, inicializálja az adatbázist, majd megjeleníti a szerepkör-választót. Innen felhasználói vagy adminisztrátori módban nyitható meg a főablak (`MainFrame`).

## Fő funkciók

- **Vizuális parkolótérkép:** a helyek színe jelzi, hogy szabadok vagy foglaltak; a foglalt helyeknél látható a foglaló, a rendszám és a foglalás kezdete.
- **Foglalás és felszabadítás:** szabad hely foglalható a kiválasztott felhasználó nevére. Felszabadításkor a program megjeleníti a parkolás időtartamát és a fizetendő összeget.
- **Díjszámítás:** a parkolóhely óradíja alapján, az eltelt percekből számolja a díjat; a számítás legalább egy percet vesz figyelembe, az összeget pedig egész forintra felfelé kerekíti.
- **Felhasználókezelés:** új felhasználó vehető fel névvel, e-mail-címmel és rendszámmal. Adminisztrátori módban felhasználó is törölhető; ilyenkor a hozzá tartozó foglalások felszabadulnak.
- **Parkolóhely-kezelés:** adminisztrátori módban hely adható hozzá sorral, sorszámmal és óradíjjal, illetve hely törölhető.
- **Szerepkörváltás és frissítés:** a főablakban bármikor váltható a felhasználói és adminisztrátori nézet, illetve frissíthetők az adatok.
- **MySQL-adattárolás:** az alkalmazás indításkor létrehozza a szükséges táblákat. Üres parkolóhely-tábla esetén alap felhasználókat és parkolóhelyeket is feltölt tesztadatként.

## Szükséges környezet

- Java-kompatibilis fejlesztői környezet (a projekt Visual Studio Code-ban, külön build eszköz nélkül használható).
- MySQL 8.4.7 szerver, alapértelmezés szerint `localhost:3306` címen, `root` felhasználóval és üres jelszóval. A kapcsolati beállítások a `src/parkolo/DatabaseManager.java` fájlban találhatók.
- MySQL Connector/J JDBC-driver a projekt `lib/` mappájában. A VS Code beállítása a `lib/**/*.jar` fájlokat automatikusan felveszi a hivatkozott könyvtárak közé.
- A `parkolo` adatbázis előzetes létrehozása:

```sql
CREATE DATABASE IF NOT EXISTS `parkolo`
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_hungarian_ci;
```

Az adatbázisban a `felhasznalok`, `parkolohelyek` és `foglalasok` táblákat a program automatikusan létrehozza.

## Indítás Visual Studio Code-ban

1. Nyisd meg a projekt mappáját a VS Code-ban.
2. Helyezd el a MySQL Connector/J `.jar` fájlt a `lib/` mappában.
3. Ellenőrizd, hogy fut a MySQL-szerver, és létezik a `parkolo` adatbázis.
4. Nyisd meg az `src/parkolo/App.java` fájlt, majd indítsd el a `main` metódust a **Run** gombbal vagy az **F5** billentyűvel.
