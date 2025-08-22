package catering.businesslogic.staff;

public class Staff {
    private int id;
    private String name;
    private String surname;
    private String fiscalCode;
    private String address;
    private String phone;
    private String email;
    private String role;
    private String status = "FREE";
    private int remainingHoliday = 20;
    private boolean permanent;
    private String contractStartDate;
    private String contractEndDate;


    public void addDetails(String n, String s, String fc, String a, String pn, String e,
                           String r, String st, int rh, boolean ft, String csd, String ced) {
        this.name = n; this.surname = s; this.fiscalCode = fc; this.address = a;
        this.phone = pn; this.email = e; this.role = r;
        if (st != null) this.status = st;
        this.remainingHoliday = rh; this.permanent = ft;
        this.contractStartDate = csd; this.contractEndDate = ced;
    }

    // getters/setters
    public int getId() { return id; }             public void setId(int id) { this.id = id; }
    public String getName() { return name; }      public String getSurname() { return surname; }
    public String getFiscalCode() { return fiscalCode; }
    public String getAddress() { return address; } public void setAddress(String address) { this.address = address; }
    public String getPhone() { return phone; }    public void setPhoneNumber(Integer pn) { this.phone = pn == null?null:String.valueOf(pn); }
    public String getEmail() { return email; }    public void setEmail(String email) { this.email = email; }
    public String getRole() { return role; }      public void setRole(String role) { this.role = role; }
    public String getStatus() { return status; }  public void setWorking() { this.status = "WORKING"; } public void setFree() { this.status = "FREE"; }
    public int getRemainingHoliday() { return remainingHoliday; } public void setRemainingHoliday(int rh) { this.remainingHoliday = rh; }
    public boolean getPermanent() { return permanent; } public void setPermanent(boolean permanent) { this.permanent = permanent; }
    public String getContractStartDate() { return contractStartDate; }
    public String getContractEndDate() { return contractEndDate; }
}
