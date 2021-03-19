package Tasks;

import messages.Message;
import messages.STORED;
import peer.Peer;
import storage.Chunk;
import utils.Utils;

public class PutchunkTask extends Task {
    public PutchunkTask(Peer peer, Message message) {
        super(peer, message);
    }

    @Override
    public void run() {

        System.out.println(String.format("Received PUTCHUNK: chunk no: %d ; file: %s",this.message.chunkNo,this.message.fileId));

        Chunk c;
        if(!this.peer.hasStoredChunk(this.message.fileId,this.message.chunkNo)){
            c = new Chunk(this.message);
            this.peer.addStoredChunk(c.getUniqueId(),c);
        }
        else{
            c = this.peer.getStoredChunk(this.message.fileId,this.message.chunkNo);

            if(c.isSavedLocally()){
                System.out.println(String.format("Sent STORED: chunk no: %d ; file: %s",c.getChunkNo(),c.getFileId()));
                STORED message = new STORED(this.peer.getProtocolVersion(),this.peer.getId(),c.getFileId(),c.getChunkNo());
                this.peer.sendControlMessage(message);
                return;
            }
        }

        Utils.sleepRandom();


        if (c.needsReplication()){
            c.setSavedLocally(true);
            c.addPeer(this.peer.getId());
            this.peer.storeChunk(c);

            STORED message = new STORED(this.peer.getProtocolVersion(),this.peer.getId(),c.getFileId(),c.getChunkNo());
            this.peer.sendControlMessage(message);
            System.out.println(String.format("Sent STORED: chunk no: %d ; file: %s",c.getChunkNo(),c.getFileId()));
        }
        else{
            this.peer.removeStoredChunk(c.getUniqueId());
            System.out.println(String.format("Chunk No: %d of file: %s is already completely replicated",c.getChunkNo(),c.getFileId()));
        }


    }

}
