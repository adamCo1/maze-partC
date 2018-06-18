package View;

import StageUtills.Sounds;
import StageUtills.StageHolder;
import StageUtills.IHolder;
import ViewModel.MyViewModel;
import Work.DoWork;
import algorithms.search.Solution;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import java.io.FileInputStream;
import java.net.URL;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;
import java.util.ResourceBundle;

public class View implements Observer, IVIew, Initializable {


    private int timePassedSoFar ;
    private Thread timeThread = new Thread(() -> updateTime());
    private Solution solution;
    private String skin;
    private boolean cancelGeneration = false;
    @FXML
    private MyViewModel viewModel;
    public MazeDisplayer mazeDisplayer;
    public javafx.scene.control.TextField txtfld_rowsNum;
    public javafx.scene.control.TextField txtfld_columnsNum;
    public javafx.scene.control.Label lbl_rowsNum , showTime , time_remaining;
    public javafx.scene.control.Label lbl_columnsNum;
    public javafx.scene.control.Button btn_generateMaze , btn_solve , btn_hideSolution;
    public javafx.scene.control.MenuItem menu_item_save;

    public void setViewModel(MyViewModel viewModel) {
        this.viewModel = viewModel;
        bindProperties(viewModel);
        Sounds.getInstance().playBackgroundMusic();
    }

    private void updateTime(){
        while(true){
            timePassedSoFar = Integer.parseInt(getTimePassed());
        }
    }

    private void bindProperties(MyViewModel viewModel) {
        lbl_rowsNum.textProperty().bind(viewModel.characterPositionRow);
        lbl_columnsNum.textProperty().bind(viewModel.characterPositionColumn);
        this.showTime.textProperty().bind(viewModel.TimePassed);
    }

    @Override
    public void update(Observable o, Object arg) {
        if (o == viewModel) {
            displayMaze(viewModel.getMze());
            if(this.mazeDisplayer.checkSolved())
                endGame();
            btn_generateMaze.setDisable(false);
        }
    }

    @Override
    public void displayMaze(int[][] maze) {

        this.mazeDisplayer.setMaze(maze);
        int characterPositionRow = this.viewModel.getCharPositionRowIndex();
        int characterPositionColumn = this.viewModel.getCharacterColIndex();
        mazeDisplayer.setCharacterPosition(characterPositionRow, characterPositionColumn);
        this.characterPositionRow.set(characterPositionRow + "");
        this.characterPositionColumn.set(characterPositionColumn + "");

    }

    /**
     * maze is solved . ask the user how to keep on going
     */

    //region Handle

    public void closeFromMenu(){
        this.viewModel.shutDownServers();
        System.exit(0);
    }

    private void endGame(){
        Sounds sounds = Sounds.getInstance();
        sounds.stopAllIngameMusic();
        FXMLLoader loader = new FXMLLoader();
        Stage endGameStage = new Stage();
        try {
            Parent root = loader.load(getClass().getResource("GameDone.fxml").openStream());
            Scene endScene = new Scene(root,600,400);
            endScene.getStylesheets().add((getClass().getResource("View.css").toExternalForm()));
            GameDoneController gameDone = loader.getController();
            gameDone.setViewModel(this.viewModel);
            endGameStage.setScene(endScene);
            IHolder holder = StageHolder.getInstance();
            holder.registerObject(endGameStage);
            endGameStage.showAndWait();
            endGameStage.close();
            sounds.playBackgroundMusic();
        }catch (Exception e){
            e.printStackTrace();
        }

        this.mazeDisplayer.clearBoard();

    }


    public void handleZoomOnMaze(ScrollEvent event){

        if(event.getSource().equals(this.mazeDisplayer)) {
                this.mazeDisplayer.handleZoom(event);
            }
        event.consume();
    }

