/*
    Projektarbete - Program A
    Kurs: OP1
    Klass: SYSJG4
    Elever: Marcus Richardson, Michael Hejl

    Programmering av Michael & Marcus

 */

package MMBOS;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;
import com.google.gson.GsonBuilder;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;

public class Main {
    // Globala variabler
    public static int bankID = -1;
    public static String menuFormater = "%-10s %-25s\n";
    public static Scanner keyBoard = new Scanner(System.in);
    public static File customersFile = new File("files/customers.cus");
    public static File accountsFile = new File("files/accounts.acc");
    public static File pendingPaymentsFile = new File("files/pendingpayments.pay");
    public static File nextAccountNumber = new File("files/nextaccountnumber.acc");
    public static File transferLogFile = new File("logs/transfers.log");
    public static File hashtoryFile = new File("logs/hashtory.log");
    public static int nextAccountNumb;
    public static String newAccountNumber;
    public static ArrayList<Payments> paymentsList = new ArrayList<>();
    public static ArrayList<Customers> customersList = new ArrayList<>();
    public static ArrayList<Accounts> accountsList = new ArrayList<>();
    public static int numOfZerosInHash = 3;
    public static ArrayList<BlockCheck> blockChecker = new ArrayList<>();
    public static final String role = "Customer";

    public static void main(String[] args) throws FileNotFoundException, NoSuchAlgorithmException, InterruptedException {

        checkForFiles();
        fetchCustomers();
        fetchAccounts();
        fetchPaymentOrders();
        headMenuChoices();

    }

    /**
     * check hash
     * @author Michael
     * @throws FileNotFoundException
     */
    private static void checkTransferHash() throws FileNotFoundException, InterruptedException {
        blockChecker.clear();
        Scanner checkTransfersFile = new Scanner(transferLogFile);
        String previousHash;
        int i = 0;
        while (checkTransfersFile.hasNextLine()) {
            try {
                previousHash = blockChecker.get(blockChecker.size() - 1).hash;

            } catch (Exception e) {
                previousHash = "0";
            }
            String rowsFromFile = checkTransfersFile.nextLine();
            blockChecker.add(new BlockCheck(rowsFromFile, previousHash));
            System.out.println("Hash av block " + i + " . . .");
            Thread.sleep(300);
            blockChecker.get(i).mineBlock(numOfZerosInHash);
            i++;
        }
        String blockchainJson = new GsonBuilder().setPrettyPrinting().create().toJson(blockChecker);
        System.out.println(blockchainJson);
        System.out.println("\nBlockchain är giltig: " + isChainValid());
    }

    /**
     * fetch previous hash from log and check against transfer log
     * add new transfers to log file, hash to log file and perform hash check
     * @author Michael
     * @param fromAccount
     * @param toAccount
     * @param transferAmount
     * @param transferMessage
     * @throws FileNotFoundException
     */
    private static void logTransfer(String fromAccount, String toAccount, double transferAmount, String transferMessage) throws FileNotFoundException {
        Scanner checkTransfersFile = new Scanner(transferLogFile);
        Scanner checkHashtoryFile = new Scanner(hashtoryFile);
        blockChecker.clear();
        String dataString = fromAccount+";"+toAccount+";"+transferAmount+";"+transferMessage;
        String previousHash = "";
        int i = 0;
        while (checkTransfersFile.hasNextLine()) {
            String rowsFromFile = checkTransfersFile.nextLine();
            String rowsFromHashtory = checkHashtoryFile.nextLine();
            String[] readerHashParts = rowsFromHashtory.split(";");
            blockChecker.add(new BlockCheck(rowsFromFile, readerHashParts[1], Long.parseLong(readerHashParts[2]), Integer.parseInt(readerHashParts[3])));
            previousHash = readerHashParts[0];
            i++;
        }
        blockChecker.add(new BlockCheck(dataString, previousHash));
        blockChecker.get(i).mineBlock(numOfZerosInHash);
        try {
            Files.write(Paths.get(String.valueOf(hashtoryFile)), (blockChecker.get(i).hash+";"+blockChecker.get(i).previousHash+";"+blockChecker.get(i).settimeStamp+";"+blockChecker.get(i).setNonce+"\n").getBytes(), StandardOpenOption.APPEND);
            Files.write(Paths.get(String.valueOf(transferLogFile)), (dataString+"\n").getBytes(), StandardOpenOption.APPEND);
        }
        catch (Exception e) {
            System.out.println("Problem vid skrivning till log filer");
        }
    }

    /**
     * lists customers.
     * @author Michael
     */
    private static void listCustomers() {
        String customerFormat = "%-5s %-20s %-25s\n";
        System.out.println("\nListar alla bankens kunder");
        System.out.format(customerFormat, "POS", "PERSONNUMMER", "KUND");
        for (int i = 0; i < customersList.size(); i++) {
            System.out.format(customerFormat, i, customersList.get(i).getPersonalID().substring(0, 8) + "-" + customersList.get(i).getPersonalID().substring(8), customersList.get(i).firstName + " " + customersList.get(i).lastName);
        }
    }

    /**
     * creates new account.
     * @author Michael
     */
    private static boolean createNewAccount() {

        listCustomers();
        System.out.print("\nAnge POS som kontot skall tilldelas: ");
        String customerID = keyBoard.nextLine();
        boolean term;
        term = checkInput(customerID);

        if (!term && Integer.parseInt(customerID) < customersList.size()) {
            Random randomizer = new Random();
            String randomAccount = "";
            for (int i = 0; i < 5; i++) {
                randomAccount = randomAccount + String.valueOf(randomizer.nextInt(10));
            }
            newAccountNumber = nextAccountNumb + randomAccount;

            Accounts addAccount = new Accounts(Long.parseLong(newAccountNumber), customersList.get(Integer.parseInt(customerID)).getPersonalID(), 0);
            accountsList.add(addAccount);
            return true;
        } else {
            System.out.println("Felaktig inmatning.");
        }
        return false;
    }

    /**
     * Writes accounts to file.
     * @author Michael
     * @return
     */
    private static boolean saveAccountsToFile() {
        try {
            FileWriter writeToFile = new FileWriter(accountsFile);
            for (int i = 0; i < accountsList.size(); i++) {
                writeToFile.write(accountsList.get(i).accountNumber + ";" + accountsList.get(i).personalID + ";" + accountsList.get(i).cashInAccount + "\n");
            }
            writeToFile.close();
            return true;
        } catch (Exception e) {
            System.out.println("Problem vid skapandet av konto");
        }
        return false;
    }

    /**
     * Writes Payorders to file.
     * @author Marcus
     */
    private static void savePaymentOrdersFile() {

        try {
            FileWriter myWriter = new FileWriter(pendingPaymentsFile);
            for (int i = 0; i < paymentsList.size(); i++) {
                Payments p = paymentsList.get(i);
                myWriter.write(p.fromAccount + ";" + p.toAccount + ";" + p.moneyAmount + ";"
                        + p.day +
                        "\n");
            }
            myWriter.close();
        } catch (Exception e) {
            System.out.println("Problem vid inskrivning av paymentorders.");
        }
    }

    /**
     * Writes next account number to file.
     * @author Michael
     */
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

