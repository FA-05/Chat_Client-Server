package client_server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Client {

    Socket cSocket;
    String hostname;
    int port;
    private int ID;
    private String cUserName;
    private BufferedReader inFromServer;
    private PrintWriter outToServer;
    private Scanner sc = new Scanner(System.in);

    public Client(String hostname, int port, String cUserName)  {
        this.hostname = hostname;
        this.port = port;
        
        try {
            this.cUserName=cUserName;
            //socket con i parametri del server
            this.cSocket = new Socket(this.hostname, this.port);
            
            //creiamo nel costruttore i flussi di I/O
            this.inFromServer = new BufferedReader(new InputStreamReader(this.cSocket.getInputStream()));
            this.outToServer = new PrintWriter(this.cSocket.getOutputStream(), true);
            
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void AvviaClient() {
        try {
                     
            //Invio UserName al server
            outToServer.println(cUserName);
            //assegnamo al client l'ID comunicato dal server
            this.setID(Integer.parseInt(inFromServer.readLine()));
            
            AvviaChat();
            
            //lettura risposta del server
            //System.out.println("risposta dal server: " + inFromServer.readLine());


        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void AvviaChat() throws IOException {
        
        // Crea e avvia un thread per ascoltare i messaggi in arrivo
        Thread listenerThread = new Thread(() -> {
            try {
                String serverMessage;

                while (true) {
                    
                    if(Thread.currentThread().isInterrupted()){
                        break;
                    }
                    
                    serverMessage = inFromServer.readLine();
                    
                    if (serverMessage != null){
                        System.out.println(serverMessage);
                    }   
                }

            } catch (IOException e) {
                if (!Thread.currentThread().isInterrupted()) {
                    e.printStackTrace();
                }
            }
        });
        
        listenerThread.start();
        
        while (true) {
            try {
                listenerThread.sleep(100);
            } catch (InterruptedException ex) {
                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            }
            System.out.print("[" + cUserName + "]-->");
            String messaggio = sc.nextLine();

            if (messaggio.equals("@quit") ) {
                System.out.println("Arrivederci "+cUserName);
                listenerThread.interrupt(); // Interruzione del thread di ascolto
                outToServer.println("@quit");
                break;
            } else if(messaggio.equals("@option") || messaggio.equals("@?")|| messaggio.equals("@info")){
                stampaOptionMenu();
            }else{
                outToServer.println(messaggio);
            }
        }   
        
        System.out.println("["+cUserName+"] --> Chiudo le risore");
        //chiudiamo le risorse
        outToServer.close();
        inFromServer.close();
        this.cSocket.close();
        
        
    }
    
    
    

    //getter & setter per l'ID univoco del client
    public int getID() {
        return ID;
    }

    private void setID(int ID) {
        this.ID = ID;
    }

    private void stampaOptionMenu() {
        System.out.println("\n\t\t\t---MENU---");
        System.out.println("@broadcast@Message\t--> To send the message to all client");
        System.out.println("@UserName@Message\t--> To send the message to a specific client");
        System.out.println("@printC \t\t--> To print the list of all client");
        System.out.println("@quit \t\t\t--> To quit the chat\n");
    }
}
