import javax.swing.*;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

/*classe usata per gestire la chat di gruppo quando si lavora all'editing di un documento
* @author Gabriele Sipione mat:534248*/

@SuppressWarnings("Duplicates")
public class ChatHandler implements Runnable {
    private JTextArea chat;
    private byte[] buffer;
    private MulticastSocket multicastSocket;

    public ChatHandler(JTextArea chat, InetAddress inetAddress) {
        this.chat = chat;
        this.buffer=new byte[2048];
        try {
            /*mi metto in ascolto sulla porta 5252 e mi unisco al gruppo multicast associato al documento*/
            this.multicastSocket=new MulticastSocket(5252);
            this.multicastSocket.joinGroup(inetAddress);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*metodo usato per aggiornare il tempo reale la chat del gruppo di editors di un documento*/
    @Override
    public void run() {
        while(true){
            try {
                DatagramPacket dp = new DatagramPacket(buffer, buffer.length);
                if (!multicastSocket.isClosed()) {
                    multicastSocket.receive(dp);
                    String newMessage=new String(buffer,0,buffer.length);
                    chat.append(newMessage + "\n");
                    buffer=new byte[2048];
                }
            } catch (IOException e) {
                multicastSocket.close();
                multicastSocket.disconnect();
            }
        }
    }

    /*metodo usato per chiudere la socket channel quando viene chiuso il pannelo di editing*/
    public void terminate(){
        multicastSocket.close();
        multicastSocket.disconnect();
    }
}
