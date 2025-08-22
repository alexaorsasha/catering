package catering.businesslogic.staff;

import catering.persistence.StaffPersistence;

import java.util.ArrayList;

public class StaffManager {
    private final ArrayList<UserEventReceiver> receivers = new ArrayList<>();
    private final StaffPersistence persistence = new StaffPersistence();

    public StaffManager() { receivers.add(persistence); } // registra la persistenza

    private void notifyAdd(Staff s) { receivers.forEach(r -> r.updateAddStaff(s)); }
    private void notifyUpdate(Staff s) { receivers.forEach(r -> r.updateStaff(s)); }
    @SuppressWarnings("unused") private void notifyDelete(Staff s) { receivers.forEach(r -> r.updateDeleteStaff(s)); }

    // 1) Consultare elenco personale
    public ArrayList<Staff> getAllStaff() { return persistence.loadAllStaff(); }

    // 2) Assumere un lavoratore (minimo per partire)
    public Staff hireStaff(String name, String surname, String fiscalCode,
                           String address, String phone, String email,
                           String role, boolean permanent) throws StaffException {
        if (name == null || surname == null || fiscalCode == null || fiscalCode.isBlank())
            throw new StaffException("Nome, cognome e CF sono obbligatori");
        // unicità CF basata sulla lista attuale
        for (Staff s0 : getAllStaff()) {
            if (fiscalCode.equalsIgnoreCase(s0.getFiscalCode())) {
                throw new StaffException("Esiste già uno staff con questo CF");
            }
        }
        Staff s = new Staff();
        s.addDetails(name, surname, fiscalCode, address, phone, email, role,
                "FREE", 20, permanent, null, null);
        notifyAdd(s);
        return s;
    }
}
