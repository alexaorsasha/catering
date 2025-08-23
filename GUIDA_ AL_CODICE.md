# 📖 GUIDA AL CODICE

## 🔹 Introduzione
Questo modulo estende il sistema **CatERing** per includere la **gestione del personale**:
- Assunzioni, aggiornamenti, licenziamenti e promozioni.
- Gestione ferie con controlli sulle sovrapposizioni e sul monte ferie residuo.
- Gestione dei team e delle assegnazioni.
- Persistenza su **SQLite** con tabella `staff` e tabelle correlate (`holidays`, `feedback`, `team`, `recruitment_proposal`).
- Copertura con **test unitari**.

---

## 🔹 Struttura del progetto

```
catering/
 ├── database/
 │    ├── catering.db
 │    └── catering_init_sqlite.sql
 └── src/main/java/catering/
      ├── businesslogic/
      │    ├── staff/
      │    │    ├── Staff.java
      │    │    ├── StaffManager.java
      │    │    ├── StaffException.java
      │    │    ├── StaffEventReceiver.java
      │    │    ├── Holidays.java
      │    │    ├── Feedback.java
      │    │    ├── Team.java
      │    │    ├── RecruitmentProposal.java
      │    │    └── ...
      │    └── shift/, menu/, recipe/... (altri moduli già esistenti)
      └── persistence/
           ├── PersistenceManager.java
           └── StaffPersistence.java
```

Test unitari:
```
src/test/java/staff/
 ├── StaffTest.java
 └── StaffManagerTest.java
```

---

## 🔹 Classi principali

### 🧑‍🤝‍🧑 Staff
Rappresenta un dipendente.

**Campi principali**:
- `id`, `name`, `surname`, `fiscalCode` (univoco).
- `address`, `phone`, `email`.
- `role`, `status` (FREE, WORKING, FIRED, ON_HOLIDAY).
- `remainingHoliday`, `permanent`.
- `contractStartDate`, `contractEndDate`.

**Metodi chiave**:
- `addDetails(...)` – inizializza tutti i campi.
- Getter/setter per dati anagrafici e contrattuali.
- `setWorking()`, `setFree()` – aggiornano lo stato.

---

### 🛠️ StaffManager
Controller di dominio che gestisce tutte le operazioni sul personale.

**Operazioni principali**:
- `hireStaff(...)` – assume nuovo staff (unicità CF).
- `manageStaff(...)` – aggiorna indirizzo, telefono, email, ruolo.
- `fireStaff(...)` – licenzia (soft: status = FIRED).
- `promoteStaff(...)` – promuove staff o cambia ruolo.
- `requestHolidays(...)` – richiede ferie (con controllo sovrapposizioni).
- `approveHolidays(...)` / `denyHolidays(...)` – approvazione ferie.
- `assignStaff(...)` / `removeStaff(...)` – assegna o rimuove da un team.

Utilizza metodi di **notify** per avvisare i receiver (es. la persistenza).

---

### 💾 StaffPersistence
Gestisce la **persistenza in SQLite**.

**Tabelle create da `ensureSchema()`**:
- `staff` – dati dipendenti.
- `holidays` – ferie richieste.
- `feedback` – note sui dipendenti.
- `team`, `staff_team` – gestione team.
- `recruitment_proposal` – proposte di assunzione.

**Metodi principali**:
- `loadAllStaff()` – carica tutti i dipendenti.
- `updateAddStaff()`, `updateStaff()`, `updateDeleteStaff()` – CRUD staff.
- `updateHolidays()`, `existsHolidayOverlap()`, `decrementHolidays()`.
- `updateFeedback()`.
- `updateTeamStaff()` / `removeFromTeam()`.
- `updateRecruitmentProposal()`.

---

### 📅 Holidays
Rappresenta una richiesta di ferie.

- **Campi**: `id`, `staff`, `startDate`, `endDate`, `accepted`.
- **Metodi**:
    - `approveRequest()`, `denyRequest()`.
    - `save()`, `update()`, `delete()`.
    - `loadByStaff(staff)`.

---

### 📝 Feedback
Rappresenta un feedback associato a uno staff.

