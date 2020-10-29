package MMBOS;

import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {
    public static File accountsFile = new File("files/accounts.acc");
    public static ArrayList <Accounts> accountsList = new ArrayList<>();
    public static File pendingPaymentsFile = new File("files/pendingpayments.pay");
    public static File transferLogFile = new File("logs/transfers.log");
    public static File hashtoryFile = new File("logs/hashtory.log");
    public static int numOfZerosInHash = 3;
    public static ArrayList<BlockCheck> blockChecker = new ArrayList<>();

    public static void main(String[] args) throws InterruptedException, FileNotFoundException {
        for (int i = 0; i > -1; i++) {
            Scanner pendingPayments = new Scanner(pendingPaymentsFile);
            while (pendingPayments.hasNextLine()) {
                String rowsFromFile = pendingPayments.nextLine();
                String[] readerParts = rowsFromFile.split(";");
                if (readerParts[3].equals(String.valueOf(LocalDate.now()))) {
                    System.out.println("Överföring från: " + readerParts[0] + " till: " + readerParts[1] + " utförs");
                    checkTransferHash(readerParts[0], readerParts[1], Double.parseDouble(readerParts[2]), readerParts[4]);
                    Thread.sleep(1000);
                }
            }
            Thread.sleep(1000);
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
        System.out.print(blockchainJson);
        if (isChainValid()) System.out.println("\nHashkontontrollen utförd, alla uppgifter stämmer!");
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
