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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

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
    public static int nextAccountNumb;
    public static String newAccountNumber;

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

    public void doTransferBetweenAccounts(long fromAccountNumber, long toAccountNumber, double amountToTransfer) throws IOException {
        LocalDate dateForTransfer;
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
                        break;
                    }
                    Accounts updateAccount = new Accounts(fromAccountNumber, accountsList.get(i).getPersonalID(), (accountsList.get(i).cashInAccount - amountToTransfer));
                    accountsList.set(i, updateAccount);
                }
                if (accountsList.get(i).accountNumber == toAccountNumber) {
                    Accounts updateAccount = new Accounts(toAccountNumber, accountsList.get(i).getPersonalID(), (accountsList.get(i).cashInAccount + amountToTransfer));
                    accountsList.set(i, updateAccount);
                }
            }
            updateAccountListComboBoxes();
            saveAccountsToFile();
            alertPopup("Överföringen mellan dina konton utfördes!", "Kontoöverföring");
        } else {
            String message;
            if (transferMessage.getText().isEmpty()) {
                message = "";
            } else {
                message = transferMessage.getText();
            }
            try {
                Files.write(Paths.get("../files/pendingpayments.pay"), (fromAccountNumber+";"+toAccountNumber+";"+amountToTransfer+";"+dateForTransfer+";"+message+"\n").getBytes(), StandardOpenOption.APPEND);
                alertPopup("Överföringen har lagts till och kommer att genomföras det valda datumet","Kontoöverföring");
            }
            catch (Exception e) {
                System.out.println("Problem vid skrivning till överföringsfilen.");
            }
        }
    }

    public void doTransferOtherAccount(long fromAccountNumber, long toAccountNumber, double amountToTransfer) throws IOException {
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
                        break;
                    }
                    Accounts updateAccount = new Accounts(fromAccountNumber, accountsList.get(i).getPersonalID(), (accountsList.get(i).cashInAccount - amountToTransfer));
                    accountsList.set(i, updateAccount);
                }
                if (accountsList.get(i).accountNumber == toAccountNumber) {
                    Accounts updateAccount = new Accounts(toAccountNumber, accountsList.get(i).getPersonalID(), (accountsList.get(i).cashInAccount + amountToTransfer));
                    accountsList.set(i, updateAccount);
                }
            }
            updateAccountListComboBoxes();
            saveAccountsToFile();
            alertPopup("Överföringen/Betalningen har utförts!", "Betalning / Överföring till annans konto");
        } else {
            String message;
            if (transferMessageOther.getText().isEmpty()) {
                message = "";
            } else {
                message = transferMessageOther.getText();
            }
            try {
                Files.write(Paths.get("files/pendingpayments.pay"), (fromAccountNumber+";"+toAccountNumber+";"+amountToTransfer+";"+dateForTransfer+";"+message+"\n").getBytes(), StandardOpenOption.APPEND);
                alertPopup("Överföringen/Betalningen har lagts till och kommer att genomföras det valda datumet","Betalning / Överföring till annans konto");
            }
            catch (Exception e) {
                System.out.println("Problem vid skrivning till överföringsfilen.");
            }
        }
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
                    + String.valueOf(accountsList.get(i).accountNumber).substring(6) + " \tDisponibelt belopp: "
                    + accountsList.get(i).cashInAccount + "kr";
            myAccountList.getItems().add(item);
        }
    }

    private void hideAllGroups() {
        groupTransferOwnAccount.setVisible(false);
        groupTransferOtherAccount.setVisible(false);
        groupCreateNewAccount.setVisible(false);
        groupDeposit.setVisible(false);
    }

    public void menuDoTranferOtherClicked (Event e) {
        hideAllGroups();
        comboMenu.setValue("Gör en betalning till annans konto");
        groupTransferOtherAccount.setVisible(true);
    }
    public void menuDoTransferClicked (Event e) {
        hideAllGroups();
        comboMenu.setValue("Gör en överföring mellan egna konton");
        groupTransferOwnAccount.setVisible(true);
    }
    public void menuDepositClicked (Event e) {
        hideAllGroups();
        comboMenu.setValue("Uttag från konto");
        groupDeposit.setVisible(true);
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
    public void loginIn(String passingInfo, String name) {
        hideAllGroups();
        clearAllLists();
        loggedinText.setText(passingInfo);
        loggedinText.setVisible(false);
        loggedinID=passingInfo;
        fetchAccounts();
        myAccountList.setPrefWidth(300);
        for (int i = 0; i< accountsList.size(); i++) {
            if (!accountsList.get(i).getPersonalID().equals(loggedinID)) continue;
            String item = String.valueOf(accountsList.get(i).accountNumber).substring(0,4) + " "
                    + String.valueOf(accountsList.get(i).accountNumber).substring(4,6) + " "
                    + String.valueOf(accountsList.get(i).accountNumber).substring(6) + " \tDisponibelt belopp: "
                    + accountsList.get(i).cashInAccount + "kr";
            myAccountList.getItems().add(item);
            cbTransferFromAccount.getItems().add(accountsList.get(i).accountNumber);
            cbTransferToAccount.getItems().add(accountsList.get(i).accountNumber);
            cbTransferOtherFromAccount.getItems().add(accountsList.get(i).accountNumber);
            cbDepositFromAccount.getItems().add(accountsList.get(i).accountNumber);
        }
        headerText.setText(name+"s konton i banken");
        comboMenu.getItems().clear();
        comboMenu.getItems().addAll(
                "Öppna nytt konto",
                "Uttag från konto",
                "Gör en överföring mellan egna konton",
                "Gör en betalning till annans konto"
        );
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
                + newAccountNumber.substring(6) + " \tDisponibelt belopp: "
                + "0.0kr";
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

    /**
     * action event for combo menu
     * @author Michael
     * @param actionEvent
     */
    public void comboMenu(ActionEvent actionEvent) {
        hideAllGroups();
        if (comboMenu.getValue().equals("Gör en överföring mellan egna konton")){
            groupTransferOwnAccount.setVisible(true);
        } else if (comboMenu.getValue().equals("Gör en betalning till annans konto")) {
            groupTransferOtherAccount.setVisible(true);
        } else if (comboMenu.getValue().equals("Öppna nytt konto")) {
            groupCreateNewAccount.setVisible(true);
        } else if (comboMenu.getValue().equals("Uttag från konto")) {
            groupDeposit.setVisible(true);
        }
    }

}