package it.unipi.tonystark.combiner;

import it.unipi.tonystark.Utils;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class LetterCount {

    private static final Logger logger = LogManager.getLogger(LetterCount.class);

    public static class CountMapper extends Mapper<Object, Text, Text, LongWritable> {

        private final static LongWritable one = new LongWritable(1);
        private final static Text letterCoutKey = new Text(Utils.getLETTER_COUNT_KEY());
        @Override
        public void map(Object key, Text value, Context context) throws IOException, InterruptedException {

            logger.info("combiner::CountMapper1.map() called");

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

    public static class CountReducer extends Reducer<Text, LongWritable, Text, LongWritable> {
        @Override
        public void reduce(Text key, Iterable<LongWritable> values, Context context) throws IOException, InterruptedException {

            logger.info("combiner::CountReducer1.reduce() called");

            long sum = 0;
            for (LongWritable val : values) {
                sum += val.get();
            }
            context.write(key, new LongWritable(sum));
        }
    }
}
