// 2071173 김영민
import java.io.Serial;
import java.io.Serializable;
import javax.swing.ImageIcon;

public class VideoObj implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    public final static int MODE_LOGIN = 0x1;
    public final static int MODE_LOGOUT = 0x2;

    public final static int MODE_NOTSTARTED = -1;
    public final static int MODE_ENDED = 0;
    public final static int MODE_PLAYING = 1;
    public final static int MODE_PAUSED = 2;
    public final static int MODE_BUFFERING = 3;
    public final static int MODE_VIDEOCUED = 5;


    String userID;
    int mode;
    String message;
    ImageIcon image;
    long size;

    public VideoObj(String userID, int mode, String message, ImageIcon image, long size){
        this.userID = userID;
        this.mode = mode;
        this.message = message;
        this.image = image;
        this.size = size;
    }

    public VideoObj(String userID, int mode, String message, ImageIcon icon){
        this(userID,mode,message,icon,0);
    }

    public VideoObj(String userID, int mode, String message){
        this(userID,mode,message,null);
    }

//  public ChatMsg(String userID, int mode, ImageIcon icon){
//    this(userID,mode,null,icon);
//  }

    public VideoObj(String userID, int mode, String filename, long size){
        this(userID,mode,filename,null, size);
    }

    public VideoObj(String userID, int mode){
        this(userID,mode,null,null);
    }

}
