package staff;

import catering.businesslogic.CatERing;
import catering.businesslogic.staff.Staff;
import catering.businesslogic.staff.StaffException;
import catering.businesslogic.staff.StaffManager;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class StaffTest {

    @Test
    void hireAndList() throws StaffException {
        StaffManager sm = CatERing.getInstance().getStaffManager();
        ArrayList<Staff> before = sm.getAllStaff();

        Staff s = sm.hireStaff(
                "Mario", "Rossi", "MRARSS80A01H501X",
                "Via Verdi 10", "3331112222", "mario@example.com",
                "Waiter", true
        );

        assertTrue(s.getId() > 0);
        ArrayList<Staff> after = sm.getAllStaff();
        assertEquals(before.size() + 1, after.size());
    }
}
