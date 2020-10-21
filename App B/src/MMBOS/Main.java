package MMBOS;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.util.HashMap;

public class Main extends Application {
    HashMap <String, Scene> mapScenes = new HashMap<>();
    Stage appWin = null;
    @Override
    public void start(Stage appWin) throws Exception{
        this.appWin = appWin;
        appWin.setTitle("** M M B O S **");

        FXMLLoader firstFXLoader = new FXMLLoader(getClass().getResource("Login.fxml"));
        Parent rootFirst = firstFXLoader.load();
        LoginController loginController = firstFXLoader.getController();
        loginController.main = this;
        Scene loginScene = new Scene(rootFirst, 800, 400);
        mapScenes.put("loginScene", loginScene);
        appWin.setScene(loginScene);
        appWin.show();

        FXMLLoader secondFXLoader = new FXMLLoader(getClass().getResource("MainApplication.fxml"));
        Parent rootSecond = secondFXLoader.load();
        MainController mainController = secondFXLoader.getController();
        mainController.main = this;
        Scene mainScene = new Scene(rootSecond, 800, 400);
        mapScenes.put("mainScene", mainScene);
        //loginController.mainC = mainController;

    }

    public static void main(String[] args) {
        launch(args);
    }
}
