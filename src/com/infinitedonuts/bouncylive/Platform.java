package com.infinitedonuts.bouncylive;

import java.util.ArrayList;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;

public class Platform {

		protected ShapeDrawable mDrawable = null;
		protected int color;
		protected Rect screenRect = null;  // the literal screen-coordinate bounds of the rectangle
		protected Rect worldRect = null; // the bounds on the rectangle with respect to the world coordinate system
		protected PhysicsWorld world = null;
		protected Context context;
		
		public static final int NO_COLLISION = 0;
		public static final int COLLIDE_GENERIC_SEGMENT = 9;
		public static final int COLLIDE_CORNER_A = 10;
		public static final int COLLIDE_CORNER_B = 11;
		public static final int COLLIDE_INSIDE = 12;
		
		public static final int TOP_LINE = 1;
		public static final int LEFT_LINE = 2;
		public static final int RIGHT_LINE = 3;
		public static final int BOTTOM_LINE = 4;
		
		public static final int CORNER_TOPLEFT = 5;
		public static final int CORNER_TOPRIGHT = 6;
		public static final int CORNER_BOTTOMLEFT = 7;
		public static final int CORNER_BOTTOMRIGHT = 8;
		
		

		// points and line segments for the rectangle in the world coordinate system
		LineSegment topLine, leftLine, bottomLine, rightLine = null;
		Point topLeft, topRight, bottomLeft, bottomRight = null;
				
		public Rect getWorldRect() {
			return this.worldRect;
		}
		public Rect getScreenRect() {
			return this.screenRect;
		}		
		
		public LineSegment getLineSegment( int lineType ) {
			switch( lineType ) {
				case TOP_LINE:
					return topLine;
				case BOTTOM_LINE:
					return bottomLine;
				case LEFT_LINE:
					return leftLine;
				case RIGHT_LINE:
					return rightLine;
			    default:
			        return null;
			}
		}
		public Point getCorner ( int cornerType ) {
			switch( cornerType ) {
				case CORNER_TOPLEFT:
					return topLeft;
				case CORNER_BOTTOMLEFT:
					return bottomLeft;
				case CORNER_BOTTOMRIGHT:
					return bottomRight;
				case CORNER_TOPRIGHT:
					return topRight;
			    default:
			        return null;
			}
		}		
		
		
		public int circleIntersectsRectSegment( Point testPoint, double radius, LineSegment line ) {
			
			int retval = NO_COLLISION;
			

			
			// if it's a vertical line return the horizontal distance
		    if ( line.a.x == line.b.x) { 
		      double xdist = (double)Math.abs(line.a.x - testPoint.x);
		      // assumes point A of line will always be the leftmost one.  enforce in LineSegment constructor
		      if ( xdist <= radius && testPoint.y >= line.a.y && testPoint.y <= line.b.y ) {
		    	  retval = COLLIDE_GENERIC_SEGMENT;
		      }      
		    }

		    // if it's a horizontal line return the vertical distance
		    if ( line.a.y == line.b.y )  {
		      double ydist = (double)Math.abs(line.a.y - testPoint.y);
		      if ( ydist <= radius && testPoint.x >= line.a.x && testPoint.x <= line.b.x ) {
		    	  retval = COLLIDE_GENERIC_SEGMENT;
		      }
		    }
			    
		  	  // check if the distance between the center of the cirlce and either end of the line is less than the radius.
		      if ( world.getWorldDistance( testPoint, line.a) <= radius ) {
		    	  return Platform.COLLIDE_CORNER_A;
		      }
		      if ( world.getWorldDistance(testPoint, line.b) <= radius ) {
		    	  return Platform.COLLIDE_CORNER_B;
		      }
    
		    
		    return retval;

		    // not needed for rect, so i'll skip for now.
	/*	    // otherwise, find the slope of the line
		    double m_one = getSlope(line.a, line.b);
		    
		    // the other slope is at a right angle.
		    double m_two = -1.0 / m_one;      

		    // find the y-intercepts.
		    double b_one = line.a.y - line.a.x * m_one; 
		    double b_two = testPoint.y - testPoint.x * m_two;

		    // find the point of intersection
		    double x = (b_two - b_one) / (m_one - m_two); 
		    double y = m_one * x + b_one;

		    // find the x and y distances
		    double x_dist = x - testPoint.x;  
		    double y_dist = y - testPoint.y;

		    // and return the total distance.
		    return Math.sqrt(x_dist * x_dist + y_dist * y_dist); */
		    
		    
		}		

		
		public Platform(Context c, PhysicsWorld w, int colorID, Rect worldRect ) {
			super();

			context = c;
			mDrawable = new ShapeDrawable( new RectShape() );
			setColor( colorID );
			
			this.color = colorID;
			this.world = w;
			
			this.worldRect = worldRect;
			screenRect = new Rect( (int)world.worldToScreenX(worldRect.left), 
							 (int)world.worldToScreenY(worldRect.top), 
							 (int)world.worldToScreenX(worldRect.right), 
							 (int)world.worldToScreenY(worldRect.bottom )
					        );
			mDrawable.setBounds( screenRect );
			calculateLines();
		}
		
		public void setColor( int colorID ) {
			Resources r = context.getResources();
			mDrawable.getPaint().setColor(r.getColor(colorID));			
		}
		
