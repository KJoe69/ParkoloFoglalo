# Parkoló Foglaló Alkalmazás (Java Swing + MySQL JDBC)

Ez a projekt egy **sima Java projekt (`No build tools`)** Visual Studio Code-hoz, amely MySQL 8.4.7 adatbázissal és Swing grafikus felülettel valósítja meg a vizuális parkolótérképet, a szerepkör-választót, a felhasználók és parkolóhelyek kezelését, valamint a percalapú díjszámítást.

## 1. Előfeltételek beállítása
1. **MySQL 8.4.7**: Győződj meg róla, hogy fut a MySQL szerver a `localhost:3306` címen (`root` felhasználó, üres jelszóval).
2. **Adatbázis létrehozása**: Futtasd le az alábbi SQL parancsot (ahogy a specifikációban megadtad):
   ```sql
   CREATE DATABASE IF NOT EXISTS `parkolo` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_hungarian_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
   ```
   *(A szükséges táblákat – `felhasznalok`, `parkolohelyek`, `foglalasok` – a Java program automatikusan létrehozza és feltölti kezdő tesztadatokkal az első indításkor!)*

3. **MySQL JDBC Driver (`mysql-connector-j`) elhelyezése**:
   - Töltsd le a hivatalos MySQL Connector/J `.jar` fájlt (pl. `mysql-connector-j-8.4.0.jar`) innen: https://dev.mysql.com/downloads/connector/j/ (válaszd a *Platform Independent* ZIP-et).
   - Másold be a kicsomagolt `.jar` fájlt a projekt **`lib/`** mappájába!
   - A `.vscode/settings.json` már előre be van állítva úgy, hogy a `lib/**/*.jar` fájlokat automatikusan betöltse a `Referenced Libraries` közé.

## 2. Futtatás Visual Studio Code-ban
1. Csomagold ki a `ParkoloFoglalo.zip` fájlt, és nyisd meg a `ParkoloFoglalo` mappát a VS Code-ban (**File -> Open Folder...**).
2. Másold a MySQL Connector `.jar` fájlt a `lib` mappába.
3. Nyisd meg az `src/parkolo/App.java` fájlt, és kattints a `main` metódus feletti **Run** gombra (vagy nyomj **F5**-öt).
