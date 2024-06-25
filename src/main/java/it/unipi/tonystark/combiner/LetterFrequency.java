package it.unipi.tonystark.combiner;

import it.unipi.tonystark.MapReduceApp;
import it.unipi.tonystark.Utils;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

public class LetterFrequency {

    public static class FreqMapper extends Mapper<Object, Text, Text, LongWritable> {

        private static LongWritable one;
        private Boolean normalized;

        /**
         * Setup method to get the normalize parameter from the configuration
         * @param context the context of the job
         */
        @Override
        public void setup(Context context) {
            normalized = Boolean.parseBoolean(context.getConfiguration().get(MapReduceApp.getNORMALIZE_PARAM_NAME()));

            one = new LongWritable(1);
        
        }

        /**
         * Map method to count the number of letters in the input text
         * @param key the key of the input
         * @param value the value of the input
         * @param context the context of the job
         * @throws IOException
         * @throws InterruptedException
         */
        @Override
        public void map(Object key, Text value, Context context) throws IOException, InterruptedException {

            Text letter = new Text();
            String line = Utils.filterCharacters(value.toString(), normalized);

            // For each character in the input text, emit a key-value pair
            for(Character c: line.toCharArray()) {
                letter.set(String.valueOf(c));
                context.write(letter, one);
            }
        }
    }

    public static class FreqCombiner extends Reducer<Text, LongWritable, Text, LongWritable>
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

    public static class FreqReducer extends Reducer<Text, LongWritable, Text, DoubleWritable> {
        private long letterCount;

        /**
         * Setup method to get the letter count from the configuration
         * @param context the context of the job
         * @throws IOException
         */
        public void setup(Context context) throws IOException {
            letterCount = context.getConfiguration().getLong(MapReduceApp.getLETTER_COUNT_VALUE_PARAM_NAME(), 1);
        }

        /**
         * Reduce method to calculate the frequency of each letter
         * @param key the key of the input
         * @param values the values of the input
         * @param context the context of the job
         * @throws IOException
         * @throws InterruptedException
         */
        @Override
        public void reduce(Text key, Iterable<LongWritable> values, Context context) throws IOException, InterruptedException {

            long sum = 0;

            // Sum the values of the input
            for (LongWritable val : values) {
                sum += val.get();
            }

            // Calculate the frequency of the letter
            double freq = (double) sum / (double) letterCount;

            // Emit the key-value pair
            context.write(key, new DoubleWritable(freq));
        }

    }
}

