package project1;
/***
 * filename: MaxVbl.java
 * This class is used to store the minimum solution.
 * @author Saurabh Anant Wani
 */

import edu.rit.pj2.Vbl;


public class MinVbl implements Vbl{
	
	public Minmax minObj;

	//Creates object of the solution class.
	public MinVbl(Minmax minObj2) {
		this.minObj = minObj2;
	}


	/**
	 * Create a clone of this shared variable.
	 *
	 * @return The cloned object.
	 */
	public Object clone() {

		try {
			MinVbl vbl = (MinVbl) super.clone();

			if (this.minObj != null)
				vbl.minObj = (Minmax) this.minObj.clone();
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
//		this.minObj.solnMin(((MinVbl)vbl).minObj);
	}

	
	/**
	 * Set this shared variable to the given shared variable.
	 *
	 * @param vbl Shared variable.
	 */
	public void set(Vbl vbl) {
		this.minObj.copy(((MinVbl) vbl).minObj);
	}


}
