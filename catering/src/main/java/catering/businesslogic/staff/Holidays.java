package catering.businesslogic.staff;

import catering.persistence.PersistenceManager;
import catering.persistence.ResultHandler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class Holidays {
    private int id;
    private Staff staff;
    private boolean accepted;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    public Holidays() { }

    public Holidays(Staff staff, LocalDateTime startDate, LocalDateTime endDate) {
        this.staff = staff;
        this.startDate = startDate;
        this.endDate = endDate;
        this.accepted = false;
    }


    public void approveRequest() { this.accepted = true; }
    public void denyRequest() { this.accepted = false; }


    public boolean save() {
        if (id != 0) return false;
        String sql = "INSERT INTO holidays (staff_id,start_date,end_date,accepted) VALUES (?,?,?,?)";
        PersistenceManager.executeUpdate(sql, staff.getId(), startDate.toString(), endDate.toString(), accepted ? 1 : 0);
        id = PersistenceManager.getLastId();
        return id > 0;
    }

    public boolean update() {
        if (id == 0) return false;
        String sql = "UPDATE holidays SET start_date=?, end_date=?, accepted=? WHERE id=?";
        int rows = PersistenceManager.executeUpdate(sql, startDate.toString(), endDate.toString(), accepted ? 1 : 0, id);
        return rows > 0;
    }

    public boolean delete() {
        if (id == 0) return false;
        int rows = PersistenceManager.executeUpdate("DELETE FROM holidays WHERE id=?", id);
        if (rows > 0) id = 0;
        return rows > 0;
    }

    // ===== static loaders (utile per test/GUI) =====
    public static ArrayList<Holidays> loadByStaff(Staff s) {
        ArrayList<Holidays> list = new ArrayList<>();
        String sql = "SELECT * FROM holidays WHERE staff_id = ?";
        PersistenceManager.executeQuery(sql, new ResultHandler() {
            @Override
            public void handle(ResultSet rs) throws SQLException {
                Holidays h = new Holidays();
                h.id = rs.getInt("id");
                h.staff = s;
                h.startDate = LocalDateTime.parse(rs.getString("start_date"));
                h.endDate = LocalDateTime.parse(rs.getString("end_date"));
                h.accepted = rs.getInt("accepted") == 1;
                list.add(h);
            }
        }, s.getId());
        return list;
    }

    // ===== getters/setters =====
    public int getId() { return id; } public void setId(int id) { this.id = id; }
    public Staff getStaff() { return staff; }
    public LocalDateTime getStartDate() { return startDate; }
    public LocalDateTime getEndDate() { return endDate; }
    public Boolean getAccepted() { return accepted; }
}
