package catering.businesslogic.staff;

import catering.businesslogic.user.User;
import catering.businesslogic.shift.Shift;

public interface UserEventReceiver {
    void updateAddStaff(Staff staff);
    void updateStaff(Staff staff);
    void updateDeleteStaff(Staff staff);
    // le altre (holidays/feedback/team) le aggiungeremo dopo
    default void dateShiftUser(User usr, Shift shift) { /*  */ }
}
