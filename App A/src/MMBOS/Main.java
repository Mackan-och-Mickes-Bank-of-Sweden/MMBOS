package MMBOS;

public class Main {
    public static String menuFormater = "%-10s %-25s\n";
    public static void main(String[] args) {
	// Skapa huvudmeny
        System.out.println("       M & M Bank of Sweden       ");
        System.out.format("%34s\n","----------------------------------");
        System.out.format(menuFormater,"[1]", "Skapa konto");
        System.out.format(menuFormater,"[2]", "Lista alla konton");
        System.out.format(menuFormater,"[3]", "Insättningar");
        System.out.format(menuFormater,"[4]", "Uttag");
        System.out.format(menuFormater,"[5]", "Betalningsuppdrag (OCR)");
        System.out.format(menuFormater,"[6]", "Banktillgångar");
        System.out.format(menuFormater,"[7]", "Överföringar");
        System.out.format("%34s\n","----------------------------------");
        System.out.format(menuFormater,"[0]", "Avsluta programmet");
    }
}
