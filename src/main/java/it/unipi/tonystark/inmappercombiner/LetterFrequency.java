package it.unipi.tonystark.inmappercombiner;

import it.unipi.tonystark.MapReduceApp;
import it.unipi.tonystark.Utils;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class LetterFrequency {

    public static class FreqMapper extends Mapper<Object, Text, Text, LongWritable> {

        // Associative array used to perform in mapping combining
        private static Map<Text, LongWritable> map;
        private Boolean normalize;

        /**
         * Setup method to get the normalize parameter from the configuration
         * @param context the context of the job
         */
        @Override
        public void setup(Context context) {

            map = new HashMap<>();
            normalize = Boolean.parseBoolean(context.getConfiguration().get(MapReduceApp.getNORMALIZE_PARAM_NAME()));
        }

        /**
         * Map method to count the number of letters in the input text
         * @param key the key of the input text
         * @param value the value of the input text
         * @param context the context of the job
         * @throws IOException
         * @throws InterruptedException
         */
        @Override
        public void map(Object key, Text value, Context context) throws IOException, InterruptedException {

            //convert each character to a lower case
            String line = value.toString().toLowerCase();

            line = Utils.filterCharacters(line, normalize);

            for(Character c: line.toCharArray()) {
                //H{t} = H{t} + 1
                if(map.containsKey(new Text(String.valueOf(c)))) {
                    map.put(new Text(String.valueOf(c)), new LongWritable(map.get(new Text(String.valueOf(c))).get() + 1));
                } else {
                    map.put(new Text(String.valueOf(c)), new LongWritable(1));
                }
            }
        }

        /**
         * Cleanup method to emit the key-value pairs
         * @param context the context of the job
         * @throws IOException
         * @throws InterruptedException
         */
        @Override
        public void cleanup(Context context) throws IOException, InterruptedException {

            for(Map.Entry<Text, LongWritable> entry: map.entrySet()) {
                context.write(entry.getKey(), entry.getValue());
            }
        }
    }

    public static class FreqReducer extends Reducer<Text, LongWritable, Text, DoubleWritable> {
        // Total number of letters
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
            for (LongWritable val : values) {
                sum += val.get();
            }
            double freq = (double) sum / (double) letterCount;

            context.write(key, new DoubleWritable(freq));
        }

    }
}
