error id: file:///D:/Data/Life/UNPAR/SEM%206/JARKOM/tugas/Tubes/Server.java:java/lang/System#
file:///D:/Data/Life/UNPAR/SEM%206/JARKOM/tugas/Tubes/Server.java
empty definition using pc, found symbol in pc: java/lang/System#
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 523
uri: file:///D:/Data/Life/UNPAR/SEM%206/JARKOM/tugas/Tubes/Server.java
text:
```scala
import java.io.*;
import javax.net.ssl.*;

public class Server {
    public static void main (String argv[]) throws Exception
    {
        String clientSentence;
        String capitalizedSentence;

        // SSL Server Socket Factory untuk membuat SSL Server Socket
        SSLServerSocketFactory factory =
                (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();

        SSLServerSocket welcomeSocket =
                (SSLServerSocket) factory.createServerSocket(6789);

        S@@ystem.out.println("SSL TCP server started...");

        while (true)
        {
            // accept connection dari client
            SSLSocket connectionSocket = (SSLSocket) welcomeSocket.accept();

            System.out.println("Accept connection from "
                + connectionSocket.getInetAddress().getHostAddress()
                + " on port " + welcomeSocket.getLocalPort());

            BufferedReader inFromClient = new BufferedReader(
                    new InputStreamReader(connectionSocket.getInputStream()));

            DataOutputStream outToClient =
                    new DataOutputStream(connectionSocket.getOutputStream());

            clientSentence = inFromClient.readLine();

            capitalizedSentence = clientSentence.toUpperCase() + '\n';

            outToClient.writeBytes(capitalizedSentence);
        }
    }
}
```


#### Short summary: 

empty definition using pc, found symbol in pc: java/lang/System#