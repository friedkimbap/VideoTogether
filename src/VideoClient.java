import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javax.swing.border.LineBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import org.w3c.dom.stylesheets.DocumentStyle;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.util.Objects;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.DefaultStyledDocument;

public class VideoClient extends JFrame{
    private JTextField t_input, t_userID;
    private JFXPanel t_display;
    private JTextPane chat_display;
    private StyledDocument document;
    private JLabel startOrpauseButton,b_send, b_select;
    private String serverAddress;
    private int serverPort;
    private Socket socket;
    private ObjectOutputStream out;
    private Thread receiveThread;
    private String uid;
    private ImageIcon startIcon = new ImageIcon("images/btnstart.png");
    private ImageIcon pauseIcon = new ImageIcon("images/btnstop.png");

    private UserObj user; // 고유 user 객체

    private WebEngine webEngine;

    public VideoClient(String serverAddress, int serverPort) throws IOException {
        super("With Talk");
        VideoObj video = new VideoObj("브이로그");
        video.o_name = "김영민";
        video.videoMode=VideoObj.MODE_Create;
        video.user_num=1;
        video.videoTime=0;
        video.name="브이로그";
        video.id="TfatKL3cI7I";
        user = new UserObj("김영민", UserObj.MODE_LogIn, video);


        this.serverAddress = serverAddress;
        this.serverPort = serverPort;

        buildGUI();

        setSize(new Dimension(1200,600));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        addComponentListener(new ComponentAdapter() {
//            public void componentResized(ComponentEvent e) {
//                Platform.runLater(()->{
//                   webEngine.executeScript("setSize("+t_display.getSize().getWidth()+","+t_display.getSize().getHeight()+");");
//                });
//            }
//        });


        setVisible(true);

        connectToServer();
        send(user);
        user.mode = UserObj.MODE_StartVideo;
        send(user);
        user.mode = UserObj.MODE_WatchingVideo;
    }


    public void buildGUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        add(VTPanel(),BorderLayout.SOUTH);
    }

    public JPanel createDisplayPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setPreferredSize(new Dimension(700, 387));
        t_display=  new JFXPanel(); // JFX패널을 사용하면 JFX 애플리케이션을 사용할 수 있음.(원래는 반대인데 이해하기 쉽게) 웹뷰나 웹엔진을 사용 가능

        Platform.runLater(()-> { // JFX 애플리케이션 스레드에서 작업을 실행하는 거임, 웹페이지의 변화를 안전하게 적용해줌, 모든 웹페이지에 변경할 사항들은 이 안에서 일어나게 해야함
            WebView webView = new WebView(); // 웹페이지를 표시하는 화면 JFX 컴포넌트임
            webEngine = webView.getEngine(); // 이거는 웹브라우저의 역할이라고 보면 됨
            webEngine.load(getClass().getResource("./index.html").toExternalForm()); // 이게 index.html을 띄우는거임 클래스의 경로에서 index.html을 찾아내는거임, 그리고 이거를 알맞은 형식으로 바꾸는거
            webEngine.setOnError(event -> System.out.println("JavaScript Error: " + event.getMessage()));
            webEngine.setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/85.0.4183.121 Safari/537.36");
            webView.setDisable(false);

            Scene scene = new Scene(webView); // Scene 클래스 역시 jfx 애플리케이션의 그래픽처리를 담당함
            t_display.setScene(scene);

            addComponentListener(new ComponentAdapter() {
                public void componentResized(ComponentEvent e) {
                    Platform.runLater(()->{
                    webEngine.executeScript("setSize("+(Number)getSize().getWidth()+","+(Number)getSize().getHeight()+");");
                });
            }
        });
        });


        p.add(t_display,BorderLayout.CENTER);
        return p;
    }

