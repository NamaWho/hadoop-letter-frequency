package it.unipi.tonystark.combiner;

import it.unipi.tonystark.exception.KeyValueException;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

import static it.unipi.tonystark.Utils.readLetterCount;

public class LetterFrequency {
    public static class CountMapper extends Mapper<Object, Text, Text, LongWritable> {

        private final static LongWritable one = new LongWritable(1);
        @Override
        public void map(Object key, Text value, Context context) throws IOException, InterruptedException {

            Text letter = new Text();

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
           String path = context.getConfiguration().get("outputPath");
            try {
                letterCount = readLetterCount(context.getConfiguration(), path);

            } catch (KeyValueException e) {
                System.out.println("Error in reading the letter count from the file::"+path);
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

