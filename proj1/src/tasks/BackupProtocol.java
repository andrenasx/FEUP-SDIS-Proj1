package Tasks;


import messages.Message;
import messages.PUTCHUNK;
import peer.Peer;
import storage.Chunk;



public class BackupProtocol implements Runnable {
    private Peer peer;
    private Chunk chunk;

    public BackupProtocol(Peer peer, Chunk chunk) {
        this.peer=peer;
        this.chunk=chunk;
    }

    @Override
    public void run() {

        if (this.peer.hasStoredChunk(this.chunk.getUniqueId())) {
           this.peer.addStoredChunk(this.chunk.getUniqueId(),this.chunk);
        }

        Message message = new PUTCHUNK(this.peer,this.chunk);

        int i = 0;
        do {
            this.peer.sendBackupMessage(message);
            System.out.println(String.format("Sent PUTCHUNK: chunk no: %d ; file: %s",message.chunkNo,message.fileId));
            int wait = (int) Math.pow(2, i) * 500;


            try { Thread.sleep(wait); } catch (InterruptedException e) {
                e.printStackTrace();
            }
            i++;
        } while (i < Task.MAX_ATTEMPTS && this.chunk.needsReplication());

        this.chunk.clearBody();
    }
}
