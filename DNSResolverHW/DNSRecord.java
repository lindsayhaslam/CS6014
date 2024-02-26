import java.io.*;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

public class DNSRecord {
    //This class would represent DNS resource records, which are the answers in
    // DNS responses. It would parse resource record fields like the name, type,
    // class, TTL (time to live), and the actual data.
    /*                                   1  1  1  1  1  1
      0  1  2  3  4  5  6  7  8  9  0  1  2  3  4  5
    +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
    |                                               |
    /                                               /
    /                      NAME                     /
    |                                               |
    +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
    |                      TYPE                     |
    +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
    |                     CLASS                     |
    +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
    |                      TTL                      |
    |                                               |
    +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
    |                   RDLENGTH                    |
    +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--|
    /                     RDATA                     /
    /                                               /
    +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+*/
    private String[] name;
    private short type;
    private short recordClass;
    private int ttl;
    private short rdLength;
    private byte[] rData;
    public static Date creationDate;
    DNSMessage message_;

    public DNSRecord(){
    }

    static DNSRecord decodeRecord(InputStream input, DNSMessage message) throws IOException {
        DNSRecord record = new DNSRecord();
        DataInputStream dataInputStream = new DataInputStream(input);
        dataInputStream.mark(2);
        short firstTwoBytes = dataInputStream.readShort();


        if((firstTwoBytes & 0xC000) == 0xC000){
            int offset = firstTwoBytes & 0x3FFF;
            record.name = message.readDomainName(offset);
            System.out.println("Record name" + Arrays.toString(record.name));
        } else {
            dataInputStream.reset();
            record.name = message.readDomainName(dataInputStream);
        }


        //Read the type and class (both are 2 bytes)
        record.type = dataInputStream.readShort();
        record.recordClass = dataInputStream.readShort();

        //TTL (4 bytes)
        //All bits are set to 1, but is treated as a long because of L suffix
        record.ttl = dataInputStream.readInt();

        //RDLength (2 bytes)
        record.rdLength = dataInputStream.readShort();

        //rData
        record.rData = new byte[record.rdLength];
        dataInputStream.readFully(record.rData);
        record.creationDate = new Date();
        return record;
    }
    public void writeBytes(ByteArrayOutputStream stream, HashMap<String, Integer> domainNameLocations) throws IOException{
        DNSMessage.writeDomainName(stream, domainNameLocations, name);
        DataOutputStream dataOutputStream = new DataOutputStream(stream);
        //Type and class as 16-bit integers.
        dataOutputStream.writeShort(type);
        dataOutputStream.writeShort(recordClass);
        //Write the TTL as a 32-bit integer
        dataOutputStream.writeInt(ttl);
        //Write RDLENGTH as a 16-bit integer. Length of RDATA.
        dataOutputStream.writeShort(rData.length);
        //Write RDATA directly.
        dataOutputStream.write(rData);
    }

    public boolean isExpired(){
        Date now = new Date();
        long currentTimeMillis = now.getTime();
        long expirationTime = creationDate.getTime() + (ttl *1000);
        return currentTimeMillis > expirationTime;
    }

    @Override
    public String toString(){
        return "DNSRecord {" +
                "\n  Name=" + Arrays.toString(name) +
                ",\n  Type=" + type +
                ", Record Class=" + recordClass +
                ", Time to Live=" + ttl +
                ", rdLength =" + rdLength +
                ", rData=" + Arrays.toString(rData) +
                ", creationDate =" + creationDate +
                "\n}";
    }
}
