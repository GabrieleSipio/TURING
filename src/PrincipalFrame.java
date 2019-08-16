

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;

@SuppressWarnings("Duplicates")

/*Classe che definisce il frame principale di login/registrazione
* @author Gabriele Sipione mat:534248*/

public class PrincipalFrame extends JFrame {
    private Font f=new Font("Dialog",Font.PLAIN,14);
    private JFrame frame;
    private JLabel usrLabel,psswLabel;
    private JTextField usrtxf;
    private JPasswordField psswf;
    private JButton lgnbttn, rgstrbttn;
    private JCheckBox shwpssw;
    private IRegister stub;
    private Dimension size;
    private SocketChannel socketChannel;

    public PrincipalFrame(IRegister stub, SocketChannel socketChannel){
        this.stub=stub;
        this.socketChannel=socketChannel;
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (ClassNotFoundException | UnsupportedLookAndFeelException | IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        }
        frame=new JFrame();
        frame.setSize(513,369);
        frame.setLocationRelativeTo(null);
        ListenerForWindow lfw=new ListenerForWindow();
        frame.addWindowListener(lfw);
        frame.setTitle("disTribUted collaboRative edItiNG");
        Toolkit toolkit=Toolkit.getDefaultToolkit();
        size=toolkit.getScreenSize();
        int x= (size.width/2)-(frame.getWidth()/2);
        int y= (size.height/2)-(frame.getHeight()/2);
        frame.setLocation(x,y);

        ImageIcon background = new ImageIcon("src\\Graphic\\back.jpg");
        Image img = background.getImage();
        background = new ImageIcon(img.getScaledInstance(513,369, Image.SCALE_SMOOTH));
        JLabel b = new JLabel("", background, JLabel.CENTER);
        b.setBounds(0,0,513,369);

        JPanel panel=(JPanel)frame.getContentPane();
        panel.setLayout(null);



        usrtxf=new JTextField("", 5);
        usrtxf.setBounds(163,87,187,31);

        usrLabel=new JLabel("Username: ");
        usrLabel.setFont(f);
        usrLabel.setBounds(90,92,usrLabel.getPreferredSize().width,usrLabel.getPreferredSize().height);



        psswf=new JPasswordField("");
        psswf.setEchoChar('•');
        psswf.setBounds(163,149,187,31);

        psswLabel=new JLabel("Password: ");
        psswLabel.setFont(f);
        psswLabel.setBounds(93,154,psswLabel.getPreferredSize().width,psswLabel.getPreferredSize().height);

        shwpssw=new JCheckBox("Show Password:");
        shwpssw.setFont(f);
        shwpssw.setHorizontalTextPosition(SwingConstants.LEFT);
        shwpssw.setBounds(236,189,shwpssw.getPreferredSize().width,shwpssw.getPreferredSize().height);

        lgnbttn=new JButton("Login");
        lgnbttn.setBounds(151,229,212,31);

        rgstrbttn=new JButton("Register");
        rgstrbttn.setBounds(151,268,212,31);

        ListenForButton lfb=new ListenForButton();
        lgnbttn.addActionListener(lfb);
        rgstrbttn.addActionListener(lfb);
        ListenForItem lfi=new ListenForItem();
        shwpssw.addItemListener(lfi);
        panel.add(usrtxf);
        panel.add(usrLabel);
        panel.add(psswf);
        panel.add(psswLabel);
        panel.add(shwpssw);
        panel.add(lgnbttn);
        panel.add(rgstrbttn);
        frame.getRootPane().setDefaultButton(lgnbttn);
        frame.setVisible(true);
        frame.setResizable(false);
        panel.setOpaque(false);
        frame.add(b);

    }

    private class ListenForButton implements ActionListener{

