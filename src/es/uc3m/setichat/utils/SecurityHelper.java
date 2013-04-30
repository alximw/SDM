package es.uc3m.setichat.utils;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.KeySpec;


import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import android.content.Context;
import android.content.Intent;
import android.security.KeyChain;
import android.security.KeyChainAliasCallback;


public class SecurityHelper{

	public static final String SETICHAT_AES_MODE="AES/CBC/PKCS5Padding";
	public static final String LOCAL_AES_MODE="AES/ECB/PKCS5Padding";
	public static final String SETICHAT_CERTIFICATE_ALGORITHM="SHA1WithRSAEncryption";
	public static final int RSAPAIR_KEY_SIZE=1024;
	public static final int ROUNDS_NUMBER=1000;
	
	
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

	//salt generator, the salt is used to add entropy to the user's password
	public static byte[] generateSalt(){
		
		byte[] salt=new byte[16];
		try{
		SecureRandom sr=SecureRandom.getInstance("SHA1PRNG");
		sr.nextBytes(salt);
		}catch(Exception e ){	
			e.printStackTrace();
		}
		return salt;
	}
	
	//key derivation method
	public static SecretKey derivePassword(String key, byte[] salt){

		SecretKey secret = null; 
		
		try{
		KeySpec spec = new PBEKeySpec(key.toCharArray(), salt, ROUNDS_NUMBER, 128);
		SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");		
		SecretKey temp= skf.generateSecret(spec);
		secret = new SecretKeySpec(temp.getEncoded(), "AES");
		
		}catch(Exception e ){
			
			e.printStackTrace();
			
		}
		
		return secret;
	}
		
		//compute the SHA1 digest 
	public static String generateSHA1Hash(byte[] data){
		
		MessageDigest mDigest=null;
		try {
			mDigest = MessageDigest.getInstance("SHA1");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
        byte[] result = mDigest.digest(data);
        StringBuffer sb = new StringBuffer();
		
        for (int i = 0; i < result.length; i++) {
            sb.append(Integer.toString((result[i] & 0xff) + 0x100, 16).substring(1));
        }
         
        return sb.toString();
		
	}
		
	//simplified version of AES128 method used for message encryption
	//this version uses ECB mode instead of CBC 
	public static byte[] AES128(int mode,SecretKey key,byte[] data){
		byte[] result=null;
		
		try{
			Cipher cipher=Cipher.getInstance(LOCAL_AES_MODE);
			cipher.init(mode, key);
			result=cipher.doFinal(data);
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return result;
	}
	
	
}
