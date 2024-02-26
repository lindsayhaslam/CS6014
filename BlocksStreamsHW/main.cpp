#include <iostream>
#include <array>
#include <vector>
#include <algorithm> //Shuffle
#include "functions.h"

using namespace std;

using Key = array<uint8_t, 8>;
using Message = array<uint8_t, 8>;
using SubsTable = array<uint8_t, 256>;
using AllSubstitutionTables = array<SubsTable, 8>;

//***PART 1****
int main() {
    //Generating key based on password
    std::string password = "Hello";
    Key key = generateKey(password);

    //The array of arrays
    array<array<uint8_t, 256>, 8> allArrays{};
    //Fill and shuffle substitution table
    for (SubsTable& table : allArrays){
        fillArray(table);
        shuffleSubsTable(table);
    }

    //Part 3!
    //Setting the message
    std::string message = "Lindsay";
    Message blockMessage{};

    //Will need copies fo part 5 (part 2)
    array<uint8_t, 8> blockCopy{};
    array<uint8_t, 8> blockCopy2{};

    //Convert string input into a block message
    for (int i = 0; i < blockMessage.size(); i++){
        //Put characters into block message to fit the format
        blockMessage[i] = message[i];
    }
    

    //Iterate over 16 times, //FILL THIS IN
    for (int j = 0; j < 16; j++) {
        //XOR the current state.
        for (int i = 0; i < blockMessage.size(); i++) {
            blockMessage[i] ^= key[i];
        }
        //If you have an array, that holds all of the substitution tables, then do the next part
        for (int i = 0; i < blockMessage.size(); i++) {
            //Replace message byte with corresponding byte in the array.
            blockMessage[i] = allArrays[i][blockMessage[i]];
        }
        //Rotate state to the left by 1 bit
        rotateLeft(blockMessage);
    }
    cout << "After Encryption: ";
    for(int i = 0; i < blockMessage.size(); i++) {
        cout << blockMessage[i];
    }

    //Make hard copy for part 5.
    copy(begin(blockMessage), end(blockMessage), begin(blockCopy));
    copy(begin(blockMessage), end(blockMessage), begin(blockCopy2));

    //Part 4!
    for (int j = 0; j < 16; j++){
        rotateRight(blockMessage);

        //Index of subst table.
        for (int i = 0; i < blockMessage.size(); i++){
            //Index inside the subst table.
            for (int k = 0; k < 256; k++){
                if (allArrays[i][k] == blockMessage[i]){
                    //Reset block message byte to the index.
                    blockMessage[i] = static_cast<uint8_t>(k);
                    break;
                }
            }
        }
        //Reverse XOR
        for (int i = 0; i < blockMessage.size(); i++){
            blockMessage[i] ^= key[i];
        }
    }

    //Convert back to string
    string decryptedMessage;
    for (int i = 0; i < blockMessage.size(); i++){
        decryptedMessage += static_cast<char>(blockMessage[i]);
    }
    cout << "\nAfter decryption: " << decryptedMessage << "\n";

    //Part 5!
    //DECRYPT A MESSAGE USING THE WRONG PASSWORD.
    array<uint8_t, 8> wrongKey{0,0,0,0,0,0,0,0};
    array<uint8_t, 8> wrongPassword{};
    for (int i = 0; i < 8; i++){
        //Switch up the password.
        password[i] = static_cast<uint8_t>((i + 1) * 4);
    }
    for (int i = 0; i < password.size(); i++){
        key[i % 8] = key[i % 8] ^ password[i];
    }
    cout << "Bad Key: ";
    for (int i = 0; i < 8; i++){
        cout << static_cast<int>(key[i]);
    }

    //Do 16 rounds, like in Part 4 for decryption.
    for (int j = 0; j < 16; j++){
        rotateRight(blockCopy);

        //Index of subst table.
        for (int i = 0; i < blockCopy.size(); i++){
            //Index inside the subst table.
            for (int k = 0; k < 256; k++){
                if (allArrays[i][k] == blockCopy[i]){
                    //Reset block message byte to the index.
                    blockCopy[i] = static_cast<uint8_t>(k);
                    break;
                }
            }
        }
        //Reverse XOR
        for (int i = 0; i < blockCopy.size(); i++){
            blockCopy[i] ^= key[i];
        }
    }

    //Convert back to string
    string decryptedWrongPass;
    for(int i = 0; i < blockCopy.size(); i++){
        decryptedWrongPass += static_cast<char>(blockCopy[i]);
    }
    cout << "\nDecryption after wrong password: " << decryptedWrongPass << "\n";

    //MODIFY 1 BIT OF THE CIPHERTEXT AND THEN DECRYPT WITH CORRECT PASSWORDS.
    blockCopy[4] |= 1 << 2;

    return 0;
}

