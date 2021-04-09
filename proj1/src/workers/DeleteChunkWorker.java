package workers;

import messages.RemovedMessage;
import peer.Peer;
import storage.Chunk;
import utils.Utils;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class DeleteChunkWorker implements Runnable {
    private final Peer peer;
    private final Chunk chunk;
    private final ScheduledThreadPoolExecutor scheduler;

    public DeleteChunkWorker(Peer peer, Chunk chunk) {
        this.peer = peer;
        this.chunk = chunk;
        this.scheduler = new ScheduledThreadPoolExecutor(1);
    }

    @Override
    public void run() {
        this.peer.getStorage().deleteStoredChunk(this.chunk, "RECLAIMING");

        RemovedMessage removedMessage = new RemovedMessage(this.peer.getProtocolVersion(), this.peer.getId(), this.chunk.getFileId(), this.chunk.getChunkNo());
        // Try to send REMOVED message max 3 times
        this.scheduler.submit(() -> this.sendRemovedMessage(removedMessage, 0));
    }

    private void sendRemovedMessage(RemovedMessage removedMessage, int attempt) {
        if (attempt < Utils.MAX_3_ATTEMPTS) {
            this.peer.sendControlMessage(removedMessage);
            //System.out.printf("Sent REMOVED for chunk %s\n", this.chunk.getUniqueId());

            int finalAttempt = ++attempt;
            this.scheduler.schedule(() -> this.sendRemovedMessage(removedMessage, finalAttempt), (long) (Math.pow(2, attempt) * 1000), TimeUnit.MILLISECONDS);
        }
    }
}
