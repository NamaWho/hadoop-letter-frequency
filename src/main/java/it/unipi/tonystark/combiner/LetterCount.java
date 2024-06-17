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

    private final static IntWritable one = new IntWritable(1);

    //NullWritable is a special type of Writable that is used to represent null key values.
    //It is used in cases where the key is not needed. In our case, we store the total count of letters in the file.
    //NullWritable.get() returns an instance of NullWritable and this instance is a singleton.
    private final static NullWritable nullKey = NullWritable.get();

    private static final Logger logger = LogManager.getLogger(LetterCount.class);

    public static class CountMapper extends Mapper<Object, Text, NullWritable, IntWritable> {

        @Override
        public void map(Object key, Text value, Context context) throws IOException, InterruptedException {

            logger.info("CountMapper.map() called");

            //convert each character to lower case
            String line = value.toString().toLowerCase();

            for (int i = 0; i < line.length(); i++) {
                char c = line.charAt(i);
                if (Character.isLetter(c)) {
                    context.write(nullKey, one);
                }
            }
        }
    }

    public static class CountReducer extends Reducer<NullWritable, IntWritable, NullWritable, LongWritable> {

        @Override
        public void reduce(NullWritable key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {

            logger.info("CountReducer.reduce() called");

            long sum = 0;
            for (IntWritable val : values) {
                sum += val.get();
            }
            context.write(nullKey, new LongWritable(sum));
        }
    }
}
