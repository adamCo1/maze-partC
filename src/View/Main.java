package View;

import MyModel.MyModel;
import StageUtills.Sounds;
import StageUtills.StageHolder;
import StageUtills.IHolder;
import ViewModel.MyViewModel;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
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
                Sounds.getInstance().playClickMusic();
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                Optional<ButtonType> result = alert.showAndWait();
                if (result.get() == ButtonType.OK){
                    primaryStage.close();
                    exit();
                } else {
                    // ... user chose CANCEL or closed the dialog
                    windowEvent.consume();
                }
            }
        });
    }


    public static void main(String[] args) {
        launch(args);
    }
}
