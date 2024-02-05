import java.io.*;

public class DNSHeader {
    //This class would represent the DNS header and handle parsing and constructing
    //the header portion of DNS messages. The header includes fields like transaction
    //ID, flags, counts of questions, and answer/resource records.

    //Variables
    private short transactionID;
    private short flags;
    private int qr;
    private int opCode;
    private int aa;
    private int tc;
    private int rd;
    private int ra;
    private int z;
    private int rCode;
    private short questions;
    private short answerRRs;
    private short authorityRRs;
    private short additionalRRs;


    //Constructor
    public DNSHeader(){
        transactionID = 0;
        qr = 0;
        opCode = 0;
        aa = 0;
        tc = 0;
        rd = 0;
        ra = 0;
        z = 0;
    }

    //Getter methods for each field
    public int getTransactionID() {
        return transactionID;
    }
    public int getFlags() {
        return flags;
    }
    public int getQuestions() {
        return questions;
    }
    public int getAnswerRRs() {
        return answerRRs;
    }
    public int getAuthorityRRs() {
        return authorityRRs;
    }
    public int getAdditionalRRs() {
        return additionalRRs;
    }

    public int getQr() {
        return qr;
    }
    public int getOpCode() {
        return opCode;
    }
    public int getAa(){
        return aa;
    }
    public int getTc(){
        return tc;
    }
    public int getRd(){
        return rd;
    }
    public int getRa(){
        return ra;
    }
    public int getZ(){
        return z;
    }
    public int getrCode(){
        return rCode;
    }

    //Decode header from InputStream
    public static DNSHeader decodeHeader(InputStream input) throws IOException {
        DNSHeader header = new DNSHeader();
        DataInputStream dataInputStream = new DataInputStream(input);
        header.transactionID = dataInputStream.readShort();
        header.flags = dataInputStream.readShort();
        header.questions = dataInputStream.readShort();
        header.answerRRs =dataInputStream.readShort();
        header.authorityRRs = dataInputStream.readShort();
        header.additionalRRs = dataInputStream.readShort();

        // Extract fields from flags
        header.qr = (header.flags >>>15) & 0b1; //It is 0 when it's a query and a 1 when it is a response
        header.opCode= (header.flags >>>11) & 0b1111;
        header.aa = (header.flags >>>10) & 0b1;
        header.tc = (header.flags >>>9) & 0b1;
        header.rd = (header.flags >>>8) & 0b1;
        header.ra = (header.flags >>>7) & 0b1;
        header.z = (header.flags >>>6) & 0b111;
        header.rCode = (header.flags >>>3) & 0b1111;

        return header;
    }

    public static DNSHeader buildHeaderForResponse(DNSMessage request, DNSMessage response){
        DNSHeader response_header = request.getHeader();
        response_header.qr = 1;
        response_header.answerRRs = 1;
        return response_header;
    }

   public void writeBytes(OutputStream output) throws IOException {
       DataOutputStream outputStream = new DataOutputStream(output);
       outputStream.writeShort(transactionID);
       outputStream.writeShort(flags);
       outputStream.writeShort(questions);
       outputStream.writeShort(answerRRs);
       outputStream.writeShort(authorityRRs);
       outputStream.writeShort(additionalRRs);
    }

    @Override
    public String toString(){
        //Print out everything in a nice way.
        return "DNSHeader {" +
                "\n  transactionID=" + transactionID +
                ",\n  flags=" + flags +
                " (qr=" + qr +
                ", opCode=" + opCode +
                ", aa=" + aa +
                ", tc=" + tc +
                ", rd=" + rd +
                ", ra=" + ra +
                ", z=" + z +
                ", rCode=" + rCode + ")" +
                ",\n  questions=" + questions +
                ",\n  answerRRs=" + answerRRs +
                ",\n  authorityRRs=" + authorityRRs +
                ",\n  additionalRRs=" + additionalRRs +
                "\n}";
    }
}
