package com.infinitedonuts.bouncylive;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Random;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;

import com.infinitedonuts.palette.ColourLoversPalette;

public class PhysicsWorld extends View {



	// test object parameters
	private boolean enableSlingShot = false;
	private boolean enablePlatformTest = false;
	
	int currentPaletteID;
	
	public int getCurrentPaletteID() {
		return currentPaletteID;
	}

	String[] paletteIDs;

	private int gravityDirection = 1;
	
	private Resources res;
	
	// initialization parameters
	private int ballCount = 16;
	public int getBallCount() {
		return ballCount;
	}

	public void setBallCount(int ballCount) {
		this.ballCount = ballCount;
	}
	
	public static final int NEVER_OPTION = 0;
	public static final int MINUTE_OPTION = 1;
	public static final int HOUR_OPTION = 2;
	public static final int DAY_OPTION = 3;
	public static final int WEEK_OPTION = 4;	
	
	private static final long MINUTE = 60000;
	private static final long HOUR = 3600000;
	private static final long DAY = 86400000;
	private static final long WEEK = 604800000;
	
	private boolean autoChangePalette = true;
	public boolean getAutoChangePaletteSetting() {
		return autoChangePalette;
	}

	public void setAutoChangePalette(boolean autoChangePalette) {
		this.autoChangePalette = autoChangePalette;
		this.lastAutoChange = this.roundDateDown(new Date(), this.autoChangeInterval);
	}

	private long autoChangeInterval = MINUTE;
	public void setAutoChangeInterval(int intervalOptionIndex) {
		this.autoChangePalette = true;
		switch(intervalOptionIndex) {
		case NEVER_OPTION:
			this.autoChangePalette = false;
			break;
		case MINUTE_OPTION: 
			this.autoChangeInterval = MINUTE;
			break;
		case HOUR_OPTION: 
			this.autoChangeInterval = HOUR;
			break;			
		case DAY_OPTION: 
			this.autoChangeInterval = DAY;
			break;
		case WEEK_OPTION: 
			this.autoChangeInterval = WEEK;
			break;
		}
		this.lastAutoChange = this.roundDateDown(new Date(), this.autoChangeInterval);
	}

	private Date lastAutoChange;
	
	
	ColourLoversPalette currentPalette;
	SharedPreferences globalSharedPreferences;
	
	private int minBallSize = 60;
	private int maxBallSize = 110;

	// test environment parameters
	private boolean enableAccelerometerGravity = true;	
	private boolean enableGravity = false;
	private static final boolean enableCollisionDetection = true;
	private static final boolean enableCollisionResponse = true;
	private static final boolean enableBorders = true;
	
	
	private String bgcolorHex = "FFFFFF";
	private int bgcolor;
	
	public void setBackgroundColor( String hexCode ) {
		bgcolorHex = hexCode;
		bgcolor = Color.parseColor("#" + hexCode );
	}
	
	public String getBackgroundColorHex() {
		return this.bgcolorHex;
	}
	
	public int getBackgroundColor() {
		return bgcolor;
	}

	// environmental variables
	private static final int TERMINAL_VELOCITY = 275;
	private double GRAVITY_ACCELERATION = 3;
	
	private static final int UPDATE_DELAY = 5;
	public static final int STATUS_BAR_HEIGHT = 85;
	
	// make a setting for the live wallpaper
	private double accelerometer_gravity_factor = 0.4;  // use for accelerometer-based stuff.  I think 1 is the max I want.
	




	//private CannonBall cannonBall = null;
	private PhysicsBall mBall = null;
	private Display mDisplay = null;	
	private Handler handler = null;  // game loop
	private ArrayList<PhysicsBall> allSprites = null;
	//private ArrayList<Platform> allPlatforms = null;

	// world scale parameters.  I am not defining a static pixel scale
	// because we may have lots of different pixel densities.  Rather,
	// I'll define static dimensions for the world.
	private int worldHeight = 1600; // meters
	private int worldWidth = 1000; // meters
	private int screenHeight, screenWidth;	
	
