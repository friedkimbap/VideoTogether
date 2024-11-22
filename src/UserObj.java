import java.io.Serial;
import java.io.Serializable;

public class UserObj implements Serializable {
  @Serial
  private static final long serialVersionUID = 2L;


  String name;
  String chat;
  int chat_mode;
}
