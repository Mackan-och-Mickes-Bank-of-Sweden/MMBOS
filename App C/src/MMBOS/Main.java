package MMBOS;

import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * App C.
 * @author Michael
 *
 */

public class Main {
    public static File accountsFile = new File("files/accounts.acc");
    public static File pendingPaymentsFile = new File("files/pendingpayments.pay");
    public static File paymentProblemsFile = new File("files/paymentproblems.pay");
    public static File transferLogFile = new File("logs/transfers.log");
    public static File hashtoryFile = new File("logs/hashtory.log");
    public static int numOfZerosInHash = 3;
    public static int repeatTransferDays = 3;
    public static ArrayList <Accounts> accountsList = new ArrayList<>();
    public static ArrayList <Pending> pendingPayments = new ArrayList<>();
    public static ArrayList <BlockCheck> blockChecker = new ArrayList<>();

    public static void main(String[] args) throws InterruptedException, IOException {
        for (int i = 0; i > -1; i++) {
            fetchPendingPayments();
            for (int j=0; j<pendingPayments.size(); j++) {
                if (pendingPayments.get(j).transferDate.equals(String.valueOf(LocalDate.now()))){
                    System.out.print("Transaktion från konto: " + pendingPayments.get(j).fromAccount + " till konto: " + pendingPayments.get(j).toAccount + " genomförs");
                    Thread.sleep(300);
                    System.out.print(" .");
                    Thread.sleep(300);
                    System.out.print(" .");
                    Thread.sleep(300);
                    System.out.println(" .");
                    Thread.sleep(300);
                    if (updateAccountFundings(pendingPayments.get(j).fromAccount, pendingPayments.get(j).toAccount, pendingPayments.get(j).transferAmount)) {
                        checkTransferHash(pendingPayments.get(j).fromAccount, pendingPayments.get(j).toAccount, pendingPayments.get(j).transferAmount, pendingPayments.get(j).transferMessage);
                        pendingPayments.remove(j);
                    } else {
                        try {
                            Files.write(Paths.get(String.valueOf(paymentProblemsFile)), (pendingPayments.get(j).fromAccount+";"+pendingPayments.get(j).toAccount+";"+pendingPayments.get(j).transferAmount+";"+pendingPayments.get(j).transferDate+";"+String.valueOf(LocalDate.now().plusDays(repeatTransferDays))+";"+pendingPayments.get(j).transferMessage+";"+"1\n").getBytes(), StandardOpenOption.APPEND);
                        }
                        catch (Exception e) {
                            System.out.println("Problem vid skrivning till paymentproblems.pay");
                        }
                        Pending laterDate = new Pending(pendingPayments.get(j).fromAccount, pendingPayments.get(j).toAccount, pendingPayments.get(j).transferAmount, String.valueOf(LocalDate.now().plusDays(repeatTransferDays)), pendingPayments.get(j).transferMessage);
                        pendingPayments.set(j,laterDate);
                        System.out.println("Transaktion från konto: " + pendingPayments.get(j).fromAccount + " till konto: " + pendingPayments.get(j).toAccount + " kunde inte genomföras pga otillräckligt saldo, nytt försök " +  String.valueOf(LocalDate.now().plusDays(repeatTransferDays)));
                    }
                    Thread.sleep(1000); //Utför en transaktion per sekund utav de som faller på dagens datum.
                }
            }
            savePendingPayments();
            Thread.sleep(600000); // Loopa var 10:de minut
        }
    }
    public static boolean updateAccountFundings(String fromAccount, String toAccount, double transferAmount) throws IOException {
        fetchAccounts();
        for(int i=0; i<accountsList.size(); i++) {
            if(fromAccount.equals(String.valueOf(accountsList.get(i).accountNumber))) {
                if (accountsList.get(i).cashInAccount < transferAmount) {
                    return false;
                } else {
                    double newCashValue = (accountsList.get(i).cashInAccount - transferAmount);
                    Accounts updateAccount = new Accounts(Long.parseLong(fromAccount), accountsList.get(i).personalID, newCashValue);
                    accountsList.set(i, updateAccount);
                }

            }
            if (toAccount.equals(String.valueOf(accountsList.get(i).accountNumber))) {
                double newCashValue = (accountsList.get(i).cashInAccount + transferAmount);
                Accounts updateAccount = new Accounts(Long.parseLong(toAccount), accountsList.get(i).personalID, newCashValue);
                accountsList.set(i, updateAccount);
            }

        }
        FileWriter fw = new FileWriter(accountsFile);
        for (int i = 0; i < accountsList.size(); i++) {
            Accounts a = accountsList.get(i);
            fw.write(a.accountNumber + ";" + a.personalID + ";" + a.cashInAccount + "\n");
        }
        fw.close();
        return true;
    }

    public static void savePendingPayments() throws IOException {
        FileWriter fw = new FileWriter(pendingPaymentsFile);
        for(int i = 0; i < pendingPayments.size(); i++){
            Pending pp = pendingPayments.get(i);
            fw.write(pp.fromAccount + ";" + pp.toAccount + ";" + pp.transferAmount + ";"
                    + pp.transferDate + ";" + pp.transferMessage + "\n");
        }
        fw.close();
    }

    public static void fetchPendingPayments() throws FileNotFoundException {
        pendingPayments.clear();
        Scanner sc = new Scanner(pendingPaymentsFile);
        while (sc.hasNextLine()) {
            String rowsFromFile = sc.nextLine();
            String[] readerParts = rowsFromFile.split(";");
            Pending addPending = new Pending(readerParts[0],readerParts[1], Double.parseDouble(readerParts[2]), readerParts[3], readerParts[4]);
            pendingPayments.add(addPending);
        }
    }

    /**
     * method to check if hash chain is valid, returns true/false
     * @author Michael
     * @return
     */
    public static Boolean isChainValid() {
        BlockCheck currentBlock;
        BlockCheck previousBlock;
        String zeros = new String(new char[numOfZerosInHash]).replace('\0', '0');
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

            if (!currentBlock.hash.substring(0, numOfZerosInHash).equals(zeros)) {
                System.out.println("Denna hash kunde inte lösas.");
                return false;
            }
        }
        return true;
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
    private static void checkTransferHash(String fromAccount, String toAccount, double transferAmount, String transferMessage) throws FileNotFoundException {
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
        String blockchainJson = new GsonBuilder().setPrettyPrinting().create().toJson(blockChecker);
        try {
            Files.write(Paths.get(String.valueOf(hashtoryFile)), (blockChecker.get(i).hash+";"+blockChecker.get(i).previousHash+";"+blockChecker.get(i).settimeStamp+";"+blockChecker.get(i).setNonce+"\n").getBytes(), StandardOpenOption.APPEND);
            Files.write(Paths.get(String.valueOf(transferLogFile)), (dataString+"\n").getBytes(), StandardOpenOption.APPEND);
        }
        catch (Exception e) {
            System.out.println("Problem vid skrivning till log filer");
        }
        //System.out.print(blockchainJson); //Skriv ut hela hash kedjan
        if (isChainValid()) System.out.println("Transaktion genomförd. Hashkontontroll godkänd.");
    }

    /**
     * fetch all accounts from csv file
     * @author Michael
     */
    public static void fetchAccounts() {
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
}
