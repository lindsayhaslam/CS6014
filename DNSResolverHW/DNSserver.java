import javax.xml.crypto.Data;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;

public class DNSserver {
        //Try catch thisssssss.
        //Maybe add some global variables
        //While true
        //Decode message, which is the request
        //check the cache for the question, in that request
        //getQuestions(), store in questions
        //if found and not expired, then answer is the answer we want (the empty array, by making a new one and add the record so that we only have one answer. Which is why you want to set it to new everytime.)
        //Else, ask google (InetAddress googleDNS = InetAddress.getByName("8.8.8.8")
        //Create new and send the forward packet to Google (socket.send)
        //Declare new datagram packet, called response packet.
        //Decode that
        //Add the answer to the cache.
        //Outside of the IF Else, you send it back to the user. Create responseMessage by calling DNSMessage.buildResponse(), and a line for
        //byte[] responseData = responseMessage.toBytes();
        //Create new responsePacket
        //socket.send(responsePacket)
        //You may not need a socket close.
        //Empty response message
        //Build response is at the bottom
        private static final int SERVER_PORT = 8053;
        private static final String GOOGLE_DNS_ADDRESS = "8.8.8.8";
        private static final int BUFFER_SIZE = 1024;
        private static final DNSCache cache = new DNSCache();

        public static void main(String[] args) {
            System.out.println("Listening on port: " + SERVER_PORT);

            try (DatagramSocket socket = new DatagramSocket(SERVER_PORT)) {
                while (true) {
                    byte[] buffer = new byte[BUFFER_SIZE];
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet); // Receive request from client

                    DNSMessage requestMessage = DNSMessage.decodeMessage(packet.getData());
                    DNSMessage responseMessage = new DNSMessage(); // Initialize an empty response message

                    for (DNSQuestion question : requestMessage.getQuestions()) {
                        DNSRecord record = cache.queryRecord(question);
                        if (record != null && !record.isExpired()) {
                            // If cached answer is found and not expired, use it
                            responseMessage = DNSMessage.buildResponse(requestMessage, new DNSRecord[]{record});
                        } else {
                            // Else, forward the request to Google's DNS
                            InetAddress googleDNS = InetAddress.getByName(GOOGLE_DNS_ADDRESS);
                            DatagramPacket forwardPacket = new DatagramPacket(buffer, buffer.length, googleDNS, 53);
                            socket.send(forwardPacket); // Send request to Google

                            byte[] responseBuffer = new byte[BUFFER_SIZE];
                            DatagramPacket responsePacket = new DatagramPacket(responseBuffer, responseBuffer.length);
                            socket.receive(responsePacket); // Receive response from Google

                            DNSMessage googleResponse = DNSMessage.decodeMessage(responsePacket.getData());
                            for (DNSRecord answer : googleResponse.getAnswer()) {
                                cache.insertRecord(question, answer); // Cache the new answer
                            }
                            responseMessage = googleResponse; // Use Google's response as the response message
                        }
                    }

                    // Send the response back to the client
                    byte[] responseData = responseMessage.toBytes();
                    DatagramPacket responsePacket = new DatagramPacket(responseData, responseData.length, packet.getAddress(), packet.getPort());
                    socket.send(responsePacket); // Send response to the client
                }
            } catch (IOException e) {
                System.err.println("Server exception: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
