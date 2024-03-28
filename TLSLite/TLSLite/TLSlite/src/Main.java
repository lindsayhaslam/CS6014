import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;



public class Main {

    public static void main(String[] args) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, IOException, BadPaddingException, InvalidKeyException {
        //Test to make sure encrypt and decrypt work.
        //Hardcoded 128-bit AES key
        byte[] keyBytes = new byte[] {
                (byte)0x00, (byte)0x01, (byte)0x02, (byte)0x03,
                (byte)0x04, (byte)0x05, (byte)0x06, (byte)0x07,
                (byte)0x08, (byte)0x09, (byte)0x0A, (byte)0x0B,
                (byte)0x0C, (byte)0x0D, (byte)0x0E, (byte)0x0F
        };

        // Hardcoded 128-bit IV
        byte[] ivBytes = new byte[] {
                (byte)0x0F, (byte)0x0E, (byte)0x0D, (byte)0x0C,
                (byte)0x0B, (byte)0x0A, (byte)0x09, (byte)0x08,
                (byte)0x07, (byte)0x06, (byte)0x05, (byte)0x04,
                (byte)0x03, (byte)0x02, (byte)0x01, (byte)0x00
        };

        // Example MAC key (could be same as encryption key for testing)
        byte[] macKey = keyBytes; // In real application scenarios, use a distinct key for MAC

        // Original plaintext
        String plaintext = "This is a test!";
        System.out.println("Original: " + plaintext);

        // Encrypt
        byte[] encrypted = Shared.encryptMsg(plaintext.getBytes(StandardCharsets.UTF_8), keyBytes, ivBytes, macKey);
        System.out.println("Encrypted: " + Arrays.toString(encrypted));

        // Decrypt
        byte[] decrypted = Shared.decryptMsg(encrypted, keyBytes, ivBytes, macKey);
        String decryptedText = new String(decrypted, StandardCharsets.UTF_8);
        System.out.println("Decrypted: " + decryptedText);
    }
}