import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;


/*classe che implementa il server di TURING gestito tramite Java NIO
* @author Gabriele Sipione mat:534248*/

public class TURINGServer {

    public static void main(String[] args) {
        try {
            IRegister reg=new Register();
            LocateRegistry.createRegistry(1919);
            Registry registry=LocateRegistry.getRegistry(1919);
            registry.rebind("Database",reg);
            System.out.println("TURING Server online!");
            /*inizializzo il TURINGCore che contiene le operazioni principali di I/O del programma*/
            ITURINGCore auth= new TURINGCore(((Register) reg).getDB(),((Register) reg).getDocs());
            Selector selector = Selector.open();
            ServerSocketChannel Socket = ServerSocketChannel.open();
            Socket.bind(new InetSocketAddress("localhost", 2020));
            System.out.println("[TURING server] " + InetAddress.getLocalHost().getHostAddress() + " in ascolto sulla porta 2020");
            Socket.configureBlocking(false);
            Socket.register(selector,SelectionKey.OP_ACCEPT);

            while (true) {

                selector.select();

                Set<SelectionKey> keys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = keys.iterator();

                while (iterator.hasNext()) {
                    SelectionKey myKey = iterator.next();


                    if (myKey.isAcceptable()) {
                        SocketChannel clienSC = Socket.accept();

                        clienSC.configureBlocking(false);

                        clienSC.register(selector, SelectionKey.OP_READ);

                    } else if (myKey.isReadable()) {
                        SocketChannel clienSC = (SocketChannel) myKey.channel();
                        ByteBuffer buff = ByteBuffer.allocate(1024);
                        Parameters inParam=null;
                        try {
                            clienSC.read(buff);
                            byte[] readArr = buff.array();
                            ByteArrayInputStream bis = new ByteArrayInputStream(readArr);
                            ObjectInputStream ois = new ObjectInputStream(bis);
                            inParam = (Parameters) ois.readObject();
                        }catch(IOException e){
                            /*gestisco il fatto che quando la connessione del client viene interrotta in maniera anomala
                            * chiamo il metodo cleanOnlineSet per portare il sistema in uno stato consistente,
                            * chiudo la connessione verso quel dato client e non faccio più nulla*/
                            System.err.println("[TURING server] client " + clienSC.getRemoteAddress() + " has crashed!");
                            auth.cleanOnlineSet(clienSC.getRemoteAddress());
                            clienSC.close();
                        }
                        if (clienSC.isOpen()) {
                            int result;
                            ArrayList<Object> flags = new ArrayList<>();
                            switch (inParam.getOperation()) {
                                case "Login":
                                    System.out.println("[TURING server] Operazione richiesta " + inParam.getOperation());
                                    /*chiamo il metodo login dalla classe TURINGCore che mi restituisce l'esito dell'operazione*/
                                    result = auth.login(inParam.getFields().get(0), inParam.getFields().get(1), clienSC.getRemoteAddress());
                                    if (result == 0) {
                                        /*in caso di esito positivo stampo a video l'indirizzo remoto del client che si è appena connesso*/
                                        System.out.println("[TRUING server] Connection Accepted: " + clienSC.getRemoteAddress() + "\n");
                                    }
                                    /*preparo il pachetto da mandare al client per rendergli noto l'esito del'operazione*/
                                    flags.add(inParam.getOperation()); //0--->Login
                                    flags.add(result);                 //1--->esito dell'operazione
                                    clienSC.register(selector, SelectionKey.OP_WRITE, flags);
                                    break;
                                case "Create":
                                    System.out.println("[TURING server] Operazione richiesta " + inParam.getOperation());
                                    String owner = inParam.getFields().get(0);
                                    String docName = inParam.getFields().get(1);
                                    int SecNo = Integer.parseInt(inParam.getFields().get(2));
                                    /*chiamo il metodo create dalla classe TURINGCore che mi restituisce l'esito dell'operazione*/
                                    result = auth.create(owner, docName, SecNo);
                                    /*preparo il pachetto da mandare al client per rendergli noto l'esito del'operazione*/
                                    flags.add(inParam.getOperation()); //0--->Create
                                    flags.add(result);                 //1---> esito dell'operazione
                                    clienSC.register(selector, SelectionKey.OP_WRITE, flags);
                                    break;
                                case "Invite":
                                    System.out.println("[TURING server] Operazione richiesta " + inParam.getOperation());
                                    /*chiamo il metodo invite dalla classe TURINGCore che mi restituisce l'esito dell'operazione*/
                                    result = auth.invite(inParam.getFields().get(0), inParam.getFields().get(1));
                                    /*preparo il pachetto da mandare al client per rendergli noto l'esito del'operazione e per notificare l'invito al diretto interessato*/
                                    flags.add(inParam.getOperation());     //0---> Invite
                                    flags.add(result);                     //1---> esito dell'operazione
                                    flags.add(inParam.getFields().get(0)); //2---> username dell'utente
                                    flags.add(inParam.getFields().get(1)); //3---> documento su cui si vuole invitare a collaborare
                                    clienSC.register(selector, SelectionKey.OP_WRITE, flags);
                                    break;
                                case "Show Section" :
                                    System.out.println("[TURING server] Operazione richiesta " + inParam.getOperation());
                                    String src=inParam.getFields().get(0) ; // nome documento
                                    String sec=inParam.getFields().get(1);  // nome sezione
                                    /*chiamo il metodo showSec di TURINGCore che provvede a mandare la sezione richiesta attraverso una socket channel apposita*/
                                    auth.showSec(src,sec);
                                    break;
                                case "Show Document":
                                    System.out.println("[TURING server] Operazione richiesta " + inParam.getOperation());
                                    docName=inParam.getFields().get(0); // nome documento
                                    /*chiamo il metodo showDoc di TURINGCore che provvede a mandare il documento richiesto attraverso una socket channel apposita*/
                                    auth.showDoc(docName);
                                    break;
                                case "Edit":
                                    System.out.println("[TURING server] Operazione richiesta " + inParam.getOperation());
                                    String edSrc= inParam.getFields().get(0); //nome documento
                                    String edSec= inParam.getFields().get(1); //nome sezione
                                    int numSec= Integer.parseInt(inParam.getFields().get(2));
                                    String user= inParam.getFields().get(3);
                                    System.out.println("[TURING Server] invio risposta..");
                                    /*chiamo il metodo edit di TURINGCore che provvede a mandare la sezione richiesta attraverso una socket channel apposita*/
                                    result=auth.edit(edSrc,edSec,numSec,user);
                                    flags.add(inParam.getOperation());
                                    flags.add(result);
                                    clienSC.register(selector, SelectionKey.OP_WRITE, flags);
                                    break;
                                case "End Edit":
                                    System.out.println("[TURING server] Operazione richiesta " + inParam.getOperation());
                                    int newSecLength= Integer.parseInt(inParam.getFields().get(0));
                                    src= inParam.getFields().get(1);
                                    sec= inParam.getFields().get(2);
                                    numSec=Integer.parseInt(inParam.getFields().get(3));
                                    user= inParam.getFields().get(4);
                                    ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
                                    serverSocketChannel.socket().bind(new InetSocketAddress("localhost", 2050));
                                    SocketChannel fileReciever = serverSocketChannel.accept();
                                    ByteBuffer newS=ByteBuffer.allocate(newSecLength);
                                    newS.clear();
                                    fileReciever.read(newS);
                                    newS.flip();
                                    String newSec= new String(newS.array());
                                    /*chiamo il metodo endEdit di TURINGCore che provvede a riportare i cambiamenti fatti sulla sezione e rilasciale la lock sul documento*/
                                    auth.endEdit(newSec,numSec,user,src,sec);
                                    newS.clear();
                                    fileReciever.close();
                                    serverSocketChannel.close();
                                    break;
                                case "Send Message":
                                    user= inParam.getFields().get(0);
                                    docName= inParam.getFields().get(1);
                                    sec= inParam.getFields().get(2);
                                    String message= inParam.getFields().get(3);
                                    String packet=user+" ("+sec+") : "+message;
                                    auth.addNewChatMessage(user,docName,sec,message);
                                    /*mando il messaggio al gruppo multicast associato al documento*/
                                    DatagramSocket datagramSocket=new DatagramSocket();
                                    InetAddress inet=auth.getDocumentSet().get(docName).getInetAddress();
                                    DatagramPacket datagramPacket=new DatagramPacket(packet.getBytes(),packet.getBytes().length,inet,5252);
                                    datagramSocket.send(datagramPacket);
                                    break;
                                case "Edit Frame Closed":
                                    /*Rilascio la lock sulla sezione senza salvare nulla delle modifiche fatte fino a quel momento*/
                                    src=inParam.getFields().get(0);                            //0--->documento su cui sta avvenendo la modifica
                                    int index=Integer.parseInt(inParam.getFields().get(1));           //1---> indice della sezione su cui sta avvenendo l'editing
                                    String usr=inParam.getFields().get(2);                            //2--->utente che sta modificando la sezione
                                    ((Register) reg).getDocs().get(src).removeEditor(usr,index);
                                    ((Register) reg).getDB().get(usr).setInEditing("noDoc");
                                    break;
                                case "Logout":
                                    System.out.println("[TURING server] Operazione richiesta " + inParam.getOperation());
                                    /*chiamo il metodo logout dalla classe TURINGCore*/
                                    auth.logout(inParam.getFields().get(0)); //0---> Logout
                                    break;
                                case "Exit":
                                    /*chiudo la connessione verso il client mantenendo il server ancora attivo in attesa di altre connessioni*/
                                    clienSC.close();
                                    System.out.println("\n[TURING server] It's time to close connection as we got 'Exit' command ");
                                    System.out.println("\n[TURING server] Server will keep running. Try running clienSC again to establish new connection");
                                    break;
                            }
                        }
                    }
                    else if (myKey.isWritable()){
                        System.out.println("Sending requested file...");
                        SocketChannel clientSC= (SocketChannel) myKey.channel();
                        ArrayList<Object> attachment=(ArrayList<Object>) myKey.attachment();
                        ByteBuffer buff=ByteBuffer.allocate(1024);
                        Integer res;
                        /*mi ricavo l'operazione da eseguire*/
                        String op=String.valueOf(attachment.get(0));
                        switch (op){
                            case "Login" :
                            case "Create" :
                            case "Edit"  :
                                /*invio al client l'esito delle operazione*/
                                res = (Integer) attachment.get(1);
                                System.out.println("[TURING Server] invio risposta..");
                                buff.clear();
                                buff.put(res.byteValue());
                                buff.flip();
                                clientSC.write(buff);
                                buff.clear();
                                attachment.clear();
                                break;
                            case "Invite" :
                                /*invio al client il risultato dell'operazione*/
                                res = (Integer) attachment.get(1);
                                System.out.println(res);
                                System.out.println("[TURING Server] invio risposta..");
                                buff.clear();
                                buff.put(res.byteValue());
                                String destName=String.valueOf(attachment.get(2));
                                /*controllo che l'utente che sto invitando è di fatti all'interno del set degli utenti online in TURING*/
                                if (auth.isIn(destName) && res==0){
                                    /*in caso sia presente apro una connessione UDP per far visualizzare a video l'invito ricevuto*/
                                    DatagramSocket datagramSocket=new DatagramSocket();
                                    String newInvDoc= String.valueOf(attachment.get(3));
                                    DatagramPacket dp=new DatagramPacket(newInvDoc.getBytes(),newInvDoc.length(),auth.getAddress(destName));
                                    datagramSocket.send(dp);
                                }
                                buff.flip();
                                clientSC.write(buff);
                                buff.clear();
                                attachment.clear();
                                break;
                        }
                        clientSC.register(selector,SelectionKey.OP_READ);
                    }

                    iterator.remove();
                }
            }
        }   catch (RemoteException e){
            System.err.println(e.getMessage());
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
    }
}
