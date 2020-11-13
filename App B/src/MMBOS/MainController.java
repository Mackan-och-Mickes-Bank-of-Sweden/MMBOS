package MMBOS;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;
import java.util.Scanner;
import java.util.function.UnaryOperator;

public class MainController {
    public static MainController mc;
    public MainController(){
        mc = this;
    }
    Main main;

    public static File accountsFile = new File("files/accounts.acc");
    public static ArrayList <Accounts> accountsList = new ArrayList<>();
    public static String loggedinID;
    public static File nextAccountNumber = new File("files/nextaccountnumber.acc");
    public static File paymentProblemsFile = new File("files/paymentproblems.pay");
    public static File pendingPaymentsFile = new File("files/pendingpayments.pay");
    public static ArrayList <TransferMessages> transferFailMassages = new ArrayList<>();
    public static int nextAccountNumb;
    public static String newAccountNumber;
    public static ArrayList <BlockCheck> blockChecker = new ArrayList<>();
    public static File transferLogFile = new File("logs/transfers.log");
    public static File hashtoryFile = new File("logs/hashtory.log");
    public static int numOfZerosInHash = 3;
    public NumberFormat nfSv = NumberFormat.getCurrencyInstance(new Locale("sv", "SE"));
    public static ArrayList <Pending> allPendingPayments = new ArrayList<>();

    @FXML private Label loggedinText;
    @FXML private ListView myAccountList;
    @FXML private ComboBox comboMenu;
    @FXML private ComboBox cbTransferFromAccount;
    @FXML private ComboBox cbTransferToAccount;
    @FXML private Label headerText;
    @FXML private TextField transferMessage;
    @FXML private TextField transferAmount;
    @FXML private DatePicker datepickerTransfer;
    @FXML private Pane groupTransferOwnAccount;
    @FXML private Pane groupTransferOtherAccount;
    @FXML private DatePicker datepickerTransferOther;
    @FXML private ComboBox cbTransferOtherFromAccount;
    @FXML private TextField transferMessageOther;
    @FXML private TextField toAccountOther;
    @FXML private TextField transferAmountOther;
    @FXML private Pane groupCreateNewAccount;
    @FXML private CheckBox checkboxCreateNewAccount;
    @FXML private Pane groupDeposit;
    @FXML private TextField depositAmount;
    @FXML private ComboBox cbDepositFromAccount;
    @FXML private AnchorPane depositPopup;
    @FXML private Pane groupDeleteTransfer;
    @FXML private ListView pendingTransfersList;

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

    public void doTransferBetweenAccounts(long fromAccountNumber, long toAccountNumber, double amountToTransfer) throws IOException {
        LocalDate dateForTransfer;
        String transferMess;
        if (transferMessage.getText().isEmpty()) {
            transferMess = "";
        } else {
            transferMess = transferMessage.getText();
        }
        if (checkMessage(transferMessage.getText())) {
            alertPopup("Ditt överföringsmeddelande innehåller ogiltiga tecken eller är för långt - max 20 tecken!","Kontoöverföring");
            return;
        }
        if(datepickerTransfer.getValue() != null){
            dateForTransfer = datepickerTransfer.getValue();
        } else {
            dateForTransfer = LocalDate.now();
        }
        if (dateForTransfer.isBefore(LocalDate.now())) {
            alertPopup("Datum kan inte vara tidigare än dagens datum","Kontoöverföring");
        }
        else if (dateForTransfer.equals(LocalDate.now())) {
            for (int i = 0; i < accountsList.size(); i++) {
                if (accountsList.get(i).accountNumber == fromAccountNumber) {
                    if (accountsList.get(i).cashInAccount < amountToTransfer) {
                        alertPopup("Det finns inte tillräckligt med likvida medel på kontot för att utföra överföringen!", "Kontoöverföring");
                        return;
                    }
                    Accounts updateAccount = new Accounts(fromAccountNumber, accountsList.get(i).getPersonalID(), (accountsList.get(i).cashInAccount - amountToTransfer));
                    accountsList.set(i, updateAccount);
                }
                if (accountsList.get(i).accountNumber == toAccountNumber) {
                    Accounts updateAccount = new Accounts(toAccountNumber, accountsList.get(i).getPersonalID(), (accountsList.get(i).cashInAccount + amountToTransfer));
                    accountsList.set(i, updateAccount);
                }
            }
            logTransfer(String.valueOf(fromAccountNumber), String.valueOf(toAccountNumber), amountToTransfer, transferMess);
            updateAccountListComboBoxes();
            saveAccountsToFile();
            alertPopup("Överföringen mellan dina konton utfördes!", "Kontoöverföring");
        } else {
            try {
                Files.write(Paths.get("files/pendingpayments.pay"), (fromAccountNumber+";"+toAccountNumber+";"+amountToTransfer+";"+dateForTransfer+";"+transferMess+"\n").getBytes(), StandardOpenOption.APPEND);
                alertPopup("Överföringen har lagts till och kommer att genomföras det valda datumet","Kontoöverföring");
            }
            catch (Exception e) {
                System.out.println("Problem vid skrivning till överföringsfilen.");
            }
        }
        transferMessage.setText("");
        transferAmount.setText("");
    }

