package app_b;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("appb.fxml"));
        Parent root = loader.load();
        primaryStage.setTitle("** M M B O S **");
        primaryStage.setScene(new Scene(root, 800, 400));
        primaryStage.show();
        Controller controllerRef = loader.getController();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
