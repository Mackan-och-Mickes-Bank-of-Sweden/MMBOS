package MMBOS;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
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
    public static ArrayList <Customers> customersList = new ArrayList<>();
    public static ArrayList <Accounts> accountsList = new ArrayList<>();


    public static void main(String[] args) {

        checkForFiles();
        fetchCustomers();
        fetchAccounts();

        while (true) {
            String headMenuChoise = printHeadMenu();
            if (headMenuChoise.equals("0")) break;

// TODO: Skapa kund
            if (headMenuChoise.equals("1")) {

                //Block genesisBlock = new Block("20201;20202;15000", "0");
                //Block secondBlock = new Block("Yo im the second block",genesisBlock.hash);
            }

// TODO: Skapa konto
            if (headMenuChoise.equals("2")) {

            }

// TODO: Lista kunder
            if (headMenuChoise.equals("3")) {
                String customerFormat = "%-15s %-25s\n";
                System.out.println("\nListar alla bankens kunder");
                System.out.format(customerFormat, "Personnummer", "Namn");
                for (int i=0; i<customersList.size(); i++) {
                    System.out.format(customerFormat, customersList.get(i).getPersonalID(), customersList.get(i).firstName + " " + customersList.get(i).lastName);
                }
                System.out.println("Tryck <enter> för att gå tillbaka till huvudmenyn igen");
                String keyPress = keyBoard.nextLine();
            }

// TODO: Lista konton
            if (headMenuChoise.equals("4")) {
                String accountFormat = "%-10s %10.2f\n";
                System.out.println("\nListar alla konton på banken");
                System.out.format("%-10s %10s\n", "Konto", "Belopp");
                for (int i=0; i<accountsList.size(); i++) {
                    System.out.format(accountFormat, accountsList.get(i).accountNumber, accountsList.get(i).cashInAccount);
                }
                System.out.println("Tryck <enter> för att gå tillbaka till huvudmenyn igen");
                String keyPress = keyBoard.nextLine();
            }

// TODO: Insättningar
            if (headMenuChoise.equals("5")) {

            }

// TODO: Uttag
            if (headMenuChoise.equals("6")) {

            }

// TODO: Betalningsuppdrag
            if (headMenuChoise.equals("7")) {

            }

// TODO: Banktillgångar
            if (headMenuChoise.equals("8")) {

            }

// TODO: Överföringar
            if (headMenuChoise.equals("9")) {

            }

        }
    }

    private static String printHeadMenu() {
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
        return headMenuChoise;
    }

    private static void fetchAccounts() {
        try {
            Scanner accountsFileReader = new Scanner(accountsFile);
            while (accountsFileReader.hasNextLine()) {
                String rowsFromFile = accountsFileReader.nextLine();
                String[] readerParts = rowsFromFile.split(";");
                Accounts readAccount = new Accounts(Integer.parseInt(readerParts[0]), readerParts[1], Double.parseDouble(readerParts[2]));
                accountsList.add(readAccount);
            }
        } catch (Exception e) {
            System.out.print(e.getMessage() +"\n" + e.getStackTrace());
            return;
        }
    }

    private static void fetchCustomers() {
        try {
            Scanner customerFileReader = new Scanner(customersFile);
            while (customerFileReader.hasNextLine()) {
                String rowsFromFile = customerFileReader.nextLine();
                String[] readerParts = rowsFromFile.split(";");
                Customers readCustomer = new Customers(readerParts[0], readerParts[1], readerParts[2], readerParts[3]);
                customersList.add(readCustomer);
            }
        } catch (Exception e) {
            System.out.print(e.getMessage() +"\n" + e.getStackTrace());
            return;
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
            System.out.print(e.getMessage() +"\n" + e.getStackTrace());
            return;
        }
    }
}
