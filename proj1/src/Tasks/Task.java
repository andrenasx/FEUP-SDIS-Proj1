package Tasks;


import messages.Message;
import peer.Peer;
import storage.Chunk;

public abstract class Task implements Runnable{
    protected Peer peer;
    protected Message message;
    public static int MAX_ATTEMPTS =5;


    public Task(Peer peer, Message message){
        this.peer=peer;
        this.message=message;
    }


    @Override
    public void run() {
    }
}