		public Platform(Context c, PhysicsWorld w, int colorID, int x, int y, int width, int height ) {
			super();

			context = c;
			this.world = w;
			
			mDrawable = new ShapeDrawable( new RectShape() );
			setColor( colorID );
			this.color = colorID;
			
			this.worldRect = new Rect(x, y, width+x, y+height);
			screenRect = new Rect( (int)world.worldToScreenX(x), 
					 (int)world.worldToScreenY(y), 
					 (int)world.worldToScreenX(width+x), 
					 (int)world.worldToScreenY(height+y )
			        );			
			mDrawable.setBounds( screenRect );		
			
			calculateLines();
		}
	
		
		private void calculateLines() {

			topLeft = new Point( worldRect.left, worldRect.top );
			topRight = new Point( worldRect.right, worldRect.top );
			bottomLeft = new Point( worldRect.left, worldRect.bottom );
			bottomRight = new Point( worldRect.right, worldRect.bottom);

			topLine = new LineSegment( topLeft, topRight);
			leftLine = new LineSegment(topLeft, bottomLeft );
			rightLine = new LineSegment(topRight, bottomRight );
			bottomLine = new LineSegment(bottomLeft, bottomRight);
		
		}
		
		public void onDraw(Canvas c) {
			mDrawable.draw(c);
		}
		
		
		public int intersects( PhysicsBall ball ) {
			    
			    int retval = NO_COLLISION;
			    Point ballCenter = ball.getCenterPoint();
			    boolean pointInRect = worldRect.contains( ballCenter.x, ballCenter.y );
			    if ( pointInRect ) {
			    	return COLLIDE_INSIDE;  // for breakpoint
			    }
			    // for a thin line, multiple segments could collide at once.  should factor in the movement of the ball			    
			    // to see which collision should be responded to first.
			    
			    // determine the direction of the ball's travel and decide which
			    // walls to evaluate based on that.
			    int xDirection = PhysicsHelper.NO_CHANGE;  // defaults; direction of travel is unknown.
			    int yDirection = PhysicsHelper.NO_CHANGE;
			    Point historicalPoint = ball.getLastDistinctPoint();
			    if ( historicalPoint != null ) {
			    	Point worldLocation = ball.getLocation();
				    xDirection = PhysicsHelper.getXDirection(historicalPoint, worldLocation);
				    yDirection = PhysicsHelper.getYDirection(historicalPoint, worldLocation);
			    }
			    
			    // this should eliminate my ever being able to throw the ball THROUGH the platform.
			    // but in the (literal) corner cases, I may also need to consider the magnitude of 
			    // the overlap to determine which intersection occurred first and so whether to do the x or y bounce.
			    
			    if ( yDirection == PhysicsHelper.DOWN ) {
				    int topCollide = circleIntersectsRectSegment( ball.getCenterPoint(), ball.getRadius(), topLine );
			    	if ( topCollide ==  COLLIDE_GENERIC_SEGMENT ) {
				    		retval = TOP_LINE;
			    	}
			    	if ( topCollide ==  COLLIDE_CORNER_A ) {
			    		retval = CORNER_TOPLEFT;
			    	}		    	
			    	if ( topCollide ==  COLLIDE_CORNER_B ) {
			    		retval = CORNER_TOPRIGHT;
			    	}			    	
			    }

			    if ( xDirection == PhysicsHelper.RIGHT ) {
				    int leftCollide = circleIntersectsRectSegment( ball.getCenterPoint(), ball.getRadius(), leftLine );
			    	if ( leftCollide ==  COLLIDE_GENERIC_SEGMENT ) {
				    		retval = LEFT_LINE;
			    	}
			    	if ( leftCollide ==  COLLIDE_CORNER_A ) {
			    		retval = CORNER_TOPLEFT;
			    	}		    	
			    	if ( leftCollide ==  COLLIDE_CORNER_B ) {
			    		retval = CORNER_BOTTOMLEFT;
			    	}
			    }
		    	
			    if ( xDirection == PhysicsHelper.LEFT ) {
				    int rightCollide = circleIntersectsRectSegment( ball.getCenterPoint(), ball.getRadius(), rightLine );
			    	if ( rightCollide ==  COLLIDE_GENERIC_SEGMENT ) {
				    		retval = RIGHT_LINE;
			    	}
			    	if ( rightCollide ==  COLLIDE_CORNER_A ) {
			    		retval = CORNER_TOPRIGHT;
			    	}		    	
			    	if ( rightCollide ==  COLLIDE_CORNER_B ) {
			    		retval = CORNER_BOTTOMRIGHT;
			    	}		    	
			    }
		    
		    	if ( yDirection == PhysicsHelper.UP ) {
				    int bottomCollide = circleIntersectsRectSegment( ball.getCenterPoint(), ball.getRadius(), bottomLine );
			    	if ( bottomCollide ==  COLLIDE_GENERIC_SEGMENT ) {
				    		retval = BOTTOM_LINE;
			    	}
			    	if ( bottomCollide ==  COLLIDE_CORNER_A ) {
			    		retval = CORNER_BOTTOMLEFT;
			    	}		    	
			    	if ( bottomCollide ==  COLLIDE_CORNER_B ) {
			    		retval = CORNER_BOTTOMRIGHT;
			    	}			    	
		    	}

			    return retval;
			
		}
		
		
		  public int clamp (int i, int low, int high) {
			    return java.lang.Math.max (java.lang.Math.min (i, high), low);
			  }
		
		
		
		
}
