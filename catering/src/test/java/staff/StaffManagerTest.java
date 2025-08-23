package staff;

import catering.businesslogic.CatERing;
import catering.businesslogic.staff.*;
import catering.persistence.PersistenceManager;
import org.junit.jupiter.api.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.DisplayName.class)
public class StaffManagerTest {

    private StaffManager sm;

    private static String uniqueCF() {
        return "TESTCF_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    @BeforeEach
    void cleanAndInit() {
        // pulizia dati di precedenti run (solo quelli dei test)
        PersistenceManager.executeUpdate("DELETE FROM holidays WHERE staff_id IN (SELECT id FROM staff WHERE fiscal_code LIKE 'TESTCF_%')");
        PersistenceManager.executeUpdate("DELETE FROM feedback WHERE staff_id IN (SELECT id FROM staff WHERE fiscal_code LIKE 'TESTCF_%')");
        PersistenceManager.executeUpdate("DELETE FROM staff_team WHERE staff_id IN (SELECT id FROM staff WHERE fiscal_code LIKE 'TESTCF_%')");
        PersistenceManager.executeUpdate("DELETE FROM staff WHERE fiscal_code LIKE 'TESTCF_%'");
        PersistenceManager.executeUpdate("DELETE FROM team   WHERE type LIKE 'TESTTEAM_%'");

        sm = CatERing.getInstance().getStaffManager();

    }

    @Test
    @DisplayName("01 - Hire & List aumenta di 1")
    void hireAndList() throws StaffException {
        ArrayList<Staff> before = sm.getAllStaff();

        Staff s = sm.hireStaff(
                "Mario", "Rossi", uniqueCF(),
                "Via Verdi 10", "3331112222", "mario@example.com",
                "Waiter", true
        );

        assertTrue(s.getId() > 0);
        ArrayList<Staff> after = sm.getAllStaff();
        assertEquals(before.size() + 1, after.size());
    }

    @Test
    @DisplayName("02 - ManageStaff aggiorna indirizzo/telefono/email/ruolo")
    void manageStaff_updates() throws StaffException {
        Staff s = sm.hireStaff("Luca","Bianchi", uniqueCF(),
                "Via Roma 1","333123","luca@ex.com","Runner", true);

        sm.manageStaff(s, "Via Milano 22", "3339998888", "luca.new@ex.com", "Chef");

        assertEquals("Via Milano 22", s.getAddress());
        assertEquals("3339998888", s.getPhone());
        assertEquals("luca.new@ex.com", s.getEmail());
        assertEquals("Chef", s.getRole());
    }

    @Test
    @DisplayName("03 - Request + Approve Holidays scala le ferie")
    void holidays_request_approve() throws StaffException {
        Staff s = sm.hireStaff("Anna","Neri", uniqueCF(),
                "Via A","3400000000","anna@ex.com","Waiter", true);
        int before = s.getRemainingHoliday();

        Holidays h = sm.requestHolidays(s,
                LocalDateTime.of(2025,7,1,0,0),
                LocalDateTime.of(2025,7,3,23,59),
                3);

        assertNotNull(h);
        assertTrue(h.getId() > 0);
        assertEquals(before, s.getRemainingHoliday(), "Non devono scalare prima dell'approvazione");

        sm.approveHolidays(h);
        assertEquals(before - 3, s.getRemainingHoliday());
        assertTrue(h.getAccepted());
    }

    @Test
    @DisplayName("04 - Deny Holidays non scala le ferie")
    void holidays_deny_no_decrement() throws StaffException {
        Staff s = sm.hireStaff("Sara","Blu", uniqueCF(),
                "Via B","3401111111","sara@ex.com","Chef", true);
        int before = s.getRemainingHoliday();

        Holidays h = sm.requestHolidays(s,
                LocalDateTime.of(2025,8,10,0,0),
                LocalDateTime.of(2025,8,12,23,59),
                3);

        sm.denyHolidays(h);
        assertFalse(h.getAccepted());
        assertEquals(before, s.getRemainingHoliday());
    }

    @Test
    @DisplayName("05 - Overlap Holidays lancia StaffException")
    void holidays_overlap_throws() throws StaffException {
        Staff s = sm.hireStaff("Paolo","Verdi", uniqueCF(),
                "Via C","3402222222","paolo@ex.com","Waiter", true);

        Holidays h1 = sm.requestHolidays(s,
                LocalDateTime.of(2025,9,1,0,0),
                LocalDateTime.of(2025,9,3,23,59),
                3);
        assertTrue(h1.getId() > 0);

        assertThrows(StaffException.class, () ->
                sm.requestHolidays(s,
                        LocalDateTime.of(2025,9,2,0,0),   // overlap
                        LocalDateTime.of(2025,9,4,23,59),
                        3)
        );
    }

    @Test
    @DisplayName("06 - Fire lancia se staff in WORKING")
    void fire_whenWorking_throws() throws StaffException {
        Staff s = sm.hireStaff("Marta","Gialli", uniqueCF(),
                "Via D","3403333333","marta@ex.com","Assistant", true);
        s.setWorking(); // simuliamo lavoratore occupato

        assertThrows(StaffException.class, () -> sm.fireStaff(s));
    }

    @Test
    @DisplayName("07 - Assign e Remove Team cambiano lo stato")
    void assign_remove_team_changes_status() throws StaffException {
        // crea un team reale a DB
        PersistenceManager.executeUpdate("INSERT INTO team (team_size, type) VALUES (?, ?)", 0, "TESTTEAM_" + UUID.randomUUID());
        int teamId = PersistenceManager.getLastId();
        Team team = Team.createTeam();
        team.setId(teamId);
        team.setType("sala");

        Staff s = sm.hireStaff("Gino","Rossi", uniqueCF(),
                "Via E","3404444444","gino@ex.com","Runner", true);

        sm.assignStaff(s, team);
        assertEquals("WORKING", s.getStatus());

        sm.removeStaff(s, team);
        assertEquals("FREE", s.getStatus());
    }

    @Test
    @DisplayName("08 - PromoteStaff aggiorna il ruolo")
    void promote_staff_changes_role() throws StaffException {
        Staff s = sm.hireStaff("Leo","Mauri", uniqueCF(),
                "Via F","3405555555","leo@ex.com","Runner", true);

        sm.promoteStaff(s, "Chef");
        assertEquals("Chef", s.getRole());
    }

    @Test
    @DisplayName("09 - Approve Holidays con ferie insufficienti lancia")
    void approve_holidays_insufficient_throws() throws StaffException {
        Staff s = sm.hireStaff("Iris","Blu", uniqueCF(),
                "Via G","3406666666","iris@ex.com","Assistant", true);

        // imposta poche ferie
        s.setRemainingHoliday(1);

        Holidays h = sm.requestHolidays(s,
                LocalDateTime.of(2025,10,1,0,0),
                LocalDateTime.of(2025,10,3,23,59),
                3);

        assertThrows(StaffException.class, () -> sm.approveHolidays(h));
    }

    @Test
    @DisplayName("10 - Fire hard-delete rimuove lo staff dal DB")
    void fire_hard_delete_removes_staff() throws StaffException {
        Staff s = sm.hireStaff("Nina","Rosa", uniqueCF(),
                "Via H","3407777777","nina@ex.com","Waiter", true);

        // verifichiamo presente
        var before = sm.getAllStaff().stream().filter(x -> x.getId() == s.getId()).count();
        assertEquals(1, before);

        sm.fireStaffHardDelete(s);

        // ricarica da DB e verifica assenza
        var after = sm.getAllStaff().stream().filter(x -> x.getId() == s.getId()).count();
        assertEquals(0, after);
    }
}
