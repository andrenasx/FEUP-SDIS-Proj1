package utils;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

public class Utils {
    public static final int CRLF = 0xDA;
    public static final int MAX_5_ATTEMPTS = 5;
    public static final int MAX_3_ATTEMPTS = 3;
    public static final int CHUNK_SIZE = 64000;

    public static int getRandom(int max) {
        return new Random().nextInt(max + 1);
    }

    public static int getRandomEn(int max, double occupiedSpace, double maxSpace) {
        int lowerBound = (int) (max * (occupiedSpace / maxSpace));
        return new Random().nextInt(max - lowerBound + 1) + lowerBound;
    }

    public static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            System.err.println("Can't sleep");
        }
    }

    public static String createFileId(String filePath) throws IOException, NoSuchAlgorithmException {
        // Create string from file metadata
        String bitstring = filePath;
        String fileId = null;

        BasicFileAttributes metadata = Files.readAttributes(Paths.get(filePath), BasicFileAttributes.class);
        bitstring = filePath + metadata.creationTime() + metadata.lastModifiedTime() + metadata.size();

        // Apply SHA256 to the string
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(bitstring.getBytes(StandardCharsets.UTF_8));
        fileId = String.format("%064x", new BigInteger(1, hash));

        return fileId;
    }

    public static boolean isChunkFromFile(String fileId, String chunkId) {
        // chunkId like <fileId>_<chunkNo>, splits so we compare fileId
        return fileId.equals(chunkId.split("_")[0]);
    }
}
