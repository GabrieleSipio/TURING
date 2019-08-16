import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/*classe che esprime l'entita di un documento con tutte le proprità che quest'ultimo possiede in TURING
* @author Gabriele Sipione mat:534248 */

public class Docs implements Serializable {
    private static final long serialVersionUID = 1L;
    /*nome del documento e indirizzo multicast associato ad esso*/
    private String docName,address;
    /*buffer dei messaggi scambiati durante l'edit delle sezioni*/
    private ArrayList<ChatterBox> chatBuffer;
    /*Struttura che mantiene tutti gli utenti che stanno operando un editing di una certa sezione*/
    private ConcurrentHashMap<String,Integer> editors;
    /*struttura che mantiene lo stato di editing(true) o free(false) per i documenti*/
    private Boolean[] inEdit;

    public Docs (String docName,int nSec){
        this.docName=docName;
        int tmp = (int)(Math.random()*40);
        while(tmp < 24 || tmp > 40) {
            tmp = (int)(Math.random()*40);
        }
        tmp += 200;
        /*l'indirizzo multicat viene generato randomicamento per ogni documento quango questo viene creato*/
        this.address= tmp + "." + (int)(Math.random()*256) + "." + (int)(Math.random()*256) + "." + (int)(Math.random()*256);
        editors=new ConcurrentHashMap<>();
        inEdit=new Boolean[nSec+1];
        /*la struttura che mantiene lo stato dei documenti viene inizializzata completamente a free*/
        for (int i=0;i<inEdit.length;i++){
            inEdit[i]=false;
        }
        chatBuffer=new ArrayList<>();
    }

    /*metodo che restituisce il nome del documento*/
    public String getDocName(){
        return docName;
    }

    /*metodo che restituisce l'inidrizzo multicast del documento*/
    public InetAddress getInetAddress() throws UnknownHostException {
        return InetAddress.getByName(address);
    }

    /*metodo che restituisce la sezione che un utente sta modificando*/
    public int getEditSection(String name){
        return editors.get(name);
    }

    /*metodo usato per sapere se una data sezione è impegata in editing o è libera per essere editata*/
    public boolean isBusy(int index){
        return inEdit[index];
    }

    /* metodo usato per aggiungere un editor al set e segnare la sezione come occupata in editing*/
    public  synchronized void addEditor(String editor,int secNum){
        editors.put(editor,secNum);
        inEdit[secNum]=true;
    }

    /*metodo utilizzato per rimuovere un editor dal set e segnare la sezione come libera quando ha finito di fare l'editing o se c'è stato un arresto anomalo
    * il metodo controlla anche se il set è vuoto o meno in quanto, nel caso sia vuoto, questo indica che nessuno sta più lavorando all'editing
    * e il buffer dei messaggi deve essere svuotato */
    public synchronized void removeEditor(String editor,int secNum){
        editors.remove(editor);
        inEdit[secNum]=false;
        if (editors.isEmpty()){
            chatBuffer.clear();
        }
    }

    /*metodo usato per aggiungere un messaggio al buffer dei messaggi del documento*/
    public  synchronized void addMessage(String user,String sect,String message){
        chatBuffer.add(new ChatterBox(user,sect,message));
    }

    /*metodo usato per ottenere la chat dei messaggi scambiati fino alla chiamata di questo metodo, in caso ci siano*/
    public ArrayList<ChatterBox> getChatBuffer() {
        return chatBuffer;
    }
}
