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


    String name;
    String user_num;

}
