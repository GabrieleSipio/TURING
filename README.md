# TURING
Reti di Calcolatori progetto a.a. 2018/2019: disTribUted collaboRative edItiNG (TURING)

Distributed collaborative editing (TURING) è un progetto che si pone l’obbiettivo di implementare una scrittura collaborativa, ovvero un tipo di scrittura che consiste nella creazione di testi da parte di un gruppo di persone, dove ognuno fornisce un contributo individuale alla produzione del documento finale.


Overview:
Per ottenere questo obbiettivo si è scelto di implementare un sistema client-server che utilizza le API Java NIO in cui la concorrenza è gestita completamente con i Monitor. In particolare, il server ha al suo interno un selettore che gestisce i vari canali (i diversi client) connessi con il server. Viene utilizzata una SocketChannel per leggere da questi canali implementando il protocollo TCP. Come strutture dati per implementare le applicazioni vengono usate le classi:
-	Data
-	Docs
-	ChatterBox
-	Parameters
L’applicazione, altresì, implementa due interfacce:
-	IRegister: definisce i metodi per gestire le operazioni di registrazione e per ottenere accesso alle informazioni di alcune strutture dati necessarie via RMI
-	ITURINGCore : definisce i metodi principali di I/O di TURING
Queste due interfacce sono poi implementate, rispettivamente, nelle classi Register e TURINGCore. L’interfaccia grafica del programma è stata implementata tramite Java Swing, in particolare sono state definite le classi:
-	PrincipalFrame
-	WorkSpace
Strutture Dati:
Data:
Questa classe di supporto serve ad esprimere le caratteristiche che l’entità utente deve avere all’interno di TURING. In particolare, il server gestisce una ConcurrentHashMap<String,Data> recuperata da dei file locali; quest’ultima contiene una associazione univoca tra l’username di un utente e i suoi dati personali (Data). La classe, infatti, contiene i seguenti dati:
-	passwd: una stringa che contiene la password associata all’username 
-	inEditing: una stringa che viene utilizzata per sapere quale documento l’utente sta editando, viene inizializzata con il token “noDoc”, il quale indica che l’utente non è impegnato nell’operazione di editing di alcun documento
-	propDocs: Un set composto da elementi della classe Docs; contiene i documenti di proprietà dell’utente
-	invitedDocs: un set composto da elementi della classe Docs; contiene i documenti a cui l’utente è stato invitato a collaborare
La classe implementa poi i seguenti metodi:
-	String getPasswd(): metodo usato per restituire la pasword dell'utente
-	void addNewDoc(Docs document): metodo usato per aggiungere un nuovo documento di proprietà dell'utente
-	ArrayList<Docs> showDocs(): metodo che restituisce il set dei  documenti di proprietà dell'utente
-	ArrayList<Docs> showInvitedDocs(): metodo che restituisce i documenti su cui l'utente è stato invitato a collaborare
-	ArrayList<String> getDocument(): metodo che restituisce una struttura che mantiene il nome dei documenti risultante dall'unione dei set dei documenti di proprietà dell'utente e dei documenti su cui l'utente è stato invitato a collaborare
-	void addInvitedDoc(Docs document): aggiungo un nuovo documento a cui l'utente è invitato a partecipare
-	String getInEditing(): restituisce il valore della variabile inEditing che sarà o il nome del documento su cui l'utente sta effettuando l'editing, nel caso questa azione stia venendo effettuata, oppure il flag "noDoc” che indica che l'utente non sta lavorando all'editing di nessuna sezione
-	void setInEditing(String inEditing): metodo usato per settare il parametro inEditing sul documento che si sta editando o al flag "noDoc" in caso l'operazione di editing sia cessata
Docs:
Questa classe viene usata per esprimere le caratteristiche che un documento possiede all’interno di TURING, viene usata dal server tramite una ConcurrentHashMap <String,Docs> che è una struttura che mantiene traccia di tutti i documenti presenti in TURING e mantiene un’associazione univoca tra il nome del documento e le sue proprietà. In particolare, queste ultime sono:
-	docName: una stringa contenente il nome del documento
-	address: una stringa contenente l’indirizzo multicast associato al documento
-	chatBuffer: un ArrayList di tipo Chatterbox, ovvero una struttura che mantiene i messaggi scambiati da un gruppo di editors che sta lavorando all’editing del documento 
-	editors: una ConcurrentHashMap<String,Integer> che mantiene un set di tutti gli utenti che stanno lavorando all’editing di una determinata sezione
-	inEdit: un array di tipo booleano che indica se le sezioni sono libere o sono occupate in editing
È bene notare come, alla creazione di un nuovo documento, quando viene chiamato il costruttore della classe, l’indirizzo multicast viene creato in modo randomico chiamando 4 Math.random() distinte per ogni parte dell’indirizzo; questo ci permette di non doverci preoccupare del fatto di poter avere indirizzi multicast uguali perché le probabilità che questo accada sono molto basse. Altresì l’array di booleani inEdit viene inizializzato completamente a false indicando il fatto che, alla creazione del documento, tutte le sezioni sono libere per l’editing. La classe implementa poi i seguenti metodi:
-	String getDocName(): metodo che restituisce il nome del documento
-	InetAddress getInetAddress(): metodo che restituisce l'indirizzo multicast del documento
-	int getEditSection(String name): metodo che restituisce la sezione che un utente sta modificando
-	boolean isBusy(int index): metodo usato per sapere se una data sezione è impegnata in editing o è libera per essere editata
-	void addEditor(String editor,int secNum): metodo usato per aggiungere un editor al set e segnare la sezione come occupata in editing
-	void removeEditor(String editor,int secNum): metodo utilizzato per rimuovere un editor dal set e segnare la sezione come libera quando ha finito di fare l'editing o se c'è stato un arresto anomalo il metodo controlla anche se il set è vuoto o meno in quanto, nel caso sia vuoto, questo indica che nessuno sta più lavorando all'editing e il buffer dei messaggi deve essere svuotato
-	void addMessage(String user,String sect,String message): metodo usato per aggiungere un messaggio al buffer dei messaggi del documento
-	ArrayList<ChatterBox> getChatBuffer(): metodo usato per ottenere la chat dei messaggi scambiati fino alla chiamata di questo metodo, in caso ci siano
ChatterBox:
Questa classe viene usata per esprimere le caratteristiche che un messaggio inviato ad un gruppo di editors in TUIRING deve avere. In particolare, tramite dei getters, permette di sapere oltre al messaggio ricevuto, chi è il mittente e su quale sezione quest’ultimo sta lavorando.
Parameters:
Questa classe viene usata per mandare i parametri di I/O dal client al server, tramite i getters, è possibile ottenere il tipo di operazione che si sta richiedendo con i parametri ad essa associati
Classi per la gestione delle operazioni di I/O:
Register:
Questa classe, usando le API RMI, ha il compito di gestire l’operazione di registrazione al sistema di TURING e di fornire delle strutture dati necessarie per le operazioni. Oltre questo ha il compito di andare a prendere, non appena il costruttore viene chiamato, le informazioni relative agli utenti e ai documenti presenti nel sistema dai due file (Databate.txt per gli utenti e Documents.txt per i documenti) deserializzandole e assegnandole a due ConcurrentHashMap rispettivamente <String,Data> per gli utenti e di <String,Docs> per i documenti, la prima mantiene un’associazione tra l’username dell’utente e i suoi dati personali, mentre la seconda mantiene un’associazione tra il nome dei documenti e l’entità che rappresenta il documento con tutte le sue proprietà. Se questi due file sono vuoti o non sono comunque presenti, il programma procede ad inizializzare come vuote le due ConcurrentHashMap e ,in caso i file non siano presenti, li crea e procede all’esecuzione.  La classe implementa, altresì, i seguenti metodi:
-	ConcurrentHashMap<String, Data> getDB(): metodo usato per restituire la struttura che contiene informazioni relative agli utenti
-	ConcurrentHashMap<String, Docs> getDocs(): metodo usato per restituire la struttura che contiene informazioni relative ai documenti presenti nel sistema
-	boolean register(String name, String password) throws RemoteException: metodo ereditato dall’interfaccia IRegister che gestisce l’operazione di registrazione al servizio di TURING. In particolare, il metodo controlla che il nome scelto dall’utente non sia già presente nel database degli utenti; se questo controllo va a buon fine allora procede ad inserire il nuovo utente nel set degli utenti presenti nel sistema e aggiorna il file contenente le informazioni relative agli utenti per mantenere il sistema consistente in caso di arresti anomali.
-	ArrayList<Docs> getMyDocs(String name): metodo usato per ottenere il set dei documenti di proprietà di un utente di nome "name"
-	ArrayList<Docs> getMyInvitation(String name) throws RemoteException: metodo ereditato dall’interfaccia IRegister, ha il compito di restituire, dato il nome di un utente, i documenti a cui quest’ultimo è stato invitato a collaborare
-	ArrayList<String> getAllDocs(String name) throws  RemoteException: metodo ereditato dall’interfaccia IRegister, ha il compito di restituire, dato il nome di un utente, i documenti di proprietà di quest’ultimo 
-	ArrayList<ChatterBox> getHistory(String docName) throws RemoteException: metodo ereditato dall’interfaccia IRegister, ha il compito di restituire, dato il nome di un documento, il buffer dei messaggi associato al gruppo di editing che sta lavorando su quest’ultimo.
-	InetAddress getDocAddress(String docName) throws RemoteException, UnknownHostException: metodo ereditato dall’interfaccia IRegister, ha il compito di restituire dato il nome di un documento, l’indirizzo multicast associato a quest’ultimo
-	String isEdited(String docName,int sez) throws RemoteException: metodo ereditato dall’interfaccia IRegister, ha il compito individuare, dato il nome di un documento e il numero di una sezione, se quest’ultima risulta in editing o meno, in caso positivo restituirà un token che verrà mostrato in fase di visualizzazione del documento, altrimenti restituirà una stringa vuota
-	boolean isEditing(String name) throws RemoteException: metodo ereditato dall’interfaccia IRegister, ha il compito, dato il nome di un utente, di comunicare se quest’ultimo è coinvolto in un operazione di editing o meno 
TURINGCore:
Questa classe gestisce le operazioni principali di I/O in TURING, mantiene, al suo interno, un set degli utenti online in TURING con un ConcurrentHashMap<String,SocketAddress> che mantiene l’associazione tra l’user è il suo indirizzo IP ogni volta che si autentica in TURING. Interagisce con il database degli utenti e dei documenti presenti nel sistema per effettuare le operazioni di I/O. La classe implementa i seguenti metodi:
o	int login(String name, String password, SocketAddress IP): metodo utilizzato per effettuare l’autenticazione dell’utente di nome “name” e con password “password”. Se l’utente passa i controlli lo aggiungo all’online set con il suo IP
o	void logout(String name) throws RemoteException: metodo per effettuare il logout dell'utente con nome "name" aggiornando il database con tutte le modifiche che l'utente ha fatto durante la sessione (portandolo in uno stato consistente) e rimuovendo l’utente dall’online set
o	int create(String owner, String dirName, int numSezioni): metodo per creare un nuovo documento con nome "dirName" con "numSezioni" sezioni e aggiungerlo al document set di "owner"
o	int invite(String name, String Doc): metodo per invitare un utente di nome "name" a collaborare all'editing del documento "doc"
o	void showSec(String src, String sec): metodo per inviare una sezione "sez" appartenente al documento "src" attraverso la SocketChannel
o	void showDoc(String docName): metodo per inviare un intero documento doc attraverso la SocketChannel
o	int edit(String src, String sec, int index, String user):  metodo per inviare una sezione "sez" appartenente al documento "src" all'utente user
o	void endEdit(String modifiedDoc, int index, String user,String src,String sec): metodo per dichiarare la fine dell'editing di un documento
o	boolean isIn(String name): metodo che mi indica se un utente è effettivamente online su TURING
o	ConcurrentHashMap<String, Docs> getDocumentSet(): metodo usato per restituire l’intero set dei documenti presenti in TURING
o	SocketAddress getAddress(String name): metodo usato per individuare l’indirizzo IP di un utente connesso a TURING
o	void cleanOnlineSet (SocketAddress socket): metodo per eliminare, eventualmente, un utente il quale client si è chiuso per motivi anomali evitando di portare modifiche parziali al database
o	void addNewChatMessage(String user,String docName,String sect,String message): metodo per aggiungere un messaggio, inviato da un utente "user", al buffer dei messaggi del documento "docName"
Classi per l’interfaccia grafica:
L’interfaccia grafica di TURING è implementata usando Java Swing e consiste di due classi principali.
PrincipalFrame:
Definisce il frame di autenticazione al programma di TURING. Permette di eseguire le operazioni di Login e di Registrazione al servizio di TURING
WorkSpace:
Definisce l’hub di lavoro per TURING. Al suo interno è possibile eseguire tutte le operazioni di I/O quali creazione di un documento, editing di una sezione di un documento, invitare un utente a collaborare all’editing di un documento, visione di un documento o di una sezione specifica e logout dal servizio di TURING.
