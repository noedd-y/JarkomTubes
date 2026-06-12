import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.net.ssl.*;
import javax.swing.*;

public class ClientGUI {

	private SSLSocket socket;
	private DataOutputStream out;
	private BufferedReader in;

	private JFrame frame;
	private JTextArea chatArea;
	private JTextField inputField;
	private JTextField roomField;
	private JTextField usernameField;
	private JButton sendBtn;
	private JButton joinBtn;
	private JButton listBtn;
	private JButton addBtn;
	private JButton exitBtn;
	private JButton loginBtn;
	private JPanel cards;
	private CardLayout cardLayout;

	public ClientGUI() {
		initUI();
	}

	private void initUI() {
		frame = new JFrame("Chat Room Client");
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setSize(600, 400);
		frame.setLayout(new BorderLayout());

		cards = new JPanel();
		cardLayout = new CardLayout();
		cards.setLayout(cardLayout);

		JPanel loginPanel = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(8, 8, 8, 8);
		gbc.gridx = 0;
		gbc.gridy = 0;
		loginPanel.add(new JLabel("Enter your username:"), gbc);

		usernameField = new JTextField(18);
		gbc.gridy = 1;
		loginPanel.add(usernameField, gbc);

		loginBtn = new JButton("Continue");
		gbc.gridy = 2;
		loginPanel.add(loginBtn, gbc);

		cards.add(loginPanel, "login");

		JPanel chatPanel = new JPanel(new BorderLayout());
		chatArea = new JTextArea();
		chatArea.setEditable(false);
		JScrollPane scroll = new JScrollPane(chatArea);
		chatPanel.add(scroll, BorderLayout.CENTER);

		JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
		roomField = new JTextField(12);
        addBtn = new JButton("Add Room");
		joinBtn = new JButton("Join");
		listBtn = new JButton("List Rooms");
		exitBtn = new JButton("Exit");

		top.add(new JLabel("Room:"));
		top.add(roomField);
		top.add(addBtn);
		top.add(joinBtn);
		top.add(listBtn);
		top.add(exitBtn);

		chatPanel.add(top, BorderLayout.NORTH);

		JPanel bottom = new JPanel(new BorderLayout());
		inputField = new JTextField();
		sendBtn = new JButton("Send");
		bottom.add(inputField, BorderLayout.CENTER);
		bottom.add(sendBtn, BorderLayout.EAST);

		chatPanel.add(bottom, BorderLayout.SOUTH);
		cards.add(chatPanel, "chat");

		frame.add(cards, BorderLayout.CENTER);

		// Actions
		loginBtn.addActionListener(e -> login());
		usernameField.addActionListener(e -> login());

		sendBtn.addActionListener(e -> sendChat());
		inputField.addActionListener(e -> sendChat());

		joinBtn.addActionListener(e -> joinRoom());
		listBtn.addActionListener(e -> requestRoomList());
		addBtn.addActionListener(e -> addRoom());
		exitBtn.addActionListener(e -> closeConnection());

		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				closeConnection();
			}
		});

		setChatControlsEnabled(false);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	private void connectToServer(String username) {
		try {
			SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
			socket = (SSLSocket) factory.createSocket("127.0.0.1", 6789);

			out = new DataOutputStream(socket.getOutputStream());
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			out.writeBytes("/username " + username + "\n");
			out.flush();

			appendChat("=== Connected as " + username + " ===");

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
			setChatControlsEnabled(true);

		} catch (Exception e) {
			JOptionPane.showMessageDialog(frame, "Failed to connect to server:\n" + e.getMessage(), "Connection Error", JOptionPane.ERROR_MESSAGE);
			appendChat("Could not connect: " + e.getMessage());
			cardLayout.show(cards, "login");
		}
	}

	private void login() {
		String username = usernameField.getText().trim();
		if (username.isEmpty()) {
			JOptionPane.showMessageDialog(frame, "Please enter a username before continuing.", "Username Required", JOptionPane.WARNING_MESSAGE);
			return;
		}

		connectToServer(username);
		frame.setTitle("Chat Room Client - " + username);
		cardLayout.show(cards, "chat");
	}

	private void setChatControlsEnabled(boolean enabled) {
		sendBtn.setEnabled(enabled);
		inputField.setEnabled(enabled);
		joinBtn.setEnabled(enabled);
		listBtn.setEnabled(enabled);
		addBtn.setEnabled(enabled);
		exitBtn.setEnabled(enabled);
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

	private void addRoom() {
		//add room ini untuk room baru, kalau join room itu untuk masuk ke room yang sudah ada
        String room = roomField.getText().trim();
        if (room.isEmpty() || out == null) return;
        try {
            out.writeBytes("/addroom " + room + "\n");
            out.flush();
        } catch (Exception e) {
            appendChat("Failed to add room: " + e.getMessage());
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