- **Campi**: `id`, `staff`, `description`.
- **Gestito da**: `StaffPersistence.updateFeedback()`.

---

### 👥 Team
Rappresenta un gruppo di staff.

- **Campi**: `id`, `teamSize`, `type`, `components`.
- **Metodi**: `addComponent()`, `removeComponent()`, `isStaffInTeam()`.

---

### 📑 RecruitmentProposal
Rappresenta una proposta di assunzione.

- **Campi**: `id`, `staff`, `accepted`.
- **Metodi**: `approveProposal()`, `denyProposal()`.

---

### 🔔 StaffEventReceiver
Interfaccia che riceve notifiche sugli eventi relativi allo staff.

- `updateAddStaff`, `updateStaff`, `updateDeleteStaff`.
- `updateHolidays`, `updateFeedback`, `updateTeamStaff`.
- `updateRecruitmentProposal`.
- Implementata da `StaffPersistence`.

---

## 🔹 Persistenza

Tutto passa da `PersistenceManager`, che fornisce:
- `executeUpdate(sql, params...)` – INSERT/UPDATE/DELETE.
- `executeQuery(sql, handler, params...)` – SELECT.
- `getLastId()` – ultimo id inserito.

---

## 🔹 Database (SQLite)

Estratto schema:

```sql
CREATE TABLE staff (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  name TEXT NOT NULL,
  surname TEXT NOT NULL,
  fiscal_code TEXT UNIQUE NOT NULL,
  address TEXT,
  phone TEXT,
  email TEXT,
  role TEXT,
  status TEXT DEFAULT 'FREE',
  remaining_holiday INTEGER DEFAULT 20,
  permanent INTEGER DEFAULT 0,
  contract_start_date TEXT,
  contract_end_date TEXT
);

CREATE TABLE holidays (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  staff_id INTEGER NOT NULL,
  start_date TEXT NOT NULL,
  end_date TEXT NOT NULL,
  accepted INTEGER DEFAULT 0,
  FOREIGN KEY(staff_id) REFERENCES staff(id)
);
```

(simili per `feedback`, `team`, `staff_team`, `recruitment_proposal`)

---

## 🔹 Test

### ✅ StaffTest
Testa le **operazioni base sullo staff**:
- Assunzione e controllo duplicati CF.
- Gestione attributi anagrafici.

### ✅ StaffManagerTest
Testa i **casi d’uso principali**:
1. Hire & List – aggiunta staff.
2. ManageStaff – aggiornamento attributi.
3. Request + Approve Holidays – scala ferie residue.
4. Deny Holidays – non scala ferie.
5. Overlap Holidays – lancia eccezione.
6. Fire staff – impedito se WORKING.
7. Assign & Remove staff dai team.

**Tutti i test passano ✔️**

---

## 🔹 Esempi d’uso

```java
StaffManager sm = new StaffManager();

// 1) Assunzione
Staff mario = sm.hireStaff("Mario", "Rossi", "MRARSS80A01F205X",
        "Via Roma 1", "3331234567", "mario@catering.it",
        "Cuoco", true);

// 2) Aggiornamento dati
sm.manageStaff(mario, "Via Milano 10", null, "nuova.email@catering.it", null);

// 3) Richiesta ferie
Holidays ferie = sm.requestHolidays(mario,
        LocalDateTime.of(2025, 8, 10, 0, 0),
        LocalDateTime.of(2025, 8, 20, 0, 0),
        null);

// 4) Approvazione ferie
sm.approveHolidays(ferie);

// 5) Assegnazione a un team
Team cucina = new Team();
cucina.setType("Cucina");
sm.assignStaff(mario, cucina);
```

---

# 📐 Pattern usati nel progetto

Il progetto applica diversi **pattern GoF** (Gang of Four) e **GRASP**.  
Per ogni pattern indichiamo **Cos’è**, **Dove**, **Perché**, **Esempio** (estratti Java) ed **Effetto**.

---

## 🔹 GoF

