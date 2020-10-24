package MMBOS;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;

import java.io.File;
import java.io.FileNotFoundException;
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

    public static File accountsFile = new File("../files/accounts.acc");
    public static ArrayList <Accounts> customersAccountsList = new ArrayList<>();
    public static ArrayList <Accounts> allAccountsList = new ArrayList<>();
    public static String loggedinID;
    public static File nextAccountNumber = new File("../files/nextaccountnumber.acc");
    public static int nextAccountNumb;
    public static String newAccountNumber;

    @FXML private Label loggedinText;
    @FXML private ListView myAccountList;
    @FXML private ComboBox comboMenu;
    @FXML private ComboBox cbTransferFromAccount;
    @FXML private ComboBox cbTransferToAccount;
    @FXML private Label headerText;
    @FXML private Button doTransferButton;
    @FXML private TextField transferMessage;
    @FXML private TextField transferAmount;
    @FXML private DatePicker datepickerTransfer;
    @FXML private Pane groupTransferOwnAccount;
    @FXML private Pane groupTransferOtherAccount;
    @FXML private ComboBox cbTransferFromAccountOther;
    @FXML private MenuItem menuLoggaut;
    @FXML private Pane groupCreateNewAccount;
    @FXML private Button createNewAccountButton;
    @FXML private CheckBox checkboxCreateNewAccount;
    @FXML private Pane groupDeposit;
    @FXML private Button depositButton;
    @FXML private TextField depositAmount;
    @FXML private ComboBox cbDepositFromAccount;
    @FXML private AnchorPane depositPopup;
    @FXML private Button cashButton;

    //TODO:doTransferBetweenAccounts - spara nya saldon till filen

    public void doTransferBetweenAccounts(long fromAccountNumber, long toAccountNumber, double amountToTransfer) {
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
            for (int i = 0; i < customersAccountsList.size(); i++) {
                if (customersAccountsList.get(i).accountNumber == fromAccountNumber) {
                    if (customersAccountsList.get(i).cashInAccount < amountToTransfer) {
                        alertPopup("Det finns inte tillräckligt med likvida medel på kontot för att utföra överföringen!", "Kontoöverföring");
                        break;
                    }
                    Accounts updateAccount = new Accounts(fromAccountNumber, customersAccountsList.get(i).getPersonalID(), (customersAccountsList.get(i).cashInAccount - amountToTransfer));
                    customersAccountsList.set(i, updateAccount);
                }
                if (customersAccountsList.get(i).accountNumber == toAccountNumber) {
                    Accounts updateAccount = new Accounts(toAccountNumber, customersAccountsList.get(i).getPersonalID(), (customersAccountsList.get(i).cashInAccount + amountToTransfer));
                    customersAccountsList.set(i, updateAccount);
                }
            }
            updateAccountListComboBoxes();
            alertPopup("Överföringen mellan dina konton utfördes!", "Kontoöverföring");
        } else {

            //TODO: spara överföringen till csv fil för senare överföring via timer App C
        }
    }

    public void updateAccountListComboBoxes() {
        myAccountList.getItems().clear();
        for (int i = 0; i< customersAccountsList.size(); i++) {
            String item = String.valueOf(customersAccountsList.get(i).accountNumber).substring(0,4) + " "
                    + String.valueOf(customersAccountsList.get(i).accountNumber).substring(4,6) + " "
                    + String.valueOf(customersAccountsList.get(i).accountNumber).substring(6) + " \tSaldo: "
                    + customersAccountsList.get(i).cashInAccount + "kr";
            myAccountList.getItems().add(item);
        }
    }

    public void doTransferButtonClicked (Event e) {
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
        groupTransferOwnAccount.setVisible(false);
        groupTransferOtherAccount.setVisible(false);
        groupCreateNewAccount.setVisible(false);
        groupDeposit.setVisible(false);

        loggedinText.setText(passingInfo);
        loggedinText.setVisible(false);
        loggedinID=passingInfo;
        fetchAccounts();
        myAccountList.setMaxSize(300,600);
        for (int i = 0; i< customersAccountsList.size(); i++) {
            String item = String.valueOf(customersAccountsList.get(i).accountNumber).substring(0,4) + " "
                    + String.valueOf(customersAccountsList.get(i).accountNumber).substring(4,6) + " "
                    + String.valueOf(customersAccountsList.get(i).accountNumber).substring(6) + " \tSaldo: "
                    + customersAccountsList.get(i).cashInAccount + "kr";
            myAccountList.getItems().add(item);
            cbTransferFromAccount.getItems().add(customersAccountsList.get(i).accountNumber);
            cbTransferToAccount.getItems().add(customersAccountsList.get(i).accountNumber);
            cbTransferFromAccountOther.getItems().add(customersAccountsList.get(i).accountNumber);
            cbDepositFromAccount.getItems().add(customersAccountsList.get(i).accountNumber);
        }
        headerText.setText(name+"s konton i banken");
        comboMenu.getItems().addAll(
                "Öppna nytt konto",
                "Insättning till konto",
                "Uttag från konto",
                "Gör en överföring mellan egna konton",
                "Gör en betalning till annans konto"
        );
    }

    public void depositButtonClicked (Event e) {
        if (depositAmount.getText() != null && !cbDepositFromAccount.getSelectionModel().isEmpty()) {
            depositPopup.setVisible(true);
        } else {
            alertPopup("Antingen har belopp eller uttagskonto inte angivits","Uttag från konto");
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
          Alert alert = new Alert(Alert.AlertType.WARNING, "Du måste ha läst villkoren för kontot och kryssa i rutan!", ButtonType.OK);
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
        customersAccountsList.add(addAccount);
        allAccountsList.add(addAccount);
        String item = newAccountNumber.substring(0,4) + " "
                + newAccountNumber.substring(4,6) + " "
                + newAccountNumber.substring(6) + " \tSaldo: "
                + "0.0kr";
        myAccountList.getItems().add(item);
        cbTransferFromAccount.getItems().add(newAccountNumber);
        cbTransferToAccount.getItems().add(newAccountNumber);
        cbTransferFromAccountOther.getItems().add(newAccountNumber);
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
            Files.write(Paths.get("../files/accounts.acc"), (accountnumber+";"+personalid+";"+cash+"\n").getBytes(), StandardOpenOption.APPEND);
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
            customersAccountsList.clear();
            allAccountsList.clear();
            while (accountsFileReader.hasNextLine()) {
                String rowsFromFile = accountsFileReader.nextLine();
                String[] readerParts = rowsFromFile.split(";");
                Accounts readAccountforAll = new Accounts(Long.parseLong(readerParts[0]), readerParts[1], Double.parseDouble(readerParts[2]));
                allAccountsList.add(readAccountforAll);
                if (!readerParts[1].equals(loggedinID)) continue;
                Accounts readAccount = new Accounts(Long.parseLong(readerParts[0]), readerParts[1], Double.parseDouble(readerParts[2]));
                customersAccountsList.add(readAccount);
            }
        } catch (Exception e) {
            System.out.print(e.getMessage() +"\n" + e.getStackTrace());
            return;
        }
    }

    /**
     * log out from system, change scene to login
     * @author Michael
     */
    public void setMenuLoggaut() {
        alertPopup("Du loggas nu ut ur systemet!", "Välkommen åter!");
        main.appWin.setScene(main.mapScenes.get("loginScene"));
    }

    /**
     * action event for combo menu
     * @author Michael
     * @param actionEvent
     */
    public void comboMenu(ActionEvent actionEvent) {
       if (comboMenu.getValue().equals("Gör en överföring mellan egna konton")){
           groupTransferOwnAccount.setVisible(true);
       } else {
           groupTransferOwnAccount.setVisible(false);
       }
       if (comboMenu.getValue().equals("Gör en betalning till annans konto")) {
           groupTransferOtherAccount.setVisible(true);
       } else {
           groupTransferOtherAccount.setVisible(false);
       }
       if (comboMenu.getValue().equals("Öppna nytt konto")) {
           groupCreateNewAccount.setVisible(true);
       } else {
           groupCreateNewAccount.setVisible(false);
       }
        if (comboMenu.getValue().equals("Uttag från konto")) {
            groupDeposit.setVisible(true);
        } else {
            groupDeposit.setVisible(false);
        }
    }

}