package MyModel;


import Client.*;
import IO.MyCompressorOutputStream;
import IO.MyDecompressorInputStream;
import Server.*;
import StageUtills.Sounds;
import View.View;
import Work.DoWork;
import algorithms.mazeGenerators.Maze;
import algorithms.mazeGenerators.MyMazeGenerator;
import algorithms.mazeGenerators.Position;
import algorithms.search.Solution;
import javafx.application.Platform;
import javafx.scene.input.KeyCode;
import sun.awt.Mutex;

import java.io.*;
import java.net.InetAddress;
import java.util.Observable;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class MyModel extends Observable implements IModel {

    private Mutex generateMutex , solveMutex;
    private Solution solution;
    private ExecutorService threadPool ;
    private Maze maze ;
    private Server genServer ;
    private Server solveServer;
    private int charRowPos = 0 ;
    private int charColPos = 0 ;
    private int rowEndIndex , colEndIndex;

    public MyModel(){
        this.threadPool = Executors.newCachedThreadPool();
        this.generateMutex = new Mutex();
        this.solveMutex = new Mutex();
    }


    @Override
    public void generateMaze(int rows, int cols) {

        threadPool.execute(()-> {
            generateMutex.lock();
            try {
                Client client = new Client(InetAddress.getLocalHost(),5402,new IClientStrategy(){

                    @Override
                    public void clientStrategy(InputStream inputStream, OutputStream outputStream){

                        try{
                        ObjectOutputStream toServer = new ObjectOutputStream(outputStream);
                        ObjectInputStream fromServer = new ObjectInputStream(inputStream);
                        toServer.flush();
                        int[] dim = new int[]{rows,cols};
                        toServer.writeObject(dim);
                        toServer.flush();
                        byte[] compressedMaze = (byte[]) fromServer.readObject();
                        InputStream in = new MyDecompressorInputStream(new ByteArrayInputStream(compressedMaze));
                        byte[] decompressed = new byte[rows*cols+12];
                        in.read(decompressed);
                        maze = new Maze(decompressed);
                        charRowPos = maze.getStartPosition().getRowIndex();
                        charColPos = maze.getStartPosition().getColumnIndex();
                        rowEndIndex = maze.getGoalPosition().getRowIndex();
                        colEndIndex = maze.getGoalPosition().getColumnIndex();
                        } catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                });
                client.communicateWithServer();
            }catch (Exception e){
                System.out.println("eror in generating maze with " + rows +" rows ans " + cols + " cols");
                e.printStackTrace();
            }
            update();
            generateMutex.unlock();
       });

    }


    private void update(){
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                setChanged();
                notifyObservers();
            }
        });
    }

    @Override
    public int moveCharacter(KeyCode movement) {

        int result = 1 ;

        switch (movement) {
            case UP:
                if(possibleMove(charRowPos-1,charColPos)) {
                    charRowPos--;
                    result = 0;
                }
                break;
            case DOWN:
                if(possibleMove(charRowPos+1,charColPos)) {
                    charRowPos++;
                    result = 0;
                }
                break;
            case RIGHT:
                if(possibleMove(charRowPos,charColPos+1)) {
                    charColPos++;
                    result = 0;
                }
                break;
            case LEFT:
                if(possibleMove(charRowPos,charColPos-1)) {
                    charColPos--;
                    result = 0 ;
                }
                break;
            case NUMPAD8:
                if(possibleMove(charRowPos-1,charColPos)) {
                    charRowPos--;
                    result = 0;
                }
                break;
            case NUMPAD2:
                if(possibleMove(charRowPos+1,charColPos)) {
                    charRowPos++;
                    result = 0;
                }
                break;
            case NUMPAD6:
                if(possibleMove(charRowPos,charColPos+1)) {
                    charColPos++;
                    result = 0;
                }
                break;
            case NUMPAD4:
                if(possibleMove(charRowPos,charColPos-1)) {
                    charColPos--;
                    result = 0 ;
                }
                break;
            case NUMPAD1://down left
                if(possibleSidewayMove(charRowPos+1,charColPos,charRowPos+1,charColPos-1)||
                possibleSidewayMove(charRowPos,charColPos-1,charRowPos+1,charColPos-1)){
                    charRowPos++;
                    charColPos--;
                    result = 0;
                }
                break;
            case NUMPAD3://down right
                if(possibleSidewayMove(charRowPos+1,charColPos,charRowPos+1,charColPos+1)||
                possibleSidewayMove(charRowPos,charColPos+1,charRowPos+1,charColPos+1)){
                    charColPos++;
                    charRowPos++;
                    result = 0 ;
                }
                break;
            case NUMPAD7://up left
                if(possibleSidewayMove(charRowPos-1,charColPos,charRowPos-1,charColPos-1) ||
                possibleSidewayMove(charRowPos,charColPos-1,charRowPos-1,charColPos-1)){
                    charColPos--;
                    charRowPos--;
                    result = 0 ;
                }
                break;
            case NUMPAD9:
                if(possibleSidewayMove(charRowPos-1,charColPos,charRowPos-1,charColPos+1)||
                possibleSidewayMove(charRowPos,charColPos+1,charRowPos-1,charColPos+1)){
                    charColPos++;
                    charRowPos--;
                    result=0;
                }
                break;
        }

            setChanged();
            notifyObservers();
            return result;
    }

    public boolean possibleSidewayMove(int destRowNormal , int destColNormal , int destRowSide , int destColSide){

        int[][] matrix = this.maze.getMaze();

        if(destRowNormal < 0 || destRowSide < 0 || destRowNormal >= matrix[0].length || destRowSide >= matrix[0].length)
            return false;

        if(destColNormal < 0 || destColSide < 0 || destColNormal >= matrix.length || destColSide >= matrix.length)
            return false;

        if(matrix[destRowNormal][destColNormal] == 1 || matrix[destRowSide][destColSide] == 1)
            return false;

        return true;
    }

    public boolean possibleMove(int destRow , int destCol){
        if(destRow >= this.maze.getMaze()[0].length || destRow < 0 || destCol < 0 || destCol >= this.maze.getMaze().length)
            return false;

        return maze.getMaze()[destRow][destCol] == 0 || (destRow == rowEndIndex && destCol == colEndIndex) ;
    }

    @Override
    public void setCharRowPos(int row) {
        this.charRowPos = row;
        update();
    }

    @Override
    public void setCharColPos(int col) {
        this.charColPos = col;
        //update();
    }

    @Override
    public void solveMaze() {
        if(maze == null)
            return;

        threadPool.execute(() ->{
            this.solveMutex.lock();
            final Solution[] sol = new Solution[1];
            try{
                Client client = new Client(InetAddress.getLocalHost(), 5403, new IClientStrategy() {
                    @Override
                    public void clientStrategy(InputStream inputStream, OutputStream outputStream) {
                        try {
                            ObjectOutputStream toServer = new ObjectOutputStream(outputStream);
                            ObjectInputStream fromServer = new ObjectInputStream(inputStream);
                            toServer.flush();
                            toServer.writeObject(maze);
                            toServer.flush();
                            Solution solution = (Solution)fromServer.readObject();
                            sol[0] = solution;

                        }catch (Exception e){

                        }
                 }
                });
                client.communicateWithServer();
                this.solution = sol[0];
                this.solveMutex.unlock();
            }catch (Exception e){

            }});

    }

    public Solution getSolution(){
        Solution ans ;
        this.solveMutex.lock();
        ans = this.solution;
        this.solveMutex.unlock();
        return ans;
    }

    @Override
    public int getRowEndIndex() {
        int ans ;
        generateMutex.lock();
        ans = this.rowEndIndex;
        generateMutex.unlock();
        return ans;
    }

    @Override
    public int getColEndIndex() {
        int ans ;
        generateMutex.lock();
        ans = this.colEndIndex;
        generateMutex.unlock();

        return ans;
    }

    @Override
    public void shutDownServers() {
        this.genServer.stop();
        this.solveServer.stop();
    }

    @Override
    public int[][] getMaze() {
        return maze.getMaze();
    }


    @Override
    public int getCharRowPosition() {
        return this.charRowPos;
    }

    @Override
    public int getCharColPosition() {
        return this.charColPos;
    }

    @Override
    public void startServers() {

        genServer = new Server(5402, 2000, new ServerStrategyGenerateMaze());
        solveServer = new Server(5403,2000,new ServerStrategySolveSearchProblem());

        genServer.start();
        solveServer.start();
    }


    /**
     *
     * @param FilePath full path
     * @return game saved or not
     */
    public boolean saveGame(String FilePath){
        MyCompressorOutputStream compressor = null;
        ByteArrayOutputStream compressedMazeBuffer=null;
        FileOutputStream toFile = null;
        ObjectOutputStream toFileAsObject = null;
        File saveFile = new File(FilePath);
        try {
            toFile = new FileOutputStream(FilePath);
            compressedMazeBuffer = new ByteArrayOutputStream();
            compressor = new MyCompressorOutputStream(compressedMazeBuffer);

            toFile.flush();
            compressedMazeBuffer.flush();
            compressor.flush();


            //hold the compressed maze in buffer
            byte[] mazeInBytes = this.maze.toByteArray();
            compressor.write(mazeInBytes);

            compressor.flush();

            compressor.close();

            //wrap the toFile stream
            toFileAsObject = new ObjectOutputStream(toFile);
            toFileAsObject.flush();

            //first write the position.
            Position currPos = new Position(charRowPos,charColPos);

            toFileAsObject.writeObject(currPos);

            toFileAsObject.flush();

            //then write the compressed maze as an object;
            toFileAsObject.writeObject(compressedMazeBuffer.toByteArray());


            //need to add music\animation saver

        } catch (FileNotFoundException e) {
            System.out.println("illegal path,unable to save file");
            return false;
        } catch (IOException e) {
            System.out.println("I/O exception,unable to save file");
            return false;
        }

        try {
            toFileAsObject.close();
            compressedMazeBuffer.close();
            toFile.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("error closing streams , yet game saved");
        }
        return true;
    }


    public boolean loadGame(String pathToFile){
        FileInputStream fromFile = null;
        ByteArrayInputStream compressedMazeBuffer = null;
        MyDecompressorInputStream decompressor = null;
        ObjectInputStream fromFileAsObject = null;

        //get first object, the position . then get byte maze to buffer , decompress , and rebuild it.
        try {
            File loadFile = new File(pathToFile);
            System.out.println(pathToFile);
            if(!loadFile.exists()){
                System.out.println(pathToFile);
                return false;
            }
            fromFile = new FileInputStream(loadFile);
            fromFileAsObject = new ObjectInputStream(fromFile);


            Position positionFromFile =(Position) fromFileAsObject.readObject();
            byte[] byteMazeFromFile = (byte[]) fromFileAsObject.readObject();
            compressedMazeBuffer = new ByteArrayInputStream(byteMazeFromFile);
            byte[] decompressedMazeInBytes = new byte [10000] ;
            decompressor = new MyDecompressorInputStream(compressedMazeBuffer);
            decompressor.read(decompressedMazeInBytes);
            Maze decompressedMaze = new Maze(decompressedMazeInBytes);
            this.maze = decompressedMaze;
            this.charRowPos=positionFromFile.getRowIndex();
            this.charColPos=positionFromFile.getColumnIndex();
            decompressor.close();
            compressedMazeBuffer.close();
            fromFileAsObject.close();
            fromFile.close();
            setChanged();
            notifyObservers();
            return true;



        } catch (FileNotFoundException e) {
            System.out.println("file not found");
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            System.out.println("class not found");
        }

        return false;


    }

}
