/*
 * Filename: DNAInfo.java
 * Author: Saurabh A. Wani
 * This class contains information of the DNA.
 */
import java.io.Serializable;

public class DNAInfo implements Serializable{
	//Contains the score for a given match.
	int score;
	//Contains the index.
	int index;
	//Contains the id for a given DNA sequence. 
	String sid;
	//Threshold
	double threshold;
	private static final long serialVersionUID = 1L;
	//Default constructor. 
	public DNAInfo() {}

	/**
	 * Parameterized constructor. 
	 * @param score		Score for a given match.
	 * @param index 	Index at which the score was obtained. 
	 * @param sid		ID of the DNA sequence. 
	 * @param threshold threshold of the given match.
	 * @return 			None
	 */
	public DNAInfo(int score, int index, String sid, double threshold){
		this.score = score;
		this.index = index;
		this.sid = sid;
		this.threshold = threshold;
	}
}
