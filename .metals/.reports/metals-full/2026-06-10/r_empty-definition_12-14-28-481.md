error id: file:///D:/Data/Life/UNPAR/SEM%206/JARKOM/tugas/Tubes/Client.java:javax/net/ssl/SSLSocketFactory#
file:///D:/Data/Life/UNPAR/SEM%206/JARKOM/tugas/Tubes/Client.java
empty definition using pc, found symbol in pc: javax/net/ssl/SSLSocketFactory#
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 389
uri: file:///D:/Data/Life/UNPAR/SEM%206/JARKOM/tugas/Tubes/Client.java
text:
```scala
import java.io.*;
import javax.net.ssl.*;

public class Client {
    public static void main (String argv[]) throws Exception
    {
        String sentence;
        String modifiedSentence;

        BufferedReader inFromUser =
                new BufferedReader(new InputStreamReader(System.in));

        // SSL Socket Factory untuk membuat SSL Socket
        SSLSocketFactor@@y factory =
                (SSLSocketFactory) SSLSocketFactory.getDefault();

        SSLSocket clientSocket =
                (SSLSocket) factory.createSocket("127.0.0.1", 6789);

        DataOutputStream outToServer =
                new DataOutputStream(clientSocket.getOutputStream());

        BufferedReader inFromServer =
                new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        sentence = inFromUser.readLine();

        outToServer.writeBytes(sentence + '\n');

        modifiedSentence = inFromServer.readLine();

        System.out.println("From Server " + modifiedSentence);

        clientSocket.close();
    }
}
```


#### Short summary: 

empty definition using pc, found symbol in pc: javax/net/ssl/SSLSocketFactory#