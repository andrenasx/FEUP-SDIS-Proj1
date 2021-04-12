package workers;

import messages.RemovedMessage;
import peer.Peer;
import storage.Chunk;
import utils.Utils;

import java.util.concurrent.TimeUnit;

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
        // Try to send REMOVED message max 3 times
        this.peer.getScheduler().submit(() -> this.sendRemovedMessage(removedMessage, 0));
    }

    private void sendRemovedMessage(RemovedMessage removedMessage, int attempt) {
        this.peer.sendControlMessage(removedMessage);

        int currentAttempt = attempt + 1;
        if (currentAttempt < Utils.MAX_3_ATTEMPTS) {
            this.peer.getScheduler().schedule(() -> this.sendRemovedMessage(removedMessage, currentAttempt), (long) (Math.pow(2, attempt) * 1000), TimeUnit.MILLISECONDS);
        }
    }
}