////*****PART 2********
//class RC4 {
//private:
//    vector<int> S;
//    int i, j;
//
//    void swap(int a, int b) {
//        int temp = S[a];
//        S[a] = S[b];
//        S[b] = temp;
//    }
//
//public:
//    RC4(const string &key) : i(0), j(0), S(256) {
//        for (int k = 0; k < 256; ++k) {
//            S[k] = k;
//        }
//        for (int k = 0, j = 0; k < 256; ++k) {
//            j = (j + S[k] + key[k % key.length()]) % 256;
//            swap(k, j);
//        }
//    }
//
//    int getNextByte() {
//        i = (i + 1) % 256;
//        j = (j + S[i]) % 256;
//        swap(i, j);
//        return S[(S[i] + S[j]) % 256];
//    }
//
//    string encryptDecrypt(const string &data) {
//        string result = data;
//        for (size_t k = 0; k < data.size(); ++k) {
//            result[k] ^= getNextByte();
//        }
//        return result;
//    }
//};
//
////FOR PART 2 OF PART 2
//string xorStrings(const string& a, const string& b) {
//    string result;
//    size_t len = min(a.size(), b.size());
//    for (size_t i = 0; i < len; ++i) {
//        result.push_back(a[i] ^ b[i]);
//    }
//    return result;
//}
//
//int main() {
//    //PART 1: VERIFY THAT DECRYPTING A MESSAGE WITH A DIFFERENT KEY THAN THE ENCRYPTION KEY DOES NOT REVEAL THE PLAINTEXT
//    const string key1 = "key1";
//    const string key2 = "differentKey";
//    const string originalMessage = "Secret Message";
//
//    //Encrypt the message with key1
//    cout << "PART 1!" << endl;
//    RC4 rc4_encrypt(key1);
//    string encryptedMessage = rc4_encrypt.encryptDecrypt(originalMessage);
//    cout << "Encrypted message: " << encryptedMessage << endl;
//
//    //Attempt to decrypt the message with key2
//    RC4 rc4_decrypt(key2);
//    string decryptedMessage = rc4_decrypt.encryptDecrypt(encryptedMessage);
//    cout << "Decrypted message with different key: " << decryptedMessage << endl;
//
//    //Verify that the decrypted message does not reveal the plaintext
//    if (decryptedMessage != originalMessage) {
//        cout << "The decrypted message does not match the original message! \n" << endl;
//    }
//
//    //PART 2: VERIFY THAT ENCRYPTING 2 MESSAGES USING THE SAME KEYSTREAM IS INSECURE
//    const string key = "secretKey";
//    const string plaintext1 = "Message One";
//    const string plaintext2 = "Message Two";
//
//    cout << "PART 2!" << endl;
//
//    //Encrypt both messages with the same key
//    RC4 rc4_encrypt1(key);
//    string encrypted1 = rc4_encrypt1.encryptDecrypt(plaintext1);
//
//    //Re-initialized to simulate starting from the same state
//    RC4 rc4_encrypt2(key);
//    string encrypted2 = rc4_encrypt2.encryptDecrypt(plaintext2);
//
//    //XOR the two encrypted messages
//    string encryptedXOR = xorStrings(encrypted1, encrypted2);
//
//    cout << "Encrypted Message 1: " << encrypted1 << endl;
//    cout << "Encrypted Message 2: " << encrypted2 << endl;
//    cout << "XOR of Encrypted Messages: " << encryptedXOR << endl;
//
//    //PART 3: MODIFY PART OF A MESSAGE USING A BIT-FLIPPING ATTACK.
//    const string key5 = "secretKey";
//    string originalMessage5 = "Your salary is $1000";
//
//    cout << "PART 3!" << endl;
//
//    //Encrypt the original message
//    RC4 rc4_encrypt5(key5);
//    string encryptedMessage5 = rc4_encrypt5.encryptDecrypt(originalMessage5);
//
//    //Determine the position of the segment to change
//    size_t pos = originalMessage5.find("$1000");
//    if (pos != string::npos) {
//        //Original and target segments
//        string originalSegment = "$1000";
//        string targetSegment = "$9999";
//
//        //Apply the difference to the encrypted message by XORing
//        for (size_t i = 0; i < originalSegment.size(); ++i) {
//            encryptedMessage5[pos + i] ^= originalSegment[i] ^ targetSegment[i];
//        }
//    }
//
//    //Decrypt the modified message
//    RC4 rc4_decrypt5(key5);
//    string decryptedModifiedMessage = rc4_decrypt5.encryptDecrypt(encryptedMessage5);
//
//    cout << "Original Message: " << originalMessage5 << endl;
//    cout << "Decrypted Modified Message: " << decryptedModifiedMessage << endl;
//
//    return 0;
//}