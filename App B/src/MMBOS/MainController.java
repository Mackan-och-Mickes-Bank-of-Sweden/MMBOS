package MMBOS;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.Scanner;

public class MainController {
    public static MainController mc;
    public MainController(){
        mc = this;
    }
    Main main;

    public static File accountsFile = new File("../files/accounts.acc");
    public static ArrayList <Accounts> accountsList = new ArrayList<>();
    public static String loggedinID;
    public static File nextAccountNumber = new File("../files/nextaccountnumber.acc");
    public static int nextAccountNumb;
    public static String newAccountNumber;

    @FXML private Label loggedinText;
    @FXML private ListView myAccountList;
    @FXML private ComboBox comboMenu;
    @FXML private ComboBox fromAccount;
    @FXML private ComboBox toAccount;
    @FXML private Label headerText;
    @FXML private Button doTransferButton;
    @FXML private TextField transferMessage;
    @FXML private TextField transferAmount;
    @FXML private DatePicker datepickerTransfer;
    @FXML private Pane groupTransferOwnAccount;
    @FXML private Pane groupTransferOtherAccount;
    @FXML private ComboBox fromAccountOther;
    @FXML private MenuItem menuLoggaut;
    @FXML private Pane groupCreateNewAccount;
    @FXML private Button createNewAccountButton;
    @FXML private CheckBox checkboxCreateNewAccount;

    //TODO:doTransferBetweenAccounts - skapa error vid otillräckliga medel, datumkontroll, spara nya saldon till filen

    public void doTransferBetweenAccounts(long fromAccountNumber, long toAccountNumber, double transferAmount) {
        for (int i=0; i<accountsList.size(); i++) {
            if (accountsList.get(i).accountNumber==fromAccountNumber) {
                Accounts updateAccount = new Accounts(fromAccountNumber, accountsList.get(i).getPersonalID(),(accountsList.get(i).cashInAccount-transferAmount));
                accountsList.set(i, updateAccount);
            }
            if (accountsList.get(i).accountNumber==toAccountNumber) {
                Accounts updateAccount = new Accounts(toAccountNumber, accountsList.get(i).getPersonalID(),(accountsList.get(i).cashInAccount+transferAmount));
                accountsList.set(i, updateAccount);
            }
        }
        myAccountList.getItems().clear();
        for (int i=0; i<accountsList.size(); i++) {
            String item = String.valueOf(accountsList.get(i).accountNumber).substring(0,4) + " "
                    + String.valueOf(accountsList.get(i).accountNumber).substring(4,6) + " "
                    + String.valueOf(accountsList.get(i).accountNumber).substring(6) + " \tSaldo: "
                    + accountsList.get(i).cashInAccount + "kr";
            myAccountList.getItems().add(item);
        }
    }

    public void doTransferButtonClicked (Event e) {
        if (fromAccount.hasProperties() && toAccount.hasProperties()) {
            doTransferBetweenAccounts(Long.parseLong(fromAccount.getValue().toString()), Long.parseLong(toAccount.getValue().toString()), Double.parseDouble(transferAmount.getText()));
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Överföringen mellan dina konton utfördes!", ButtonType.OK);
            alert.setTitle("** M M B O S **");
            alert.setHeaderText("Kontoöverföring");
            alert.showAndWait();
        }
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

        loggedinText.setText(passingInfo);
        loggedinText.setVisible(false);
        loggedinID=passingInfo;
        fetchAccounts();
        myAccountList.setMaxSize(300,600);
        for (int i=0; i<accountsList.size(); i++) {
            String item = String.valueOf(accountsList.get(i).accountNumber).substring(0,4) + " "
                    + String.valueOf(accountsList.get(i).accountNumber).substring(4,6) + " "
                    + String.valueOf(accountsList.get(i).accountNumber).substring(6) + " \tSaldo: "
                    + accountsList.get(i).cashInAccount + "kr";
            myAccountList.getItems().add(item);
            fromAccount.getItems().add(accountsList.get(i).accountNumber);
            toAccount.getItems().add(accountsList.get(i).accountNumber);
            fromAccountOther.getItems().add(accountsList.get(i).accountNumber);
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
        accountsList.add(addAccount);
        String item = newAccountNumber.substring(0,4) + " "
                + newAccountNumber.substring(4,6) + " "
                + newAccountNumber.substring(6) + " \tSaldo: "
                + "0.0kr";
        myAccountList.getItems().add(item);
        fromAccount.getItems().add(newAccountNumber);
        toAccount.getItems().add(newAccountNumber);
        fromAccountOther.getItems().add(newAccountNumber);
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
     * fetch all the users accounts from csv file
     * @author Michael
     */
    public static void fetchAccounts() {
        try {
            Scanner accountsFileReader = new Scanner(accountsFile);
            accountsList.clear();
            while (accountsFileReader.hasNextLine()) {
                String rowsFromFile = accountsFileReader.nextLine();
                String[] readerParts = rowsFromFile.split(";");
                if (!readerParts[1].equals(loggedinID)) continue;
                Accounts readAccount = new Accounts(Long.parseLong(readerParts[0]), readerParts[1], Double.parseDouble(readerParts[2]));
                accountsList.add(readAccount);


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
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "Du loggas nu ut ur systemet!", ButtonType.OK);
        alert.setTitle("** M M B O S **");
        alert.setHeaderText("Välkommen åter!");
        alert.showAndWait();
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
    }

}