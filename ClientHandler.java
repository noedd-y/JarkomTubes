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
        try {

            in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));

            out = new DataOutputStream(socket.getOutputStream());

            System.out.println("Client connected: " +
                    socket.getInetAddress().getHostAddress());

            String msg;

            while ((msg = in.readLine()) != null) {

                System.out.println("Received: " + msg);

                //1. COMMAND JOIN ROOM
                if (msg.startsWith("/join ")) {

                    String roomName = msg.substring(6);

                    ChatRoom.joinRoom(roomName, out);

                    out.writeBytes("Joined room: " + roomName + "\n");
                    out.flush();
                }

                //2. EXIT ROOM
                else if (msg.equals("/leave")) {

                    ChatRoom.leaveRoom(out);

                    out.writeBytes("Left room\n");
                    out.flush();
                }
                //3. Menampilkan semua room yang tersedia
                else if (msg.equals("/listroom")) {

                    String roomList = ChatRoom.listRooms();

                    out.writeBytes(roomList + "\n");
                    out.flush();
                }

                //4. CHAT MESSAGE (NORMAL MESSAGE)
                else {

                    ChatRoom.broadcast(msg, out);
                }
            }

        } catch (Exception e) {
            System.out.println("Client disconnected");
            ChatRoom.leaveRoom(out);
        }
    }
}