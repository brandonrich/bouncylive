package com.infinitedonuts.bouncylive;

import android.graphics.Point;

public class PhysicsHelper {
		
	public static final int AWAY = 0;
	public static final int TOWARD = 1;	
	
	public static final int LEFT = 0;
	public static final int RIGHT = 1;
	public static final int UP = 2;
	public static final int DOWN = 3;
	public static final int NO_CHANGE = 4;	
	
	    // returns a force that, if applied to an object at point A, will push it away from point B
	    // with the given magnitude.  direction == PhysicsHelper.AWAY or PhysicsHelper.TOWARD point B
	    public static ForceVector pushForce( Point a, Point b, double magnitude, int direction ) {
			double angle = PhysicsHelper.calcAngleDegrees(a, b);
			switch ( direction ) {
				case PhysicsHelper.AWAY:
				angle = -angle;
					break;
				case PhysicsHelper.TOWARD:
					angle = (-angle) + 180;
					break;
				
			}
			
			angle = Math.toRadians(angle);
			double xcomp = Math.cos(angle)* Math.abs(magnitude);
			double ycomp = -Math.sin(angle)* Math.abs(magnitude);  // negative cause on screen, down is positive.
			return new ForceVector( xcomp, ycomp );
	    }	
	    
	    
	    public static boolean circlesOverlap( PhysicsBall b1, PhysicsBall b2 ) {
	    	int center1 =  1;
	    	return true;
	    }
	    
	    // 
	    public static int getXDirection( Point last, Point current ) {
	    	int retval = PhysicsHelper.NO_CHANGE;
	    	if ( last.x < current.x ) {
	    		retval = PhysicsHelper.RIGHT;
	    	} else if ( last.x > current.x ) {
	    		retval = PhysicsHelper.LEFT;
	    	} 
	    	return retval;
	    }
	    
	    public static int getYDirection( Point last, Point current ) {
	    	int retval = PhysicsHelper.NO_CHANGE;
	    	if ( last.y < current.y ) {
	    		retval = PhysicsHelper.DOWN;
	    	} else if ( last.y > current.y ) {
	    		retval = PhysicsHelper.UP;
	    	} 
	    	return retval;
	    }  	    

	    // 
	    public static int getXDelta( Point last, Point current ) {
	    	return current.x - last.x;
	    }
	    
	    public static int getYDelta( Point last, Point current ) {
	    	
	    	return current.y - last.y;
	    }   	    
	    
	    public static float calcAngleDegrees (Point p1, Point p2 )
	    {
	        float f1 = (float)Math.toDegrees( Math.atan2( p1.y-p2.y, p1.x-p2.x ) );
	/*        if ( f1 >= 360 ) f1-=360;
	        if ( f1 <   0 ) f1+=360;*/
	        return f1;
	    }	    
	    
		public static double getSlope( Point a, Point b ) {
			return (double)((a.y-b.y)-(a.x-b.x));
		}	
		
		
		public static Point midpoint( Point a, Point b ) {
			return new Point( (a.x+b.x)/2, (a.y+b.y)/2 );
		}		
	    
}
