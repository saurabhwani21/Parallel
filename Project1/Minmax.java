package project1;
/**
 * Filename: Minimax.java
 * This class calculates the minimum and the maximum solutions.
 * @author Saurabh Anant Wani
 *
 */



public class Minmax  implements Cloneable{

	//Variables to store values of x,y,z of a solution.
	long x;
	long y;
	long z;
		
	//Constructor used for initializing max and min solution objects.
	public Minmax(long limit) {
		this.x = limit;
		this.y = limit;
		this.z = limit;
	}
	
	//Create an object with the values.
	public Minmax(long x, long y, long z){
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	



	/**
	    * Make this Minmax be a deep copy of the given Minmax.
	    *
	    * @param  solObj  Minmax to copy.
	    *
	    * @return  This Minmax.
	    */
	   public Minmax copy
	      (Minmax solObj)
	      {
	      this.x  = solObj.x;
	      this.y  = solObj.y;
	      this.z  = solObj.z;
	      return this;
	      }

	   /**
	    * Create a clone of this Minmax.
	    *
	    * @return  Clone.
	    */
	   public Object clone()
	      {
	      try
	         {
	         Minmax solObj = (Minmax) super.clone();
	         solObj.copy (this);
	         return solObj;
	         }
	      catch (CloneNotSupportedException exc)
	         {
	         throw new RuntimeException ("Shouldn't happen", exc);
	         }
	      }
	
	   /**
	    * This function compares the two solutions for the minimum solution.
	    * @param h	Object of the class containing solution to be compared with. 
	    */
		public void solnMin(Minmax h){
			
			if(this.x > h.x){
				this.x = h.x;
				this.y = h.y;
				this.z = h.z;
			}
			else if (this.x == h.x && this.y > h.y){
				this.x = h.x;
				this.y = h.y;
				this.z = h.z;
			}
			else if (this.x == h.x && this.y == h.y && this.z > h.z){
				this.x = h.x;
				this.y = h.y;
				this.z = h.z;
			}
			
					
		}
		
	   /**
	    * This function compares the two solutions for the maximum solution.
	    * @param h	Object of the class containing solution to be compared with. 
	    */		
		public void solnMax(Minmax h){
			
			if(this.x < h.x){
				this.x = h.x;
				this.y = h.y;
				this.z = h.z;
			}
			else if (this.x == h.x && this.y < h.y){
				this.x = h.x;
				this.y = h.y;
				this.z = h.z;
			}
			else if (this.x == h.x && this.y == h.y && this.z < h.z){
				this.x = h.x;
				this.y = h.y;
				this.z = h.z;
			}
					
		}
	
	
	
}
