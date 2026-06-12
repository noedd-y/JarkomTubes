import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import javax.net.ssl.*;
import javax.swing.*;

public class ClientGUI {

    private SSLSocket socket;
    private DataOutputStream out;
    private BufferedReader in;

    private JFrame frame;
    private JEditorPane chatArea;
    private JTextField inputField;
    private JTextField roomField;
    private JTextField mainRoomField;
    private JTextField usernameField;
    private JButton sendBtn;
    private JButton joinBtn;
    private JButton listBtn;
    private JButton addBtn;
    private JButton exitBtn;
    private JButton loginBtn;
    private JButton closeRoomBtn;
    private JLabel roomInfoLabel;
    private JPanel cards;
    private CardLayout cardLayout;
    private String currentUsername;
    private String currentRoom;
    private String currentRoomOwner; // Dinamis mendeteksi owner asli ruangan
    private boolean isOwner; 

    private DefaultListModel<String> roomsModel;
    private JList<String> roomsList;
    private List<String> roomNames;

    private JPanel membersPanel;
    private List<String> memberNames = new ArrayList<>();

    public ClientGUI() {
        initUI();
    }

    private void initUI() {
        frame = new JFrame("Chat Room Client");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(700, 450);
        frame.setLayout(new BorderLayout());

        cards = new JPanel();
        cardLayout = new CardLayout();
        cards.setLayout(cardLayout);

        // ===================== LOGIN PANEL =====================
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

        // ===================== MAIN MENU PANEL =====================
        JPanel mainPanel = new JPanel(new BorderLayout());
        roomsModel = new DefaultListModel<>();
        roomNames = new ArrayList<>();
        roomsList = new JList<>(roomsModel);
        roomsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane roomsScroll = new JScrollPane(roomsList);
        
        JPanel joinInputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        mainRoomField = new JTextField(16);
        JButton joinRoomBtn = new JButton("Join Room");
        joinInputPanel.add(new JLabel("Room name:"));
        joinInputPanel.add(mainRoomField);
        joinInputPanel.add(joinRoomBtn);
        mainPanel.add(joinInputPanel, BorderLayout.NORTH);
        mainPanel.add(roomsScroll, BorderLayout.CENTER);
        
        JPanel mainBtns = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton refreshRoomsBtn = new JButton("Refresh");
        JButton createRoomBtn = new JButton("Create Room");
        JButton logoutBtn = new JButton("Logout");
        mainBtns.add(refreshRoomsBtn);
        mainBtns.add(createRoomBtn);
        mainBtns.add(logoutBtn);
        mainPanel.add(mainBtns, BorderLayout.SOUTH);

        cards.add(mainPanel, "main");

        // ===================== CHAT PANEL =====================
        JPanel chatPanel = new JPanel(new BorderLayout());
        chatArea = new JEditorPane();
        chatArea.setContentType("text/html");
        chatArea.setEditable(false);
        chatArea.setText("<html><body></body></html>");
        JScrollPane scroll = new JScrollPane(chatArea);

        // Members list on the right
        membersPanel = new JPanel();
        membersPanel.setLayout(new BoxLayout(membersPanel, BoxLayout.Y_AXIS));
        JScrollPane membersScroll = new JScrollPane(membersPanel);
        membersScroll.setPreferredSize(new Dimension(220, 0));

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(new JLabel(" Members List:"), BorderLayout.NORTH);
        rightPanel.add(membersScroll, BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scroll, rightPanel);
        split.setResizeWeight(0.75);
        chatPanel.add(split, BorderLayout.CENTER);

        // Top area: room info + controls
        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));

        roomInfoLabel = new JLabel("Not in a room");
        roomInfoLabel.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        topContainer.add(roomInfoLabel);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        roomField = new JTextField(12);
        addBtn = new JButton("Add Room");
        joinBtn = new JButton("Join");
        listBtn = new JButton("List Rooms");
        exitBtn = new JButton("Exit Room");
        closeRoomBtn = new JButton("Close Room");

        top.add(new JLabel("Room:"));
        top.add(roomField);
        top.add(addBtn);
        top.add(joinBtn);
        top.add(listBtn);
        top.add(exitBtn);
        top.add(closeRoomBtn);

        topContainer.add(top);
        chatPanel.add(topContainer, BorderLayout.NORTH);

        JPanel bottom = new JPanel(new BorderLayout());
        inputField = new JTextField();
        sendBtn = new JButton("Send");
        bottom.add(inputField, BorderLayout.CENTER);
        bottom.add(sendBtn, BorderLayout.EAST);

        chatPanel.add(bottom, BorderLayout.SOUTH);
        cards.add(chatPanel, "chat");

        frame.add(cards, BorderLayout.CENTER);

        // ===================== ACTIONS =====================
        loginBtn.addActionListener(e -> login());
        usernameField.addActionListener(e -> login());

        sendBtn.addActionListener(e -> sendChat());
        inputField.addActionListener(e -> sendChat());

        joinBtn.addActionListener(e -> joinRoom());
        listBtn.addActionListener(e -> requestRoomList());
        addBtn.addActionListener(e -> addRoom());
        exitBtn.addActionListener(e -> leaveRoomToMain());

        refreshRoomsBtn.addActionListener(e -> requestRoomList());
        
        joinRoomBtn.addActionListener(e -> {
            String room = mainRoomField.getText().trim();
            if (!room.isEmpty()) {
                roomField.setText(room);
                joinRoom();
            } else {
                JOptionPane.showMessageDialog(frame, "Please type a room name in the input field to join.", "Input Required", JOptionPane.WARNING_MESSAGE);
            }
        });

        createRoomBtn.addActionListener(e -> {
            String room = JOptionPane.showInputDialog(frame, "Enter new room name:");
            if (room != null && !room.trim().isEmpty()) {
                roomField.setText(room.trim());
                addRoom();
            }
        });

        logoutBtn.addActionListener(e -> {
            closeConnection();
            cardLayout.show(cards, "login");
        });

        closeRoomBtn.addActionListener(e -> closeRoom());

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                closeConnection();
            }
        });

        setChatControlsEnabled(false);
        updateOwnerControls();
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

            appendSystemMessage("Connected as " + username);

            Thread readThread = new Thread(() -> {
                try {
                    String msg;
                    while ((msg = in.readLine()) != null) {
                        String trimmedMsg = msg.trim();

                        if (msg.startsWith("ROOMLIST:")) {
                            String payload = msg.substring("ROOMLIST:".length()).trim();
                            handleRoomList(payload);
                            continue;
                        }

                        if (msg.startsWith("MEMBERS:")) {
                            String payload = msg.substring("MEMBERS:".length()).trim();
                            handleMemberList(payload);
                            continue;
                        }

                        if (msg.startsWith("KICKED:")) {
                            String fromRoom = msg.substring("KICKED:".length()).trim();
                            handleKicked(fromRoom);
                            continue;
                        }

                        if (currentRoom != null && !trimmedMsg.isEmpty() && !trimmedMsg.contains(":") && !trimmedMsg.contains(" ")) {
                            addIncomingMemberUpdate(trimmedMsg);
                            continue;
                        }

                        if (msg.startsWith("CHAT:")) {
                            appendChatMessage(msg.substring("CHAT:".length()).trim());
                            continue;
                        }

                        if (msg.startsWith("INFO:")) {
                            appendSystemMessage(msg.substring("INFO:".length()).trim());
                            continue;
                        }

                        if (msg.contains(":")) {
                            appendChatMessage(msg);
                        } else {
                            appendSystemMessage(msg);
                            
                            // Otomatis minta list terbaru ke server jika mendeteksi teks notifikasi join/masuk room
                            String lowerMsg = trimmedMsg.toLowerCase();
                            if (currentRoom != null && (lowerMsg.contains("join") || lowerMsg.contains("room") || lowerMsg.contains("masuk"))) {
                                triggerSilentInfoRequest();
                            }
                        }
                    }
                } catch (Exception ex) {
                    appendSystemMessage("Connection closed.");
                }
            });

            readThread.start();
            setChatControlsEnabled(true);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "Failed to connect to server:\n" + e.getMessage(), "Connection Error", JOptionPane.ERROR_MESSAGE);
            appendSystemMessage("Could not connect: " + e.getMessage());
            cardLayout.show(cards, "login");
        }
    }

    // ===================== HANDLER: SERVER EVENTS =====================

    private void handleRoomList(String payload) {
        String[] parts = payload.isEmpty() ? new String[0] : payload.split(",");
        SwingUtilities.invokeLater(() -> {
            roomsModel.clear();
            roomNames.clear();
            for (String p : parts) {
                p = p.trim();
                if (p.isEmpty()) continue;

                String roomName;
                String display;

                if (p.contains("|")) {
                    String[] info = p.split("\\|");
                    roomName = info[0].trim();
                    StringBuilder sb = new StringBuilder(roomName);
                    sb.append("  (Owner: ");
                    if (info.length > 1) sb.append(info[1].trim());
                    if (info.length > 2) sb.append(", Members: ").append(info[2].trim());
                    sb.append(")");
                    display = sb.toString();
                } else {
                    roomName = p.split("\\s+")[0];
                    display = p;
                }

                roomNames.add(roomName);
                roomsModel.addElement(display);
            }
        });
    }

    private void handleMemberList(String payload) {
        String[] parts = payload.isEmpty() ? new String[0] : payload.split(",");
        SwingUtilities.invokeLater(() -> {
            memberNames.clear();
            String serverOwner = null;
            for (int i = 0; i < parts.length; i++) {
                String u = parts[i].trim();
                if (u.isEmpty()) continue;
                if (i == 0) serverOwner = u; 
                if (!memberNames.contains(u)) {
                    memberNames.add(u);
                }
            }
            // REVISI DINAMIS: Ambil owner langsung dari nama pertama yang diberikan server
            if (!isOwner && serverOwner != null) {
                currentRoomOwner = serverOwner;
            }
            updateOwnerControls();
            updateRoomInfoLabel();
        });
    }

    private void addIncomingMemberUpdate(String username) {
        SwingUtilities.invokeLater(() -> {
            if (!memberNames.contains(username)) {
                memberNames.add(username);
            }
            
            // REVISI DINAMIS: Jika belum terdaftar owner-nya, isi dengan nama pertama yang terbaca
            if (isOwner) {
                currentRoomOwner = currentUsername;
            } else if (currentRoomOwner == null) {
                currentRoomOwner = username; 
            }
            
            updateOwnerControls();
            updateRoomInfoLabel();
        });
    }

    private void triggerSilentInfoRequest() {
        try {
            if (out != null) {
                out.writeBytes("/info\n");
                out.flush();
            }
        } catch (Exception ignored) {}
    }

    private void handleKicked(String fromRoom) {
        SwingUtilities.invokeLater(() -> {
            if (currentRoom != null && currentRoom.equalsIgnoreCase(fromRoom)) {
                JOptionPane.showMessageDialog(frame,
                        "You have been removed from room '" + fromRoom + "' by the owner.",
                        "Kicked", JOptionPane.WARNING_MESSAGE);
                resetRoomState();
                cardLayout.show(cards, "main");
                setChatControlsEnabled(false);
                requestRoomList();
            }
        });
    }

    private void resetRoomState() {
        currentRoom = null;
        currentRoomOwner = null;
        isOwner = false;
        memberNames.clear();
        rebuildMembersPanel();
        roomField.setText("");
        mainRoomField.setText("");
        updateOwnerControls();
        updateRoomInfoLabel();
    }

    private void updateOwnerControls() {
        closeRoomBtn.setEnabled(isOwner && currentRoom != null);
        rebuildMembersPanel();
    }

    private void rebuildMembersPanel() {
        membersPanel.removeAll();
        
        if (currentUsername != null && !memberNames.contains(currentUsername)) {
            memberNames.add(0, currentUsername);
        }

        for (String member : memberNames) {
            JPanel row = new JPanel(new BorderLayout());
            row.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
            
            JLabel label = new JLabel(member);
            label.setOpaque(true);
            
            if (currentUsername != null && member.equals(currentUsername)) {
                label.setForeground(new Color(0x2E7D32)); 
                label.setText(member + " (You)");
            } else if (currentRoomOwner != null && member.equals(currentRoomOwner)) {
                // REVISI DINAMIS: Pewarnaan orange tag Owner berdasarkan variabel dinamis currentRoomOwner
                label.setForeground(new Color(0xD84315)); 
                label.setText(member + " (Owner)");
            } else {
                label.setForeground(new Color(0x1565C0)); 
            }
            
            row.add(label, BorderLayout.WEST);
            
            // Tombol Kick tampil dinamis untuk siapa pun selain diri kita sendiri
            if (currentUsername != null && !member.equals(currentUsername)) {
                JButton kickButton = new JButton("Kick");
                kickButton.setMargin(new Insets(2, 6, 2, 6));
                
                // Hanya menyala dan aktif jika kita terdeteksi sebagai owner ruangan saat ini
                if (isOwner) {
                    kickButton.setEnabled(true); 
                    kickButton.addActionListener(e -> {
                        int ok = JOptionPane.showConfirmDialog(frame,
                            "Kick user '" + member + "' from the room?",
                            "Confirm Kick", JOptionPane.YES_NO_OPTION);
                        if (ok == JOptionPane.YES_OPTION) {
                            kickUserByName(member);
                        }
                    });
                } else {
                    kickButton.setEnabled(false); 
                }
                row.add(kickButton, BorderLayout.EAST);
            }
            
            membersPanel.add(row);
        }
        membersPanel.revalidate();
        membersPanel.repaint();
    }

    private void updateRoomInfoLabel() {
        if (currentRoom == null) {
            roomInfoLabel.setText("Not in a room");
        } else {
            
            String ownerInfo = isOwner ? currentUsername : (currentRoomOwner != null ? currentRoomOwner : "?");
            String role = isOwner ? " (You are the owner)" : "";
            roomInfoLabel.setText("Room: " + currentRoom + " | Owner: " + ownerInfo + role);
        }
    }

    // ===================== LOGIN / CONNECTION =====================

    private void login() {
        String username = usernameField.getText().trim();
        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Please enter a username before continuing.", "Username Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        connectToServer(username);
        currentUsername = username;
        frame.setTitle("Chat Room Client - " + username);
        cardLayout.show(cards, "main");
        requestRoomList();
    }

    private void setChatControlsEnabled(boolean enabled) {
        sendBtn.setEnabled(enabled);
        inputField.setEnabled(enabled);
        joinBtn.setEnabled(enabled);
        listBtn.setEnabled(enabled);
        addBtn.setEnabled(enabled);
        exitBtn.setEnabled(enabled);
    }

    // ===================== CHAT ACTIONS =====================

    private void sendChat() {
        String text = inputField.getText().trim();
        if (text.isEmpty() || out == null) return;

        try {
            out.writeBytes(text + "\n");
            out.flush();
            inputField.setText("");
        } catch (Exception e) {
            appendSystemMessage("Failed to send message: " + e.getMessage());
        }
    }

    private void closeRoom() {
        if (currentRoom == null || out == null) return;
        if (!isOwner) {
            JOptionPane.showMessageDialog(frame, "Only the room owner can close this room.", "Not Allowed", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int ok = JOptionPane.showConfirmDialog(frame,
                "Close room '" + currentRoom + "'? All members will be disconnected from this room.",
                "Confirm Close Room", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;

        try {
            out.writeBytes("/closeroom\n");
            out.flush();
        } catch (Exception e) {
            appendSystemMessage("Failed to close room: " + e.getMessage());
        }
    }

    private void kickUserByName(String user) {
        if (currentRoom == null || out == null) return;
        if (!isOwner) {
            JOptionPane.showMessageDialog(frame, "Only the room owner can kick users.", "Not Allowed", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            out.writeBytes("/kick " + user + "\n");
            out.flush();
            appendSystemMessage("Kick request sent for user: " + user);
            
            memberNames.remove(user);
            rebuildMembersPanel();
        } catch (Exception e) {
            appendSystemMessage("Failed to kick user: " + e.getMessage());
        }
    }

    private void joinRoom() {
        String room = roomField.getText().trim();
        if (room.isEmpty()) return;
        if (out == null) {
            JOptionPane.showMessageDialog(frame, "Not connected to server.", "Not Connected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            out.writeBytes("/join " + room + "\n");
            out.flush();

            currentRoom = room;
            currentRoomOwner = null; // Biarkan dibaca dinamis lewat respons server 
            isOwner = false; 
            
            memberNames.clear();
            if (currentUsername != null) memberNames.add(currentUsername);
            
            updateOwnerControls();
            updateRoomInfoLabel();

            chatArea.setText("<html><body></body></html>");
            appendSystemMessage("Joining room: " + room + " ...");

            cardLayout.show(cards, "chat");
            setChatControlsEnabled(true);
            inputField.requestFocusInWindow();

            out.writeBytes("/info\n");
            out.flush();
        } catch (Exception e) {
            appendSystemMessage("Failed to join room: " + e.getMessage());
        }
    }

    private void requestRoomList() {
        if (out == null) return;
        try {
            out.writeBytes("/listroom\n");
            out.flush();
        } catch (Exception e) {
            appendSystemMessage("Failed to request room list: " + e.getMessage());
        }
    }

    private void addRoom() {
        String room = roomField.getText().trim();
        if (room.isEmpty()) return;
        if (out == null) {
            JOptionPane.showMessageDialog(frame, "Not connected to server.", "Not Connected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            out.writeBytes("/addroom " + room + "\n");
            out.flush();

            currentRoom = room;
            currentRoomOwner = currentUsername; // Pengisi owner pertama kali secara dinamis
            isOwner = true; 
            
            memberNames.clear();
            if (currentUsername != null) memberNames.add(currentUsername);

            chatArea.setText("<html><body></body></html>");
            appendSystemMessage("Created and joining room: " + room + " ...");

            cardLayout.show(cards, "chat");
            setChatControlsEnabled(true);
            inputField.requestFocusInWindow();

            updateOwnerControls();
            updateRoomInfoLabel();

            out.writeBytes("/info\n");
            out.flush();
        } catch (Exception e) {
            appendSystemMessage("Failed to add room: " + e.getMessage());
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
        appendSystemMessage("Disconnected.");
        frame.dispose();
    }

    private void leaveRoomToMain() {
        try {
            if (out != null) {
                out.writeBytes("/leave\n");
                out.flush();
            }
        } catch (Exception ignored) {}

        resetRoomState();
        setChatControlsEnabled(false);
        cardLayout.show(cards, "main");
        requestRoomList();
    }

    private void appendSystemMessage(String msg) {
        SwingUtilities.invokeLater(() -> {
            String formatted = "<div style='text-align:center; color:gray; margin:4px;'>"
                    + escapeHtml(msg) + "</div>";
            appendHtml(formatted);
        });
    }

    // ===================== CHAT RENDERING =====================

    private void appendChatMessage(String msg) {
        SwingUtilities.invokeLater(() -> {
            String formatted;
            int colon = msg.indexOf(":");
            if (colon > 0) {
                String sender = msg.substring(0, colon).trim();
                String rest = msg.substring(colon + 1).trim();

                if (currentUsername != null && sender.equals(currentUsername)) {
                    formatted = "<div style='text-align:right; margin:4px 8px;'>"
                        + "<span style='background-color:#DCF8C6; padding:4px 10px; border-radius:10px;'>"
                        + "<span style='color:#1B5E20;'><b>You</b></span>: " + escapeHtml(rest)
                        + "</span></div>";
                } else {
                    formatted = "<div style='text-align:left; margin:4px 8px;'>"
                        + "<span style='background-color:#ECECEC; padding:4px 10px; border-radius:10px;'>"
                        + "<span style='color:#0D47A1;'><b>" + escapeHtml(sender) + "</b></span>: " + escapeHtml(rest)
                        + "</span></div>";
                }
            } else {
                formatted = "<div style='text-align:center; color:gray; margin:4px;'>"
                    + escapeHtml(msg) + "</div>";
            }
            appendHtml(formatted);
        });
    }

    private void appendHtml(String formatted) {
        try {
            String body = chatArea.getText();
            int idx = body.lastIndexOf("</body>");
            if (idx >= 0) {
                body = body.substring(0, idx) + formatted + body.substring(idx);
            } else {
                body += formatted;
            }
            chatArea.setText(body);
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
        } catch (Exception e) {
            chatArea.setText(chatArea.getText() + "\n" + formatted);
        }
    }

    private static String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    public static void main(String[] args) {
        System.setProperty("javax.net.ssl.trustStore", "serverkeystore.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", "123456");

        SwingUtilities.invokeLater(() -> new ClientGUI());
    }
}