package workers;

import messages.RemovedMessage;
import peer.Peer;
import storage.Chunk;

public class DeleteChunkWorker implements Runnable {
    private final Peer peer;
    private final Chunk chunk;

    public DeleteChunkWorker(Peer peer, Chunk chunk) {
        this.peer = peer;
        this.chunk = chunk;
    }

    @Override
    public void run() {
        this.peer.getStorage().deleteStoredChunk(this.chunk, "RECLAIMING");

        RemovedMessage removedMessage = new RemovedMessage(this.peer.getProtocolVersion(), this.peer.getId(), this.chunk.getFileId(), this.chunk.getChunkNo());
        this.peer.sendControlMessage(removedMessage);
        //System.out.printf("Sent REMOVED for chunk %s\n", this.chunk.getUniqueId());
        this.peer.getStorage().saveState();
    }
}
