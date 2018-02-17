
/*
 * Filename: DnaQuery.java
 * Author: Saurabh A. Wani
 * This file compares the given query string with the dna sequences available for best match.
 */

import java.io.IOException;
import edu.rit.io.InStream;
import edu.rit.io.OutStream;
import edu.rit.pj2.Tuple;
import edu.rit.pj2.Vbl;
import edu.rit.pjmr.Combiner;
import edu.rit.pjmr.Customizer;
import edu.rit.pjmr.Mapper;
import edu.rit.pjmr.PjmrJob;
import edu.rit.pjmr.Reducer;
import edu.rit.pjmr.TextDirectorySource;
import edu.rit.pjmr.TextId;

//This is the main class which performs the job.
public class DnaQuery extends PjmrJob<TextId, String, String, solReduce> {

	// PJMR job main program.
	public void main(String[] args) throws Exception {
		// Checks if the input is valid.
		if (args.length != 4)
			usage();

		// Extracts the nodes to be used.
		String[] nodes = args[0].split(",");
		// Extracts the source directory.
		String file = args[1];
		// Variable to store the pattern to be compared.
		String pattern = null;
		// Extracting the pattern to be compared.
		double threshold1 = Double.parseDouble(args[3]);
		if (args.length >= 3) {
			pattern = args[2];
		}

		// Creating array of arguments for mapper.
		String arg[] = { pattern, String.valueOf(threshold1) };

		// Assigning number of threads to be used.
		int NT = Math.max(threads(), 1);

		// Configure mapper tasks.
		for (String node : nodes)
			mapperTask(node).source(new TextDirectorySource(file)).mapper(NT, MyMapper.class, arg);

		// Configure reducer tasks.
		reducerTask().customizer(MyCustomizer.class).reducer(MyReducer.class);

		// Starting the job.
		startJob();
	}

	// This class perform the mapping task.
	private static class MyMapper extends Mapper<TextId, String, String, solReduce> {

		// Pattern to be matched.
		private String pattern;
		// Id of the DNA sequence.
		public String sid;
		// DNA sequence
		public String seq;
		// Variable to store threshold obtained after every comparison.
		double threshold;
		//Variable containing threshold entered by the user.
		double oldthreshold;
	

		/**
		 *  Initializes the variables.
		 *  @param args 	String array containing the pattern and the threshold obtained as input.
		 *  @param combiner Object to add the score and its index in the combiner.
		 *  @return None
		 */
		public void start(String[] args, Combiner<String, solReduce> combiner) {
			sid = "";
			seq = "";
			threshold = 0;
			pattern = args[0];
			oldthreshold = Double.parseDouble(args[1]);
		
		}

		/**
		 * Processes the given DNA sequence and inserts the substring having acceptable threshold.
		 * @param seq 		DNA sequence to be checked.
		 * @param sid 		ID of the DNA sequence to be checked.
		 * @param pattern 	Pattern to be compared with.
		 * @param combiner 	Object to add the score and its index in the combiner.
		 * @return None
		 */		
		public void processDNA(String seq, String sid, String pattern, Combiner<String, solReduce> combiner) {
			//Length of the DNA sequence.
			int len1 = seq.length();
			// Length of the pattern to be compared.
			int len2 = pattern.length();

			// Comparing to the sequences.
			for (int j = 0; j < (len1 - len2) + 1; j++) {
				String temp = seq.substring(j, j + len2);
				// To store the comparison score
				int tempresult = 0;
				for (int k = 0; k < len2; k++) {
					//Finding out if the character matches.
					if (temp.charAt(k) == pattern.charAt(k)) {
						tempresult++;
					}
				}
				// Calculating threshold.
				threshold = (double) tempresult / (double) pattern.length();

				// Recording the data if threshold is greater than or
				// equal to given threshold.
				if (threshold >= oldthreshold) {
					DNAInfo in = new DNAInfo(tempresult, j, sid, threshold);
					combiner.add(sid, new solReduce(in));
				}

			}

		}

