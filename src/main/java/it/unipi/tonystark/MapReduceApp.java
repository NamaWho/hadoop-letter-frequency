package it.unipi.tonystark;

import it.unipi.tonystark.combiner.LetterCount;
import it.unipi.tonystark.combiner.LetterFrequency;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
public class MapReduceApp {

    private static final Logger logger = LogManager.getLogger(MapReduceApp.class);
    private static final int DEFAULT_NUM_REDUCERS = 3;
    public static void main(String[] args) throws IOException, InterruptedException, ClassNotFoundException {
        Configuration conf = new Configuration();
        String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();

        if (otherArgs.length < 3) {
            logger.error("Usage: MapReduceApp < <input>... <output1> <output2>");
            System.exit(1);
        }

        //Start the job to count the total number of letters
        logger.info("Starting the job to count the total number of letters");

        Job countLetterJob = Job.getInstance(conf, "Letter Count Job");

        //Set the jar by class
        countLetterJob.setJarByClass(LetterCount.class);

        countLetterJob.setMapperClass(LetterCount.CountMapper.class);

        countLetterJob.setCombinerClass(LetterCount.CountReducer.class);
        countLetterJob.setReducerClass(LetterCount.CountReducer.class);

        countLetterJob.setOutputKeyClass(org.apache.hadoop.io.Text.class);
        countLetterJob.setOutputValueClass(org.apache.hadoop.io.LongWritable.class);

        //Set the input paths for the first job
        for (int i = 0; i < otherArgs.length - 2; i++) {
            FileInputFormat.addInputPath(countLetterJob, new Path(otherArgs[i]));
        }

        //Set the output path for the first job
        FileOutputFormat.setOutputPath(countLetterJob, new Path(otherArgs[otherArgs.length - 2]));

        setNumReducers(countLetterJob, conf);

        // wait for the completion of the first job
        if(!countLetterJob.waitForCompletion(true)){
            logger.error("Error in the job to count the total number of letters");
            System.exit(1);
        }

        // Start the job to calculate the frequency of each letter
        Job frequencyLetterJob = Job.getInstance(conf, "Letter Frequency Job");

        //Use the context to pass the name of the path of the output file of the first job
        frequencyLetterJob.getConfiguration().set("outputPath", otherArgs[otherArgs.length - 2]);

        //Set the jar by class
        frequencyLetterJob.setJarByClass(LetterFrequency.class);

        frequencyLetterJob.setMapperClass(LetterFrequency.CountMapper.class);
        frequencyLetterJob.setCombinerClass(LetterFrequency.CountReducer.class);
        frequencyLetterJob.setReducerClass(LetterFrequency.CountReducer.class);

        setNumReducers(frequencyLetterJob, conf);

        countLetterJob.setOutputKeyClass(org.apache.hadoop.io.Text.class);
        countLetterJob.setOutputValueClass(org.apache.hadoop.io.LongWritable.class);

        //Set the input path for the second job
        for (int i = 0; i < otherArgs.length - 2; i++) {
            FileInputFormat.addInputPath(frequencyLetterJob, new Path(otherArgs[i]));
        }

        //Set the output path for the second job
        FileOutputFormat.setOutputPath(frequencyLetterJob, new Path(otherArgs[otherArgs.length - 1]));

        // wait for the completion of the second job
        System.exit(frequencyLetterJob.waitForCompletion(true) ? 0 : 1);


    }

    private static void setNumReducers(Job job, Configuration conf) {
        //check if the number of reducers is set in the configuration
        int numReducers = conf.getInt("mapreduce.job.reduces", DEFAULT_NUM_REDUCERS);
        job.setNumReduceTasks(numReducers);
    }
}
