package storage;

import peer.Peer;
import protocol.BackupProtocol;
import protocol.DeleteProtocol;
import protocol.RestoreProtocol;
import utils.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;


public class StorageFile {
    private final Peer peer;
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

            Chunk chunk = new Chunk(this.fileId, i, this.replicationDegree, data);
            this.peer.getStorage().addSentChunk(chunk);
            this.num_chunks++;

            BackupProtocol bp = new BackupProtocol(this.peer, chunk);
            this.peer.submitBackupThread(bp);

            System.out.printf("Submitted chunk %d of file %s\n", i, fileId);
        }

        // If the file size is a multiple of the chunk size, the last chunk has size 0
        if (fileSize % CHUNK_SIZE == 0) {
            Chunk chunk = new Chunk(this.fileId, ++i, this.replicationDegree, new byte[0]);
            this.peer.getStorage().addSentChunk(chunk);
            this.num_chunks++;

            BackupProtocol bp = new BackupProtocol(this.peer, chunk);
            this.peer.submitBackupThread(bp);

            System.out.printf("Submitted chunk %d of file %s\n", i, fileId);
        }

        fileReader.close();
    }

    public void delete() {
        DeleteProtocol dp = new DeleteProtocol(this.peer, this.fileId);
        this.peer.submitControlThread(dp);
    }

    public void restore() throws Exception {
        List<Future<Chunk>> receivedChunks = new ArrayList<>();

        ConcurrentHashMap<String, Chunk> sentChunks = this.peer.getStorage().getSentChunks();
        for (Map.Entry<String, Chunk> entry : sentChunks.entrySet()) {
            if (Utils.isChunkFromFile(this.fileId, entry.getKey())) {
                RestoreProtocol rp = new RestoreProtocol(this.peer, entry.getValue());
                receivedChunks.add(this.peer.submitControlThread(rp));
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

            // If chunk or its body is null abort
            if (chunk == null || chunk.getBody() == null) {
                System.out.println("Error retrieving chunk, aborting restore");
                return;
            }
            // If not the last chunk but body has less than 64KB abort
            else if ((chunk.getChunkNo() != this.num_chunks - 1) && chunk.getBody().length != CHUNK_SIZE) {
                System.out.println("Not last chunk with less than 64KB, aborting restore");
                return;
            }

            ByteBuffer buffer = ByteBuffer.wrap(chunk.getBody());
            channel.write(buffer, (long) CHUNK_SIZE * chunk.getChunkNo());
        }

        channel.close();
        raf.close();
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
}
