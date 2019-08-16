import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.*;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.regex.Pattern;

/*classe composita che gestisce i frame grafici di l'hub centrale di TRUING e i frame per eseguire le operazioni di I/O
* @author Gabriele Sipione mat:534248*/

@SuppressWarnings("Duplicates")
public class WorkSpace extends JFrame{
    private String user;
    private  Font f=new Font("Dialog",Font.PLAIN,17);
    private JFrame frame;
    private JPanel hub,chat;
    private JLabel username;
    private JButton create,showSection, showDocument, editDoc, invite, logOut;
    private JTextArea mailBox,log;
    private Dimension size;
    private IRegister stub;
    private Icon crt,edit,shw,inv,lgot;
    private SocketChannel socketChannel;
    private Thread tr;
    private InBoxHandler iBH;
    /*set usato per mantenere traccia dei frame aperti durante la sessione di lavoro in TURING*/
    private ArrayList<JFrame> internalFrames;


    public WorkSpace(String user, IRegister stub, SocketChannel socketChannel){
        this.stub=stub;
        this.socketChannel=socketChannel;
        this.user=user;
        internalFrames=new ArrayList<>();
        frame=new JFrame("TURING Hub");
        frame.setSize(900,569);
        Toolkit toolkit=Toolkit.getDefaultToolkit();
        size=toolkit.getScreenSize();
        int x= (size.width/2)-(frame.getWidth()/2);
        int y= (size.height/2)-(frame.getHeight()/2);
        frame.setLocation(x,y);
        frame.setLocationRelativeTo(null);
        ListenerForWindow lfw=new ListenerForWindow();
        frame.addWindowListener(lfw);

        ImageIcon background = new ImageIcon("src\\Graphic\\workgui.jpg");
        Image img = background.getImage();
        background = new ImageIcon(img.getScaledInstance(900,569, Image.SCALE_SMOOTH));
        JLabel b = new JLabel("", background, JLabel.CENTER);
        b.setBounds(0,0,900,569);

        hub=(JPanel)frame.getContentPane();
        hub.setLayout(null);



        crt=new ImageIcon("src\\Graphic\\new.png");
        create=new JButton("Create Document",crt);
        create.setFont(f);
        create.setBounds(13,50,create.getPreferredSize().width,create.getPreferredSize().height);
        create.setToolTipText("Create a new Document");

        shw=new ImageIcon("src\\Graphic\\show.png");
        showDocument=new JButton("Show Document",shw);
        showDocument.setFont(f);
        showDocument.setBounds(13,100,create.getPreferredSize().width,create.getPreferredSize().height);
        showDocument.setToolTipText("Get either a section of a document or the entire file");

        edit=new ImageIcon("src\\Graphic\\edit.png");
        editDoc=new JButton("Edit Document",edit);
        editDoc.setFont(f);
        editDoc.setBounds(13,150,create.getPreferredSize().width,create.getPreferredSize().height);
        editDoc.setToolTipText("Edit a section of a document");

        inv=new ImageIcon("src\\Graphic\\invite.png");
        invite=new JButton("Invite",inv);
        invite.setFont(f);
        invite.setBounds(13,400,create.getPreferredSize().width,create.getPreferredSize().height);
        invite.setToolTipText("Invite another user of TURING to collaborate with the editing of your documents");

        lgot=new ImageIcon("src\\Graphic\\logout.png");
        logOut=new JButton("Logout",lgot);
        logOut.setFont(f);
        logOut.setBounds(13,450,create.getPreferredSize().width,create.getPreferredSize().height);
        logOut.setToolTipText("Logout from TURING");

        mailBox=new JTextArea();
//        mailBox.setBounds(43+create.getWidth(),55, 415,logOut.getY()+create.getHeight()-40);
        mailBox.setEditable(false);
        mailBox.setWrapStyleWord(true);
        mailBox.setLineWrap(true);
        mailBox.setFont(f);
        mailBox.setText("You are invited to collaborate for the editing of the following documents:\n");
        DefaultCaret caret = (DefaultCaret)mailBox.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        JScrollPane scrollableMailBox = new JScrollPane(mailBox);
        scrollableMailBox.setBounds(43+create.getWidth(),55, 415,logOut.getY()+create.getHeight()-40);
        scrollableMailBox.getViewport().setOpaque(false);
        scrollableMailBox.setOpaque(false);
        /*inizializzo la mailbox andando a recuperare "offline" i documenti a cui l'utente è stato invitato tramite il metodo
        * getMyInvitation()*/
        try {
            ArrayList<Docs> invDocs=stub.getMyInvitation(user);
            for (int i=0; i<invDocs.size();i++){
                mailBox.append(invDocs.get(i).getDocName() + "\n");
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        /*Il thread tr rimane sempre in esecuzione, fino a che l'utente è connesso, in attesa di ricevere nuovi documenti su cui
        * collaborare*/
        try {
            iBH=new InBoxHandler(mailBox,socketChannel.getLocalAddress());
            tr = new Thread(iBH);
            tr.start();
        } catch (IOException e) {
            e.printStackTrace();
        }


        log=new JTextArea();
//        log.setBounds(653,55, (55+create.getWidth()),logOut.getY()+create.getHeight()-40);
        log.setEditable(false);
        log.setFont(f);
        DefaultCaret logCaret = (DefaultCaret)log.getCaret();
        logCaret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        JScrollPane scrollableLog = new JScrollPane(log);
        scrollableLog.setBounds(653,55, (55+create.getWidth()),logOut.getY()+create.getHeight()-40);
        scrollableLog.getViewport().setOpaque(false);
        scrollableLog.setOpaque(false);

        log.setText("Hi, " + user + "\n" + "Your Documents are: \n");
        try {
            for (Docs d : stub.getMyDocs(user)){
             log.append(d.getDocName() + "\n");
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        ListenerForButton lfb=new ListenerForButton();
        logOut.addActionListener(lfb);
        showDocument.addActionListener(lfb);
        editDoc.addActionListener(lfb);
        invite.addActionListener(lfb);
        create.addActionListener(lfb);


        hub.add(create);
        hub.add(showDocument);
        hub.add(editDoc);
        hub.add(invite);
        hub.add(logOut);
        hub.add(scrollableMailBox);
        hub.add(scrollableLog);

        frame.setVisible(true);
        frame.setResizable(false);
        hub.setOpaque(false);
        frame.add(b);
        }

        private class ListenerForButton implements ActionListener{
            @Override
            public void actionPerformed(ActionEvent e) {
              if (e.getSource()==logOut){
                  /*listener che cattura la pressione del tasto di logout*/
                  try {
                      if (stub.isEditing(user)){
                          /*nel caso l'utente che ha deciso di fare il logout abbia aperto il frame di editing gli viene richiesto prima di terminare l'operazione di modo da evitare
                          * inconsistenze nei documenti*/
                          JOptionPane.showMessageDialog(WorkSpace.this, "You must cease the editing first!", "Error", JOptionPane.ERROR_MESSAGE);
                      }
                      else{
                          ArrayList<String> param = new ArrayList<>();
                          param.add(user);
                          Parameters request = new Parameters(logOut.getText(), param);
                          PrincipalFrame.sendMessage(socketChannel, request);
                          /*mando i parametri per effettuare il logout al server tramite la socket channel*/
                          frame.setVisible(false);
                          /*killo il thread che fino a questo momento era in ascolto*/
                          iBH.shutDown();
                          tr.interrupt();
                          frame.dispose();
                          /*chiudo tutti i frame che sono aperti*/
                          disposeAll();
                      new PrincipalFrame(stub, socketChannel);
                      }
                  } catch (RemoteException ex) {
                      ex.printStackTrace();
                  }
              }
                else if (e.getSource()==editDoc){
                    /*listener che cattura la pressione del tasto di edit aprendo un nuovo frame per poter procedere con l'operazione*/
                    new EditFrame();
                }
                else if (e.getSource()==invite){
                  /*listener che cattura la pressione del tasto invite aprendo un nuovo frame a cui viene fornita la lista dei documenti di proprietà dell'utente*/
                    try {
                      new InvitationFrame(stub.getMyDocs(user));
                  } catch (RemoteException ex) {
                      ex.printStackTrace();
                  }
              }
                else if (e.getSource()==create){
                  /*listener che cattura la pressione del tasto di create aprendo un nuovo frame per poter procedere procedere con l'operazione*/
                  new CreationFrame();

                }
                else if (e.getSource()==showDocument){
                  /*listener che cattura la pressione del tasto di show aprendo un nuovo frame per poter procedere procedere con l'operazione*/
                        new ShowDoc();
                }
            }
        }

        private class ListenerForWindow implements WindowListener{

            @Override
            public void windowOpened(WindowEvent e) {

            }

            @Override
            public void windowClosing(WindowEvent e) {
                /*listener che cattura la pressione dell'exit button che, in caso di conferma da parte dell'utente, esegue la procedura di logout e richiama il frame di autenticazione, altrimenti non fa nulla*/
                int confirm=JOptionPane.showConfirmDialog(WorkSpace.this,"Closing the window will make you log out from TURING.\nAre you sure?","Warning!",JOptionPane.YES_NO_OPTION);
                if (confirm==JOptionPane.YES_OPTION){
                    ArrayList<String> fields=new ArrayList<>();
                    fields.add(user);
                    Parameters outParam=new Parameters(logOut.getText(),fields);
                    PrincipalFrame.sendMessage(socketChannel,outParam);
                    frame.setVisible(false);
                    iBH.shutDown();
                    tr.interrupt();
                    frame.dispose();
                    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    new PrincipalFrame(stub,socketChannel);
                }
                else{
                    frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
                }

            }

            @Override
            public void windowClosed(WindowEvent e) {

            }

            @Override
            public void windowIconified(WindowEvent e) {

            }

            @Override
            public void windowDeiconified(WindowEvent e) {

            }

            @Override
            public void windowActivated(WindowEvent e) {

            }

            @Override
            public void windowDeactivated(WindowEvent e) {

            }
        }

        /*frame per gestire l'operazione di creazione del documento*/
        private class CreationFrame extends JFrame {
        private String[] times={"1","2","3","4","5","6","7","8","9","10","11","12","13","14","15","16","17","18","19","20","21","22","23","24","25","26","27","28","29","30"};
        private JFrame cFrame;
        private Dimension size;
        private JLabel docName,secNo;
        private JPanel cPanel;
        private JTextField textField;
        private JComboBox num;
        private JButton createDoc;


        @SuppressWarnings("Duplicates")
        public CreationFrame(){
            cFrame=new JFrame("Create a New Document");
            /*aggiungo il frame al set dei frame presenti nella sessione di lavoro dell'utente*/
            internalFrames.add(cFrame);
            cFrame.setSize(400,250);
            Toolkit toolkit=Toolkit.getDefaultToolkit();
            size=toolkit.getScreenSize();
            int x= (size.width/2)-(cFrame.getWidth()/2);
            int y= (size.height/2)-(cFrame.getHeight()/2);
            cFrame.setLocation(x,y);

            cPanel=(JPanel)cFrame.getContentPane();
            cPanel.setLayout(null);

            ImageIcon background = new ImageIcon("src\\Graphic\\utilback.jpg");
            Image img = background.getImage();
            background = new ImageIcon(img.getScaledInstance(400,250, Image.SCALE_SMOOTH));
            JLabel b = new JLabel("", background, JLabel.CENTER);
            b.setBounds(0,0,400,250);

            docName=new JLabel("Doc Name: ");
            docName.setFont(f);
            docName.setForeground(Color.WHITE);
            docName.setBounds(35,30,docName.getPreferredSize().width,docName.getPreferredSize().height);
            docName.setToolTipText("Enter the name for the new Document");

            textField=new JTextField();
            textField.setBounds(35+docName.getWidth(),30,234,docName.getPreferredSize().height+4);



            secNo=new JLabel("Sections Number: ");
            secNo.setFont(f);
            secNo.setForeground(Color.WHITE);
            secNo.setBounds(15,(200-textField.getSize().height)/2,secNo.getPreferredSize().width,secNo.getPreferredSize().height);

            num=new JComboBox<>(times);
            num.setBounds(15+secNo.getWidth(),(200-textField.getSize().height)/2,234,textField.getSize().height);
            num.setToolTipText("Select the sectons number (at least 1)");
            num.setSelectedIndex(-1);

            createDoc=new JButton("Create");
            createDoc.setFont(f);
            createDoc.setBounds((cFrame.getWidth()-333)/2,(350-num.getSize().height)/2,333,31);

            ListenForButton lfb=new ListenForButton();
            createDoc.addActionListener(lfb);

            ListenForWindow lfw=new ListenForWindow();
            cFrame.addWindowListener(lfw);

            cPanel.add(docName);
            cPanel.add(textField);
            cPanel.add(secNo);
            cPanel.add(num);
            cPanel.add(createDoc);
            cFrame.getRootPane().setDefaultButton(createDoc);
            cFrame.setVisible(true);
            cFrame.setResizable(false);
            cPanel.setOpaque(false);
            cFrame.add(b);
        }

        private class ListenForButton implements ActionListener{

            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getSource()==createDoc) {
                    if (num.getSelectedIndex()==-1 || textField.getText().length()==0) {
                        /*nel caso uno dei due campi del frame sia vuoto viene richiesta la completazione*/
                        JOptionPane.showMessageDialog(CreationFrame.this, "Error!\nMake sure that all field had been completed!" , "Error", JOptionPane.ERROR_MESSAGE);
                    }else{
                        try {
                            /*mando i parametri necessari per eseguire l'operazione di create */
                            ArrayList<String> fields = new ArrayList<>();
                            fields.add(user);
                            fields.add(textField.getText());
                            fields.add(String.valueOf(num.getSelectedIndex()));
                            Parameters outParam = new Parameters(createDoc.getText(), fields);
                            PrincipalFrame.sendMessage(socketChannel, outParam);
                            ByteBuffer buff = ByteBuffer.allocate(1024);
                            socketChannel.read(buff);
                            buff.flip();
                            int result = buff.get();
                            /*faccio un controllo sull'esito risultante dell'operazione*/
                            if (result < 0) {
                                /*il nome scelto per il documento è già presente nel sistema e quindi viene richiesto di sceglierne un altro*/
                                JOptionPane.showMessageDialog(CreationFrame.this, "there is already a document named " + textField.getText() + "\nSelect another name", "Error", JOptionPane.ERROR_MESSAGE);
                                textField.requestFocus();
                            } else if (result == 0) {
                                /*la creazione del documento non è andata a buon fine*/
                                JOptionPane.showMessageDialog(CreationFrame.this, "An error Occurred during the creation of document" + textField.getText(), "Error", JOptionPane.ERROR_MESSAGE);
                            } else {
                                /*il documento è stato creato con successo quindi provvedo ad aggiungerlo al log dei documenti di proprietà dell'utente chiudo il frame e lo rimuovo dal set dei frame presenti nel sistema*/
                                JOptionPane.showMessageDialog(CreationFrame.this, "Document creation with success!\n section num :" + Integer.parseInt(String.valueOf(num.getSelectedItem())), "", JOptionPane.INFORMATION_MESSAGE);
                                log.append(textField.getText() + "\n");
                                cFrame.setVisible(false);
                                cFrame.dispose();
                                internalFrames.remove(cFrame);
                            }
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                }
            }
        }

        private class ListenForWindow implements WindowListener{
            @Override
            public void windowOpened(WindowEvent e) {

            }
            /*listener usato per catturare l'azione della pressione dell'exit button nel frame che provvede a rimuoverlo dal set dei frame presenti nella sessione*/
            @Override
            public void windowClosing(WindowEvent e) {
                internalFrames.remove(cFrame);
            }

            @Override
            public void windowClosed(WindowEvent e) {

            }

            @Override
            public void windowIconified(WindowEvent e) {

            }

            @Override
            public void windowDeiconified(WindowEvent e) {

            }

            @Override
            public void windowActivated(WindowEvent e) {

            }

            @Override
            public void windowDeactivated(WindowEvent e) {

            }
        }
    }

        /*frame per gestire l'invio di inviti ad altri utenti per collaborare all'editing di un dato documento*/
        private class InvitationFrame extends JFrame {
        private ArrayList<Docs> usrDoc;
        private JFrame frame;
        private Dimension size;
        private JLabel userLabel,docLabel;
        private JPanel panel;
        private JTextField usrName;
        private JComboBox documents;
        private JButton inviteUser;
        @SuppressWarnings("Duplicates")
        public InvitationFrame(ArrayList<Docs> usrDocs){
            this.usrDoc=usrDocs;
            frame=new JFrame("Invite a User");
            /*lo aggiungo al set dei frame presenti nella sessione di lavoro dell'utente*/
            internalFrames.add(frame);
            frame.setSize(400,250);
            Toolkit toolkit=Toolkit.getDefaultToolkit();
            size=toolkit.getScreenSize();
            int x= (size.width/2)-(frame.getWidth()/2);
            int y= (size.height/2)-(frame.getHeight()/2);
            frame.setLocation(x,y);

            ImageIcon background = new ImageIcon("src\\Graphic\\utilback.jpg");
            Image img = background.getImage();
            background = new ImageIcon(img.getScaledInstance(400,250, Image.SCALE_SMOOTH));
            JLabel b = new JLabel("", background, JLabel.CENTER);
            b.setBounds(0,0,400,250);

            panel=(JPanel)frame.getContentPane();
            panel.setLayout(null);

            userLabel=new JLabel("Username: ");
            userLabel.setFont(f);
            userLabel.setForeground(Color.WHITE);
            userLabel.setBounds(35,30,userLabel.getPreferredSize().width,userLabel.getPreferredSize().height);
            userLabel.setToolTipText("Select the user that you want to invite");

            usrName=new JTextField();
            usrName.setBounds(35+userLabel.getWidth(),30,234,userLabel.getPreferredSize().height+4);


            docLabel=new JLabel("Documents: ");
            docLabel.setFont(f);
            docLabel.setForeground(Color.WHITE);
            docLabel.setBounds(35,(200-usrName.getSize().height)/2,docLabel.getPreferredSize().width,docLabel.getPreferredSize().height);
            String[] myDocs=new String[usrDocs.size()];
            int i=0;
            for (Docs d : usrDocs){
                myDocs[i++]=d.getDocName();
            }
            documents=new JComboBox<>(myDocs);
            documents.setBounds(35+docLabel.getWidth(),(200-usrName.getSize().height)/2,234,usrName.getSize().height);
            documents.setToolTipText("Select Documents");
            documents.setSelectedIndex(-1);

            inviteUser=new JButton("Invite");
            inviteUser.setFont(f);
            inviteUser.setBounds((frame.getWidth()-333)/2,(350-documents.getSize().height)/2,333,31);

            ListenerForButton lfb=new ListenerForButton();
            inviteUser.addActionListener(lfb);

            ListenForWindow lfw=new ListenForWindow();
            frame.addWindowListener(lfw);

            panel.add(userLabel);
            panel.add(usrName);
            panel.add(docLabel);
            panel.add(documents);
            panel.add(inviteUser);
            frame.getRootPane().setDefaultButton(inviteUser);
            frame.setVisible(true);
            frame.setResizable(false);
            panel.setOpaque(false);
            frame.add(b);
        }

        private class ListenerForButton implements ActionListener{

            /*listener usato per catturare l'azione della pressione del tasto invite*/
            @Override
            public void actionPerformed(ActionEvent e) {
                if(e.getSource()==inviteUser) {
                    if (usrName.getText().equals(user)) {
                        /*sis ta cercando di invitare se stessi la cosa viene quindi notificata*/
                        JOptionPane.showMessageDialog(InvitationFrame.this, "You can't invite yourself!", "Error", JOptionPane.ERROR_MESSAGE);
                        usrName.requestFocus();
                    }else if (documents.getSelectedIndex()==-1 || usrName.getText().length()==0){
                        /*uno o più campi necessari allo svolgimento dell'operazione sono mancanti*/
                        JOptionPane.showMessageDialog(InvitationFrame.this, "Error!\nMake sure that all field had been completed!", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                    else {
                        /*i campi sono validi quindi procedo a preparare il pacchetto contente tutti i campi necessari all'esecuzione dell'operazione di invite e li mando al server trmaite la socket channel*/
                        ArrayList<String> param = new ArrayList<>();
                        param.add(usrName.getText());
                        param.add(String.valueOf(documents.getSelectedItem()));
                        Parameters request = new Parameters(inviteUser.getText(), param);
                        PrincipalFrame.sendMessage(socketChannel, request);
                        ByteBuffer buff = ByteBuffer.allocate(1024);
                        try {
                            socketChannel.read(buff);
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                        buff.flip();
                        int result = buff.get();
                        /*faccio un controllo sull'esito risultante dell'operazione*/
                        if (result == 0) {
                            /*l'operazione è stata completata con successo chiudo il frame e lo rimuovo dal set dei frame presenti nella sessione di lavoro dell'utente*/
                            JOptionPane.showMessageDialog(InvitationFrame.this, "Invitation successfully sent to  " + usrName.getText() + "!", "", JOptionPane.INFORMATION_MESSAGE);
                            frame.setVisible(false);
                            frame.dispose();
                            internalFrames.remove(frame);
                        } else if (result<0){
                            /*il nome inserito non corrisponde a nessuno degli utenti registrati in turing la cosa viene notificata e si chiede la modifica dell'username*/
                            JOptionPane.showMessageDialog(InvitationFrame.this, "The name you typed does not match any of the users registered in TURING.\nPlease try again.", "Error", JOptionPane.ERROR_MESSAGE);
                            usrName.requestFocus();
                        }
                        else{
                            /*l'utente è stato già invitato a collaborare sul documento dato*/
                            JOptionPane.showMessageDialog(InvitationFrame.this, "It seems that the Username that you have entered is already invited to collaborate on this document!\nPlease try again!", "Error", JOptionPane.ERROR_MESSAGE);
                            usrName.requestFocus();
                        }
                    }
                }
            }
        }

        private class ListenForWindow implements WindowListener{
                @Override
                public void windowOpened(WindowEvent e) {

                }
                /*listener usato per catturare la pressione dell'exit button che provvede a rimovere il frame dal set dei frame presenti nella sessione*/
                @Override
                public void windowClosing(WindowEvent e) {
                    internalFrames.remove(frame);
                }

                @Override
                public void windowClosed(WindowEvent e) {

                }

                @Override
                public void windowIconified(WindowEvent e) {

                }

                @Override
                public void windowDeiconified(WindowEvent e) {

                }

                @Override
                public void windowActivated(WindowEvent e) {

                }

                @Override
                public void windowDeactivated(WindowEvent e) {

                }
            }

        }

        /*frame per gestire l'operazione di visione di un documento intero o di una sua sezione in particolare*/
        private class ShowDoc extends JFrame {
            private File[] dirList;
            private JFrame shFrame;
            private JPanel panel;
            private Dimension size;
            private JLabel doc;
            private JButton showD, showS;
            private JComboBox docName;

            public ShowDoc() {
                shFrame = new JFrame("Show a Document");
                /*aggiungo il frame al set dei frame presenti all'interno della sessione di lavoro dell'utente*/
                internalFrames.add(shFrame);
                shFrame.setSize(400, 250);
                Toolkit toolkit = Toolkit.getDefaultToolkit();
                size = toolkit.getScreenSize();
                int x = (size.width / 2) - (shFrame.getWidth() / 2);
                int y = (size.height / 2) - (shFrame.getHeight() / 2);
                shFrame.setLocation(x, y);
                panel = (JPanel) shFrame.getContentPane();
                panel.setLayout(null);

                ImageIcon background = new ImageIcon("src\\Graphic\\utilback.jpg");
                Image img = background.getImage();
                background = new ImageIcon(img.getScaledInstance(400,250, Image.SCALE_SMOOTH));
                JLabel b = new JLabel("", background, JLabel.CENTER);
                b.setBounds(0,0,400,250);

                doc = new JLabel("Documents: ");
                doc.setFont(f);
                doc.setForeground(Color.WHITE);
                doc.setBounds(20, 30, doc.getPreferredSize().width, doc.getPreferredSize().height);
                doc.setToolTipText("Select the Document wich you are interested to see");

                File dir = new File("src\\Documents");
                dirList = dir.listFiles();
                String[] fileN = new String[dirList.length];
                int i = 0;
                for (File child : dirList) {
                    fileN[i++] = child.getName();
                }

                docName = new JComboBox<>(fileN);
                docName.setBounds(20 + doc.getWidth(), 30, 234, doc.getSize().height);
                docName.setToolTipText("Select the sectons number (at least 1)");

                showD = new JButton("Show Document");
                showD.setFont(f);
                showD.setBounds((shFrame.getWidth() - 153) / 2, 90, showD.getPreferredSize().width, showD.getPreferredSize().height);


                showS = new JButton("Show Section");
                showS.setFont(f);
                showS.setBounds((shFrame.getWidth() - 153) / 2, 150, showD.getPreferredSize().width, showD.getPreferredSize().height);

                ListenerForButton lfb = new ListenerForButton();
                showD.addActionListener(lfb);
                showS.addActionListener(lfb);

                ListenForWindow lfw=new ListenForWindow();
                shFrame.addWindowListener(lfw);

                panel.add(doc);
                panel.add(docName);
                panel.add(showD);
                panel.add(showS);
                shFrame.setVisible(true);
                shFrame.setResizable(false);
                panel.setOpaque(false);
                shFrame.add(b);
            }


            private class ListenerForButton implements ActionListener {

                @Override
                public void actionPerformed(ActionEvent e) {
                    /*listener usato per catturare l'azione della pressione del tasto show Document*/
                    if (e.getSource() == showD) {
                        /*provvedo a generare il pacchetto contente i parametri necessari all'esecuzione dell'operazione di show e li invio attraverso la socket channel
                        * e provvedo a chiudere il frame e rimouverlo dal set dei documenti presenti nella sessione di lavoro dell'utente*/
                        ArrayList<String> fields = new ArrayList<>();
                        fields.add(String.valueOf(docName.getSelectedItem()));
                        Parameters outParam = new Parameters(showD.getText(), fields);
                        PrincipalFrame.sendMessage(socketChannel, outParam);
                        recieveDoc(String.valueOf(docName.getSelectedItem()));
                        shFrame.setVisible(false);
                        shFrame.dispose();
                        internalFrames.remove(shFrame);

                    } else if (e.getSource() == showS) {
                        /*listener usato per catturare l'azione della pressione del tasto show Section che provvede ad aprire un nuovo frame per procedere con l'operazione che richiede il nome
                        * del documento scelto e provvede a chiudere il frame corrente rimuovendolo dal set dei frame presenti nella sessione di lavoro dell'utente*/
                        new SectionFrame(String.valueOf(dirList[docName.getSelectedIndex()]));
                        shFrame.setVisible(false);
                        shFrame.dispose();
                        internalFrames.remove(shFrame);
                    }
                }
            }

            private class ListenForWindow implements WindowListener{
                @Override
                public void windowOpened(WindowEvent e) {

                }

                /*listener usato per catturare la pressione dell'exti button che provvede a chiudere il frame rimuovendolo dal set dei frame presenti nella sessione di lavoro dell'utente*/
                @Override
                public void windowClosing(WindowEvent e) {
                    internalFrames.remove(shFrame);
                }

                @Override
                public void windowClosed(WindowEvent e) {

                }

                @Override
                public void windowIconified(WindowEvent e) {

                }

                @Override
                public void windowDeiconified(WindowEvent e) {

                }

                @Override
                public void windowActivated(WindowEvent e) {

                }

                @Override
                public void windowDeactivated(WindowEvent e) {

                }
            }

            /*frame per gestire l'operazione di visione di una specifica sezione*/
            private class SectionFrame extends JFrame {
                private String source;
                private JFrame secFrame;
                private JPanel panel;
                private JButton showB;
                private JComboBox sections;

                public SectionFrame(String source) {
                    this.source = source;
                    secFrame = new JFrame("Choose a Section");
                    /*lo aggiungo al set dei frame presenti nella sessione di lavoro dell'utente*/
                    internalFrames.add(secFrame);
                    secFrame.setSize(400, 200);
                    Toolkit toolkit = Toolkit.getDefaultToolkit();
                    size = toolkit.getScreenSize();
                    int x = (size.width / 2) - (secFrame.getWidth() / 2);
                    int y = (size.height / 2) - (secFrame.getHeight() / 2);
                    secFrame.setLocation(x, y);
                    ListenerforWindow lfw = new ListenerforWindow();
                    secFrame.addWindowListener(lfw);
                    panel = (JPanel) secFrame.getContentPane();

                    ImageIcon background = new ImageIcon("src\\Graphic\\utilback.jpg");
                    Image img = background.getImage();
                    background = new ImageIcon(img.getScaledInstance(400,200, Image.SCALE_SMOOTH));
                    JLabel b = new JLabel("", background, JLabel.CENTER);
                    b.setBounds(0,0,400,200);

                    File sDir = new File(source);
                    File[] dirs = sDir.listFiles();
                    /*ordino l'array di file ottenuti dal path del documento in modo da visualizzarli in ordine*/
                    Arrays.sort(dirs,Comparator.comparing(File::getName,new FilenameComparator()));
                    String[] sec = new String[dirs.length];
                    int i = 0;
                    for (File child : dirs) {
                        sec[i++] = child.getName();
                    }
                    sections = new JComboBox<>(sec);
                    sections.setBounds(20, 30, 360, sections.getPreferredSize().height);

                    showB = new JButton("Show");
                    showB.setBounds((shFrame.getWidth() - 153) / 2, 120, 153, showB.getPreferredSize().height);
                    ListenerForButton lfb = new ListenerForButton();

                    showB.addActionListener(lfb);

                    panel.add(sections);
                    panel.add(showB);
                    panel.setLayout(null);
                    secFrame.getRootPane().setDefaultButton(showB);
                    secFrame.setVisible(true);
                    secFrame.setResizable(false);
                    panel.setOpaque(false);
                    secFrame.add(b);
                }

                private class ListenerForButton implements ActionListener {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        /*listener usato per catturare la pressione del tasto show*/
                        if (e.getSource() == showB) {
                            /*provvedo a generare il pacchetto contenente i parametri necessari all'perazione di show, li mando al server attraverso la socket channel e chiudo il frame
                            * rimuovendolo dal set dei frame presenti nella sessione di lavoro dell'utente*/
                            ArrayList<String> fields = new ArrayList<>();
                            fields.add(source);
                            fields.add(String.valueOf(sections.getSelectedItem()));
                            Parameters outParam = new Parameters(showS.getText(), fields);
                            PrincipalFrame.sendMessage(socketChannel, outParam);
                            recieveSec(String.valueOf(sections.getSelectedItem()),source);
                            secFrame.setVisible(false);
                            secFrame.dispose();
                            internalFrames.remove(secFrame);
                        }
                    }
                }

                private class ListenerforWindow implements WindowListener {

                    @Override
                    public void windowOpened(WindowEvent e) {

                    }

                    /*listener utilizzato per catturare la pressione dell'exti button che provvede a richiamare il frame padre showDoc, a chiudere questo e rimuoverlo dal set dei frame
                    * presenti nella sessione di lavoro dell'utente*/
                    @Override
                    public void windowClosing(WindowEvent e) {
                        new ShowDoc();
                        secFrame.setVisible(false);
                        secFrame.dispose();
                        internalFrames.remove(secFrame);
                    }

                    @Override
                    public void windowClosed(WindowEvent e) {

                    }

                    @Override
                    public void windowIconified(WindowEvent e) {

                    }

                    @Override
                    public void windowDeiconified(WindowEvent e) {

                    }

                    @Override
                    public void windowActivated(WindowEvent e) {

                    }

                    @Override
                    public void windowDeactivated(WindowEvent e) {

                    }
                }

                /*metodo usato per ricevere una sezione attraverso una socket channel apposita in quanto ha proprietà differenti rispetto a quella principale*/
                private void recieveSec(String sec,String src){
                    try {
                        /*apro la socket channel e mi metto in ascolto sulla porta 2000 in attesa di connessioni*/
                        ServerSocketChannel serverSocketChannel=ServerSocketChannel.open();
                        serverSocketChannel.socket().bind(new InetSocketAddress("localhost",2000));
                        SocketChannel fileReciever=serverSocketChannel.accept();
                        ByteBuffer buffer=ByteBuffer.allocate(1024);
                        buffer.clear();
                        /*creo un documento temporaneo su cui scrivere il file che mi sta venendo inviato*/
                        File file=new File("src\\Documents", sec);
                        if (file.createNewFile()) {
                            /*se l'operazione di creazione è andata a buon fine procedo ad aprire la filechannel e a scrivere dentro il file provvisorio, 1024 bytes alla volta, il file che
                            * mi sta venendo inviato*/
                            FileChannel fileChannel = FileChannel.open(Paths.get("src\\Documents\\" + sec), StandardOpenOption.WRITE);
                            while (fileReciever.read(buffer) > 0) {
                                buffer.flip();
                                fileChannel.write(buffer);
                                buffer.clear();
                            }
                            /*una volta terminata l'operazione di scrittura sul file temporaneo creo il frame per visualizzare il documento*/
                            DocFrame docFrame = new DocFrame(src + "\\" + sec+stub.isEdited(String.valueOf(docName.getSelectedItem()),sections.getSelectedIndex()));
                            /*chiudo sia la filechannel che tutti gli altri canali di comunicazione*/
                            fileChannel.close();
                            fileReciever.close();
                            serverSocketChannel.close();
                            String line;
                            FileReader fr = new FileReader(file);
                            BufferedReader reader = new BufferedReader(fr);
                            /*leggo tutto il file riga per riga e lo carico nel frame*/
                            while ((line = reader.readLine()) != null) {
                                docFrame.textArea.append(line + "\n");
                            }
                            fr.close();
                            reader.close();
                            /*rendo visibile il frame completamente caricato*/
                            docFrame.makeVisible();
                            /*elimino il file di appoggio*/
                            if (!file.delete()){
                                System.err.println("Cannot delete the temporary file");
                            }
                        }
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }

            /*frame utilizzato per la visualizzazione del documento o della sezione richiesta dall'utente*/
            public class DocFrame extends JFrame {

            private JFrame docFrame;
            private JTextArea textArea;
            private String name;

            public DocFrame(String name){
                this.name=name;
                docFrame = new JFrame(this.name);
                /*aggiungo il frame al set dei frame presenti nella sessione di lavoro dell'utente*/
                internalFrames.add(docFrame);

                ListenerForWindow lfw=new ListenerForWindow();
                docFrame.addWindowListener(lfw);

                docFrame.setSize(900, 569);
                Toolkit toolkit = Toolkit.getDefaultToolkit();
                Dimension size = toolkit.getScreenSize();
                int x = (size.width / 2) - (docFrame.getWidth() / 2);
                int y = (size.height / 2) - (docFrame.getHeight() / 2);
                docFrame.setLocation(x, y);
                docFrame.setLocationRelativeTo(null);

                JPanel panel = (JPanel) docFrame.getContentPane();
                panel.setLayout(new BorderLayout());

                textArea = new JTextArea();
                textArea.setBounds(0, 0, panel.getWidth(), panel.getHeight());
                textArea.setFont(f);
                textArea.setEditable(false);
                textArea.setWrapStyleWord(true);
                textArea.setLineWrap(true);

                JScrollPane scrollableTextArea = new JScrollPane(textArea);
                scrollableTextArea.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
                panel.add(scrollableTextArea);

                textArea.setRows(10);
                textArea.setColumns(10);

                panel.setBorder(new EtchedBorder());

                docFrame.setResizable(false);
            }
            /*metodo per rendere visibile il documento*/
            public void makeVisible(){
                docFrame.setVisible(true);
            }
            private class ListenerForWindow implements WindowListener{
                @Override
                public void windowOpened(WindowEvent e) {

                }

                /*listener utilizzato per catturare la pressione dell'exit button che provvede a chiudere il frame e a rimuoverlo dal set dei frames predenti nella sessione di lavoro dell'utente*/
                @Override
                public void windowClosing(WindowEvent e) {
                    internalFrames.remove(docFrame);
                }

                @Override
                public void windowClosed(WindowEvent e) {

                }

                @Override
                public void windowIconified(WindowEvent e) {

                }

                @Override
                public void windowDeiconified(WindowEvent e) {

                }

                @Override
                public void windowActivated(WindowEvent e) {

                }

                @Override
                public void windowDeactivated(WindowEvent e) {

                }
            }
            }

            /*metodo usato per ricevere un intero documento attraverso una socket channel apposita in quanto ha propretà differenti rispetto a quella principale*/
            private void recieveDoc(String docName){
                try {
                    /*apro la socket channel e mi metto in ascolto sulla porta 2000 in attesa di connessioni*/
                    ServerSocketChannel serverSocketChannel=ServerSocketChannel.open();
                    serverSocketChannel.socket().bind(new InetSocketAddress("localhost",2000));
                    SocketChannel fileReciever=serverSocketChannel.accept();
                    ByteBuffer buffer=ByteBuffer.allocate(1024);
                    buffer.clear();
                    /*creo un file temporaneo dove scrivere il documento che mi sta venendo inviato attraverso la socket channel*/
                    File file=new File("src\\Documents\\tmp");
                    if (file.createNewFile()) {
                        /*se la creazione del file è andata a buon fine procedo all'apertura del filechannel e scrivo, 1024 bytes alla volta, il documento che sto ricevendo nel file
                        * di appoggio temporaneo*/
                        FileChannel fileChannel = FileChannel.open(Paths.get("src\\Documents\\tmp"), StandardOpenOption.WRITE);
                        while (fileReciever.read(buffer) > 0) {
                            buffer.flip();
                            fileChannel.write(buffer);
                            buffer.clear();
                        }
                        /*terminata l'operazione di scrittura sul file provvisorio creo il frame dove visualizzare il documento*/
                        DocFrame docFrame = new DocFrame("src\\Documents\\" + docName);
                        /*chiudo la file channel e gli altri canali di comunicazione*/
                        fileChannel.close();
                        fileReciever.close();
                        serverSocketChannel.close();
                        String line;
                        FileReader fr = new FileReader(file);
                        BufferedReader reader = new BufferedReader(fr);
                        /*leggo tutto il file riga per riga e lo carico nel frame*/
                        while ((line = reader.readLine()) != null) {
                            docFrame.textArea.append(line + "\n");
                        }
                        fr.close();
                        reader.close();
                        /*rendo visibile il frame completamente caricato*/
                        docFrame.makeVisible();
                        /*elimino il file di appoggio*/
                        if (!file.delete()){
                            System.err.println("Cannot delete the temporary file");
                        }
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
    }

        /*frame per gestire l'operazione di edit di una sezione di un documento*/
        public class EditFrame extends JFrame{
                private JFrame eFrame;
                private JPanel ePanel;
                private Dimension size;
                private JButton eButton;
                private JComboBox eBoxDoc,eBoxSect;
                private JLabel docName, secName;


           public EditFrame(){
               eFrame = new JFrame("Edit a Document");
               /*aggiungo il frame al set dei frames presenti nella sessione di lavoro degli utenti*/
               internalFrames.add(eFrame);
               eFrame.setSize(400, 250);
               Toolkit toolkit = Toolkit.getDefaultToolkit();
               size = toolkit.getScreenSize();
               int x = (size.width / 2) - (eFrame.getWidth() / 2);
               int y = (size.height / 2) - (eFrame.getHeight() / 2);
               eFrame.setLocation(x, y);

               ImageIcon background = new ImageIcon("src\\Graphic\\utilback.jpg");
               Image img = background.getImage();
               background = new ImageIcon(img.getScaledInstance(400,250, Image.SCALE_SMOOTH));
               JLabel b = new JLabel("", background, JLabel.CENTER);
               b.setBounds(0,0,400,250);

               ePanel = (JPanel) eFrame.getContentPane();
               ePanel.setLayout(null);
               try {
                   ArrayList<String> edDocs=stub.getAllDocs(user);
                   String[] invDocs= edDocs.toArray(new String[0]);
                   eBoxDoc = new JComboBox<>(invDocs);
               } catch (RemoteException e) {
                   e.printStackTrace();
               }
               docName = new JLabel("Document: ");
               docName.setFont(f);
               docName.setForeground(Color.WHITE);
               docName.setBounds(30, 45, docName.getPreferredSize().width, docName.getPreferredSize().height);

               eBoxDoc.setBounds(docName.getWidth()+25, docName.getY(), 240, 23);
               eBoxDoc.setSelectedIndex(-1);

               secName = new JLabel("Section: ");
               secName.setFont(f);
               secName.setForeground(Color.WHITE);
               secName.setBounds(docName.getX(), docName.getY()+60, secName.getPreferredSize().width, secName.getPreferredSize().height);

               eBoxSect=new JComboBox<>();
               eBoxSect.setBounds(secName.getWidth()+25,secName.getY(),eBoxDoc.getWidth(),eBoxDoc.getPreferredSize().height);

               eButton = new JButton("Edit");
               eButton.setFont(f);
               eButton.setBounds((eFrame.getWidth() - 153) / 2, 150, 153, eButton.getPreferredSize().height);

               ListenerForButton lfb=new ListenerForButton();
               eBoxDoc.addActionListener(lfb);
               eButton.addActionListener(lfb);

               ListenForWindow lfw=new ListenForWindow();
               eFrame.addWindowListener(lfw);

               ePanel.add(eBoxDoc);
               ePanel.add(eBoxSect);
               ePanel.add(eButton);
               ePanel.add(docName);
               ePanel.add(secName);
               eFrame.getRootPane().setDefaultButton(eButton);
               eFrame.setVisible(true);
               eFrame.setResizable(false);
               ePanel.setOpaque(false);
               eFrame.add(b);
           }

           private class ListenerForButton implements ActionListener {
               @Override
               public void actionPerformed(ActionEvent e) {
                   /*listener utilizzato in combinazione con la combobox di scelta dei documenti che provvede a caricare, in base al documento scelto, le sezioni di cui è composto
                   * o imposta la combobox vuota se non è stato scelto alcun documento*/
                   if (e.getSource()==eBoxDoc){
                        assert eBoxDoc.getSelectedItem()!=null;
                        if(!eBoxDoc.getSelectedItem().equals("Select Document")) {
                            File sections = new File("src\\Documents\\" + eBoxDoc.getSelectedItem());
                            File[] list = sections.listFiles();
                            Arrays.sort(list,Comparator.comparing(File::getName,new FilenameComparator()));
                            assert list!=null;
                            String[] subDir = new String[list.length + 1];
                            int i=0;
                            for (File sec : list) {
                                subDir[i++] = sec.getName();
                            }
                            eBoxSect.setModel(new DefaultComboBoxModel<>(subDir));


                        }
                        else{
                            eBoxSect.setModel(new DefaultComboBoxModel<>());
                        }
                    }
                   /*listener utilizzato per catturare la pressione del tasto edit*/
                    else if(e.getSource()==eButton){
                        if (eBoxDoc.getSelectedIndex()==-1){
                            /*non è stato scelto alcun documento*/
                            JOptionPane.showMessageDialog(EditFrame.this, "Error!\nYou must choose a document to proceed!", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                        else {
                            try{
                            if (stub.isEditing(user)) {
                                /*l'utente è già coinvolto in una sessione di editing e quindi dovrà prima terminarla per potersi mettere al lavoro sul documento scelto*/
                                JOptionPane.showMessageDialog(EditFrame.this, "You can only edit a section at the time!", "Error", JOptionPane.ERROR_MESSAGE);

                            } else {
                                /*genero il pacchetto contenente i parametri necessari ad eseguire l'operazione di edit e li spedico attraverso la socket channel*/
                                ArrayList<String> fields = new ArrayList<>();
                                fields.add(String.valueOf(eBoxDoc.getSelectedItem()));
                                fields.add(String.valueOf(eBoxSect.getSelectedItem()));
                                fields.add(String.valueOf(eBoxSect.getSelectedIndex()));
                                fields.add(user);
                                Parameters outParam = new Parameters(eButton.getText(), fields);
                                PrincipalFrame.sendMessage(socketChannel, outParam);
                                ByteBuffer buffer = ByteBuffer.allocate(1024);

                                /*apro una socket channel apposita mettendomi in ascolto nell'eventualità che la sezione richiesta non sia già occupata e la possa ricevere*/
                                ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
                                serverSocketChannel.socket().bind(new InetSocketAddress("localhost", 9000));
                                SocketChannel fileReciever = serverSocketChannel.accept();
                                socketChannel.read(buffer);
                                buffer.flip();
                                int result = buffer.get();
                                /*controllo l'esito risultante dell'operazione*/
                                if (result == 1) {
                                    /*la sessione era libera quindi chiamo il metodo editSec per procedere alla modifica, chiudendo questo frame e rimuovendolo dal set dei frames presenti
                                    * nel sessione di lavoro dell'utente*/
                                    editSec(String.valueOf(eBoxSect.getSelectedItem()), String.valueOf(eBoxDoc.getSelectedItem()), fileReciever);
                                    eFrame.setVisible(false);
                                    eFrame.dispose();
                                    internalFrames.remove(eFrame);
                                } else {
                                    /*la sezione è già in editing da parte di un altro utente*/
                                    JOptionPane.showMessageDialog(EditFrame.this, "Error!\nThe section that ou required is already in editing by another user\nTry later or select another section", "Error", JOptionPane.ERROR_MESSAGE);
                                }
                                /*chiudo i canali di comunicazione*/
                                fileReciever.close();
                                serverSocketChannel.close();
                                }
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                        }
                   }
               }
            }

           private class ListenForWindow implements WindowListener{
               @Override
               public void windowOpened(WindowEvent e) {

               }

               /*listener utilizato per catturare la pressione dell'exit button che provvede a chiudere il frame e ad eliminarlo dal set dei documenti presenti nella sessione di lavoro
               * dell'utente*/
               @Override
               public void windowClosing(WindowEvent e) {
                   internalFrames.remove(eFrame);
               }

               @Override
               public void windowClosed(WindowEvent e) {

               }

               @Override
               public void windowIconified(WindowEvent e) {

               }

               @Override
               public void windowDeiconified(WindowEvent e) {

               }

               @Override
               public void windowActivated(WindowEvent e) {

               }

               @Override
               public void windowDeactivated(WindowEvent e) {

               }
           }

           /*frame usato per gestire l'operazione di modifica della sezione indicata dall'utente*/
           private class SecEditingSpace extends JFrame{
               private JFrame editFrame;
               private JPanel editPanel;
               private Dimension size;
               private JTextArea docArea,chatArea,messageArea;
               private JButton endEdit,sendMessage;
               private JScrollPane scrollableDocArea,scrollableChatArea,scrollableMessageArea;
               private  Font f=new Font("Dialog",Font.PLAIN,17);
               private String secName;
               private ChatHandler cH;
               private Thread thr;

               public SecEditingSpace(String secName){
                   this.secName=secName;
                   editFrame=new JFrame(secName);
                   editFrame.setSize(1050,900);
                   Toolkit toolkit=Toolkit.getDefaultToolkit();
                   size=toolkit.getScreenSize();
                   int x= (size.width/2)-(editFrame.getWidth()/2);
                   int y= (size.height/2)-(editFrame.getHeight()/2);
                   editFrame.setLocation(x,y);
                   ListenerForWindow lfw=new ListenerForWindow();
                   editFrame.addWindowListener(lfw);

                   editPanel=(JPanel)editFrame.getContentPane();
                   editPanel.setLayout(null);

                   docArea=new JTextArea();
                   docArea.setWrapStyleWord(true);
                   docArea.setLineWrap(true);
                   scrollableDocArea = new JScrollPane(docArea);
                   scrollableDocArea.setBounds(15,10,680,846);
                   scrollableDocArea.getViewport().setOpaque(false);
                   scrollableDocArea.setOpaque(false);

                   chatArea=new JTextArea();
                   /*inizializzo l'area di chat con i messaggi che sono stati invitati in precedenza (ove dovessero esserci)*/
                   try {
                       for (ChatterBox msg : stub.getHistory(String.valueOf(eBoxDoc.getSelectedItem()))){
                           chatArea.append(msg.getUser()+" ( "+msg.getSection()+"): "+ msg.getMessage()+"\n");
                       }
                   } catch (RemoteException e) {
                       e.printStackTrace();
                   }
                   /*avvio un thread che sta costantemente in ascolto, fino a che questo frame è aperto, in attesa di nuovi messaggi*/
                   try {
                       cH=new ChatHandler(chatArea,stub.getDocAddress(String.valueOf(eBoxDoc.getSelectedItem())));
                       thr=new Thread(cH);
                       thr.start();
                   } catch (RemoteException | UnknownHostException e) {
                       e.printStackTrace();
                   }
                   chatArea.setEditable(false);
                   chatArea.setWrapStyleWord(true);
                   chatArea.setLineWrap(true);
                   scrollableChatArea = new JScrollPane(chatArea);
                   scrollableChatArea.setBounds((scrollableDocArea.getX()*2)+scrollableDocArea.getWidth(),scrollableDocArea.getY(),315,scrollableDocArea.getHeight()-250);
                   scrollableChatArea.getViewport().setOpaque(false);
                   scrollableChatArea.setOpaque(false);

                   messageArea=new JTextArea();
                   messageArea.setWrapStyleWord(true);
                   messageArea.setLineWrap(true);
                   DefaultCaret caret = (DefaultCaret)messageArea.getCaret();
                   caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
                   scrollableMessageArea = new JScrollPane(messageArea);
                   scrollableMessageArea.setBounds((scrollableDocArea.getX()*2)+scrollableDocArea.getWidth(),scrollableChatArea.getHeight()+15,scrollableChatArea.getWidth(),200);
                   scrollableMessageArea.getViewport().setOpaque(false);
                   scrollableMessageArea.setOpaque(false);

                   docArea.setFont(f);
                   chatArea.setFont(f);
                   messageArea.setFont(f);

                   sendMessage=new JButton("Send Message");
                   sendMessage.setFont(f);
                   sendMessage.setBounds(scrollableMessageArea.getX()+(scrollableMessageArea.getWidth()/32),scrollableChatArea.getHeight()+220,sendMessage.getPreferredSize().width,sendMessage.getPreferredSize().height);

                   endEdit=new JButton("End Edit");
                   endEdit.setFont(f);
                   endEdit.setBounds(sendMessage.getX()+sendMessage.getWidth()+20,sendMessage.getY(),sendMessage.getPreferredSize().width,sendMessage.getPreferredSize().height);

                   ListenerForButton lfb=new ListenerForButton();
                   endEdit.addActionListener(lfb);
                   sendMessage.addActionListener(lfb);

                   editPanel.add(scrollableDocArea);
                   editPanel.add(scrollableChatArea);
                   editPanel.add(scrollableMessageArea);
                   editPanel.add(sendMessage);
                   editPanel.add(endEdit);
                   editFrame.setVisible(true);
                   editFrame.setResizable(false);
               }

               private class ListenerForButton implements ActionListener{
                   @Override
                   public void actionPerformed(ActionEvent e) {
                       /*listener utilizzato per gestire la pressione del tasto send message*/
                       if (e.getSource()==sendMessage){
                           if (messageArea.getText().equals("")){
                               /*il messaggio è vuoto è quindi non viene inviato nulla, si sceglie di notificarlo all'utente*/
                               JOptionPane.showMessageDialog(EditFrame.this, "Error!\nYou can't send an empty message!", "Error", JOptionPane.ERROR_MESSAGE);
                               messageArea.requestFocus();
                           }
                           else {
                               /*preparo il pacchetto contenente tutti i parametri necessari ad eseguire l'operazione di send message e lo mando attraverso la socket channel*/
                               ArrayList<String> fields = new ArrayList<>();
                               fields.add(user);
                               fields.add(String.valueOf(eBoxDoc.getSelectedItem()));
                               fields.add(String.valueOf(eBoxSect.getSelectedItem()));
                               fields.add(messageArea.getText());
                               Parameters outParam = new Parameters(sendMessage.getText(), fields);
                               PrincipalFrame.sendMessage(socketChannel, outParam);
                               messageArea.setText("");
                               messageArea.requestFocus();
                           }
                       }
                       /*listener usato per catturare la pressione del tasto end edit*/
                       else if (e.getSource()==endEdit){
                           /*preparo il pacchetto contenente tutti i parametri necessarri all'esecuzione dell'operazione di end edit e li mando attraverso la socket channel*/
                           ArrayList<String> fields=new ArrayList<>();
                           fields.add(String.valueOf(docArea.getText().getBytes().length));
                           fields.add(String.valueOf(eBoxDoc.getSelectedItem()));
                           fields.add(String.valueOf(eBoxSect.getSelectedItem()));
                           fields.add(String.valueOf(eBoxSect.getSelectedIndex()));
                           fields.add(user);
                           Parameters param=new Parameters(endEdit.getText(),fields);
                           PrincipalFrame.sendMessage(socketChannel,param);
                           try {
                               SocketChannel fileSender = SocketChannel.open();
                               fileSender.connect(new InetSocketAddress("localhost",2050));
                               ByteBuffer b=ByteBuffer.allocate(docArea.getText().getBytes().length);
                               b.clear();
                               b.put(docArea.getText().getBytes());
                               b.flip();
                               fileSender.write(b);
                               b.clear();
                               fileSender.close();
                           } catch (IOException ex) {
                               ex.printStackTrace();
                           }
                           /*mandato il pacchetto chiudo il frame e nel farlo killo anche il thread*/
                           editFrame.setVisible(false);
                           cH.terminate();
                           thr.interrupt();
                           editFrame.dispose();
                       }
                   }
               }

               private class ListenerForWindow implements WindowListener{
                   @Override
                   public void windowOpened(WindowEvent e) {

                   }

                   @Override
                   public void windowClosing(WindowEvent e) {
                       /*listener usato per gestire la pressione dell'exit button che, in caso di conferma, provvede ad inviare un pacchetto contenete i parametri necessari per cancellare
                       * tutte le modifihe fatte alla sezione e rimuovere l'utente dal set di editors, altrimenti non fa nulla*/
                       int confirm=JOptionPane.showConfirmDialog(SecEditingSpace.this,"Closing the window will make you lose all the changes made to the document!\nAre you sure?","Warning!",JOptionPane.YES_NO_OPTION);
                       if (confirm==JOptionPane.YES_OPTION){
                           ArrayList<String> fields=new ArrayList<>();
                           fields.add(String.valueOf(eBoxDoc.getSelectedItem()));
                           fields.add(String.valueOf(eBoxSect.getSelectedIndex()));
                           fields.add(user);
                           Parameters param=new Parameters("Edit Frame Closed",fields);
                           PrincipalFrame.sendMessage(socketChannel,param);
                           /*chiudo il frame e killo il thread in ascolto*/
                           editFrame.setVisible(false);
                           cH.terminate();
                           thr.interrupt();
                           editFrame.dispose();
                       }
                       else{
                           editFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
                       }
                   }

                   @Override
                   public void windowClosed(WindowEvent e) {
                   }

                   @Override
                   public void windowIconified(WindowEvent e) {

                   }

                   @Override
                   public void windowDeiconified(WindowEvent e) {

                   }

                   @Override
                   public void windowActivated(WindowEvent e) {

                   }

                   @Override
                   public void windowDeactivated(WindowEvent e) {

                   }
               }
           }

           /*metodo usato per ricevere una sezione da modificare attraverso una socket channel apposita in quanto ha proprietà diverse da quella principale*/
           private void editSec(String sec,String src,SocketChannel fileReciever){
               try {
                   ByteBuffer buffer=ByteBuffer.allocate(1024);
                   buffer.clear();
                   /*creo un file temporaneo di appoggio sul quale scrive la sezione che mi sta arrivando*/
                   File file=new File("src\\Documents", sec);
                   if (file.createNewFile()) {
                       /*se l'operazione di creazione è andata a buon fine procedo ad aprire la filechannel e scrivo sul file temporaneo, 1024 bytes alla volta, la sezione che mi sta arrivando*/
                       FileChannel fileChannel = FileChannel.open(Paths.get("src\\Documents\\" + sec), StandardOpenOption.WRITE);
                       while (fileReciever.read(buffer) > 0) {
                           buffer.flip();
                           fileChannel.write(buffer);
                           buffer.clear();
                       }
                        /*terminata la scrittura sul file temporaneo provvedo a chreare il frame di editing vero e proprio*/
                       SecEditingSpace space=new SecEditingSpace(src+"\\"+sec);
                       fileChannel.close();
                       String line;
                       FileReader fr = new FileReader(file);
                       BufferedReader reader = new BufferedReader(fr);
                       /*leggo tutto il file riga per riga e lo carico nel frame*/
                       while ((line = reader.readLine()) != null) {
                           space.docArea.append(line + "\n");
                       }
                       fr.close();
                       reader.close();
                       /*elimino il file di appoggio*/
                       if (!file.delete()){
                           System.err.println("Cannot delete the temporary file");
                       }
                   }
               } catch (IOException ex) {
                   ex.printStackTrace();
               }
           }
        }

        /*metodo per chiudere tutti i frame aperti nella sessione di lavoro dell'utente*/
        private void disposeAll(){
        for (JFrame fr : internalFrames){
            fr.setVisible(false);
            fr.dispose();
        }
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

