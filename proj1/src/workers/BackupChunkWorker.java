package workers;

import messages.DeleteMessage;
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
        int attempt=0;
        // Try to send PUTCHUNK message max 5 times or until Replication degree is met
        this.scheduler.schedule(() -> this.sendPutChunk(putChunkMessage, attempt), (int) Math.pow(2, attempt) * 1000, TimeUnit.MILLISECONDS);
    }

    private void sendPutChunk(PutChunkMessage putChunkMessage, int attempt){
        this.peer.sendBackupMessage(putChunkMessage);

        if(attempt<Utils.MAX_5_ATTEMPTS && this.chunk.needsReplication()) {
            int finalAttempt = ++attempt;
            this.scheduler.schedule(() -> this.sendPutChunk(putChunkMessage, finalAttempt), (int) Math.pow(2, attempt) * 1000, TimeUnit.MILLISECONDS);
        }
        else{
            this.chunk.clearBody();
        }

    }


}
