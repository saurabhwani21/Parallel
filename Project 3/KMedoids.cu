//Filename: KMedoids.cu
//Author  : Saurabh A. Wani 


//Device kernel to calculate the distance of all points from 
// all possible medoids.
//Called with grid of 2-D blocks. 
//Result is stored in 2-D matrix. 
//First vector x	Contains all x coordinates of the data points.
//Second vector y	Contains all y coordinates of the data points.
//Third vecotr z	Contains all z coordinates of the data points.
//Output Matrix 	Stores the distances of all points from to their respective medoids.  
extern "C" __global__ void Kmed
	(double *x,
	 double *y,
	 double *z,
	 double **totDist,
	 int N)
	 {
	 	//First medoid.
	 	int row = blockIdx.y*blockDim.y + threadIdx.y;
	 	//Second medoid.
	 	int col = blockIdx.x*blockDim.x + threadIdx.x;
	 	double dist1;
	 	double dist2;
	 	// dist for given pair of medoids
	 		 	
	 	
	 	
	 	//Compute the distance between point and medoids. 
	 	if (row < N && col < N )
		 	{
		 		if (row == col)
			 		{
			 			totDist[row][col] = 0;
			 		}
		 		else
			 		{
			 			double tDist = 0.0;		
			 			for ( int k=0; k < N; k+=1)
			 				{
			 					dist1 = abs(x[row] - x[k]) + abs(y[row] - y[k]) + abs(z[row] - z[k]);
			 					dist2 = abs(x[col] - x[k]) + abs(y[col] - y[k]) + abs(z[col] - z[k]);
			 					if (dist1 < dist2)
			 						{
			 							tDist = tDist + dist1;
			 						}
			 					else
			 						{
			 							tDist = tDist + dist2;
			 						}
			 				}
			 			totDist[row][col] = tDist;
		 			}
		 	}					
	 						
	 						
	 						
	 						
}	 						
