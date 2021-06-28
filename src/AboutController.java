package sample;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextArea;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.ResourceBundle;

public class AboutController implements Initializable {
    @FXML private Label AboutLabel;
    @FXML private JFXTextArea AboutTextArea;
    @FXML private JFXButton AboutDev;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        AboutDev.setOnAction(e -> {
            Alert aboutDev = new Alert(AlertType.INFORMATION," Developed and tested by IIT2018032 ", ButtonType.OK);
            aboutDev.setHeaderText("Information regarding the program here is legit and genuine");
            aboutDev.setTitle("About Developer");
            aboutDev.setResizable(false);
            aboutDev.showAndWait();
        });
    }

}
