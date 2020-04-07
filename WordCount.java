import java.io.IOException;
import java.util.StringTokenizer;
import java.util.HashMap;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;


public class WordCount {
  public static class WordCountMapper extends Mapper<Object, Text, Text, Text>{

    public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
      String line = value.toString(); 
      String fileName = ((FileSplit) context.getInputSplit()).getPath().getName();
      StringTokenizer tokenizer = new StringTokenizer(line);
     
      while (tokenizer.hasMoreTokens()) {
      	String current = tokenizer.nextToken();
       	context.write(new Text(current), new Text(fileName));
      }
    }
  }

  public static class WordCountReducer extends Reducer<Text,Text,Text,Text> {
  	public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
      int sum = 0;
      HashMap map = new HashMap();

      for (Text value : values) {
      	String term = value.toString();
        
        if (term != null && map.get(term) != null) {
        	sum = (int)(map.get(term));
        	map.put(term, ++sum);
        } else {
        	map.put(term, 1);
        }
      }

      context.write(key, new Text(map.toString()));
    }
  }

  public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
  	if (args.length != 2) {
  		System.err.println("Usage: Word Count <input path> <output path>");
  	}

  	Job job = new Job();
  	job.setJarByClass(WordCount.class);
  	job.setJobName("Word Count");

  	FileInputFormat.addInputPath(job, new Path(args[0]));
  	FileOutputFormat.setOutputPath(job, new Path(args[1]));

  	job.setMapperClass(WordCountMapper.class);
  	job.setReducerClass(WordCountReducer.class);

  	job.setOutputKeyClass(Text.class);
  	job.setOutputValueClass(Text.class);
  	job.waitForCompletion(true);
  }
}