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
    public static class CountMapper extends Mapper<Object, Text, Text, LongWritable> {

        private final static Text letterCoutKey = new Text(MapReduceApp.getLETTER_COUNT_KEY());

        //define the associative array used to perform in mapping combinig
        private static Map<String, Long> map;

        private Boolean normalize;
        @Override
        public void setup(Context context) {

            map = new HashMap<>();
            normalize = Boolean.parseBoolean(context.getConfiguration().get(MapReduceApp.getNORMALIZE_PARAM_NAME()));
        }
        @Override
        public void map(Object key, Text value, Context context) throws IOException, InterruptedException {

            //convert each character to a lower case
            String line = value.toString().toLowerCase();

            line = Utils.filterCharacters(line, normalize);

            for(Character c: line.toCharArray()) {
                //H{t} = H{t} + 1
                if(map.containsKey(letterCoutKey.toString())){
                    long currentValue = map.get(letterCoutKey.toString());
                    map.put(letterCoutKey.toString(), currentValue+1);
                }
                else
                    map.put(letterCoutKey.toString(), 1L);
            }
            /* casa -> total_letter_count: 4 */
        }
        @Override
        public void cleanup(Context context) throws IOException, InterruptedException {

            for (Map.Entry<String, Long> entry : map.entrySet()) {
                String key = entry.getKey();
                Long value = entry.getValue();
                context.write(new Text(key), new LongWritable(value));
            }
            /* total_letter_count: 4 */
        }
    }

    public static class CountReducer extends Reducer<Text, LongWritable, Text, LongWritable> {
        @Override
        public void reduce(Text key, Iterable<LongWritable> values, Context context) throws IOException, InterruptedException {

            long sum = 0;
            for (LongWritable val : values) {
                sum += val.get();
            }
            context.write(key, new LongWritable(sum));
        }
        /* total_letter_count: 4 */
    }
}
