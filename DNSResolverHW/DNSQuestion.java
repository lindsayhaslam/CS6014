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
    private  String[] domainName;
    private int queryType;
    private int queryClass;
    //Possibly will need getter functions

    static DNSQuestion decodeQuestion(InputStream input, DNSMessage message) throws IOException {
        DataInputStream dataInputStream = new DataInputStream(input);
        DNSQuestion question = new DNSQuestion();
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
    public String toString() {
        return super.toString();
    }

    //This will be used by hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o){
            return true;
        }
     if (this.getClass() != o.getClass()){
         return false;
     }
     DNSQuestion question = (DNSQuestion) o;
     if (this.queryClass != question.queryClass){
         return false;
     }
     if (this.queryType != question.queryType){
         return false;
     }
        return Arrays.equals(this.domainName, question.domainName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(Arrays.hashCode(domainName), queryType, queryClass);
    }
}

