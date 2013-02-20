package es.uc3m.setichat.utils;

import java.io.IOException;
import java.io.StringReader;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

public class XMLParser {


	
	public XMLParser(){
		
		
		
		
	}
	
	
	public  XmlPullParser getParser(){
		
		
		XmlPullParser xpp=null;
		XmlPullParserFactory factory=null;
		try {
			factory = XmlPullParserFactory.newInstance();
		} catch (XmlPullParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		factory.setNamespaceAware(true);
		
		try {
			xpp = factory.newPullParser();
		} catch (XmlPullParserException e) {
			
			e.printStackTrace();
		}
        
		
		return xpp;
	}
	

	
	
public String getTagValue(String XMLMessage, String Tag){
	String value=null;
	int eventType=0;
	boolean found=false;
	XmlPullParser xpp=getParser();
	
	try {
		xpp.setInput(new StringReader(XMLMessage));
	
	
     while (eventType != XmlPullParser.END_DOCUMENT) {
      if(eventType == XmlPullParser.START_DOCUMENT) {
    	  
      } else if(eventType == XmlPullParser.START_TAG) {
          if(xpp.getName().equals(Tag)){
        	 found=true;
        	  
          }
      } else if(eventType == XmlPullParser.END_TAG) {
    	  
      } else if(eventType == XmlPullParser.TEXT) {
    	  
    	 if (found) value=xpp.getText();
    	 found=false;
      
      }
      eventType = xpp.next();
     }
	}catch(XmlPullParserException e){
		
		e.printStackTrace();
		
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
return value;
}
	
	
	
	
	

	

}
