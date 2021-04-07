package storage;

import peer.Peer;
import workers.BackupChunkWorker;
import workers.DeleteFileWorker;
import workers.RestoreChunkWorker;
import utils.Utils;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;


public class StorageFile implements Serializable {


    private transient Peer peer;
    private final String filePath;
    private final String fileId;
    private final int replicationDegree;
    private static final int CHUNK_SIZE = 64000;
    private int num_chunks = 0;

    public StorageFile(Peer peer, String filePath, int replicationDegree) throws Exception {
        this.peer = peer;
        this.filePath = filePath;
        this.replicationDegree = replicationDegree;

        this.fileId = Utils.createFileId(filePath);
    }

    public void backup() throws IOException {
        System.out.printf("\n[BACKUP] Initiated backup for file: %s\n", fileId);

        // Read file data, split chunks and send them
        File file = new File(this.filePath);
        int fileSize = (int) file.length();
        FileInputStream fileReader = new FileInputStream(file);

        int i = 0;
        for (int bytesRead = 0; bytesRead < fileSize; i++) {
            byte[] data;
            if (fileSize - bytesRead >= CHUNK_SIZE) {
                data = new byte[CHUNK_SIZE];
                bytesRead += fileReader.read(data, 0, CHUNK_SIZE);
            }
            else {
                data = new byte[fileSize - bytesRead];
                bytesRead += fileReader.read(data, 0, fileSize - bytesRead);
            }

            // Create new Chunk and add to peer storage sentChunk map
            Chunk chunk = new Chunk(this.fileId, i, this.replicationDegree, data);
            this.peer.getStorage().addSentChunk(chunk);
            this.num_chunks++;

            // Submit backup worker
            BackupChunkWorker worker = new BackupChunkWorker(this.peer, chunk);
            this.peer.submitBackupThread(worker);

            System.out.printf("[BACKUP] Submitted backup for chunk: %s_%d\n", fileId, i);
        }

        // If the file size is a multiple of the chunk size, the last chunk has size 0
        if (fileSize % CHUNK_SIZE == 0) {
            Chunk chunk = new Chunk(this.fileId, ++i, this.replicationDegree, new byte[0]);
            this.peer.getStorage().addSentChunk(chunk);
            this.num_chunks++;

            BackupChunkWorker worker = new BackupChunkWorker(this.peer, chunk);
            this.peer.submitBackupThread(worker);

            System.out.printf("[BACKUP] Submitted backup for chunk: %s_%d\n", fileId, i);
        }

        fileReader.close();

    }

    public void delete() {
        // Submit delete worker for this file
        DeleteFileWorker worker = new DeleteFileWorker(this.peer, this.fileId);
        this.peer.submitControlThread(worker);

        System.out.printf("\n[DELETION] Submitted delete for file: %s\n", this.fileId);
    }

    public void restore() throws Exception {
        System.out.printf("\n[RESTORE] Initiated restore for file: %s\n", fileId);

        List<Future<Chunk>> receivedChunks = new ArrayList<>();

        // Create a restore worker for each chunk of the file
        ConcurrentHashMap<String, Chunk> sentChunks = this.peer.getStorage().getSentChunks();
        for (Chunk chunk : sentChunks.values()) {
            if (chunk.getFileId().equals(this.fileId)) {
                RestoreChunkWorker worker = new RestoreChunkWorker(this.peer, chunk);
                receivedChunks.add(this.peer.submitControlThread(worker));
                System.out.printf("[RESTORE] Submitted restore for chunk: %s\n", chunk.getUniqueId());
            }
        }

        // Create restored file path
        File file = new File(this.filePath);
        String restoredFilePath = file.getParent() + "/restored_" + file.getName();
        Files.createDirectories(Paths.get(file.getParent()));

        // Open channel to write information
        RandomAccessFile raf = new RandomAccessFile(restoredFilePath, "rw");
        FileChannel channel = raf.getChannel();

        for (Future<Chunk> chunkFuture : receivedChunks) {
            Chunk chunk = chunkFuture.get();

            // Abort if chunk or its body is null
            if (chunk == null || chunk.getBody() == null) {
                System.err.println("Error retrieving chunk, aborting restore");
                return;
            }
            // Abort if not the last chunk but body has less than 64KB
            else if ((chunk.getChunkNo() != this.num_chunks - 1) && chunk.getBody().length != CHUNK_SIZE) {
                System.err.println("Not last chunk with less than 64KB, aborting restore");
                return;
            }

            // Write body to respective position offset in file
            ByteBuffer buffer = ByteBuffer.wrap(chunk.getBody());
            channel.write(buffer, (long) CHUNK_SIZE * chunk.getChunkNo());

            // Clear Chunk body so we don't waste memory
            chunk.clearBody();
        }

        channel.close();
        raf.close();

        System.out.printf("[RESTORE] Finished restore for file %s\n", this.fileId);
    }


    public String getFilePath() {
        return filePath;
    }

    public String getFileId() {
        return fileId;
    }

    public int getReplicationDegree() {
        return replicationDegree;
    }

    public void setPeer(Peer peer) {
        this.peer = peer;
    }
}
