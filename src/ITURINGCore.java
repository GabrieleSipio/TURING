import java.net.SocketAddress;
import java.rmi.RemoteException;
import java.util.concurrent.ConcurrentHashMap;

/*Interfaccia che definisce i metodi principali di I/O di TURING implementata nella classe TURINCore
* @author: Gabriele Sipione mat:534248*/

public interface ITURINGCore {

    //metodo per effettuare l'autenticazione di un utente con nome  "name" e con password "password", se l'utente supera i controlli lo aggiungo all'online set con il suo IP
    int login(String name, String password, SocketAddress IP);
    //metodo per effettuare il logout dell'utente con nome "name" aggiornando il database con tutte le modifiche che l'utente ha fatto durante la sessione portandolo in uno stato consistente
    void logout(String name) throws RemoteException;
    //metodo per creare un nuovo documento con nome "dirName" con "numSezioni" sezioni e aggiungerlo al documento set di "owner"
    int create(String owner, String dirName, int numSezioni);
    //metodo per invitare un utente di nome "name" a collaborare all'editing del documento "doc"
    int invite(String name, String Doc);
    //metodo per inviare una sezione "sez" appartenente al documento "scr" attraverso la socket channel
    void showSec(String src, String sec);
    //metodo per inviare un intero documento doc attraverso la socket channel
    void showDoc(String docName);
    //metodo per inviare una sezione "sez" appartenente al documento "src" all'utente user
    int edit(String src, String sec, int index, String user);
    //metodo per dichiarare la fine dell'editing di un documento
    void endEdit(String modifiedDoc, int index, String user,String src,String sec);
    //metodo che mi indica se un utente è effettivamente online su TURING
    boolean isIn(String name);
    //metodo usato per restituire l’intero set dei documenti presenti in TURING
    ConcurrentHashMap<String, Docs> getDocumentSet();
    //metodo usato per individuare l’indirizzo IP di un utente connesso a TURING
    SocketAddress getAddress(String name);
    //metodo per eliminare, eventualmente, un utente il quale client si è chiuso per motivi anomali evitando di portare modifiche parziali al database
    void cleanOnlineSet (SocketAddress socket);
    //metodo per aggiungere un messeggio, inviato da un utente "user", al buffer dei messaggi del documento "docName"
    void addNewChatMessage(String user,String docName,String sect,String message);

}
