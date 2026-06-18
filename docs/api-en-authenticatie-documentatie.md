# SportClub – Web API & Authenticatie Documentatie

---

## 1. Web API & Azure Cloud

De backend is gebouwd met **Ktor (Kotlin)** en draait op **Azure App Service**. De database is **PostgreSQL 16**, gehost op Azure Database for PostgreSQL. Gevoelige gegevens (JWT-secret, databasewachtwoord) worden beheerd via **Azure Key Vault**.

### Architectuur

```plantuml
@startuml
skinparam componentStyle rectangle
skinparam backgroundColor #FAFAFA

package "Client" {
  [Web App] as web
  [Android App] as android
}
package "Azure Cloud" {
  [Azure App Service\n(Ktor Backend :8080)] as backend
  [Azure Database\nfor PostgreSQL 16] as db
  [Azure Key Vault\n(secrets)] as kv
}

web --> backend : HTTPS REST /api/v1/*
android --> backend : HTTPS REST /api/v1/*
backend --> db : JDBC
backend --> kv : Managed Identity
@enduml
```

**Base URL:** `https://<app-service>.azurewebsites.net/api/v1`

### Endpoints overzicht

| Groep | Methode | Pad | Toegang |
|---|---|---|---|
| Auth | POST | `/auth/login` | Publiek |
| Auth | POST | `/auth/register` | Publiek |
| Gebruikers | GET/PUT | `/users/me` | Ingelogd |
| Gebruikers | GET/POST/PUT/DELETE | `/users`, `/users/{id}`, `/users/instructors` | Employee |
| Workouts | GET | `/workouts` | Publiek |
| Workouts | POST/PUT/DELETE | `/workouts/{id}` | Employee |
| Lessen | GET | `/lessons`, `/lessons/{id}/occupancy` | Publiek |
| Lessen | POST/PUT/DELETE | `/lessons`, `/lessons/recurring` | Employee |
| Lessen | GET | `/lessons/my-lessons` | Instructor |
| Reserveringen | GET/POST/DELETE | `/reservations/me`, `/reservations` | Ingelogd |
| Wachtlijst | GET/POST/DELETE | `/reservations/waitlist/me` | Ingelogd |
| Deelnemerslijst | GET | `/reservations/lesson/{id}` | Instructor/Employee |
| Lidmaatschappen | GET | `/memberships/prices` | Publiek |
| Lidmaatschappen | GET/POST/PUT | `/memberships/me`, `/memberships/{id}/cancel` | Ingelogd |

### Voorbeelden

**Inloggen:**
```http
POST /api/v1/auth/login
Content-Type: application/json

{ "email": "jan@example.com", "password": "Welkom123!" }
```
```json
{ "token": "eyJhbGci...", "userId": "a1b2c3...", "role": "Member", "email": "jan@example.com" }
```

**Les reserveren:**
```http
POST /api/v1/reservations
Authorization: Bearer eyJhbGci...
Content-Type: application/json

{ "lessonId": "c3d4e5...", "bikeId": null }
```
```json
{ "id": "a7b8c9...", "lessonId": "c3d4e5...", "userId": "a1b2c3...", "status": "Reserved" }
```

**Lessen ophalen (datumbereik):**
```http
GET /api/v1/lessons?from=2026-06-16T00:00:00&to=2026-06-22T23:59:59
```

---

## 2. Authenticatie en autorisatie

### Aanpak

De API gebruikt **JWT (JSON Web Token)** met het **HMAC256-algoritme**. Er is geen externe identity provider (geen Azure AD, geen OAuth). De backend beheert zelf de tokens en wachtwoordcontrole.

### Loginflow

```plantuml
    @startuml
    skinparam backgroundColor #FAFAFA
    actor Gebruiker
    participant Frontend
    participant "Ktor Backend" as api
    database PostgreSQL
    
    Gebruiker -> Frontend : e-mail + wachtwoord
    Frontend -> api : POST /auth/login
    api -> PostgreSQL : Zoek gebruiker op e-mail
    PostgreSQL --> api : Gebruikersrij + BCrypt-hash
    api -> api : BCrypt.verify(wachtwoord, hash)
    alt Geldig
      api -> api : Genereer JWT (24 uur geldig)
      api --> Frontend : { token, userId, role }
      Frontend -> Frontend : Sla token op
    else Ongeldig
      api --> Frontend : 401 Unauthorized
    end
    @enduml
```

### JWT-structuur

Het token bevat drie onderdelen: **header**, **payload** en **signature**.

```
eyJhbGciOiJIUzI1NiJ9 . eyJ1c2VySWQiOiJhMWIy..., "role": "Member", "exp": 1750118400 } . <handtekening>
```

De backend verifieert bij elke beveiligde aanvraag de **handtekening** (met het JWT-secret uit Key Vault), de **issuer/audience** en de **vervaldatum**.

```http
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

### Wachtwoordbeveiliging

Wachtwoorden worden gehashed met **BCrypt (kostfactor 12)** – nooit als platte tekst opgeslagen. BCrypt bevat een willekeurig salt, waardoor hetzelfde wachtwoord altijd een andere hash oplevert. De hoge kostfactor maakt brute-force aanvallen traag.

### Rollen en toegangsrechten

Er zijn drie rollen, opgeslagen als claim in het JWT-token:

```plantuml
@startuml
skinparam backgroundColor #FAFAFA

actor Member
actor Instructor
actor Employee

Member --> (Lessen bekijken)
Member --> (Les reserveren / annuleren)
Member --> (Wachtlijst beheren)
Member --> (Eigen profiel & lidmaatschap)

Instructor --> (Eigen lessen inzien)
Instructor --> (Deelnemerslijst inzien)

Employee --> (Gebruikers & instructeurs beheren)
Employee --> (Workouts & lessen beheren)
Employee --> (Deelnemerslijst inzien)
Employee --> (Alle Member-rechten)
@enduml
```

| Rol | Standaard voor | Beschrijving |
|---|---|---|
| **Member** | Nieuwe registraties | Kan lessen boeken en lidmaatschap beheren |
| **Instructor** | Aangemaakt door Employee | Kan eigen lessen en deelnemers inzien |
| **Employee** | Beheerder | Volledige toegang tot alle beheerfuncties |

### Foutcodes

| Code | Betekenis |
|---|---|
| `401 Unauthorized` | Token ontbreekt, ongeldig of verlopen |
| `403 Forbidden` | Ingelogd, maar verkeerde rol |
| `400 Bad Request` | Ongeldige invoer |
| `409 Conflict` | Dubbele boeking, les vol, annulering te laat |
| `404 Not Found` | Resource bestaat niet |

---
*Versie 1.0 – Juni 2026*