		/**
		 *  Maps the substring from the DNA sequence based on its match with the pattern.
		 *  @param id 		Id to uniquely identify each record.
		 *  @param contents Part from the file.
		 *  @param combiner Object to add the score and its index in the combiner.
		 *  @return 		None 
		 */
		public void map(TextId id, String contents, Combiner<String, solReduce> combiner) {
			int index = 0;
			// Check if the line contains the id of the DNA sequence.
			if (contents.contains(">")) {

				// If the seq is not empty then it processing is done on it. 
				if (seq != "") {
					// Processing the DNA sequence.
					processDNA(seq, sid, pattern, combiner);
				}

				// Extracting the DNA id.
				for (int i = 0; i < contents.length(); i++) {
					if (String.valueOf(contents.charAt(i)).equals(">")) {
						continue;
					} else if (String.valueOf(contents.charAt(i)).equals(" ")) {
						index = i;
						break;
					}
				}
				seq = "";
				sid = contents.substring(1, index);

			} else {
				// Concatenate the sequence.
				seq = seq + contents;
			}

		}

		/**
		 * Performing one last processing before completion of mapper task.
		 * @param  combiner Object to add the score and its index in the combiner.
		 * @return 			None
		 */	
		public void finish(Combiner<String, solReduce> combiner) {
			//Processing the last part of the sequence.
			processDNA(seq, sid, pattern, combiner);
		}

	}

	// Class to arrange the obtained results based on their scores.
	private static class MyCustomizer extends Customizer<String, solReduce> {
		/**
		 * Comparing to string matches based on their score.
		 * @return Solution greater than the other
		 */
		public boolean comesBefore(String k1, solReduce l1, String k2, solReduce l2) {
			if (l1.info.score > l2.info.score)
				return true;
			else if (l1.info.score < l2.info.score)
				return false;
			else
				return k1.compareTo(k2) < 0;
		}
	}

	// This class is used to display the final results.
	private static class MyReducer extends Reducer<String, solReduce> {
		
		/**
		 * Prints the final output.
		 * @param key 	ID of the DNA sequence. 
		 * @param score Score for that particular DNA sequence. 
		 * @return 		None
		 */
		public void reduce(String key, solReduce score) {
			System.out.println(score.info.score + "\t" + score.info.index + "\t" + key);
			System.out.flush();
		}
	}

	/**
	 * Handle invalid input.
	 * @return None 
	 */
	public void usage() {
		System.err.print("Input error!\n");
		terminate(1);
	}

}

// This claas does the reduction. 
class solReduce extends Tuple implements Vbl {
	// DNA information
	DNAInfo info;

	// Default constructor.
	public solReduce() {
		this.info = new DNAInfo();
	}

	/**
	 * Parametarized constructor.
	 * @param info Object to store DNA information.
	 * @return None
	 */
	public solReduce(DNAInfo info) {
		this.info = new DNAInfo();
		this.info.index = info.index;
		this.info.score = info.score;
		this.info.sid = info.sid;
		this.info.threshold = info.threshold;
	}

	/**
	 * Overrides the basic functionality of reduction.  
	 * @return None
	 */
	public void reduce(Vbl arg0) {

		if (this.info.score < ((solReduce) arg0).info.score) {
			this.info.index = ((solReduce) arg0).info.index;
			this.info.sid = ((solReduce) arg0).info.sid;
			this.info.score = ((solReduce) arg0).info.score;
			this.info.threshold = ((solReduce) arg0).info.threshold;
		} else if (this.info.score == ((solReduce) arg0).info.score) {
			if (this.info.index > ((solReduce) arg0).info.index) {
				this.info.index = ((solReduce) arg0).info.index;
				this.info.sid = ((solReduce) arg0).info.sid;
				this.info.score = ((solReduce) arg0).info.score;
				this.info.threshold = ((solReduce) arg0).info.threshold;
			}
		}
	}

	/**
	 * Set this shared variable to the given shared variable.
	 * @param Shared variable.
	 * @return None
	 */
	public void set(Vbl arg0) {
		this.info.index = ((solReduce) arg0).info.index;
		this.info.sid = ((solReduce) arg0).info.sid;
		this.info.score = ((solReduce) arg0).info.score;
		this.info.threshold = ((solReduce) arg0).info.threshold;
	}

	/**
	 * Create a clone of this shared variable.
	 * @return vbl Clone of the object. 
	 */
	public Object clone() {
		solReduce vbl = (solReduce) super.clone();
		if (this.info != null)
			vbl.info = this.info;
		return vbl;
	}

	/**
	 * Deserialize object to retrieve counter values.
	 * @param argo Object to be deserialized. 
	 * @return None
	 */
	public void readIn(InStream arg0) throws IOException {
		info = (DNAInfo) arg0.readObject();

	}

	/**
	 * Serialize object containing the counter values. 
	 * @param Object to be serialized. 
	 * @return None
	 */
	public void writeOut(OutStream arg0) throws IOException {

		arg0.writeObject(info);

	}
}
