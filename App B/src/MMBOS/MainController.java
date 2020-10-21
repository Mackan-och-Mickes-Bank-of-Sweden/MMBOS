package MMBOS;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;

public class MainController {
    public MainController(){
        mc = this;
    }
    public static File accountsFile = new File("../files/accounts.acc");
    public static ArrayList <Accounts> accountsList = new ArrayList<>();
    public static String loggedinID;
    Main main=null;
    public static MainController mc = null;

    @FXML
    private Label loggedinText;

    @FXML
    private ListView myAccountList;

    @FXML
    public void setLoggedin(String passingInfo) {
        loggedinText.setText(passingInfo);
        loggedinText.setVisible(false);
        loggedinID=passingInfo;
        fetchAccounts();
        myAccountList.setMaxSize(300,600);
        for (int i=0; i<accountsList.size(); i++) {
            String item = String.valueOf(accountsList.get(i).accountNumber).substring(0,4) + " "
                    + String.valueOf(accountsList.get(i).accountNumber).substring(4,6) + " "
                    + String.valueOf(accountsList.get(i).accountNumber).substring(6) + "\t Saldo: "
                    + accountsList.get(i).cashInAccount + "kr";
            myAccountList.getItems().add(item);
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
}