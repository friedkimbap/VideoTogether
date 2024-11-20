import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.util.Objects;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

public class VideoClient extends JFrame{
    private JTextField t_input, t_userID, t_hostAddr, t_portNum;
    private JFXPanel t_display;
    private JButton b_connect, b_disconnect, b_send, b_select, b_exit;
    private String serverAddress;
    private int serverPort;
    private Socket socket;
    private ObjectOutputStream out;
    //  private BufferedReader in;
    private Thread receiveThread;
    private String uid;

    private WebEngine webEngine;

    public VideoClient(String serverAddress, int serverPort) {
        super("With Talk");

        this.serverAddress = serverAddress;
        this.serverPort = serverPort;

        buildGUI();

        setBounds(100,100,820,750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setVisible(true);
    }

    public void buildGUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        add(createDisplayPanel(), BorderLayout.CENTER);

        JPanel p = new JPanel(new BorderLayout());
        p.add(createInputPanel(),BorderLayout.NORTH);
        //p.add(createInfoPanel(), BorderLayout.CENTER);
        p.add(createControlPanel(), BorderLayout.SOUTH);

        add(p,BorderLayout.SOUTH);
    }

    public JPanel createDisplayPanel() {
        JPanel p = new JPanel(new BorderLayout());
        t_display=  new JFXPanel(); // JFX패널을 사용하면 JFX 애플리케이션을 사용할 수 있음.(원래는 반대인데 이해하기 쉽게) 웹뷰나 웹엔진을 사용 가능

        Platform.runLater(()-> { // JFX 애플리케이션 스레드에서 작업을 실행하는 거임, 웹페이지의 변화를 안전하게 적용해줌, 모든 웹페이지에 변경할 사항들은 이 안에서 일어나게 해야함
            WebView webView = new WebView(); // 웹페이지를 표시하는 화면 JFX 컴포넌트임
            webEngine = webView.getEngine(); // 이거는 웹브라우저의 역할이라고 보면 됨
            webEngine.load(getClass().getResource("./index.html").toExternalForm()); // 이게 index.html을 띄우는거임 클래스의 경로에서 index.html을 찾아내는거임, 그리고 이거를 알맞은 형식으로 바꾸는거
            webView.setDisable(true);

            Scene scene = new Scene(webView); // Scene 클래스 역시 jfx 애플리케이션의 그래픽처리를 담당함
            t_display.setScene(scene);
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
        b_send = new JButton("보내기");
        b_select = new JButton("선택하기");


        p.add(t_input, BorderLayout.CENTER);
        buttonPanel.add(b_send);
        buttonPanel.add(b_select);
        p.add(buttonPanel, BorderLayout.EAST);

//        b_send.setEnabled(false);
//        b_select.setEnabled(false);
//        t_input.setEnabled(false);

        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setMultiSelectionEnabled(false);
        chooser.addChoosableFileFilter(txtFilter);
        chooser.addChoosableFileFilter(imgFilter);
        chooser.setAcceptAllFileFilterUsed(false);

        ActionListener actionListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //sendMessage();
            }
        };

        b_send.addActionListener(actionListener);
        t_input.addActionListener(actionListener);

        b_select.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

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

//    public JPanel createInfoPanel() {
//        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
//
//        t_userID = new JTextField(7);
//        t_hostAddr = new JTextField(12);
//        t_portNum = new JTextField(5);
//
//        t_userID.setText("guest" + getLocalAddr().split("\\.")[3]);
//        t_hostAddr.setText(this.serverAddress);
//        t_portNum.setText(String.valueOf(this.serverPort));
//
//        t_portNum.setHorizontalAlignment(JTextField.CENTER);
//
//        p.add(new JLabel("아이디:"));
//        p.add(t_userID);
//        p.add(new JLabel("서버주소:"));
//        p.add(t_hostAddr);
//        p.add(new JLabel("포트번호:"));
//        p.add(t_portNum);
//
//        return p;
//    }

    public JPanel createControlPanel() {
        JPanel p = new JPanel(new GridLayout(1,3));
        b_connect = new JButton("접속하기");
        b_disconnect = new JButton("접속끊기");
        b_exit = new JButton("나가기");

        p.add(b_connect);
        p.add(b_disconnect);
        p.add(b_exit);

        b_connect.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                Platform.runLater(()->{
                    webEngine.executeScript("playVideo();");
                });
            }
        });

        b_disconnect.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                Platform.runLater(()->{
                    webEngine.executeScript("pauseVideo();");
                });
            }
        });