    public JPanel createInputPanel() {
        JPanel p = new JPanel(new BorderLayout());
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JFileChooser chooser = new JFileChooser();

        FileNameExtensionFilter txtFilter = new FileNameExtensionFilter("Text File", "txt");
        FileNameExtensionFilter imgFilter = new FileNameExtensionFilter("Image File", "png", "jpg", "gif");

        t_input = new JTextField();
        t_input.setBackground(new Color(219,127,35));
        t_input.setBorder(new LineBorder(new Color(219,127,35)));
        b_send = new JLabel(new ImageIcon("images/sendbutton.png"));
        b_select = new JLabel(new ImageIcon("images/selectbutton.png"));


        p.add(t_input, BorderLayout.CENTER);
        buttonPanel.setBackground(new Color(219,127,35));
        buttonPanel.add(b_send);
        buttonPanel.add(b_select);
        p.add(buttonPanel, BorderLayout.EAST);


        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setMultiSelectionEnabled(false);
        chooser.addChoosableFileFilter(txtFilter);
        chooser.addChoosableFileFilter(imgFilter);
        chooser.setAcceptAllFileFilterUsed(false);


        b_send.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                user.mode = UserObj.MODE_ChatStr;
                if(!(user.chat = t_input.getText()).isEmpty()){
                    send(user);
                    t_input.setText("");
                }
            }
        });
        t_input.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                user.mode = UserObj.MODE_ChatStr;
                if(!(user.chat = t_input.getText()).isEmpty()){
                    send(user);
                    t_input.setText("");
                }
            }
        });

        b_select.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {

                int ret = chooser.showOpenDialog(VideoClient.this);
                if (ret != JFileChooser.APPROVE_OPTION) {
                    JOptionPane.showMessageDialog(VideoClient.this, "파일을 선택하지 않았습니다.");
                    return;
                }

                File selectedFile = chooser.getSelectedFile();
                String fileExtension = selectedFile.getName().substring(selectedFile.getName().lastIndexOf(".") + 1);
                long fileSizeInBytes = selectedFile.length();
                long fileSizeInMB = fileSizeInBytes / (1024 * 1024);
                long maxSizeInMB = 10;

                if (fileSizeInMB > maxSizeInMB) {
                    JOptionPane.showMessageDialog(VideoClient.this, "파일 크기가 너무 큽니다: " + fileSizeInMB + " MB"); return;
                }

                t_input.setText(selectedFile.getAbsolutePath());

                if (fileExtension.equals("txt")) {

                    //sendFile();
                } else if (fileExtension.equals("png")||fileExtension.equals("jpg")||fileExtension.equals("gif")) {
                    //sendImage();
                }

            }
        });

        return p;
    }


    public JPanel VTPanel(){
        JPanel VTPanel = new JPanel(new BorderLayout());
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBackground(new Color(176,46,46));
        leftPanel.setPreferredSize(new Dimension(213,387));

        JLabel logoLabel = new JLabel();
        logoLabel.setIcon(new ImageIcon("images/logo.png"));
        logoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        logoLabel.setBackground(new Color(176,46,46));
        logoLabel.setBorder(new EmptyBorder(30,0,0,0));


        JLabel idLabel = new JLabel(user.name, SwingConstants.CENTER);
        idLabel.setOpaque(true);
        idLabel.setBackground(new Color(176,46,46));
        idLabel.setForeground(Color.WHITE);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setBackground(new Color(176, 46, 46));
        buttonPanel.setBorder(new EmptyBorder(0, 10, 10, 10));

        startOrpauseButton = new JLabel(startIcon);
        startOrpauseButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        startOrpauseButton.setPreferredSize(new Dimension(125, 49));
        startOrpauseButton.setMinimumSize(new Dimension(125, 49));
        startOrpauseButton.setMaximumSize(new Dimension(125, 49));
        startOrpauseButton.setBorder(new EmptyBorder(0, 0, 20, 0));

        JPanel skipPanel = new JPanel();
        skipPanel.setLayout(new BoxLayout(skipPanel, BoxLayout.LINE_AXIS));

        JLabel prevButton = new JLabel(new ImageIcon("images/btnprev.png"));
        prevButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        prevButton.setPreferredSize(new Dimension(57, 29));
        prevButton.setMinimumSize(new Dimension(57, 29));
        prevButton.setMaximumSize(new Dimension(57, 29));


        JLabel nextButton = new JLabel(new ImageIcon("images/btnnext.png"));
        nextButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        nextButton.setPreferredSize(new Dimension(57, 29));
        nextButton.setMinimumSize(new Dimension(57, 29));
        nextButton.setMaximumSize(new Dimension(57, 29));

        JPanel spacePanel = new JPanel();
        spacePanel.setBackground(new Color(176, 46, 46));
        spacePanel.setMaximumSize(new Dimension(11, 29));
        spacePanel.setPreferredSize(new Dimension(11, 29));

        buttonPanel.add(startOrpauseButton);
        skipPanel.add(prevButton);
        skipPanel.add(spacePanel);
        skipPanel.add(nextButton);
        buttonPanel.add(skipPanel);

        leftPanel.add(logoLabel, BorderLayout.NORTH);
        leftPanel.add(idLabel, BorderLayout.CENTER);
        leftPanel.add(buttonPanel, BorderLayout.SOUTH);

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setPreferredSize(new Dimension(213,387));

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setPreferredSize(new Dimension(213,60));
        infoPanel.setBackground(new Color(219,127,35));


        JPanel topInfoPanel = new JPanel(new BorderLayout());
        topInfoPanel.setBackground(new Color(219,127,35));

        JLabel titleLabel = new JLabel(user.video.name, SwingConstants.CENTER);
        titleLabel.setPreferredSize(new Dimension(112, 20));
        titleLabel.setFont(new Font(null, Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        topInfoPanel.add(titleLabel, BorderLayout.WEST);


        JLabel participantsLabel = new JLabel("접속자 수 : " + user.video.user_num, SwingConstants.CENTER);
        participantsLabel.setPreferredSize(new Dimension(111, 20));
        participantsLabel.setFont(new Font(null, Font.PLAIN, 10));
        participantsLabel.setForeground(Color.WHITE);
        topInfoPanel.add(participantsLabel, BorderLayout.EAST);

        JPanel bottomInfoPanel = new JPanel(new BorderLayout());
        bottomInfoPanel.setBackground(new Color(219,127,35));

        JLabel ownerLabel = new JLabel("주최자 : " + user.video.o_name, SwingConstants.CENTER);
        ownerLabel.setPreferredSize(new Dimension(112, 20));
        ownerLabel.setFont(new Font(null, Font.PLAIN, 10));
        ownerLabel.setForeground(Color.WHITE);
        bottomInfoPanel.add(ownerLabel, BorderLayout.WEST);


        JLabel exitButton = new JLabel(new ImageIcon("images/btnexit.png"), SwingConstants.CENTER);
        exitButton.setBackground(new Color(219,127,35));
        exitButton.setPreferredSize(new Dimension(111, 20));
        exitButton.setPreferredSize(new Dimension(71,20));
        bottomInfoPanel.add(exitButton, BorderLayout.EAST);

        infoPanel.add(topInfoPanel);
        infoPanel.add(bottomInfoPanel);

        rightPanel.add(infoPanel, BorderLayout.NORTH);

        document = new DefaultStyledDocument();
        chat_display= new JTextPane(document);
        chat_display.setBackground(new Color(176,46,46));
        chat_display.setBorder(new LineBorder(new Color(176,46,46)));
        chat_display.setEditable(false);

        JScrollPane scrollPane = new JScrollPane(chat_display);

        rightPanel.add(scrollPane, BorderLayout.CENTER);

        rightPanel.add(createInputPanel(),BorderLayout.SOUTH);

        add(leftPanel, BorderLayout.WEST);
        add(createDisplayPanel(), BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);

        setSize(900, 600);

        startOrpauseButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(user.mode == UserObj.MODE_WatchingVideo){
                    if(user.video.videoMode == VideoObj.MODE_Play){
                        user.video.videoMode = VideoObj.MODE_Pause;
                    } else if (user.video.videoMode == VideoObj.MODE_Pause){
                        user.video.videoMode = VideoObj.MODE_Play;
                    } else {
                        user.video.videoMode = VideoObj.MODE_Play;
                    }
                    send(user);
                }
            }
        });

        prevButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(user.mode == UserObj.MODE_WatchingVideo){
                    user.video.videoMode = VideoObj.Mode_ChangeTime;
                    Platform.runLater(() -> {
                        Object result = webEngine.executeScript("getTime();");
                        if(result instanceof Number && result != null){
                            double time = ((Number) result).doubleValue();
                            if(time>=10){
                                System.out.println("현재 영상 시간 >>"+time);
                                user.video.videoTime = (int) time -10;
                            } else {
                                user.video.videoTime = 0;
                            }
                        }
                        send(user);
                    });
                }
            }
        });

        nextButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(user.mode == UserObj.MODE_WatchingVideo){
                    user.video.videoMode = VideoObj.Mode_ChangeTime;
                    Platform.runLater(() -> {
                        Object result = webEngine.executeScript("getTime();");
                        if(result instanceof Number && result != null){
                            double time = ((Number) result).doubleValue();
                            if(time>=0){
                                System.out.println("현재 영상 시간 >>"+time);
                                user.video.videoTime = (int) time + 10;
                                send(user);
                            }
                        }
                    });
                }
            }
        });

        return VTPanel;
    }


    public String getLocalAddr() {
        InetAddress local = null;
        String addr = "";

        try {
            local = InetAddress.getLocalHost();
            addr = local.getHostAddress();
            System.out.println(addr);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        return addr;
    }

    public void connectToServer() throws IOException, UnknownHostException {
        socket = new Socket();
        SocketAddress sa = new InetSocketAddress(serverAddress, serverPort);
        socket.connect(sa, 3000);

        out = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
        out.flush();

        receiveThread = new Thread(new Runnable() {
            private ObjectInputStream in = null ;

            @Override
            public void run() {
                try {
                    in = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
                } catch (IOException e) {
//                    printDisplay(">> 입력 스트림이 열리지 않음.");
                }
                while(receiveThread == Thread.currentThread()) {
                    receiveMessage();
                }
            }

            public void receiveMessage() {
                try {
                        UserObj inMsg = (UserObj) in.readObject();
                        if (inMsg == null) {
                            disconnect();
                            return;
                        }
                        if(user.mode == UserObj.MODE_WatchingVideo && user.video.o_name.equals(inMsg.video.o_name)){
                            switch (inMsg.video.videoMode){
                                case VideoObj.MODE_Pause -> {
                                    Platform.runLater(()-> {
                                        webEngine.executeScript("pauseVideo();");
                                    });
                                    user.video.videoMode = VideoObj.MODE_Pause;
                                    user.mode = UserObj.MODE_WatchingVideo;
                                    startOrpauseButton.setIcon(startIcon);
                                }
                                case VideoObj.MODE_Play -> {
                                    Platform.runLater(()-> {
                                        webEngine.executeScript("playVideo();");
                                    });
                                    user.video.videoMode = VideoObj.MODE_Play;
                                    user.mode = UserObj.MODE_WatchingVideo;
                                    startOrpauseButton.setIcon(pauseIcon);
                                }
                                case VideoObj.Mode_ChangeTime -> {
                                    user.video.videoTime = inMsg.video.videoTime;
                                    System.out.println("바뀔 시간 >>"+user.video.videoTime);
                                    Platform.runLater(new Runnable() {
                                        @Override
                                        public void run() {
                                            webEngine.executeScript("setTime("+user.video.videoTime+");");
                                            webEngine.executeScript("setTime("+user.video.videoTime+");");
                                        }
                                    });
                                    if(startOrpauseButton.getIcon() == startIcon){
                                        user.video.videoMode = VideoObj.MODE_Play;
                                    } else if(startOrpauseButton.getIcon() == pauseIcon){
                                        user.video.videoMode = VideoObj.MODE_Pause;
                                    }
                                }

                            }
                        }
                        else if(user.mode == UserObj.MODE_ChatStr) {
                            String chat = inMsg.chat;
                            printDisplay(chat);
                        }
                    } catch (IOException | ClassNotFoundException e) {
                    throw new RuntimeException(e);

                }
            }

        });

        receiveThread.start();
    }

    public void disconnect() {
        user.mode = UserObj.MODE_LogOut;
        send(user);

        try {
            receiveThread = null;
            socket.close();
        } catch (IOException e) {
            System.err.println(">> 클라이언트 닫기 오류: " + e.getMessage());
            System.exit(-1);
        }
    }


    private void send(UserObj msg){
        try {
            out.writeObject(new UserObj(msg));
            out.flush();
        } catch (IOException e) {
            System.err.println(">> 클라이언트 일반 전송 오류: "+e.getMessage());
        }
    }

    public void printDisplay(String str){
        int len = chat_display.getDocument().getLength();

        chat_display.setCaretPosition(len);
        chat_display.setForeground(Color.WHITE);

        try {
            document.insertString(len,str+"\n",null);
        } catch (BadLocationException e) {
            throw new RuntimeException(e);
        }
    }



    public static void main(String[] args) {
        try {
            new VideoClient("localhost", 54320);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
