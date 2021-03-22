package tasks;

import messages.DeleteMessage;
import messages.Message;
import peer.Peer;

public class DeleteProtocol implements Runnable{
    private final Peer peer;
    private final String fileId;

    public DeleteProtocol(Peer peer, String fileId) {
        this.peer = peer;
        this.fileId = fileId;
    }

    @Override
    public void run() {
        Message deleteMessage = new DeleteMessage(this.peer.getProtocolVersion(), this.peer.getId(), this.fileId);
        // Try to send DELETE message max 5 times
        int attempt = 0;
        do {
            this.peer.sendControlMessage(deleteMessage);
            System.out.println("Sent DELETE" );
            int wait = (int) Math.pow(2, attempt) * 1000;

            try {
                Thread.sleep(wait);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            attempt++;
        } while (attempt < Task.MAX_ATTEMPTS);

        this.peer.deleteSentChunks(this.fileId);
    }
}
