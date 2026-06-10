import javax.net.ssl.*;

public class Server {

    public static void main(String[] args) throws Exception {
        // Membuat factory untuk SSL Server Socket (yang bikin koneksi aman (HTTPS versi socket))
        SSLServerSocketFactory factory =
                (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();

        //Membuat SSL Server Socket di port 6789 Server akan "listen" koneksi dari client di port ini
        SSLServerSocket serverSocket =
                (SSLServerSocket) factory.createServerSocket(6789);

        System.out.println("SSL Chat Room Server Started...");

        //loop agar server selalu hidup
        while (true) {
            //Menunggu client masuk (blocking), kalau ada client connect → baru lanjut
            SSLSocket socket = (SSLSocket) serverSocket.accept();
            //setiap client yang masuk dibuat 1 handler baru agar bisa multi-user (tidak saling blocking)
            ClientHandler handler = new ClientHandler(socket);
            //jalankan handler di thread baru agar server tetap bisa terima client lain
            Thread t = new Thread(handler);
            t.start();
        }
    }
}