    /**
     *
     * move character with the mouse
     * @param event mouse event
     */
    public void handleMouseDragEnter(MouseEvent event){

        if(event.getSource().equals(this.mazeDisplayer) && this.mazeDisplayer != null) {

            double cellWidth = this.mazeDisplayer.getCellWidth();
            double cellHeight = this.mazeDisplayer.getCellHeight();
            double x = event.getX() , y = event.getY();

            try {
                int charRow = Integer.parseInt(this.characterPositionRow.get());
                int charCol = Integer.parseInt(this.characterPositionColumn.get());

            if(x >= charCol*cellWidth-cellWidth && x <= charCol*cellWidth+cellWidth*2 &&
                    y >= charRow*cellHeight-cellHeight && y <= charRow*cellHeight+cellHeight*2){

                if (x > charCol * cellWidth + cellWidth) { // move right
                    if (this.viewModel.legalCharacterMove(charRow, charCol + 1)) {
                        this.viewModel.setCharacterRowIndex(charRow);
                        this.viewModel.setCharacterColIndex(charCol+1);
                    }
                }

                else if (x < charCol * cellWidth) {//left
                    if (this.viewModel.legalCharacterMove(charRow, charCol - 1)) {
                        this.viewModel.setCharacterRowIndex(charRow);
                        this.viewModel.setCharacterColIndex(charCol-1);
                    }
                }
                else if (y < charRow * cellHeight){
                    if (this.viewModel.legalCharacterMove(charRow - 1, charCol)){
                        this.viewModel.setCharacterRowIndex(charRow-1);
                        this.viewModel.setCharacterColIndex(charCol);
                    }
                }
                else if(y > charRow*cellHeight+cellHeight){
                    if(this.viewModel.legalCharacterMove(charRow+1,charCol)) {
                        this.viewModel.setCharacterRowIndex(charRow+1);
                        this.viewModel.setCharacterColIndex(charCol);
                    }
                }
            }

        }catch (Exception e) {
                System.out.println("error with mouse movement");
            }
        }
        event.consume();
    }


