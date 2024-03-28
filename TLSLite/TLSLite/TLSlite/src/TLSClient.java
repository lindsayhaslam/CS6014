

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.Certificate;
import java.math.BigInteger;
import java.util.Arrays;


public class TLSClient {
   private final byte[] nonce;
   private static Certificate clientCert;
   private final BigInteger dhPublicKey;
   private final BigInteger dhPrivateKey;
    private PrivateKey rsaPrivateKey;
   private static byte[] dhSignedKey;
   private static ByteArrayOutputStream allMessages;
    public TLSClient() throws Exception {
        String signedClientCertificate = "/Users/lindsayhaslam/CS6014/CS6014/TLSLite/TLSLite/CASignedClientCertificate.pem";
        String clientPrivateKey = "/Users/lindsayhaslam/CS6014/CS6014/TLSLite/TLSLite/clientPrivateKey.der";

        nonce = generateNonce();
        clientCert = Shared.loadCertificate(signedClientCertificate);
        rsaPrivateKey = Shared.getRSAPrivateKey(clientPrivateKey);

        // Correctly initialize dhPrivateKey within the range [1, n-2]
        SecureRandom random = new SecureRandom();
        BigInteger one = BigInteger.ONE;
        BigInteger nMinusTwo = Shared.n.subtract(one.add(one));
        int bitLength = Shared.n.bitLength();
        dhPrivateKey = new BigInteger(bitLength, random).mod(nMinusTwo).add(one);

        System.out.println("dhPrivateKey value: " + dhPrivateKey);

        //dhPrivateKey = new BigInteger(Integer.toString(new SecureRandom().nextInt()));
        dhPublicKey = Shared.getDHPublicKey(dhPrivateKey);
        dhSignedKey = Shared.getDHsignedKey(rsaPrivateKey, dhPublicKey);
        allMessages = new ByteArrayOutputStream();

        //Checking the value
        System.out.println("dhPublicKey value: " + dhPublicKey);
        System.out.println("dhSignedKey length: " + (dhSignedKey != null ? dhSignedKey.length : "null"));
    }

    public void startHandshake(ObjectInputStream inputStream, ObjectOutputStream outputStream) throws Exception {
        try {
            System.out.println("Connected to server!");

            //Step 1: Send nonce
            byte[] clientNonce = generateNonce();
            outputStream.writeObject(clientNonce);
            allMessages.write(clientNonce);

            //Step 2: Receive server certificate, serverDHPub, and signed Diffie-Hellman public key
            BigInteger serverDHPub = Shared.validateAndReturnPublicKey(inputStream, allMessages);

            //Step 3: Send certificate, Diffie-Hellman public key, and signed DHPub to Server
            Shared.sendCertAndKeys(outputStream, clientCert, dhPublicKey, dhSignedKey);
            allMessages.write(clientCert.getEncoded());
            allMessages.write(dhPublicKey.toByteArray());
            allMessages.write(dhSignedKey);

            //FOR TESTING NULL POINTER
            //serverDHPub = Shared.validateAndReturnPublicKey(inputStream, allMessages);
            if (serverDHPub == null) {
                System.out.println("serverDHPub is null");
            } else {
                System.out.println("serverDHPub is not null: " + serverDHPub);
            }

            //TESTING: Logging right before using dhPrivateKey
            System.out.println("Using dhPrivateKey for sharedMasterSecret: " + dhPrivateKey);

            //Step 4: Generate the shared, Master Diffie-Hellman secret key
            //Then compute 6 session keys via a HKDF: 2 each of encryption keys, MAC keys, and initialization vectors (IVs) for CBC.
            BigInteger sharedMasterSecret = Shared.getSharedDHKey(dhPrivateKey, serverDHPub);
            if (sharedMasterSecret == null) {
                System.out.println("sharedMasterSecret is null after using dhPrivateKey");
            } else {
                System.out.println("sharedMasterSecret calculated successfully");
            }
            Shared.makeSecretKeys(clientNonce, sharedMasterSecret.toByteArray());
            System.out.println("FOR TESTING: Client secret keys have been generated.");

            //Step 5: Receive handshake messages from server and double check that they are the same
            byte[] serverHandshakeMsg = (byte[]) inputStream.readObject();
            System.out.println("Prepare for history verification.");
            if (Shared.verifyMessageHistory(serverHandshakeMsg, allMessages.toByteArray(), Shared.serverMAC)) {
                System.out.println("Message history match!");
            } else {
                System.out.println("Message history does not match!");
            }

            //Step 6: Send HMAC of all handshake messages so far (including the previous step) using the clients's MAC key.
            byte[] allHandshakeMsgs = Shared.macMessage(allMessages.toByteArray(), Shared.clientMAC);
            outputStream.writeObject(allHandshakeMsgs);
            System.out.println("All handshake messages sent to server!");


        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Error during TLS handshake", e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private byte[] generateNonce() {
        SecureRandom secureRandom = new SecureRandom();
        //32 bytes = 256 bits
        byte[] nonce = new byte[32];
        //This fills the nonce with random numbers.
        secureRandom.nextBytes(nonce);
        return nonce;
    }


    //TEST
    public static void testCommunication(ObjectInputStream inputStream, ObjectOutputStream outputStream) throws IOException, ClassNotFoundException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        //Receiving and decrypting a message from the server
        byte[] testMsgReceived = (byte[]) inputStream.readObject();
        byte[] decryptedMsg = Shared.decryptMsg(testMsgReceived, Shared.serverEncrypt, Shared.serverInitVector, Shared.serverMAC);
        String plaintextMsg = new String(decryptedMsg, StandardCharsets.UTF_8);

        //Logging received message information
        System.out.println("Encrypted message received: " + Arrays.toString(testMsgReceived));
        System.out.println("Original message received: " + plaintextMsg);

        String secondMsg = "What's up, Server?";
        byte[] encryptedSecondMsg = Shared.encryptMsg(secondMsg.getBytes(), Shared.clientEncrypt, Shared.clientInitVector, Shared.clientMAC);
        System.out.println("Plaintext message to be sent: " + secondMsg);
        System.out.println("Encrypted message to be sent: " + Arrays.toString(encryptedSecondMsg));

        outputStream.writeObject(encryptedSecondMsg);
        outputStream.flush();
        System.out.println("Message sent to server.");
    }

    public static void main(String[] args) throws Exception {
        TLSClient client = new TLSClient();
        String serverAddress = "127.0.0.1";
        int serverPort = 5678;
        try {
            Socket socket = new Socket(serverAddress, serverPort);
            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
            client.startHandshake(inputStream, outputStream);
            client.testCommunication(inputStream, outputStream); // Adjusted to not pass streams since they are now class members
        } catch (Exception e) {
            System.out.println("TLS handshake failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
