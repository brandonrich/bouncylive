package com.infinitedonuts.bouncylive;



import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Handler;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

import com.infinitedonuts.palette.ColourLoversPalette;

/*
 * This animated wallpaper draws a rotating wireframe cube.
 */
public class BouncyLive extends WallpaperService {
	
	public static final String SHARED_PREFS_NAME = "bouncylivesettings";	
    private final Handler mHandler = new Handler();
    private Context mContext = null;
  
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }
    


	@Override
    public void onCreate() {
        super.onCreate();
        //android.os.Debug.waitForDebugger();
        Log.v("BMR", "BMR | wallpaper service onCreate");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v("BMR", "BMR | wallpaper service onDestroy");
    }

    @Override
    public Engine onCreateEngine() {
    	mContext = this.getApplicationContext();
    	Log.v("BMR", "BMR | wallpaper service onCreateEngine");
        return new BouncyEngine(mContext);
    }
    
    class BouncyEngine extends Engine
    implements SharedPreferences.OnSharedPreferenceChangeListener {
    	
    	boolean mVisible = true;
    	PhysicsWorld world = null;
    	SharedPreferences mPrefs = null;
    	Context mContext;
    	Resources mRes;
    	String [] paletteIDs;
    	String [] colorCodes;
    	
    	// default values for preferences.  might change in response to device capabilities.
    	String defaultAutoChangeIndex = "0";
    	int defaultBGColorIndex = 0;
    	int defaultPaletteIndex = 0;
    	int defaultGravityValue = 30;
    	boolean defaultTiltValue = false;
    	String defaultGravityStateIndex = "0";    	
    	int defaultBallCount = 14;
    	
        private final Runnable mRun = new Runnable() {
            public void run() {
                drawFrame();
            }
        };    	

        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);
            
            paletteIDs = mContext.getResources().getStringArray(R.array.paletteIDs);
            colorCodes = mContext.getResources().getStringArray(R.array.colorCodes);
            Log.v("BMR", "BMR | engine onCreate");

            // By default we don't get touch events, so enable them.
            setTouchEventsEnabled(true);
            
        }
    	
        @Override
        public void onDestroy() {
            super.onDestroy();
            Log.v("BMR", "BMR | engine onDestroy");
            mHandler.removeCallbacks(mRun);
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
        	Log.v("BMR", "BMR | engine visibility changed: " + visible );
            mVisible = visible;
            if (visible) {
                drawFrame();
            } else {
                mHandler.removeCallbacks(mRun);
            }
        }        
    	
		public BouncyEngine(Context c) {
			super();
			// TODO Auto-generated constructor stub
			world = new PhysicsWorld( c, null );
			mContext = c;
			mRes = c.getResources();
			
			mPrefs = BouncyLive.this.getSharedPreferences(SHARED_PREFS_NAME, 0);
            mPrefs.registerOnSharedPreferenceChangeListener(this);
            onSharedPreferenceChanged(mPrefs, null);			
			
			Log.v("BMR", "BMR | engine constructor");
		}
		
        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);
            // store the center of the surface, so we can draw the cube in the right spot
            Configuration config = getResources().getConfiguration();
            world.setCoordinateSystem(config.orientation);
            drawFrame();
            Log.v("BMR", "BMR | engine surfacechanged");
        }

        @Override
        public void onSurfaceCreated(SurfaceHolder holder) {
            super.onSurfaceCreated(holder);
            Log.v("BMR", "BMR | engine surface created");
        }

        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            super.onSurfaceDestroyed(holder);
            mVisible = false;
            mHandler.removeCallbacks(mRun);
            mPrefs.unregisterOnSharedPreferenceChangeListener(this);
            Log.v("BMR", "BMR | engine onSurfaceDestroyed");
        }		
        
        @Override
        public void onOffsetsChanged(float xOffset, float yOffset,
                float xStep, float yStep, int xPixels, int yPixels) {
            drawFrame();
            Log.v("BMR", "BMR | engine offsets changed");
        }

        /*
         * Store the position of the touch event so we can use it for drawing later
         */
        @Override
        public void onTouchEvent(MotionEvent event) {
        	world.onTouchEvent(event);
        	super.onTouchEvent(event);
        }

        /*
         * Draw one frame of the animation. This method gets called repeatedly
         * by posting a delayed Runnable. You can do any drawing you want in
         * here. This example draws a wireframe cube.
         */
        void drawFrame() {
            final SurfaceHolder holder = getSurfaceHolder();

           Canvas c = null;
            try {
                c = holder.lockCanvas();
                if (c != null) {
                	c.drawColor(0xffffffff);  // clears the canvas with white
                	world.updateWorld();
                	world.onDraw(c);

                }
            } finally {
            	try {
            		if (c != null) holder.unlockCanvasAndPost(c);
            	} catch ( Exception e ) {
            		int x = 0;   // do nothing
            	}
            }
           

            
            // Reschedule the next redraw
            mHandler.removeCallbacks(mRun);
            if (mVisible) {
                mHandler.postDelayed(mRun, 5);
            }
        }

		@Override
		public void onSharedPreferenceChanged(
				SharedPreferences sharedPreferences, String key) {
			
/*			Map m = sharedPreferences.getAll();
			Editor e = sharedPreferences.edit();
			e.clear();  // TEMP!!
			e.commit();  // TEMP!!
			Map m2 = sharedPreferences.getAll();
*/
		
			Resources res = mContext.getResources();
			
			String gravityState = sharedPreferences.getString(res.getString(R.string.setting_key_gravitystate), defaultGravityStateIndex);
			if ( gravityState.compareTo("0") == 0 ) {
				world.setGravity(false);
			} else {
				world.setGravity(true);
				world.reverseGravity(false);
				if ( gravityState.compareTo("-1") == 0 ) {
					world.reverseGravity(true);
				}
			}
			//Log.v("BMR", "BMR | gravity is " + gravityState );
			
			Boolean tiltOn = sharedPreferences.getBoolean(res.getString(R.string.setting_key_tilt), defaultTiltValue);
			world.doTiltControl(tiltOn);
			//Log.v("BMR", "BMR | tiltOn is " + tiltOn);			
			
			int gravityValue = sharedPreferences.getInt(res.getString(R.string.setting_key_gravitymagnitude), defaultGravityValue);
			world.setGravityMagnitude( gravityValue );
			
			
			String key2 = res.getString(R.string.setting_key_palette);
			int paletteIndex = sharedPreferences.getInt(key2, defaultPaletteIndex );//ColourLoversPalette.RANDOM);
			if ( paletteIndex != ColourLoversPalette.RANDOM ) {
				if ( paletteIDs == null ) {
					paletteIDs = mRes.getStringArray(R.array.paletteIDs);
				}
				Log.v("BMR", "BMR | palette ID selected: " + Integer.parseInt(paletteIDs[paletteIndex]) );
				int currentWorldPaletteID = world.getCurrentPaletteID();
				int proposedPaletteID = Integer.parseInt(paletteIDs[paletteIndex]);
				if ( proposedPaletteID != currentWorldPaletteID ) {
					world.applyPalette(Integer.parseInt(paletteIDs[paletteIndex]));
				}
			} else {
				if ( world.getCurrentPaletteID() != ColourLoversPalette.RANDOM ) {
					world.applyPalette(ColourLoversPalette.RANDOM);
					Log.v("BMR", "BMR | Random palette it is.");
				}
			}
			
			
			int ballCount = sharedPreferences.getInt(res.getString(R.string.setting_key_ballcount), defaultBallCount);
			if ( ballCount != world.getBallCount() ) {
				world.setBallCount( ballCount );
				world.generateBouncyBalls(ballCount, ColourLoversPalette.NO_CHANGE );	
				
			}
			
			
			int bgcolorIndex = sharedPreferences.getInt(res.getString(R.string.setting_key_backgroundcolor), defaultBGColorIndex );
			if ( colorCodes == null ) {
				colorCodes = mRes.getStringArray(R.array.colorCodes);
			}
			String newColorHex = colorCodes[bgcolorIndex];
			int newColor = Color.parseColor("#" + newColorHex );
			if ( newColor != world.getBackgroundColor() ) {
				world.setBackgroundColor(newColorHex);
			}
			
			
			
			
			int autoChangeOptionIndex = Integer.parseInt(sharedPreferences.getString(res.getString(R.string.setting_key_autochange), defaultAutoChangeIndex ));
			boolean autoChangeOn = world.getAutoChangePaletteSetting();
			if ( 
					( autoChangeOptionIndex == PhysicsWorld.NEVER_OPTION && autoChangeOn ) ||
					( autoChangeOptionIndex != PhysicsWorld.NEVER_OPTION && !autoChangeOn )
			   ){
				world.setAutoChangePalette(!(autoChangeOptionIndex == PhysicsWorld.NEVER_OPTION));
				world.setAutoChangeInterval(autoChangeOptionIndex);
			}
		}
        
    	
    }


    
}
