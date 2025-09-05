package catering.businesslogic.staff;

import catering.persistence.StaffPersistence;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

public class StaffManager {
    private final ArrayList<StaffEventReceiver> receivers = new ArrayList<>();
    private final StaffPersistence persistence = new StaffPersistence();

    public StaffManager() {
        receivers.add(persistence);     // registra la persistenza
        // opzionale: abilita durante sviluppo per creare tabelle se mancano
        // persistence.ensureSchema();
    }

    /* ============ notify helpers ============ */
    private void notifyAdd(Staff s)    { receivers.forEach(r -> r.updateAddStaff(s)); }
    private void notifyUpdate(Staff s) { receivers.forEach(r -> r.updateStaff(s)); }
    private void notifyDelete(Staff s) { receivers.forEach(r -> r.updateDeleteStaff(s)); }
    private void notifyHoliday(Staff s, Holidays h) { receivers.forEach(r -> r.updateHolidays(s, h)); }
    private void notifyFeedback(Staff s, Feedback f) { receivers.forEach(r -> r.updateFeedback(s, f)); }
    private void notifyTeam(Staff s, Team t) { receivers.forEach(r -> r.updateTeamStaff(s, t)); }

    /* ============ queries ============ */
    public ArrayList<Staff> getAllStaff() { return persistence.loadAllStaff(); }

    /* ============ operations ============ */

    // Assumere
    public Staff hireStaff(String name, String surname, String fiscalCode,
                           String address, String phone, String email,
                           String role, boolean permanent) throws StaffException {
        if (name == null || surname == null || fiscalCode == null || fiscalCode.isBlank())
            throw new StaffException("Nome, cognome e CF sono obbligatori");
        for (Staff s0 : getAllStaff()) {
            if (fiscalCode.equalsIgnoreCase(s0.getFiscalCode())) {
                throw new StaffException("Esiste già uno staff con questo CF");
            }
        }
        Staff s = new Staff();
        s.addDetails(name, surname, fiscalCode, address, phone, email, role,
                Staff.STATUS_FREE, 20, permanent, null, null);
        notifyAdd(s);
        return s;
    }

    // Gestire dati (update parziale)
    public void manageStaff(Staff s, String newAddress, String newPhone, String newEmail, String newRole) {
        if (newAddress != null) s.setAddress(newAddress);
        if (newPhone != null)   s.setPhoneNumber(newPhone);
        if (newEmail != null)   s.setEmail(newEmail);
        if (newRole != null)    s.setRole(newRole);
        notifyUpdate(s); // -> UPDATE
    }

    // Licenziare (soft: cambia stato)
    public void fireStaff(Staff s) throws StaffException {
        if (s == null) throw new StaffException("Staff nullo");
        if (Staff.STATUS_WORKING.equalsIgnoreCase(s.getStatus()))
            throw new StaffException("Impossibile licenziare: dipendente in WORKING");

        s.setStatus(Staff.STATUS_FIRED);
        notifyUpdate(s);

    }

    //Licenziare (hard: delete)
    public void fireStaffHardDelete(Staff s) throws StaffException {
        if (s == null) throw new StaffException("Staff nullo");
        if (Staff.STATUS_WORKING.equalsIgnoreCase(s.getStatus()))
            throw new StaffException("Impossibile licenziare: dipendente in WORKING");
        notifyDelete(s); // StaffPersistence elimina da staff + tabelle correlate
    }



    // Promuovere
    public void promoteStaff(Staff s, String newRole) throws StaffException {
        if (s == null) throw new StaffException("Staff nullo");
        if (newRole == null || newRole.isBlank()) throw new StaffException("Ruolo non valido");
        s.setRole(newRole);
        notifyUpdate(s);
    }

    /* ===== FERIE ===== */

    public Holidays requestHolidays(Staff staff, LocalDateTime start, LocalDateTime end)
            throws StaffException {
        if (staff == null || start == null || end == null || !start.isBefore(end))
            throw new StaffException("Date ferie non valide");
        if (persistence.existsHolidayOverlap(staff, start.toString(), end.toString()))
            throw new StaffException("Periodo di ferie sovrapposto");

        Holidays h = new Holidays(staff, start, end); // accepted=false
        notifyHoliday(staff, h); // -> INSERT in holidays
        return h;
    }

    public void approveHolidays(Holidays h) throws StaffException {
        if (h == null) throw new StaffException("Holiday null");
        Staff s = h.getStaff();
        int req = (int) ChronoUnit.DAYS.between(h.getStartDate().toLocalDate(), h.getEndDate().toLocalDate()) + 1;
        if (req > s.getRemainingHoliday()) throw new StaffException("Ferie insufficienti");
        h.approveRequest();
        notifyHoliday(s, h);            // -> UPDATE accepted=1
        persistence.decrementHolidays(s, req);
        persistence.setStatus(s, Staff.STATUS_ON_HOLIDAY); // opzionale
        notifyUpdate(s);
    }

    public void denyHolidays(Holidays h) throws StaffException {
        if (h == null) throw new StaffException("Holiday null");
        h.denyRequest();
        notifyHoliday(h.getStaff(), h);
    }

    /* ===== TEAM ===== */

    public void assignStaff(Staff staff, Team team) throws StaffException {
        if (staff == null || team == null) throw new StaffException("Staff/Team nullo");
        if (Staff.STATUS_ON_HOLIDAY.equalsIgnoreCase(staff.getStatus()))
            throw new StaffException("Impossibile assegnare: in ferie");
        team.addComponent(staff);
        staff.setWorking();
        notifyTeam(staff, team);
        notifyUpdate(staff);
    }

    public void removeStaff(Staff staff, Team team) throws StaffException {
        if (staff == null || team == null) throw new StaffException("Staff/Team nullo");
        team.removeComponent(staff);
        persistence.removeFromTeam(staff, team);
        staff.setFree();
        notifyTeam(staff, team);
        notifyUpdate(staff);
    }

    /* ===== FEEDBACK ===== */

    public void addFeedback(Staff s, String feedback) throws StaffException {
        if (s == null || feedback == null) throw new StaffException("Staff/Feedback nullo");
        Feedback f = new Feedback(s, feedback);
        persistence.updateFeedback(s, f);
        notifyFeedback(s, f);


    }
}
