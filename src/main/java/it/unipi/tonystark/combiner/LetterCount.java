package it.unipi.tonystark.combiner;

import it.unipi.tonystark.Utils;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

public class LetterCount {
    public static class CountMapper extends Mapper<Object, Text, Text, LongWritable> {

        private final static LongWritable one = new LongWritable(1);
        private final static Text letterCoutKey = new Text(Utils.getLETTER_COUNT_KEY());
        @Override
        public void map(Object key, Text value, Context context) throws IOException, InterruptedException {

            //convert each character to lower case
            String line = value.toString().toLowerCase();

            for (int i = 0; i < line.length(); i++) {
                char c = line.charAt(i);
                if (Character.isLetter(c)) {
                    context.write(letterCoutKey, one);
                }
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
