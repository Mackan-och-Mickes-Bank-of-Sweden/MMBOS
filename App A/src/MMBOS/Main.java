/*
    Projektarbete - Program A
    Kurs: OP1
    Klass: SYSJG4
    Elever: Marcus Richardson, Michael Hejl

    Programmering av Michael

 */

package MMBOS;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import com.google.gson.GsonBuilder;

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
    public static ArrayList<Block> blockchain = new ArrayList<>(); //BlockChain...
    public static int numOfZerosInHash = 3;
    public static ArrayList<BlockCheck> blockChecker = new ArrayList<>();
    public static final String role = "Customer";

    public static void main(String[] args) throws FileNotFoundException {

        checkForFiles();
        fetchCustomers();
        fetchAccounts();
        fetchPaymentOrders();

        while (true) {
            String headMenuChoice = printHeadMenu();
            if (headMenuChoice.equals("0")) break;


            if (headMenuChoice.equals("2")) {
                createNewCustomer();
            }

// Menyval 3 : Skapa nytt konto
            if (headMenuChoice.equals("3")) {
                createNewAccount();
                if (saveAccountsToFile()) {
                    System.out.println("Kontonummer: " + newAccountNumber + " skapades utan problem.");
                }
                fetchAccounts();
                updNextAccountNumberFile();

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
                String accountFormat = "%-15s %10.2f\n";
                System.out.println("\nListar alla konton på banken");
                System.out.format("%-15s %10s\n", "KONTO", "SALDO");
                for (int i = 0; i < accountsList.size(); i++) {
                    System.out.format(accountFormat, String.valueOf(accountsList.get(i).accountNumber).substring(0, 4) + " " + String.valueOf(accountsList.get(i).accountNumber).substring(4, 6) + " " + String.valueOf(accountsList.get(i).accountNumber).substring(6), accountsList.get(i).cashInAccount);
                }
                System.out.println("Tryck <enter> för att återgå till huvudmenyn.");
                String keyPress = keyBoard.nextLine();
            }

// TODO: Insättningar
            if (headMenuChoice.equals("6")) {
                depositCash();
            }

// TODO: Uttag
            if (headMenuChoice.equals("7")) {
                withdrawalCash();
            }

// TODO: Betalningsuppdrag
            if (headMenuChoice.equals("8")) {
                paymentOrders();
            }

// TODO: Banktillgångar
            if (headMenuChoice.equals("9")) {
                showBankVault();
            }

// TODO: Överföringar
            if (headMenuChoice.equals("10")) {
                transferCash();
            }

            if (headMenuChoice.equals("11")) {
                checkTransferHash();

                System.out.println("Tryck <enter> för att återgå till huvudmenyn.");
                String keyPress = keyBoard.nextLine();
            }

            if (headMenuChoice.equals("12")) {
                login();
            }

            if(headMenuChoice.equals("1")){
                logout();
            }

            if(headMenuChoice.equals("15")){
                System.out.println("Felaktig inmatning.");
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
        for (int i = 0; i < customersList.size(); i++) {
            System.out.format(customerFormat, i, customersList.get(i).getPersonalID().substring(0, 8) + "-" + customersList.get(i).getPersonalID().substring(8), customersList.get(i).firstName + " " + customersList.get(i).lastName);
        }
    }

    private static void createNewAccount() {
        Random randomizer = new Random();
        String randomAccount = "";
        for (int i = 0; i < 5; i++) {
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

    private static void savePaymentOrdersFile() {

        try {
            FileWriter myWriter = new FileWriter(pendingPaymentsFile);
            for(int i = 0; i < paymentsList.size(); i++){
                Payments p = paymentsList.get(i);
                myWriter.write(p.fromAccount + ";" + p.toAccount + ";" + p.moneyAmount + ";"
                        + p.day + "\n");
            }
            myWriter.close();
        } catch(Exception e){
            System.out.println("Problem vid inskrivning av paymentorders.");
        }
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
        //Code by Marcus
        String headMenuChoise;
        if(bankID < 0){
            System.out.println("       M & M Bank of Sweden       ");
            System.out.format("%34s\n", "----------------------------------");
            System.out.format(menuFormater, "[1]", "logga in");
            System.out.format("%34s\n", "----------------------------------");
            System.out.format(menuFormater, "[0]", "Avsluta programmet");
            System.out.print("\nMenyval: ");
            headMenuChoise = keyBoard.nextLine();
            if(Integer.parseInt(headMenuChoise) > 1){
                headMenuChoise = "15";
            }
            if(Integer.parseInt(headMenuChoise) == 1){
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
                System.out.format(menuFormater, "[9]", "Banktillgångar");
                System.out.format(menuFormater, "[10]", "Överföringar");
                System.out.format("%34s\n", "----------------------------------");
                System.out.format(menuFormater, "[0]", "Avsluta programmet");
                System.out.print("\nMenyval: ");
                headMenuChoise = keyBoard.nextLine();
                if(Integer.parseInt(headMenuChoise) > 10){
                    headMenuChoise = "15";
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
                headMenuChoise = keyBoard.nextLine();
                if(Integer.parseInt(headMenuChoise) > 3){
                    headMenuChoise = "15";
                } else if(Integer.parseInt(headMenuChoise) == 2){
                    headMenuChoise = "4";
                } else if(Integer.parseInt(headMenuChoise) == 3){
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
                System.out.format(menuFormater, "[7]", "Uttag");
                System.out.format(menuFormater, "[8]", "Betalningsuppdrag (OCR)");
                System.out.format(menuFormater, "[9]", "Överföringar");
                System.out.format("%34s\n", "----------------------------------");
                System.out.format(menuFormater, "[0]", "Avsluta programmet");
                System.out.print("\nMenyval: ");
                headMenuChoise = keyBoard.nextLine();
                if(Integer.parseInt(headMenuChoise) > 9){
                    headMenuChoise = "15";
                }
                else if(Integer.parseInt(headMenuChoise) == 9){
                    headMenuChoise = "10";
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
                headMenuChoise = keyBoard.nextLine();
                if(Integer.parseInt(headMenuChoise) > 6){
                    headMenuChoise = "15";
                } else if(Integer.parseInt(headMenuChoise) == 2){
                    headMenuChoise = "3";
                } else if(Integer.parseInt(headMenuChoise) == 3){
                    headMenuChoise = "6";
                } else if(Integer.parseInt(headMenuChoise) == 4){
                    headMenuChoise = "7";
                } else if(Integer.parseInt(headMenuChoise) == 5){
                    headMenuChoise = "8";
                } else if(Integer.parseInt(headMenuChoise) == 6){
                    headMenuChoise = "10";
                }

            } else {
                headMenuChoise = "15";
            }
        }
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
            System.out.print(e.getMessage() + "\n" + e.getStackTrace());
            return;
        }
    }

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

    private static void fetchPaymentOrders(){
        try{
            Scanner paymentOrderReader = new Scanner(pendingPaymentsFile);
            while(paymentOrderReader.hasNextLine()){
                String rowsFromFile = paymentOrderReader.nextLine();
                String[] readerparts = rowsFromFile.split(";");
                Payments payments = new Payments(readerparts[0], readerparts[1], readerparts[2], readerparts[3]);
                paymentsList.add(payments);
            }
            paymentOrderReader.close();
        } catch(Exception e) {

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
            System.out.print(e.getMessage() + "\n" + e.getStackTrace());
            return;
        }
    }

    public static Boolean isChainValid() {
        Block currentBlock;
        Block previousBlock;
        String hashTarget = new String(new char[numOfZerosInHash]).replace('\0', '0');

        //loop through blockchain to check hashes:
        for (int i = 1; i < blockchain.size(); i++) {
            currentBlock = blockchain.get(i);
            previousBlock = blockchain.get(i - 1);
            //compare registered hash and calculated hash:
            if (!currentBlock.hash.equals(currentBlock.calculateHash())) {
                System.out.println("Nuvarande hash är korrupt.");
                return false;
            }
            //compare previous hash and registered previous hash
            if (!previousBlock.hash.equals(currentBlock.previousHash)) {
                System.out.println("Föregåene hash är korrupt.");
                return false;
            }
            //check if hash is solved
            if (!currentBlock.hash.substring(0, numOfZerosInHash).equals(hashTarget)) {
                System.out.println("Detta block har inte hämtats.");
                return false;
            }
        }
        return true;
    }

    /*
    Code by Marcus
     */

    public static void createNewCustomer() {

        System.out.print("Enter first name: ");
        String firstname = keyBoard.nextLine();
        System.out.print("Enter lastname: ");
        String lastname = keyBoard.nextLine();
        System.out.print("Enter birth ID: ");
        String personID = keyBoard.nextLine();
        System.out.print("Enter new password: ");
        String password1 = keyBoard.nextLine();
        System.out.print("Enter new password again: ");
        String password2 = keyBoard.nextLine();

        boolean term;
        if (password1.equals(password2)) {
            try {
                term = checkID(personID);
            } catch (Exception e) {
                System.out.println("Something went wrong with ID creation...");
                term = false;
            }

            if (term) {
                Customers newCustomer = new Customers(personID, firstname, lastname, password1, role);
                customersList.add(newCustomer);
            }

        } else {
            System.out.println("Enter the same password twice.");
        }

    }

    public static boolean checkID(String personID) {

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
            System.out.println("Successfully created new account!");
            return true;
        } else {
            System.out.println("Invalid controllnumber.");
            return false;
        }
    }

    public static void login() {
        System.out.println("Login to your account");

        System.out.print("Enter birth ID: ");
        String birthID = keyBoard.nextLine();
        System.out.print("Enter password: ");
        String password = keyBoard.nextLine();

        boolean term = true;
        for (int i = 0; i < customersList.size(); i++) {
            if (customersList.get(i).getPersonalID().equals(birthID)) {
                if (customersList.get(i).passWd.equals(password)) {
                    System.out.println("Welcome " + customersList.get(i).firstName + " " + customersList.get(i).lastName);
                    bankID = i;
                    term = false;
                }
            }
        }
        if (term) {
            System.out.println("Wrong ID and/or password. . .");
        }
    }

    public static void logout(){
        System.out.println("logging off. . .");
        bankID = -1;
    }

    public static void depositCash(){
        String format = "%-15s %15s %15s\n";
        System.out.format((format), "Account ID", "AccountNumber", "MoneyInAccount");

        for (int i = 0; i < accountsList.size(); i++) {
            if (accountsList.get(i).personalID.equals(customersList.get(bankID).getPersonalID())) {
                System.out.format((format), i, accountsList.get(i).accountNumber, accountsList.get(i).cashInAccount);
            }
        }
        System.out.print("Which account would you like to deposit your money to? choose with help of \"Account ID\": ");
        String accID = keyBoard.nextLine();
        System.out.print("How much money would you like to deposit?");
        String amount = keyBoard.nextLine();

        if(accountsList.get(Integer.parseInt(accID)).personalID.equals(customersList.get(bankID).getPersonalID())){
            accountsList.get(Integer.parseInt(accID)).depositCash(Double.parseDouble(amount));
            System.out.println("Successfully deposited " + amount + " to your account!");
            saveAccountsToFile();
        } else{
            System.out.println("Choose an account ID from the list next time.");
        }
    }

    public static void withdrawalCash(){
        String format = "%-15s %15s %15s\n";
        System.out.format((format), "Account ID", "AccountNumber", "MoneyInAccount");

        for (int i = 0; i < accountsList.size(); i++) {
            if (accountsList.get(i).personalID.equals(customersList.get(bankID).getPersonalID())) {
                System.out.format((format), i, accountsList.get(i).accountNumber, accountsList.get(i).cashInAccount);
            }
        }
        System.out.print("From which account would you like to withdrawal your money from? choose with help of \"Account ID\":");
        String accID = keyBoard.nextLine();
        System.out.print("How much money would you like to withdrawal? Amount: ");
        String amount = keyBoard.nextLine();

        if(accountsList.get(Integer.parseInt(accID)).cashInAccount > Double.parseDouble(amount)){
            accountsList.get(Integer.parseInt(accID)).withdrawlCash(Double.parseDouble(amount));
            System.out.println("Successfully withdrew " + amount + " from your account!");
            saveAccountsToFile();
        }else{
            System.out.println("You don't have enough money in your account.");
        }
    }

    public static void paymentOrders(){

        System.out.println("To whom would you like to do your payment to?");
        String format = "%-15s %-15s %-15s %15s %20s \n";
        System.out.format((format), "Account ID", "Name", "Lastname", "Account number", "Amount of money");

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

        System.out.print("choose with help of \"Account ID\": ");
        String newAccID = keyBoard.nextLine();
        System.out.print("Enter amount of money to transfer: ");
        String amountMoney = keyBoard.nextLine();

        String format2 = "%-15s %-15s %-20s\n";
        System.out.format((format2), "Account ID", "AccountNumber", "MoneyInAccount");
        for (int x = 0; x < accountsList.size(); x++) {
            if (accountsList.get(x).personalID.equals(customersList.get(bankID).getPersonalID())) {
                System.out.format((format2), x, accountsList.get(x).accountNumber, accountsList.get(x).cashInAccount);
            }
        }

        System.out.print("From which account would you like to transfer the money? choose with help of \"Account ID\": ");
        String myAccID = keyBoard.nextLine();
        double myMoney = accountsList.get(Integer.parseInt(myAccID)).cashInAccount;
        LocalDate today = LocalDate.now();
        today = today.plusDays(3);

        if (Double.parseDouble(amountMoney) < myMoney) {
            if (accountsList.get(Integer.parseInt(myAccID)).personalID.equals(customersList.get(bankID).getPersonalID())
                    && !accountsList.get(Integer.parseInt(newAccID)).personalID.equals(customersList.get(bankID).getPersonalID())){

                myAccID = String.valueOf(accountsList.get(Integer.parseInt(myAccID)).accountNumber);
                newAccID = String.valueOf(accountsList.get(Integer.parseInt(newAccID)).accountNumber);

                Payments newpay = new Payments(myAccID, newAccID, amountMoney, String.valueOf(today));
                paymentsList.add(newpay);
                savePaymentOrdersFile();

                for(int y = 0; y < accountsList.size(); y++){
                    if(String.valueOf(accountsList.get(y).accountNumber).equals(myAccID)){
                        accountsList.get(y).withdrawlCash(Double.parseDouble(amountMoney));
                        saveAccountsToFile();
                    }
                }

            } else{
                System.out.println("Choose a valid account ID next time.");
            }
        } else{
            System.out.println("You don't have enough money in your account.");
        }

    }

    public static void showBankVault(){
        double money = 0;
        for(int i = 0; i < accountsList.size(); i++){
            money = money + accountsList.get(i).getCash();
        }
        System.out.println("Amount of money stored in the bank at the moment: " + money);
    }

    public static void transferCash(){

        String format = "%-15s %15s %20s \n";
        System.out.format((format), "Account ID", "Account number", "Amount of money");

        for (int i = 0; i < accountsList.size(); i++) {
            if (accountsList.get(i).personalID.equals(customersList.get(bankID).getPersonalID())) {
                System.out.format((format), i, accountsList.get(i).accountNumber, accountsList.get(i).cashInAccount);

            }
        }

        System.out.print("from which account would you like to transfer from? choose with the help of \"Account ID\": ");
        String fromAccIndex = keyBoard.nextLine();
        System.out.print("How much money would you like to transfer? ");
        String amount = keyBoard.nextLine();
        System.out.print("Which account would you like to transfer to? choose with the help of \"Account ID\": ");
        String newAccIndex = keyBoard.nextLine();

        boolean term = true;
        try {
            Integer.parseInt(newAccIndex);
            Integer.parseInt(amount);
            Integer.parseInt(fromAccIndex);
            if(Integer.parseInt(newAccIndex) > accountsList.size()
                    || Integer.parseInt(fromAccIndex) > accountsList.size()){
                term = false;
            }
        } catch(Exception e) {
            term = false;
        }

        if(term){
            if(accountsList.get(Integer.parseInt(fromAccIndex)).personalID.equals(customersList.get(bankID).getPersonalID())
                    && accountsList.get(Integer.parseInt(newAccIndex)).personalID.equals(customersList.get(bankID).getPersonalID())){

                if(accountsList.get(Integer.parseInt(fromAccIndex)).getCash() > Integer.parseInt(amount)) {
                    accountsList.get(Integer.parseInt(fromAccIndex)).withdrawlCash(Integer.parseInt(amount));
                    accountsList.get(Integer.parseInt(newAccIndex)).depositCash(Integer.parseInt(amount));
                    // UPPDATERA ACCOUNT FILES HÄR
                }
                else{
                    System.out.println("You don't have enough money in that account!");
                }
            }
            else{
                System.out.println("You need to enter a valid index.");
            }
        }
        else{
            System.out.println("You need to enter a valid index.");
        }

    }
}


