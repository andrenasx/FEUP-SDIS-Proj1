package peer;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface PeerInterface extends Remote {
    void backup(String filepath, int replication) throws RemoteException;
    void restore(String filepath) throws RemoteException;
    void delete(String filepath) throws RemoteException;
    void reclaim(int diskspace) throws RemoteException;
    String state() throws RemoteException;
}
