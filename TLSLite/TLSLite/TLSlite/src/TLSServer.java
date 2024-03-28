import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.Certificate;
import java.util.Arrays;

public class TLSServer {
    //Server Setup
    private static BigInteger dhPublicKey;
    private static BigInteger dhPrivateKey;

    private static byte[] dhSignedKey;
    private static Certificate serverCert;
    private PrivateKey rsaPrivateKey;
    static BigInteger sharedSecret;

    private static ByteArrayOutputStream allMessages;

    public TLSServer() throws Exception {
        String signedServerCertificate = "/Users/lindsayhaslam/CS6014/CS6014/TLSLite/TLSLite/CASignedServerCertificate.pem";
        String serverPrivateKey = "/Users/lindsayhaslam/CS6014/CS6014/TLSLite/TLSLite/serverPrivateKey.der";

        serverCert = Shared.loadCertificate(signedServerCertificate);
        //DH private key
        dhPrivateKey = new BigInteger(2048, new SecureRandom());
        //DH public key
        dhPublicKey = Shared.getDHPublicKey(dhPrivateKey);
        rsaPrivateKey = Shared.getRSAPrivateKey(serverPrivateKey);
        dhSignedKey = Shared.getDHsignedKey(rsaPrivateKey, dhPublicKey);

        allMessages = new ByteArrayOutputStream();
    }
    public void startServer() throws IOException {
        int serverPort = 5678;
        try (ServerSocket serverSocket = new ServerSocket(serverPort)) {
            System.out.println("Server listening on port " + serverPort);

            while (true) {
                Socket socket = serverSocket.accept();
                ObjectOutputStream serverOut = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream serverIn = new ObjectInputStream(socket.getInputStream());
                System.out.println("Client connected!");
                //Call the handshake!
                performHandshakeSteps(serverIn, serverOut);
                testCommunication(serverIn, serverOut);
            }
            //ClassNotFoundException e was added to avoid putting in random try-catches. (Suggested by IDE) Could be wrong.
        } catch (Exception e) {
            System.out.println("Server exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void performHandshakeSteps(ObjectInputStream serverIn, ObjectOutputStream serverOut) throws Exception {
        //Step 1: Receive nonce from client
        byte[] clientNonce = (byte[]) serverIn.readObject();
        System.out.println("Server: Received nonce from client!");
        allMessages.write(clientNonce);

        //Step 2: Send server's certificate, serverDHPub, and signed Diffie-Hellman public key
        Shared.sendCertAndKeys(serverOut, serverCert, dhPublicKey, dhSignedKey);
        allMessages.write(serverCert.getEncoded());
        allMessages.write(dhPublicKey.toByteArray());
        allMessages.write(dhSignedKey);

        //Step 3: Receive server certificate, serverDHPub, and signed Diffie-Hellman public key
        BigInteger clientDHPub = Shared.validateAndReturnPublicKey(serverIn, allMessages);
        assert(clientDHPub != null);

        //Step 4: Get shared DH secret key
        sharedSecret = Shared.getSharedDHKey(dhPrivateKey, clientDHPub);
        System.out.println("Shared secret!");
        Shared.makeSecretKeys(clientNonce, sharedSecret.toByteArray());
        System.out.println("Server: Secret keys have been generated.");

        //Step 5:
        byte[] allHandshakeMsgs = Shared.macMessage(allMessages.toByteArray(), Shared.serverMAC);
        serverOut.writeObject(allHandshakeMsgs);
        System.out.println("All handshake messages sent to client!");

        //Step 6: Receive handshake messages from server and double check that they are the same
        byte[] serverHandshakeMsg = (byte[]) serverIn.readObject();
        if (Shared.verifyMessageHistory(serverHandshakeMsg, allMessages.toByteArray(), Shared.clientMAC)) {
            System.out.println("Message history match!");
        } else {
            System.out.println("Message history does not match!");
        }
    }

    //TEST
    public static void testCommunication(ObjectInputStream inputStream, ObjectOutputStream outputStream) throws IOException, ClassNotFoundException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        String firstMsg = "What's up, Client? This is the server.";
        byte[] encryptFirstMsg = Shared.encryptMsg(firstMsg.getBytes(), Shared.serverEncrypt, Shared.serverInitVector, Shared.serverMAC);

        System.out.println("Original message to be sent: " + firstMsg);

        System.out.println("Encrypted message to be sent: " + Arrays.toString(encryptFirstMsg));
        outputStream.writeObject(encryptFirstMsg);
        outputStream.flush();
        System.out.println("Message sent to client.");

        byte[] testMsgReceived = (byte[]) inputStream.readObject();
        byte[] decryptedMsg = Shared.decryptMsg(testMsgReceived, Shared.clientEncrypt, Shared.clientInitVector, Shared.clientMAC);
        String plaintextMsg = new String(decryptedMsg, StandardCharsets.UTF_8);

        System.out.println("Encrypted message received: " + Arrays.toString(testMsgReceived));
        System.out.println("Plaintext message received: " + plaintextMsg);
    }

    public static void main(String[] args) throws Exception {
        TLSServer server = new TLSServer();
        try {
            server.startServer();
        } catch (IOException e) {
            System.out.println("Failed to start server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}