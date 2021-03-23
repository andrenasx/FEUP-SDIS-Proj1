package storage;

import peer.Peer;
import tasks.BackupProtocol;
import tasks.DeleteProtocol;
import utils.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;


public class StorageFile {
    private final Peer peer;
    private final String filePath;
    private String fileId;
    private final int replicationDegree;
    private static final int CHUNK_SIZE = 64000;

    public StorageFile(Peer peer, String filePath, int replicationDegree) throws IOException, NoSuchAlgorithmException {
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

            BackupProtocol bp = new BackupProtocol(this.peer, chunk);
            this.peer.submitBackupThread(bp);

            System.out.printf("Submitted chunk %d of file %s\n", i, fileId);
        }

        // If the file size is a multiple of the chunk size, the last chunk has size 0
        if (fileSize % CHUNK_SIZE == 0) {
            Chunk chunk = new Chunk(this.fileId, ++i, this.replicationDegree, new byte[0]);
            this.peer.getStorage().addSentChunk(chunk);

            BackupProtocol bp = new BackupProtocol(this.peer, chunk);
            this.peer.submitBackupThread(bp);

            System.out.printf("Submitted chunk %d of file %s\n", i, fileId);
        }

        fileReader.close();
    }


    public void delete() {
        DeleteProtocol delete = new DeleteProtocol(this.peer, this.fileId);
        this.peer.submitControlThread(delete);
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
