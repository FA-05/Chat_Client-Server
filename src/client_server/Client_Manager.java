package client_server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Client_Manager implements Runnable {

    private Socket cSocket;
    private int idClient;
    private Server server;
    private BufferedReader inFromClient;
    private PrintWriter outToClient;
    private String cUserName;

    public Client_Manager(Socket cSocket, int idClient, Server server) {
        try {
            this.cSocket = cSocket;
            this.idClient = idClient;
            //riferimento al server chiamante
            this.server = server;

            //creiamo il flusso di IO del client
            this.inFromClient = new BufferedReader(new InputStreamReader(this.cSocket.getInputStream()));
            this.outToClient = new PrintWriter(this.cSocket.getOutputStream(), true);
        } catch (IOException ex) {
            Logger.getLogger(Client_Manager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void run() {

        try {

            cUserName = inFromClient.readLine();
            //System.out.println("[Manager] --> Ho preso in carico il client " + cUserName);

            this.server.addUserNameToList(cUserName); //Aggiungo il nome del client nella Lista

            //inviamo al client il suo id univoco
            outToClient.println(this.idClient);
            this.server.addClientToList(this); // Aggiungo il client-manager alla HashMap

            AvvioManager();

            //eventuali altre operazioni
            //risposta al client
            //outToClient.println("[Manager]-->risposta al client: grazie per tutto il pesce "+cUserName);
        } catch (IOException ex) {
            Logger.getLogger(Client_Manager.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void AvvioManager() throws IOException {

        String messaggio;
        String ListaClient;
        String destinazione;
        String[] infoMessaggio;

        while (true) {
            messaggio = inFromClient.readLine();
            //leggiamo il messaggio inviato dal client
            //System.out.println("\n[Manager- "+cUserName+"] -->Ho ricevuto dal client il seguente messaggio: " + messaggio);

            if (messaggio.equals("@quit") || messaggio.equals("@Q")) {
                System.out.println("\n[Manager-" + cUserName + "] --> Chiudo la risorsa");
                quitConnection();
                break;
            } else if (messaggio.equals("@printC")) {
                ListaClient = this.server.printUserNameList(cUserName);
                outToClient.println(ListaClient);
            } else {

                if (checkMessaggio(messaggio)) {

                    infoMessaggio = messaggio.split("@");

                    destinazione = infoMessaggio[1];
                    messaggio = infoMessaggio[2];

                    System.out.println("\n[Manager-" + cUserName + "] -->Ho ricevuto dal client il seguente messaggio: " + messaggio + " da inviare in " + destinazione);

                    if (destinazione.equals("B") || destinazione.equals("broadcast") || destinazione.equals("b") || destinazione.equals("Broadcast")) {
                        this.server.inviaBroadcast(messaggio, cUserName, idClient);
                    } else {
                        //Verifico se il client esiste 
                        if (this.server.clientExist(destinazione, idClient)) {
                            int cDestID = this.server.getClientID_byHostName(destinazione);
                            this.server.inviaUnicast(messaggio, cUserName ,destinazione, cDestID);
                        } else {
                            outToClient.println("\n[Server]--> Errore: Il Client non esiste (@printC)!\n");
                        }
                    }

                }else{
                    outToClient.println("\n[Server] --> Errore: Il comando e incopleto (@info)\n");
                }

            }
        }
    }

    void OutputClient(String Messaggio) {
        outToClient.println(Messaggio);
    }

    private void quitConnection() {
        try {
            //chiusura delle risortse e della connessione
            this.server.removeClientFromList(this.idClient);
            this.server.removeUserNameToList(cUserName);
            this.inFromClient.close();
            this.outToClient.close();
            this.cSocket.close();
            
        } catch (IOException ex) {
            Logger.getLogger(Client_Manager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public String toString() {
        //ToDo
        return this.cSocket.getInetAddress().getHostAddress() + "\tHostName: " + this.cSocket.getInetAddress().getHostName();
    }

    public int getIdClient() {
        return idClient;
    }

    private boolean checkMessaggio(String messaggio) {
        boolean rtn=false;
        Pattern pattern = Pattern.compile("^@.*@.+");
        Matcher matcher = pattern.matcher(messaggio);
        if(matcher.matches()){
            rtn=true;
        }
        return rtn;
        
    }

}
