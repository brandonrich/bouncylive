package com.infinitedonuts.bouncylive;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Stack;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

public class PhysicsBall { //extends View {
	
	// visual properties
    protected ShapeDrawable mDrawable;
    protected int ballColor;
    
    public int getBallColor() {
		return ballColor;
	}

	// position and size properties
    protected double width, height;
    protected int lastX, lastY;
    protected double radius;
    protected Point worldLocation;
    protected Point screenLocation;
    protected Rect myLastBounds;
    protected boolean disableTouch = false;
    
    // current movement status
    protected ForceVector vel;
    protected boolean beingHeld;
    
    // used in allowing finger fling.  pretty hacky.
    protected Stack<Point> previousLocations = null;
    
    // physical attributes
    protected double mBounciness = 0.4;  // negative can simulate stickiness
    protected double mass = 1;  // negative values will float
    
    protected Context context = null;

	protected PhysicsWorld world = null;
    
	public void draw(Canvas c) {
		mDrawable.draw(c);
	}
    
	public PhysicsWorld getWorld() {
		return world;
	}

	public void setWorld(PhysicsWorld world) {
		this.world = world;
	}

	protected static final double MAX_RADIUS_PERCENT = .1;   // upper bound for the size of the ball's radius relative to the shorter screen size    
    
	public double getArea() {
		return Math.PI * radius * radius;
	}
	
    public double getBounciness() {
		return mBounciness;
	}

	public void setBounciness(double bounciness) {
		this.mBounciness = bounciness;
	}

    public PhysicsBall(Context context, AttributeSet attrs) {
		//super(context, attrs);
		// TODO Auto-generated constructor stub
		
    	this.context = context;
		TypedArray a = context.obtainStyledAttributes(attrs,
	                R.styleable.PhysicsBall);
		
		

		
		// inflate attributes from xml
		int x = a.getInt(R.styleable.PhysicsBall_initialX, 10);
		int y = a.getInt(R.styleable.PhysicsBall_initialY, 10);
			

		
		ballColor = a.getColor(R.styleable.PhysicsBall_color, 
				context.getResources().getColor(R.color.lightBlue));  // default to blue
		
		radius = a.getFloat(R.styleable.PhysicsBall_radius, 30.0f);
		width = (int)Math.floor(radius*2);
		height = (int)Math.floor(radius*2);		
		 
        
		// initialize velocity
        vel = new ForceVector(0,0);
        worldLocation = new Point(x, y);
        lastX = lastY = 10;
        beingHeld = false;
        previousLocations = new Stack<Point>();

        mDrawable = new ShapeDrawable(new OvalShape());
        mDrawable.getPaint().setColor(ballColor);
        
	}

    
    public PhysicsBall(Context context, PhysicsWorld world) {
		//super(context, attrs);
		// TODO Auto-generated constructor stub
	    this.world=world;

	    this.context = context;
	    
		// inflate attributes from xml
		int x = 200;
		int y = 200;
			

		
		ballColor = context.getResources().getColor(R.color.lightBlue);  // default to blue
		
		radius = 100.0f;
		width = (int)Math.floor(radius*2);
		height = (int)Math.floor(radius*2);		
		 
        
		// initialize velocity
        vel = new ForceVector(0,0);
        worldLocation = new Point(x, y);
        lastX = lastY = 10;
        beingHeld = false;
        previousLocations = new Stack<Point>();

        mDrawable = new ShapeDrawable(new OvalShape());
        mDrawable.getPaint().setColor(ballColor);
        
	}
    

    public PhysicsBall(Context context, PhysicsWorld world,
    		int initialX, int initialY, int colorValue, float radius ) {
		//super(context, attrs);
		// TODO Auto-generated constructor stub
	    this.world=world;
	    
	    this.context = context;

		
		// inflate attributes from xml
		int x = initialX;
		int y = initialY;
			
		Resources r = context.getResources();
		ballColor = colorValue;
		//ballColor = r.getColor(colorID);  // default to blue
		
		this.radius = radius;
		width = (int)Math.floor(radius*2);
		height = (int)Math.floor(radius*2);		
		 
        
		// initialize velocity
        vel = new ForceVector(0,0);
        worldLocation = new Point(x, y);
        lastX = lastY = 10;
        beingHeld = false;
        previousLocations = new Stack<Point>();

        mDrawable = new ShapeDrawable(new OvalShape());
        mDrawable.getPaint().setColor(colorValue);
        
	}
    

            
    
    
    
