package it.unipi.tonystark;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.FSDataInputStream;
import java.io.BufferedReader;

import java.io.IOException;
import java.io.InputStreamReader;

public class Utils {
    public static long readLetterCount(Configuration conf, String path) throws IOException {

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
                String firstLine = bufferedReader.readLine();

                if (firstLine != null) {
                    long textLength = Long.parseLong(firstLine);
                    totalTextLength += textLength;
                }

                // Close the input stream
                bufferedReader.close();
                inputStream.close();
            }
        }

        // Return the total text length
        return totalTextLength;
    }
}