	private Context mContext;
	
	
	// accelerometer variables
    private Sensor mAccelerometer;
    private float mSensorX;
    private float mSensorY;	
    private long mSensorTimeStamp;
    private long mCpuTimeStamp;      

    public void setCoordinateSystem( int orientation ) {
    	switch( orientation ) {
	    	case Configuration.ORIENTATION_LANDSCAPE:
	    		worldWidth = 1600; // meters
	    		worldHeight = 1000; // meters 		
	    		break;
	    	case Configuration.ORIENTATION_PORTRAIT:
	    		worldHeight = 1600; // meters
	    		worldWidth = 1000; // meters  	    		
	    		break;
    	}

    	DisplayMetrics dm = new DisplayMetrics();
		WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
		wm.getDefaultDisplay().getMetrics(dm);
		mDisplay = wm.getDefaultDisplay();
		screenWidth = dm.widthPixels;
		screenHeight = dm.heightPixels;   
		
/*		switch (mDisplay.getOrientation()) {
        case Surface.ROTATION_0:
            mSensorX = event.values[0];
            mSensorY = event.values[1];
            break;
        case Surface.ROTATION_90:
            mSensorX = -event.values[1];
            mSensorY = event.values[0];
            break;
        case Surface.ROTATION_180:
            mSensorX = -event.values[0];
            mSensorY = -event.values[1];
            break;
        case Surface.ROTATION_270:
        	break;
		}*/
    }
    
