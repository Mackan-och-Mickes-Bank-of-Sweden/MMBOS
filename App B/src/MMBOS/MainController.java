package MMBOS;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.io.File;
import java.util.ArrayList;
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

    @FXML
    private Label loggedinText;
    @FXML
    private ListView myAccountList;
    @FXML
    private ComboBox comboMenu;
    @FXML
    private ComboBox fromAccount;
    @FXML
    private ComboBox toAccount;
    @FXML
    private Label headerText;
    @FXML
    private Button doTransferButton;
    @FXML
    private TextField transferMessage;
    @FXML
    private TextField transferAmount;
    @FXML
    private DatePicker datepickerTransfer;
    @FXML
    private Pane groupTransferOwnAccount;
    @FXML
    private Pane groupTransferOtherAccount;
    @FXML
    private ComboBox fromAccountOther;
    @FXML
    private MenuItem menuLoggaut;
    @FXML
    private Pane groupCreateNewAccount;
    @FXML
    private Button createNewAccountButton;
    @FXML
    private CheckBox checkboxCreateNewAccount;

    @FXML
    public void setLoggedin(String passingInfo, String name) {
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

    public void createNewAccountButtonClicked (Event e) {
    if (checkboxCreateNewAccount.isSelected()) {

    } else {
        Alert alert = new Alert(Alert.AlertType.WARNING, "Du måste ha läst villkoren för kontot och kryssa i rutan!", ButtonType.OK);
        alert.showAndWait();
    }
    }
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
    public void setMenuLoggaut() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "Du loggas nu ut ur systemet!", ButtonType.OK);
        alert.setTitle("** M M B O S **");
        alert.setHeaderText("Välkommen åter!");
        alert.showAndWait();
        main.appWin.setScene(main.mapScenes.get("loginScene"));
    }
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