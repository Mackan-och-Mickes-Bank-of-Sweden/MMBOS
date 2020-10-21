package MMBOS;

import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Scanner;

public class LoginController {
    public static LoginController lc;
    public LoginController(){
        lc = this;
    }
    Main main;
    public static File customersFile = new File("../files/customers.cus");
    public static ArrayList <Customers> customersList = new ArrayList<>();

    @FXML
    public TextField personalIDField;
    public TextField passwordField;
    public Button loginButton;


    public void loginButtonClicked(Event e) throws NoSuchAlgorithmException, IOException {

        fetchCustomers();

        for (int i=0; i<customersList.size(); i++) {
            StringBuilder sb = md5Pass(passwordField.getText());
            if (personalIDField.getText().equals(customersList.get(i).getPersonalID()) && customersList.get(i).passWd.equals(sb.toString())) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Hej " + customersList.get(i).firstName + " " + customersList.get(i).lastName + ", tack för att du logga in!", ButtonType.OK);
                alert.setTitle("** M M B O S **");
                alert.setHeaderText("Inloggning till banken");
                String path2 = "src/MMBOS/startup.mp3";
                Media mp3Startup2 = null;
                MediaPlayer mediaPlayer2 = null;
                mp3Startup2 = new Media(new File(path2).toURI().toString());
                mediaPlayer2 = new MediaPlayer(mp3Startup2);
                mediaPlayer2.setAutoPlay(true);
                alert.showAndWait();
                main.appWin.setScene(main.mapScenes.get("mainScene"));
                MainController.mc.setLoggedin(customersList.get(i).getPersonalID());
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
