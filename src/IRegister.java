import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
/*interfaccia che defineisce i metodi per gestire le operazioni di registrazione e ottenere le strutture dati per le operazioni di TURING
* @author Gabriele Sipione mat:534248*/

public interface IRegister extends Remote {

    /*metodo usato per registrare un utente di nome "name" con una password "password" a TURING*/
    boolean register(String name, String password) throws RemoteException;
    /*metodo usato per ottenere il set dei documenti a cui un utente di nome "name" è stato invitato a collaborare*/
    ArrayList<Docs> getMyInvitation(String name) throws RemoteException;
    /*metodo usato per ottenere il set dei documenti di proprietà di un utente di nome "name" */
    ArrayList<Docs> getMyDocs(String name) throws RemoteException;
    /*metodo usato per ottenere il set dei documenti derivato dall'unione dei set di documenti a cui un utente di nome "name" è stato invitato a collaborare e di quelli che sono di sua proprietà*/
    ArrayList<String> getAllDocs(String name) throws  RemoteException;
    /*metodo usato per ottenere la chatHistory di un documento per caricarla quando si fa l'editing*/
    ArrayList<ChatterBox> getHistory(String docName) throws RemoteException;
    /*metodo usato per ottenere l'indirizzo multicast associato ad un documento*/
    InetAddress getDocAddress(String docName) throws RemoteException, UnknownHostException;
    /*metodo usato per sapere se una sezione è in editing o meno*/
    String isEdited(String name,int sez) throws RemoteException;
    /*metodo usato per sapere se un utente è impegnato in un operazione di editing*/
    boolean isEditing(String name) throws RemoteException;

}
