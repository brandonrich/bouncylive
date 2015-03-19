package com.infinitedonuts.bouncylive;

import android.graphics.Point;

public class LineSegment {
	public Point a = null;
	public Point b = null;
	
	public LineSegment(Point a, Point b) {
		super();
		// enforce rules -- point a should be the leftmost, or, if colinear on x, the uppermost.
		if ( a.x < b.x ) {
			this.a = a;
			this.b = b;
		} else {
			if( a.x > b.x ) {
				this.b = a;
				this.a = b;	
			} else {
				// the have the same x.
				if ( a.y > b.y ) {
					this.b = a;
					this.a = b;						
				} else {
					// note this case also accommodates the scenario when the two points are identical.
					this.a = a;
					this.b = b;					
				}
			}
		}
	}
	
		
}