    /**
     * Headmenu choice method, directs the user from the choice of printHeadMenu.
     * @author Marcus & Michael
     * @throws NoSuchAlgorithmException
     * @throws FileNotFoundException
     * @throws InterruptedException
     */
    private static void headMenuChoices() throws NoSuchAlgorithmException, FileNotFoundException, InterruptedException {

        while (true) {
            String headMenuChoice = printHeadMenu();
// Menyval 0 : Avslutar program
            if (headMenuChoice.equals("0")) {
                System.out.println("Programmet avslutas. . .");
                break;
            }

// Menyval 1 : Logga ut
            if (headMenuChoice.equals("1")) {
                logout();
            }

// Menyval 2 : Skapa ny kund
            if (headMenuChoice.equals("2")) {
                createNewCustomer();
                System.out.println("Tryck <enter> för att återgå till huvudmenyn.");
                String keyPress = keyBoard.nextLine();
            }

// Menyval 3 : Skapa nytt konto
            if (headMenuChoice.equals("3")) {
                if (createNewAccount() && saveAccountsToFile()) {
                    System.out.println("Kontonummer: " + newAccountNumber + " skapades utan problem.");
                    fetchAccounts();
                    updNextAccountNumberFile();
                }
                System.out.println("Tryck <enter> för att återgå till huvudmenyn.");
                String keyPress = keyBoard.nextLine();
            }

// Menyval 4 : Lista alla kunder
            if (headMenuChoice.equals("4")) {
                listCustomers();
                System.out.println("Tryck <enter> för att återgå till huvudmenyn.");
                String keyPress = keyBoard.nextLine();
            }

// Menyval 5 : Lista alla konton
            if (headMenuChoice.equals("5")) {
                listAccounts();
                System.out.println("Tryck <enter> för att återgå till huvudmenyn.");
                String keyPress = keyBoard.nextLine();
            }

// Menyval 6 : Sätt in pengar
            if (headMenuChoice.equals("6")) {
                depositCash();
                System.out.println("Tryck <enter> för att återgå till huvudmenyn.");
                String keyPress = keyBoard.nextLine();
            }

// Menyval 7 : Ta ut pengar
            if (headMenuChoice.equals("7")) {
                withdrawalCash();
                System.out.println("Tryck <enter> för att återgå till huvudmenyn.");
                String keyPress = keyBoard.nextLine();
            }

// Menyval 8 : Betalningsuppdrag
            if (headMenuChoice.equals("8")) {
                paymentOrders();
                System.out.println("Tryck <enter> för att återgå till huvudmenyn.");
                String keyPress = keyBoard.nextLine();
            }

// Menyval 9 : Visa bankvalv
            if (headMenuChoice.equals("9")) {
                showBankVault();
                System.out.println("Tryck <enter> för att återgå till huvudmenyn.");
                String keyPress = keyBoard.nextLine();
            }

// Menyval 10 : Överför pengar mellan konton
            if (headMenuChoice.equals("10")) {
                transferCash();
                System.out.println("Tryck <enter> för att återgå till huvudmenyn.");
                String keyPress = keyBoard.nextLine();
            }
// Menyval 11 :
            if (headMenuChoice.equals("11")) {
                checkTransferHash();
                System.out.println("Tryck <enter> för att återgå till huvudmenyn.");
                String keyPress = keyBoard.nextLine();
            }
// Menyval 12 : Logga in
            if (headMenuChoice.equals("12")) {
                login();
            }
// Menyval 13 : Ta bort betalningsuppdrag
            if (headMenuChoice.equals("13")) {
                removePaymentOrder();
                System.out.println("Tryck <enter> för att återgå till huvudmenyn.");
                String keyPress = keyBoard.nextLine();
            }
//Menyval 14 : Accountnat enum insättning.
            if (headMenuChoice.equals("14")) {
                accountantDeposit();
                System.out.println("Tryck <enter> för att återgå till huvudmenyn.");
                String keyPress = keyBoard.nextLine();

            }
//Menyval 15 : Accountant enum betalningsuppdrag.
            if (headMenuChoice.equals("15")) {
                accountantPaymentorders();
                System.out.println("Tryck <enter> för att återgå till huvudmenyn.");
                String keyPress = keyBoard.nextLine();
            }
//Menyval 16 : Används vid felaktik inmatning
            if (headMenuChoice.equals("16")) {
                System.out.println("Felaktig inmatning.");
            }
//Menyval 17 : Används vid customer create account
            if(headMenuChoice.equals("17")){
                customerCreateAcc();
                if(saveAccountsToFile()){
                    System.out.println("Kontonummer: " + newAccountNumber + " skapades utan problem.");
                    fetchAccounts();
                    updNextAccountNumberFile();
                }
                System.out.println("Tryck <enter> för att återgå till huvudmenyn.");
                String keyPress = keyBoard.nextLine();
            }
        }
    }

