package it.unipi.tonystark.combiner;

import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

import static it.unipi.tonystark.Utils.readLetterCount;

public class LetterFrequency {
    private static final Logger logger = LogManager.getLogger(LetterFrequency.class);

    public static class CountMapper extends Mapper<Object, Text, Text, IntWritable> {

        private final static IntWritable one = new IntWritable(1);
        private final static Text letter = new Text();
        @Override
        public void map(Object key, Text value, Context context) throws IOException, InterruptedException {

            logger.info("CountMapper.map() called");

            //convert each character to lower case
            String line = value.toString().toLowerCase();

            for (int i = 0; i < line.length(); i++) {
                char c = line.charAt(i);
                if (Character.isLetter(c)) {
                    letter.set(String.valueOf(c));
                    context.write(letter, one);
                }
            }
        }
    }

    public static class CountReducer extends Reducer<Text, IntWritable, Text, DoubleWritable> {

        private final static DoubleWritable result = new DoubleWritable(0);
        private long letterCount;
        public void setup(Context context) throws IOException {
           String path = context.getConfiguration().get("outputPath");
           letterCount = readLetterCount(context.getConfiguration(), path);
        }

        @Override
        public void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {

            logger.info("CountReducer.reduce() called");

            long sum = 0;
            for (IntWritable val : values) {
                sum += val.get();
            }
            double freq = (double) sum / (double) letterCount;
            result.set(freq);
            context.write(key, result);
        }
    }
}