    public void setColorByResourceID( int newColor ) {
    	mDrawable.getPaint().setColor(context.getResources().getColor(newColor));
    }
    
    public void setColor( int newColorCode ) {
    	mDrawable.getPaint().setColor(newColorCode);
    }    

    
    public void setOriginalColor() {
    	mDrawable.getPaint().setColor(context.getResources().getColor(this.ballColor));
    }
    
    public double getRadius() {
    	return radius;
    }
    
    public void applyForce( ForceVector f ) {
    	//vel.setXForce( )= f;
    	if ( !beingHeld ) {
			vel.x += (f.x*mass);
			vel.y += (f.y*mass);  // experimenting here...
    	}
    }
    
    public void applyFriction(double fx, double fy) {
    	vel.x = vel.x * fx;
    	vel.y = vel.y * fy;
    }
    
    public void update() {
    	this.setLocation( new Point(worldLocation.x + (int)vel.x, worldLocation.y + (int)vel.y ));    
    }
    
    // find the last historical point that does not equal the current one.
    public Point getLastDistinctPoint() {
    	if ( previousLocations == null ) {
    		return null;
    	}
    	if ( previousLocations.size() == 0 ) {
    		return null;
    	}
    	int i = previousLocations.size() - 1;
    	Point checkMe = previousLocations.get(i);
    	while( worldLocation.x == checkMe.x && worldLocation.y == checkMe.y  ) {
    		i--;
    		if ( i >= 0 ) {
    			checkMe = previousLocations.get(i);
    		} else {
    			break;
    		}
    	}
    	return checkMe;
    }
    

    
   
    
    // if platform p == null, assume it is the world boundaries (they are not explicit platform objects...yet) 
    public void doWallBounce( Platform p, int wallType ) {
    	
    	// variables needed for corner bound
   		Point rectCornerPoint = null;
		int degreesToXIntercept, degreesToYIntercept;    	
    	
		double newY = -(this.getVelocityY())*this.getBounciness();
    	int newX = (int)Math.floor((-this.getVelocityX()*(this.getBounciness())));
    	double currentVelocityY = this.getVelocityY();
    	double currentVelocityX = this.getVelocityX();
    	
    	Rect rect = null;
    	if ( p != null ) {
    		rect = p.getWorldRect();
    	}
		
    	switch( wallType ) {
    	case Platform.TOP_LINE:    // top of a platform, or could be the world floor.
    		this.setVelocity(currentVelocityX, newY);
    		
    		int floor = 0;
    		if ( p == null ) {
    			floor = (int)(world.getWorldHeight() - world.screenToWorldY(PhysicsWorld.STATUS_BAR_HEIGHT));
    		} else {
    			// set floor to the top line segment
    			floor = (int)(rect.top);    			
    		}
    		this.applyFriction(0.85, 1);
   			this.setLocation(new Point(worldLocation.x, (int)(floor-width)));    		
    		break;
    	case Platform.BOTTOM_LINE:    // bottom of a platform, or could be the world ceiling.
    		this.setVelocity(currentVelocityX, newY);
    		if ( p == null ) {
    			this.setLocation(new Point(worldLocation.x, 0));  // not strictly necessary
    		} else {
    			// set floor to the top line segment
    			this.setLocation(new Point(worldLocation.x, rect.bottom));  // not strictly necessary    			
    		}    		
    		   		
    		break;    
    	case Platform.LEFT_LINE:  // left side of platform or world right wall  		
    		if ( p == null ) {
    			// right wall
    			this.setLocation(new Point((int)(world.getWorldWidth()-width), worldLocation.y));
    		} else {
    			// left wall of rectangle
    			this.setLocation(new Point((int)(rect.left-width), worldLocation.y)); 			
    		}    		
    		this.setVelocity(newX, currentVelocityY);
    		break;
    	case Platform.RIGHT_LINE:  // right side of platform or world left wall
    		if ( p == null ) {
    			// right wall
    			this.setLocation(new Point(0, worldLocation.y));
    		} else {
    			// left wall of rectangle
    			this.setLocation(new Point(rect.right, worldLocation.y)); 			
    		}      		
    		
    		this.setVelocity(newX, currentVelocityY);
    		break;    
    		
    	case Platform.CORNER_BOTTOMLEFT:
    	case Platform.CORNER_BOTTOMRIGHT:
    	case Platform.CORNER_TOPLEFT:
    	case Platform.CORNER_TOPRIGHT:
    		doCornerBounce( wallType, p );
    		break;    		
    	case Platform.COLLIDE_INSIDE:
    		// need to find the corner we passed to get in here.
    		// this is likely (but not guaranteed!) to be the closest corner, so let's try that first.
    		// test with topLeft
    		doCornerBounce( Platform.CORNER_TOPLEFT, p );
    		
    		break;
/*    		// back up over timestamp
    		collisionPoint = this.getCenterPoint();
    		while ( (p.intersects(this) != Platform.NO_COLLISION) && (this.previousLocations.size() != 0) ) {
    			this.rewindLocation();  // take a step back
    		}
    		noCollisionPoint = this.getCenterPoint();
    		
    		// find out what direction the ball WAS going and bounce accordingly.
    		// actually we don't need direction; we just need to know which axis of travel was of greater magnitude!
    		int xd = PhysicsBall.getXDelta(noCollisionPoint, collisionPoint);
		    int yd = PhysicsBall.getYDelta(noCollisionPoint, collisionPoint); 
		    if ( Math.abs(xd) > Math.abs(yd) ) {
		    	this.setVelocity(newX, currentVelocityY);
		    } else {
		    	this.setVelocity(currentVelocityX, newY);   // need to know direction
		    }
    		
    		
    		//this.applyForce( PhysicsHelper.pushForce(noCollisionPoint, collisionPoint, world.getDistance(noCollisionPoint, collisionPoint), PhysicsHelper.AWAY));
    		int y = 1;  // this is just here to have a breakpoint
    		break;*/
    	
    		
    	}
    }
    
    
    // attempts to discern whether the corner collision is greater on the x axis or y axis, and
    // determines the bounce direction accordingly.
    public void doCornerBounce( int cornerType, Platform p) {
    	
/*    	Point rectCornerPoint = null;
    	int degreesToXIntercept, degreesToYIntercept;
    	switch( cornerType ) {
			case Platform.CORNER_BOTTOMLEFT:
				rectCornerPoint = p.topLeft;
				degreesToXIntercept = 0;
				degreesToYIntercept = 90;
				break;    		
			case Platform.CORNER_BOTTOMRIGHT:
				rectCornerPoint = p.topLeft;
				degreesToXIntercept = 180;
				degreesToYIntercept = 90;
				break;
			case Platform.CORNER_TOPLEFT:
				rectCornerPoint = p.topLeft;
				degreesToXIntercept = 0;
				degreesToYIntercept = 270;
				break;
			case Platform.CORNER_TOPRIGHT:
				rectCornerPoint = p.topLeft;
				degreesToXIntercept = 180;
				degreesToYIntercept = 270;
				break;
			default:
				return;
    	}
    	
		double newY = -(this.getVelocityY())*this.getBounciness();
    	int newX = (int)Math.floor((-this.getVelocityX()*(this.getBounciness())));
    	double currentVelocityY = this.getVelocityY();
    	double currentVelocityX = this.getVelocityX();
    	
    	double radius_n = this.getRadius() - world.getDistance(rectCornerPoint, this.getCenterPoint());
		Point xIntercept = new Point();
		xIntercept.x = (int)(rectCornerPoint.x + radius_n*Math.cos(Math.toRadians(degreesToXIntercept)));
		xIntercept.y = (int)(rectCornerPoint.y + radius_n*Math.sin(Math.toRadians(degreesToXIntercept)));
		
		Point yIntercept = new Point();
		yIntercept.x = (int)(rectCornerPoint.x + radius_n*Math.cos(Math.toRadians(degreesToYIntercept)));
		yIntercept.y = (int)(rectCornerPoint.y + radius_n*Math.sin(Math.toRadians(degreesToYIntercept)));
		
		double xdist = world.getDistance(xIntercept, rectCornerPoint);
		double ydist = world.getDistance(yIntercept, rectCornerPoint);

	    if ( Math.abs(xdist) > Math.abs(ydist) ) {
	    	this.setVelocity(newX, currentVelocityY);
	    } else {
	    	this.setVelocity(currentVelocityX, newY);   // need to know direction
	    }  		
	    */
		Point collisionPoint = this.getCenterPoint();
		while ( (p.intersects(this) != Platform.NO_COLLISION) && (this.previousLocations.size() != 0) ) {
			this.rewindLocation();  // take a step back
		}
		Point noCollisionPoint = this.getCenterPoint();    	
		
		// okay, we have two points now.  let's do a binary search to try to find the single collision point
		Point halfwayPoint = PhysicsHelper.midpoint(collisionPoint, noCollisionPoint);
		double distance = world.getWorldDistance(collisionPoint, noCollisionPoint);
		while ( distance > 3 ) {  // arbitrary value
			this.setCenterPoint(halfwayPoint);
			if ( p.intersects(this) != Platform.NO_COLLISION) {
				collisionPoint = this.getCenterPoint();
			} else {
				noCollisionPoint = this.getCenterPoint();
			}
			halfwayPoint = PhysicsHelper.midpoint(collisionPoint, noCollisionPoint);			
			distance = world.getWorldDistance(collisionPoint, noCollisionPoint);
		}
		this.setCenterPoint(noCollisionPoint);
		this.setVelocity(0, 0);
		// we should now be located at more or less the exact collision point.
    }
    
    
    // checks for collision.  If one occurs, the "this"
    // ball gets pushed away from otherBall.
    public void avoid( PhysicsBall otherBall ) {
    	double r1, r2;
    	Point center1 = this.getCenterPoint();
    	Point center2 = otherBall.getCenterPoint();
    	r1 = this.getRadius();   
    	r2 = otherBall.getRadius();
    	double distance = world.getWorldDistance( center1, center2 );
		if ( distance < ( r1 + r2 ) ) {
			// collision!!
			double angle = PhysicsHelper.calcAngleDegrees(center1, center2); //Math.atan(dy/dx);
			double intrusion = (r1+r2) - distance;
			
			// need to do an equal velocity at the opposite angle
			double newAngle = -angle;

			newAngle = Math.toRadians(newAngle);
			double xcomp = Math.cos(newAngle)* Math.abs(intrusion);
			double ycomp = -Math.sin(newAngle)* Math.abs(intrusion);  // negative cause on screen, down is positive.

			ForceVector fv = PhysicsHelper.pushForce(center1, center2, intrusion, PhysicsHelper.AWAY);
			this.applyForce( fv );
			
		}    	
    }
    
    
    public Point getLocation() {
    	return worldLocation;
    }
    
