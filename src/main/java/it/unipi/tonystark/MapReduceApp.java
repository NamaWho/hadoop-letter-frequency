package it.unipi.tonystark;

import it.unipi.tonystark.exception.KeyValueException;
import lombok.Getter;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MapReduceApp {

    @Getter
    private static final String LETTER_COUNT_KEY = "total_letter_count";

    @Getter
    private static final String COUNT_OUTPUT_PATH_PARAM_NAME = "countOutputPath";

    @Getter
    private static final String NORMALIZE_PARAM_NAME = "normalize";
    private static final int JOB_TYPE_INDEX = 0;
    private static final int NUM_REDUCERS_INDEX = 1;
    private static final int NORMALIZE_INDEX = 2;
    private static final int INPUT_PATH_INDEX = 3;
    private static final int MIN_ARGS_LENGTH = 6;
    public static void main(String[] args) throws IOException, InterruptedException, ClassNotFoundException {
        Configuration conf = new Configuration();
        String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();

        if (!checkParameters(otherArgs, conf)) {
            System.exit(1);
        }

        String packagePath = "it.unipi.tonystark" + "." + otherArgs[JOB_TYPE_INDEX];

        Job countLetterJob = configureCountLetterJob(conf, otherArgs, packagePath);

        if (!countLetterJob.waitForCompletion(true)) {
            System.err.println("Error in the count letter job");
            System.exit(1);
        }

        Job frequencyLetterJob = configureFrequencyLetterJob(conf, otherArgs, packagePath);

        System.exit(frequencyLetterJob.waitForCompletion(true) ? 0 : 1);
    }

    private static Job configureCountLetterJob(Configuration conf, String[] args, String packagePath) throws IOException, ClassNotFoundException {

        Job job = Job.getInstance(conf, "Letter Count Job");

        job.getConfiguration().set(NORMALIZE_PARAM_NAME, args[NORMALIZE_INDEX]);

        job.setJarByClass(Class.forName(packagePath + ".LetterCount"));

        job.setMapperClass((Class<Mapper>) Class.forName(packagePath + ".LetterCount$CountMapper"));

        if(args[JOB_TYPE_INDEX].equals("combiner")){
            job.setCombinerClass((Class<Reducer>) Class.forName(packagePath + ".LetterCount$CountReducer"));
        }

        job.setReducerClass((Class<Reducer>) Class.forName(packagePath + ".LetterCount$CountReducer"));

        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(LongWritable.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(LongWritable.class);

        for (int i = INPUT_PATH_INDEX; i < args.length - 2; i++) {
            FileInputFormat.addInputPath(job, new Path(args[i]));
        }

        FileOutputFormat.setOutputPath(job, new Path(args[args.length - 2]));

        return job;
    }

    private static Job configureFrequencyLetterJob(Configuration conf, String[] args, String packagePath) throws IOException, ClassNotFoundException {

        Job job = Job.getInstance(conf, "Letter Frequency Job");

        job.getConfiguration().set(NORMALIZE_PARAM_NAME, args[NORMALIZE_INDEX]);

        job.getConfiguration().set(COUNT_OUTPUT_PATH_PARAM_NAME, args[args.length - 2]);

        job.setJarByClass(Class.forName(packagePath + ".LetterFrequency"));

        job.setMapperClass((Class<Mapper>) Class.forName(packagePath + ".LetterFrequency$CountMapper"));

        if (args[JOB_TYPE_INDEX].equals("combiner")) {
            job.setCombinerClass((Class<Reducer>) Class.forName(packagePath + ".LetterFrequency$CountCombiner"));
        }

        job.setReducerClass((Class<Reducer>) Class.forName(packagePath + ".LetterFrequency$CountReducer"));


        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(LongWritable.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(DoubleWritable.class);

        for (int i = INPUT_PATH_INDEX; i < args.length - 2; i++) {
            FileInputFormat.addInputPath(job, new Path(args[i]));
        }

        FileOutputFormat.setOutputPath(job, new Path(args[args.length - 1]));

        setNumReducers(job, args[NUM_REDUCERS_INDEX]);

        return job;
    }
    private static void setNumReducers(Job job, String numReducers) {
        job.setNumReduceTasks(Integer.parseInt(numReducers));
    }
    public static long getLetterCount(Configuration conf, String path) throws IOException, KeyValueException {

        // Read the output of the first job
        FileSystem fs = FileSystem.get(conf);
        Path outputDirPath = new Path(path);

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
    public static boolean checkParameters(String[] args, Configuration conf) {
        if (args.length < MIN_ARGS_LENGTH) {
            System.err.println("Usage: MapReduceApp <type> <numReducers> <normalize> [<input>...] <output1> <output2>");
            return false;
        }

        if (!args[JOB_TYPE_INDEX].equals("combiner") && !args[JOB_TYPE_INDEX].equals("inmappercombiner")) {
            System.err.println("The first argument must be 'combiner' or 'inmappercombiner'");
            return false;
        }

        try {
            int numReducers = Integer.parseInt(args[NUM_REDUCERS_INDEX]);
            if (numReducers < 1) {
                System.err.println("The number of reducers must be a positive integer");
                return false;
            }
        } catch (NumberFormatException e) {
            System.err.println("The number of reducers must be a valid integer");
            return false;
        }

        if (!args[NORMALIZE_INDEX].equals("true") && !args[NORMALIZE_INDEX].equals("false")) {
            System.err.println("The 'normalize' argument must be 'true' or 'false'");
            return false;
        }

        // Check if the input paths exist in the hadoop file system
        FileSystem fs;
        try {
            fs = FileSystem.get(conf);
            for (int i = INPUT_PATH_INDEX; i < args.length - 2; i++) {
                Path inputPath = new Path(args[i]);
                if (!fs.exists(inputPath)) {
                    System.err.println("Input path " + inputPath + " does not exist");
                    return false;
                }
            }
        } catch (IOException e) {
            System.err.println("Error while getting the file system");
            return false;
        }

        return true;
    }
}
