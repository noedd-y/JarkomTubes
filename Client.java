import java.io.*;
import javax.net.ssl.*;

public class Client {

    public static void main(String[] args) throws Exception {

        BufferedReader inFromUser =
                new BufferedReader(new InputStreamReader(System.in));

        SSLSocketFactory factory =
                (SSLSocketFactory) SSLSocketFactory.getDefault();

        SSLSocket socket =
                (SSLSocket) factory.createSocket("127.0.0.1", 6789);

        DataOutputStream out =
                new DataOutputStream(socket.getOutputStream());

        BufferedReader in =
                new BufferedReader(new InputStreamReader(socket.getInputStream()));

        out.writeBytes("/join room\n");
        out.flush();

        System.out.println("=== Connected to Chat Server ===");

        //THREAD untuk TERIMA PESAN (real-time receiver)
        Thread readThread = new Thread(() -> {
            try {
                String msg;
                while ((msg = in.readLine()) != null) {
                    System.out.println("\n[Friend] " + msg);
                    System.out.print("You: ");
                }
            } catch (Exception e) {
                System.out.println("Connection closed.");
            }
        });

        readThread.start();

        //MAIN THREAD untuk KIRIM PESAN
        String msg;
        while (true) {
            System.out.print("You: ");
            msg = inFromUser.readLine();

            if (msg.equalsIgnoreCase("exit")) {
                socket.close();
                break;
            }

            out.writeBytes(msg + "\n");
            out.flush();
        }
    }
}