    public void generateMaze() {
        Sounds sound = Sounds.getInstance();
        sound.getInstance().playClickMusic();

        try {
            int rows = Integer.valueOf(this.txtfld_rowsNum.getText());
            int cols = Integer.valueOf(this.txtfld_columnsNum.getText());
            this.btn_generateMaze.setDisable(true);//choose the skins
            handleChooseSkins();
            if(this.cancelGeneration) {
                this.cancelGeneration = false;
                this.btn_generateMaze.setDisable(false);
                return;
            }
            sound.getInstance().stopBackgroundMusic();
            sound.getInstance().stopAllIngameMusic();
            if(this.skin.equals("Gothic")){
                sound.getInstance().playGothicIngameMusic();
            }else if(this.skin.equals("Classic")){
                sound.getInstance().playIngameMusic();
            }else if(this.skin.equals("Nostalgic")){
                sound.getInstance().playNostalgicIngameMusic();
            }


            this.viewModel.generateMaze(rows, cols);
            try {
                Thread.sleep(10);//give the executor enough time to start the generating process
            } catch (Exception e) {
                e.printStackTrace();
            }
            this.mazeDisplayer.setEndPosition(this.viewModel.getRowEndPosition(), this.viewModel.getColEndPosition());
            DoWork task = new DoWork();
            task.setViewModel(this.viewModel);
            new Thread(task).start();
            this.solution = null;
            this.mazeDisplayer.setSolution(null);
        }catch (Exception e){//generate error in number format screen
            Stage errorFormat = new Stage();
            AnchorPane pane = new AnchorPane();
            Scene errorScene = new Scene(pane,500,300);
            errorScene.getStylesheets().add(getClass().getResource("View.css").toExternalForm());

            Button btn_back = new Button("Back");
            btn_back.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    errorFormat.close();
                }
            });
            setButtonSettings(btn_back,200,250,100,30);
            Label errorLabel = setLabel(150,120,"Error! entered invalid number !");

            pane.getChildren().addAll(errorLabel,btn_back);
            errorFormat.setScene(errorScene);
            errorFormat.initModality(Modality.APPLICATION_MODAL);
            errorFormat.showAndWait();

        }

    }


    private void setButtonSettings(Button button , double x , double y , double width , double height){
        button.setLayoutX(x);
        button.setLayoutY(y);
        button.setPrefWidth(width);
        button.setPrefHeight(height);
    }

    private Label setLabel(double x , double y , String text ){
        Label label = new Label(text);
        label.setLayoutX(x);
        label.setLayoutY(y);
        return label;
    }

    private void handleChooseSkins(){
        try {
            final String[] skin_name = {""};
            Stage skinsStage = new Stage();
            AnchorPane pane = new AnchorPane();
            Scene skinscene = new Scene(pane, 800, 500);
            skinscene.getStylesheets().add(getClass().getResource("View.css").toExternalForm());
            Sounds sound = Sounds.getInstance();

            Button btn_classic = new Button("Classic");
            Button btn_gothic = new Button("Gothic");
            Button btn_Nostalgic = new Button("Nostalgic");

            setButtonSettings(btn_classic, 100, 450, 100, 30);
            setButtonSettings(btn_gothic, 350, 450, 100, 30);
            setButtonSettings(btn_Nostalgic, 600, 450, 100, 30);

            FileInputStream classicInput = new FileInputStream("resources/Images/character_Classic_skin.png");
            FileInputStream nostalgicInput = new FileInputStream("resources/Images/character_Nostalgic_skin.png");
            FileInputStream gothicInput = new FileInputStream("resources/Images/character_Gothic_skin.png");
            Image classicChoice = new Image(classicInput);
            ImageView classicView = new ImageView(classicChoice);
            Image nostalgicChoice = new Image(nostalgicInput);
            ImageView nostalgicView = new ImageView(nostalgicChoice);
            Image gothicChoice = new Image(gothicInput);
            ImageView gothicView = new ImageView(gothicChoice);
            setImageView(classicView,40,150,200,250);
            setImageView(nostalgicView,550,150,180,200);
            setImageView(gothicView,290,150,200,200);
            btn_classic.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    sound.playClickMusic();
                    setSkin("Classic");
                    skinsStage.close();
                }
            });

            btn_gothic.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    sound.playClickMusic();
                    setSkin("Gothic");
                    skinsStage.close();
                }
            });

            btn_Nostalgic.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    sound.playClickMusic();
                    setSkin("Nostalgic");
                    skinsStage.close();
                }
            });

            skinsStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent event) {
                    sound.getInstance().playClickMusic();
                    cancelGeneration = true;
                }
            });

            pane.getChildren().addAll(btn_classic, btn_gothic, btn_Nostalgic,classicView,nostalgicView,gothicView);
            skinsStage.initModality(Modality.APPLICATION_MODAL);
            skinsStage.setScene(skinscene);
            skinsStage.showAndWait();

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void setImageView(ImageView imageView,double x , double y , double width , double height){
        imageView.setLayoutX(x);
        imageView.setLayoutY(y);
        imageView.setFitWidth(width);
        imageView.setFitHeight(height);

    }

    private void setSkin(String skin_name){
        this.skin = skin_name;
        this.mazeDisplayer.handleSkinSetting(skin_name);
    }

    public void solveMaze(ActionEvent actionEvent) {
        Sounds.getInstance().playClickMusic();
        this.btn_solve.setDisable(true);
        this.viewModel.solveMaze();
        try{
            Thread.sleep(10);//give time to start solving
        }catch (Exception e){
            e.printStackTrace();
        }
        this.solution = this.viewModel.getSolution();
        if(this.solution == null){//error. no maze to solve
            Stage noMazeErrorStage = new Stage();
            AnchorPane pane = new AnchorPane();
            Scene scene = new Scene(pane,500,300);
            scene.getStylesheets().add(getClass().getResource("View.css").toExternalForm());
            Button btn_back = new Button("Back");
            btn_back.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    noMazeErrorStage.close();
                }
            });
            setButtonSettings(btn_back,200,250,100,30);
            Label label = setLabel(100,120,"No maze to solve. Please generate one first.");

            pane.getChildren().addAll(btn_back,label);
            noMazeErrorStage.setScene(scene);
            noMazeErrorStage.initModality(Modality.APPLICATION_MODAL);
            noMazeErrorStage.showAndWait();

        }else
            this.mazeDisplayer.setSolution(this.solution);

        this.btn_solve.setDisable(false);
    }

    public void setResizeEvent(Scene scene) {
        scene.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneWidth, Number newSceneWidth) {
                mazeDisplayer.handleResizingWidth(newSceneWidth.doubleValue() - oldSceneWidth.doubleValue());
            }
        });
        scene.heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneHeight, Number newSceneHeight) {
                mazeDisplayer.handleesizingHeight(newSceneHeight.doubleValue()-oldSceneHeight.doubleValue());

            }
        });

    }

    public void handleHideSolution(){
        Sounds.getInstance().playClickMusic();
        btn_hideSolution.setDisable(true);
        this.mazeDisplayer.setSolution(null);
        btn_hideSolution.setDisable(false);
    }

    public void KeyPressed(KeyEvent keyEvent) {
        int result = viewModel.moveCharacter(keyEvent.getCode());
        if(result == 0) {//so normal movement has been made
            Sounds.getInstance().playMovement1Sound();
        }else{//bumped into a wall
            Random rnd = new Random();
            double ans = rnd.nextDouble();
            if(ans <=0.33)
                Sounds.getInstance().playWall1Sound();
            if(ans > 0.33 && ans <= 0.66)
                Sounds.getInstance().playWall2Sound();
            if(ans > 0.66 && ans <= 1)
                Sounds.getInstance().playWall3Sound();
            }
        keyEvent.consume();
    }
    //endregion


    //region String Property for Binding

    public StringProperty timeRemaining = new SimpleStringProperty("0");

    public StringProperty characterPositionRow = new SimpleStringProperty();

    public StringProperty characterPositionColumn = new SimpleStringProperty();

    public String getCharacterPositionRow() {
        return characterPositionRow.get();
    }

    public StringProperty characterPositionRowProperty() {
        return characterPositionRow;
    }

    public String getCharacterPositionColumn() {
        return characterPositionColumn.get();
    }

    public String getTimePassed(){
        return timeRemaining.get();
    }

    public void setTimePassed(int seconds){
        this.timeRemaining.set(seconds+"");
    }

    public StringProperty characterPositionColumnProperty() {
        return characterPositionColumn;
    }

    public void About(ActionEvent actionEvent) {
        try {
            Stage stage = new Stage();
            stage.setTitle("AboutController");
            FXMLLoader fxmlLoader = new FXMLLoader();
            Parent root = fxmlLoader.load(getClass().getResource("About.fxml").openStream());
            Scene scene = new Scene(root, 400, 350);
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL); //Lock the window until it closes
            stage.show();
        } catch (Exception e) {

        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        DoWork task = new DoWork();
        task.setViewModel(this.viewModel);
      //  new Thread(task).start();
    }
    //endregion

    @FXML
    private void saveGame(ActionEvent actionEvent){
        FXMLLoader loader = new FXMLLoader();
        Stage saveGameStage = new Stage();
        saveGameStage.initModality(Modality.APPLICATION_MODAL);
        saveGameStage.setTitle("Save menu");
        try {
            Parent root = loader.load(getClass().getResource("GameSave.fxml").openStream());
            Scene saveScene = new Scene(root,600,400);
            saveScene.getStylesheets().add(getClass().getResource("View.css").toExternalForm());
            GameSaveController gameSave = loader.getController();
            gameSave.setViewModel(this.viewModel);
            saveGameStage.setScene(saveScene);
            StageHolder holder = StageHolder.getInstance();
            holder.registerObject(saveGameStage);
            saveGameStage.showAndWait();
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @FXML
    private void loadGame(ActionEvent actionEvent){
        FXMLLoader loader = new FXMLLoader();
        Stage loadGameStage = new Stage();
        loadGameStage.initModality(Modality.APPLICATION_MODAL);
        loadGameStage.setTitle("Load menu");
        try {
            Parent root = loader.load(getClass().getResource("GameLoad.fxml").openStream());
            Scene loadScene = new Scene(root,600,400);
            loadScene.getStylesheets().add(getClass().getResource("View.css").toExternalForm());
            GameLoadController gameLoad = loader.getController();
            gameLoad.setViewModel(this.viewModel);
            loadGameStage.setScene(loadScene);
            StageHolder holder = StageHolder.getInstance();
            holder.registerObject(loadGameStage);
            loadGameStage.showAndWait();

        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
