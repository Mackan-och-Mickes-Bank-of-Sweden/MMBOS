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

/**
 * @author Michael
 */
public class LoginController {
    public static LoginController lc;
    public LoginController(){
        lc = this;
    }
    Main main;
    public static File customersFile = new File("../files/customers.cus");
    public static ArrayList <Customers> customersList = new ArrayList<>();
    public static String sessionName;

    @FXML
    public TextField personalIDField;
    public TextField passwordField;
    public Button loginButton;

    /**
     * Event when login button is clicked
     * Loads main scene if login details are correct.
     * @author Michael
     * @param e
     * @throws NoSuchAlgorithmException
     * @throws IOException
     */
    public void loginButtonClicked(Event e) throws NoSuchAlgorithmException, IOException {

        fetchCustomers();
        Boolean loginOk = false;
        for (int i=0; i<customersList.size(); i++) {
            StringBuilder sb = md5Pass(passwordField.getText());
            if (personalIDField.getText().equals(customersList.get(i).getPersonalID()) && customersList.get(i).passWd.equals(sb.toString())) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Hej " + customersList.get(i).firstName + " " + customersList.get(i).lastName + ", tack för att du logga in!", ButtonType.OK);
                alert.setTitle("** M M B O S **");
                alert.setHeaderText("Inloggning till banken");
                //playSound("src/MMBOS/startup.mp3");
                alert.showAndWait();
                main.appWin.setScene(main.mapScenes.get("mainScene"));
                MainController.mc.loginIn(customersList.get(i).getPersonalID(), customersList.get(i).firstName + " " + customersList.get(i).lastName);
                personalIDField.setText("");
                passwordField.setText("");
                loginOk = true;
                break;

            }
        }
        if (loginOk == false) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Du har angivit fel personnummer eller lösenord!", ButtonType.OK);
            alert.setTitle("** M M B O S **");
            alert.setHeaderText("Inloggning till banken");
            alert.showAndWait();
        }
    }

    private void playSound(String path) {
        Media mp3Startup = null;
        MediaPlayer mediaPlayer = null;
        mp3Startup = new Media(new File(path).toURI().toString());
        mediaPlayer = new MediaPlayer(mp3Startup);
        mediaPlayer.setAutoPlay(true);
    }

    /**
     *
     * @author Michael
     * @param text input string
     * @return md5 password
     * @throws NoSuchAlgorithmException
     */
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

    /**
     * Gets all the customers from csv file
     * @author Michael
     */
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
