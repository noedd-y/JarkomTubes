import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.net.ssl.*;
import javax.swing.*;

public class ClientGUI {

	private SSLSocket socket;
	private DataOutputStream out;
	private BufferedReader in;
	private String userName;

	private JFrame frame;
	private JTextArea chatArea;
	private JTextField inputField;
	private JTextField roomField;
	private JTextField userNameField;
	private JButton sendBtn;
	private JButton joinBtn;
	private JButton listBtn;
	private JButton setNameBtn;
	private JButton exitBtn;

	public ClientGUI() {
		promptForUserName();
		initUI();
		connectToServer();
	}

	private void promptForUserName() {
		userName = JOptionPane.showInputDialog(null, 
			"Enter your username:", "Chat Room Client", 
			JOptionPane.PLAIN_MESSAGE);
		if (userName == null || userName.trim().isEmpty()) {
			userName = "User" + System.currentTimeMillis() % 10000;
		}
	}

	private void initUI() {
		frame = new JFrame("Chat Room Client - " + userName);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setSize(700, 450);
		frame.setLayout(new BorderLayout());

		chatArea = new JTextArea();
		chatArea.setEditable(false);
		JScrollPane scroll = new JScrollPane(chatArea);
		frame.add(scroll, BorderLayout.CENTER);

		JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
		
		userNameField = new JTextField(userName, 10);
		setNameBtn = new JButton("Set Name");
		
		roomField = new JTextField(12);
		joinBtn = new JButton("Join");
		listBtn = new JButton("List Rooms");
		exitBtn = new JButton("Exit");

		top.add(new JLabel("Name:"));
		top.add(userNameField);
		top.add(setNameBtn);
		top.add(new JLabel("  |  Room:"));
		top.add(roomField);
		top.add(joinBtn);
		top.add(listBtn);
		top.add(exitBtn);

		frame.add(top, BorderLayout.NORTH);

		JPanel bottom = new JPanel(new BorderLayout());
		inputField = new JTextField();
		sendBtn = new JButton("Send");
		bottom.add(inputField, BorderLayout.CENTER);
		bottom.add(sendBtn, BorderLayout.EAST);

		frame.add(bottom, BorderLayout.SOUTH);

		// Actions
		sendBtn.addActionListener(e -> sendChat());
		inputField.addActionListener(e -> sendChat());

		joinBtn.addActionListener(e -> joinRoom());
		listBtn.addActionListener(e -> requestRoomList());
		setNameBtn.addActionListener(e -> setUserName());
		exitBtn.addActionListener(e -> closeConnection());

		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				closeConnection();
			}
		});

		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	private void connectToServer() {
		try {
			SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
			socket = (SSLSocket) factory.createSocket("127.0.0.1", 6789);

			out = new DataOutputStream(socket.getOutputStream());
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			appendChat("=== Connected to Chat Server ===");
			
			// Send username to server
			out.writeBytes("/name " + userName + "\n");
			out.flush();

			Thread readThread = new Thread(() -> {
				try {
					String msg;
					while ((msg = in.readLine()) != null) {
						appendChat(msg);
					}
				} catch (Exception ex) {
					appendChat("Connection closed.");
				}
			});

			readThread.start();

		} catch (Exception e) {
			JOptionPane.showMessageDialog(frame, "Failed to connect to server:\n" + e.getMessage(), "Connection Error", JOptionPane.ERROR_MESSAGE);
			appendChat("Could not connect: " + e.getMessage());
		}
	}

	private void sendChat() {
		String text = inputField.getText().trim();
		if (text.isEmpty() || out == null) return;

		try {
			out.writeBytes(text + "\n");
			out.flush();
			inputField.setText("");
		} catch (Exception e) {
			appendChat("Failed to send message: " + e.getMessage());
		}
	}

	private void joinRoom() {
		String room = roomField.getText().trim();
		if (room.isEmpty() || out == null) return;

		try {
			out.writeBytes("/join " + room + "\n");
			out.flush();
		} catch (Exception e) {
			appendChat("Failed to join room: " + e.getMessage());
		}
	}

	private void requestRoomList() {
		if (out == null) return;

		try {
			out.writeBytes("/listroom\n");
			out.flush();
		} catch (Exception e) {
			appendChat("Failed to request room list: " + e.getMessage());
		}
	}

	private void setUserName() {
		String newName = userNameField.getText().trim();
		if (newName.isEmpty() || out == null) return;

		try {
			out.writeBytes("/name " + newName + "\n");
			out.flush();
			userName = newName;
			frame.setTitle("Chat Room Client - " + userName);
		} catch (Exception e) {
			appendChat("Failed to set username: " + e.getMessage());
		}
	}

	private void closeConnection() {
		try {
			if (out != null) {
				out.writeBytes("/leave\n");
				out.flush();
			}
		} catch (Exception ignored) {}

		try { if (socket != null) socket.close(); } catch (Exception ignored) {}
		appendChat("Disconnected.");
		frame.dispose();
	}

	private void appendChat(String msg) {
		SwingUtilities.invokeLater(() -> {
			chatArea.append(msg + "\n");
			chatArea.setCaretPosition(chatArea.getDocument().getLength());
		});
	}

	public static void main(String[] args) {
		System.setProperty(
			"javax.net.ssl.trustStore",
			"serverkeystore.jks"
		);

		System.setProperty(
			"javax.net.ssl.trustStorePassword",
			"123456"
		);
		
		SwingUtilities.invokeLater(() -> new ClientGUI());
	}
}