//        b_connect.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                VideoClient.this.serverAddress = t_hostAddr.getText(); // 이곳은 ActionListener객체임
//                VideoClient.this.serverPort = Integer.parseInt(t_portNum.getText());
//
//                try {
//                    connectToServer();
//                    sendUserID();
//                } catch (IOException ex) {
////                    printDisplay(">> 서버와의 연결 오류: "+ex.getMessage());
//                    return;
//                }
//
//                b_connect.setEnabled(false);
//                b_disconnect.setEnabled(true);
//
//                t_input.setEnabled(true);
//                b_send.setEnabled(true);
//                b_select.setEnabled(true);
//                b_exit.setEnabled(false);
//
//                t_userID.setEditable(false);
//                t_hostAddr.setEditable(false);
//                t_portNum.setEditable(false);
//            }
//        });
//
//        b_disconnect.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                disconnect();
//
//                b_connect.setEnabled(true);
//                b_disconnect.setEnabled(false);
//
//                t_input.setEnabled(false);
//                b_send.setEnabled(false);
//                b_select.setEnabled(false);
//                b_exit.setEnabled(true);
//
//                t_userID.setEditable(true);
//                t_hostAddr.setEditable(true);
//                t_portNum.setEditable(true);
//            }
//
//        });
//
//        b_exit.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                System.exit(0);
//            }
//
//        });

        return p;
    }

//    public void printDisplay(String str){
////    t_display.append(str+"\n");
//        int len = t_display.getDocument().getLength();
//
//        t_display.setCaretPosition(len);
//
////        try {
//////            document.insertString(len,str+"\n",null);
////        } catch (BadLocationException e) {
////            throw new RuntimeException(e);
////        }
//    }

//    public void printDisplay(ImageIcon image){
//        int len = t_display.getDocument().getLength();
//
//        t_display.setCaretPosition(len);
//
//        if (image.getIconWidth() > 400){
//            Image img = image.getImage();
//            Image changeImg = img.getScaledInstance(400, -1, Image.SCALE_SMOOTH);
//            image = new ImageIcon(changeImg);
//        }
//
//        printDisplay("");
////        t_display.insertIcon(image);
//    }


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