### **Singleton** → `CatERing`
- **Cos’è:** garantisce un’unica istanza globale.
- **Dove:** classe `CatERing`.
- **Perché:** `CatERing` rappresenta il *contesto applicativo* (accesso centralizzato ai manager) e deve essere unico.
- **Esempio:**
  ```java
  public final class CatERing {
      private static CatERing instance;
      private final StaffManager staffManager = new StaffManager();

      private CatERing() {}

      public static synchronized CatERing getInstance() {
          if (instance == null) instance = new CatERing();
          return instance;
      }

      public StaffManager getStaffManager() { return staffManager; }
  }

  // Uso
  CatERing app = CatERing.getInstance();
  StaffManager sm = app.getStaffManager();
- **Effetto:** una sola sorgente di verità; si evitano stati divergenti e cicli di vita non controllati.

---

### **Observer** → `StaffManager` ↔ `StaffEventReceiver` / `StaffPersistence`

- **Cos’è:** il *Subject* notifica automaticamente gli *Observer* quando cambia stato.
- **Dove:** `StaffManager` (Subject) e implementazioni di `StaffEventReceiver` (Observers, es. `StaffPersistence`).
- **Perché:** separare la logica di dominio dalla persistenza/telemetria; reagire agli eventi senza accoppiare componenti.
- **Esempio:**
  ```java
  public class StaffManager {
      private final List<StaffEventReceiver> receivers = new ArrayList<>();

      public void addReceiver(StaffEventReceiver r) { receivers.add(r); }

      private void notifyAdd(Staff s) { receivers.forEach(r -> r.updateAddStaff(s)); }
      private void notifyUpdate(Staff s) { receivers.forEach(r -> r.updateStaff(s)); }
      private void notifyHolidays(Staff s, Holidays h) {
          receivers.forEach(r -> r.updateHolidays(s, h));
      }
  }

  public class StaffPersistence implements StaffEventReceiver {
      @Override public void updateAddStaff(Staff s) { /* INSERT ... */ }
      @Override public void updateStaff(Staff s)    { /* UPDATE ... */ }
      @Override public void updateHolidays(Staff s, Holidays h) { /* INSERT/UPDATE holidays ... */ }
  }
- **Effetto:** *decoupling* tra dominio e I/O; test più semplici (mock/fake observer).

---

### **Strategy** → `StaffEventReceiver` (strategie di reazione agli eventi)

- **Cos’è:** consente di sostituire il comportamento a runtime senza toccare il contesto.

- **Dove:** interfaccia `StaffEventReceiver` con strategie alternative (p.es. persistenza, audit logger, metriche).

- **Perché:** estendere il sistema aggiungendo nuovi comportamenti agli eventi (senza modificare `StaffManager`).

- **Esempio:**
```java
public interface StaffEventReceiver {
    default void updateAddStaff(Staff s) {}
    default void updateStaff(Staff s) {}
    default void updateHolidays(Staff s, Holidays h) {}
}

public class AuditLoggerReceiver implements StaffEventReceiver {
    @Override public void updateAddStaff(Staff s) {
        System.out.println("[AUDIT] Hired: " + s.getFiscalCode());
    }
}

// Wiring
StaffManager sm = CatERing.getInstance().getStaffManager();
sm.addReceiver(new StaffPersistence());
sm.addReceiver(new AuditLoggerReceiver()); // strategia aggiuntiva
```

- **Effetto:** alta estendibilità; comportamenti plug-and-play per la stessa interfaccia.

---

## Composite → Team con Staff

- **Cos’è:** consente di trattare uniformemente oggetti singoli e composti.

- **Dove:** `Team` aggrega più `Staff` come componenti.

- **Perché:** un team è un insieme di membri, ma spesso va gestito come un’unità (assegnazioni, dimensione, ecc.).

- **Esempio:**
```java
public class Team {
    private final Set<Staff> components = new HashSet<>();

    public void addComponent(Staff s)     { components.add(s); }
    public void removeComponent(Staff s)  { components.remove(s); }
    public boolean isStaffInTeam(Staff s) { return components.contains(s); }
    public int size()                     { return components.size(); }
}

// Uso uniforme nello use case
public void assignStaff(Staff staff, Team team) { team.addComponent(staff); }
```

