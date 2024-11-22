import java.io.Serial;
import java.io.Serializable;
import java.util.List;

public class VideoListObj  implements Serializable {
  @Serial
  private static final long serialVersionUID = 0L;

  public final static int MODE_TEXT = 0;
  public final static int MODE_IMG = 1;

  List<VideoObj> videoList;

}
