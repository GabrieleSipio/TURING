import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/*classe che implementa il client di TURING
* @author Gabriele Sipione mat:534248*/

public class TURINGClient {
    public static void main(String[] args) {
        try {
            /*vado a prendere il riferimento remoto al database degli utenti caricato dal server*/
            Registry registry = LocateRegistry.getRegistry(1919);
            IRegister stub=(IRegister) registry.lookup("Database");
            /*apro la socket channel e mi collenetto sulla porta 2020*/
            SocketChannel clientSC= SocketChannel.open(new InetSocketAddress("localhost",2020));
            /*chiamo il frame di utenticazione di TURING*/
            new PrincipalFrame(stub,clientSC);
        } catch (NotBoundException | IOException e) {
            System.err.println("[TURING client] server has crashed... try to restart you're client later");
        }
    }
}
