package com.infinitedonuts.palette;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.util.Log;
import android.util.Xml;

import com.infinitedonuts.bouncylive.R;

// creates an array of colors from the provided colorlovers palette ID
public class ColourLoversPalette extends DefaultHandler {

	private String apiURL;
	private String paletteID;
	private String creatorID;
	private String paletteDirectURL;
	private String badgeURL;
	private String paletteName;
    private ArrayList<Integer> colors = new ArrayList();
    private StringBuilder builder;
    public static final int RANDOM = -1;
    public static final int DEFAULT_PALETTE = 555;
    public static final int NO_CHANGE = -2;

    
    
   
    
	public ColourLoversPalette() {
		super();
		this.colors = new ArrayList<Integer>();
	}
    
	public ColourLoversPalette(Context context, int paletteID) {
		super();
		
		Resources res = context.getResources();

		// check to see if we have it local
		if ( paletteID == ColourLoversPalette.RANDOM ) {
			this.apiURL = "http://www.colourlovers.com/api/palettes/random";
		} else {
			this.apiURL = "http://www.colourlovers.com/api/palette/" + paletteID + "?showPaletteWidths=1";
		}
		this.colors = new ArrayList<Integer>();

	
		String xmlString = "";
		if ( paletteID != DEFAULT_PALETTE ) {
			
			// do we have the xml local?  how to tell?
			int fileID = res.getIdentifier( "x" + String.valueOf(paletteID), "raw", context.getPackageName() );
			if ( fileID != 0 ) {
				StringBuffer fileData = new StringBuffer(10000);
		        InputStream is = res.openRawResource(fileID);
		    	BufferedReader r = new BufferedReader(new InputStreamReader(is));
		    	String line;
		    	try {
		    		while ((line = r.readLine()) != null) {
		    			fileData.append(line);
		    	    }   	    
			    	xmlString = fileData.toString();
		    	} catch (Exception e ) {
		    		xmlString = "";
		    		//Log.v("BMR", "BMR | error reading xml: " + e.getMessage() );
		    	}
			} else {
				// fetch from ColourLovers API
				RestClient rc = new RestClient(apiURL);
				try {
				    rc.Execute(RestClient.GET);
				} catch (Exception e) {
				    e.printStackTrace();
				}
				xmlString = rc.getResponse();	
			}
			 
			 	
	        try {
	            Xml.parse(xmlString, this);
	        } catch (Exception e) {
	            throw new RuntimeException(e);
	        }
		} else {
			// default palette
	        colors.add((int)res.getColor(R.color.red));  // make default work later
	        colors.add((int)res.getColor(R.color.yellow));
	        colors.add((int)res.getColor(R.color.physicsBallGreen));
	        colors.add((int)res.getColor(R.color.lightBlue));
	        colors.add((int)res.getColor(R.color.purple));
	        colors.add((int)res.getColor(R.color.orange));			
		}
    

	}
	
	public void addColor( int hexcode ) {
		colors.add(hexcode);
	}
	
	public ArrayList<Integer> getColors() {
		return colors;
	}

	
    @Override
    public void characters(char[] ch, int start, int length)
            throws SAXException {
        super.characters(ch, start, length);
        builder.append(ch, start, length);
    }

    @Override
    public void endElement(String uri, String localName, String name)
            throws SAXException {
        super.endElement(uri, localName, name);
        
        // get color hex values
        if (localName.equalsIgnoreCase("hex")){
        	int c = Color.parseColor("#" + builder.toString().trim() );
        	this.addColor(c);
        }
        if (localName.equalsIgnoreCase("id")){
        	paletteID = builder.toString().trim();
        } 
        if (localName.equalsIgnoreCase("userName")){
        	this.creatorID = builder.toString().trim();
        }      
        if (localName.equalsIgnoreCase("title")){
        	this.paletteName = builder.toString().trim();
        }      
        if (localName.equalsIgnoreCase("badgeUrl")){
        	this.badgeURL = builder.toString().trim();
        }        
        
/*		RestClient rc = new RestClient(badgeURL);
		try {
		    rc.Execute(RestClient.GET);
		} catch (Exception e) {
		    e.printStackTrace();
		}
		String badgePNGRaw = rc.getResponse();*/
		
        builder.setLength(0);
        int x = 0;
    }

    @Override
    public void startDocument() throws SAXException {
        super.startDocument();
        builder = new StringBuilder();
        int x = 0;
    }

    @Override
    public void startElement(String uri, String localName, String name,
            Attributes attributes) throws SAXException {
        super.startElement(uri, localName, name, attributes);
        int x = 0;
    }	

 
    
	
}
