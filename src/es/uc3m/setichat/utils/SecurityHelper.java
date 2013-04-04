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
import java.security.interfaces.RSAPublicKey;


import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;


public class SecurityHelper {

	public static final String SETICHAT_AES_MODE="AES/CBC/PKCS5Padding";
	public static final String SETICHAT_CERTIFICATE_ALGORITHM="SHA1WithRSAEncryption";
	public static final int RSAPAIR_KEY_SIZE=1024;
	
	
	//generate AES128 KEY
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
	
	//generate AES 128 IV
	public static IvParameterSpec generateAESIV(){
		byte[] IV=new byte[16];
		SecureRandom rand=new SecureRandom();
		rand.nextBytes(IV);
		return new IvParameterSpec(IV);
	}
	
	//use AES128 symetric cipher
	public static byte[] AES128(int opMode,SecretKeySpec key,byte[] IV,String mode,byte[] text ) {
		byte[] encryptedText=null;
		try{
		Cipher SymKeyCipher = Cipher.getInstance(mode);
		SymKeyCipher.init(opMode, key, new IvParameterSpec(IV));
		
		encryptedText=SymKeyCipher.doFinal(text);
		}catch(Exception e){
			
			e.printStackTrace();
		}
		
		return encryptedText;

	}
	
	//generate RSA keyPair with keysize key length
	public static KeyPair generateRSAKeyPair(int keysize) {
		KeyPair pair=null;
		 KeyPairGenerator keyGen = null;
		try {
			keyGen = KeyPairGenerator.getInstance("RSA");
		     keyGen.initialize(keysize);
		     pair=keyGen.genKeyPair();
	
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return pair;
	}
	
	
	//use RSA asymmetric cipher
	public static byte[] publicCipher(int opMode,byte[] plaintext,Key key){
		
		byte []result=null;
		Cipher publicKeyCipher;
		try {
			publicKeyCipher = Cipher.getInstance("RSA/ECB/PKCS1PADDING");
			publicKeyCipher.init(opMode, key);
			result = publicKeyCipher.doFinal(plaintext);

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
		} 
		
		
		return result;
	}
	

	//get dataToSign signed with key
	public static byte[] getSign(byte[] dataToSign,PrivateKey key){
		
		byte []signatureResult=null;
		try {
			Signature sign = Signature.getInstance("SHA1withRSA");
			sign.initSign(key);
			sign.update(dataToSign);
			signatureResult = sign.sign();
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SignatureException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		
		
		return signatureResult;

	}
	
	//verify with signature and key the text on data  
	public static boolean verifySign(byte[] data,byte[] signature,PublicKey key){
		
		boolean result=false;
		try {
			Signature sign = Signature.getInstance("SHA1withRSA");
			sign.initVerify(key);
			sign.update(data);	
			result=sign.verify(signature);
		
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SignatureException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		
		
		return result;

	}
	
	
	
	
	
}
