/*
 * filename: KMedoids.java
 * Author: Saurabh A. Wani
 * This file calculates the best solution of K-medoids clustering for given points. 
 */

import edu.rit.gpu.CacheConfig;
import edu.rit.gpu.Gpu;
import edu.rit.gpu.GpuDoubleArray;
import edu.rit.gpu.GpuDoubleMatrix;
import edu.rit.gpu.Kernel;
import edu.rit.gpu.Module;
import edu.rit.pj2.Task;
import edu.rit.util.Instance;

public class KMedoids extends Task {

	// Kernel function interface.
	private static interface KMedKernel extends Kernel
	{
		public void Kmed (GpuDoubleArray x, GpuDoubleArray y, GpuDoubleArray z, GpuDoubleMatrix totDist, int N);
	}
	
	//GPU block dimensions = NT x NT
	private static final int NT = 32;
	
	/**
	 *  Task main program
	 *  Return:	None
	 */
	public void main (String[] args) throws Exception{
		
		// Validate command line arguments.
		if (args.length != 1)
			usage();

		// Initialize GPU.
		Gpu gpu = Gpu.gpu();
		gpu.ensureComputeCapability(2, 0);

		PointGroup pg = (PointGroup) Instance.newInstance(args[0]);
		int N = pg.N();
		
		// Array to store locations of points based on x,y,z axes.
		GpuDoubleArray x = gpu.getDoubleArray(N);
		GpuDoubleArray y = gpu.getDoubleArray(N);
		GpuDoubleArray z = gpu.getDoubleArray(N);
		GpuDoubleMatrix totDist = gpu.getDoubleMatrix(N, N);
		
		//Creating point object for generating data points.		
		Point p1 = new Point();
		// Generating all points
		for (int i = 0; i < N; i++) {
			pg.nextPoint(p1);
			x.item[i] = p1.x;
			y.item[i] = p1.y;
			z.item[i] = p1.z;
		}
		
		// Passing points from CPU to GPU
		x.hostToDev();
		y.hostToDev();
		z.hostToDev();
		
		//Clustering
		Module module  = gpu.getModule("KMedoids.ptx");
		KMedKernel kernel = module.getKernel(KMedKernel.class);
		kernel.setBlockDim(NT,NT);		//32x32
		kernel.setGridDim((N + NT -1)/NT, (N + NT -1)/NT);
		kernel.setCacheConfig(CacheConfig.CU_FUNC_CACHE_PREFER_L1);
		kernel.Kmed(x, y, z, totDist, N);
		
		//Getting results from GPU to CPU.
		totDist.devToHost();
		//Printing the final result.
		soln(totDist, N);
			
	}
	

	/**
	 * Function to find the best solution and print the results. 
	 * @param totDist	Final total distance of all points from their cluster.
	 * @param N			Total number of data points.
	 * Return:			None
	 */	
	public void soln (GpuDoubleMatrix totDist, int N){
		double finaldist = 0.0;
		int a=0,b=0;
		boolean flag = true;
		for (int i = 0; i < N; i++){
			for (int j = 0; j < N; j++){
				if (i==j){
					continue;
				}
				if (flag == true){
					flag = false;
					finaldist = totDist.item[i][j];
					a = i;
					b = j;
				}
				else {
					if (totDist.item[i][j] < finaldist) {
						finaldist = totDist.item[i][j];
						a = i;
						b = j;
					} else if (totDist.item[i][j] == finaldist) {
						if (i < a) {
							finaldist = totDist.item[i][j];
							a = i;
							b = j;
						} else if (i == a) {
							if (j < b) {
								finaldist = totDist.item[i][j];
								a = i;
								b = j;
							}
						}
					}
				}
			}
		}
		//Printing the results.
		System.out.println(a);
		System.out.println(b);
		System.out.printf("%.3f\n",finaldist);
		
	}
	
	/**
	 * Print a usage message and exit. 
	 * Return: None
	 */
	public static void usage() {
		System.err.println("Usage: Invalid argument");
		System.exit(1);
	}

	// Specify that this task requires only one core.
	protected static int coresRequired() {
		return 1;
	}

	// Specify that this task requires only one GPU accelerator.
	protected static int gpusRequired() {
		return 1;
	}
	
}
