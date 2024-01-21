import java.io.IOException;
import java.util.StringTokenizer;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;

public class TFIDF {

  public static void main(String[] args) throws Exception {
    Path input = new Path(args[0]);
    Path termFrequency = new Path(args[1]);
    Path documentFrequency = new Path(args[2]);
    // Path output = new Path(args[3]);

    termFrequency(input, termFrequency);
    documentFrequency(termFrequency, documentFrequency);
    // termFrequencyInverseDocumentFrequency(documentFrequency, output);
    System.exit(0);
  }

  public static enum Counters {
    DOCUMENTS,
    TERMS
  }

  public static void termFrequency(Path input, Path output) throws Exception {
    Job job = Job.getInstance(new Configuration(), "Term Frequency");
    job.setJarByClass(TFIDF.class);
    job.setMapperClass(TFMapper.class); // Replace with your TFMapper class
    job.setCombinerClass(TFReducer.class); // Replace with your TFReducer class
    job.setReducerClass(TFReducer.class); // Replace with your TFReducer class
    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(IntWritable.class);
    FileInputFormat.addInputPath(job, input);
    FileOutputFormat.setOutputPath(job, output);

    if (!job.waitForCompletion(true)) {
      System.exit(1);
    }
  }

  public static class TFMapper extends Mapper<Object, Text, Text, IntWritable> {
    private final static IntWritable one = new IntWritable(1);
    private Text word = new Text();
    private String filename;

    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
      // Extracting filename from the input split
      FileSplit fileSplit = (FileSplit) context.getInputSplit();
      filename = fileSplit.getPath().getName();
    }

    public void map(
      Object key, Text value, Context context
    ) throws IOException, InterruptedException {
      StringTokenizer itr = new StringTokenizer(value.toString());
      while (itr.hasMoreTokens()) {
        word.set(itr.nextToken() + "#" + filename); // Composite key (word#filename)
        context.write(word, one);
      }
    }
  }

  public static class TFReducer extends Reducer<Text, IntWritable, Text, IntWritable> {
    private IntWritable result = new IntWritable();

    public void reduce(
      Text key, Iterable<IntWritable> values, Context context
    ) throws IOException, InterruptedException {
      int sum = 0;
      for (IntWritable val : values) {
        sum += val.get();
      }
      result.set(sum);
      context.write(key, result);
    }
  }

  public static void documentFrequency(Path input, Path output) throws Exception {
    Job job = Job.getInstance(new Configuration(), "Document Frequency");
    job.setJarByClass(TFIDF.class);
    job.setMapperClass(DFMapper.class); // Replace with your DFMapper class
    job.setReducerClass(DFReducer.class); // Replace with your DFReducer class
    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(Text.class);
    FileInputFormat.addInputPath(job, input); // Input from Stage 1
    FileOutputFormat.setOutputPath(job, output); // Intermediate output

    if (!job.waitForCompletion(true)) {
      System.exit(1);
    }
  }

  public static class DFMapper extends Mapper<Object, Text, Text, Text> {

    private Text docId = new Text();
    private Text wordAndCount = new Text();

    public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
        String[] idAndCounter = value.toString().split("\t");
        String[] wordAndDoc = idAndCounter[0].split("#");
        String d = wordAndDoc[1];
        String w = wordAndDoc[0];
        String c = idAndCounter[1];
        docId.set(d);
        wordAndCount.set(w + "=" + c);
        context.write(docId, wordAndCount);
        
    }
  }

  public static class DFReducer extends Reducer<Text, Text, Text, Text> {

    private Text wordAndDoc = new Text();
    private Text wordCountDivDocCount = new Text();

    public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
      int documentSize = 0;
      Map<String, Integer> counts = new HashMap<String, Integer>();
      for (Text val : values) {
        String[] wordAndCount = val.toString().split("=");
        String w = wordAndCount[0];
        int c = Integer.parseInt(wordAndCount[1]);  
        counts.put(w, c);
        documentSize += c;
      }
      for (String word : counts.keySet()) {
        wordAndDoc.set(word + "#" + key.toString());
        wordCountDivDocCount.set(counts.get(word) + "/" + documentSize);
        context.write(wordAndDoc, wordCountDivDocCount);
      }
    }
  }

  // public static class IntSumReducer
  //      extends Reducer<Text,IntWritable,Text,IntWritable> {
  //   private IntWritable result = new IntWritable();

  //   public void reduce(Text key, Iterable<IntWritable> values,
  //                      Context context
  //                      ) throws IOException, InterruptedException {
  //     int sum = 0;
  //     for (IntWritable val : values) {
  //       sum += val.get();
  //     }
  //     result.set(sum);
  //     context.write(key, result);
  //   }
  // }

  // public static void termFrequencyInverseDocumentFrequency(
  //   Path input, Path output
  // ) throws Exception {
  //   Job job = Job.getInstance(
  //     new Configuration(), 
  //     "Term Frequency Inverse Document Frequency"
  //   );
  //   job.setJarByClass(TFIDF.class);
  //   job.setMapperClass(TFIDFMapper.class); // Replace with your TFIDFMapper class
  //   job.setReducerClass(TFIDFReducer.class); // Replace with your TFIDFReducer class
  //   job.setOutputKeyClass(Text.class);
  //   job.setOutputValueClass(DoubleWritable.class);
  //   FileInputFormat.addInputPath(job, input); // Input from Stage 2
  //   FileOutputFormat.setOutputPath(job, output); // Final output

  //   if (!job.waitForCompletion(true)) {
  //     System.exit(1);
  //   }
  // } 
}