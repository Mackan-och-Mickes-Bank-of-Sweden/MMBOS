package MMBOS;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Main {
// Globala variabler
    public static String menuFormater = "%-10s %-25s\n";
    public static Scanner keyBoard = new Scanner(System.in);
    public static File customersFile = new File("src/MMBOS/files/customers.cus");
    public static File accountsFile = new File("src/MMBOS/files/accounts.acc");
    public static File pendingPaymentsFile = new File("src/MMBOS/files/pendingpayments.pay");
    public static File nextAccountNumber = new File("src/MMBOS/files/nextaccountnumber.acc");
    public static int nextAccountNumb;

    public static void main(String[] args) {

        checkForFiles();

// Skapar huvudmenyn
        System.out.println("       M & M Bank of Sweden       ");
        System.out.format("%34s\n","----------------------------------");
        System.out.format(menuFormater,"[1]", "Skapa kund");
        System.out.format(menuFormater,"[2]", "Skapa konto");
        System.out.format(menuFormater,"[3]", "Lista alla kunder");
        System.out.format(menuFormater,"[4]", "Lista alla konton");
        System.out.format(menuFormater,"[5]", "Insättningar");
        System.out.format(menuFormater,"[6]", "Uttag");
        System.out.format(menuFormater,"[7]", "Betalningsuppdrag (OCR)");
        System.out.format(menuFormater,"[8]", "Banktillgångar");
        System.out.format(menuFormater,"[9]", "Överföringar");
        System.out.format("%34s\n","----------------------------------");
        System.out.format(menuFormater,"[0]", "Avsluta programmet");
        System.out.print("\nMenyval: ");
        String headMenuChoise = keyBoard.nextLine();

        while (true) {
            if (headMenuChoise.equals("0")) break;

// Skapa nytt konto
            if (headMenuChoise.equals("1")) {

            }

        }
    }

    private static void checkForFiles() {
        try {
            if (!customersFile.exists()) {
                customersFile.createNewFile();
            }
            if (!accountsFile.exists()) {
                accountsFile.createNewFile();
            }
            if (!pendingPaymentsFile.exists()) {
                pendingPaymentsFile.createNewFile();
            }
            if (!nextAccountNumber.exists()) {
                nextAccountNumber.createNewFile();
            }
            Scanner fileReader = new Scanner(nextAccountNumber); // Unikt kontonummer.
            if (fileReader.hasNextLine()) {
                nextAccountNumb = fileReader.nextInt();
            } else {
                nextAccountNumb = 20201; // Första unika kontonumret.
            }
        } catch (Exception e) {
            System.out.println("Problem vid skapandet av nödvändiga filer.");
        }
    }
}
