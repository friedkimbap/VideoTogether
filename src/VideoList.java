import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Vector;

public class VideoList extends JFrame {
    private JButton b_new, b_logout;
    private Socket socket;
    private ObjectOutputStream out;

    public VideoList(String userId, Socket socket, ObjectOutputStream out, Vector<VideoObj> videoList) {
        super("Screening");

        this.socket = socket;
        this.out = out;

        // 전체 레이아웃 설정
        setLayout(new BorderLayout());
        add(createLeftPanel(userId), BorderLayout.WEST); // 왼쪽 사용자 정보 패널
        add(createVideoListPanel(videoList), BorderLayout.CENTER); // 비디오 리스트 패널

        setBounds(500, 200, 800, 600); // 창 크기 설정
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    // 왼쪽 패널 생성 (사용자 정보 및 버튼)
    private JPanel createLeftPanel(String userId) {
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBackground(new Color(139, 35, 35)); // 배경 색: 짙은 빨강
        leftPanel.setBorder(new EmptyBorder(20, 20, 20, 20)); // 내부 여백

        // ID 표시
        JLabel labelId = new JLabel("ID");
        labelId.setFont(new Font("Arial", Font.BOLD, 18));
        labelId.setForeground(Color.WHITE);
        labelId.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel labelUserId = new JLabel(userId);
        labelUserId.setFont(new Font("Arial", Font.PLAIN, 16));
        labelUserId.setForeground(Color.WHITE);
        labelUserId.setAlignmentX(Component.CENTER_ALIGNMENT);

        // NEW 버튼
        b_new = new JButton("NEW");
        b_new.setAlignmentX(Component.CENTER_ALIGNMENT);
        b_new.setBackground(Color.ORANGE);
        b_new.setFont(new Font("Arial", Font.BOLD, 16));

        // LOGOUT 버튼
        b_logout = new JButton("LOGOUT");
        b_logout.setAlignmentX(Component.CENTER_ALIGNMENT);
        b_logout.setBackground(Color.ORANGE);
        b_logout.setFont(new Font("Arial", Font.BOLD, 16));
        b_logout.addActionListener(e -> logout());

        // 패널에 구성 요소 추가
        leftPanel.add(labelId);
        leftPanel.add(Box.createVerticalStrut(10));
        leftPanel.add(labelUserId);
        leftPanel.add(Box.createVerticalStrut(30));
        leftPanel.add(b_new);
        leftPanel.add(Box.createVerticalStrut(10));
        leftPanel.add(b_logout);

        return leftPanel;
    }

    // 비디오 리스트 패널 생성
    private JPanel createVideoListPanel(Vector<VideoObj> videoList) {
        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(Color.WHITE); // 우측 전체 패널 흰색 배경
        listPanel.setBorder(new EmptyBorder(0, 0, 0, 0)); // 여백 제거

        for (VideoObj video : videoList) {
            JPanel videoPanel = new JPanel(new BorderLayout());
            videoPanel.setBackground(Color.ORANGE); // 각 영상 항목의 배경을 주황색으로 설정

            // 상단 제목
            JLabel videoTitle = new JLabel(video.name);
            videoTitle.setFont(new Font("Arial", Font.BOLD, 14));
            videoTitle.setForeground(Color.WHITE); // 제목 글자 색상: 흰색

            // 하단 설명
            JLabel videoDescription = new JLabel(video.o_name + " 님의 상영회");
            videoDescription.setFont(new Font("Arial", Font.PLAIN, 12));
            videoDescription.setForeground(Color.WHITE);
            videoDescription.setForeground(Color.DARK_GRAY);

            // 제목과 설명을 중앙에 배치하기 위한 패널
            JPanel textPanel = new JPanel();
            textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
            textPanel.setBackground(Color.ORANGE); // 주황색 배경
            textPanel.add(videoTitle);
            textPanel.add(videoDescription);

            // 입장하기 버튼
            JButton joinButton = new JButton("입장하기");
            joinButton.setBackground(Color.RED); // 빨간색
            joinButton.setForeground(Color.WHITE); // 흰색 텍스트
            joinButton.setFont(new Font("Arial", Font.BOLD, 12));
            joinButton.addActionListener(e -> joinVideo(video));

            // 패널 배치
            videoPanel.add(textPanel, BorderLayout.CENTER);
            videoPanel.add(joinButton, BorderLayout.EAST);

            listPanel.add(videoPanel);
        }

        // 하단 빈 여백 추가
        JPanel emptyPanel = new JPanel();
        emptyPanel.setPreferredSize(new Dimension(800, 80)); // 영상 항목 크기와 동일한 크기로 설정
        emptyPanel.setBackground(Color.WHITE); // 흰색 배경
        listPanel.add(emptyPanel);

        return listPanel;
    }

    // 상영회 입장 처리
    private void joinVideo(VideoObj video) {
        try {
            out.writeObject(new UserObj(video.id, UserObj.MODE_JoinVideo));
            out.flush();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "입장 실패", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // 로그아웃 처리
    private void logout() {
        try {
            out.writeObject(new UserObj("LOGOUT", UserObj.MODE_LogOut));
            out.flush();
            socket.close();
            System.exit(0);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "로그아웃 실패", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
