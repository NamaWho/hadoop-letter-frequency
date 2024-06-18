package it.unipi.tonystark;

import org.apache.hadoop.conf.Configuration;
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

import java.io.IOException;
public class MapReduceApp {

    //private static final Logger logger = LogManager.getLogger(MapReduceApp.class);
    public static void main(String[] args) throws IOException, InterruptedException, ClassNotFoundException {
        Configuration conf = new Configuration();
        String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();

        if (otherArgs.length < 5) {
            //logger.error("Usage: MapReduceApp <type> <numReducers> [<input>...] <output1> <output2>");
            System.out.println("Usage: MapReduceApp <type> <numReducers> [<input>...] <output1> <output2>");
            System.exit(1);
        }

        if(!otherArgs[0].equals("combiner") && !otherArgs[0].equals("inmappercombiner")){
            //logger.error("The first argument must be 'combiner' or 'inmappercombiner'");
            System.out.println("The first argument must be 'combiner' or 'inmappercombiner'");
            System.exit(1);
        }

        String packagePath = "it.unipi.tonystark" + "." + otherArgs[0];

        Job countLetterJob = configureCountLetterJob(conf, otherArgs, packagePath);

        if (!countLetterJob.waitForCompletion(true)) {
            //logger.error("Error in the job to count the total number of letters");
            System.exit(1);
        }

        Job frequencyLetterJob = configureFrequencyLetterJob(conf, otherArgs, packagePath);

        System.exit(frequencyLetterJob.waitForCompletion(true) ? 0 : 1);
    }

    private static Job configureCountLetterJob(Configuration conf, String[] args, String packagePath) throws IOException, ClassNotFoundException {

        Job job = Job.getInstance(conf, "Letter Count Job");

        job.setJarByClass(Class.forName(packagePath + ".LetterCount"));

        job.setMapperClass((Class<Mapper>) Class.forName(packagePath + ".LetterCount$CountMapper"));

        if(args[0].equals("combiner")){
            job.setCombinerClass((Class<Reducer>) Class.forName(packagePath + ".LetterCount$CountReducer"));
        }

        job.setReducerClass((Class<Reducer>) Class.forName(packagePath + ".LetterCount$CountReducer"));

        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(LongWritable.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(LongWritable.class);

        for (int i = 2; i < args.length - 2; i++) {
            FileInputFormat.addInputPath(job, new Path(args[i]));
        }

        FileOutputFormat.setOutputPath(job, new Path(args[args.length - 2]));

        setNumReducers(job, args[1]);

        return job;
    }

    private static Job configureFrequencyLetterJob(Configuration conf, String[] args, String packagePath) throws IOException, ClassNotFoundException {

        Job job = Job.getInstance(conf, "Letter Frequency Job");

        job.getConfiguration().set("outputPath", args[args.length - 2]);

        job.setJarByClass(Class.forName(packagePath + ".LetterFrequency"));

        job.setMapperClass((Class<Mapper>) Class.forName(packagePath + ".LetterFrequency$CountMapper"));

        if (args[0].equals("combiner")) {
            job.setCombinerClass((Class<Reducer>) Class.forName(packagePath + ".LetterFrequency$CountCombiner"));
        }

        job.setReducerClass((Class<Reducer>) Class.forName(packagePath + ".LetterFrequency$CountReducer"));


        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(LongWritable.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(DoubleWritable.class);

        for (int i = 2; i < args.length - 2; i++) {
            FileInputFormat.addInputPath(job, new Path(args[i]));
        }

        FileOutputFormat.setOutputPath(job, new Path(args[args.length - 1]));

        setNumReducers(job, args[1]);

        return job;
    }
    private static void setNumReducers(Job job, String numReducers) {
        try {

            int numReducersInt = Integer.parseInt(numReducers);

            if (numReducersInt < 0) {
                //logger.error("The number of reducers must be greater than or equal to 0");
                System.exit(1);
            }

            job.setNumReduceTasks((numReducersInt == 0) ? 1 : numReducersInt);

        } catch (NumberFormatException e) {
            //logger.error("The number of reducers must be an integer");
            System.exit(1);
        }
    }
}
