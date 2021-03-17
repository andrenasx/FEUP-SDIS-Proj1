package storage;

import Tasks.BackupProtocol;
import peer.Peer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class StorageFile {


    private final Peer peer;
    private String filePath;
    private String fileId;
    private int replicationDegree;
    private static final int CHUNK_SIZE = 64000;

    public StorageFile(Peer peer, String filePath, int replicationDegree) {
        this.peer=peer;
        this.filePath = filePath;
        this.replicationDegree = replicationDegree;

        this.createFileId();
    }


    public void backup() throws IOException {

        File file = new File(this.filePath);
        int fileSize = (int) file.length();
        FileInputStream fileReader = new FileInputStream(file);
        for (int i=0,bytesRead =0;  bytesRead<=fileSize;i++){
            byte[] data;
            if(fileSize-bytesRead>=CHUNK_SIZE){
                data=new byte[CHUNK_SIZE];
                bytesRead+= fileReader.read(data,0,CHUNK_SIZE);
            }
            else{
                data=new byte[fileSize-bytesRead];
                bytesRead+= fileReader.read(data,0,fileSize-bytesRead);
            }

            Chunk chunk = new Chunk(this.fileId,i,this.replicationDegree,data);
            this.peer.addSentChunk(chunk);
            BackupProtocol backup = new BackupProtocol(this.peer,chunk);

            this.peer.submitBackupThread(backup);



            System.out.println(String.format("Submitted chunk %d",i));

            if(fileSize-bytesRead==0){
                if (fileSize%CHUNK_SIZE==0){
                    chunk = new Chunk(this.fileId,++i,this.replicationDegree,new byte[0]);
                    this.peer.addSentChunk(chunk);
                    backup = new BackupProtocol(this.peer,chunk);
                    this.peer.submitBackupThread(backup);
                }
                break;
            }
        }
        fileReader.close();
    }




    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileId() {
        return fileId;
    }

    public int getReplicationDegree() {
        return replicationDegree;
    }

    public void setReplicationDegree(int replicationDegree) {
        this.replicationDegree = replicationDegree;
    }


    private void createFileId() {
        String hashSource = this.filePath;
        try {
            BasicFileAttributes metadata = Files.readAttributes(Paths.get(this.filePath), BasicFileAttributes.class);
            hashSource = this.filePath + metadata.creationTime() + metadata.lastModifiedTime() + metadata.size();
        } catch (IOException e) {
            System.out.println("Can't load metadata");
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(hashSource.getBytes(StandardCharsets.UTF_8));
            this.fileId = String.format("%064x", new BigInteger(1, hash));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }




}
