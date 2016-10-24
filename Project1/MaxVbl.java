package project1;
/***
 * filename: MaxVbl.java
 * This class is used to store the maximum solution.
 * @author Saurabh Anant Wani
 */

import edu.rit.pj2.Vbl;


public class MaxVbl implements Vbl{
	
	
	public Minmax maxObj;
	
	//Creates object of the solution class.
	public MaxVbl(Minmax maxObj2) {
		this.maxObj = maxObj2;
	}


	/**
	 * Create a clone of this shared variable.
	 *
	 * @return The cloned object.
	 */
	public Object clone() {
		try {
			MaxVbl vbl = (MaxVbl) super.clone();

			if (this.maxObj != null)
				vbl.maxObj = (Minmax) this.maxObj.clone();
			return vbl;
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException("Shouldn't happen", e);
		}
	}

	/**
	 * Overrides the basic functionality of reduction.\ Finds the global minimum
	 * and maximum.
	 */
	public void reduce(Vbl vbl) {		
		this.maxObj.solnMax(((MaxVbl)vbl).maxObj);		
	}

	
	/**
	 * Set this shared variable to the given shared variable.
	 *
	 * @param vbl Shared variable.
	 */
	public void set(Vbl vbl) {
		this.maxObj.copy(((MaxVbl) vbl).maxObj);
	}


}
