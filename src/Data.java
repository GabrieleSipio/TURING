import java.io.Serializable;
import java.util.ArrayList;
@SuppressWarnings("Duplicates")

/*classe esprime le caratteristiche dell'entità utente
* @author Gabriele Sipione mat:534248*/

public class Data implements Serializable {
    private static final long serialVersionUID = 1L;
    /*password associata all'account dell'utente e nome del documento che sta editando*/
    private String passwd,inEditing="noDoc";
    /*struttura che mantiene i documenti creati dall'utente con tutte le loro proprietà*/
    private ArrayList<Docs> propDocs;
    /*struttura che mantiene i documenti a cui l'utente è stato invitato a collaborare per l'editing*/
    private ArrayList<Docs>invitedDocs;

    public Data(String passwd){
        this.passwd=passwd;
        this.propDocs=new ArrayList<>();
        this.invitedDocs=new ArrayList<>();
    }

    /*metodo usato per restituire la pasword dell'utente*/
    public String getPasswd(){
        return passwd;
    }

    /* metodo usato per aggiungere un nuovo documento di proprietà dell'utente*/
    public void addNewDoc(Docs document){
        propDocs.add(document);
    }

    /*metodo che restituisce i documenti di proprietà dell'utente*/
    public ArrayList<Docs> showDocs(){
        return propDocs;
    }

    /*metodo che restituisce i docuemnti su cui l'utente è stato invitato a collaborare*/
    public ArrayList<Docs> showInvitedDocs(){
        return invitedDocs;
    }

    /*metodo che restituisce una struttura che mantiene il nome dei documenti risultante dall'unione dei set dei documenti di proprietà dell'utente e dei documenti
    * su cui l'utente è stato invitato a collaborare*/
    public ArrayList<String> getDocument(){
        ArrayList<String> myStringDoc=new ArrayList<>();
        for (Docs doc : showInvitedDocs()){
            myStringDoc.add(doc.getDocName());
        }
        for (Docs doc : showDocs()){
            myStringDoc.add(doc.getDocName());
        }
        return myStringDoc;
    }

    /*aggiungo un nuovo documento a cui l'utente è invitato a partecipare*/
    public void addInvitedDoc(Docs document){
        invitedDocs.add(document);
    }

    /*restituisce il valore della variabile inEditing che sarà o il nome del documento su cui l'utente sta effettuando l'editing, nel caso questa azione stia venendo effettuata, oppure
    * il flag "noDoc" che indica che l'utente non sta lavorando all'editing di nessuna sezione"*/
    public String getInEditing() {
        return inEditing;
    }

    /*metodo usato per settare il parametro inEditing sul documento che si sta editando o al flag "noDoc" in caso l'operazione di editing sia cessata*/
    public void setInEditing(String inEditing) {
        this.inEditing = inEditing;
    }
}

