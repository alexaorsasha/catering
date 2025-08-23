package catering.businesslogic.staff;

import java.util.ArrayList;

public class Team {
    private int id;
    private int teamSize;
    private String type;
    private final ArrayList<Staff> components = new ArrayList<>();

    public static Team createTeam() { return new Team(); }
    public void removeTeam() { /* opzionale */ }

    public void addComponent(Staff staff) {
        if (!components.contains(staff)) {
            components.add(staff);
            teamSize = components.size();
        }
    }

    public void removeComponent(Staff staff) {
        components.remove(staff);
        teamSize = components.size();
    }

    public int getId() { return id; }            public void setId(int id) { this.id = id; }
    public int getTeamSize() { return teamSize; }
    public String getType() { return type; }     public void setType(String type) { this.type = type; }
    public ArrayList<Staff> getComponents() { return components; }
}
