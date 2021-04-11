package workers;

import messages.DeleteMessage;
import peer.Peer;
import utils.Utils;

import java.util.concurrent.TimeUnit;

public class DeleteFileWorker implements Runnable {
    private final Peer peer;
    private final String fileId;

    public DeleteFileWorker(Peer peer, String fileId) {
        this.peer = peer;
        this.fileId = fileId;
    }

    @Override
    public void run() {
        DeleteMessage deleteMessage = new DeleteMessage(this.peer.getProtocolVersion(), this.peer.getId(), this.fileId);

        // Try to send DELETE message max 3 times
        this.peer.getScheduler().submit(() -> this.sendDeleteMessage(deleteMessage, 0));
        this.peer.getStorage().deleteSentChunks(this.fileId);
    }

    private void sendDeleteMessage(DeleteMessage deleteMessage, int attempt) {
        this.peer.sendControlMessage(deleteMessage);
        //System.out.printf("Sent DELETE for chunk %s\n", this.chunk.getUniqueId());

        int currentAttempt = attempt + 1;
        if (attempt < Utils.MAX_3_ATTEMPTS) {
            this.peer.getScheduler().schedule(() -> this.sendDeleteMessage(deleteMessage, currentAttempt), (long) Math.pow(2, attempt) * 1000, TimeUnit.MILLISECONDS);
        }
    }
}
