package MMBOS;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;

import java.io.File;
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

        setGlobalEventHandler(rootFirst);

    }

    private void setGlobalEventHandler(Node root) {
        root.addEventHandler(KeyEvent.KEY_PRESSED, ev -> {
            if (ev.getCode() == KeyCode.ENTER) {
                LoginController.lc.loginButton.fire();
                ev.consume();
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