    public void setCenterPoint( Point newCenterPoint ) {
    	setLocation( new Point( (int)(newCenterPoint.x-radius), (int)(newCenterPoint.y-radius)));
    }    
    
    public void setLocation( Point newWorldLocation ) {
    	worldLocation = newWorldLocation;
    	screenLocation = world.worldToScreen(newWorldLocation);
    	mDrawable.setBounds(screenLocation.x, screenLocation.y, 
    			(int)Math.floor(screenLocation.x + world.worldToScreenX(width)), 
    			(int)Math.ceil(screenLocation.y + world.worldToScreenY(height)));
    	myLastBounds = new Rect(screenLocation.x, screenLocation.y, 
    			(int)Math.floor(screenLocation.x + world.worldToScreenX(width)), 
    			(int)Math.ceil(screenLocation.y + world.worldToScreenY(height)));

    	previousLocations.push(newWorldLocation); 
    	//Log.v("BMR", "BALL: world: " + worldLocation + "/ screen: " + screenLocation);
    	//invalidate();
    }
    
    // same as setLocation but doesn't add to history (actually steps back through history)
    public void rewindLocation() {
    	worldLocation = previousLocations.pop();
    	screenLocation = world.worldToScreen(worldLocation);
    	mDrawable.setBounds(screenLocation.x, screenLocation.y, 
    			(int)Math.floor(screenLocation.x + world.worldToScreenX(width)), 
    			(int)Math.ceil(screenLocation.y + world.worldToScreenY(height)));
    }
    
