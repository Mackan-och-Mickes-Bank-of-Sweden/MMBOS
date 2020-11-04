package MMBOS;

public class TransferMessages {
    public String fromAccount, toAccount, orgDate, newDate, transferMessage;
    public int messageID;
    public double transferAmount;

    public TransferMessages(String fromAccount, String toAccount, double transferAmount, String orgDate, String newDate, String transferMessage, int messageID) {
        this.fromAccount = fromAccount;
        this.toAccount = toAccount;
        this.transferAmount = transferAmount;
        this.orgDate = orgDate;
        this.newDate = newDate;
        this.transferMessage = transferMessage;
        this.messageID = messageID;
    }
}
