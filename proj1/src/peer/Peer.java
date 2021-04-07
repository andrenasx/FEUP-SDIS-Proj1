package peer;

import channel.MulticastChannel;
import messages.Message;
import storage.Chunk;
import storage.StorageFile;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Peer implements PeerInit {
    private final int id;
    private final String protocolVersion;
    private final String serviceAccessPoint; // remoteObjectName since we will be using RMI

    private final PeerStorage storage;

    // Multicast Channels
    private final MulticastChannel mcChannel; // Control Channel
    private final MulticastChannel mdbChannel; // Data Backup Channel
    private final MulticastChannel mdrChannel; // Data Restore Channel

    private final ExecutorService threadPoolMC;
    private final ExecutorService threadPoolMDB;
    private final ExecutorService threadPoolMDR;

    public static int MAX_THREADS = 16;
    public static int MAX_THREADS_C = 128;

    public Peer(String[] args) throws IOException {
        this.protocolVersion = args[0];
        this.id = Integer.parseInt(args[1]);
        this.serviceAccessPoint = args[2];

        // Create Peer Internal State
        storage = PeerStorage.loadStorage(this);

        // Create Channels
        mcChannel = new MulticastChannel(args[3], Integer.parseInt(args[4]), this);
        mdbChannel = new MulticastChannel(args[5], Integer.parseInt(args[6]), this);
        mdrChannel = new MulticastChannel(args[7], Integer.parseInt(args[8]), this);

        this.threadPoolMC = Executors.newFixedThreadPool(MAX_THREADS_C);
        this.threadPoolMDB = Executors.newFixedThreadPool(MAX_THREADS);
        this.threadPoolMDR = Executors.newFixedThreadPool(MAX_THREADS);
    }


    public static void main(String[] args) throws IOException {
        if (args.length != 9) {
            System.out.println("Usage: java Peer <protocolVersion> <peerId> <serviceAccessPoint> <mcAddress> <mcPort> <mdbAddress> <mdbPort> <mdrAddress> <mdrPort>");
            return;
        }

        Peer peer = new Peer(args);
        /*PrintStream fileStream = new PrintStream("peer" + peer.id + ".txt");
        System.setOut(fileStream);*/

        System.out.println(peer);

        // Start RMI
        PeerInit stub = (PeerInit) UnicastRemoteObject.exportObject(peer, 0);
        Registry registry = LocateRegistry.getRegistry();
        registry.rebind(peer.serviceAccessPoint, stub);

        // Execute Channels
        (new Thread(peer.mcChannel)).start();
        (new Thread(peer.mdbChannel)).start();
        (new Thread(peer.mdrChannel)).start();
    }

    public void submitControlThread(Runnable action) {
        this.threadPoolMC.submit(action);
    }

    public Future<Chunk>  submitControlThread(Callable<Chunk> action) {
        return this.threadPoolMC.submit(action);
    }

    public void submitBackupThread(Runnable action) {
        this.threadPoolMDB.submit(action);
    }

    public void submitRestoreThread(Runnable action) {
        this.threadPoolMDR.submit(action);
    }

    public void sendControlMessage(Message message) {
        this.mcChannel.sendMessage(message.encode());
    }

    public void sendBackupMessage(Message message) {
        this.mdbChannel.sendMessage(message.encode());
    }

    public void sendRestoreMessage(Message message) {
        this.mdrChannel.sendMessage(message.encode());
    }

    public String getProtocolVersion() {
        return this.protocolVersion;
    }

    public int getId() {
        return this.id;
    }

    public PeerStorage getStorage() {
        return this.storage;
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

    @Override
    public void backup(String filepath, int replicationDegree) {
        try {
            StorageFile storageFile = new StorageFile(this, filepath, replicationDegree);
            storageFile.backup();
            this.storage.getFileMap().put(filepath, storageFile);
        } catch (Exception e) {
            System.out.println("Can't backup file " + filepath);
        }
    }

    @Override
    public void delete(String filepath) {
        StorageFile storageFile = this.storage.getFileMap().get(filepath);
        if (storageFile == null) {
            System.out.println("Can't delete file " + filepath + ", not found");
            return;
        }
        storageFile.delete();
        this.storage.getFileMap().remove(filepath);
    }

    @Override
    public void restore(String filepath) throws RemoteException {
        StorageFile storageFile = this.storage.getFileMap().get(filepath);
        if (storageFile == null) {
            System.out.println("Can't restore file " + filepath + ", not found");
            return;
        }

        try {
            storageFile.restore();
        } catch (Exception e) {
            System.out.println("Error restoring file " + filepath);
        }
    }

    @Override
    public void reclaim(double maxKBytes) throws RemoteException {
        this.storage.reclaim(this, maxKBytes);
}

    @Override
    public String state() throws RemoteException {
        return this.storage.toString();
    }
}
