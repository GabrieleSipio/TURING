import java.io.Serializable;
import java.util.ArrayList;

/*classe usata per mandare i parametri delle operazioni di I/O in TURING
* @author Gabriele Sipione mat:534248*/

public class Parameters implements Serializable {

    private final static long serialVersionUID=1L;
    /*struttura che contine i campi associati all'operazione che si sta eseguendo*/
    private ArrayList<String> fields;
    /*indicatore dell'operazione che si sta eseguendo*/
    private String operation;

    public Parameters(String operation, ArrayList<String> fields){
        this.fields=fields;
        this.operation=operation;
    }

    /*metodo che restituisce l'operazione che sta vendendo eseguita*/
    public String getOperation(){
        return operation;
    }

    /*metodo che restituisce i campi associati all'operazione in esecuzione*/
    public ArrayList<String> getFields(){
        return fields;
    }
}
