import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class DNSMessage {
    private DNSHeader header;
    private DNSQuestion[] questions;
    private DNSRecord[] answers;
    private DNSRecord[] authorityRecords;
    private DNSRecord[] additionalRecords;
    static byte[] messageBytes;

    HashMap<String, Integer> domainLocations;

    public DNSQuestion[] getQuestions(){
        return questions;
    }
    public DNSRecord[] getAnswer(){
        return answers;
    }
    public DNSHeader getHeader() {
        return header;
    }

    //Empty constructor
    public DNSMessage(){
    }


    public static DNSMessage decodeMessage(byte[] bytes) throws IOException {
        DNSMessage message = new DNSMessage();
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        message.messageBytes = bytes;
        //Store parts in array.
        //Initialize arrays according to the size by the counts of the decoded header.
        message.header = DNSHeader.decodeHeader(byteArrayInputStream);
        message.questions = new DNSQuestion[message.header.getQuestions()];
        message.answers = new DNSRecord[message.header.getAnswerRRs()];
        message.authorityRecords = new DNSRecord[message.header.getAuthorityRRs()];
        message.additionalRecords = new DNSRecord[message.header.getAdditionalRRs()];

//        for (int i = 0; i < message.header.getQuestions(); i++) {
//            message.questions[i] = DNSQuestion.decodeQuestion(byteArrayInputStream, message);
//        }
//
//        for (int i = 0; i < message.header.getAnswerRRs(); i++) {
//            message.answers[i] = DNSRecord.decodeRecord(dataInputStream, message);
//        }
//
//        for (int i = 0; i < message.header.getAuthorityRRs(); i++) {
//            message.authorityRecords[i] = DNSRecord.decodeRecord(dataInputStream, message);
//        }
//        for (int i = 0; i < message.header.getAdditionalRRs(); i++) {
//            message.additionalRecords[i] = DNSRecord.decodeRecord(dataInputStream, message);
//        }

        return message;
    }

    public static String[] readDomainName(InputStream inputStream) throws IOException {
//        DataInputStream dataInputStream = new DataInputStream(inputStream);
//        String qName = "";
//        int length;
//        ArrayList<String> arrayList = new ArrayList<>();
//
//        while ((length = dataInputStream.readByte())> 0){
//            byte[] record = new byte[length];
//            for(int i = 0; i < length; i++){
//                record[i] = dataInputStream.readByte();
//            }
//            qName = new String (record, StandardCharsets.UTF_8);
//            arrayList.add(qName);
//        }
//
//        String[] returnString = new String[arrayList.size()];
//        for(int i = 0; i < arrayList.size(); i++){
//            returnString[i] = arrayList.get(i);
//        }

        List<String> labels = new ArrayList<>();
        DataInputStream stream = new DataInputStream(inputStream);
        byte length = stream.readByte();
        if (length == 0){
            return new String[0];
        }

        while(length!=0){
            byte[] buffer;
            buffer = stream.readNBytes(length);
            String str = new String(buffer, StandardCharsets.UTF_8);
            labels.add(str);
            length = stream.readByte();
        }
        return labels.toArray(new String[0]);
    }

    public static String[] readDomainName(int firstByte) throws IOException {
        //Read the pieces of a domain name from a specific position in the message (for compression)
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(messageBytes, firstByte, messageBytes.length - firstByte);
        return readDomainName(byteArrayInputStream);
    }


    public static DNSMessage buildResponse(DNSMessage request, DNSRecord[] answers) {
            DNSMessage response = new DNSMessage();
            response.header = DNSHeader.buildHeaderForResponse(request, response);
            response.questions = request.questions;
            response.answers = answers;
            // Initialize authority and additional records if necessary
            // For a basic implementation, these can be empty arrays.
            response.authorityRecords = new DNSRecord[0];
            response.additionalRecords = new DNSRecord[0];
            return response;
        }

    public byte[] toBytes() throws IOException {
        //Convert the DNS message into a byte array for transmission
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        //Hashmap for domainLocations
        domainLocations = new HashMap<>();
        //Header, Question, Answer
        header.writeBytes(byteArrayOutputStream);
        //Answers should not be in a for loop!!!!
        //answers[0].writeBytes (myStream?, map)? Might cause nullptr, but should be fixed in the server.
        for (DNSQuestion question: questions){
            question.writeBytes(byteArrayOutputStream, domainLocations);
        }
        for(DNSRecord answer: answers){
            answer.writeBytes(byteArrayOutputStream, domainLocations);
        }
        for(DNSRecord authority: authorityRecords){
            authority.writeBytes(byteArrayOutputStream, domainLocations);
        }
        for(DNSRecord additional: additionalRecords){
            additional.writeBytes(byteArrayOutputStream, domainLocations);
        }
        return byteArrayOutputStream.toByteArray();
    }


    //Don't do anything to this yet.
    public static void writeDomainName(ByteArrayOutputStream outputStream, HashMap<String, Integer> domainLocations, String[] domainPieces) throws IOException {
        DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
        String domainName = joinDomainName(domainPieces);

        if(domainLocations.containsKey(domainName)){
            int location = domainLocations.get(domainName);
            dataOutputStream.writeShort(0xC000 | location);
        } else {
            int location = outputStream.size();
            domainLocations.put(domainName, location);

            for (String label : domainPieces){
                dataOutputStream.writeByte(label.length());
                dataOutputStream.writeBytes(label);
            }

            dataOutputStream.writeByte(0);
        }
    }

    public static String joinDomainName(String[] pieces) {
        //Join the pieces of the domain name with dots
        return String.join(".", pieces);
    }

    @Override
    public String toString() {
        return "DNSMessage{" +
                "\n  Questions=" + Arrays.toString(questions) +
                ",\n  Answers=" + Arrays.toString(answers) +
                " Authority Records=" + Arrays.toString(authorityRecords) +
                ", Additional Records=" + Arrays.toString(additionalRecords) +
                "\n}";
    }
}
