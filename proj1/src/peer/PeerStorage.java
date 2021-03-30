package peer;

import storage.Chunk;
import storage.StorageFile;
import workers.DeleteChunkWorker;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentHashMap;

public class PeerStorage implements Serializable {
    private int storageCapacity;
    private final ConcurrentHashMap<String, Chunk> storedChunks;
    private final ConcurrentHashMap<String, Chunk> sentChunks;
    private final ConcurrentHashMap<String, StorageFile> fileMap;
    private final String storagePath;

    public PeerStorage(int id) {
        this.storageCapacity = 100000; // 100000 KB
        this.storedChunks = new ConcurrentHashMap<>();
        this.sentChunks = new ConcurrentHashMap<>();
        this.fileMap = new ConcurrentHashMap<>();

        this.storagePath = "../assets/Peer" + id + "/";
    }

    public void storeChunk(Chunk chunk, byte[] body) throws IOException {
        // Create chunk file
        Path path = Paths.get(this.storagePath + chunk.getUniqueId());
        Files.createDirectories(path.getParent());

        // Write body to file
        FileOutputStream out = new FileOutputStream(this.storagePath + chunk.getUniqueId());
        out.write(body);
        out.close();

        System.out.println("Chunk no " + chunk.getChunkNo() + " stored successfully");
    }

    public byte[] restoreChunkBody(String chunkId) throws IOException {
        // Read all information from chunk file
        File file = new File(this.storagePath + chunkId);
        return Files.readAllBytes(file.toPath());
    }

    public void deleteStoredChunk(Chunk chunk) {
        System.out.printf("Called DELETE for %s\n", this.storagePath + chunk.getUniqueId());

        // Delete stored chunk file and remove it from map
        File file = new File(this.storagePath + chunk.getUniqueId());
        if (file.delete()) {
            System.out.printf("Deleted chunk %s\n", chunk.getUniqueId());
            this.storedChunks.remove(chunk.getUniqueId());
        }
        else {
            System.out.printf("Error deleting chunk %s\n", chunk.getUniqueId());
        }
    }

    public void deleteSentChunks(String fileId) {
        // Delete all sent chunks with given fileId
        for (Chunk chunk : this.sentChunks.values()) {
            if (chunk.getFileId().equals(fileId)) {
                this.sentChunks.remove(chunk.getUniqueId());
            }
        }
    }

    private double getUsedSpace() {
        double used = 0;
        for (Chunk chunk : this.storedChunks.values()) {
            used += chunk.getSize();
        }
        return used;
    }

    public void reclaim(Peer peer, int maxKBytes) {
        if (maxKBytes == 0) {
            for (Chunk chunk : storedChunks.values()) {
                DeleteChunkWorker worker = new DeleteChunkWorker(peer, chunk);
                peer.submitControlThread(worker);
            }
        }
        else {
            this.storageCapacity = maxKBytes;
            // TODO try to delete as many chunk with rep degree above desired as possible to achieve less than new max capacity
        }
    }

    public boolean hasEnoughSpace(double chunkSize) {
        return this.getUsedSpace() + chunkSize <= this.storageCapacity;
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

    public Chunk getStoredChunk(String fileId, int chunkNo) {
        return this.storedChunks.get(fileId + "_" + chunkNo);
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

    public ConcurrentHashMap<String, Chunk> getStoredChunks() {
        return this.storedChunks;
    }

    public ConcurrentHashMap<String, Chunk> getSentChunks() {
        return this.sentChunks;
    }

    public ConcurrentHashMap<String, StorageFile> getFileMap() {
        return fileMap;
    }

    public int getStorageCapacity() {
        return storageCapacity;
    }

    public String getStoragePath() {
        return storagePath;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("---Backed up Files---\n");
        for (StorageFile storageFile : this.fileMap.values()) {
            sb.append("FILE -> pathname: ")
                    .append(storageFile.getFilePath())
                    .append(" ; id: ")
                    .append(storageFile.getFileId())
                    .append(" ; desired replication degree: ")
                    .append(storageFile.getReplicationDegree())
                    .append("\n");

            for (Chunk chunk : this.sentChunks.values()) {
                if (chunk.getFileId().equals(storageFile.getFileId())) {
                    sb.append("\t").append(chunk.toStringSent()).append("\n");
                }
            }
            sb.append("\n");
        }

        sb.append("\n---Stored Chunks---\n");
        for (Chunk chunk : this.storedChunks.values()) {
            sb.append(chunk.toStringStored()).append("\n");
        }

        sb.append("\n---Storage---\n")
                .append("Maximum capacity: ").append(this.storageCapacity).append(" KBytes\n")
                .append("Used space: ").append(this.getUsedSpace()).append(" KBytes\n");

        return sb.toString();
    }
}
