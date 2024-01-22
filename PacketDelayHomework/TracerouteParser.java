import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TracerouteParser {
    public static void main(String[] args) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader("second.txt"));
            BufferedWriter writer = new BufferedWriter(new FileWriter("output2.txt"));

            String line;
            int currentHop = -1;
            String currentIp = "";
            double totalDelay = 0;
            int delayCount = 0;
            Pattern hopPattern = Pattern.compile("^\\s*(\\d+)\\s+.*?\\(([^)]+)\\)");
            Pattern delayPattern = Pattern.compile("([0-9]+\\.[0-9]+) ms");

            while ((line = reader.readLine()) != null) {
                Matcher hopMatcher = hopPattern.matcher(line);
                Matcher delayMatcher = delayPattern.matcher(line);

                if (hopMatcher.find()) {
                    int hopNumber = Integer.parseInt(hopMatcher.group(1));

                    if (hopNumber != currentHop) {
                        if (currentHop != -1) {
                            double averageDelay = delayCount > 0 ? totalDelay / delayCount : 0;
                            writer.write("Hop " + currentHop + " (" + currentIp + "): Average Delay = " + averageDelay + " ms\n");
                        }

                        currentHop = hopNumber;
                        currentIp = hopMatcher.group(2);
                        totalDelay = 0;
                        delayCount = 0;
                    }
                }
                while (delayMatcher.find()) {
                    totalDelay += Double.parseDouble(delayMatcher.group(1));
                    delayCount++;
                }
            }

            // Handle the last hop
            if (currentHop != -1) {
                double averageDelay = delayCount > 0 ? totalDelay / delayCount : 0;
                writer.write("Hop " + currentHop + " (" + currentIp + "): Average Delay = " + averageDelay + " ms\n");
            }

            reader.close();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

