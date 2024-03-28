import java.io.*;
import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Arrays;

public class Shared {
    //Session Keys
    static byte[] serverEncrypt;
    static byte[] clientEncrypt;
    static byte[] serverMAC;
    static byte[] clientMAC;
    static byte[] serverInitVector;
    static byte[] clientInitVector;

    //Diffie-Hellman Parameters
    public static final String safePrime1536 = "FFFFFFFFFFFFFFFFC90FDAA22168C234C4C6628B80DC1CD1" +
            "29024E088A67CC74020BBEA63B139B22514A08798E3404DD" +
            "EF9519B3CD3A431B302B0A6DF25F14374FE1356D6D51C245" +
            "E485B576625E7EC6F44C42E9A637ED6B0BFF5CB6F406B7ED" +
            "E386BFB5A899FA5AE9F24117C4B1FE649286651ECE65381D" +
            "8C3871A203B8E301FE0F9D0C9D4CFF3B48973DFF2F21B8A1" +
            "8338DCC7C90C2B3DE2196B85C129E8B3D1E1F3F0D3F000ED" +
            "EDB883DFE16C3C8D31D9207393171A3D6F7DDF3591F2D9FF" +
            "07B4749B2E231C1879498CC0D4DFF6B6C0E078F095BCD2B4" +
            "5E726C4269C01F4D2D5AB9A33181B6A6F3F4F3399B6A1F12" +
            "A344DFFDD6E3E3BFDF6F3D6F1F5A6B6E97037A8DADF70C1E" +
            "8A1E001FFF4FAD021C60557F3E898EFDBC3C529B8E4ADB55" +
            "A3B420F3B9D3FF573C984DC837ACDFAAB85C5F0D7F365866" +
            "EA3669B1CFD055E2AF7C7396FDCEF3B1095DA76A65C5F22A" +
            "255E5E0728E809B6FA7D1DD5347FCCAFB8EE07F30A823C97" +
            "3070A4B533C8D2A519FB699D0B7F3C38A971BF889A3EDB93" +
            "0AB6A233A286C30ADB8E69EABAB6FFCB1E340FCCFBACD251" +
            "BE7FBDB38E327C470FE222B3BEEA2113B7A4137CA67C2D41" +
            "89F5BCC11B2DE4914E6136724187758C2B3AECF8B8E9C6B6" +
            "7FEECF245DB61BDBF97D11D49F7A8443D0822E506A9F4611" +
            "647B5451AAAAAAAAAAAAAAAA";

    //Convert the hexadecimal string to a BigInteger
   public static final BigInteger n = new BigInteger(safePrime1536, 16);
   public static final BigInteger g = BigInteger.valueOf(2);

    //Load the certificate
    public static X509Certificate loadCertificate(String certificatePath) throws Exception{
        FileInputStream file = new FileInputStream(certificatePath);
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate cert = (X509Certificate) cf.generateCertificate(file);
        file.close();
        return cert;
    }

    //Generate DH public key from DH shared params and DH private key
    public static BigInteger getDHPublicKey(BigInteger dhPrivateKey) {
        return g.modPow(dhPrivateKey, n);
    }

