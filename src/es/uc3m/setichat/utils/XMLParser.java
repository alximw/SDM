package es.uc3m.setichat.utils;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

public class XMLParser {


	
	public XMLParser(){
		
		
		
		
	}
	
	
	public static XmlPullParser getParser(){
		
		
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
	

}