    /**
     * printHeadMenu prints diffrent types of menues depending on your enum. Also depends on bankID to be above or equal to zero(0).
     * Calls method headMenuCheck.
     * @author Marcus
     * @return variable headmenuChoice with an integer.
     */
    private static String printHeadMenu() throws FileNotFoundException{

        String headMenuChoise;
        if (bankID < 0) {
            System.out.println("       M & M Bank of Sweden       ");
            System.out.format("%34s\n", "----------------------------------");
            System.out.format(menuFormater, "[1]", "logga in");
            System.out.format("%34s\n", "----------------------------------");
            System.out.format(menuFormater, "[0]", "Avsluta programmet");
            System.out.print("\nMenyval: ");
            headMenuChoise = headMenuCheck();
            if (Integer.parseInt(headMenuChoise) > 1) {
                headMenuChoise = "16";
            }
            if (Integer.parseInt(headMenuChoise) == 1) {
                headMenuChoise = "12";
            }

        } else {
            if (customersList.get(bankID).role.equals(Customers.roleAtBank.CEO)) {

                System.out.println("       M & M Bank of Sweden       ");
                System.out.format("%34s\n", "----------------------------------");
                System.out.format(menuFormater, "[1]", "logga ut");
                System.out.format(menuFormater, "[2]", "Skapa kund");
                System.out.format(menuFormater, "[3]", "Skapa konto");
                System.out.format(menuFormater, "[4]", "Lista alla kunder");
                System.out.format(menuFormater, "[5]", "Lista alla konton");
                System.out.format(menuFormater, "[6]", "Insättningar");
                System.out.format(menuFormater, "[7]", "Uttag");
                System.out.format(menuFormater, "[8]", "Betalningsuppdrag (OCR)");
                System.out.format(menuFormater, "[9]", "Ta bort betalningsuppdrag");
                System.out.format(menuFormater, "[10]", "Överföringar");
                System.out.format(menuFormater, "[11]", "Banktillgångar");
                System.out.format("%34s\n", "----------------------------------");
                System.out.format(menuFormater, "[12]", "Hashkontroll");
                System.out.format(menuFormater, "[0]", "Avsluta programmet");
                System.out.print("\nMenyval: ");
                headMenuChoise = headMenuCheck();
                if (Integer.parseInt(headMenuChoise) > 12) {
                    headMenuChoise = "16";
                } else if (Integer.parseInt(headMenuChoise) == 11) {
                    headMenuChoise = "9";
                } else if (Integer.parseInt(headMenuChoise) == 9) {
                    headMenuChoise = "13";
                } else if (Integer.parseInt(headMenuChoise) == 12) {
                    headMenuChoise = "11";
                }

            } else if (customersList.get(bankID).role.equals(Customers.roleAtBank.Administator)) {

                System.out.println("       M & M Bank of Sweden       ");
                System.out.format("%34s\n", "----------------------------------");
                System.out.format(menuFormater, "[1]", "Logga ut");
                System.out.format(menuFormater, "[2]", "Lista alla kunder");
                System.out.format(menuFormater, "[3]", "Lista alla konton");
                System.out.format("%34s\n", "----------------------------------");
                System.out.format(menuFormater, "[0]", "Avsluta programmet");
                System.out.print("\nMenyval: ");
                headMenuChoise = headMenuCheck();
                if (Integer.parseInt(headMenuChoise) > 3) {
                    headMenuChoise = "16";
                } else if (Integer.parseInt(headMenuChoise) == 2) {
                    headMenuChoise = "4";
                } else if (Integer.parseInt(headMenuChoise) == 3) {
                    headMenuChoise = "5";
                }

            } else if (customersList.get(bankID).role.equals(Customers.roleAtBank.Accountant)) {

                System.out.println("       M & M Bank of Sweden       ");
                System.out.format("%34s\n", "----------------------------------");
                System.out.format(menuFormater, "[1]", "logga ut");
                System.out.format(menuFormater, "[2]", "Skapa kund");
                System.out.format(menuFormater, "[3]", "Skapa konto");
                System.out.format(menuFormater, "[4]", "Lista alla kunder");
                System.out.format(menuFormater, "[5]", "Lista alla konton");
                System.out.format(menuFormater, "[6]", "Insättningar");
                System.out.format(menuFormater, "[7]", "Betalningsuppdrag (OCR)");
                System.out.format(menuFormater, "[8]", "Ta bort betalningsuppdrag");
                System.out.format("%34s\n", "----------------------------------");
                System.out.format(menuFormater, "[0]", "Avsluta programmet");
                System.out.print("\nMenyval: ");
                headMenuChoise = headMenuCheck();
                if (Integer.parseInt(headMenuChoise) > 8) {
                    headMenuChoise = "16";
                } else if (Integer.parseInt(headMenuChoise) == 6) {
                    accountantDeposit();
                    headMenuChoise = "14";
                } else if (Integer.parseInt(headMenuChoise) == 7) {
                    accountantPaymentorders();
                    headMenuChoise = "15";
                } else if (Integer.parseInt(headMenuChoise) == 8) {
                    headMenuChoise = "13";
                }

            } else if (customersList.get(bankID).role.equals(Customers.roleAtBank.Customer)) {

                System.out.println("       M & M Bank of Sweden       ");
                System.out.format("%34s\n", "----------------------------------");
                System.out.format(menuFormater, "[1]", "logga ut");
                System.out.format(menuFormater, "[2]", "Skapa konto");
                System.out.format(menuFormater, "[3]", "Insättningar");
                System.out.format(menuFormater, "[4]", "Uttag");
                System.out.format(menuFormater, "[5]", "Betalningsuppdrag (OCR)");
                System.out.format(menuFormater, "[6]", "Överföringar");
                System.out.format("%34s\n", "----------------------------------");
                System.out.format(menuFormater, "[0]", "Avsluta programmet");
                System.out.print("\nMenyval: ");
                headMenuChoise = headMenuCheck();
                if (Integer.parseInt(headMenuChoise) > 6) {
                    headMenuChoise = "16";
                } else if (Integer.parseInt(headMenuChoise) == 2) {
                    headMenuChoise = "17";
                } else if (Integer.parseInt(headMenuChoise) == 3) {
                    headMenuChoise = "6";
                } else if (Integer.parseInt(headMenuChoise) == 4) {
                    headMenuChoise = "7";
                } else if (Integer.parseInt(headMenuChoise) == 5) {
                    headMenuChoise = "8";
                } else if (Integer.parseInt(headMenuChoise) == 6) {
                    headMenuChoise = "10";
                }

            } else {
                headMenuChoise = "16";
            }
        }
        return headMenuChoise;
    }

    /**
     * Headmenu check method, calls method checkInput.
     * Checks if the input is an integer and above or equal to zero(0), continues until it is.
     * @author Marcus
     * @return menu with a valid integer.
     */
    private static String headMenuCheck() {
        String menu;
        boolean term;
        do {
            menu = keyBoard.nextLine();
            term = checkInput(menu);
            if (term){
            System.out.println("Felaktig inmatning. . .");
            System.out.print("Menyval: ");
            }
        } while (term);
        return menu;
    }