   public static byte[] getDHsignedKey(PrivateKey rsaPrivateKey, BigInteger dhPublicKey) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature signer = Signature.getInstance("SHA256withRSA");
        signer.initSign(rsaPrivateKey);
        signer.update(dhPublicKey.toByteArray());
        return signer.sign();
   }

    public static PrivateKey getRSAPrivateKey(String pathRSA) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        InputStream input = new FileInputStream(pathRSA);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(input.readAllBytes());
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(keySpec);
    }

    public static boolean validate(Certificate certificate) throws Exception {
        String certCAFilePath = "/Users/lindsayhaslam/CS6014/CS6014/TLSLite/TLSLite/CACertificate.pem";
        Certificate caCertificate = loadCertificate(certCAFilePath);
        try {
            PublicKey caPublicKey = caCertificate.getPublicKey();
            certificate.verify(caPublicKey);
            return true;
        } catch(Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static BigInteger validateAndReturnPublicKey(ObjectInputStream inputStream, ByteArrayOutputStream allMessages) throws Exception {
     //Read everything
            Certificate certificate = (Certificate) inputStream.readObject();
            allMessages.write(certificate.getEncoded());
            BigInteger dhPublic = (BigInteger) inputStream.readObject();

            allMessages.write(dhPublic.toByteArray());
            byte[] signedDH = (byte[]) inputStream.readObject();
            allMessages.write(signedDH);
            System.out.println("Received handshake files.");

            //Verify certificate and signature
            boolean isValid = Shared.validate(certificate);
            if(isValid){
                //TODO: HI!
                System.out.println("Certificate validated!");
            }
            else{
                System.out.println("Certificate not validated.");
            }

            return dhPublic;
    }

    public static byte[] hkdfExpand(byte[] input, String tag) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec keySpec = new SecretKeySpec(input, "HmacSHA256");
        HMAC.init(keySpec);
        //?
        HMAC.update((tag + "\1").getBytes());

        byte[] okm = HMAC.doFinal();
        byte[] result = new byte[16];
        System.arraycopy(okm, 0, result, 0, result.length);
        return result;
    }

    public static void makeSecretKeys(byte[] clientNonce, byte[] sharedSecret) throws NoSuchAlgorithmException, InvalidKeyException {
        byte[] prk = hkdfExpand(sharedSecret, "masterKey" + new String(clientNonce, StandardCharsets.UTF_8));

        //Derive the necessary keys and IVs using the hkdfExpand() function
        serverEncrypt = hkdfExpand(prk, "server encrypt");
        clientEncrypt = hkdfExpand(serverEncrypt, "client encrypt");
        serverMAC = hkdfExpand(clientEncrypt, "server MAC");
        clientMAC = hkdfExpand(serverMAC, "client MAC");
        serverInitVector = hkdfExpand(clientMAC, "server IV");
        clientInitVector = hkdfExpand(serverInitVector, "client IV");

        System.out.println("Secret key has been made!");
    }

    //Need macMessage()
    public static byte[] macMessage(byte[] message, byte[] key) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "HmacSHA256");
        HMAC.init(secretKeySpec);
        HMAC.update(message);
        return HMAC.doFinal();
    }

    public static boolean verifyMessageHistory(byte[] firstMacMsg, byte[] secondMacMsg, byte[] key) throws NoSuchAlgorithmException, InvalidKeyException {
        byte[] thisSecondMacMsg = Shared.macMessage(secondMacMsg, key);
        System.out.println("Verified message history!");
        return Arrays.equals(thisSecondMacMsg, firstMacMsg);
    }

    static void sendCertAndKeys(ObjectOutputStream outputStream, Certificate certificate, BigInteger publicKey, byte[] signedKey) throws IOException {
        outputStream.writeObject(certificate);
        outputStream.writeObject(publicKey);
        outputStream.writeObject(signedKey);
        System.out.println("Handshake sent!");
        outputStream.flush();
    }

    public static BigInteger getSharedDHKey(BigInteger privateKey, BigInteger otherPublicKey){
        if (privateKey == null) {
            throw new IllegalArgumentException("Error: Private key is null.");
        }
        if (otherPublicKey == null) {
            throw new IllegalArgumentException("Error: Other public key is null.");
        }
        if (n == null) {
            throw new IllegalStateException("Error: Modulus 'n' is null.");
        }
        try {
            return otherPublicKey.modPow(privateKey, n);
        } catch (NullPointerException e) {
            // This catch block is just to catch any unexpected NullPointerException.
            // Ideally, you should never reach this point because of the above checks.
            e.printStackTrace();
            throw new RuntimeException("Unexpected NullPointerException in getSharedDHKey.");
        }
    }

    public static byte[] encryptMsg(byte[] originalText, byte[] encryptKey, byte[] iv, byte[] macKey ) throws NoSuchAlgorithmException, InvalidKeyException, IOException, NoSuchPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {

        // Calculate HMAC
        System.out.println("Encrypt Key: " + Arrays.toString(encryptKey));
        System.out.println("Encrypt IV: " + Arrays.toString(iv));

        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec macKeySpec = new SecretKeySpec(macKey, "HmacSHA256");
        mac.init(macKeySpec);
        byte[] HMAC = mac.doFinal(originalText);

        // Concatenate originalText and HMAC
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byteArrayOutputStream.write(originalText);
        byteArrayOutputStream.write(HMAC);
        byte[] originalTxtWithHMAC = byteArrayOutputStream.toByteArray();

        // Encrypt concatenated data
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKeySpec keySpec = new SecretKeySpec(encryptKey, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);


        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
        return cipher.doFinal(originalTxtWithHMAC); // IV is assumed to be known or handled outside this method

    }

    public static byte[] decryptMsg(byte[] cipherText, byte[] decryptKey, byte[] iv, byte[] macKey) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        //TO CHECK TO SEE IF IV AND KEYS ARE THE SAME
        System.out.println("Decrypt Key: " + Arrays.toString(decryptKey));
        System.out.println("Decrypt IV: " + Arrays.toString(iv));

        //ROUND 3
        // Initialize Cipher for Decryption
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKeySpec keySpec = new SecretKeySpec(decryptKey, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
        byte[] decryptedContent = cipher.doFinal(cipherText);

        // Assuming HMAC-SHA256 produces 32 bytes HMAC
        if (decryptedContent.length < 32) {
            throw new IllegalArgumentException("Decrypted content is too short.");
        }
        byte[] originalText = Arrays.copyOfRange(decryptedContent, 0, decryptedContent.length - 32);
        byte[] extractedHMAC = Arrays.copyOfRange(decryptedContent, decryptedContent.length - 32, decryptedContent.length);

        // Recompute HMAC for the original text to verify
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec macKeySpec = new SecretKeySpec(macKey, "HmacSHA256");
        mac.init(macKeySpec);
        byte[] recomputedHMAC = mac.doFinal(originalText);

        // Check if the recomputed HMAC matches the extracted HMAC
        if (!Arrays.equals(extractedHMAC, recomputedHMAC)) {
            throw new SecurityException("HMAC verification failed, indicating potential tampering.");
        }

        return originalText;
    }
}
