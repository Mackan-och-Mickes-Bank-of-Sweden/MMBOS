package MMBOS;

public class Pending {
    public String fromAccount, toAccount, transferMessage, transferDate;
    public double transferAmount;

    public Pending(String fromAccount, String toAccount, double transferAmount,String transferDate, String transferMessage) {
        this.fromAccount = fromAccount;
        this.toAccount = toAccount;
        this.transferAmount = transferAmount;
        this.transferMessage = transferMessage;
        this.transferDate = transferDate;
    }
}