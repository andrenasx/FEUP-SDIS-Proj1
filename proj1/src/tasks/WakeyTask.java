package tasks;

import messages.WakeyMessage;
import peer.Peer;
import workers.DeleteFileTCPWorker;

import java.util.Map;
import java.util.Set;

public class WakeyTask extends Task {
    public WakeyTask(Peer peer, WakeyMessage message) {
        super(peer, message);
    }

    @Override
    public void run() {
        System.out.printf("[WAKEY] Received WakeyWakey from Peer%d\n", this.message.getSenderId());

        // Send DELETE message for files that the peer that woke up had backed up
        for (Map.Entry<String, Set<Integer>> entry : this.peer.getStorage().getDeletedFilesMap().entrySet()) {
            if (entry.getValue().contains(this.message.getSenderId())) {
                // Submit delete worker for this file
                DeleteFileTCPWorker worker = new DeleteFileTCPWorker(this.peer, entry.getKey(), new String(this.message.getBody()));
                this.peer.submitControlThread(worker);

                System.out.printf("[DELETION-TCP] Submitted delete for Peer%d for file: %s\n", this.message.getSenderId(), entry.getKey());
            }
        }
    }
}
