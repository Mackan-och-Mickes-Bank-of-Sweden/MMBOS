package app_b;

import javafx.fxml.FXML;
import javafx.event.Event;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;


public class Controller {
    public TextField personalIDField=null;
    public TextField passwordField=null;
    @FXML
    public void loginButtonClicked(Event e){
    if (personalIDField.getText().equals("demo") && passwordField.getText().equals("demo")){
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "Hej Michael, tack för att du logga in!", ButtonType.OK);
        alert.setTitle("Meddelanderuta");
        alert.setHeaderText("Inloggning till banken");
        alert.showAndWait();

    }
    }

}
