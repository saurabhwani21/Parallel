import edu.rit.util.Random;
import java.util.NoSuchElementException;

/**
 * Class RandomGraph encapsulates a random undirected graph generated using the
 * Gilbert procedure. Each possible edge appears in the graph with probability
 * E/choose(V,2).
 *
 * @author  Alan Kaminsky
 * @version 01-Sep-2016
 */
public class RandomGraph
	implements Graph
	{

	private int V;          // Number of vertices
	private int E;          // Number of edges
	private int toVisit;    // Number of edges still to visit
	private int toGenerate; // Number of edges still to generate
	private int v1;         // First vertex of current edge
	private int v2;         // Second vertex of current edge
	private Random prng;    // Pseudorandom number generator

	/**
	 * Construct a new random graph.
	 *
	 * @param  V     Number of vertices, V &ge; 2.
	 * @param  E     Number of edges, 0 &le; E &le; choose(V,2).
	 * @param  seed  Random seed.
	 *
	 * @exception  IllegalArgumentException
	 *     (unchecked exception) Thrown if V or E is out of range.
	 */
	public RandomGraph
		(int V,
		 int E,
		 long seed)
		{
		System.out.println("inside");
		// Verify preconditions.
		this.V = V;
		this.E = E;
		if (V < 2)
			throw new IllegalArgumentException (String.format
				("RandomGraph(): V = %d illegal", V));
		toVisit = V*(V - 1)/2;
		if (0 > E || E > toVisit)
			throw new IllegalArgumentException (String.format
				("RandomGraph(): E = %d illegal", E));

		// Initialize fields.
		toGenerate = E;
		v1 = 0;
		v2 = 1;
		prng = new Random (seed);
		System.out.println("outside");
		}

	/**
	 * Returns the number of vertices in this graph, V.
	 */
	public int V()
		{
		return V;
		}

	/**
	 * Returns the number of edges in this graph, E.
	 */
	public int E()
		{
		return E;
		}

	/**
	 * Obtain the next edge in this graph. This method must be called
	 * repeatedly, E times, to obtain all the edges. Each time this method is
	 * called, it stores, in the v1 and v2 fields of object e, the vertices
	 * connected by the next edge. Each vertex is in the range 0 .. V-1.
	 *
	 * @param  edge  Edge object in which to store the vertices.
	 *
	 * @exception  NoSuchElementException
	 *     (unchecked exception) Thrown if this method is called more than E
	 *     times.
	 */
	public void nextEdge
		(Edge e)
		{
		// Verify preconditions.
		if (toGenerate == 0)
			throw new NoSuchElementException();

		// Visit edges until the next randomly chosen one is generated.
		boolean found = false;
		while (! found)
			{
			// Generate current edge with probability p.
			double p = (double)toGenerate/(double)toVisit;
			if (prng.nextDouble() < p)
				{
				e.v1 = this.v1;
				e.v2 = this.v2;
				-- toGenerate;
				found = true;
				}

			// Update current edge.
			-- toVisit;
			++ v2;
			if (v2 == V)
				{
				++ v1;
				v2 = v1 + 1;
				}
			}
		}

	}
