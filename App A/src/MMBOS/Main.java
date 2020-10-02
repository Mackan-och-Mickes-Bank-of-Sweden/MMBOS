/*
    Projektarbete - Program A
    Kurs: OP1
    Klass: SYSJG4
    Elever: Marcus Richardson, Michael Hejl

    Programmering av Michael

 */

package MMBOS;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import com.google.gson.GsonBuilder;

public class Main {
// Globala variabler
    public static String menuFormater = "%-10s %-25s\n";
    public static Scanner keyBoard = new Scanner(System.in);
    public static File customersFile = new File("src/MMBOS/files/customers.cus");
    public static File accountsFile = new File("src/MMBOS/files/accounts.acc");
    public static File pendingPaymentsFile = new File("src/MMBOS/files/pendingpayments.pay");
    public static File nextAccountNumber = new File("src/MMBOS/files/nextaccountnumber.acc");
    public static int nextAccountNumb;
    public static String newAccountNumber;
    public static ArrayList <Customers> customersList = new ArrayList<>();
    public static ArrayList <Accounts> accountsList = new ArrayList<>();
    public static ArrayList<Block> blockchain = new ArrayList<Block>(); //BlockChain...
    public static int numOfZerosInHash = 3;

    public static void main(String[] args) {

        checkForFiles();
        fetchCustomers();
        fetchAccounts();

        while (true) {
            String headMenuChoice = printHeadMenu();
            if (headMenuChoice.equals("0")) break;

// TODO: Skapa kund
            if (headMenuChoice.equals("1")) {

                blockchain.add(new Block("20201;20202;15000", "0"));
                System.out.println("Hash av block 1 ... ");
                blockchain.get(0).mineBlock(numOfZerosInHash);
                blockchain.add(new Block("20204;20205;20000",blockchain.get(blockchain.size()-1).hash));
                System.out.println("Hash av block 2 ... ");
                blockchain.get(1).mineBlock(numOfZerosInHash);
                blockchain.add(new Block("20280;20201;10000",blockchain.get(blockchain.size()-1).hash));
                System.out.println("Hash av block 3 ... ");
                blockchain.get(2).mineBlock(numOfZerosInHash);

                System.out.println("\nBlockchain är giltig: " + isChainValid());

                String blockchainJson = new GsonBuilder().setPrettyPrinting().create().toJson(blockchain);
                System.out.println("\nThe block chain: ");
                System.out.println(blockchainJson);

                System.out.println("Tryck <enter> för att återgå till huvudmenyn.");
                String keyPress = keyBoard.nextLine();
            }

// Menyval 1 : Skapa nytt konto
            if (headMenuChoice.equals("2")) {
                createNewAccount();
                if(saveAccountsToFile()) {
                    System.out.println("Kontonummer: " + newAccountNumber + " skapades utan problem.");
                }
                fetchAccounts();
                updNextAccountNumberFile();

                System.out.println("Tryck <enter> för att återgå till huvudmenyn.");
                String keyPress = keyBoard.nextLine();
            }

// Menyval 2 : Lista alla kunder
            if (headMenuChoice.equals("3")) {
                String customerFormat = "%-15s %-25s\n";
                System.out.println("\nListar alla bankens kunder");
                System.out.format(customerFormat, "Personnummer", "Namn");
                for (int i=0; i<customersList.size(); i++) {
                    System.out.format(customerFormat, customersList.get(i).getPersonalID(), customersList.get(i).firstName + " " + customersList.get(i).lastName);
                }
                System.out.println("Tryck <enter> för att återgå till huvudmenyn.");
                String keyPress = keyBoard.nextLine();
            }

// Menyval 3 : Lista alla konton
            if (headMenuChoice.equals("4")) {
                String accountFormat = "%-15s %10.2f\n";
                System.out.println("\nListar alla konton på banken");
                System.out.format("%-15s %10s\n", "Kontonummer", "Belopp");
                for (int i=0; i<accountsList.size(); i++) {
                    System.out.format(accountFormat, String.valueOf(accountsList.get(i).accountNumber).substring(0,4) +" "+ String.valueOf(accountsList.get(i).accountNumber).substring(4,6)+" "+String.valueOf(accountsList.get(i).accountNumber).substring(6), accountsList.get(i).cashInAccount);
                }
                System.out.println("Tryck <enter> för att återgå till huvudmenyn.");
                String keyPress = keyBoard.nextLine();
            }

// TODO: Insättningar
            if (headMenuChoice.equals("5")) {
                updNextAccountNumberFile();
                System.out.println("Tryck <enter> för att återgå till huvudmenyn.");
                String keyPress = keyBoard.nextLine();
            }

// TODO: Uttag
            if (headMenuChoice.equals("6")) {

                System.out.println("Tryck <enter> för att återgå till huvudmenyn.");
                String keyPress = keyBoard.nextLine();
            }

// TODO: Betalningsuppdrag
            if (headMenuChoice.equals("7")) {

                System.out.println("Tryck <enter> för att återgå till huvudmenyn.");
                String keyPress = keyBoard.nextLine();
            }

// TODO: Banktillgångar
            if (headMenuChoice.equals("8")) {

                System.out.println("Tryck <enter> för att återgå till huvudmenyn.");
                String keyPress = keyBoard.nextLine();
            }

// TODO: Överföringar
            if (headMenuChoice.equals("9")) {

                System.out.println("Tryck <enter> för att återgå till huvudmenyn.");
                String keyPress = keyBoard.nextLine();
            }

        }
    }