	public PhysicsWorld(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		DisplayMetrics dm = new DisplayMetrics();
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		wm.getDefaultDisplay().getMetrics(dm);
		screenWidth = dm.widthPixels;
		screenHeight = dm.heightPixels;
		
		
		
        //if ( enableAccelerometerGravity ) {
        	
            mDisplay = wm.getDefaultDisplay(); 	
        	
	        // Locate the SensorManager using Activity.getSystemService
	        SensorManager sm = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
	         
	        // adapted from accelerometer play sample
	        mAccelerometer = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
	        sm.registerListener(accelerometerListener, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
	        
	        // Register your SensorListener
	        //sm.registerListener(sensorListener, sm.getDefaultSensor(SensorManager.SENSOR_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        //}		
		
	    lastAutoChange = roundDateDown( new Date(), this.autoChangeInterval );
		allSprites = new ArrayList<PhysicsBall>();
		mContext = context;
		res = mContext.getResources();
		
		paletteIDs = res.getStringArray(R.array.paletteIDs);
		
		currentPaletteID = ColourLoversPalette.DEFAULT_PALETTE;
		generateBouncyBalls(ballCount, ColourLoversPalette.DEFAULT_PALETTE);
		
		
//		cannonBall = new CannonBall( context, this, new Point(500, 1100));
		
		
		handler = new Handler();
		
		globalSharedPreferences = mContext.getSharedPreferences("bouncylivesettings", 0);
		//globalSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext); 
		//handler.postDelayed(r, UPDATE_DELAY);
		
	}
	
	
	
	public PhysicsWorld(Context context) {
		super(context);
		mContext = context;
		res = mContext.getResources();
		lastAutoChange = roundDateDown( new Date(), this.autoChangeInterval );
		currentPaletteID = ColourLoversPalette.DEFAULT_PALETTE;
		paletteIDs = res.getStringArray(R.array.paletteIDs);
		globalSharedPreferences = mContext.getSharedPreferences("bouncylivesettings", 0);
	}  
	
	
	public void applyPalette( int paletteID ) {
		generateBouncyBalls( this.ballCount, paletteID );
	}
	
	
	public void generateBouncyBalls(int ballCount, int paletteID) {
		
		allSprites.clear();

        ArrayList<Integer> colors = new ArrayList();
        if ( paletteID != ColourLoversPalette.NO_CHANGE ) {
        	this.currentPaletteID = paletteID;
        	currentPalette = new ColourLoversPalette(this.mContext, paletteID);  //empty one // ColourLoversPalette.RANDOM); //1651922);
        }
        colors = currentPalette.getColors();

		Random generator = new Random();
		int randomIndex = generator.nextInt( colors.size() ); // random color generator
		int startingX = 100 * generator.nextInt( 7 ) + 1;		
		int startingY = 100 * generator.nextInt( 4 ) + 1;
		float randomSize = (float)generator.nextInt( maxBallSize-minBallSize ) + minBallSize;
				
		int nextColor;
		// generate balls
		for ( int i = 0; i < ballCount; i++ ) {	
			nextColor = colors.get(i % colors.size());
			mBall = new PhysicsBall(mContext, this, startingX, startingY, nextColor, randomSize ); //(int)res.getColor(colors.get(randomIndex)), randomSize);
			allSprites.add(mBall);
			
			randomIndex = generator.nextInt( colors.size() );
			randomSize = (float)generator.nextInt( maxBallSize-minBallSize ) + minBallSize;		
			startingY = 100 * generator.nextInt( 4 ) + 1;
			startingX = 100 * generator.nextInt( 7 ) + 1;
		}
				
		this.invalidate();		
		
	}
		
	
	
	public void updateWorld() {
		/*        	Iterator<Platform> pi = allPlatforms.iterator();
    	Platform plat = null;
    	while( pi.hasNext() ) {
    		plat = (Platform)pi.next();
    		plat.setColor(R.color.black);
    	}     */   	
    	
    	if ( autoChangePalette ) {
    		doAutoChangePalette();
    	}		
		
    	PhysicsBall ball = null;
    	Iterator<PhysicsBall> i = allSprites.iterator();
    	while ( i.hasNext() ) {
    		ball = (PhysicsBall)i.next();
    		if ( enableGravity ) {
    			doGravity(ball);
    		}
    		if ( enableBorders ) {
    			doBorders(ball);      
    		}
    		if ( enableCollisionDetection ) {
    			doCollisions( ball );
    		}
    	}        	
    	
    	// update ball positions
    	i = allSprites.iterator();
    	while ( i.hasNext() ) {
    		ball = (PhysicsBall)i.next();
    		ball.update();
    	}
/*        	
    	if ( enableSlingShot ) {
    		cannonBall.update();
    	}*/
    	
    	PhysicsWorld.this.invalidate();		
	}
	

    
    final Runnable r = new Runnable()
    {
        public void run() 
        {

        	updateWorld();
        	
            handler.postDelayed(this, UPDATE_DELAY);
        }
    };

    
    private Date roundDateDown( Date d, long interval ) {
		long newTime = (d.getTime() / interval)*interval;  // integer division should drop off the remainder
		Date newDate = new Date(newTime);
		//Log.v("BMR", "BMR | roundDateDown " + DateFormat.getDateTimeInstance(
	      //      DateFormat.LONG, DateFormat.LONG).format(d) + " --> " + DateFormat.getDateTimeInstance(
	    	//            DateFormat.LONG, DateFormat.LONG).format(newDate) );
		
		return newDate;
	}    
    
    public void doAutoChangePalette() {
    	// figure out if it's time for a palette change
    	Date now = new Date();
    	long elapsed = now.getTime() - lastAutoChange.getTime();
    	int randomPaletteID;
    	int randomIndex;
    	if ( elapsed > this.autoChangeInterval ) {
    		
    		// select a random palette and apply
    		Random generator = new Random();
    		randomIndex = generator.nextInt( paletteIDs.length ); // random color generator
    		randomPaletteID = Integer.parseInt( paletteIDs[ randomIndex ]);
    		
    		while( randomPaletteID == this.currentPaletteID ) {
    			randomIndex = generator.nextInt( paletteIDs.length ); // random color generator
        		randomPaletteID = Integer.parseInt( paletteIDs[ randomIndex ]);
    		}
    		
    		
    		String key2 = res.getString(R.string.setting_key_palette);
    		Editor e = globalSharedPreferences.edit();
    		e.putInt(key2, randomIndex);
    		e.commit();  // this fires the onSharedPreferencesChanged listener in the engine object, so it both persists the newly
    				     // selected palette as the default choice but also updates the palette itself (hence no need to applyPalette explicitly)
    		
    		//this.applyPalette( randomPaletteID );
    		//Log.v("BMR", "BMR | randomized palette ");
    		lastAutoChange = roundDateDown( new Date(), this.autoChangeInterval );
    		
    		
    	}
    	
    	
    }
    
    @Override
	public boolean onTouchEvent(MotionEvent event) {

    	// TODO find the ball under this touch event.
    	if ( mBall != null ) {
    		mBall.onTouchEvent(event);
    	}
    	this.invalidate();
		return true;
	}
	public Point worldToScreen( Point worldPoint ) {
    	Point resultPoint = new Point();
    	resultPoint.x = (int)Math.floor(worldToScreenX( worldPoint.x ));
    	resultPoint.y = (int)Math.floor(worldToScreenY( worldPoint.y ));
    	return resultPoint;
    }
    
    public Point screenToWorld( Point screenPoint ) {
    	Point resultPoint = new Point();
    	resultPoint.x = (int)Math.floor(screenToWorldX( screenPoint.x ));
    	resultPoint.y = (int)Math.floor(screenToWorldY( screenPoint.y ));
    	return resultPoint;
    }
    
    public double worldToScreenX( double worldXvalue ) {
    	return ( worldXvalue / worldWidth ) * screenWidth;
    }
    public double worldToScreenY( double worldYvalue ) {
    	return ( worldYvalue / worldHeight ) * screenHeight;
    }    
    
    
    public double screenToWorldX( double screenXvalue ) {
    	return ( screenXvalue / screenWidth ) * worldWidth;
    }
    public double screenToWorldY( double screenYvalue ) {
    	return ( screenYvalue / screenHeight ) * worldHeight;
    }
    
    public double getFloor() {
    	return this.getWorldHeight() - this.screenToWorldY(PhysicsWorld.STATUS_BAR_HEIGHT);	
    }
    
    public void doGravity( PhysicsBall ball ) {
    	double yvel = ball.getVelocityY();
		if (yvel < TERMINAL_VELOCITY ) {
			// do gravity
			// apply at an angle of 270 (this should allow me to vary the angle later and
			//                           affect gravity via tilt)
			if ( enableAccelerometerGravity ) {
    			//int angleDegrees = 270;
    			//double test = Math.cos(Math.toRadians(angleDegrees));
    			//double xgrav = GRAVITY_ACCELERATION * Math.cos(Math.toRadians(angleDegrees));
    			//double ygrav = GRAVITY_ACCELERATION * Math.sin(Math.toRadians(angleDegrees)) * -1;  // -1 because higher y coordinates are lower on the device
				//Log.v("BMR", "BMR | tilt gravity: " + accelerometer_gravity_factor);
    			ball.applyForce( new ForceVector( -mSensorX*accelerometer_gravity_factor*gravityDirection, mSensorY*accelerometer_gravity_factor*gravityDirection ));
			} else {
				ball.applyForce( new ForceVector( 0, GRAVITY_ACCELERATION * gravityDirection ));
			}
		}    	
    }
    
    
    public void doCollisions( PhysicsBall ball ) {
    	
    	// check for collisions with other circles
    	Iterator<PhysicsBall> j = allSprites.iterator();
    	PhysicsBall otherBall = null;
    	
    	while ( j.hasNext() ) {
    		otherBall = (PhysicsBall)j.next();
    		if ( ball != otherBall ) {
    			ball.avoid(otherBall);
    		}
    	}

/*    	Iterator<Platform> p = allPlatforms.iterator();
    	Platform plat = null;
    	while( p.hasNext() ) {
    		plat = (Platform)p.next();
    		int collisionType = plat.intersects(ball);
    		if ( collisionType != Platform.NO_COLLISION ) {

    			plat.setColor(R.color.red);
    			if ( enableCollisionResponse ) {
    				ball.doWallBounce(plat, collisionType);
    			}
    		} else {
    			plat.setColor(R.color.black);
    		}
    	}*/

    }
    
    public double getWorldDistance( Point p1, Point p2 ) {
    	int x1 = p1.x;
    	int x2 = p2.x;
    	int y1 = p1.y;
    	int y2 = p2.y;
		double dy = (y2-y1);
		double dx = (x2-x1);
		return Math.sqrt(dx*dx + dy*dy);    	
    }
    

   
    
    public void doBorders( PhysicsBall ball ) {
		// apply gravity and such
		Point p = ball.getLocation();
		double radius = ball.getRadius();
		int width = (int)Math.floor(radius*2);
		double yvel = ball.getVelocityY();
		double xvel = ball.getVelocityX();    	
    	
		// floor collision + bounce
		double worldStatusBarHeight = PhysicsWorld.this.screenToWorldY(STATUS_BAR_HEIGHT);
		boolean onFloor = ( p.y + yvel ) > (worldHeight-width-worldStatusBarHeight);
		boolean onCeiling = (( p.y + yvel ) < 0 );
		if ( onFloor ) {
			ball.doWallBounce( null, Platform.TOP_LINE );
		}
		if ( onCeiling ) {
			ball.doWallBounce( null, Platform.BOTTOM_LINE );
		}

    	// wall collisions + bounce
    	int newXLocation = (int)(p.x + xvel);
    	boolean onRightWall = (newXLocation > (worldWidth-width)); 
    	if ( onRightWall ) {
    		ball.doWallBounce( null, Platform.LEFT_LINE );
    	}    	
    	if (newXLocation < 0){
    		ball.doWallBounce( null, Platform.RIGHT_LINE );
    	}    	
    }
    
	public int getWorldHeight() {
		return worldHeight;
	}

	public int getWorldWidth() {
		return worldWidth;
	}
	
/*	public ArrayList<Platform> getPlatforms() {
		return this.allPlatforms;
	}*/
    
	
	// not working right now...
/*	public void onConfigurationChanged(Configuration newConfig) {
	    super.onConfigurationChanged(newConfig);

    	int shortSide  = this.worldHeight;
    	int longSide = this.worldWidth;
    	if ( this.worldWidth < this.worldHeight ) {
    		shortSide = this.worldWidth;
    		longSide = this.worldHeight;
    	} 
	    
	    // Checks the orientation of the screen
	    if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
	    	this.worldWidth = longSide;
	    	this.worldHeight = shortSide;
	    } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
	        this.worldWidth = shortSide;
	        this.worldHeight = longSide;
	    }

	}*/	

	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		
    	Paint p = new Paint();
    	
    	canvas.drawColor(this.bgcolor);
		
		super.onDraw(canvas);
		
		PhysicsBall ball = null;
		Iterator<PhysicsBall> i = allSprites.iterator();
    	while ( i.hasNext() ) {
    		ball = (PhysicsBall)i.next();
    		ball.draw(canvas);
    		Point center = this.worldToScreen(ball.getCenterPoint());
    		p.setColor(ball.getBallColor());
    		canvas.drawCircle((float)center.x,(float)center.y, 
    				(float)this.worldToScreenX(ball.getRadius()), p);
    	}

		
	}
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// TODO Auto-generated method stub
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    	int h = getMeasuredHeight();
    	int w = getMeasuredWidth();
    	setMeasuredDimension(w,h);		
	}
	

	
	/*public boolean circleIntersectsRectSegment( Point testPoint, double radius, LineSegment line ) {
	
		boolean retval = false;
		

		
		// if it's a vertical line return the horizontal distance
	    if ( line.a.x == line.b.x) { 
	      double xdist = (double)Math.abs(line.a.x - testPoint.x);
	      // assumes point A of line will always be the leftmost one.  enforce in LineSegment constructor
	      if ( xdist <= radius && testPoint.y >= line.a.y && testPoint.y <= line.b.y ) {
	    	  retval = true;
	      } else {
	    	  retval = false;
	      }	      
	    }

	    // if it's a horizontal line return the vertical distance
	    if ( line.a.y == line.b.y )  {
	      double ydist = (double)Math.abs(line.a.y - testPoint.y);
	      if ( ydist <= radius && testPoint.x >= line.a.x && testPoint.x <= line.b.x ) {
	    	  retval = true;
	      } else {
	    	  retval = false;
	      }
	    }
		    
	  	  // check if the distance between the center of the cirlce and either end of the line is less than the radius.
	  	  if ( ( this.getWorldDistance( testPoint, line.a) <= radius ) ||
	  	     ( this.getWorldDistance(testPoint, line.b) <= radius ) ) {
	  		  return true;
	  	  }	    
	    
	    return retval;

	    // not needed for rect, so i'll skip for now.
	    // otherwise, find the slope of the line
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
	    return Math.sqrt(x_dist * x_dist + y_dist * y_dist); 
	    
	    
	}*/
	
	
