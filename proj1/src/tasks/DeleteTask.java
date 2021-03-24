package tasks;

import messages.DeleteMessage;
import peer.Peer;
import storage.Chunk;

import java.util.concurrent.ConcurrentHashMap;

public class DeleteTask extends Task {
    public DeleteTask(Peer peer, DeleteMessage message) {
        super(peer, message);
    }

    @Override
    public void run() {
        System.out.printf("Received DELETE for file %s\n", this.message.fileId);
        ConcurrentHashMap<String, Chunk> chunks = this.peer.getStorage().getStoredChunks();
        for (Chunk chunk : chunks.values()) {
            if (chunk.getFileId().equals(this.message.fileId)) {
                this.peer.getStorage().deleteChunk(chunk);
            }
        }
    }
}
