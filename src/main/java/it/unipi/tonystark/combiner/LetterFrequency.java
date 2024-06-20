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

        private Boolean normalized;
        @Override
        public void setup(Context context) {
            normalized = Boolean.parseBoolean(context.getConfiguration().get(MapReduceApp.getNORMALIZE_PARAM_NAME()));
        }
        @Override
        public void map(Object key, Text value, Context context) throws IOException, InterruptedException {

            Text letter = new Text();

            String line = Utils.filterCharacters(value.toString(), normalized);

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
            letterCount = context.getConfiguration().getLong(MapReduceApp.getLETTER_COUNT_VALUE_PARAM_NAME(), 1);
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

