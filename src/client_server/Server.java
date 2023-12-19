package client_server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server implements Runnable {

    ServerSocket sSocket = null;
    Thread tServer = null;
    int port;
    static int progressiveClientID = 0;
    private HashMap<Integer, Client_Manager> clientList;
    private List<String> userNameList;

    public Server(int port) {
        this.port = port;
        this.clientList = new HashMap<Integer, Client_Manager>();
        this.userNameList= new ArrayList<String>();
        try {
            sSocket = new ServerSocket(this.port);
            System.out.println("----Server avviato e in attesa di connessione----");
            tServer = new Thread(this);
            
            tServer.start(); //avvio automatico del server
            
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void run() {
        while (true) {
            try {

                //crea un socket per ogni client disonibile
                Socket cSocket = sSocket.accept();
                Client_Manager manager = new Client_Manager(cSocket, ++progressiveClientID, this);
                Thread connManager = new Thread(manager);
                connManager.start();
                
            } catch (IOException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    //funzione per aggiungere un client alla lista del server
    public void addClientToList(Client_Manager client)
    {
        int index_username=0;
        this.clientList.put(client.getIdClient(), client);
        System.out.println("\n----------------------Nuovo client connesso! Ho " + this.clientList.size() + " client in lista----------------------");
        for(Integer i:this.clientList.keySet()) {
            System.out.println("Client : " + i + "\tIP: " + this.clientList.get(i).toString()+ "\tUserName: "+userNameList.get(index_username));
            index_username++;
        }
        System.out.println("---------------------------------------------------------------------------------------");
    }
    
    //funzione per rimuovere un client dalla lista del server
    public void removeClientFromList(int ID)
    {
        int index_username=0;
        this.clientList.remove(ID);
        System.out.println("\n----------------------Client disconnesso! Ho " + this.clientList.size() + " client in lista----------------------");
        for(Integer i:this.clientList.keySet()) {
            System.out.println("Client : " + i + "\tIP: " + this.clientList.get(i).toString()+ "\tUserName: "+userNameList.get(index_username));
            index_username++;
        }
        System.out.println("---------------------------------------------------------------------------------------");
    }
    
    public void addUserNameToList(String UserName){
        userNameList.add(UserName);
    }
    
    
    public void removeUserNameToList(String UserName){
        userNameList.remove(UserName);
        
    }
    
    public String printUserNameList(String cUserName){
        boolean stampa=false;
        int client_index=1;
        String rtn="\n\n---CLIENT DISPONIBILI---";
        for(int i=0;i<userNameList.size();i++){
           if(!cUserName.equals(userNameList.get(i))){
               stampa=true;
               rtn+="\n"+(client_index)+") "+userNameList.get(i);
               client_index++;
           }
           
        }
        
        if(stampa==false){
            rtn+="\nNon ci sono client connessi!";
        }
        
        return rtn+"\n";
    }
    
    
    public  void inviaBroadcast(String Messaggio, String cUserName, int idClient) {
        System.out.println("[Server] --> "+ Messaggio+" inviato in broadcast");
        for(Integer i:this.clientList.keySet()) {
            
            if(idClient !=i){
                this.clientList.get(i).OutputClient("\n[Broadcast]["+cUserName+"] --> "+Messaggio);
            }
        }
        
    }

    boolean clientExist(String destinazione, int idClient) {
        int index_username=0;
        boolean rtn=false;
        for(Integer i:this.clientList.keySet()) {
            
            if(idClient !=i && destinazione.equals(userNameList.get(index_username))){
                rtn=true;
            }
            index_username++;
        }
        
        return rtn;

    }

    public int getClientID_byHostName(String destinazione) {
        int id=0;
        int index_username=0;
        for(Integer i:this.clientList.keySet()) {
            
            if(destinazione.equals(userNameList.get(index_username))){
                id=i;
            }
            index_username++;
        }
        return id;
    }
    
    
    
    void inviaUnicast(String messaggio, String cUserName, String destinazione, int cDestID) {
        
        System.out.println("[Server] --> "+ messaggio+" inviato a "+destinazione);
        for(Integer i:this.clientList.keySet()) {
            
            if(cDestID == i){
                this.clientList.get(i).OutputClient("\n["+cUserName+"] --> "+messaggio);
            }
        }
        
    }
}
