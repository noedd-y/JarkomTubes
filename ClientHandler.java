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
                        // RoomHandler.setUserName(user, username);
                        user.setUsername(username);
                        out.writeBytes("Username set to: " + username + "\n");
                        out.flush();
                    }
                }
                else if (msg.startsWith("/addroom ")) {

                    String roomName = msg.substring(9);

                    if(RoomHandler.createRoom(roomName, user) != null){
                        out.writeBytes("Created room: " + roomName + "\n");
                        out.flush();
                    }
                    else{
                        out.writeBytes("Unable to create room: " + roomName + "\n");
                        out.flush();
                    }
                    
                }
                // join room
                else if (msg.startsWith("/join ")) {

                    String roomName = msg.substring(6);

                    if(RoomHandler.joinRoom(roomName, user)){
                        out.writeBytes("Joined room: " + roomName + "\n");
                        out.flush();
                    }
                    else {
                        out.writeBytes("Unable to join: " + roomName + "\n");
                        out.flush();
                    }
                    
                }
                // exit room
                else if (msg.equals("/leave")) {

                    if(RoomHandler.leaveRoom(user)){
                        out.writeBytes("Left room\n");
                        out.flush();
                    }
                    else {
                        out.writeBytes("Unable to leave room\n");
                        out.flush();
                    }
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
                    
                    String result = RoomHandler.infoRoom(currentRoom);

                    out.writeBytes(result);
                    out.flush();
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