    public void doTransferOtherAccount(long fromAccountNumber, long toAccountNumber, double amountToTransfer) throws IOException {
        String transferMess;
        if (transferMessageOther.getText().isEmpty()) {
            transferMess = "";
        } else {
            transferMess = transferMessageOther.getText();
        }
        if (checkMessage(transferMessageOther.getText())) {
            alertPopup("Ditt överföringsmeddelande innehåller ogiltiga tecken eller är för långt - max 20 tecken!","Betalning / Överföring till annans konto");
            return;
        }
        LocalDate dateForTransfer;
        if(datepickerTransferOther.getValue() != null){
            dateForTransfer = datepickerTransferOther.getValue();
        } else {
            dateForTransfer = LocalDate.now();
        }
        if (dateForTransfer.isBefore(LocalDate.now())) {
            alertPopup("Datum kan inte vara tidigare än dagens datum","Betalning / Överföring till annans konto");
        }
        else if (dateForTransfer.equals(LocalDate.now())) {
            for (int i = 0; i < accountsList.size(); i++) {
                if (accountsList.get(i).accountNumber == fromAccountNumber) {
                    if (accountsList.get(i).cashInAccount < amountToTransfer) {
                        alertPopup("Det finns inte tillräckligt med likvida medel på kontot för att utföra överföringen!", "Betalning / Överföring till annans konto");
                        //break;
                        return;
                    }
                    Accounts updateAccount = new Accounts(fromAccountNumber, accountsList.get(i).getPersonalID(), (accountsList.get(i).cashInAccount - amountToTransfer));
                    accountsList.set(i, updateAccount);
                }
                if (accountsList.get(i).accountNumber == toAccountNumber) {
                    Accounts updateAccount = new Accounts(toAccountNumber, accountsList.get(i).getPersonalID(), (accountsList.get(i).cashInAccount + amountToTransfer));
                    accountsList.set(i, updateAccount);
                }
            }
            logTransfer(String.valueOf(fromAccountNumber), String.valueOf(toAccountNumber), amountToTransfer, transferMess);
            updateAccountListComboBoxes();
            saveAccountsToFile();
            alertPopup("Överföringen/Betalningen har utförts!", "Betalning / Överföring till annans konto");
        } else {
            try {
                Files.write(Paths.get("files/pendingpayments.pay"), (fromAccountNumber+";"+toAccountNumber+";"+amountToTransfer+";"+dateForTransfer+";"+transferMess+"\n").getBytes(), StandardOpenOption.APPEND);
                alertPopup("Överföringen/Betalningen har lagts till och kommer att genomföras det valda datumet","Betalning / Överföring till annans konto");
            }
            catch (Exception e) {
                System.out.println("Problem vid skrivning till överföringsfilen.");
            }
        }
        transferMessageOther.setText("");
        transferAmountOther.setText("");
        toAccountOther.setText("");
    }

