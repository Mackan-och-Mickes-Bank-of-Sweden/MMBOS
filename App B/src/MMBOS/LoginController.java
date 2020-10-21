package MMBOS;

import javafx.fxml.FXML;
import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Scanner;


public class LoginController {
    Main main=null;
    public static File customersFile = new File("../files/customers.cus");
    public static ArrayList <Customers> customersList = new ArrayList<>();
    public TextField personalIDField;
    public TextField passwordField;
    public Button loginButton;
    public String loggedinID = "";
    //MainController mainC = null;

    public void loginButtonClicked(Event e) throws NoSuchAlgorithmException, IOException {

        fetchCustomers();

        for (int i=0; i<customersList.size(); i++) {

            StringBuilder sb = md5Pass(passwordField.getText());
            if (personalIDField.getText().equals(customersList.get(i).getPersonalID()) && customersList.get(i).passWd.equals(sb.toString())) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Hej " + customersList.get(i).firstName + " " + customersList.get(i).lastName + ", tack för att du logga in!", ButtonType.OK);
                alert.setTitle("** M M B O S **");
                alert.setHeaderText("Inloggning till banken");
                alert.showAndWait();
                loggedinID = customersList.get(i).getPersonalID();
                main.appWin.setScene(main.mapScenes.get("mainScene"));
                MainController.mc.setLoggedin(loggedinID);
                break;
            }
        }
    }

    private static StringBuilder md5Pass(String text) throws NoSuchAlgorithmException {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        md5.update(text.getBytes());
        byte[] md5Password = md5.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : md5Password) {
            sb.append(String.format("%02x", b & 0xff));
        }
        return sb;
    }

    public static void fetchCustomers() {
        try {
            Scanner customerFileReader = new Scanner(customersFile);
            while (customerFileReader.hasNextLine()) {
                String rowsFromFile = customerFileReader.nextLine();
                String[] readerParts = rowsFromFile.split(";");
                Customers readCustomer = new Customers(readerParts[0], readerParts[1], readerParts[2], readerParts[3]);
                customersList.add(readCustomer);
            }
        } catch (Exception e) {
            System.out.print(e.getMessage() +"\n" + e.getStackTrace());
            return;
        }
    }

}
