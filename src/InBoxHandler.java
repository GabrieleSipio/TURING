import javax.swing.*;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.net.SocketException;

/*Handler che ha il compito di rimanere in ascolto in modo costante per gestire il log è notificare ogni volta che si viene invitati
* a collaborare ad un documento
* @author Gabriele Sipione mat:534248*/

@SuppressWarnings("Duplicates")
public class InBoxHandler implements Runnable {
    private JTextArea inbox;
    private DatagramSocket ds;
    private byte[] buffer;

    public InBoxHandler(JTextArea mailbox, SocketAddress sa){
        this.inbox=mailbox;
        buffer=new byte[1024];
        try {
            ds=new DatagramSocket(sa);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    @Override
    /*ogni  volta che il server manda all'utente a cui corrisponde la socket address un nuovo documento a cui è stato invitato
    * lo aggiungo in realtime alla mailbox nel workspace*/
    public void run() {
        while (true){
            try {
                DatagramPacket dp= new DatagramPacket(buffer,buffer.length);
                if (!ds.isClosed()){
                    ds.receive(dp);
                    /*una volta ricevuto il documento al quale si è invitati lo si appende alla TextArea del log*/
                    String news= new String(buffer,0,buffer.length);
                    inbox.append(news + "\n");
                }
            } catch (IOException e) {
                ds.close();
                ds.disconnect();
            }

        }
    }

    /*metodo usato per chiudere la DatagramSocket quando il Workspace dell'utente si chiude */
    public void shutDown(){
        ds.close();
        ds.disconnect();
    }
}
