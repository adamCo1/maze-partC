package ImageFactory;

import javafx.scene.image.Image;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public abstract class ImageFactory implements ISkinsImages{

    protected String skin_name;
    protected Image background ;

    public ImageFactory(String skin_name){
        this.skin_name = skin_name;
        try {
            this.background = new Image(new FileInputStream("resources/Images/background.jpg"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public Image getBackground(){
        return this.background;
    }

    public Image getWallImage(){
        Image ans = null ;
        try {
           ans = new Image(new FileInputStream("resources/Images/wall_"+this.skin_name+"_skin.jpg"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return ans;
    }


    public Image getCharacterImage(){
        Image ans = null;
        try {
            ans = new Image(new FileInputStream("resources/Images/character_"+this.skin_name+"_skin.png"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return ans;
    }


    public Image getPathWayImage(){
        Image ans = null;
        try {
            ans = new Image(new FileInputStream("resources/Images/pathway_"+this.skin_name+"_skin.png"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return ans;
    }

    public Image getSolutionPathImage(){
        Image ans = null;
        try {
            ans = new Image(new FileInputStream("resources/Images/solutionpath_"+this.skin_name+"_skin.png"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return ans;
    }

    public Image getEndImage(){
        Image ans = null;
        try {
            ans = new Image(new FileInputStream("resources/Images/end_"+this.skin_name+"_skin.png"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return ans;

    }

}
