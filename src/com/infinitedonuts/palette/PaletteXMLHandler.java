package com.infinitedonuts.palette;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.os.Message;

public class PaletteXMLHandler extends DefaultHandler {
	
	
	private ColourLoversPalette c;
    private StringBuilder builder;
    
    public ColourLoversPalette getPalette(){
        return this.c;
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
        if (this.c != null){
            if (localName.equalsIgnoreCase("hex")){
                //currentMessage.setTitle(builder.toString());
            	c.addColor(Integer.parseInt(builder.toString()));
            } 
            builder.setLength(0);    
        }
    }

    @Override
    public void startDocument() throws SAXException {
        super.startDocument();
        //messages = new ArrayList<Message>();
        builder = new StringBuilder();
    }

    @Override
    public void startElement(String uri, String localName, String name,
            Attributes attributes) throws SAXException {
        super.startElement(uri, localName, name, attributes);
        if (localName.equalsIgnoreCase("palette")){
            this.c = new ColourLoversPalette();
        }
    }	
	

}
