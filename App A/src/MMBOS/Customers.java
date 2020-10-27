package MMBOS;

public class Customers {
    public enum roleAtBank{
        Customer,
        Accountant,
        Administator,
        CEO
    }
    public String firstName, lastName, passWd;
    roleAtBank role;
    private String personalID;

    public Customers(String personalID, String firstName, String lastName, String passWd, String role) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.passWd = passWd;
        this.personalID = personalID;
        this.role = roleAtBank.valueOf(role);
    }

    public String getPersonalID() {
        return personalID;
    }
}
