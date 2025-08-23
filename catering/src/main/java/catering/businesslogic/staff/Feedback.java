package catering.businesslogic.staff;

public class Feedback {
    private int id;
    private Staff staff;
    private String description;

    public Feedback(Staff staff, String description) {
        this.staff = staff;
        this.description = description;
    }

    public int getId() { return id; }       public void setId(int id) { this.id = id; }
    public Staff getStaff() { return staff; }
    public String getDescription() { return description; }
}
