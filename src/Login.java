import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;
import java.util.Vector;

public class Login extends JPanel {
    private JTextField t_id;
    private JButton b_login;
    private Socket socket;
    private ObjectOutputStream out;
    private Vector<VideoObj> videoList;
    private Thread receiverThread;

    public Login() {
        // GUI 구성
        setLayout(new BorderLayout());
        add(createTitlePanel(), BorderLayout.NORTH); // 상단 타이틀 패널
        add(createInputPanel(), BorderLayout.CENTER); // ID 입력창 패널
        add(createButtonPanel(), BorderLayout.SOUTH); // 버튼 패널

        setBounds(500, 300, 600, 400); // 창 크기 설정
        setBackground(new Color(139, 35, 35)); // 배경색 설정
        setVisible(true);

        connectToServer();
    }

    // 타이틀 패널 생성
    private JPanel createTitlePanel() {
        JPanel p = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                // 타원 그리기
                g.setColor(Color.ORANGE);
                g.fillOval(200, 30, 200, 50); // 타원의 위치와 크기 조정

                // 텍스트 "screening" 그리기
                g.setColor(Color.WHITE);
                g.setFont(new Font("Arial", Font.BOLD, 24));
                FontMetrics fm = g.getFontMetrics();
                String text = "screening";
                int textWidth = fm.stringWidth(text);
                int textX = 200 + (200 - textWidth) / 2;
                int textY = 30 + (50 + fm.getAscent()) / 2 - 5;
                g.drawString(text, textX, textY);
            }
        };
        p.setPreferredSize(new Dimension(600, 90)); // 패널 크기 조정
        p.setBackground(new Color(139, 35, 35)); // 배경색 설정
        return p;
    }

    // ID 입력창 패널 생성
    private JPanel createInputPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(new Color(139, 35, 35)); // 배경색 설정
        GridBagConstraints gbc = new GridBagConstraints();

        JLabel l_id = new JLabel("ID");
        l_id.setForeground(Color.WHITE);
        l_id.setFont(new Font("Arial", Font.BOLD, 16));

        t_id = new JTextField(10); // 입력창 크기
        t_id.setBackground(Color.gray); // 어두운 회색 배경
        t_id.setForeground(Color.black); // 텍스트 색상
        t_id.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY)); // 경계선

        // "ID" 레이블 위치
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.EAST;
        p.add(l_id, gbc);

        // 입력창 위치
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        p.add(t_id, gbc);

        return p;
    }

    // 버튼 패널 생성
    private JPanel createButtonPanel() {
        JPanel p = new JPanel();
        p.setBackground(new Color(139, 35, 35)); // 황색 배경

        b_login = new JButton("LOGIN");
        b_login.setPreferredSize(new Dimension(150, 40));
        b_login.setFont(new Font("Arial", Font.BOLD, 16));
        b_login.setForeground(Color.WHITE);
        b_login.setBackground(Color.orange); // 빨간색
//        b_login.setFocusPainted(false);

        b_login.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String id = t_id.getText().trim();
                if (!id.isEmpty()) {
                    sendLoginRequest(id);
                } else {
                    JOptionPane.showMessageDialog(Login.this, "ID를 입력하세요.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        p.add(b_login);
        return p;
    }

    // 서버 연결
    private void connectToServer() {
        try {
            socket = new Socket("127.0.0.1", 54320); // 서버 주소와 포트
            out = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "서버 연결 실패", "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }
    }

    // 로그인 요청 전송
    private void sendLoginRequest(String id) {
        try {
            UserObj user = new UserObj(id, UserObj.MODE_LogIn);
            out.writeObject(user);
            out.flush();

            // 로그인 성공 시 VideoList 창으로 전환
            Vector<VideoObj> videoList = fetchVideoList();
            remove(this);
            add(new VideoList(id, socket, out, videoList));
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "로그인 요청 실패", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // 비디오 목록 가져오기 (더미 데이터로 처리)
    private Vector<VideoObj> fetchVideoList() {
        // 실제 서버 통신으로 교체 가능
        Vector<VideoObj> videoList = new Vector<>();
        videoList.add(new VideoObj("브이로그 - 홍길동 님의 상영회"));
        videoList.add(new VideoObj("강의 - 강찬 님의 상영회"));
        videoList.add(new VideoObj("영화 - 윤지문덕 님의 상영회"));
        return videoList;
    }

    public static void main(String[] args) {
        new Login();
    }
}
