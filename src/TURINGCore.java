
import java.io.*;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

@SuppressWarnings("Duplicates")
 /*Classe che gestisce le operazioni principali di I/O di TURING, mantiene al suo interno un set riservato agli utienti online che contiene il loro IP quando si autenticano in TURING,
 *un database con cui interagisce quando vengono fatte operazioni di I/O  e un database di tutti i documenti presenti nel sistema con tutte le loro caratteristiche
 * @author Gabriele Sipione mat:534248*/

public class TURINGCore implements ITURINGCore {

    private ConcurrentHashMap<String, Data> db;
    private ConcurrentHashMap<String,SocketAddress> onlineusr;
    private ConcurrentHashMap<String, Docs> documentSet;

    public TURINGCore(ConcurrentHashMap<String,Data> db,ConcurrentHashMap<String,Docs> documentSet) {
        this.db=db;
        onlineusr=new ConcurrentHashMap<>();
        this.documentSet=documentSet;
    }

    @Override
    public boolean isIn(String name){
        return onlineusr.containsKey(name);
   }

    @Override
    public SocketAddress getAddress(String name) {
        return onlineusr.get(name);
    }

    @Override
    public void cleanOnlineSet (SocketAddress socket){
        String strKey = null;
        /*controllo tutto l'entry set di onlinerusr fino a che non trovo una corrispondenza con l'indirizzo del client che è crashato*/
        for(ConcurrentHashMap.Entry entry: onlineusr.entrySet()){
            if(socket.equals(entry.getValue())){
                strKey = entry.getKey().toString();
                break; //faccio un break perchè questo vuol dire che ho trovato quello che stavo cercando
            }
        }

       if (strKey!=null && onlineusr.containsKey(strKey)){
           /*se sono entrato in questo ramo vuol dire che ho trovato una corrispondenza nell'entrySet, e che quell'utente era online prima di crashare
           * quindi faccio la remove dall'online set (evito di chiamare l'operazione di logout perchè così evito ci siano scritture parziali)*/
           if (!db.get(strKey).getInEditing().equals("noDoc")){
               documentSet.get(db.get(strKey).getInEditing()).removeEditor(strKey,documentSet.get(db.get(strKey).getInEditing()).getEditSection(strKey));
               db.get(strKey).setInEditing("noDoc");
           }
           onlineusr.remove(strKey);
       }
       try {
           /*aggiorno comunque il database dei documenti in caso l'utente ne abbia creat di nuovi al momento dell'arresto anomalo*/
           File file = new File("DataBase.txt");
           FileOutputStream f = new FileOutputStream(file);
           ObjectOutputStream s = new ObjectOutputStream(f);
           s.writeObject(db);
           s.close();
           File doc = new File("Documents.txt");
           FileOutputStream d = new FileOutputStream(doc);
           ObjectOutputStream ois = new ObjectOutputStream(d);
           ois.writeObject(documentSet);
           ois.close();
       } catch (IOException e) {
           e.printStackTrace();
       }
    }

    @Override
    public ConcurrentHashMap<String, Docs> getDocumentSet(){
        return documentSet;
    }

    @Override
    public synchronized int login(String name, String password, SocketAddress IP) {
        if (db.containsKey(name) && db.get(name).getPasswd().equals(password)){
            if (!onlineusr.containsKey(name)){
                  onlineusr.put(name,IP);
                //l'operazione di login si è conclusa con successo
                return 0;
            }
            //lo username immesso è già online
            return 1;
        }
        //lo username o la password sono errati
        return -1;
    }

