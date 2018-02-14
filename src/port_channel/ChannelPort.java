package port_channel;

import models.ClassroomMessage;
import models.Constants;
import java.util.*;
import java.util.concurrent.*; 
import java.io.*;
import java.net.*; 
import java.util.logging.Level;
import java.util.logging.Logger;

public class ChannelPort {
    private int portNum;
    ServerSocket serverSocket = null;
    Socket clientSocket = null;
    
    private ArrayList<ObjectOutputStream> outs = new ArrayList<ObjectOutputStream>(); 
    private ArrayList<Listener> listeners = new ArrayList<Listener>(); 
    private ArrayList<String> ids = new ArrayList<String>();
    private ConcurrentLinkedQueue<ClassroomMessage> que = new ConcurrentLinkedQueue<ClassroomMessage>(); 
    
    public ChannelPort(int portNum) {
        this.portNum = portNum; 
    } 

    public void initialize() {
        
        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                try { 
                    serverSocket = new ServerSocket(getPortNum());
                } catch (IOException ex) {
                    Logger.getLogger(ChannelPort.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                if (serverSocket == null)
                    return;
                
                while (true) {
                    try {
                        clientSocket = serverSocket.accept();
                        
                        outs.add(new ObjectOutputStream(clientSocket.getOutputStream())); 
                
                        ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream()); 
                        Listener listener = new Listener(in, ChannelPort.this);
                        listeners.add(listener);
                        
                        listener.start();
                    } catch (IOException ex) {
                        Logger.getLogger(ChannelPort.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(ChannelPort.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        });
        
        t.start();
        
        System.out.println("Connections are all established."); 
    } 

    synchronized void gotMessage(ClassroomMessage msg) {
        que.offer(msg); 

        notifyAll(); 
    } 

    public synchronized ClassroomMessage receive() { 
        while (que.isEmpty()) { 
            try {
                wait(); 
            } catch (InterruptedException ire) {
                ire.printStackTrace();
            }
        } 
        
        ClassroomMessage msg = que.poll(); 
        System.out.println("receive: " + msg);
        
        filter(msg);
        
        return msg; 
    } 

    public synchronized void broadcast(ClassroomMessage msg) { 
        for (int i = 0; i < outs.size(); i++) { 
            send(i, msg);
        }
    }
    
    public void send(int index, ClassroomMessage msg) { 
        try {
            outs.get(index).writeObject(msg);
            outs.get(index).flush();
        } catch (IOException ex) {
            Logger.getLogger(ChannelPort.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void filter(ClassroomMessage msg) {
        String id = msg.getPersonId();
        
        if (msg.getType().equals(Constants.MSG_TYPE_LOGIN)) {
            if (ids.indexOf(id) == -1) {
                ids.add(id);
            }
        }
        else if (msg.getType().equals(Constants.MSG_TYPE_LOGOUT)) {
            int pos = ids.indexOf(id);
            if (pos != -1) {
                outs.remove(pos);
                listeners.remove(pos);
                ids.remove(pos);
            }
        }
    }

    /**
     * @return the portNum
     */
    public int getPortNum() {
        return portNum;
    }

    /**
     * @param portNum the portNum to set
     */
    public void setPortNum(int portNum) {
        this.portNum = portNum;
    }

}

class Listener extends Thread { 
    ObjectInputStream in; 
    ChannelPort cPort; 
    final int ERR_THRESHOLD = 100; 

    public Listener(ObjectInputStream in, ChannelPort cPort) { 
        this.in = in;  
        this.cPort = cPort; 
    } 

    public void run() { 
        ClassroomMessage msg; 
        int errCnt = 0; 
        
        while (in != null) { 
            try { 
                msg = (ClassroomMessage)in.readObject(); 
                System.out.println(msg); 
                cPort.gotMessage(msg); 
            } catch (ClassNotFoundException cnfe) { 
                cnfe.printStackTrace(); 
            } catch (SocketException se) { 
                System.err.println(se); 
                errCnt++; 
                if (errCnt > ERR_THRESHOLD)  System.exit(0); 
            } catch (IOException ioe) { 
                ioe.printStackTrace(); 
            }
        }
    }
}

