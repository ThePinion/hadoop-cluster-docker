import java.io.IOException;
import java.util.StringTokenizer;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.text.DecimalFormat;

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
    Path output = new Path(args[3]);

    long numDocuments = termFrequency(input, termFrequency);
    documentFrequency(termFrequency, documentFrequency);
    termFrequencyInverseDocumentFrequency(documentFrequency, output, numDocuments);
    System.exit(0);
  }

  public static enum Counters {
    DOCUMENTS,
  }

  public static long termFrequency(Path input, Path output) throws Exception {
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
    long numDocuments = job.getCounters().findCounter(TFIDF.Counters.DOCUMENTS).getValue();
    return numDocuments;
  }

  // DocumentId, [words] => (word#documentId, 1)
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
      context.getCounter(TFIDF.Counters.DOCUMENTS).increment(1);
      StringTokenizer itr = new StringTokenizer(value.toString());
      while (itr.hasMoreTokens()) {
        word.set(itr.nextToken() + "#" + filename); // Composite key (word#filename)
        context.write(word, one);
      }
    }
  }

  // (word#documentId, 1) => (word#documentId, count)
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

  // (word#documentId, count) => (documentId, word=count)
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

  // (documentId, word=count) => (word#documentId, count/documentSize)
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

  public static void termFrequencyInverseDocumentFrequency(
    Path input, Path output, long numDocuments
  ) throws Exception {
    Job job = Job.getInstance(
      new Configuration(), 
      "Term Frequency Inverse Document Frequency"
    );
    job.getConfiguration().setLong("custom.numDocuments", numDocuments);
    job.setJarByClass(TFIDF.class);
    job.setMapperClass(TFIDFMapper.class); // Replace with your TFIDFMapper class
    job.setReducerClass(TFIDFReducer.class); // Replace with your TFIDFReducer class
    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(Text.class);
    FileInputFormat.addInputPath(job, input); // Input from Stage 2
    FileOutputFormat.setOutputPath(job, output); // Final output

    if (!job.waitForCompletion(true)) {
      System.exit(1);
    }
  } 

  // (word#documentId, count/documentSize) => (word, documentId=count/documentSize)
  public static class TFIDFMapper extends Mapper<Object, Text, Text, Text> {

    private Text word = new Text();
    private Text tfidf = new Text();

    public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
      String[] idAndCounter = value.toString().split("\t");
      String[] wordAndDoc = idAndCounter[0].split("#");
      String w = wordAndDoc[0];
      String d = wordAndDoc[1];
      String c = idAndCounter[1];
      word.set(w);
      tfidf.set(d + "=" + c);
      context.write(word, tfidf);
    }
  }

  // (word, [documentId=count/documentSize]) => (word#documentId, [tfidf's])
  public static class TFIDFReducer extends Reducer<Text, Text, Text, Text> {
    private Text wordAndDoc = new Text();
    private Text counts = new Text();
    private static final DecimalFormat FORMAT = new DecimalFormat("###.########");
    private long numDocuments;

    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
        Configuration conf = context.getConfiguration();
        // Retrieve numDocuments from configuration with a default value of 0
        numDocuments = conf.getLong("custom.numDocuments", 0);
    }

    public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
      long numDocumentsWithKey = 0;
      // Map<documentId, "count/documentSize">
      Map<String, String> frequencies = new HashMap<String, String>();
      for(Text val: values) {
        String[] docAndCount = val.toString().split("=");
        String d = docAndCount[0];
        String cAndct = docAndCount[1];
        Integer c = Integer.parseInt(cAndct.split("/")[0]);
        if (c > 0) { numDocumentsWithKey++; }
        frequencies.put(d, cAndct);
      }
      for (Map.Entry<String, String> entry : frequencies.entrySet()) {
        String doc = entry.getKey();
        String value = entry.getValue();
        String[] cAndct = value.split("/");
        Integer c = Integer.parseInt(cAndct[0]);
        Integer ct = Integer.parseInt(cAndct[1]);
        Double tf = (double) c / (double) ct;
        Double docsWithKeyNormalized = (double)(numDocumentsWithKey == (long)0 ? (long)1 : numDocumentsWithKey);
        Double idf = Math.log10((double) numDocuments / docsWithKeyNormalized );
        Double tfidf = tf * idf;
        wordAndDoc.set(key + "#" + doc);
        counts.set("[" 
          + numDocumentsWithKey + "/" + numDocuments + ", " 
          + value + ", " 
          + FORMAT.format(tfidf)
        + "]");
        context.write(wordAndDoc, counts);
      }
    }
  }
}