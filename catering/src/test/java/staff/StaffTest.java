package staff;

import catering.businesslogic.CatERing;
import catering.businesslogic.staff.Staff;
import catering.businesslogic.staff.StaffException;
import catering.businesslogic.staff.StaffManager;
import catering.persistence.PersistenceManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class StaffTest {

    // CF univoco per evitare collisioni tra run
    private static String uniqueCF() {
        return "TESTCF_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    @BeforeEach
    void cleanOnlyTestData() {
        // rimuove SOLO i dati creati dai test (safe)
        PersistenceManager.executeUpdate(
                "DELETE FROM holidays WHERE staff_id IN (SELECT id FROM staff WHERE fiscal_code LIKE 'TESTCF_%')"
        );
        PersistenceManager.executeUpdate(
                "DELETE FROM feedback WHERE staff_id IN (SELECT id FROM staff WHERE fiscal_code LIKE 'TESTCF_%')"
        );
        PersistenceManager.executeUpdate(
                "DELETE FROM staff_team WHERE staff_id IN (SELECT id FROM staff WHERE fiscal_code LIKE 'TESTCF_%')"
        );
        PersistenceManager.executeUpdate(
                "DELETE FROM staff WHERE fiscal_code LIKE 'TESTCF_%'"
        );
        PersistenceManager.executeUpdate(
                "DELETE FROM team WHERE type LIKE 'TESTTEAM_%'"
        );
    }

    @Test
    void hireAndList() throws StaffException {
        StaffManager sm = CatERing.getInstance().getStaffManager();

        ArrayList<Staff> before = sm.getAllStaff();

        Staff s = sm.hireStaff(
                "Mario", "Rossi", uniqueCF(),
                "Via Verdi 10", "3331112222", "mario@example.com",
                "Waiter", true
        );

        assertTrue(s.getId() > 0, "Dopo l'insert l'ID deve essere > 0");
        ArrayList<Staff> after = sm.getAllStaff();
        assertEquals(before.size() + 1, after.size(), "La lista deve crescere di 1");
    }
}