//    public void connectToServer() throws IOException, UnknownHostException {
//        socket = new Socket();
//        SocketAddress sa = new InetSocketAddress(serverAddress, serverPort);
//        socket.connect(sa, 3000);
//
//        out = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
//
//        receiveThread = new Thread(new Runnable() {
//            private ObjectInputStream in = null ;
//
//            @Override
//            public void run() {
//                try {
//                    in = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
//                } catch (IOException e) {
////                    printDisplay(">> 입력 스트림이 열리지 않음.");
//                }
//                while(receiveThread == Thread.currentThread()) {
//                    receiveMessage();
//                }
//            }
//
//            public void receiveMessage() {
//                try {
//                    VideoObj inMsg = (VideoObj)in.readObject();
//                    if (inMsg == null) {
//                        disconnect();
////                        printDisplay(">> 서버 연결 끊김");
//                        return;
//                    }
////          printDisplay(msg);
//                    switch (inMsg.mode){
//                        case VideoObj.MODE_TX_STRING -> {
////                            printDisplay(inMsg.userID + ": " + inMsg.message);
//                        }
//                        case VideoObj.MODE_TX_IMAGE -> {
//                            BufferedImage bufferedImage = new BufferedImage(inMsg.image.getIconWidth(), inMsg.image.getIconHeight(), BufferedImage.TYPE_INT_RGB );
//                            bufferedImage.getGraphics().drawImage(inMsg.image.getImage(), 0, 0, null);
//
//                            ImageIO.write(bufferedImage, "png", new File(inMsg.message));
////
////                            printDisplay(inMsg.userID + ": " + inMsg.message);
////                            printDisplay(inMsg.image);
//                        }
//                        case VideoObj.MODE_TX_FILE -> {
//                            String[] messageLines = inMsg.message.split("\n");
//                            File file = new File(messageLines[0]);
//                            try (FileOutputStream fos = new FileOutputStream(file)) {
//                                for (int i = 1; i < messageLines.length; i++) {
//                                    fos.write(messageLines[i].getBytes());
//                                    fos.write('\n');
//                                }
//                                fos.flush();
//                            } catch (IOException e) {
////                                printDisplay(">> 파일 저장 중 오류 발생: " + e.getMessage());
//                            }
////
////                            printDisplay(inMsg.userID + ": " + inMsg.message);
////                            printDisplay("");
//                        }
//
//                    }
//                } catch (IOException e) {
////                    printDisplay(">> 연결을 종료했습니다.");
//                } catch (ClassNotFoundException e) {
////                    printDisplay(">> 잘못된 객체가 전달되었습니다.");
//                }
//            }
//
//        });
//
//        receiveThread.start();
//    }
//
//    public void disconnect() {
//        send(new VideoObj(uid, VideoObj.MODE_LOGOUT));
//
//        try {
//            receiveThread = null;
//            socket.close();
//        } catch (IOException e) {
//            System.err.println(">> 클라이언트 닫기 오류: " + e.getMessage());
//            System.exit(-1);
//        }
//    }
//
//
//
//    public void sendMessage() {
//        String message = t_input.getText();
//        if(message==null) return;
//
////    try {
////      out.write(message+"\n");
////      out.flush();
////    } catch (IOException e) {
////      System.err.println(">> 클라이언트 일반 전송 오류: "+e.getMessage());
////      System.exit(-1);
////    }
//        send(new VideoObj(uid, VideoObj.MODE_TX_STRING, message));
//
//        t_input.setText("");
//    }
//
//    public void sendImage() {
//        String filename = t_input.getText().strip();
//        if (filename.isEmpty()) return;
//
//        File file = new File(filename);
//        if(!file.exists()) {
////            printDisplay(">> 파일이 존재하지 않습니다: "+filename);
//            return;
//        }
//        ImageIcon image = new ImageIcon(filename);
//        send(new VideoObj(uid, VideoObj.MODE_TX_IMAGE, file.getName(), image));
//
//        t_input.setText("");
//    }
//
//    public void sendFile() {
//        int k_byte = 1024*1024;
//
//        String filename = t_input.getText().strip();
//        t_input.setText("");
//        if (filename.isEmpty()) return;
//
//        File file = new File(filename);
//        if(!file.exists()) {
////            printDisplay(">> 파일이 존재하지 않습니다: "+filename);
//            return;
//        }
//        byte[] bytes = new byte[1024*k_byte];
//        try {
//            FileInputStream fis = new FileInputStream(file);
//            int size = fis.read(bytes);
//
//            String fileContent = file.getName() + "\n" + new String(bytes, 0, size);
//
//            send(new VideoObj(uid, VideoObj.MODE_TX_FILE, fileContent, size));
//        } catch (FileNotFoundException e) {
////            printDisplay(">> 파일이 존재하지 않습니다: " + filename);
//        } catch (IOException e) {
////            printDisplay(">> 파일 읽기 중 오류 발생: " + e.getMessage());
//        }
//    }
//
//    public void sendUserID() {
//        uid = t_userID.getText();
//        send(new VideoObj(uid, VideoObj.MODE_LOGIN));
//    }
//
//    private void send(VideoObj msg){
//        try {
//            out.writeObject(msg);
//            out.flush();
//        } catch (IOException e) {
//            System.err.println(">> 클라이언트 일반 전송 오류: "+e.getMessage());
//        }
//    }



    public static void main(String[] args) {
        new VideoClient("localhost", 54320);
    }

}
