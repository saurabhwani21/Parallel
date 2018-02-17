/**
 * filename: DioEqnSmp.java
 * This is the parallel version of calculating the  possible values of the given equation.
 * 
 * @author Saurabh A. Wani (saw4058@rit.edu)
 */

import edu.rit.pj2.LongLoop;
import edu.rit.pj2.Task;
import edu.rit.pj2.vbl.LongVbl;

public class DioEqnSmp extends Task {

	// To keep count of the number of solutions.
	LongVbl counter;
	//Global object to keep track of maximum solution.	
	MaxVbl mxVbl;
	//Global object to keep track of minimum solution.		
	MinVbl mnVbl;
	
	/**
	 * Main class.
	 */
	public void main(final String[] args) throws Exception {
		// Validate command line arguments.
		validation(args);

		// Assign input to corresponding variables in the equation.
		int n = Integer.parseInt(args[0]);
		long c = Long.parseLong(args[1]);
		long lb = Long.parseLong(args[2]);
		long ub = Long.parseLong(args[3]);

		//Initializing the max solution value object.
		mxVbl = new MaxVbl (new Minmax(lb));
		//Initializing the min solution value object.
		mnVbl = new MinVbl (new Minmax(ub));
		
		// Calling function to evaluate the answers.
		equation(n, c, lb, ub);
	}

	/**
	 * Calculates possible solution for given values of n, c in range from lower
	 * bound 'lb' to upper bound 'ub'.
	 * 
	 * @param n   The power to which the values x, y and z are raised to.
	 * @param c   The constant in the equation.
	 * @param lb  The lower bound range of solution.
	 * @param ub  The upper bound range of solution.
	 */
	private void equation(final int n, final long c, final long lb, final long ub) {
		counter = new LongVbl.Sum(0);
		

		parallelFor(lb, ub).schedule(dynamic).exec(new LongLoop() {
			
			//To keep thread local count of number of solutions.
			LongVbl tcount;
			//To store thread local maximmum solution.
			MaxVbl thmax;
			//To store thread local minimmum solution.
			MinVbl thmin;
			
			public void start() {
				
				//Initialzing thread local parameters.
				thmax = threadLocal(mxVbl);
				thmin = threadLocal(mnVbl);				
				tcount = threadLocal(counter);
			}

			public void run(final long x) {

				for (long y = x; y <= ub; y++) {
					
					for (long z = lb; z <= ub; z++) {

						// Checking given condition
						if (PowerCalc.powerFunc(x, n) + PowerCalc.powerFunc(y, n) == PowerCalc.powerFunc(z, n) + c) {
							
							//Checking if solution is minimum or maximum.
							Minmax mm = new Minmax(x,y,z);

							thmax.maxObj.solnMax(mm);
							thmin.minObj.solnMin(mm);
							
							//Incrementing the local thread counter to keep track of number of solutions.
							tcount.item++;
						}
					}
				}
			}

		});
		
		//To display the results.		
		output(n, c);
	}

	/**
	 * Prints the solution/solutions if any exist.
	 * @param n	It the n from the given equation.
	 * @param c It is the constant from the given equation.
	 */
	private void output(int n, long c) {
		//If no solution is found.
		if (counter.item == 0) {
			System.out.println("0");
		}
		
		//If only one solution is found.
		else if (counter.item == 1) {
			System.out.println("1");
			System.out.println(mnVbl.minObj.x + "^" + n + " + " + mnVbl.minObj.y + "^" + n + " = "
					+ mnVbl.minObj.z + "^" + n + " + " + c);			
		} 
		
		//If multiple solutions are found.
		else {
			System.out.println(counter.item);			
			System.out.println(mnVbl.minObj.x + "^" + n + " + " + mnVbl.minObj.y + "^" + n + " = "
					+ mnVbl.minObj.z + "^" + n + " + " + c);
			System.out.println(mxVbl.maxObj.x + "^" + n + " + " + mxVbl.maxObj.y + "^" + n + " = "
					+ mxVbl.maxObj.z + "^" + n + " + " + c);			
		}
	}

	/**
	 * Prints a usage message and exit.
	 */
	private static void validation(String[] input) {
		//Check if the expected number of arguments are passed as input.
		if (input.length != 4){
			System.err.println("Error: java pj2 DioEqnSeq <n> <c> <lb> <ub>");
			terminate(1);		
		}
				
		try{
			//Check if n is an integer and greater than 2.
			int n = Integer.parseInt(input[0]);
			if (n<2){
				System.err.println("Error: n is less than 2.");
				terminate(1);
			}
			
			//Check if c, lb, ub are of type long.
			for (int i=1; i<input.length; i++){
				Long.parseLong(input[i]);
			}
		}
		catch(NumberFormatException e)
		{
			System.err.println("Error: java pj2 DioEqnSeq <n> <c> <lb> <ub>");
			System.err.println("Error: Incorrect number format");
			terminate(1);			
		}
		

	}

}