- **Effetto:** API più semplici e coerenti; operazioni omogenee su singoli e collezioni.

---

## 🔹 GRASP

### Controller → `StaffManager`

- **Cos’è:** oggetto che riceve input e coordina le operazioni del caso d’uso.

- **Dove:** `StaffManager`.

- **Perché:** centralizzare flussi “assumere”, “promuovere”, “ferie”, “assegnare a team”, applicando validazioni e orchestrazione.

- **Esempio:**
```java
public Staff hireStaff(String name, String surname, String cf, String addr, String phone, String email, String role, boolean permanent) {
    validateUniqueFiscalCode(cf);
    Staff s = new Staff();
    s.addDetails(name, surname, cf, addr, phone, email, role, permanent);
    notifyAdd(s);
    return s;
}
```

- **Effetto:** separazione UI/dominio; punto unico per policy e transazioni applicative.

---

### **Creator** → `StaffManager`

- **Cos’è:** assegna la responsabilità di creare oggetti a chi possiede i dati per istanziarli.

- **Dove:** `StaffManager` crea `Staff`, `Holidays`, `RecruitmentProposal`.

- **Perché:** il controller ha a disposizione parametri, regole e sequenza necessarie alla creazione coerente.

- **Esempio:**
```java
public Holidays requestHolidays(Staff staff, LocalDateTime start, LocalDateTime end, String note) {
    validateNoOverlap(staff, start, end);
    Holidays h = new Holidays(staff, start, end, false);
    notifyHolidays(staff, h);
    return h;
}
```

- **Effetto:** oggetti nascono già validi e consistenti; minori responsabilità alle entità chiamanti.

---

### Information Expert

- **Cos’è:** la responsabilità va alla classe che ha le informazioni necessarie per svolgerla.

**Dove:**
- `Staff` gestisce dati anagrafici e state transitions (`FREE`, `WORKING`, `FIRED`, `ON_HOLIDAY`).
- `Holidays` conosce inizio/fine e stato di approvazione.
- `StaffPersistence` detiene la conoscenza su mapping SQL.

**Perché:** ridurre passaggi di dati e duplicazioni, mantenendo le regole vicino ai dati.

- **Esempio:**
```java
public class Holidays {
    private boolean accepted;
    public void approveRequest() { this.accepted = true; }
    public void denyRequest()    { this.accepted = false; }
    public long days() { return Duration.between(startDate, endDate).toDays(); }
}
```

- **Effetto:** *responsabilità chiare*; codice più localizzato e manutenibile.

---

### Low Coupling

- **Cos’è:** minimizzare le dipendenze dirette tra componenti.

**Dove:**
- `StaffManager` dipende da `StaffEventReceiver` (interfaccia), non da `StaffPersistence`.
- Le entità (`Staff`, `Holidays`, `Team`) non conoscono il DB né le query.

**Perché:** facilitare test, sostituzioni, riuso e sviluppo parallelo.

- **Esempio:**
```java
public class StaffManager {
    private final List<StaffEventReceiver> receivers;

    public StaffManager(List<StaffEventReceiver> receivers) {
        this.receivers = new ArrayList<>(receivers); // dipendenza verso interfaccia
    }
}
```

- **Effetto:** flessibilità elevata (mock/fake nei test, pluggability a runtime).

---

### High Cohesion

- **Cos’è:** ogni classe ha responsabilità coese e circoscritte.

**Dove:**
- `StaffManager` → logica dei casi d’uso & orchestrazione.
- `StaffPersistence` → esclusivamente accesso dati/mapping SQL.
- `Staff` → stato e dati del dipendente.
- `Team` → membership e dimensione.

**Perché:** ridurre classi “dio” e codice spalmato; aumentare leggibilità e riuso.

- **Esempio:**
```java
// Esempio di coesione in StaffPersistence: solo I/O
public class StaffPersistence implements StaffEventReceiver {
    @Override public void updateStaff(Staff s) {
        // SQL UPDATE ... (nessuna logica di dominio qui)
    }
}
```

- **Effetto:** classi più semplici da capire, testare ed estendere senza effetti collaterali.
