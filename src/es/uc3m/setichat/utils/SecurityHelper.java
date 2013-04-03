package es.uc3m.setichat.utils;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;


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
	public static final String SETICHAT_CERTIFICATE_ALGORITHM="SHA1WithRSAEncryption";
	
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
	

	public static byte[] AES128(int opMode,SecretKeySpec key,String mode,String plainText ) {
		byte[] encryptedText=null;
		IvParameterSpec IV=generateAESIV();
		try{
		Cipher SymKeyCipher = Cipher.getInstance(mode);
		
		SymKeyCipher.init(opMode, key, IV);
		
		encryptedText=SymKeyCipher.doFinal(plainText.getBytes("UTF-8"));
		}catch(Exception e){
			
			e.printStackTrace();
		}
		
		return encryptedText;

	}
	
	public static KeyPair generateRSAKeyPair() {
		KeyPair pair=null;
		 KeyPairGenerator keyGen = null;
		try {
			keyGen = KeyPairGenerator.getInstance("RSA");
		     keyGen.initialize(1024);
		     pair=keyGen.genKeyPair();
				Log.i("public",new BigInteger(pair.getPublic().getEncoded()).toString(16));
				Log.i("private",new BigInteger(pair.getPrivate().getEncoded()).toString(16));
	
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return pair;
	}
	
	
	
	public static String publicCipher(int opMode,String plaintext,Key key){
		
		byte []result=null;
		Cipher publicKeyCipher;
		try {
			publicKeyCipher = Cipher.getInstance("RSA/ECB/PKCS1PADDING");
			publicKeyCipher.init(opMode, key);
			result = publicKeyCipher.doFinal(plaintext.getBytes("UTF-8"));

		} catch (NoSuchAlgorithmException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} catch (NoSuchPaddingException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		
		} catch (InvalidKeyException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		
		
		} catch (IllegalBlockSizeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return Base64.encodeToString(result, false);
	}
	

	public static String getSign(String dataToSign,PrivateKey key){
		
		byte []signatureResult=null;
		try {
			Signature sign = Signature.getInstance("SHA1withRSA");
			sign.initSign(key);
			sign.update(dataToSign.getBytes("UTF-8"));
			signatureResult = sign.sign();
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SignatureException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		
		
		return Base64.encodeToString(signatureResult, false);

	}
	
	
	public static boolean verifySign(String dataToVerify,String signature,PublicKey key){
		
		boolean result=false;
		try {
			Signature sign = Signature.getInstance("SHA1withRSA");
			sign.initVerify(key);
			sign.update(dataToVerify.getBytes("UTF-8"));	
			result=sign.verify(signature.getBytes("UTF-8"));
		
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SignatureException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		
		
		return result;

	}
	
	
	
	
	
}
