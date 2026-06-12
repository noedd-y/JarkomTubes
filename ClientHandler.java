import java.io.*;
import javax.net.ssl.*;

public class ClientHandler implements Runnable {

    private SSLSocket socket;
    private BufferedReader in;
    private DataOutputStream out;

    public ClientHandler(SSLSocket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        User user = null;
        try {

            in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));

            out = new DataOutputStream(socket.getOutputStream());

            user = new User(out);
            // RoomHandler.users.put(out, user);

            System.out.println("Client connected: " +
                    socket.getInetAddress().getHostAddress());

            String msg;

            while ((msg = in.readLine()) != null) {

                System.out.println("Received: " + msg);

                // set username
                if (msg.startsWith("/username ")) {
                    String username = msg.substring(10).trim();
                    if (!username.isEmpty()) {
                        RoomHandler.setUserName(user, username);
                        out.writeBytes("Username set to: " + username + "\n");
                        out.flush();
                    }
                }
                // join room
                else if (msg.startsWith("/join ")) {

                    String roomName = msg.substring(6).trim();

                    RoomHandler.joinRoom(roomName, user);

                    out.writeBytes("Joined room: " + roomName + "\n");
                    out.flush();

                    // ========================================================
                    // REVISI 1: BROADCAST MEMBER LIST TERBARU KE SEMUA ORANG
                    // ========================================================
                    Room currentRoom = user.getCurrentRoom();
                    if (currentRoom != null) {
                        StringBuilder sb = new StringBuilder();
                        // Baris pertama: beri tahu Client siapa Owner dinamisnya saat ini
                        if (currentRoom.getOwner() != null) {
                            sb.append(currentRoom.getOwner().getUsername()).append("\n");
                        }
                        // Baris berikutnya: kumpulkan semua member biasa
                        for (User listUser : currentRoom.getUsers()) {
                            if (currentRoom.getOwner() == listUser) {
                                continue; // Lewati owner karena sudah di baris pertama
                            }
                            sb.append(listUser.getUsername()).append("\n");
                        }

                        // Broadcast data member ke semua orang di room tersebut
                        String memberListData = sb.toString();
                        for (User u : currentRoom.getUsers()) {
                            u.getOutputStream().writeBytes(memberListData);
                            u.getOutputStream().flush();
                        }
                    }
                    // ========================================================
                }
                // exit room
                else if (msg.equals("/leave")) {

                    RoomHandler.leaveRoom(user);

                    out.writeBytes("Left room\n");
                    out.flush();
                }
                // menampilkan semua room yang tersedia
                else if (msg.equals("/listroom")) {

                    String roomList = RoomHandler.listRooms();

                    out.writeBytes(roomList + "\n");
                    out.flush();
                }
                // Menampilkan isi room
                else if (msg.equals("/info")){
                    Room currentRoom = user.getCurrentRoom();
                    if (currentRoom != null) {
                        StringBuilder sb = new StringBuilder();
                        
                        // Letakkan Owner di baris pertama
                        if (currentRoom.getOwner() != null) {
                            sb.append(currentRoom.getOwner().getUsername()).append("\n");
                        }
                        
                        // REVISI 2: Perbaikan perbandingan objek loop (listUser, bukan user)
                        for (User listUser : currentRoom.getUsers()) {
                            if (currentRoom.getOwner() == listUser) {
                                continue; 
                            }
                            sb.append(listUser.getUsername()).append("\n");
                        }
                        out.writeBytes(sb.toString());
                        out.flush();
                    }
                }
                // untuk kick user dari room 
                else if(msg.startsWith("/kick ")) {
                    String username =
                        msg.substring(6).trim();

                    RoomHandler.kickUser(user, username);
                }
                //untuk owner menutup room
                else if(msg.equals("/closeroom")) {
                    RoomHandler.closeRoom(user);
                    out.writeBytes(
                        "Room closed successfully\n"
                    );
                    out.flush();
                }
                // chat normal message
                else {
                    RoomHandler.broadcast(msg, user);
                }
            }

        } catch (Exception e) {
            System.out.println("Client disconnected");
            RoomHandler.leaveRoom(user);
        }
    }
}