    @Override
    public synchronized void logout(String name) {

        if(!db.get(name).getInEditing().equals("noDoc")){
        /*se al momento del logout l'utente stava editando un documento lo rimuovo dall'editor set e imposto il fatto che non sta più editando nessun documento*/
            documentSet.get(db.get(name).getInEditing()).removeEditor(name,documentSet.get(db.get(name).getInEditing()).getEditSection(name));
            db.get(name).setInEditing("noDoc");
        }
        /*rimuovo l'utente "name" dall'online set*/
        onlineusr.remove(name);
        try {
            /*aggiorno il database con tutte le azioni che l'utente ha compiuto prima di effettuare il logout*/
            File file = new File("DataBase.txt");
            FileOutputStream f = new FileOutputStream(file);
            ObjectOutputStream s = new ObjectOutputStream(f);
            s.writeObject(db);

            File doc = new File("Documents.txt");
            FileOutputStream d = new FileOutputStream(doc);
            ObjectOutputStream ois = new ObjectOutputStream(d);
            ois.writeObject(documentSet);
            s.close();
            ois.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized int create(String owner, String dirName, int numSezioni){
        File dir = new File("src\\Documents", dirName);
        if (dir.exists()) {
            //esiste già una directory con il nome immesso
            return -1;
        }
        if (dir.mkdirs()) {
            //l'operazione di creazione è andata a buon fine
            for (int i = 0; i <= numSezioni; i++) {
                File file = new File(dir.getPath(), "Section" + (i+1) + ".txt");
                try {
                    if (!file.createNewFile()) {
                        //il file esiste già
                        return 0;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            /*l'operazione di aggiornamento del document set è synchronized di modo da evitare race conditions quando vengono creati
            * più documenti contemporaneamente*/
            Docs newDoc=new Docs(dirName,numSezioni);
            db.get(owner).addNewDoc(newDoc);
            documentSet.put(dirName,newDoc);
            return 1;
        }
        //la directory non è stata creata
        return 0;
    }

    @Override
    public int invite(String name, String doc) {
        if (!db.containsKey(name)){
            //l'utente invitato non esiste
            return -1;
        }
        else{
            for(Docs d : db.get(name).showInvitedDocs()){
                if(d.getDocName().equals(doc))
                    //l'utente invitato sta già partecipando all'editing del documento
                    return 1;
            }
            //aggiungo il documento al set invitedDocs dell'utente "name"
            db.get(name).addInvitedDoc(documentSet.get(doc));
            return 0;
        }
    }

    @Override
    public void showSec(String src, String sec) {
        try{
            /*apro la socket channel e mi connetto per mandare il file tramite la porta 2000*/
            SocketChannel fileSender=SocketChannel.open();
            fileSender.connect(new InetSocketAddress("localhost",2000));
            ByteBuffer buff=ByteBuffer.allocate(1024);
            Path path=Paths.get(src+"\\"+sec);
            FileChannel outChannel=FileChannel.open(path, StandardOpenOption.READ);
            buff.clear();
            /*mando riga per riga il la sezione che mi è stata richiesta*/
            while (outChannel.read(buff) > 0) {
                buff.flip();
                fileSender.write(buff);
                buff.clear();
            }
            /*chiusdo la socket chanel appena ho finito*/
            fileSender.close();
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void showDoc(String docName) {
        try{
            /*apro la socket channel per inviare il documento tramite la porta 2000*/
            SocketChannel fileSender=SocketChannel.open();
            fileSender.connect(new InetSocketAddress("localhost",2000));
            ByteBuffer buff=ByteBuffer.allocate(1024);
            String source="src\\Documents\\"+docName;
            File dir = new File(source);
            /*aperto il documento tramite il path uso il metodo listFiles per farmi restituire un array di File che poi ordino con il comparator per averli nell'ordine giusto*/
            File[] dirList = dir.listFiles();
            Arrays.sort(dirList,Comparator.comparing(File::getName,new FilenameComparator()));

            int i=0;
            for (File child : dirList) {
                Path path = Paths.get(source + "\\" + child.getName());
                FileChannel outChannel = FileChannel.open(path, StandardOpenOption.READ);
                String separator;
                if (i==0){
                    separator="------------"+child.getName()+"------------";
                }
                else{
                    separator="\n\n------------"+child.getName()+"------------";
                }
                if (documentSet.get(docName).isBusy(i++)){
                    separator+="[Editing]\n\n";
                }
                else{
                    separator+="\n\n";
                }
                byte[] bytes=separator.getBytes();
                fileSender.write(ByteBuffer.wrap(bytes));
                buff.clear();
                /*mando, riga per riga ogni sezione che compone il documento*/
                while (outChannel.read(buff) > 0) {
                    buff.flip();
                    fileSender.write(buff);
                    buff.clear();
                }
            }
            fileSender.close();
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized int edit(String src, String sec, int numSec, String user) {
        /*tramite la segnatura synchronized mi assicuro che non ci siano accessi concorrenti su una stessa sezione*/
            int result=0;
            try {
                SocketChannel fileSender = SocketChannel.open();
                fileSender.socket().setReuseAddress(true);
                fileSender.connect(new InetSocketAddress("localhost", 9000));
                /*utilizzando il metodo isBusy mi assicuro che la sezione che voglio modificare non sia di fatti in editing da parte di un altro utente */
                if (!documentSet.get(src).isBusy(numSec)) {
                    ByteBuffer buff = ByteBuffer.allocate(1024);
                    Path path = Paths.get("src\\Documents\\" + src + "\\" + sec);
                    FileChannel outChannel = FileChannel.open(path, StandardOpenOption.READ);
                    buff.clear();
                    while (outChannel.read(buff) > 0) {
                        buff.flip();
                        fileSender.write(buff);
                        buff.clear();
                    }
                    /*una volta mandato il file con successo al client aggiungo lutente che ha fatto la richiesta di edting al set Editors
                    * e imposto il valore dell'array inEdit, contenuto in docs, a occupato*/

                    documentSet.get(src).addEditor(user, numSec);
                    db.get(user).setInEditing(src);
                    fileSender.close();
                    /*result diventa uno per poi mandare un valore di ritorno al client in caso di operazione conclusa con successo*/
                    result=1;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        return result;
    }

    @Override
    public synchronized void endEdit(String modifiedDoc, int index, String user, String src, String sec) {
        /*tramite la segnatura synchronized mi assicuro che non ci siano accessi concorrenti alla struttura dati
        * appena entrato rimuovo l'utente che mi ha segnalato l'endEdit dal set Editors*/
        documentSet.get(src).removeEditor(user,index);
        db.get(user).setInEditing("noDoc");
       try{
           /*sovrascrivo la sezione modificata*/
        FileChannel fileChannel=FileChannel.open(Paths.get("src\\Documents\\"+ src+"\\"+sec),StandardOpenOption.WRITE,StandardOpenOption.CREATE,StandardOpenOption.TRUNCATE_EXISTING);
        fileChannel.write(ByteBuffer.wrap(modifiedDoc.getBytes()));
        fileChannel.close();
        } catch (IOException e) {
            System.err.println("Unable to overwrite the file!");
        }
    }

    @Override
    public void addNewChatMessage(String user, String docName, String sect, String message) {
        /*trmite il database dei documenti trovo i dcoumento di cui il messaggio è relativo e aggiungo al buffer di quest'ultimo l'utente che lo ha inviato
        * la sezione che sta modificando e il messaggio in questione di modo*/
        documentSet.get(docName).addMessage(user,sect,message);
    }

    /*classe usata per ordinare il listing delle sezioni dei documenti presi tramite il metodo listFiles*/
    private static final class FilenameComparator implements Comparator<String> {
        private final Pattern NUMBERS =
                Pattern.compile("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");
        @Override public final int compare(String o1, String o2) {

            if (o1 == null || o2 == null)
                return o1 == null ? o2 == null ? 0 : -1 : 1;

            // Splittare entrambe le stringhe in base al pattern
            String[] split1 = NUMBERS.split(o1);
            String[] split2 = NUMBERS.split(o2);

            for (int i = 0; i < Math.min(split1.length, split2.length); i++) {
                char c1 = split1[i].charAt(0);
                char c2 = split2[i].charAt(0);
                int cmp = 0;

                if (c1 >= '0' && c1 <= '9' && c2 >= '0' && c2 <= '9')
                    cmp = new BigInteger(split1[i]).compareTo(new BigInteger(split2[i]));

                if (cmp == 0)
                    cmp = split1[i].compareTo(split2[i]);

                if (cmp != 0)
                    return cmp;
            }

            return split1.length - split2.length;
        }
    }

}

