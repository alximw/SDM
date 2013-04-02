package es.uc3m.setichat.utils;

import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;


import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.KeyStoreBuilderParameters;
import javax.security.auth.x500.X500Principal;
import org.bouncycastle.asn1.x509.*;
import org.bouncycastle.x509.X509V1CertificateGenerator;

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
	
	public static KeyPair generateRSAKeyPair() {
		KeyPair pair=null;
		 KeyPairGenerator keyGen = null;
		try {
			keyGen = KeyPairGenerator.getInstance("RSA");
		     keyGen.initialize(1024);
		     pair=keyGen.generateKeyPair();
				Log.i("public",new BigInteger(pair.getPublic().getEncoded()).toString(16));
				Log.i("private",new BigInteger(pair.getPrivate().getEncoded()).toString(16));
	
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return pair;
	}
	

	
	
	
	
	
	
}
