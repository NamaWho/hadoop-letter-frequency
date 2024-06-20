package it.unipi.tonystark.combiner;

import it.unipi.tonystark.MapReduceApp;
import it.unipi.tonystark.Utils;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

public class LetterCount {
    public static class CountMapper extends Mapper<Object, Text, Text, LongWritable> {

        private final static LongWritable one = new LongWritable(1);
        private final static Text letterCountKey = new Text(MapReduceApp.getLETTER_COUNT_KEY());
        private Boolean normalize;

        @Override
        public void setup(Context context) {
            normalize = Boolean.parseBoolean(context.getConfiguration().get(MapReduceApp.getNORMALIZE_PARAM_NAME()));
        }
        @Override
        public void map(Object key, Text value, Context context) throws IOException, InterruptedException {

            String line = Utils.filterCharacters(value.toString(), normalize);

            for(Character c: line.toCharArray()) {
                context.write(letterCountKey, one);
            }
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
    }
}
