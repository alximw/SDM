package es.uc3m.setichat.utils;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import es.uc3m.setichat.contactsHandling.Contact;

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
	

	
	
	
	public String getFirstTagValue(String XMLMessage, String Tag){
		String value=null;
		int eventType=0;
		boolean found=false;
		XmlPullParser xpp=getParser();
		boolean stopCondition=(eventType != XmlPullParser.END_DOCUMENT);
		
		
		try {
			xpp.setInput(new StringReader(XMLMessage));
		
		
	     while (value==null && stopCondition) {
	      if(eventType == XmlPullParser.START_DOCUMENT) {
	    	  
	      } else if(eventType == XmlPullParser.START_TAG) {
	          if(xpp.getName().equals(Tag)){
	        	 found=true;
	        	  
	          }
	      } else if(eventType == XmlPullParser.END_TAG) {
	    	  
	      } else if(eventType == XmlPullParser.TEXT) {
	    	  
	    	 if (found){ value=xpp.getText();}
	    	 
	    	 
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
	
	
public String getTagValue(String XMLMessage, String Tag){
	String value=null;
	int eventType=0;
	boolean found=false;
	XmlPullParser xpp=getParser();

	
	
	try {
		xpp.setInput(new StringReader(XMLMessage));
	
	
     while (eventType != XmlPullParser.END_DOCUMENT ) {
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
	
	
	public HashMap<String, String> retrieveContacts(String XMLMessage){
		
		HashMap<String, String> contactsTable=new HashMap<String, String>();
		int eventType=0;
		boolean foundNick=false,foundNumber=false;
		XmlPullParser xpp=getParser();
		
		String nick="",number="";
		
		try {
			xpp.setInput(new StringReader(XMLMessage));
		
		
	     while (eventType != XmlPullParser.END_DOCUMENT) {
	      if(eventType == XmlPullParser.START_DOCUMENT) {
	    	  
	      } else if(eventType == XmlPullParser.START_TAG) {
	         if(xpp.getName().equals("mobile")){
	        	 foundNumber=true;
	         }else if(xpp.getName().equals("nick")){
	        	 foundNick=true;

	         }
	          
	      } else if(eventType == XmlPullParser.END_TAG) {
	    	 
	    	  
	    	  
	      } else if(eventType == XmlPullParser.TEXT) {
	    	  if(foundNick){
	    		  nick=xpp.getText();
	    		  foundNick=false;
	    	  }else if(foundNumber){
	    		  number=xpp.getText();
	    		  foundNumber=false;
	    	  }
	    	 
	      
	      }
	      
	      if(nick!="" && number!=""){
	    	  
	    	  contactsTable.put(number, nick);
	    	  nick="";
	    	  number="";
	    	  
	      }
	      
	      eventType = xpp.next();
	     }
		}catch(XmlPullParserException e){
			
			e.printStackTrace();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		
		return contactsTable;

	}
	
	public ArrayList<Contact> retrieveContacts2(String XMLMessage){
		
		ArrayList<Contact> contacts=new ArrayList<Contact>();
		int eventType=0;
		boolean foundNick=false,foundNumber=false;
		XmlPullParser xpp=getParser();
		
		String nick="",number="";
		
		try {
			xpp.setInput(new StringReader(XMLMessage));
		
		
	     while (eventType != XmlPullParser.END_DOCUMENT) {
	      if(eventType == XmlPullParser.START_DOCUMENT) {
	    	  
	      } else if(eventType == XmlPullParser.START_TAG) {
	         if(xpp.getName().equals("mobile")){
	        	 foundNumber=true;
	         }else if(xpp.getName().equals("nick")){
	        	 foundNick=true;

	         }
	          
	      } else if(eventType == XmlPullParser.END_TAG) {
	    	 
	    	  
	    	  
	      } else if(eventType == XmlPullParser.TEXT) {
	    	  if(foundNick){
	    		  nick=xpp.getText();
	    		  foundNick=false;
	    	  }else if(foundNumber){
	    		  number=xpp.getText();
	    		  foundNumber=false;
	    	  }
	    	 
	      
	      }
	      
	      if(nick!="" && number!=""){
	    	  
	    	  contacts.add(new Contact(nick,number));
	    	  nick="";
	    	  number="";
	    	  
	      }
	      
	      eventType = xpp.next();
	     }
		}catch(XmlPullParserException e){
			
			e.printStackTrace();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		
		return contacts;

	}
	
	

	

}
