package client_server;

import java.net.ServerSocket;
import java.util.Scanner;
import java.util.regex.Pattern;


public class Client_server {

    public static void main(String[] args) {
        
        Scanner sc = new Scanner(System.in);
        int port = 5500;
        String cUserName;
        String tipo="";
        String ip="";
        

        do{
            System.out.print("Sei un Server o un Client: ");
            tipo=sc.nextLine();
        }while(!tipo.equals("Server") && !tipo.equals("Client"));
        
        if(tipo.equals("Server")){
            Server s = new Server(port);
        }else{
            do{
                System.out.print("User-Name:");
                cUserName=sc.nextLine();
                
                if(!Pattern.matches("^[^@].*", cUserName)){
                    System.out.println("\nErrore --> Il nome non puo' iniziare con @\n");
                }
                
            }while(!Pattern.matches("^[^@].*", cUserName));
            
            System.out.print("Inserire l'IP del Server: ");
            ip=sc.nextLine();
            Client c1 = new Client(ip, port,cUserName);
            c1.AvviaClient();

        }
        
        
        
        //ip: 10.10.0.1
        //Client c2 = new Client("127.0.0.1", port);
        
        
        //c2.AvviaClient();
    }
    
}
