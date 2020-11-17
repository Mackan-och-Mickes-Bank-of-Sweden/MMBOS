package MMBOS;

public class Payments {

    public String fromAccount, toAccount, moneyAmount, day, message;

    public Payments(String fromAccount, String toAccount, String moneyAmount, String day, String message){

        this.fromAccount = fromAccount;
        this.toAccount = toAccount;
        this.moneyAmount = moneyAmount;
        this.day = day;
        this.message = message;
    }
}