        @Override
        public void actionPerformed(ActionEvent e) {
            String username=usrtxf.getText();
            String password=Arrays.toString(psswf.getPassword());
            //login handler
            if (e.getSource()==lgnbttn){
                try {
                    ArrayList<String> fields=new ArrayList<>();
                    fields.add(username);
                    fields.add(password);
                    Parameters outParam=new Parameters("Login",fields);
                    /*mando i parametri dell'oparazione al server*/
                    sendMessage(socketChannel,outParam);
                    ByteBuffer buff= ByteBuffer.allocate(1024);
                    socketChannel.read(buff);
                    buff.flip();
                    int result=buff.get();
                    /*controllo l'esito dell'operazione di login*/
                    if (result<0){
                        /*i dati immessi sono errati e non corrispondono a nessun utente registrato in TURING*/
                        JOptionPane.showMessageDialog(PrincipalFrame.this,"Wrong Username or Password!\nIf you are new to TURING please register!","Error",JOptionPane.ERROR_MESSAGE);
                    }
                    else if (result>0){
                        /*si sta cercando di autenticarsi due volte con lo stesso account*/
                        JOptionPane.showMessageDialog(PrincipalFrame.this,"This user is already online!\nCheck your Username or Password!","Error",JOptionPane.ERROR_MESSAGE);
                    }
                    else {
                        /*i dati immessi sono correnti è si è trovato un riscontro nel db di TURING indipercui viene chiamato il metodo per far visualizzare lo spazio di lavoro*/
                        new WorkSpace(username,stub,socketChannel);
                        frame.setVisible(false);
                        frame.dispose();

                    }

                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            //registration handler
            else if (e.getSource()==rgstrbttn){
                if (usrtxf.getText().length()<4){
                    /*lo username con cui ci si vuole registrare è lungo meno di 4 caratteri */
                    JOptionPane.showMessageDialog(PrincipalFrame.this,"You must select a username that's longer than 4 characters!","Error",JOptionPane.ERROR_MESSAGE);
                }
                else{
                    try {
                        if (!(stub.register(username,password))){
                            /*lo username scelto è già stato preso quindi e occorre sceglierne un'altro*/
                            JOptionPane.showMessageDialog(PrincipalFrame.this,"The username is already associated to another account!","Error",JOptionPane.WARNING_MESSAGE);
                            usrtxf.requestFocus();
                        }
                        else{
                            /*la registrazione si è cnclusa con successo*/
                            JOptionPane.showMessageDialog(PrincipalFrame.this,"Regisration ended with success, enjoy!","",JOptionPane.INFORMATION_MESSAGE);
                        }
                    } catch (RemoteException e1) {
                        e1.printStackTrace();
                    }
                }
                //JOptionPane.showMessageDialog(PrincipalFrame.this,"You want to register!","",JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    private class ListenForItem implements ItemListener{
        /*Listener che serve per implementare il tasto "show password" che quando premuto mostra il testo della password che di norma viene nascosto */
        @Override
        public void itemStateChanged(ItemEvent e) {
            if (e.getStateChange()==ItemEvent.SELECTED){
                psswf.setEchoChar((char)0);
            }
            else{
                psswf.setEchoChar('•');
            }
        }
    }

    private class ListenerForWindow implements WindowListener{

        @Override
        public void windowOpened(WindowEvent e) {

        }

        @Override
        public void windowClosing(WindowEvent e) {
            /*Listener implementato per catturare l'azone di premere l'exti button della finestra che, in caso di conferma dell'azione, disconnnette il client da TURING, altrimenti non fa nulla*/
            int confirm = JOptionPane.showConfirmDialog(PrincipalFrame.this, "Closing the window will make you disconnect from TURING.\nAre you sure?", "Warning!", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {

                ArrayList<String> fields = new ArrayList<>();
                fields.add(usrtxf.getText());
                Parameters outParam = new Parameters("Exit", fields);
                PrincipalFrame.sendMessage(socketChannel, outParam);
                frame.setVisible(false);
                frame.dispose();
                try {
                    socketChannel.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            }
            else{
                frame.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
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

    /*metodo usato per inviare i parametri per le operazioni di I/O di turing al server tramite socket channel*/
    public static void sendMessage(SocketChannel socketChannel,Parameters outParam){
        try {

            ByteArrayOutputStream bos=new ByteArrayOutputStream();
            ObjectOutputStream oos=new ObjectOutputStream(bos);
            oos.writeObject(outParam);
            oos.flush();
            byte[] bytes=bos.toByteArray();
            ByteBuffer buffer= ByteBuffer.allocate(bytes.length);
            buffer.put(bytes);
            buffer.flip();
            socketChannel.write(buffer);
            buffer.clear();
        } catch (IOException e) {
            System.err.println("[TURING client] server has crashed... try to restart you're client later");
            System.exit(-1);
        }

    }
}

