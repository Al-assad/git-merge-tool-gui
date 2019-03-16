package site.assad.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("/gui.fxml"));
        primaryStage.setTitle("Git 分支合并测试小工具  - by yulinying");
        primaryStage.setScene(new Scene(root, 900, 800));
        primaryStage.setResizable(false);
        primaryStage.setOnCloseRequest(event-> System.exit(0));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
