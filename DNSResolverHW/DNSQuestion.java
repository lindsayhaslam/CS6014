import javax.xml.crypto.Data;
import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;

public class DNSQuestion {
    //This class would represent a DNS question, which includes the domain name
    // being queried and the query type (like A, AAAA, MX, etc.). It would handle
    // converting the domain name and type into the appropriate format for DNS queries.
    /*  0  1  2  3  4  5  6  7  8  9  0  1  2  3  4  5
              +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
              |                                               |
              /                     QNAME                     /
            /                                               /
            +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
            |                     QTYPE                     |
            +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
            |                     QCLASS                    |
            +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+*/

    //Variables
    private String[] domainName;
    private int queryType;
    private int queryClass;
    DNSMessage message;

    static DNSQuestion decodeQuestion(InputStream input, DNSMessage message) throws IOException {
        DNSQuestion question = new DNSQuestion();
        DataInputStream dataInputStream = new DataInputStream(input);
        //Read domain, considering compression.
        question.domainName = DNSMessage.readDomainName(input);
        //Read the query type (2 bytes)
        question.queryType = dataInputStream.readShort();
        //Read the query class (2 bytes)
        question.queryClass = dataInputStream.readShort();

        return question;
    }

    void writeBytes(ByteArrayOutputStream output, HashMap<String, Integer> domainNameLocations) throws IOException {
        DNSMessage.writeDomainName(output, domainNameLocations, domainName);
        DataOutputStream dataOutputStream = new DataOutputStream(output);
        //Write QTYPE (2 bytes)
        dataOutputStream.writeShort(queryType);
        //Write QCLASS (2 bytes)
        dataOutputStream.writeShort(queryClass);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DNSQuestion question)) return false;
        return queryType == question.queryType && queryClass == question.queryClass && Arrays.equals(domainName, question.domainName) && Objects.equals(message, question.message);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(queryType, queryClass, message);
        result = 31 * result + Arrays.hashCode(domainName);
        return result;
    }
}