    public double getVelocityX() {
    	return vel.x;
    }
    
    public double getVelocityY() {
    	return vel.y;
    }    
    
    public void setVelocity( double x, double y ) {
    	vel = new ForceVector(x, y);
    }
    
    

 // events when touching the screen
     public boolean onTouchEvent(MotionEvent event) {
    	 
    	 if ( disableTouch ) {
    		 return true;  // disable touch for the moment...
    	 }
    	 
    	 
         int eventaction = event.getAction(); 
         
         int X = (int)world.screenToWorldX(event.getX())-((int)this.width/2); 
         int Y = (int)world.screenToWorldY(event.getY())-((int)this.height/2);
         double ballRadius = PhysicsBall.this.radius;
         
         //Log.v("BMR", "BMR | x: " + X + " y:" + Y );
         
         
         
         // stop drag at left/right walls -- these work
         if ( X < 0 ) {
        	 X = 0;
         } else if ( X > (world.getWorldWidth() - ballRadius*2 ) ) {
        	 X = (int)Math.floor(world.getWorldWidth() - ballRadius*2 );
         }
         if ( Y < 0 ) {  // can't drag above ceiling -- this works
        	 Y = 0;
         }
         double yFloor = world.getFloor() - PhysicsBall.this.width;
         if( Y > yFloor ) {
        	 Y = (int)Math.ceil( yFloor );
         }
         
         
/*         // check against any platforms
         ArrayList<Platform> platforms = world.getPlatforms();
         Iterator<Platform> i = platforms.iterator();
         while ( i.hasNext() ) {
        	 Platform p = i.next();
        	 int collisionType = p.intersects(this);
        	 if ( collisionType != Platform.NO_COLLISION ) {
        		 this.doWallBounce( p, collisionType );
        	 }
         }*/
         
         

         switch (eventaction ) { 

         case MotionEvent.ACTION_DOWN: // touch down so check if the finger is on a ball
         case MotionEvent.ACTION_MOVE:   // touch drag with the ball
         	// move the balls the same as the finger
        	  this.setLocation( new Point(X,Y));
        	  this.setVelocity(0,0);
        	  this.beingHeld = true;
              break; 

         case MotionEvent.ACTION_UP: 
        		// touch drop - just do things here after dropping
        	 this.beingHeld = false;
        	 
        	 // this is the hacked-up fling code.  look for a reasonably different point
        	 // in the touch history and compute a vector from there.
             final int historySize = event.getHistorySize();
             if ( historySize > 2 ) {
	             float x2 = event.getHistoricalX(historySize-1);
	             float x1 = event.getHistoricalX(historySize-2);
	             float y2 = event.getHistoricalY(historySize-1);
	             float y1 = event.getHistoricalY(historySize-2);  
	             x2 = (int)Math.floor(world.worldToScreenX((double)x2));
	             x1 = (int)Math.floor(world.worldToScreenX((double)x1));
	             y2 = (int)Math.floor(world.worldToScreenY((double)y2));
	             y1 = (int)Math.floor(world.worldToScreenY((double)y1));
             }
             
             if ( previousLocations.size() > 10 ) {
	             previousLocations.pop();
	             previousLocations.pop();
	             previousLocations.pop();
	             previousLocations.pop();
	             previousLocations.pop();
	             Point earlierPoint = previousLocations.pop();
	             int xdiff = X-earlierPoint.x;
	             int ydiff = Y-earlierPoint.y;
	             applyForce( new ForceVector( (xdiff)/mass, (ydiff)/mass ));  // divide by two just to slow it down..
	                                                                				// that's actually pretty good!
	             previousLocations.clear();
             }
             //invalidate();
        	 
             //System.out.printf("At time %d:", event.getEventTime());
             //for (int p = 0; p < pointerCount; p++) {
              //   System.out.printf("  pointer %d: (%f,%f)",
               //      ev.getPointerId(p), ev.getX(p), ev.getY(p));
             //}
//
              break; 
         } 
         // redraw the canvas
         return true; 
 	
     }
     
