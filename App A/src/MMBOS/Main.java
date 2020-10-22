/*
    Projektarbete - Program A
    Kurs: OP1
    Klass: SYSJG4
    Elever: Marcus Richardson, Michael Hejl

    Programmering av Michael

 */

/*
 *      transfers.log           från konto; till konto; belopp; meddelande/ocr
 *      hashtory.log            kontrollhash av alla överföringar
 *      pendingpayments.pay     från konto; till konto; belopp; datum; meddelande/ocr
 *
 */

package MMBOS;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import com.google.gson.GsonBuilder;

public class Main {
// Globala variabler
    public static String menuFormater = "%-10s %-25s\n";
    public static Scanner keyBoard = new Scanner(System.in);
    public static File customersFile = new File("../files/customers.cus");
    public static File accountsFile = new File("../files/accounts.acc");
    public static File pendingPaymentsFile = new File("../files/pendingpayments.pay");
    public static File nextAccountNumber = new File("../files/nextaccountnumber.acc");
    public static File transferLogFile = new File("../logs/transfers.log");
    public static File hashtoryFile = new File("../logs/hashtory.log");
    public static int nextAccountNumb;
    public static String newAccountNumber;
    public static ArrayList <Customers> customersList = new ArrayList<>();
    public static ArrayList <Accounts> accountsList = new ArrayList<>();
    public static ArrayList<Block> blockchain = new ArrayList<>(); //BlockChain...
    public static int numOfZerosInHash = 3;
    public static ArrayList <BlockCheck> blockChecker = new ArrayList<>();

