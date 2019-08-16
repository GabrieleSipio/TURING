import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("Duplicates")

/* classe che usa le API RMI per gestire le operazioni di registrazione e gestisce il recupero dei dati dai database di utenti e documenti all'avvio di TURING
* @author Gabriele Sipione mat:534248*/

public class Register extends UnicastRemoteObject implements IRegister {

    private ConcurrentHashMap<String, Data> db;
    private ConcurrentHashMap<String,Docs> docs;
    public Register() throws RemoteException{
        try {
            /*quando viene chiamato il costruttore vado a prenderemi le informazioni relatve agli utenti e ai documenti per poter recuperare le loro attività*/
            File file = new File("DataBase.txt");
            FileInputStream f = new FileInputStream(file);
            ObjectInputStream s = new ObjectInputStream(f);
            db = (ConcurrentHashMap<String, Data>) s.readObject();
            File doc = new File("Documents.txt");
            FileInputStream d = new FileInputStream(doc);
            ObjectInputStream ois = new ObjectInputStream(d);
            docs = (ConcurrentHashMap<String, Docs>) ois.readObject();
            s.close();
            ois.close();
        } catch (IOException | ClassNotFoundException e) {

            db=new ConcurrentHashMap<>();
            docs=new ConcurrentHashMap<>();
        }
    }

    /*metodo usato per restituire la struttura che contiene le informazioni relative agli utenti*/
    public ConcurrentHashMap<String, Data> getDB(){
        return db;
    }

    /*metodo usato per restituire la struttura che contiene le informazioni relative ai documenti presenti nel sistema*/
    public ConcurrentHashMap<String, Docs> getDocs() {
        return docs;
    }


    @Override
    public synchronized boolean register(String name, String password) throws RemoteException {
        if (db.containsKey(name)){
            /*username inserito è già presente in TURING*/
            return false;
        }
        /*username è valido quindi lo inserisco nella struttura db e serializzo l'informazione del documento Database.txt per mantenerlo consistente*/
        Data data=new Data(password);
        db.put(name, data);
        try {
            File file = new File("DataBase.txt");
            FileOutputStream f = new FileOutputStream(file);
            ObjectOutputStream s = new ObjectOutputStream(f);
            s.writeObject(db);
            s.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public ArrayList<Docs> getMyInvitation(String name) throws RemoteException {
        return db.get(name).showInvitedDocs();
    }

    @Override
    public ArrayList<Docs> getMyDocs(String name) throws RemoteException {
       return db.get(name).showDocs();
    }

    @Override
    public ArrayList<String> getAllDocs(String name) throws RemoteException {
        return db.get(name).getDocument();
    }

    @Override
    public ArrayList<ChatterBox> getHistory(String docName) throws RemoteException {
        return docs.get(docName).getChatBuffer();
    }

    @Override
    public InetAddress getDocAddress(String docName) throws RemoteException, UnknownHostException {
        return docs.get(docName).getInetAddress();
    }

    @Override
    public String isEdited(String docName, int sez) throws RemoteException {
        if (docs.get(docName).isBusy(sez)){
            /*se la sezione è in editing lo notifico*/
            return "[Editing]";
        }
        return "";
    }

    @Override
    public boolean isEditing(String name) throws RemoteException{
        return !(db.get(name).getInEditing().equals("noDoc"));
    }
}
