package workers;

import messages.DeleteMessage;
import peer.Peer;
import tasks.Task;

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
        // Try to send DELETE message max 5 times
        int attempt = 0;
        do {
            this.peer.sendControlMessage(deleteMessage);
            System.out.println("Sent DELETE");
            int wait = (int) Math.pow(2, attempt) * 1000;

            try {
                Thread.sleep(wait);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while (++attempt < Task.MAX_ATTEMPTS);

        this.peer.getStorage().deleteSentChunks(this.fileId);
    }
}
