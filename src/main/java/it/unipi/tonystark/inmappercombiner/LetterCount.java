package it.unipi.tonystark.inmappercombiner;

import it.unipi.tonystark.MapReduceApp;
import it.unipi.tonystark.Utils;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class LetterCount {
    /**
     * Mapper class to count the number of letters in the input text
     */
    public static class CountMapper extends Mapper<Object, Text, Text, LongWritable> {

        private final static Text letterCountKey = new Text(MapReduceApp.getLETTER_COUNT_KEY());

        // Associative array used to perform in mapping combinig
        private static Map<String, Long> map;
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

            // Convert each character to a lower case
            String line = value.toString().toLowerCase();

            line = Utils.filterCharacters(line, normalize);

            for(Character c: line.toCharArray()) {
                // H{t} = H{t} + 1
                if(map.containsKey(letterCountKey.toString())){
                    long currentValue = map.get(letterCountKey.toString());
                    map.put(letterCountKey.toString(), currentValue + 1);
                }
                else
                    map.put(letterCountKey.toString(), 1L);
            }
        }

        /**
         * Cleanup method to emit the final key-value pairs
         * @param context the context of the job
         * @throws IOException
         * @throws InterruptedException
         */
        @Override
        public void cleanup(Context context) throws IOException, InterruptedException {

            for (Map.Entry<String, Long> entry : map.entrySet()) {
                String key = entry.getKey();
                Long value = entry.getValue();
                context.write(new Text(key), new LongWritable(value));
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
            for (LongWritable val : values) {
                sum += val.get();
            }
            context.write(key, new LongWritable(sum));
        }
    }
}
