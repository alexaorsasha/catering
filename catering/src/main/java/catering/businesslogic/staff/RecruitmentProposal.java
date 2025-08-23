package catering.businesslogic.staff;

import catering.persistence.PersistenceManager;

public class RecruitmentProposal {
    private int id;
    private Staff staff;
    private boolean accepted;

    public RecruitmentProposal() { }

    public RecruitmentProposal(Staff staff) {
        this.staff = staff;
        this.accepted = false;
    }

    public void approveProposal() { this.accepted = true; }

    public void denyProposal() { this.accepted = false; }

    public boolean save() {
        if (id != 0) return false;
        String sql = "INSERT INTO recruitment_proposal (staff_id, accepted) VALUES (?, ?)";
        PersistenceManager.executeUpdate(sql, staff.getId(), accepted ? 1 : 0);
        id = PersistenceManager.getLastId();
        return id > 0;
    }

    // ===== Getters =====
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public Staff getStaff() { return staff; }
    public Boolean getAccepted() { return accepted; }
}