    /**
     * fetch all accounts from csv
     * @author Michael
     */
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
            System.out.print(e.getMessage() + "\n" + e.getStackTrace());
            return;
        }
    }

    /**
     * fetch all customers from csv
     * @author Michael
     */
    private static void fetchCustomers() {
        try {
            Scanner customerFileReader = new Scanner(customersFile);
            while (customerFileReader.hasNextLine()) {
                String rowsFromFile = customerFileReader.nextLine();
                String[] readerParts = rowsFromFile.split(";");
                Customers readCustomer = new Customers(readerParts[0], readerParts[1], readerParts[2], readerParts[3], readerParts[4]);
                customersList.add(readCustomer);
            }
        } catch (Exception e) {
            System.out.print(e.getMessage() + "\n" + e.getStackTrace());
            return;
        }
    }

    /**
     * fetch all payments from csv
     * @author Marcus
     */
    private static void fetchPaymentOrders() {
        try {
            Scanner paymentOrderReader = new Scanner(pendingPaymentsFile);
            while (paymentOrderReader.hasNextLine()) {
                String rowsFromFile = paymentOrderReader.nextLine();
                String[] readerparts = rowsFromFile.split(";");
                Payments payments = new Payments(readerparts[0], readerparts[1], readerparts[2], readerparts[3], readerparts[4]);
                paymentsList.add(payments);
            }
            paymentOrderReader.close();
        } catch (Exception e) {
            System.out.print(e.getMessage() + "\n" + Arrays.toString(e.getStackTrace()));
        }
    }

    /**
     * Check if files exist.
     * @author Michael
     */
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
            System.out.print(e.getMessage() + "\n" + e.getStackTrace());
            return;
        }
    }

    /**
     * Checks if blockhain is valid.
     * @author Michael
     * @return
     */
    public static Boolean isChainValid() {
        BlockCheck currentBlock;
        BlockCheck previousBlock;
        String hashTarget = new String(new char[numOfZerosInHash]).replace('\0', '0');
        for (int i = 1; i < blockChecker.size(); i++) {
            currentBlock = blockChecker.get(i);
            previousBlock = blockChecker.get(i - 1);
            if (!currentBlock.hash.equals(currentBlock.calculateHashChecker())) {
                System.out.println("Nuvarande hash är korrupt.");
                return false;
            }
            if (!previousBlock.hash.equals(currentBlock.previousHash)) {
                System.out.println("Föregåene hash är korrupt.");
                return false;
            }
            if (!currentBlock.hash.substring(0, numOfZerosInHash).equals(hashTarget)) {
                System.out.println("Detta block har inte hämtats.");
                return false;
            }
        }
        return true;
    }

    /**
     * List customers with their accounts to their name.
     * @author Marcus
     */
    private static void listAccounts() {

        String accountFormat = "%-15s %10.2f %-15s %-20s\n";
        System.out.println("\nListar alla konton på banken");
        System.out.format("%-15s %10s %-15s %-20s\n", "KONTO", "SALDO", "    FÖRNAMN", "EFTERNAMN");
        for (int j = 0; j < customersList.size(); j++) {
            for (int i = 0; i < accountsList.size(); i++) {
                if (customersList.get(j).getPersonalID().equals(accountsList.get(i).personalID)) {
                    System.out.format(accountFormat, String.valueOf(accountsList.get(i).accountNumber).substring(0, 4)
                                    + " " + String.valueOf(accountsList.get(i).accountNumber).substring(4, 6) +
                                    " " + String.valueOf(accountsList.get(i).accountNumber).substring(6),
                            accountsList.get(i).cashInAccount, "    " + customersList.get(j).firstName,
                            customersList.get(j).lastName);
                }
            }
        }
    }

    /**
     * Create new customer, calls methods checkID, checkMessage & md5Pass
     * @author Marcus
     * @throws NoSuchAlgorithmException
     */
    private static void createNewCustomer() throws NoSuchAlgorithmException {
        boolean checkInput;
        System.out.print("Skriv in förnamn: ");
        String firstname = keyBoard.nextLine();
        checkInput = checkMessage(firstname);
        if(checkInput){
            return;
        }
        System.out.print("Skriv in efternamn: ");
        String lastname = keyBoard.nextLine();
        checkInput = checkMessage(lastname);
        if(checkInput){
            return;
        }
        System.out.print("Skriv in personnummer(12 siffror): ");
        String personID = keyBoard.nextLine();
        System.out.print("Skriv in nytt lösenord: ");
        String password1 = keyBoard.nextLine();
        checkInput = checkMessage(password1);
        if(checkInput){
            return;
        }
        System.out.print("Skriv in nytt lösenord igen: ");
        String password2 = keyBoard.nextLine();

        boolean term;
        if (password1.equals(password2) && personID.length() == 12) {
            try {
                term = checkID(personID);
            } catch (Exception e) {
                System.out.println("Något gick fel med personnummer...");
                term = false;
            }

            if (term) {
                Customers newCustomer = new Customers(personID, firstname, lastname, String.valueOf(md5Pass(password1)), role);
                customersList.add(newCustomer);
            }
        } else {
            System.out.println("Ogiltigt personnummer/Ogiltligt lösenord.");
        }
    }

    /**
     * CheckID, checks if the ID of a new customer is a valid ID.
     * @author Marcus
     * @param personID, ID of new customer.
     * @return true or false, depending on if it's a valid ID.
     */
    private static boolean checkID(String personID) {

        String birthyear = personID.substring(2, 11);
        char[] controllnr = personID.toCharArray();
        char[] birthyeararray = birthyear.toCharArray();
        int sum = 0;

        for (int i = 0; i < birthyear.length(); i++) {

            if ((i % 2) == 0) {
                String valueofbirth = String.valueOf(birthyeararray[i]);
                int value = Integer.parseInt(valueofbirth) * 2;

                if (value >= 10) {
                    value = (value - 10) + 1;
                }
                sum = sum + value;

            } else {
                String oddnumber = String.valueOf(birthyeararray[i]);
                int uneven = Integer.parseInt(oddnumber);
                sum = sum + uneven;
            }
        }

        int modulus = ((10 - (sum % 10)) % 10);

        if (Integer.toString(modulus).equals(String.valueOf(controllnr[11]))) {
            System.out.println("Skapdae nytt kund konto!");
            return true;
        } else {
            System.out.println("Ogiltlig kontrollsiffra...");
            return false;
        }
    }

    /**
     * Login method, calls md5Pass method.
     * Global variable bankID controlls who is signed in to the program. Fetches the value from the array index.
     * @author Marcus
     * @throws NoSuchAlgorithmException
     */
    public static void login() throws NoSuchAlgorithmException {

        System.out.println("Logga in till ditt konto");

        System.out.print("Skriv in personnummer: ");
        String birthID = keyBoard.nextLine();
        System.out.print("Skriv in lösenord: ");
        String password = keyBoard.nextLine();

        boolean term = true;
        for (int i = 0; i < customersList.size(); i++) {
            if (customersList.get(i).getPersonalID().equals(birthID)) {
                if (customersList.get(i).passWd.equals(String.valueOf(md5Pass(password)))) {
                    System.out.println("Välkommen " + customersList.get(i).firstName + " " + customersList.get(i).lastName + "!");
                    bankID = i;
                    term = false;
                }
            }
        }
        if (term) {
            System.out.println("Fel personnummer och/eller Lösenord. . .");
        }
    }

    /**
     * Logout method, bankID is set to -1 since an arrayindex can't be minus.
     * @author Marcus
     */
    public static void logout() {
        System.out.println("Du loggas ut. . .");
        bankID = -1;
    }

    /**
     * Deposit cash method, calls methods, checkInput, saveAccountsToFile & LogTransfer
     * Variable inputCheck, is key variable to check if input is valid.
     * @author Marcus
     * @throws FileNotFoundException
     */
    private static void depositCash() throws FileNotFoundException {

        String format = "%-15s %15s %15s\n";
        String transferMessage = "INSÄTTNING";
        String fromAccount = "00000000000";
        String toAccount;
        double transferAmount;
        System.out.format((format), "ACCOUNT ID", "KONTONUMMER", "SALDO");

        for (int i = 0; i < accountsList.size(); i++) {
            if (accountsList.get(i).personalID.equals(customersList.get(bankID).getPersonalID())) {
                System.out.format((format), i, accountsList.get(i).accountNumber, accountsList.get(i).cashInAccount);
            }
        }
        System.out.print("Vilket konto vill du sätta in pengar på? ta hjälp utav \"Account ID\": ");
        String accID = keyBoard.nextLine();
        boolean inputCheck;
        inputCheck = checkInput(accID);

        if(inputCheck ||
                Integer.parseInt(accID) >= accountsList.size() ||
                !accountsList.get(Integer.parseInt(accID)).personalID.equals(customersList.get(bankID).getPersonalID())){
            System.out.println("Felaktig inmatning utav Account ID.");
            return;
        }
        System.out.print("Hur mycket pengar vill du sätta in? summa: ");
        String amount = keyBoard.nextLine();
        try {
            Double.parseDouble(amount);
            if(Double.parseDouble(amount) < 0){
                System.out.println("Felaktikt värde.");
                return;
            }
        } catch (Exception e) {
            System.out.println("Felaktikt värde.");
            return;
        }

        if (accountsList.get(Integer.parseInt(accID)).personalID.equals(customersList.get(bankID).getPersonalID())) {
            accountsList.get(Integer.parseInt(accID)).depositCash(Double.parseDouble(amount));
            toAccount = String.valueOf(accountsList.get(Integer.parseInt(accID)).accountNumber);
            transferAmount = Double.parseDouble(amount);
            System.out.println("Satte in " + amount + " till ditt konto!");
            saveAccountsToFile();
            logTransfer(fromAccount, toAccount, transferAmount, transferMessage);
        } else {
            System.out.println("Ogiltligt account ID");
        }
    }

    /**
     * Withdrawal cash method, calls methods checkInput, saveAccountsToFile & LogTransfer
     * Variable inputCheck, is key variable to check if input is valid.
     * @author Marcus
     * @throws FileNotFoundException
     */
    private static void withdrawalCash() throws FileNotFoundException {

        String format = "%-15s %15s %15s\n";
        String transferMessage = "UTTAG";
        String fromAccount;
        String toAccount = "00000000000";
        double transferAmount;
        System.out.format((format), "Account ID", "KONTONUMMER", "SALDO");

        for (int i = 0; i < accountsList.size(); i++) {
            if (accountsList.get(i).personalID.equals(customersList.get(bankID).getPersonalID())) {
                System.out.format((format), i, accountsList.get(i).accountNumber, accountsList.get(i).cashInAccount);
            }
        }

        System.out.print("Ifrån vilket konto vill du ta ut pengar ifrån? ta hjälp utav \"Account ID\": ");
        String accID = keyBoard.nextLine();
        boolean inputCheck;
        inputCheck = checkInput(accID);

        if(inputCheck ||
        Integer.parseInt(accID) >= accountsList.size() ||
        !accountsList.get(Integer.parseInt(accID)).personalID.equals(customersList.get(bankID).getPersonalID())){
            System.out.println("Felaktig inmatning utav Account ID.");
            return;
        }

        System.out.print("Hur mycket pengar vill du ta ut? Summa: "); 
        String amount = keyBoard.nextLine();
        try {
            Double.parseDouble(amount);
            if(Double.parseDouble(amount) < 0){
                System.out.println("Felaktikt värde.");
                return;
            }
        } catch (Exception e) {
            System.out.println("Felaktig inmatning på pengar.");
            return;
        }

        if (accountsList.get(Integer.parseInt(accID)).cashInAccount >= Double.parseDouble(amount)) {
            accountsList.get(Integer.parseInt(accID)).withdrawlCash(Double.parseDouble(amount));
            System.out.println("Tog ut " + amount + " ifrån ditt konto!");
            saveAccountsToFile();
            fromAccount = String.valueOf(accountsList.get(Integer.parseInt(accID)).accountNumber);
            transferAmount = Double.parseDouble(amount);
            logTransfer(fromAccount, toAccount, transferAmount, transferMessage);
        } else {
            System.out.println("Du har inte tillräckligt med pengar på ditt konto.");
        }
    }

    /**
     * Paymentorders method, calls methods checkInput & savePaymentOrderFiles.
     * Variable inputCheck, is key variable to check if input is valid.
     * @author Marcus
     */
    private static void paymentOrders() {

        String format = "%-15s %-15s %-15s %15s %20s \n";
        System.out.format((format), "ACCOUNT ID", "NAMN", "EFTERNAMN", "KONTONUMMER", "SALDO");

        for (int i = 0; i < customersList.size(); i++) {
            for (int j = 0; j < accountsList.size(); j++) {
                if (customersList.get(i).getPersonalID().equals(accountsList.get(j).personalID)) {
                    if (!customersList.get(i).getPersonalID().equals(customersList.get(bankID).getPersonalID())) {
                        System.out.format((format), j, customersList.get(i).firstName, customersList.get(i).lastName,
                                accountsList.get(j).accountNumber, accountsList.get(j).cashInAccount);
                    }
                }
            }
        }

        System.out.print("Till vem skall du lägga upp ett betalningsuppdrag till? ta hjälp utav \"Account ID\": ");
        String newAccID = keyBoard.nextLine();
        boolean inputCheck;
        inputCheck = checkInput(newAccID);
        if (inputCheck ||
                Integer.parseInt(newAccID) >= accountsList.size() ||
                accountsList.get(Integer.parseInt(newAccID)).personalID.equals(customersList.get(bankID).getPersonalID())) {
            System.out.println("Ogiltligt värde på Account ID.");
            return;
        }

        System.out.print("Hur mycket pengar skall du betala? Summa: ");
        String amount = keyBoard.nextLine();
        try {
            Double.parseDouble(amount);
            if (Double.parseDouble(amount) < 0) {
                System.out.println("Felaktikt värde.");
                return;
            }
        } catch (Exception e) {
            System.out.println("Felaktig inmatning på pengar.");
            return;
        }

        String format2 = "%-15s %-15s %-20s\n";
        System.out.format((format2), "ACCOUNT ID", "KONTONUMMER", "SALDO");
        for (int x = 0; x < accountsList.size(); x++) {
            if (accountsList.get(x).personalID.equals(customersList.get(bankID).getPersonalID())) {
                System.out.format((format2), x, accountsList.get(x).accountNumber, accountsList.get(x).cashInAccount);
            }
        }

        System.out.print("Vilket konto vill du dra pengarna ifrån? ta hjälp utav \"Account ID\": ");
        String myAccID = keyBoard.nextLine();
        inputCheck = checkInput(myAccID);

        if (inputCheck ||
                Integer.parseInt(myAccID) >= accountsList.size() ||
                !accountsList.get(Integer.parseInt(myAccID)).personalID.equals(customersList.get(bankID).getPersonalID()) ||
                accountsList.get(Integer.parseInt(newAccID)).personalID.equals(customersList.get(bankID).getPersonalID())) {
            System.out.println("Felaktig inmatning på Account ID.");
            return;
        }
        System.out.print("Medelande: ");
        String msg = keyBoard.nextLine();
        inputCheck = checkMessage(msg);
        if(inputCheck){
            return;
        }

        System.out.println("Vilken dag skall betalningen göras?\n" +
                "[1]. Snarast.\n" +
                "[2]. Välj datum.");
        String dateoption = keyBoard.nextLine();

        inputCheck = checkInput(dateoption);
        LocalDate day;
        day = LocalDate.now();
        day = day.plusDays(2);

        if(!inputCheck && Integer.parseInt(dateoption) == 2) {
            System.out.print("Ange giltligt datum (tex. 2007-12-03): ");
            String date = keyBoard.nextLine();
            try{
                day = LocalDate.parse(date);
            }catch(Exception e) {
                System.out.print(e.getMessage() + "\n" + Arrays.toString(e.getStackTrace()));
                return;
            }
        }

        System.out.println("Datum för överföringen har satts till: " + day);
        myAccID = String.valueOf(accountsList.get(Integer.parseInt(myAccID)).accountNumber);
        newAccID = String.valueOf(accountsList.get(Integer.parseInt(newAccID)).accountNumber);

        Payments newpay = new Payments(myAccID, newAccID, amount, String.valueOf(day), msg);
        paymentsList.add(newpay);
        savePaymentOrdersFile();
        System.out.println("Betalningsuppdrag lyckades!");
    }

    /**
     * Remove paymentorders method, calls methods checkInput & savePaymentOrderFiles
     * Variable inputCheck, is key variable to check if input is valid.
     * @author Marcus
     */
    private static void removePaymentOrder() {

        //Skriver ut betalningsuppdragen.
        String format = "%-10s %-15s %-15s %-20s %-15s \n";
        System.out.format((format), "Index", "FRÅN KONTO", "TILL KONTO", "SUMMA", "DATUM");

        for (int i = 0; i < paymentsList.size(); i++) {
            Payments p = paymentsList.get(i);
            System.out.format((format), i, p.fromAccount, p.toAccount, p.moneyAmount, p.day);
        }

        //Kollar så att man väljer ett giltligt index.
        String index;
        boolean term;
        do {
            System.out.print("Ta bort ett betalningsuppdrag, ta hjälp utav \"Index\": : ");
            index = keyBoard.nextLine();
            term = checkInput(index);
            if (term || Integer.parseInt(index) >= paymentsList.size()) {
                System.out.println("Välj ett giltligt index");
            }
        } while (term);

        //Hämtar namn och efternamn på personen betalningen gick till, och ifrån.
        String toName = null;
        String fromName = null;
        String toLastname = null;
        String fromLastName = null;

        for (int j = 0; j < accountsList.size(); j++) {
            if (String.valueOf(accountsList.get(j).accountNumber).equals(paymentsList.get(Integer.parseInt(index)).fromAccount)) {
                for (int y = 0; y < customersList.size(); y++) {
                    if (accountsList.get(j).personalID.equals(customersList.get(y).getPersonalID())) {
                        fromName = customersList.get(y).firstName;
                        fromLastName = customersList.get(y).lastName;
                    }
                }
            }
            if (String.valueOf(accountsList.get(j).accountNumber).equals(paymentsList.get(Integer.parseInt(index)).toAccount)) {
                for (int x = 0; x < customersList.size(); x++) {
                    if (accountsList.get(j).personalID.equals(customersList.get(x).getPersonalID())) {
                        toName = customersList.get(x).firstName;
                        toLastname = customersList.get(x).lastName;
                    }
                }
            }
        }

        System.out.println("Betalningsuppdrag [" + index + "] avslutat! Tog bort betalningsuppdrag:" +
                "\nIfrån: " + fromName + " " + fromLastName +
                "\nTill: " + toName + " " + toLastname +
                "\nSumma: " + Double.parseDouble(paymentsList.get(Integer.parseInt(index)).moneyAmount));
        paymentsList.remove(Integer.parseInt(index));
        savePaymentOrdersFile();
    }

    /**
     * Show bankvault method, adds up all the money in the bank.
     * @author Marcus
     */
    public static void showBankVault() {
        double money = 0;
        for (int i = 0; i < accountsList.size(); i++) {
            money = money + accountsList.get(i).getCash();
        }
        System.out.println("Pengar i banken för tillfället: " + money);
    }

    /**
     * Transfer cash method, calls methods saveAccountsToFile & LogTransfer
     * @author Marcus
     * @throws FileNotFoundException
     */
    private static void transferCash() throws FileNotFoundException {

        String format = "%-15s %15s %20s \n";
        String transferMessage = "EGEN ÖVERFÖRING";
        String fromAccount;
        String toAccount;
        double transferAmount;
        System.out.format((format), "Account ID", "KONTONUMMER", "SALDO");

        for (int i = 0; i < accountsList.size(); i++) {
            if (accountsList.get(i).personalID.equals(customersList.get(bankID).getPersonalID())) {
                System.out.format((format), i, accountsList.get(i).accountNumber, accountsList.get(i).cashInAccount);
            }
        }

        System.out.print("Vilket konto vill du överföra pengar ifrån? ta hjälp utav \"Account ID\": ");
        String fromAccIndex = keyBoard.nextLine();
        System.out.print("Hur mycket pengar vill du överföra? ");
        String amount = keyBoard.nextLine();
        System.out.print("Vilket konto vill du överföra till? ta hjälp utav \"Account ID\": ");
        String newAccIndex = keyBoard.nextLine();

        boolean term = true;
        try {
            Integer.parseInt(newAccIndex);
            Double.parseDouble(amount);
            Integer.parseInt(fromAccIndex);
            if (Integer.parseInt(newAccIndex) >= accountsList.size()
                    || Integer.parseInt(fromAccIndex) >= accountsList.size()
                    || Integer.parseInt(newAccIndex) < 0
                    || Integer.parseInt(fromAccIndex) < 0
                    || Double.parseDouble(amount) < 0) {
                term = false;
            }
        } catch (Exception e) {
            term = false;
        }

        if (term) {
            if (accountsList.get(Integer.parseInt(fromAccIndex)).personalID.equals(customersList.get(bankID).getPersonalID())
                    && accountsList.get(Integer.parseInt(newAccIndex)).personalID.equals(customersList.get(bankID).getPersonalID())) {

                if (accountsList.get(Integer.parseInt(fromAccIndex)).getCash() >= Double.parseDouble(amount)) {
                    accountsList.get(Integer.parseInt(fromAccIndex)).withdrawlCash(Double.parseDouble(amount));
                    accountsList.get(Integer.parseInt(newAccIndex)).depositCash(Double.parseDouble(amount));
                    saveAccountsToFile();

                    fromAccount = String.valueOf(accountsList.get(Integer.parseInt(fromAccIndex)).accountNumber);
                    toAccount = String.valueOf(accountsList.get(Integer.parseInt(newAccIndex)).accountNumber);
                    transferAmount = Double.parseDouble(amount);
                    logTransfer(fromAccount, toAccount, transferAmount, transferMessage);

                } else {
                    System.out.println("Du har inte tillräckligt med pengar på kontot!");
                }
            } else {
                System.out.println("Ogiltligt account ID.");
            }
        } else {
            System.out.println("Ogiltligt account ID/Felaktigt värde av pengar.");
        }
    }

    /**
     * Accountant deposit method, used by enum Accountant. Calls methods checkInput, saveAccountsToFile & depositCash from Accounts Class.
     * Variable inputCheck, is key variable to check if input is valid.
     * @author Marcus
     */
    private static void accountantDeposit() throws FileNotFoundException {

        System.out.println("[1]. Sök efter kund i lista." +
                "\n[2]. Sök efter kunds personnummer.");
        String choice = keyBoard.nextLine();
        String transferMessage = "INSÄTTNING KASSÖR";
        String fromAccount = "00000000000";
        String toAccount;
        double transferAmount;

        if (choice.equals("1")) {
            listCustomers();

            System.out.print("Ange kund som skall göra insättning med hjälp av \"POS\": ");
            String index = keyBoard.nextLine();

            boolean inputCheck;
            inputCheck = checkInput(index);
            if(inputCheck || customersList.size() <= Integer.parseInt(index)){
                System.out.println("Ange ett giltligt index nästa gång.");
                return;
            }

            System.out.print("Hur mycket pengar skall sättas in?");
            String amount = keyBoard.nextLine();
            try {
                Double.parseDouble(amount);
                if(Double.parseDouble(amount) < 0){
                    System.out.println("Felaktikt värde.");
                    return;
                }
            } catch (Exception e) {
                System.out.println("Ange en giltlig summa nästa gång.");
                return;
            }

            String accFormat = "%15s %15s %15s\n";
            System.out.format((accFormat), "INDEX", "KONTONUMMER", "SALDO");
            String ID = customersList.get(Integer.parseInt(index)).getPersonalID();
            for (int x = 0; x < accountsList.size(); x++) {
                if (ID.equals(accountsList.get(x).personalID)) {
                    System.out.format((accFormat), x, accountsList.get(x).accountNumber, accountsList.get(x).cashInAccount);
                }
            }

            System.out.print("Vilket konto skall pengarna sättas in på?");
            String acc = keyBoard.nextLine();
            inputCheck = checkInput(acc);

            if (inputCheck ||
                    customersList.size() <= Integer.parseInt(acc) ||
                    !customersList.get(Integer.parseInt(index)).getPersonalID().equals(accountsList.get(Integer.parseInt(acc)).personalID)) {
                System.out.println("Ange ett giltligt index nästa gång.");
                return;
            }
            accountsList.get(Integer.parseInt(acc)).depositCash(Double.parseDouble(amount));

            saveAccountsToFile();
            toAccount = String.valueOf(accountsList.get(Integer.parseInt(acc)).accountNumber);
            transferAmount = Double.parseDouble(amount);
            logTransfer(fromAccount, toAccount, transferAmount, transferMessage);

        } else if (choice.equals("2")) {
            System.out.print("Sök efter personnummer: ");
            String ID = keyBoard.nextLine();
            boolean term = false;
            for (int x = 0; x < customersList.size(); x++) {
                if (customersList.get(x).getPersonalID().equals(ID)) {
                    term = true;
                    break;
                }
            }

            if (term) {
                int IDindex = -1;
                String format = "%15s %15s %15s\n";
                System.out.format((format), "INDEX", "KONTONUMMER", "SALDO");
                for (int j = 0; j < customersList.size(); j++) {
                    if (ID.equals(customersList.get(j).getPersonalID())) {
                        IDindex = j;
                        for (int y = 0; y < accountsList.size(); y++) {
                            if (accountsList.get(y).personalID.equals(ID)) {
                                System.out.format((format), y, accountsList.get(y).accountNumber, accountsList.get(y).cashInAccount);
                            }
                        }
                    }
                }

                System.out.println("Hur mycket pengar skall sättas in?");
                String amount = keyBoard.nextLine();
                try {
                    Double.parseDouble(amount);
                    if(Double.parseDouble(amount) < 0){
                        System.out.println("Felaktikt värde.");
                        return;
                    }
                } catch (Exception e) {
                    System.out.println("Ange en giltlig summa nästa gång.");
                    return;
                }

                System.out.print("Vilket konto vill du göra insättningen på? ");
                String index = keyBoard.nextLine();
                boolean inputCheck;
                inputCheck = checkInput(index);

                if (inputCheck ||
                        Integer.parseInt(index) >= customersList.size() ||
                        !customersList.get(IDindex).getPersonalID().equals(accountsList.get(Integer.parseInt(index)).personalID)) {
                    System.out.println("Ange ett giltligt index nästa gång.");
                    return;
                }
                accountsList.get(Integer.parseInt(index)).depositCash(Double.parseDouble(amount));
                saveAccountsToFile();

                toAccount = String.valueOf(accountsList.get(Integer.parseInt(index)).accountNumber);
                transferAmount = Double.parseDouble(amount);
                logTransfer(fromAccount, toAccount, transferAmount, transferMessage);

            } else {
                System.out.println("Personnummer hittades ej.");
            }
        } else {
            System.out.println("Välj 1-2 i menyn nästa gång.");
        }
    }

    /**
     * Accountant paymentorder method, calls methods AccountantPaymentChoiceOne & Two.
     * @author Marcus
     */
    private static void accountantPaymentorders() {

        System.out.println("[1]. Sök efter kund i lista." +
                "\n[2]. Sök efter kunds personnummer.");
        String choice = keyBoard.nextLine();

        if (choice.equals("1")) {
            accountantPaymentChoiceOne();

        } else if (choice.equals("2")) {
            accountantPaymentChoiceTwo();
        } else {
            System.out.println("Välj i menyn 1-2 nästa gång.");
        }

    }

    /**
     * Method used to help customers pay their paymentorders by a list. calls methods checkInput & savePaymentOrderFiles.
     * Variable inputCheck, is key variable to check if input is valid.
     * @author Marcus
     */
    private static void accountantPaymentChoiceOne(){

        boolean inputCheck;
        listCustomers();
        System.out.print("Ange kund som skall göra OCR betalning med hjälp av \"POS\": ");
        String index = keyBoard.nextLine();
        inputCheck = checkInput(index);

        if (inputCheck || customersList.size() <= Integer.parseInt(index)) {
            System.out.println("Ange ett giltligt index nästa gång.");
            return;
        }

        System.out.print("Hur mycket pengar skall betalas in? summa: ");
        String amount = keyBoard.nextLine();
        try {
            Double.parseDouble(amount);
            if(Double.parseDouble(amount) < 0){
                System.out.println("Felaktikt värde.");
                return;
            }
        } catch (Exception e) {
            System.out.println("Ange en giltlig summa nästa gång.");
            return;
        }

        String accFormat = "%-8s %-15s %10s\n";
        System.out.format((accFormat), "INDEX", "KONTONUMMER", "SALDO");
        String ID = customersList.get(Integer.parseInt(index)).getPersonalID();
        for (int x = 0; x < accountsList.size(); x++) {
            if (ID.equals(accountsList.get(x).personalID)) {
                System.out.format((accFormat), x, accountsList.get(x).accountNumber, accountsList.get(x).cashInAccount);
            }
        }
        System.out.print("Vilket konto skall pengarna dras ifrån? ta hjälp ifrån \"Index\": ");
        String acc = keyBoard.nextLine();

        inputCheck = checkInput(acc);

        if (inputCheck ||
                Integer.parseInt(acc) >= accountsList.size() ||
                !customersList.get(Integer.parseInt(index)).getPersonalID()
                        .equals(accountsList.get(Integer.parseInt(acc)).personalID)) {
            System.out.println("Ange ett giltligt index nästa gång.");
            return;
        }

            String accountFormat = "%-8s %-15s %10.2f %-15s %-20s\n";
            System.out.format("%-8s %-15s %10s %-15s %-20s\n", "INDEX", "KONTO", "SALDO", "    FÖRNAMN", "EFTERNAMN");
            for (int j = 0; j < customersList.size(); j++) {
                for (int i = 0; i < accountsList.size(); i++) {
                    if (!customersList.get(j).getPersonalID().equals(customersList.get(Integer.parseInt(index)).getPersonalID())
                            && customersList.get(j).getPersonalID().equals(accountsList.get(i).personalID)) {
                        System.out.format((accountFormat), i, String.valueOf(accountsList.get(i).accountNumber).substring(0, 4)
                                        + " " + String.valueOf(accountsList.get(i).accountNumber).substring(4, 6) +
                                        " " + String.valueOf(accountsList.get(i).accountNumber).substring(6),
                                accountsList.get(i).cashInAccount, "    " + customersList.get(j).firstName,
                                customersList.get(j).lastName);
                    }
                }
            }
            System.out.print("Vem skall betalningen gå till? ta hjälp ifrån \"Index\": ");
            String whomAcc = keyBoard.nextLine();
            inputCheck = checkInput(whomAcc);
            if (inputCheck ||
                    Integer.parseInt(whomAcc) >= accountsList.size()
                    || accountsList.get(Integer.parseInt(acc)).personalID.equals(accountsList.get(Integer.parseInt(whomAcc)).personalID)) {
                System.out.println("Ange ett giltligt index nästa gång.");
                return;
            }

            System.out.print("Medelande: ");
            String msg = keyBoard.nextLine();
            inputCheck = checkMessage(msg);
            if(inputCheck){
                return;
            }

            System.out.println("Vilken dag skall betalningen göras?\n" +
                    "[1]. Snarast.\n" +
                    "[2]. Välj datum.");
            String dateoption = keyBoard.nextLine();

            inputCheck = checkInput(dateoption);
            LocalDate day;
            day = LocalDate.now();
            day = day.plusDays(2);

            if(!inputCheck && Integer.parseInt(dateoption) == 2) {
                System.out.print("Ange giltligt datum (tex. 2007-12-03): ");
                String date = keyBoard.nextLine();
                try{
                    day = LocalDate.parse(date);
                }catch(Exception e) {
                    System.out.print(e.getMessage() + "\n" + Arrays.toString(e.getStackTrace()));
                    return;
                }
            }
            System.out.println("Datum för överföringen har satts till: " + day);
            acc = String.valueOf(accountsList.get(Integer.parseInt(acc)).accountNumber);
            whomAcc = String.valueOf(accountsList.get(Integer.parseInt(whomAcc)).accountNumber);

            Payments newPay = new Payments(acc, whomAcc, amount, String.valueOf(day), msg);
            paymentsList.add(newPay);
            savePaymentOrdersFile();
    }

    /**
     * Method used to help cusomers pay paymentorders by personal ID. calls methods checkInput and savePaymentOrderFiles.
     * Variable inputCheck, is key variable to check if input is valid.
     * @author Marcus
     */
    private static void accountantPaymentChoiceTwo() {

        System.out.print("Sök efter personnummer: ");
        String ID = keyBoard.nextLine();
        boolean term = false;

        for (int x = 0; x < customersList.size(); x++) {
            if (customersList.get(x).getPersonalID().equals(ID)) {
                System.out.println("Konton ägda av kund: " + customersList.get(x).firstName + " " + customersList.get(x).lastName);
                term = true;
            }
        }

        if (term) {
            int IDindex = -1;
            String format = "%-8s %15s %15s\n";

            System.out.format((format), "INDEX", "KONTONUMMER", "SALDO");
            for (int j = 0; j < customersList.size(); j++) {
                if (ID.equals(customersList.get(j).getPersonalID())) {
                    IDindex = j;
                    for (int y = 0; y < accountsList.size(); y++) {
                        if (accountsList.get(y).personalID.equals(ID)) {
                            System.out.format((format), y, accountsList.get(y).accountNumber, accountsList.get(y).cashInAccount);
                        }
                    }
                }
            }

            boolean inputCheck;
            System.out.print("Ifrån vilket konto skall pengarna betalas? ta hjälp ifrån \"Index\": ");
            String acc = keyBoard.nextLine();
            inputCheck = checkInput(acc);

            if (inputCheck || Integer.parseInt(acc) >= accountsList.size() ||
                    !accountsList.get(Integer.parseInt(acc)).personalID.equals(customersList.get(IDindex).getPersonalID())) {
                System.out.println("Ange ett giltigt index nästa gång.");
                return;
            }

            System.out.print("Hur mycket pengar skall betalas? summa: ");
            String amount = keyBoard.nextLine();
            try {
                Double.parseDouble(amount);
                if(Double.parseDouble(amount) < 0){
                    System.out.println("Felaktikt värde.");
                    return;
                }
            } catch (Exception e) {
                System.out.println("Ange en giltlig summa nästa gång.");
            }

            String accountFormat = "%-8s %-15s %10.2f %-15s %-20s\n";
            System.out.format("%-8s %-15s %10s %-15s %-20s\n", "INDEX", "KONTO", "SALDO", "    FÖRNAMN", "EFTERNAMN");
            for (int j = 0; j < customersList.size(); j++) {
                for (int i = 0; i < accountsList.size(); i++) {
                    if (!customersList.get(j).getPersonalID().equals(customersList.get(IDindex).getPersonalID())
                            && customersList.get(j).getPersonalID().equals(accountsList.get(i).personalID)) {
                        System.out.format((accountFormat), i, String.valueOf(accountsList.get(i).accountNumber).substring(0, 4)
                                        + " " + String.valueOf(accountsList.get(i).accountNumber).substring(4, 6) +
                                        " " + String.valueOf(accountsList.get(i).accountNumber).substring(6),
                                accountsList.get(i).cashInAccount, "    " + customersList.get(j).firstName,
                                customersList.get(j).lastName);
                    }
                }
            }

            System.out.print("Vem skall betalningen gå till? ta hjälp ifrån \"Index\": ");
            String whomAcc = keyBoard.nextLine();
            inputCheck = checkInput(whomAcc);

            if (inputCheck || Integer.parseInt(whomAcc) >= accountsList.size()
                    || accountsList.get(Integer.parseInt(acc)).personalID.equals(accountsList.get(Integer.parseInt(whomAcc)).personalID)) {
                System.out.println("Ange ett giltligt index nästa gång.");
                return;
            }

            System.out.print("Medelande: ");
            String msg = keyBoard.nextLine();
            inputCheck = checkMessage(msg);
            if(inputCheck){
                return;
            }

            System.out.println("Vilken dag skall betalningen göras?\n" +
                    "[1]. Snarast.\n" +
                    "[2]. Välj datum.");
            String dateoption = keyBoard.nextLine();

            inputCheck = checkInput(dateoption);
            LocalDate day;
            day = LocalDate.now();
            day = day.plusDays(2);

            if (!inputCheck && Integer.parseInt(dateoption) == 2) {
                System.out.print("Ange giltligt datum (tex. 2007-12-03): ");
                String date = keyBoard.nextLine();
                try {
                    day = LocalDate.parse(date);
                } catch (Exception e) {
                    System.out.print(e.getMessage() + "\n" + Arrays.toString(e.getStackTrace()));
                    return;
                }
            }
            System.out.println("Datum för överföringen har satts till: " + day);


            acc = String.valueOf(accountsList.get(Integer.parseInt(acc)).accountNumber);
            whomAcc = String.valueOf(accountsList.get(Integer.parseInt(whomAcc)).accountNumber);
            Payments newPay = new Payments(acc, whomAcc, amount, String.valueOf(day), msg);
            paymentsList.add(newPay);
            savePaymentOrdersFile();
        } else {
            System.out.println("Personnummer hittades ej.");
        }
    }

    /**
     * Creates new account for customers.
     * @author Michael & Marcus
     */
    private static void customerCreateAcc() {

        Random randomizer = new Random();
        String randomAccount = "";
        for (int i = 0; i < 5; i++) {
            randomAccount = randomAccount + String.valueOf(randomizer.nextInt(10));
        }
        newAccountNumber = nextAccountNumb + randomAccount;

        Accounts addAccount = new Accounts(Long.parseLong(newAccountNumber), customersList.get(bankID).getPersonalID(), 0);
        accountsList.add(addAccount);

        fetchAccounts();
        updNextAccountNumberFile();
    }

    /**
     * Check input method. Checks if the input is an integer and above or equal to zero(0).
     * @author Marcus
     * @param input is the String input that needs to be checked if it can be converted to an integer.
     * @return
     */
    public static boolean checkInput(String input){
        boolean value = false;
        try{
            Integer.parseInt(input);
            if(Integer.parseInt(input) < 0){
                value = true;
            }
        }catch(Exception e){
            value = true;
        }
        return value;
    }

    /**
     * Checks if message for pending payments contains ";"
     * @author Marcus
     * @param msg is the input message.
     */
    public static boolean checkMessage(String msg){
        for(int i = 0; i < msg.length(); i++){
            if(String.valueOf(msg.charAt(i)).equals(";")){
                System.out.println("Otillåtet tecken.");
                return true;
            }
        }
        return false;
    }

    /**
     * Converts password to md5 code.
     * @author Michael
     * @param text is the users password before it gets converted.
     * @return
     * @throws NoSuchAlgorithmException
     */
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