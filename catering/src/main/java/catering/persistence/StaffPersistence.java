package catering.persistence;

import catering.businesslogic.staff.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class StaffPersistence implements StaffEventReceiver {

    /* ============ opzionale: crea tabelle se mancano ============ */
    public void ensureSchema() {
        PersistenceManager.executeUpdate(
                "CREATE TABLE IF NOT EXISTS staff (" +
                        " id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        " name TEXT NOT NULL," +
                        " surname TEXT NOT NULL," +
                        " fiscal_code TEXT UNIQUE NOT NULL," +
                        " address TEXT, phone TEXT, email TEXT, role TEXT," +
                        " status TEXT DEFAULT 'FREE'," +
                        " remaining_holiday INTEGER DEFAULT 20," +
                        " permanent INTEGER DEFAULT 0," +
                        " contract_start_date TEXT, contract_end_date TEXT)"
        );
        PersistenceManager.executeUpdate(
                "CREATE TABLE IF NOT EXISTS holidays (" +
                        " id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        " staff_id INTEGER NOT NULL," +
                        " start_date TEXT NOT NULL," +
                        " end_date TEXT NOT NULL," +
                        " accepted INTEGER DEFAULT 0," +
                        " FOREIGN KEY(staff_id) REFERENCES staff(id))"
        );
        PersistenceManager.executeUpdate(
                "CREATE TABLE IF NOT EXISTS feedback (" +
                        " id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        " staff_id INTEGER NOT NULL," +
                        " description TEXT NOT NULL," +
                        " FOREIGN KEY(staff_id) REFERENCES staff(id))"
        );
        PersistenceManager.executeUpdate(
                "CREATE TABLE IF NOT EXISTS team (" +
                        " id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        " team_size INTEGER DEFAULT 0," +
                        " type TEXT)"
        );
        PersistenceManager.executeUpdate(
                "CREATE TABLE IF NOT EXISTS staff_team (" +
                        " staff_id INTEGER NOT NULL," +
                        " team_id INTEGER NOT NULL," +
                        " PRIMARY KEY (staff_id, team_id)," +
                        " FOREIGN KEY(staff_id) REFERENCES staff(id)," +
                        " FOREIGN KEY(team_id) REFERENCES team(id))"
        );
        PersistenceManager.executeUpdate(
                "CREATE INDEX IF NOT EXISTS idx_staff_cf ON staff(fiscal_code)"
        );
        PersistenceManager.executeUpdate(
                "CREATE INDEX IF NOT EXISTS idx_holidays_staff_dates ON holidays(staff_id, start_date, end_date)"
        );

        PersistenceManager.executeUpdate(
                "CREATE TABLE IF NOT EXISTS recruitment_proposal (" +
                        " id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        " staff_id INTEGER NOT NULL," +
                        " accepted INTEGER DEFAULT 0," +
                        " FOREIGN KEY(staff_id) REFERENCES staff(id))"
        );


    }

    /* ======================== LOAD ======================== */

    public ArrayList<Staff> loadAllStaff() {
        final ArrayList<Staff> list = new ArrayList<>();
        String sql = "SELECT id, name, surname, fiscal_code, address, phone, email, role, status, " +
                "remaining_holiday, permanent, contract_start_date, contract_end_date " +
                "FROM staff ORDER BY surname, name";

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

    public Team findTeamByStaff(Staff s) {
        final Team[] holder = { null };
        String sql = "SELECT t.id, t.team_size, t.type " +
                "FROM team t JOIN staff_team st ON st.team_id = t.id " +
                "WHERE st.staff_id = ? LIMIT 1";

        PersistenceManager.executeQuery(sql, new ResultHandler() {
            @Override
            public void handle(ResultSet rs) throws SQLException {
                Team t = new Team();
                t.setId(rs.getInt("id"));
                t.setType(rs.getString("type"));
                holder[0] = t;
            }
        }, s.getId());

        return holder[0];
    }

    /* ======================== WRITE (event receiver) ======================== */

    @Override
    public void updateAddStaff(Staff s) {
        String sql = "INSERT INTO staff " +
                "(name, surname, fiscal_code, address, phone, email, role, status, " +
                " remaining_holiday, permanent, contract_start_date, contract_end_date) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        PersistenceManager.executeUpdate(sql,
                s.getName(), s.getSurname(), s.getFiscalCode(),
                s.getAddress(), s.getPhone(),
                s.getEmail(), s.getRole(), s.getStatus(),
                s.getRemainingHoliday(), s.getPermanent() ? 1 : 0,
                s.getContractStartDate(), s.getContractEndDate()
        );
        s.setId(PersistenceManager.getLastId());
    }

    @Override
    public void updateStaff(Staff s) {
        String sql = "UPDATE staff SET address = ?, phone = ?, email = ?, role = ?, status = ?, " +
                "remaining_holiday = ?, permanent = ?, contract_start_date = ?, contract_end_date = ? " +
                "WHERE id = ?";

        PersistenceManager.executeUpdate(sql,
                s.getAddress(), s.getPhone(), s.getEmail(), s.getRole(), s.getStatus(),
                s.getRemainingHoliday(), s.getPermanent() ? 1 : 0,
                s.getContractStartDate(), s.getContractEndDate(),
                s.getId()
        );
    }

    @Override
    public void updateDeleteStaff(Staff s) {
        // cascata manuale
        PersistenceManager.executeUpdate("DELETE FROM staff_team WHERE staff_id = ?", s.getId());
        PersistenceManager.executeUpdate("DELETE FROM feedback   WHERE staff_id = ?", s.getId());
        PersistenceManager.executeUpdate("DELETE FROM holidays   WHERE staff_id = ?", s.getId());
        PersistenceManager.executeUpdate("DELETE FROM staff      WHERE id = ?", s.getId());
    }

    /* ===== HOLIDAYS ===== */

    @Override
    public void updateHolidays(Staff staff, Holidays h) {
        if (h.getId() == 0) {
            String ins = "INSERT INTO holidays (staff_id, start_date, end_date, accepted) VALUES (?,?,?,?)";
            PersistenceManager.executeUpdate(ins,
                    staff.getId(),
                    h.getStartDate().toString(),
                    h.getEndDate().toString(),
                    Boolean.TRUE.equals(h.getAccepted()) ? 1 : 0
            );
            h.setId(PersistenceManager.getLastId());
        } else {
            String upd = "UPDATE holidays SET start_date=?, end_date=?, accepted=? WHERE id=?";
            PersistenceManager.executeUpdate(upd,
                    h.getStartDate().toString(),
                    h.getEndDate().toString(),
                    Boolean.TRUE.equals(h.getAccepted()) ? 1 : 0,
                    h.getId()
            );
        }
    }

    public boolean existsHolidayOverlap(Staff s, String startIso, String endIso) {
        final boolean[] found = { false };
        String sql = "SELECT 1 FROM holidays " +
                "WHERE staff_id=? AND NOT (end_date < ? OR start_date > ?) LIMIT 1";
        PersistenceManager.executeQuery(sql, rs -> found[0] = true, s.getId(), startIso, endIso);
        return found[0];
    }

    public void decrementHolidays(Staff s, int days) {
        int newVal = Math.max(0, s.getRemainingHoliday() - days);
        PersistenceManager.executeUpdate("UPDATE staff SET remaining_holiday=? WHERE id=?", newVal, s.getId());
        s.setRemainingHoliday(newVal);
    }

    public void setStatus(Staff s, String status) {
        PersistenceManager.executeUpdate("UPDATE staff SET status=? WHERE id=?", status, s.getId());
        s.setStatus(status);
    }

    /* ===== FEEDBACK ===== */

    @Override
    public void updateFeedback(Staff s, Feedback f) {
        String sql = "INSERT INTO feedback (staff_id, description) VALUES (?, ?)";
        PersistenceManager.executeUpdate(sql, s.getId(), f.getDescription());
        f.setId(PersistenceManager.getLastId());
    }

    /* ===== TEAM MEMBERSHIP ===== */

    @Override
    public void updateTeamStaff(Staff s, Team t) {
        final boolean[] exists = { false };
        String check = "SELECT 1 FROM staff_team WHERE staff_id=? AND team_id=? LIMIT 1";
        PersistenceManager.executeQuery(check, rs -> exists[0] = true, s.getId(), t.getId());
        if (!exists[0]) {
            PersistenceManager.executeUpdate("INSERT INTO staff_team (staff_id, team_id) VALUES (?, ?)",
                    s.getId(), t.getId());
        }
    }

    public void removeFromTeam(Staff s, Team t) {
        PersistenceManager.executeUpdate("DELETE FROM staff_team WHERE staff_id=? AND team_id=?", s.getId(), t.getId());
    }

    @Override
    public void updateRecruitmentProposal(Staff s, RecruitmentProposal p) {
        if (p.getId() == 0) {
            PersistenceManager.executeUpdate(
                    "INSERT INTO recruitment_proposal (staff_id, accepted) VALUES (?, ?)",
                    s.getId(), Boolean.TRUE.equals(p.getAccepted()) ? 1 : 0
            );
            p.setId(PersistenceManager.getLastId());
        } else {
            PersistenceManager.executeUpdate(
                    "UPDATE recruitment_proposal SET accepted=? WHERE id=?",
                    Boolean.TRUE.equals(p.getAccepted()) ? 1 : 0, p.getId()
            );
        }
    }

}