    public void saveAccountsToFile() throws IOException {
        FileWriter fw = new FileWriter(accountsFile);
        for (int i=0; i < accountsList.size(); i++) {
            fw.write(accountsList.get(i).accountNumber + ";" + accountsList.get(i).getPersonalID() + ";" + accountsList.get(i).cashInAccount + "\n");
        }
        fw.close();
    }

    public void updateAccountListComboBoxes() {
        myAccountList.getItems().clear();
        for (int i = 0; i< accountsList.size(); i++) {
            if (!accountsList.get(i).getPersonalID().equals(loggedinID)) continue;
            String item = String.valueOf(accountsList.get(i).accountNumber).substring(0,4) + " "
                    + String.valueOf(accountsList.get(i).accountNumber).substring(4,6) + " "
                    + String.valueOf(accountsList.get(i).accountNumber).substring(6) + " \t"
                    + nfSv.format(accountsList.get(i).cashInAccount);
            myAccountList.getItems().add(item);
        }
    }

    private void hideAllGroups() {
        groupTransferOwnAccount.setVisible(false);
        groupTransferOtherAccount.setVisible(false);
        groupCreateNewAccount.setVisible(false);
        groupDeposit.setVisible(false);
        groupDeleteTransfer.setVisible(false);
    }
    public void deleteTransferButtonClicked (Event e) throws IOException {
        allPendingPayments.remove(pendingTransfersList.getSelectionModel().getSelectedIndex());
        pendingTransfersList.getItems().remove(pendingTransfersList.getSelectionModel().getSelectedIndex());
        updatePendingPaymentsFile();
    }
    public void menuDoTranferOtherClicked (Event e) {
        hideAllGroups();
        comboMenu.setValue("Registrera betalning");
        groupTransferOtherAccount.setVisible(true);
    }
    public void menuDoTransferClicked (Event e) {
        hideAllGroups();
        comboMenu.setValue("Överföring eget konto");
        groupTransferOwnAccount.setVisible(true);
    }
    public void menuDepositClicked (Event e) {
        hideAllGroups();
        comboMenu.setValue("Uttag från konto");
        groupDeposit.setVisible(true);
    }
    public void menuPending (Event e) {
        hideAllGroups();
        comboMenu.setValue("Kommande betalningar");
        groupDeleteTransfer.setVisible(true);
    }
    public void menuHelpAboutClicked (Event e) {
        alertPopup("Marcus Richardsson & Michael Hejls projektarbete i Objektorienterad Programmering 1, SYSJG4 2020","Om MMBOS - Mackan & Micke's Bank of Sweden");
    }
    public void menuNewAccountClicked (Event e) {
        hideAllGroups();
        groupCreateNewAccount.setVisible(true);
        comboMenu.setValue("Öppna nytt konto");
    }
    
    /**
     * log out from system, change scene to login
     * @author Michael
     */
    public void menuLogoutClicked() {
        alertPopup("Du loggas nu ut ur systemet!", "Välkommen åter!");
        main.appWin.setScene(main.mapScenes.get("loginScene"));
        LoginController.lc.personalIDField.requestFocus();
    }
    public void doTransferOtherButtonClicked (Event e) throws IOException {
        if (!cbTransferOtherFromAccount.getSelectionModel().isEmpty() && !toAccountOther.getText().isEmpty() && !transferAmountOther.getText().isEmpty()) {
            doTransferOtherAccount(Long.parseLong(cbTransferOtherFromAccount.getValue().toString()), Long.parseLong(toAccountOther.getText()), Double.parseDouble(transferAmountOther.getText()));
        } else {
            alertPopup("Kontrollera alla fälten, det saknas uppgifter för att kunna utföra betalningen/överföringen","Betalning / Överföring till annans konto");
        }
    }
    public void doTransferButtonClicked (Event e) throws IOException {
        if (!cbTransferFromAccount.getSelectionModel().isEmpty() && !cbTransferToAccount.getSelectionModel().isEmpty() && !transferAmount.getText().isEmpty()) {
            doTransferBetweenAccounts(Long.parseLong(cbTransferFromAccount.getValue().toString()), Long.parseLong(cbTransferToAccount.getValue().toString()), Double.parseDouble(transferAmount.getText()));
        } else {
            alertPopup("Kontrollera alla fälten, det saknas uppgifter för att kunna utföra överföringen","Kontoöverföring");
        }
    }

