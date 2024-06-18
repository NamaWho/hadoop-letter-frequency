package it.unipi.tonystark;

import it.unipi.tonystark.exception.KeyValueException;
import lombok.Getter;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
    @Getter
    private static final String LETTER_COUNT_KEY = "total_letter_count";
    public static long readLetterCount(Configuration conf, String path) throws IOException, KeyValueException {

        // Read the output of the first job
        FileSystem fs = FileSystem.get(conf);
        Path outputDirPath = new org.apache.hadoop.fs.Path(path);

        // Initialize the total text length
        long totalTextLength = 0;

        // Get a list of all files in the output directory
        FileStatus[] status = fs.listStatus(outputDirPath);

        for (FileStatus fileStatus : status) {
            String fileName = fileStatus.getPath().getName();

            // Ignore the _SUCCESS file
            if (!fileName.equals("_SUCCESS")) {
                // Open the file
                FSDataInputStream inputStream = fs.open(fileStatus.getPath());
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                // The result is on the first line of the output
                String line = bufferedReader.readLine();

                if (line != null) {

                    // Parse the key-value pair
                    Map<String, Long> keyValue = getKeyValue(line);

                    totalTextLength += keyValue.get(LETTER_COUNT_KEY);
                }

                // Close the input stream
                bufferedReader.close();
                inputStream.close();
            }
        }

        // Return the total text length
        return totalTextLength;
    }

    private static Map<String, Long> getKeyValue(String line) throws KeyValueException {

        String patternString = "^\\s*(\\w+)\\s+(\\d+)\\s*$";

        Pattern pattern = Pattern.compile(patternString);
        Matcher matcher = pattern.matcher(line);

        if (!matcher.matches()) {
            throw new KeyValueException("Invalid key-value pair: " + line);
        }

        String key = matcher.group(1);

        if (!key.equals(LETTER_COUNT_KEY)) {
            throw new KeyValueException("Invalid key: " + key);
        }

        Long value = Long.parseLong(matcher.group(2));

        Map<String, Long> map = new HashMap<>();
        map.put(key, value);

        return map;
    }

}
