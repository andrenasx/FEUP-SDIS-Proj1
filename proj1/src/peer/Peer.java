package peer;

import channel.MulticastChannel;

import java.rmi.RemoteException;

public class Peer implements PeerInterface {
    private String protocolVersion;
    private int id;
    private String serviceAccessPoint; // remoteObjectName since we will be using RMI

    // Multicast Channels
    private MulticastChannel mcChannel; // Control Channel
    private MulticastChannel mdbChannel; // Data Backup Channel
    private MulticastChannel mdrChannel; // Data Restore Channel

    public Peer(String[] args) {
        this.protocolVersion = args[0];
        this.id = Integer.parseInt(args[1]);
        this.serviceAccessPoint = args[2];

        // Create Channels
        mcChannel = new MulticastChannel(args[3], Integer.parseInt(args[4]));
        mdbChannel = new MulticastChannel(args[5], Integer.parseInt(args[6]));
        mdrChannel = new MulticastChannel(args[7], Integer.parseInt(args[8]));
    }

    @Override
    public String toString() {
        return "Peer{" +
                "protocolVersion='" + protocolVersion + '\'' +
                ", id=" + id +
                ", serviceAccessPoint='" + serviceAccessPoint + '\'' +
                ", mcChannel=" + mcChannel +
                ", mdbChannel=" + mdbChannel +
                ", mdrChannel=" + mdrChannel +
                '}';
    }

    public static void main(String[] args) {
        if(args.length != 9){
            System.out.println("Usage: java Peer <protocolVersion> <peerId> <serviceAccessPoint> <mcAddress> <mcPort> <mdbAddress> <mdbPort> <mdrAddress> <mdrPort>");
            return;
        }

        Peer peer = new Peer(args);
        System.out.println(peer);
    }

    @Override
    public void backup(String filepath, int replication) throws RemoteException {
        System.out.println("Implement BACKUP");
    }

    @Override
    public void restore(String filepath) throws RemoteException {
        System.out.println("Implement RESTORE");
    }

    @Override
    public void delete(String filepath) throws RemoteException {
        System.out.println("Implement DELETE");
    }

    @Override
    public void reclaim(int diskspace) throws RemoteException {
        System.out.println("Implement RECLAIM");
    }

    @Override
    public String state() throws RemoteException {
        System.out.println("Implement STATE");
        return null;
    }
}