    private static void createNewAccount() {
        Random randomizer = new Random();
        String randomAccount = "";
        for (int i = 0; i < 5; i++){
            randomAccount = randomAccount + String.valueOf(randomizer.nextInt(10));
        }
        newAccountNumber = nextAccountNumb + randomAccount;
        Accounts addAccount = new Accounts(Long.parseLong(newAccountNumber), "8007035531", 0);
        accountsList.add(addAccount);

    }

    private static boolean saveAccountsToFile() {
        try {
            FileWriter writeToFile = new FileWriter(accountsFile);
            for (int i=0; i < accountsList.size(); i++) {
                writeToFile.write(accountsList.get(i).accountNumber + ";" + accountsList.get(i).personalID + ";" + accountsList.get(i).cashInAccount + "\n");
            }
            writeToFile.close();
            return true;
        }
        catch (Exception e) {
            System.out.println("Problem vid skapandet av konto");
        }
        return false;
    }

    private static void updNextAccountNumberFile() {
        nextAccountNumb++;
        try {
            FileWriter writeToFile = new FileWriter(nextAccountNumber);
            writeToFile.write(String.valueOf(nextAccountNumb));
            writeToFile.close();
        } catch (Exception e) {
            System.out.print("Kunde inte uppdatera filen med nästa kontonummer.");
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
            accountsList.clear();
            while (accountsFileReader.hasNextLine()) {
                String rowsFromFile = accountsFileReader.nextLine();
                String[] readerParts = rowsFromFile.split(";");
                Accounts readAccount = new Accounts(Long.parseLong(readerParts[0]), readerParts[1], Double.parseDouble(readerParts[2]));
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
                nextAccountNumb = Integer.parseInt(fileReader.nextLine());
            } else {
                nextAccountNumb = 140337; // Första unika kontonumret.
            }
        } catch (Exception e) {
            System.out.print(e.getMessage() +"\n" + e.getStackTrace());
            return;
        }
    }

    public static Boolean isChainValid() {
        Block currentBlock;
        Block previousBlock;
        String hashTarget = new String(new char[numOfZerosInHash]).replace('\0', '0');

        //loop through blockchain to check hashes:
        for(int i=1; i < blockchain.size(); i++) {
            currentBlock = blockchain.get(i);
            previousBlock = blockchain.get(i-1);
            //compare registered hash and calculated hash:
            if(!currentBlock.hash.equals(currentBlock.calculateHash()) ){
                System.out.println("Nuvarande hash är korrupt.");
                return false;
            }
            //compare previous hash and registered previous hash
            if(!previousBlock.hash.equals(currentBlock.previousHash) ) {
                System.out.println("Föregåene hash är korrupt.");
                return false;
            }
            //check if hash is solved
            if(!currentBlock.hash.substring( 0, numOfZerosInHash).equals(hashTarget)) {
                System.out.println("Detta block har inte hämtats.");
                return false;
            }
        }
        return true;
    }

}
