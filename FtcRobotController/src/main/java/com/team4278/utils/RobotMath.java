package com.team4278.utils;



/**
*
* @author Noah Sutton-Smolin, Jamie Smith
*/
public class RobotMath {
   /**
    * Limits the angle to between 0 and 359 degrees for all math. All angles
    * should be normalized before use.
    * <p/>
    * @param angle the angle to be normalized
    * <p/>
    * @return the normalized angle on [0, 359]
    */
   public static double normalizeAngle(double angle)
   {
       double theta = ((angle % 360) + 360) % 360;
       return theta;
   }

   /**
    * Finds the shortest distance between two angles.
    *
    * @param angle1 angle
    * @param angle2 angle
    * @param shortWay if true, go the shorter way to make moves always <= 180
    * @return shortest angular distance between
    */
   public static double angleDistance(double angle1, double angle2, boolean shortWay)
   {
       double dist = normalizeAngle(angle2) - normalizeAngle(angle1);
       
       if(shortWay && Math.abs(dist) > 180)
       {
           double sgn = RobotMath.sgn(dist);
           return -sgn * (360 - Math.abs(dist));
       }
       
       return dist;
   }

   
   /**
    * Standard-ish sign function
    * @param n
    * @return
    */
   public static double sgn(double n)
   {
	   if(n == 0)
	   {
		   return 0;
	   }
	   
       return Math.abs(n) / n;
   
   }
   
   public static int sgn(int n)
   {
	   if(n == 0)
	   {
		   return 0;
	   }
	   
       return Math.abs(n) / n;
   }
  
   /**
    * Check if a motor power is between -1 and 1 inclusive
    * @param pow motor power
    * <p/>
    * @return whether or not the power is valid
    */
   public static boolean isValidPower(double pow)
   {
       return (pow >= -1 && pow <= 1);
   }

   /**
    * Makes the provided power into a valid power level.
    * <p/>
    * @param pow power level to convert
    * <p/>
    * @return a properly-limited power level
    */
   public static double makeValidPower(double pow)
   {
       return (pow < -1 ? -1 : (pow > 1 ? 1 : pow));
   }


   /**
    * Convert degrees to radians
    * @param angle degrees
    * @return radians
    */
   public static double dTR(double angle) {return Math.PI * angle / 180.0;}

   /**
    * Convert radians to degrees
    *
    * @param rad radians
    * @return degrees
    */
   public static double rTD(double rad) {return rad * (180.0 / Math.PI);}
   
   /**
    * Clamps value from (inclusive) minimum to maximum 
    * @param value
    * @param minimum
    * @param maximum
    * @return
    */
   public static int clampInt(int value, int minimum, int maximum)
   {
	   if(!(minimum <= maximum))
	   {
		   RoboLog.unexpected("RobotMath.clampInt() called with insane arguments");
	   }
	   return Math.min(Math.max(value, minimum), maximum); 
   }
   
   /**
    * Clamps value from (inclusive) minimum to maximum 
    * @param value
    * @param minimum
    * @param maximum
    * @return
    */
   public static double clampDouble(double value, double minimum, double maximum)
   {
	   return Math.min(Math.max(value, minimum), maximum); 
   }
   
   public static final double SQUARE_ROOT_TWO = Math.sqrt(2.0);

   /**
    * 
    * @param toFloor
    * @return An integer whose value is the same as or less than one lower than the argument.
    * Throws if the argument is too large to be an int.
    */
   public static int floor_double_int(double toFloor)
   {
	   double floored = Math.floor(toFloor);
	   if(toFloor > Integer.MAX_VALUE)
	   {
		   throw new IllegalArgumentException("The provided double is too large to be an int");
	   }
	   
	   return (int)floored;
   }
   
   /**
    * 
    * @param toCeil
    * @return An integer whose value is the same or less than one higher than the argument.
    * Throws if the argument is too large to be an int.
    */
   public static int ceil_double_int(double toCeil)
   {
	   double ceilinged = Math.ceil(toCeil);
	   if(ceilinged > Integer.MAX_VALUE)
	   {
		   throw new IllegalArgumentException("The provided double is too large to be an int");
	   }
	   
	   return (int)ceilinged;
   }

   //hidden constructor
   private RobotMath() {}

	/**
	 * Convert cm of robot movement to degrees of wheel movement
	 * @param cm
	 * @return
	 */
//	public static double cmToDegrees(double cm)
//	{
//		return Options.degreesPercm * cm;
//	}
	
	/**
	 * Convert cm of robot movement to wheel rotations
	 * @param cm
	 * @return
	 */
//	public static double cmToRotations(double cm)
//	{
//		return cm / Options.wheelCircumfrence;
//	}
}
