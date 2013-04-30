
package es.uc3m.setichat.contactsHandling;

import java.io.Serializable;
import java.security.KeyPair;

public  class User implements Serializable{
	
	private String nick,hash,token,salt,number;
	private KeyPair pair;
	
	public User(String nick, String number, String token){
		this.number=number;
		this.nick=nick;
		this.token=token;
		
	}

	public String getNick() {
		return nick;
	}

	public void setNick(String nick) {
		this.nick = nick;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getSalt() {
		return salt;
	}

	public void setSalt(String salt) {
		this.salt = salt;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public KeyPair getPair() {
		return pair;
	}

	public void setPair(KeyPair pair) {
		this.pair = pair;
	}
	
	public String toString(){
		String info="NICK: "+this.nick+"\n"+
					"Number: "+this.number+"\n"+
					"Token: "+this.token+"\n"+
					"Salt: "+this.salt+"\n"+
					"Hash:"+this.hash+"\n";
					
		return info;
	}
	
	
	
	
}