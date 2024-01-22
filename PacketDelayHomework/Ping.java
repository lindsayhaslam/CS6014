import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
public class Ping {
    public static void main(String[] args) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader("pingresults.txt"));
            BufferedWriter writer = new BufferedWriter(new FileWriter("pingoutput.txt"));
            String line;
            double totalDelay = 0;
            double minDelay = Double.MAX_VALUE;
            int delayCount = 0;
            Pattern rtDelayPattern = Pattern.compile("time=([0-9]+\\.[0-9]+) ms");

            while ((line = reader.readLine()) != null) {
                Matcher delayMatcher = rtDelayPattern.matcher(line);
                if (delayMatcher.find()){
                    double delay = Double.parseDouble(delayMatcher.group(1));
                    totalDelay += delay;
                    delayCount++;
                    if (delay < minDelay){
                        //Update minDelay
                        minDelay = delay;
                    }
                }

            }
            System.out.println(minDelay);
            double averageQueuingDelay = (totalDelay - (minDelay * delayCount))/delayCount;
            writer.write("The average queuing delay is: " +averageQueuingDelay);
            reader.close();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
