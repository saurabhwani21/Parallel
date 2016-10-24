
/**
 * filename: ClusterCoeff.java
 * This class calculates the number of triplets, triangles and Cluster Coefficient in a given graph.
 * Author: Saurabh A. Wani
 */

import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import edu.rit.io.InStream;
import edu.rit.io.OutStream;
import edu.rit.pj2.Job;
import edu.rit.pj2.LongLoop;
import edu.rit.pj2.Loop;
import edu.rit.pj2.Task;
import edu.rit.pj2.Tuple;
import edu.rit.pj2.Vbl;
import edu.rit.pj2.tuple.ObjectArrayTuple;
import edu.rit.pj2.vbl.LongVbl;
import edu.rit.util.BitSet;
import edu.rit.util.Instance;

public class ClusterCoeff extends Job {

	/*
	 * Main class
	 */
	public void main(String args[]) throws Exception {

		// Validating command line argument
		if (args.length != 1)
			usage();

		// Creating graph object to get the number of vertices in the graph.
		Graph g = (Graph) Instance.newInstance(args[0]);

		// Number of vertices in the graph.
		int V = g.V();

		// Initializing master-worker loop
		masterSchedule(leapfrog);
		masterFor(0, V - 1, WorkerTask.class).args(args[0]);

		// Reduction task to get the final result
		rule().atFinish().task(ReduceTask.class).args("" + V).runInJobProcess();
	}

	/**
	 * Print a usage message and exit.
	 * Return: None
	 */
	public static void usage() {
		System.err.println("Usage: Invalid argument");
		System.exit(1);
	}

	/**
	 * WorkerTask class This class is used by every node to generate graph and
	 * calculate the number of triplets and triangles in the graph generated.
	 */
	private static class WorkerTask extends Task {
		// Number of vertices in the graph
		int V;

		// Variables to maintain count of triplets and triangles
		LongVbl tripletCount;
		LongVbl triangleCount;

		// Adjacency matrix to represent the graph.
		// 'adjacent[i]' is the set of vertices adjacent to vertex i.
		BitSet[] adjacent;

		// Declaring Graph object
		Graph g;
		// Declaring variable to store number of edges in the graph.
		int E;

		/*
		 *  Main function which does all the computation part.
		 *  Return: None
		 */
		public void main(String args[]) throws Exception {

			// Initializing the graph object
			g = (Graph) Instance.newInstance(args[0]);
			// Number of vertices in the graph
			V = g.V();
			// Number of edges in the graph
			E = g.E();
			adjacent  = new BitSet[V];
			Edge edge = new Edge();

			// Initializing the adjacency matrix
			for (int j = 0; j < V; ++j) {
				adjacent[j] = new BitSet(V);
			}

			// Generating adjacency matrix
			for (int i = 0; i < E; ++i) {
				g.nextEdge(edge);
				adjacent[edge.v1].add(edge.v2);
				adjacent[edge.v2].add(edge.v1);
			}

			// Initializing the counters
			tripletCount  = new LongVbl.Sum(0);
			triangleCount = new LongVbl.Sum(0);

			// 'For loop' for the workers.
			workerFor().schedule(leapfrog).exec(new Loop() {

				// Thread local counters for triplets and triangles.
				LongVbl thrtripletCount;
				LongVbl thrtriangleCount;

				public void start() {
					thrtripletCount  = threadLocal(tripletCount);
					thrtriangleCount = threadLocal(triangleCount);

				}

				public void run(int i) throws Exception {

					for (int j = 0; j < V; j++) {

						if (adjacent[i].contains(j) && i != j) {

							for (int k = i + 1; k < V; k++) {

								// Calculating number of triplets
								if (adjacent[j].contains(k) && k != i && k != j) {
									thrtripletCount.item++;

									// Calculating number of triangles
									if (adjacent[i].contains(k)) {
										thrtriangleCount.item++;
									}
								}
							}
						}
					}

				}

			});

			// Inserting counter values tuple in tuple space
			putTuple(new ResultTuple(tripletCount.item, triangleCount.item));

		}

	}

	/*
	 * Tuple class This class is used for creating tuples to store the counter
	 * values.
	 */
	private static class ResultTuple extends Tuple implements Vbl {

		// Variables to store counter values
		public long tripletCt;
		public long triangleCt;

		/*
		 *  Default constructor
		 */
		public ResultTuple() {
		}

		/**
		 * Parameterized constructor
		 * 
		 * @param tripletCount  Number of Triplets
		 * @param triangleCount Number of Triangles
		 */
		public ResultTuple(long tripletCount, long triangleCount) {

			this.tripletCt = tripletCount;
			this.triangleCt = triangleCount;

		}

		/**
		 * Deserialize object to retrieve counter values.
		 * Return: None
		 */
		public void readIn(InStream in) throws IOException {

			tripletCt  = in.readLong();
			triangleCt = in.readLong();
		}

		/**
		 * Serialize object containing the counter values.
		 * Return: None
		 */
		public void writeOut(OutStream out) throws IOException {

			out.writeLong(tripletCt);
			out.writeLong(triangleCt);

		}

		/**
		 * Overrides the basic functionality of reduction. Finds the global
		 * values of numbers of triplets and triangles.
		 * Return: None
		 */
		public void reduce(Vbl vbl) {
			this.tripletCt  = this.tripletCt  + ((ResultTuple) vbl).tripletCt;
			this.triangleCt = this.triangleCt + ((ResultTuple) vbl).triangleCt;

		}

		/**
		 * Set this shared variable to the given shared variable.
		 *
		 * @param vbl Shared variable.
		 * Return: None
		 */
		public void set(Vbl vbl) {
			this.tripletCt  = ((ResultTuple) vbl).tripletCt;
			this.triangleCt = ((ResultTuple) vbl).triangleCt;

		}
	}

	/*
	 * Reduction task class to compute global values of the counters and the
	 * cluster coefficient.
	 */
	private static class ReduceTask extends Task {

		/*
		 * Main program to retrieve the tuples containing the counter values,
		 * find the global values of the counters and the cluster coefficient.
		 * Return: None
		 */
		public void main(String args[]) throws Exception {
			// Number of vertices in the graph.
			int V = Integer.parseInt(args[0]);

			// Object to store the final value of counters.
			ResultTuple finalAns = new ResultTuple();
			// Object to be used as template while retrieving tuple.
			ResultTuple template = new ResultTuple();

			// Object used to store the retrieved tuple
			ResultTuple taskCover;

			while ((taskCover = tryToTakeTuple(template)) != null) {
				finalAns.reduce(taskCover);
			}

			// Printing the number of triplets in the graph.
			System.out.println(finalAns.triangleCt / 3);

			// Printing the number of triangles in the graph.
			System.out.println(finalAns.tripletCt);

			// Printing the cluster coefficient of the graph.
			System.out.printf("%.5f\n", ((double) finalAns.triangleCt / finalAns.tripletCt));

		}

	}

}
