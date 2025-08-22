package catering.persistence;

import catering.businesslogic.staff.Staff;
import catering.businesslogic.staff.UserEventReceiver;
import catering.persistence.PersistenceManager;
import catering.persistence.ResultHandler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class StaffPersistence implements UserEventReceiver {

    /* ===================== LOAD HELPERS ===================== */

    public ArrayList<Staff> loadAllStaff() {
        final ArrayList<Staff> list = new ArrayList<>();

        String sql = "SELECT id, name, surname, fiscal_code, address, phone, email, role, status, "
                + "remaining_holiday, permanent, contract_start_date, contract_end_date "
                + "FROM staff ORDER BY surname, name";

        PersistenceManager.executeQuery(sql, new ResultHandler() {
            @Override
            public void handle(ResultSet rs) throws SQLException {
                Staff s = new Staff();
                s.addDetails(
                        rs.getString("name"),
                        rs.getString("surname"),
                        rs.getString("fiscal_code"),
                        rs.getString("address"),
                        rs.getString("phone"),
                        rs.getString("email"),
                        rs.getString("role"),
                        rs.getString("status"),
                        rs.getInt("remaining_holiday"),
                        rs.getInt("permanent") == 1,
                        rs.getString("contract_start_date"),
                        rs.getString("contract_end_date")
                );
                s.setId(rs.getInt("id"));
                list.add(s);
            }
        });

        return list;
    }

    /* ===================== WRITE METHODS ===================== */

    @Override
    public void updateAddStaff(Staff s) {
        String sql = "INSERT INTO staff "
                + "(name, surname, fiscal_code, address, phone, email, role, status, "
                + " remaining_holiday, permanent, contract_start_date, contract_end_date) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        PersistenceManager.executeUpdate(sql,
                s.getName(), s.getSurname(), s.getFiscalCode(),
                s.getAddress(), s.getPhone(), s.getEmail(),
                s.getRole(), s.getStatus(),
                s.getRemainingHoliday(), s.getPermanent() ? 1 : 0,
                s.getContractStartDate(), s.getContractEndDate()
        );

        // recupera l'ID generato
        int id = PersistenceManager.getLastId();
        s.setId(id);
    }

    @Override
    public void updateStaff(Staff s) {
        String sql = "UPDATE staff SET address = ?, phone = ?, email = ?, role = ?, status = ?, "
                + "remaining_holiday = ?, permanent = ?, contract_start_date = ?, contract_end_date = ? "
                + "WHERE id = ?";

        PersistenceManager.executeUpdate(sql,
                s.getAddress(), s.getPhone(), s.getEmail(), s.getRole(), s.getStatus(),
                s.getRemainingHoliday(), s.getPermanent() ? 1 : 0,
                s.getContractStartDate(), s.getContractEndDate(),
                s.getId()
        );
    }

    @Override
    public void updateDeleteStaff(Staff s) {
        PersistenceManager.executeUpdate("DELETE FROM staff WHERE id = ?", s.getId());
    }

    /* ====== (li aggiungeremo negli step successivi) ======
       - updateHolidays(...)
       - updateFeedback(...)
       - updateTeamStaff(...)
       - updateRecruitmentProposal(...)
       - helper findTeamByStaff(...) / removeFromTeam(...)
       ===================================================== */
}
