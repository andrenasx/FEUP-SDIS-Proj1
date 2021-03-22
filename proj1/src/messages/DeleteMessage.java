package messages;

import peer.Peer;
import tasks.DeleteTask;
import tasks.PutchunkTask;
import tasks.Task;

import java.nio.charset.StandardCharsets;

public class DeleteMessage extends Message{
    public DeleteMessage(String protocolVersion, int senderId, String fileId) {
        super(protocolVersion, "DELETE", senderId, fileId, -1, 0, new byte[0]);
    }

    @Override
    public byte[] encode() {
        return String.format("%s %s %d %s \r\n\r\n",
                this.protocolVersion,
                this.messageType,
                this.senderId,
                this.fileId).getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public void submitTask(Peer peer) {
        Task task = new DeleteTask(peer, this);
        peer.submitControlThread(task);
    }
}
