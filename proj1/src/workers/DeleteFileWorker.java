package workers;

import messages.DeleteMessage;
import peer.Peer;
import utils.Utils;

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
        int attempt = 0;
        do {
            this.peer.sendControlMessage(deleteMessage);
            System.out.printf("Sent DELETE for file %s\n", this.fileId);

            int wait = (int) Math.pow(2, attempt) * 1000;
            Utils.sleep(wait);
        } while (++attempt < Utils.MAX_3_ATTEMPTS);

        this.peer.getStorage().deleteSentChunks(this.fileId);
    }
}
