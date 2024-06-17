package it.unipi.tonystark.combiner;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class LetterCount {

    private static final Logger logger = LogManager.getLogger(LetterCount.class);
    private final static Text letterCoutKey = new Text("total_letter_count");

    public static class CountMapper extends Mapper<Object, Text, Text, IntWritable> {

        private final static IntWritable one = new IntWritable(1);
        @Override
        public void map(Object key, Text value, Context context) throws IOException, InterruptedException {

            logger.info("CountMapper.map() called");

            //convert each character to lower case
            String line = value.toString().toLowerCase();

            for (int i = 0; i < line.length(); i++) {
                char c = line.charAt(i);
                if (Character.isLetter(c)) {
                    context.write(letterCoutKey, one);
                }
            }
        }
    }

    public static class CountReducer extends Reducer<Text, IntWritable, Text, LongWritable> {
        @Override
        public void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {

            logger.info("CountReducer.reduce() called");

            long sum = 0;
            for (IntWritable val : values) {
                sum += val.get();
            }
            context.write(letterCoutKey, new LongWritable(sum));
        }
    }
}
