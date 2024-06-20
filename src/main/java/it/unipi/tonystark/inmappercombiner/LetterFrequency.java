package it.unipi.tonystark.inmappercombiner;

import it.unipi.tonystark.MapReduceApp;
import it.unipi.tonystark.Utils;
import it.unipi.tonystark.exception.KeyValueException;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static it.unipi.tonystark.MapReduceApp.getLetterCount;

public class LetterFrequency {
    public static class CountMapper extends Mapper<Object, Text, Text, LongWritable> {

        //define the associative array used to perform in mapping combinig
        private static Map<String, Long> map;
        private Boolean multiLingual;
        @Override
        public void setup(Context context) {

            map = new HashMap<>();
            multiLingual = Boolean.parseBoolean(context.getConfiguration().get("multiLingual"));
        }
        @Override
        public void map(Object key, Text value, Context context) throws IOException, InterruptedException {

            //convert each character to a lower case
            String line = value.toString().toLowerCase();

            line = Utils.removeNonLetters(line, multiLingual);

            for(Character c: line.toCharArray()) {
                //H{t} = H{t} + 1
                if(map.containsKey(String.valueOf(c))){
                    long currentValue = map.get(String.valueOf(c));
                    map.put(String.valueOf(c), currentValue+1);
                }
                else
                    map.put(String.valueOf(c), 1L);
            }
        }
        @Override
        public void cleanup(Context context) throws IOException, InterruptedException {

            for (Map.Entry<String, Long> entry : map.entrySet()) {
                String key = entry.getKey();
                Long value = entry.getValue();
                context.write(new Text(key), new LongWritable(value));
            }
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
