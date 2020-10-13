package MMBOS;

public class Accounts {
    public long accountNumber;
    private String personalID;
    public double cashInAccount;

    public Accounts(long accountNumber, String personalID, double cashInAccount) {
        this.accountNumber = accountNumber;
        this.personalID = personalID;
        this.cashInAccount = cashInAccount;
    }

    public String getPersonalID() {
        return personalID;
    }
}
