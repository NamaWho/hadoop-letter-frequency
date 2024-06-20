package it.unipi.tonystark.combiner;

import it.unipi.tonystark.MapReduceApp;
import it.unipi.tonystark.Utils;
import it.unipi.tonystark.exception.KeyValueException;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

import static it.unipi.tonystark.MapReduceApp.getLetterCount;


public class LetterFrequency {
    public static class CountMapper extends Mapper<Object, Text, Text, LongWritable> {

        private final static LongWritable one = new LongWritable(1);

        private Boolean multiLingual;
        @Override
        public void setup(Context context) {
            multiLingual = Boolean.parseBoolean(context.getConfiguration().get("multiLingual"));
        }
        @Override
        public void map(Object key, Text value, Context context) throws IOException, InterruptedException {

            Text letter = new Text();

            //convert each character to a lower case
            String line = value.toString().toLowerCase();

            line = Utils.removeNonLetters(line, multiLingual);

            for(Character c: line.toCharArray()) {
                letter.set(String.valueOf(c));
                context.write(letter, one);
            }
        }
    }

    public static class CountCombiner extends Reducer<Text, LongWritable, Text, LongWritable>
    {
        @Override
        public void reduce(Text key, Iterable<LongWritable> values, Context context) throws IOException, InterruptedException
        {
            long sum = 0;
            for (LongWritable val : values) {
                sum += val.get();
            }
            context.write(key, new LongWritable(sum));
        }
    }

    public static class CountReducer extends Reducer<Text, LongWritable, Text, DoubleWritable> {
        private long letterCount;
        public void setup(Context context) throws IOException {
           String path = context.getConfiguration().get(MapReduceApp.getCOUNT_OUTPUT_PATH_PARAM_NAME());
            try {
                letterCount = getLetterCount(context.getConfiguration(), path);

            } catch (KeyValueException e) {
                System.err.println("Error in reading the letter count from the file::"+path);
                throw new RuntimeException(e);
            }
        }

        @Override
        public void reduce(Text key, Iterable<LongWritable> values, Context context) throws IOException, InterruptedException {

            long sum = 0;
            for (LongWritable val : values) {
                sum += val.get();
            }
            double freq = (double) sum / (double) letterCount;

            context.write(key, new DoubleWritable(freq));
        }

    }
}

