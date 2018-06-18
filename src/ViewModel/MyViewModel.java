package ViewModel;

import MyModel.IModel;
import MyModel.MyModel;
import Work.DoWork;
import algorithms.search.Solution;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.Initializable;
import javafx.scene.input.KeyCode;

import java.io.File;
import java.util.*;

public class MyViewModel extends Observable implements Observer, IMazeViewModel {


    Thread timeThread = new Thread(() -> timePassedSoFar());
    public int passedSeconeds;
    private Timer timer ;
    private TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
            passedSeconeds++;
        }
    };
    private IModel model ;
    private int characterRowIndex , characterColIndex;

    public StringProperty TimePassed = new SimpleStringProperty("0");
    public StringProperty characterPositionRow = new SimpleStringProperty("1"); //For Binding
    public StringProperty characterPositionColumn = new SimpleStringProperty("1"); //For Binding
    public StringProperty filePathToSave = new SimpleStringProperty("untitled_maze");
    public StringProperty filePathToLoad = new SimpleStringProperty("untitled_maze");

    public MyViewModel(IModel model){
        this.passedSeconeds = 0;
        this.model = model;
        timer = new Timer();
        startTimer();
    }

    public void updateValues(){
       while(true){
           characterRowIndex = model.getCharRowPosition();
           characterColIndex = model.getCharColPosition();
           characterPositionRow.set(characterRowIndex + "");
           characterPositionColumn.set(characterColIndex + "");
       }
    }

    private void startTimer(){
        timer.scheduleAtFixedRate(timerTask,1000,1000);
    }

    public void solveMaze(){
        this.model.solveMaze();
    }

    @Override
    public void generateMaze(int rowsNum, int colsNum) {
        this.model.generateMaze(rowsNum,colsNum);
    }

    public int moveCharacter(KeyCode movement){
       return this.model.moveCharacter(movement);
    }

    @Override
    public int[][] getMze() {
        return model.getMaze();
    }

    @Override
    public int getCharRowPosition() {
        return this.model.getCharRowPosition();
    }

    @Override
    public int getCharColPosition() {
        return this.model.getCharColPosition();
    }

    @Override
    public void update(Observable o, Object arg) {

        if(o == this.model) {
            characterRowIndex = model.getCharRowPosition();
            characterColIndex = model.getCharColPosition();
            characterPositionRow.set(characterRowIndex + "");
            characterPositionColumn.set(characterColIndex + "");
            setChanged();
            notifyObservers();
        }
    }

    private void timePassedSoFar(){
        while (true) {
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    System.out.println("hu");
                    setChanged();
                    notifyObservers();
                }
            });
        }
    }

    public boolean legalCharacterMove(int rowDest , int colDest){
        return this.model.possibleMove(rowDest,colDest);
    }

    public int getCharacterColIndex(){
        return this.characterColIndex;
    }

    public int getCharPositionRowIndex(){
        return this.characterRowIndex;
    }

    public void setCharacterRowIndex(int destRow){this.model.setCharRowPos(destRow);}

    public void setCharacterColIndex(int destCol){
        this.model.setCharColPos(destCol);
    }

    public void startTimeThread(){
        this.timeThread.start();
    }


    public Solution getSolution(){
        return model.getSolution();
    }

    public int getRowEndPosition(){
        return this.model.getRowEndIndex();
    }

    public int getColEndPosition(){
        return this.model.getColEndIndex();
    }

    public void shutDownServers(){
        this.model.shutDownServers();
    }

    public boolean saveGame(){
        String gameSaveFolderPath = System.getProperty("user.home")+"\\MazeRunnerGameSaves";
        File gameSaveFolder = new File(gameSaveFolderPath);
        if(!gameSaveFolder.isDirectory())
            gameSaveFolder.mkdir();
        String savePath = gameSaveFolderPath+"\\" +filePathToSave.getValue() +".Maze";
        System.out.println(savePath);
        return ((MyModel)this.model).saveGame(savePath);
    }

    public boolean loadGame(){
        String gameLoadFile =  System.getProperty("user.home")+"\\MazeRunnerGameSaves\\"+filePathToLoad.getValue()+".Maze";
        return ((MyModel)this.model).loadGame(gameLoadFile);
    }


}
