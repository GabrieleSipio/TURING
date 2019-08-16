import java.io.Serializable;

/*classe che esprime l'entita del messaggio che viene spedito all'interno dei gruppi di editing di TURING
* @author Gabriele Sipione mat:534248*/

public class ChatterBox implements Serializable {
    private static final long serialVersionUID = 1L;
    private String user,section,message;

    public ChatterBox(String user, String section, String message) {
        this.user = user;
        this.section = section;
        this.message = message;
    }

    /*metodo usato per restituire l'utende che ha inviato il messaggio*/
    public String getUser() {
        return user;
    }

    /*metodo usato per restituire la sezione a cui il messaggio fa riferimento*/
    public String getSection() {
        return section;
    }

    /*metodo usato per restituire il messaggio effettivo che è stato mandato al gruppo*/
    public String getMessage() {
        return message;
    }
}
