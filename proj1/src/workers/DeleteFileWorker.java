package workers;

import messages.DeleteMessage;
import peer.Peer;
import utils.Utils;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class DeleteFileWorker implements Runnable {
    private final Peer peer;
    private final String fileId;
    private final ScheduledThreadPoolExecutor scheduler;

    public DeleteFileWorker(Peer peer, String fileId) {
        this.peer = peer;
        this.fileId = fileId;
        this.scheduler = new ScheduledThreadPoolExecutor(1);
    }

    @Override
    public void run() {
        DeleteMessage deleteMessage = new DeleteMessage(this.peer.getProtocolVersion(), this.peer.getId(), this.fileId);

        // Try to send DELETE message max 3 times
        this.scheduler.submit(() -> this.sendDeleteMessage(deleteMessage, 0));
    }

    private void sendDeleteMessage(DeleteMessage deleteMessage, int attempt) {
        if (attempt < Utils.MAX_3_ATTEMPTS) {
            this.peer.sendControlMessage(deleteMessage);
            //System.out.printf("Sent DELETE for chunk %s\n", this.chunk.getUniqueId());

            int finalAttempt = ++attempt;
            this.scheduler.schedule(() -> this.sendDeleteMessage(deleteMessage, finalAttempt), (long) Math.pow(2, attempt) * 1000, TimeUnit.MILLISECONDS);
        }
        else {
            this.peer.getStorage().deleteSentChunks(this.fileId);
        }
    }
}
