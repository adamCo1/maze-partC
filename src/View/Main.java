package View;

import MyModel.MyModel;
import StageUtills.Sounds;
import StageUtills.StageHolder;
import StageUtills.IHolder;
import ViewModel.MyViewModel;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.util.Observer;
import java.util.Optional;

public class Main extends Application {

    public MyViewModel viewModel;

    @Override
    public void start(Stage primaryStage) throws Exception {
        IHolder holder = StageHolder.getInstance();
        holder.registerObject(primaryStage);
        MyModel model = new MyModel();
        model.startServers();
        this.viewModel = new MyViewModel(model);
        model.addObserver(viewModel);
        //--------------
        primaryStage.setTitle("Maze Game");
        FXMLLoader fxmlLoader = new FXMLLoader();
        Parent root = fxmlLoader.load(getClass().getResource("View.fxml").openStream());
        Scene scene = new Scene(root, 1450, 950);
        scene.getStylesheets().add(getClass().getResource("View.css").toExternalForm());
        primaryStage.setScene(scene);
        //--------------
        IVIew view = fxmlLoader.getController();
        view.setResizeEvent(scene);
        view.setViewModel(viewModel);
        viewModel.addObserver((Observer) view);
        //--------------


        SetStageCloseEvent(primaryStage);
        primaryStage.show();

        //DoWork task = new DoWork();
        //task.setViewModel(viewModel);
        //new Thread(task).start();

    }

    private void exit(){
        this.viewModel.shutDownServers();
        System.exit(0);
    }

    private void SetStageCloseEvent(Stage primaryStage) {
        final boolean[] close = {false};

        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            public void handle(WindowEvent windowEvent) {
                Sounds sound = Sounds.getInstance();
                sound.playClickMusic();
                Stage confirmStage = new Stage();
                AnchorPane pane = new AnchorPane();
                Scene confirmScene = new Scene(pane,400,300);
                confirmScene.getStylesheets().add(getClass().getResource("View.css").toExternalForm());

                Button btn_ok = new Button("Ok");
                Button btn_cancel = new Button("Cancel");
                Label label = new Label("Are you sure you want to quit?");

                btn_ok.setLayoutX(100);
                btn_ok.setLayoutY(200);
                btn_ok.setPrefWidth(90);
                btn_ok.setPrefHeight(40);

                btn_cancel.setLayoutX(220);
                btn_cancel.setLayoutY(200);
                btn_cancel.setPrefWidth(90);
                btn_cancel.setPrefHeight(40);

                label.setLayoutX(100);
                label.setLayoutY(100);

                btn_ok.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        sound.playClickMusic();
                        primaryStage.close();
                        exit();
                    }
                });

                btn_cancel.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        sound.playClickMusic();
                        windowEvent.consume();
                        confirmStage.close();
                    }
                });

                confirmStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                    @Override
                    public void handle(WindowEvent event) {
                        sound.playClickMusic();
                        windowEvent.consume();
                    }
                });

                pane.getChildren().addAll(btn_cancel,btn_ok,label);
                confirmStage.setScene(confirmScene);
                confirmStage.initModality(Modality.APPLICATION_MODAL);
                confirmStage.showAndWait();

            }
        });
    }


    public static void main(String[] args) {
        launch(args);
    }
}
