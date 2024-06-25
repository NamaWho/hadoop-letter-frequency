package it.unipi.tonystark.combiner;

import it.unipi.tonystark.MapReduceApp;
import it.unipi.tonystark.Utils;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

public class LetterCount {

    /**
     * Mapper class to count the number of letters in the input text
     */
    public static class CountMapper extends Mapper<Object, Text, Text, LongWritable> {
        private static LongWritable one;
        private static Text letterCountKey;
        private Boolean normalize;

        /**
         * Setup method to get the normalize parameter from the configuration
         * @param context the context of the job
         */
        @Override
        public void setup(Context context) {
            normalize = Boolean.parseBoolean(context.getConfiguration().get(MapReduceApp.getNORMALIZE_PARAM_NAME()));

            one = new LongWritable(1);
            letterCountKey = new Text(MapReduceApp.getLETTER_COUNT_KEY());
        
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

            // Filter the characters from the input text
            String line = Utils.filterCharacters(value.toString(), normalize);

            // For each character in the input text, emit a key-value pair
            for(Character c: line.toCharArray()) {
                context.write(letterCountKey, one);
            }
        }
    }

    /**
     * Reducer class to count the number of letters in the input text
     */
    public static class CountReducer extends Reducer<Text, LongWritable, Text, LongWritable> {
        
        /**
         * Reduce method to count the number of letters in the input text
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

            // Emit the key-value pair
            context.write(key, new LongWritable(sum));
        }
    }
}
