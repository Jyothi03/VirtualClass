package port_channel;

import models.ClassroomMessage;
import models.Constants;
import java.io.*;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ChannelEndPoint { 
    String nodeId;
    ObjectOutputStream out;
    ObjectInputStream in;
    BufferedReader br; 
    Socket soc = null; 
    String ip; 
    int port; 

    public ChannelEndPoint(String nodeId, String ip, int port) {
        this.nodeId = nodeId; 
        this.ip = ip;
        this.port = port; 
    }

    public void initialize() { 
        while (soc == null) {
            try {
                soc = new Socket(ip, port); 
                
                if (soc != null) {
                    System.out.println("Got a socket."); 
                    in = new ObjectInputStream(soc.getInputStream());
                    out = new ObjectOutputStream(soc.getOutputStream());
                }
            } catch (UnknownHostException e) {
                System.err.println("Don't know about the server."); 
            } catch (IOException e) {
                System.err.println("Couldn't get I/O for the connection to server.");
            }
            
            try { 
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(ChannelEndPoint.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    } 

    public ClassroomMessage receive() { 
        ClassroomMessage result = null;
        
        try {
            result = (ClassroomMessage)in.readObject();
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ChannelEndPoint.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ChannelEndPoint.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return result; 
    } 

    public void send(ClassroomMessage msg) { 
        try { 
            out.writeObject(msg);
            out.flush();
        } catch (SocketException se) { 
            System.exit(1);
        } catch (IOException ioe) {
            ioe.printStackTrace(); 
        }
    }

    public void close() { 
        try { 
            System.out.println("Shutting down ..."); 
            
            long timestamp = System.currentTimeMillis();
            send(new ClassroomMessage(nodeId, timestamp, Constants.MSG_TYPE_LOGOUT, nodeId));
            
            in.close();
            out.close();
            soc.close();
        } catch (Exception e) { 
            e.printStackTrace();
        }
    }
}