     public Point getCenterPoint() {
    	 return new Point( (int)Math.floor(worldLocation.x + radius) ,
    			 		   (int)Math.floor(worldLocation.y + radius) );
     }
    


/*	public PhysicsBall(Context context) {
        super(context);

        int x = 10;
        int y = 10;
        int width = 300;
        int height = 50;

        mDrawable = new ShapeDrawable(new OvalShape());
        mDrawable.getPaint().setColor(0xff74AC23);
        mDrawable.setBounds(x, y, x + width, y + height);
    }*/

    protected void onDraw(Canvas canvas) {
    	//super.onDraw(canvas);
        mDrawable.draw(canvas);
        
    	Paint p = new Paint();
    	p.setColor(ballColor);
    	canvas.drawCircle(300,300, 50, p);

	        
    }
    
    public double getMass() {
		return mass;
	}

	public void setMass(double mass) {
		this.mass = mass;
	}    



    public boolean contains(Point p) {
    	//Log.v( "BMR", "BMR | world distance: " + world.getWorldDistance(p, this.getCenterPoint()) + " / radius: " + this.getRadius());
    	return (world.getWorldDistance(p, this.getCenterPoint() ) <= this.getRadius());
    }



/*
	   *//**
     * @see android.view.View#measure(int, int)
     *//*
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    	super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    	int h = getMeasuredHeight();
    	int w = getMeasuredWidth();
    	setMeasuredDimension(w,h);
    }*/

   


}    
