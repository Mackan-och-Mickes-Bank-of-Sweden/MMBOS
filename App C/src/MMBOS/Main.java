package MMBOS;

import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

public class Main {
    public static File accountsFile = new File("files/accounts.acc");
    public static ArrayList <Accounts> accountsList = new ArrayList<>();
    public static File pendingPaymentsFile = new File("files/pendingpayments.pay");
    public static File transferLogFile = new File("logs/transfers.log");
    public static File hashtoryFile = new File("logs/hashtory.log");
    public static ArrayList<Block> blockchain = new ArrayList<>(); //BlockChain...
    public static int numOfZerosInHash = 3;
    public static ArrayList<BlockCheck> blockChecker = new ArrayList<>();

    public static void main(String[] args) throws InterruptedException, FileNotFoundException {
        for (int i = 0; i > -1; i++) {
            Thread.sleep(5000);

            Scanner pendingPayments = new Scanner(pendingPaymentsFile);
            while (pendingPayments.hasNextLine()) {
                String rowsFromFile = pendingPayments.nextLine();
                String[] readerParts = rowsFromFile.split(";");
                if (readerParts[3].equals(String.valueOf(LocalDate.now()))) {
                    System.out.println("Överföring från: " + readerParts[0] + " till: " + readerParts[1] + " utförs");
                    saveTransferToLogfile(readerParts[0], readerParts[1], readerParts[2], readerParts[4]);
                    saveTransferData(readerParts[0]+";"+readerParts[1]+";"+readerParts[2]+";"+readerParts[4]);
                    Thread.sleep(500);
                }
            }
            checkTransferHash();
        }
    }

    public static void saveTransferToLogfile(String fromaccount, String toaccount, String amount, String message) {
        try {
            Files.write(Paths.get(String.valueOf(transferLogFile)), (fromaccount+";"+toaccount+";"+amount+";"+message+"\n").getBytes(), StandardOpenOption.APPEND);
        }
        catch (Exception e) {
            System.out.println("Problem vid skrivning till transferfil");
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

    private static void checkTransferHash() throws FileNotFoundException {
        Scanner checkTransfersFile = new Scanner(transferLogFile);
        Scanner checkHashtoryFile = new Scanner(hashtoryFile);

        while (checkTransfersFile.hasNextLine()) {
            String rowsFromFile = checkTransfersFile.nextLine();
            String rowsFromHashtory = checkHashtoryFile.nextLine();
            String[] readerHashParts = rowsFromHashtory.split(";");
            blockChecker.add(new BlockCheck(rowsFromFile, readerHashParts[1], readerHashParts[2],readerHashParts[3]));
        }
        //String blockchainJson = new GsonBuilder().setPrettyPrinting().create().toJson(blockChecker);
        if (isChainValid()) System.out.println("\nHashkontontrollen utförd, alla uppgifter stämmer!");
    }

    private static void saveTransferData(String data) throws FileNotFoundException {
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
        i--;
        //previousHash = blockchain.get(blockchain.size()).previousHash;
        //System.out.print(previousHash);
        //blockchain.add(new Block(data,previousHash));
        //blockchain.get(blockchain.size()).mineBlock(numOfZerosInHash);
    System.out.print(blockchain.get(i).previousHash+";"+blockchain.get(i).hash+";"+blockchain.get(i).getTimeStamp()+";"+blockchain.get(i).getNonce()+"\n");
        try {
            Files.write(Paths.get(String.valueOf(hashtoryFile)), (blockchain.get(i).previousHash+";"+blockchain.get(i).hash+";"+blockchain.get(i).getTimeStamp()+";"+blockchain.get(i).getNonce()+"\n").getBytes(), StandardOpenOption.APPEND);
        }
        catch (Exception e) {
            System.out.println("Problem vid skrivning till transferfil");
        }

       // System.out.println("\nBlockchain är giltig: " + isChainValid());

        //String blockchainJson = new GsonBuilder().setPrettyPrinting().create().toJson(blockchain);
        //System.out.println("\nThe block chain: ");
        //System.out.println(blockchainJson);
    }

    /**
     * fetch all the loged in user's accounts from csv file -> customersAccountsList
     * fetch all existing accounts from csv file -> allAccountsList
     * @author Michael
     */
    public static void fetchAccounts() {
        try {
            Scanner accountsFileReader = new Scanner(accountsFile);
            accountsList.clear();
            while (accountsFileReader.hasNextLine()) {
                String rowsFromFile = accountsFileReader.nextLine();
                String[] readerParts = rowsFromFile.split(";");
                Accounts readAccountforAll = new Accounts(Long.parseLong(readerParts[0]), readerParts[1], Double.parseDouble(readerParts[2]));
                Accounts readAccount = new Accounts(Long.parseLong(readerParts[0]), readerParts[1], Double.parseDouble(readerParts[2]));
                accountsList.add(readAccount);
            }
        } catch (Exception e) {
            System.out.print(e.getMessage() +"\n" + e.getStackTrace());
            return;
        }
    }
}
