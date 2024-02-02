import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class DNSCache {
    //This class would manage a local cache, likely using a hash table, to store DNS responses.
    //The key could be the domain name and query type, and the value would be the corresponding DNS record.
    //This class would handle cache lookups, insertions, and potentially expiration based on TTL.
//    private final HashMap<DNSQuestion, DNSRecord> cache;

    //Constructor
//    public DNSCache(){
//        this.cache = new HashMap<>();
//    }
//
//    //Handle inserting the record
//    public void insertRecord(DNSQuestion question, DNSRecord answer){
//        DNSRecord.creationDate = new Date();
//        cache.put(question, answer);
//    }
//    public synchronized DNSRecord queryRecord (DNSQuestion question){
//        checkAndCleanExpiredRecords();
//        //Return the record if it exists, null otherwise
//        return cache.getOrDefault(question, null);
//    }
//    //
//    private void checkAndCleanExpiredRecords() {
//        Iterator<Map.Entry<DNSQuestion, DNSRecord>> iterator = cache.entrySet().iterator();
//        while (iterator.hasNext()) {
//            Map.Entry<DNSQuestion, DNSRecord> entry = iterator.next();
//            DNSRecord record = entry.getValue();
//            if (record.isExpired()) {
//                iterator.remove();
//            }
//        }
//    }
    private static HashMap<DNSQuestion, DNSRecord> cache = new HashMap<>();

    // Private constructor to prevent instantiation
    public DNSCache() {
        cache = new HashMap<>();
    }

    public static DNSRecord query(DNSQuestion question) {
        // check if the cache contains the question
        if (cache.containsKey(question)) {
            // if it does, check if the record is expired
            DNSRecord record = cache.get(question);
            // if the record is expired, remove it from the cache and return null
            if (record.isExpired()) {
                cache.remove(question);
                return null;
            }
            // if the record is not expired, return the record
            else {
                return record;
            }
        }
        else {
            // if the cache does not contain the question, return null
            return null;
        }
    }

    public static void insert(DNSQuestion question, DNSRecord record) {
        // only insert if the question is not already in the cache
        if (!cache.containsKey(question)) {
            cache.put(question, record);
        }
    }
}