    public static void main(String[] args) throws FileNotFoundException, NoSuchAlgorithmException {

        checkForFiles();
        fetchCustomers();
        fetchAccounts();

        while (true) {
            String headMenuChoice = printHeadMenu();
            if (headMenuChoice.equals("0")) break;

// Menyval 1 : Skapa kund
            if (headMenuChoice.equals("1")) {
                System.out.println("Skapa ny kund");
                System.out.print("Ange personnummer: ");
                String inPersonnummer = keyBoard.nextLine();
                System.out.print("Ange förnamn: ");
                String inFirstName = keyBoard.nextLine();
                System.out.print("Ange efternamn: ");
                String inLastName = keyBoard.nextLine();
                String inPassword = String.valueOf(md5Pass(inPersonnummer.substring(8)));
                Customers newCustomer = new Customers(inPersonnummer, inFirstName, inLastName,inPassword);
                customersList.add(newCustomer);
                if (saveCustomersToFile()) {
                    System.out.println("Kunden skapades med lösenordet: " + inPersonnummer.substring(8));
                }
                System.out.println("Tryck <enter> för att återgå till huvudmenyn.");
                String keyPress = keyBoard.nextLine();
            }

// Menyval 2 : Skapa nytt konto
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

// Menyval 3 : Lista alla kunder
            if (headMenuChoice.equals("3")) {
                listCustomers();
                System.out.println("Tryck <enter> för att återgå till huvudmenyn.");
                String keyPress = keyBoard.nextLine();
            }

// Menyval 4 : Lista alla konton
            if (headMenuChoice.equals("4")) {
                String accountFormat = "%-15s %10.2f\n";
                System.out.println("\nListar alla konton på banken");
                System.out.format("%-15s %10s\n", "KONTO", "SALDO");
                for (int i=0; i<accountsList.size(); i++) {
                    System.out.format(accountFormat, String.valueOf(accountsList.get(i).accountNumber).substring(0,4) +" "+ String.valueOf(accountsList.get(i).accountNumber).substring(4,6)+" "+String.valueOf(accountsList.get(i).accountNumber).substring(6), accountsList.get(i).cashInAccount);
                }
                System.out.println("Tryck <enter> för att återgå till huvudmenyn.");
                String keyPress = keyBoard.nextLine();
            }

// TODO: Insättningar
            if (headMenuChoice.equals("5")) {

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

// Menyval 8 : Banktillgångar
            if (headMenuChoice.equals("8")) {
                double totalBankAmount = 0;
                for (int i=0; i<accountsList.size(); i++) {
                    totalBankAmount = totalBankAmount + accountsList.get(i).cashInAccount;
                }
                System.out.println("Bankens totala tillgångar: " + totalBankAmount + "kr");
                System.out.println("Tryck <enter> för att återgå till huvudmenyn.");
                String keyPress = keyBoard.nextLine();
            }

// TODO: Överföringar
            if (headMenuChoice.equals("9")) {

                System.out.println("Tryck <enter> för att återgå till huvudmenyn.");
                String keyPress = keyBoard.nextLine();
            }

            if (headMenuChoice.equals("h") || headMenuChoice.equals("H")) {
                checkTransferHash();
                //saveTransferData();
                System.out.println("Tryck <enter> för att återgå till huvudmenyn.");
                String keyPress = keyBoard.nextLine();
            }
        }
    }

    private static void checkTransferHash() throws FileNotFoundException {
        Scanner checkTransfersFile = new Scanner(transferLogFile);
        Scanner checkHashtoryFile = new Scanner(hashtoryFile);

        while (checkTransfersFile.hasNextLine()) {
            String rowsFromFile = checkTransfersFile.nextLine();
            String rowsFromHashtory = checkHashtoryFile.nextLine();
            String[] readerHashParts = rowsFromHashtory.split(";");
            blockChecker.add(new BlockCheck(rowsFromFile, readerHashParts[1], readerHashParts[2],readerHashParts[3]));
        }
        String blockchainJson = new GsonBuilder().setPrettyPrinting().create().toJson(blockChecker);
        if (isChainValid()) System.out.println("\nHashkontontrollen utförd, alla uppgifter stämmer!");
    }

    private static void saveTransferData() throws FileNotFoundException {  //Todo: Skapa transfer, spara i log med hash
        Scanner checkTransfersFile = new Scanner(transferLogFile);
        String previousHash;
        int i = 0;
        while (checkTransfersFile.hasNextLine()) {
            try {
                previousHash = blockchain.get(blockchain.size() - 1).hash;

            } catch (Exception e) {
                previousHash = "0";
            }
            String rowsFromFile = checkTransfersFile.nextLine();
            blockchain.add(new Block(rowsFromFile, previousHash));
            System.out.println("Hash av block "+i+" . . .");
            blockchain.get(i).mineBlock(numOfZerosInHash);
            i++;
        }
        System.out.println("\nBlockchain är giltig: " + isChainValid());

        String blockchainJson = new GsonBuilder().setPrettyPrinting().create().toJson(blockchain);
        System.out.println("\nThe block chain: ");
        System.out.println(blockchainJson);
    }

    private static void listCustomers() {
        String customerFormat = "%-5s %-20s %-25s\n";
        System.out.println("\nListar alla bankens kunder");
        System.out.format(customerFormat, "POS", "PERSONNUMMER", "KUND");
        for (int i=0; i<customersList.size(); i++) {
            System.out.format(customerFormat, i, customersList.get(i).getPersonalID().substring(0,8)+"-"+customersList.get(i).getPersonalID().substring(8), customersList.get(i).firstName + " " + customersList.get(i).lastName);
        }
    }

    private static void createNewAccount() {
        Random randomizer = new Random();
        String randomAccount = "";
        for (int i = 0; i < 5; i++){
            randomAccount = randomAccount + String.valueOf(randomizer.nextInt(10));
        }
        newAccountNumber = nextAccountNumb + randomAccount;
        listCustomers();
        // TODO: Lista med id och tilldela personnummer till nytt konto.
        System.out.print("\nAnge POS som kontot skall tilldelas: ");
        String customerID = keyBoard.nextLine();
        Accounts addAccount = new Accounts(Long.parseLong(newAccountNumber), customersList.get(Integer.parseInt(customerID)).getPersonalID(), 0);
        accountsList.add(addAccount);

    }

    private static boolean saveAccountsToFile() {
        try {
            FileWriter writeToFile = new FileWriter(accountsFile);
            for (int i=0; i < accountsList.size(); i++) {
                writeToFile.write(accountsList.get(i).accountNumber + ";" + accountsList.get(i).getPersonalID() + ";" + accountsList.get(i).cashInAccount + "\n");
            }
            writeToFile.close();
            return true;
        }
        catch (Exception e) {
            System.out.println("Problem vid skapandet av konto");
        }
        return false;
    }

    private static boolean saveCustomersToFile() {
        try {
            FileWriter writeToFile = new FileWriter(customersFile);
            for (int i=0; i < customersList.size(); i++) {
                writeToFile.write(customersList.get(i).getPersonalID() + ";" + customersList.get(i).firstName+ ";" + customersList.get(i).lastName + ";" + customersList.get(i).passWd + "\n");
            }
            writeToFile.close();
            return true;
        }
        catch (Exception e) {
            System.out.println("Problem vid skrivning till kundfil.");
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
        System.out.format(menuFormater,"[H]", "Hash kontroll");
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
                nextAccountNumb = 140337; // Första unika kontonummerserien.
            }
        } catch (Exception e) {
            System.out.print(e.getMessage() +"\n" + e.getStackTrace());
            return;
        }
    }

    public static Boolean isChainValid() {
        BlockCheck currentBlock;
        BlockCheck previousBlock;
        String hashTarget = new String(new char[numOfZerosInHash]).replace('\0', '0');
        int fail = 0;
        for(int i=1; i < blockChecker.size(); i++) {
            currentBlock = blockChecker.get(i);
            previousBlock = blockChecker.get(i-1);

            if(!currentBlock.hash.equals(currentBlock.calculateHashChecker()) ){
                System.out.println("\nDenna hash är korrupt.");
                System.out.println("Följande data finns registrerat för detta block:\n"+ blockChecker.get(i).getData());
                //return false;
                fail = 1;
            }
            if(!previousBlock.hash.equals(currentBlock.previousHash) ) {
                System.out.println("\nFöregåene hash är korrupt.");
                System.out.println("Följande data finns registrerat för detta block:\n"+ blockChecker.get(i-1).getData());
                //return false;
                fail = 1;
            }
//            if(!currentBlock.hash.substring( 0, numOfZerosInHash).equals(hashTarget)) {
//                System.out.println("Detta block har inte korrekt start på hash.");
//                //return false;
//                fail = 1;
//            }
        }
        if (fail==0) {
            return true;
        } else {
            return false;
        }
    }

    private static StringBuilder md5Pass(String text) throws NoSuchAlgorithmException {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        md5.update(text.getBytes());
        byte[] md5Password = md5.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : md5Password) {
            sb.append(String.format("%02x", b & 0xff));
        }
        return sb;
    }
}