/*	public boolean intersectsCircle(LineSegment s, PhysicsBall b) {
		return this.circleIntersectsRectSegment( b.getCenterPoint(), b.getRadius(), s );
		double d = distancePointToLine(b.getCenterPoint(), s);
		double ballRadius  = b.getRadius();
	    boolean ans = d <= ballRadius;
	    if ( ans ) {
	    	return true;
	    } else {
	    	return false;
	    }
	}*/

	
	
	
	private final SensorEventListener accelerometerListener = new SensorEventListener() {


		@Override
		public void onAccuracyChanged(Sensor arg0, int arg1) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onSensorChanged(SensorEvent event) {
			if ( PhysicsWorld.this.enableAccelerometerGravity ) {
				if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER)
	                return;		
				
	            switch (mDisplay.getOrientation()) {
	            case Surface.ROTATION_0:
	                mSensorX = event.values[0];
	                mSensorY = event.values[1];
	                break;
	            case Surface.ROTATION_90:
	                mSensorX = -event.values[1];
	                mSensorY = event.values[0];
	                break;
	            case Surface.ROTATION_180:
	                mSensorX = -event.values[0];
	                mSensorY = -event.values[1];
	                break;
	            case Surface.ROTATION_270:
	                mSensorX = event.values[1];
	                mSensorY = -event.values[0];
	                break;
	        }
	            mSensorX /= 10;
	            mSensorY /= 10;
	
	        mSensorTimeStamp = event.timestamp;
	        mCpuTimeStamp = System.nanoTime();
	        
				// TODO Auto-generated method stub
				float pitch = event.values[1];
				//Log.v("BMR", "BMR | pitch1 is " + pitch );
				//Log.v("BMR", "BMR | sensorX is " + mSensorX + " and Y is " + mSensorY );
			}
		}
    };    
    	
   
    public void setGravity( boolean gravityOn ) {
    	this.enableGravity = gravityOn;
    }
    public void reverseGravity( boolean reverse ) {
    	if ( reverse ) {
    		gravityDirection = -1;
    	} else {
    		gravityDirection = 1;
    	}
    }
    
    public void doTiltControl( boolean tiltOn ) {
    	this.enableAccelerometerGravity = tiltOn;
    	//Log.v("BMR", "BMR | enableAccelerometerGravity=" + this.enableAccelerometerGravity);
    }
	
    public void setGravityMagnitude( int percentVal ) {
    	this.GRAVITY_ACCELERATION = (double)percentVal / 30;
    	this.accelerometer_gravity_factor = (double)(percentVal / 40.0);
    }
}
