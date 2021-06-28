package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("MainWindow.fxml"));
        primaryStage.setTitle("Assignment 2");
        primaryStage.getIcons().add(new Image("/res/Wildcat.png"));   // ** HARDCODED
        primaryStage.setScene(new Scene(root));
        primaryStage.setResizable(false);

        primaryStage.setOnCloseRequest(e ->{
            e.consume();
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION," Sure, you want to exit ?", ButtonType.YES,ButtonType.NO);
            alert.setTitle("Are you sure?");
            alert.setResizable(false);
            alert.showAndWait();
            if(alert.getResult() == ButtonType.YES)
                primaryStage.close();
        });

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
