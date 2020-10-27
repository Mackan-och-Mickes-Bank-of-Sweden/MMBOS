package MMBOS;

public class Accounts {
    public long accountNumber;
    public String personalID;
    public double cashInAccount;

    public Accounts(long accountNumber, String personalID, double cashInAccount) {
        this.accountNumber = accountNumber;
        this.personalID = personalID;
        this.cashInAccount = cashInAccount;
    }

    public double getCash(){
        return cashInAccount;
    }

    public double depositCash(double cash){
        this.cashInAccount = this.cashInAccount + cash;
        return this.cashInAccount;
    }

    public double withdrawlCash(double cash){
        this.cashInAccount = this.cashInAccount - cash;
        return this.cashInAccount;
    }
}
