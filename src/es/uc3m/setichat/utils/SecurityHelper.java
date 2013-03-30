package es.uc3m.setichat.utils;

import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import android.util.Log;

public class SecurityHelper {

	public static final String SETICHAT_AES_MODE="AES/CBC/PKCS5Padding";
	
	
	public static SecretKeySpec generateAES128Key(){
		SecretKey secret=null;
		KeyGenerator generator=null;
		try {
			generator=KeyGenerator.getInstance("AES");
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		generator.init(128);
		secret=generator.generateKey();		
		return new SecretKeySpec(secret.getEncoded(), "AES");
	}
	
	
	
	public static IvParameterSpec generateAESIV(){
		byte[] IV=new byte[16];
		SecureRandom rand=new SecureRandom();
		rand.nextBytes(IV);
		return new IvParameterSpec(IV);
	}
	

	public static byte[] encryptAES128(IvParameterSpec IV,SecretKeySpec key,String mode,String plainText ) {
		byte[] encryptedText=null;
		
		try{
		Cipher SymKeyCipher = Cipher.getInstance(mode);
		
		SymKeyCipher.init(Cipher.ENCRYPT_MODE, key, IV);
		
		encryptedText=SymKeyCipher.doFinal(plainText.getBytes());
		}catch(Exception e){
			
			e.printStackTrace();
		}
		
		return encryptedText;

	}
}
