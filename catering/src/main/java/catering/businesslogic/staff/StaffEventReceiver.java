package catering.businesslogic.staff;

import catering.businesslogic.shift.Shift;
import catering.businesslogic.user.User;

public interface StaffEventReceiver {
    default void updateAddStaff(Staff s) {}
    default void updateStaff(Staff s) {}
    default void updateDeleteStaff(Staff s) {}

    default void updateHolidays(Staff s, Holidays h) {}
    default void updateFeedback(Staff s, Feedback f) {}
    default void updateTeamStaff(Staff s, Team t) {}

    default void updateRecruitmentProposal(Staff s, RecruitmentProposal proposal) {}

    default void dateShiftUser(User usr, Shift shift) {}
}
