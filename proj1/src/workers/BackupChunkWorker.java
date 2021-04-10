package workers;

import messages.PutChunkMessage;
import peer.Peer;
import storage.Chunk;
import utils.Utils;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class BackupChunkWorker implements Runnable {
    private final Peer peer;
    private final Chunk chunk;
    private final ScheduledThreadPoolExecutor scheduler;

    public BackupChunkWorker(Peer peer, Chunk chunk) {
        this.peer = peer;
        this.chunk = chunk;
        this.scheduler = new ScheduledThreadPoolExecutor(1);
    }

    @Override
    public void run() {
        // Add chunk to sent chunk map
        if (!this.peer.getStorage().hasSentChunk(this.chunk.getUniqueId())) {
            this.peer.getStorage().addSentChunk(this.chunk);
        }

        PutChunkMessage putChunkMessage = new PutChunkMessage(this.peer, this.chunk);
        // Try to send PUTCHUNK message max 5 times or until Replication degree is met
        this.scheduler.submit(() -> this.sendPutchunkMessage(putChunkMessage, 0));
    }

    private void sendPutchunkMessage(PutChunkMessage putChunkMessage, int attempt) {
        this.peer.sendBackupMessage(putChunkMessage);
        //System.out.printf("Sent PUTCHUNK for chunk %s\n", this.chunk.getUniqueId());

        int currentAttempt = attempt+1;
        if (currentAttempt < Utils.MAX_5_ATTEMPTS && this.chunk.needsReplication()) {
            this.scheduler.schedule(() -> this.sendPutchunkMessage(putChunkMessage, currentAttempt), (long) Math.pow(2, attempt) * 1000, TimeUnit.MILLISECONDS);
        }
        else {
            this.chunk.clearBody();
        }
    }
}