    private void alertPopup(String messageText, String headerText) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, messageText, ButtonType.OK);
        alert.setTitle("** M M B O S **");
        alert.setHeaderText(headerText);
        alert.showAndWait();
    }

    /**
     * initialize
     * @author Michael
     * @param passingInfo personalID from login controller
     * @param name loged in persons firstname and lastname
     */
    public void loginIn(String passingInfo, String name) throws IOException {
        hideAllGroups();
        clearAllLists();
        loggedinText.setText(passingInfo);
        loggedinText.setVisible(false);
        loggedinID=passingInfo;
        fetchAccounts();
        fetchTransferFailMessages();
        myAccountList.setPrefWidth(300);
        for (int i = 0; i< accountsList.size(); i++) {
            if (!accountsList.get(i).getPersonalID().equals(loggedinID)) continue;
            for (int j = 0; j< transferFailMassages.size(); j++) {
                if (transferFailMassages.get(j).fromAccount.equals(String.valueOf(accountsList.get(i).accountNumber))) {
                    alertPopup("Du har schemalagt en överföring från konto: " + accountsList.get(i).accountNumber + " som inte kunde genomföras "+ transferFailMassages.get(j).orgDate +". Nytt försök kommer att göras " + transferFailMassages.get(j).newDate, "Otillräckliga medel på kontot");
                    transferFailMassages.remove(j);
                    updateTransferFailFile();
                }
            }
            String item = String.valueOf(accountsList.get(i).accountNumber).substring(0,4) + " "
                    + String.valueOf(accountsList.get(i).accountNumber).substring(4,6) + " "
                    + String.valueOf(accountsList.get(i).accountNumber).substring(6) + " \t"
                    + nfSv.format(accountsList.get(i).cashInAccount);
            myAccountList.getItems().add(item);
            cbTransferFromAccount.getItems().add(accountsList.get(i).accountNumber);
            cbTransferToAccount.getItems().add(accountsList.get(i).accountNumber);
            cbTransferOtherFromAccount.getItems().add(accountsList.get(i).accountNumber);
            cbDepositFromAccount.getItems().add(accountsList.get(i).accountNumber);
        }
        headerText.setText(name+"s konton i banken");
        comboMenu.getItems().clear();
        comboMenu.getItems().addAll("Öppna nytt konto", "Uttag från konto", "Överföring eget konto", "Registrera betalning", "Kommande betalningar");

        UnaryOperator<TextFormatter.Change> filter = change -> {
            String text = change.getText();
            if (text.matches("[0-9]*")) {
                return change;
            }
            return null;
        };
        TextFormatter<String> textFormatter = new TextFormatter<>(filter);
        TextFormatter<String> textFormatter2 = new TextFormatter<>(filter);
        TextFormatter<String> textFormatter3 = new TextFormatter<>(filter);
        TextFormatter<String> textFormatter4 = new TextFormatter<>(filter);
        transferAmount.setTextFormatter(textFormatter);
        transferAmountOther.setTextFormatter(textFormatter2);
        depositAmount.setTextFormatter(textFormatter3);
        toAccountOther.setTextFormatter(textFormatter4);
    }

    private void clearAllLists() {
        myAccountList.getItems().clear();
        cbDepositFromAccount.getItems().clear();
        cbTransferFromAccount.getItems().clear();
        cbTransferOtherFromAccount.getItems().clear();
        cbTransferToAccount.getItems().clear();
    }

    public void depositButtonClicked (Event e) throws IOException {
        if (!depositAmount.getText().isEmpty() && !cbDepositFromAccount.getSelectionModel().isEmpty()) {
            Boolean noCash = false;
            for (int i = 0; i < accountsList.size(); i++) {
                if (accountsList.get(i).accountNumber == Long.parseLong(cbDepositFromAccount.getValue().toString())) {
                    if (accountsList.get(i).cashInAccount < Long.parseLong(depositAmount.getText())) {
                        alertPopup("Det finns inte tillräckligt med likvida medel på kontot för att ta ut beloppet!", "Bankomat");
                        noCash = true;
                        break;
                    }
                    Accounts updateAccount = new Accounts(accountsList.get(i).accountNumber, accountsList.get(i).getPersonalID(), (accountsList.get(i).cashInAccount - Long.parseLong(depositAmount.getText())));
                    accountsList.set(i, updateAccount);
                    logTransfer(String.valueOf(accountsList.get(i).accountNumber), "00000000000", Long.parseLong(depositAmount.getText()), "UTTAG");
                }
            }
            if (noCash == false) {
                updateAccountListComboBoxes();
                saveAccountsToFile();
                depositPopup.setVisible(true);
            }
        } else {
            alertPopup("Antingen har belopp eller uttagskonto inte angivits","Bankomat");
        }
    }

    public void cashButtonClicked (Event e) {
        depositPopup.setVisible(false);
    }

    /**
     * button for creating new account. checks if checkbox is checked.
     * @author Michael
     * @param e
     * @throws FileNotFoundException
     */
    public void createNewAccountButtonClicked (Event e) throws FileNotFoundException {
        if (checkboxCreateNewAccount.isSelected()) {
            if (createNewAccount()) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Ditt nya konto skapades!", ButtonType.OK);
                alert.showAndWait();
            }
        } else {
          Alert alert = new Alert(Alert.AlertType.WARNING, "Du måste ha läst villkoren för nytt konto!", ButtonType.OK);
          alert.showAndWait();
        }
    }

    /**
     * event when the "create new account" button is clicked
     * @author Michael
     * @return
     * @throws FileNotFoundException
     */
    public boolean createNewAccount() throws FileNotFoundException {
        Random randomizer = new Random();
        String randomAccount = "";
        for (int i = 0; i < 5; i++){
            randomAccount = randomAccount + String.valueOf(randomizer.nextInt(10));
        }
        Scanner fileReader = new Scanner(nextAccountNumber); // Unikt kontonummer.
        if (fileReader.hasNextLine()) {
            nextAccountNumb = Integer.parseInt(fileReader.nextLine());
        } else {
            nextAccountNumb = 140337; // Första unika kontonummerserien.
        }
        newAccountNumber = nextAccountNumb + randomAccount;
        Accounts addAccount = new Accounts(Long.parseLong(newAccountNumber), loggedinID, 0);
        accountsList.add(addAccount);
        String item = newAccountNumber.substring(0,4) + " "
                + newAccountNumber.substring(4,6) + " "
                + newAccountNumber.substring(6) + " \t"
                + nfSv.format("0");
        myAccountList.getItems().add(item);
        cbTransferFromAccount.getItems().add(newAccountNumber);
        cbTransferToAccount.getItems().add(newAccountNumber);
        cbTransferOtherFromAccount.getItems().add(newAccountNumber);
        cbDepositFromAccount.getItems().add(newAccountNumber);
        saveNewAccountToFile(newAccountNumber, loggedinID,0);
        return true;
    }

    /**
     * creates the new account and saves it to the csv file
     * @author Michael
     * @param accountnumber newly created account number
     * @param personalid personalId of account holder
     * @param cash amount in new account
     */
    public void saveNewAccountToFile(String accountnumber, String personalid, double cash) {
        try {
            Files.write(Paths.get("files/accounts.acc"), (accountnumber+";"+personalid+";"+cash+"\n").getBytes(), StandardOpenOption.APPEND);
        }
        catch (Exception e) {
            System.out.println("Problem vid skrivning till kontofil");
        }
    }

    /**
     * fetch all existing accounts from csv file -> accountsList
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
    /**
     * fetch all pending payments for customer
     * @author Michael
     */
    public void fetchMyPendingPayments() {
        try {
            Scanner fr = new Scanner(pendingPaymentsFile);
            allPendingPayments.clear();
            pendingTransfersList.getItems().clear();
            while (fr.hasNextLine()) {
                String rowsFromFile = fr.nextLine();
                String[] readerParts = rowsFromFile.split(";");
                Pending pp = new Pending(readerParts[0], readerParts[1], Double.parseDouble(readerParts[2]), readerParts[3], readerParts[4]);
                allPendingPayments.add(pp);
            }
            for (int i = 0; i< allPendingPayments.size(); i++) {
                for (int j = 0; j < accountsList.size(); j++) {
                    if (String.valueOf(accountsList.get(j).accountNumber).equals(allPendingPayments.get(i).fromAccount) && accountsList.get(j).getPersonalID().equals(loggedinID)) {
                        pendingTransfersList.getItems().add(allPendingPayments.get(i).transferDate + "\t" + allPendingPayments.get(i).fromAccount + "\t" + allPendingPayments.get(i).toAccount + "\t" + nfSv.format(allPendingPayments.get(i).transferAmount));
                    }
                }
            }
        } catch (Exception e) {
            System.out.print(e.getMessage() +"\n" + e.getStackTrace());
            return;
        }
    }
    public static void fetchTransferFailMessages() {
        try {
            Scanner fr = new Scanner(paymentProblemsFile);
            transferFailMassages.clear();
            while (fr.hasNextLine()) {
                String rowsFromFile = fr.nextLine();
                String[] readerParts = rowsFromFile.split(";");
                TransferMessages tm = new TransferMessages(readerParts[0], readerParts[1], Double.parseDouble(readerParts[2]), readerParts[3], readerParts[4], readerParts[5], Integer.parseInt(readerParts[6]));
                transferFailMassages.add(tm);
            }
        } catch (Exception e) {
            System.out.print(e.getMessage() +"\n" + e.getStackTrace());
        }
    }

    public static void updatePendingPaymentsFile() throws IOException {
        try {
            FileWriter fw = new FileWriter(pendingPaymentsFile);
            for (int i=0; i< allPendingPayments.size(); i++) {
                Pending pp = allPendingPayments.get(i);
                fw.write(pp.fromAccount+";"
                        +pp.toAccount+";"
                        +pp.transferAmount+";"
                        +pp.transferDate+";"
                        +pp.transferMessage+"\n");
            }
            fw.close();
        } catch (Exception e) {

        }
    }
    public static void updateTransferFailFile() throws IOException {
        try {
            FileWriter fw = new FileWriter(paymentProblemsFile);
            for (int i=0; i< transferFailMassages.size(); i++) {
                TransferMessages tm = transferFailMassages.get(i);
                fw.write(tm.fromAccount+";"
                +tm.toAccount+";"
                +tm.transferAmount+";"
                +tm.orgDate+";"
                +tm.newDate+";"
                +tm.transferMessage+";"
                +tm.messageID+"\n");
            }
            fw.close();
        } catch (Exception e) {

        }
    }

    /**
     * action event for combo menu
     * @author Michael
     * @param actionEvent
     */
    public void comboMenu(ActionEvent actionEvent) {
        hideAllGroups();
        if (comboMenu.getValue().equals("Överföring eget konto")){
            groupTransferOwnAccount.setVisible(true);
        } else if (comboMenu.getValue().equals("Registrera betalning")) {
            groupTransferOtherAccount.setVisible(true);
        } else if (comboMenu.getValue().equals("Öppna nytt konto")) {
            groupCreateNewAccount.setVisible(true);
        } else if (comboMenu.getValue().equals("Uttag från konto")) {
            groupDeposit.setVisible(true);
        }  else if (comboMenu.getValue().equals("Kommande betalningar")) {
            groupDeleteTransfer.setVisible(true);
            fetchMyPendingPayments();
        }
    }

    /**
     * Checks if message for pending payments contains ";"
     * @author Marcus
     * @param msg is the input message.
     */

    public static boolean checkMessage(String msg){
        for(int i = 0; i < msg.length(); i++){
            if(String.valueOf(msg.charAt(i)).equals(";")){
                //System.out.println("Otillåtet tecken.");  // line edited by michael
                return true;
            }
        }
        if (msg.length() > 20) return true; // line edit by michael
        return false;
    }

}