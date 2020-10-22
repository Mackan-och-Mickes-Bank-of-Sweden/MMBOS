package MMBOS;

public class Customers {
    public String firstName, lastName, passWd;
    private String personalID;

    /**
     * @author Michael
     * @param personalID
     * @param firstName
     * @param lastName
     * @param passWd
     */
    public Customers(String personalID, String firstName, String lastName, String passWd) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.passWd = passWd;
        this.personalID = personalID;
    }

    public String getPersonalID() {
        return personalID;
    }
}
