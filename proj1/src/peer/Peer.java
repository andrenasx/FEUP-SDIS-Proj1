package peer;

import channel.MulticastChannel;
import messages.Message;
import storage.Chunk;
import storage.StorageFile;
import utils.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Peer implements PeerInit {
    private final int id;
    private final String protocolVersion;
    private final String serviceAccessPoint; // remoteObjectName since we will be using RMI

    // Multicast Channels
    private final MulticastChannel mcChannel; // Control Channel
    private final MulticastChannel mdbChannel; // Data Backup Channel
    private final MulticastChannel mdrChannel; // Data Restore Channel

    private final ExecutorService threadPoolMC;
    private final ExecutorService threadPoolMDB;
    private final ExecutorService threadPoolMDR;

    public static int MAX_THREADS = 16;
    public static int MAX_THREADS_C = 128;

    private final ConcurrentHashMap<String, Chunk> storedChunks;
    private final ConcurrentHashMap<String, Chunk> sentChunks;
    private final ConcurrentHashMap<String, StorageFile> fileMap;
    private final String storagePath;

    public Peer(String[] args) throws IOException {
        this.protocolVersion = args[0];
        this.id = Integer.parseInt(args[1]);
        this.serviceAccessPoint = args[2];
        this.storedChunks = new ConcurrentHashMap<>();
        this.sentChunks = new ConcurrentHashMap<>();
        this.fileMap = new ConcurrentHashMap<>();

        // Create Channels
        mcChannel = new MulticastChannel(args[3], Integer.parseInt(args[4]), this);
        mdbChannel = new MulticastChannel(args[5], Integer.parseInt(args[6]), this);
        mdrChannel = new MulticastChannel(args[7], Integer.parseInt(args[8]), this);


        this.threadPoolMC = Executors.newFixedThreadPool(MAX_THREADS_C);
        this.threadPoolMDB = Executors.newFixedThreadPool(MAX_THREADS);
        this.threadPoolMDR = Executors.newFixedThreadPool(MAX_THREADS);

        this.storagePath = "../assets/Peer" + this.id + "/";
    }


    public static void main(String[] args) throws IOException {
        if (args.length != 9) {
            System.out.println("Usage: java Peer <protocolVersion> <peerId> <serviceAccessPoint> <mcAddress> <mcPort> <mdbAddress> <mdbPort> <mdrAddress> <mdrPort>");
            return;
        }

        Peer peer = new Peer(args);


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

    public void addStoredChunk(String chunkId, Chunk chunk) {
        this.storedChunks.put(chunkId, chunk);
    }

    public void removeStoredChunk(String chunkUniqueId) {
        this.storedChunks.remove(chunkUniqueId);
    }

    public void addSentChunk(Chunk chunk) {
        this.sentChunks.put(chunk.getUniqueId(), chunk);
    }

    public Chunk getStoredChunk(String chunkId) {
        return this.storedChunks.get(chunkId);
    }

    public Chunk getStoredChunk(String fileId, int chunkId) {
        return this.storedChunks.get(fileId + "_" + chunkId);
    }

    public Chunk getSentChunk(String chunkId) {
        return this.sentChunks.get(chunkId);
    }

    public Chunk getSentChunk(String fileId, int chunkId) {
        return this.sentChunks.get(fileId + "_" + chunkId);
    }


    public boolean hasStoredChunk(String fileId, int chunkId) {
        return this.storedChunks.containsKey(fileId + "_" + chunkId);
    }

    public boolean hasStoredChunk(String uniqueId) {
        return this.storedChunks.containsKey(uniqueId);
    }

    public boolean hasSentChunk(String fileId, int chunkId) {
        return this.sentChunks.containsKey(fileId + "_" + chunkId);
    }

    public boolean hasSentChunk(String uniqueId) {
        return this.sentChunks.containsKey(uniqueId);
    }

    public void submitControlThread(Runnable action) {
        this.threadPoolMC.submit(action);
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

    public ConcurrentHashMap<String, Chunk> getStoredChunks() {
        return this.storedChunks;
    }


    public void storeChunk(Chunk chunk, byte[] body) throws IOException {
        Path path = Paths.get(this.storagePath + chunk.getUniqueId());
        Files.createDirectories(path.getParent());

        FileOutputStream out = new FileOutputStream(this.storagePath + chunk.getUniqueId());
        out.write(body);
        out.close();

        System.out.println("Chunk no " + chunk.getChunkNo() + " stored successfully");
    }

    public void deleteChunk(Chunk chunk) {
        System.out.printf("Called DELETE for %s\n", this.storagePath + chunk.getUniqueId());
        File file = new File(this.storagePath + chunk.getUniqueId());
        if (file.delete()) {
            System.out.printf("Deleted chunk %s\n", chunk.getUniqueId());
            this.storedChunks.remove(chunk.getUniqueId());
        }
    }

    public void deleteSentChunks(String fileId) {
        for (Chunk chunk : this.storedChunks.values()) {
            if (chunk.getFileId().equals(fileId)) {
                this.storedChunks.remove(chunk.getUniqueId());
            }
        }
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
            this.fileMap.put(filepath, storageFile);
        } catch (Exception e) {
            System.out.printf("Can't backup file %s\n", filepath);
        }
    }

    @Override
    public void delete(String filepath) {
        StorageFile storageFile = this.fileMap.get(filepath);
        storageFile.delete();
        this.fileMap.remove(filepath);

    }

    @Override
    public void restore(String filepath) throws RemoteException {
        System.out.println("Implement RESTORE");
    }


    @Override
    public void reclaim(int diskspace) throws RemoteException {
        System.out.println("Implement RECLAIM");
    }

    @Override
    public String state() throws RemoteException {
        System.out.println("Implement STATE");
        return "IMPLEMENT IT";
    }
}
