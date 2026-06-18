# Frontend SportClub

Kotlin Multiplatform project met een **web-app**, **Android-app** en **desktop-app** voor een sportclub. Gebouwd met Compose Multiplatform.

---

## Vereisten

- **JDK 17+**
- **Android Studio** (voor de Android-app)
- **Node.js** (voor de web-app, wordt automatisch beheerd door Gradle)
- De **backend** moet draaien op `http://localhost:8080` voordat je de apps opstart

---

## Opstarten

### Web-app

**Windows (PowerShell):**
```powershell
.\gradlew.bat :webApp:jsBrowserDevelopmentRun
```

**Mac/Linux of Git Bash:**
```bash
./gradlew :webApp:jsBrowserDevelopmentRun
```

Open daarna handmatig de browser op **http://localhost:8081**

- Standaard zie je het **medewerkers/admin portaal**
- Ga naar `http://localhost:8081/#/app` voor de **leden- en instructeurs-app**

> De terminal toont `BUILD FAILED` wanneer je de server stopt met Ctrl+C — dit is normaal en geen echte fout.

---

### Android-app

Open het project in **Android Studio** en klik op **Run**, of via de terminal:

```bash
./gradlew :androidApp:assembleDebug
```

Installeer de gegenereerde APK op een emulator of fysiek apparaat.

> De Android-app verbindt automatisch met `http://10.0.2.2:8080` (de emulator-alias voor localhost).

---

### Desktop-app

```bash
./gradlew :desktopApp:run
```

Of met hot reload (herstart automatisch bij codewijzigingen):

```bash
./gradlew :desktopApp:hotRun --auto
```

---

## Projectstructuur

| Map | Inhoud |
|---|---|
| `shared/` | Gedeelde code (models, ViewModels, API-client) |
| `webApp/` | Browser-app (Kotlin/JS + Compose) |
| `androidApp/` | Android-app |
| `desktopApp/` | Desktop-app (JVM) |

---

## Backend

Alle apps verwachten de backend op **`http://localhost:8080/api/v1`**.  
Zorg dat de backend actief is voordat je een app opstart.
