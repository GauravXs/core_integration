package com.mobicule.mcollections.integration.commons;
import java.lang.reflect.Field;
import java.security.MessageDigest;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;


public class Crypto {
	//this code allows to break limit if client jdk/jre has no unlimited policy files for JCE.
	//it should be run once. So this static section is always execute during the class loading process.
	//this code is useful when working with Bouncycastle library.
	static {
	    try {
	        Field field = Class.forName("javax.crypto.JceSecurity").getDeclaredField("isRestricted");
	        field.setAccessible(true);
	        field.set(null, java.lang.Boolean.FALSE);
	    } catch (Exception ex) {
	    }
	}
	 public static byte[] SHA256(String paramString)throws Exception {
	     MessageDigest localMessageDigest = MessageDigest.getInstance("SHA-256");
	     localMessageDigest.update(paramString.getBytes("UTF-8"));
	     byte[] digest = localMessageDigest.digest();
	     return digest;
	 }
	 public static byte[] decrypt(byte[] key,byte[] data)throws Exception{
	        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
	        byte[] iv = new byte[16];
	        IvParameterSpec ivSpec = new IvParameterSpec(iv);
	        Cipher acipher = Cipher.getInstance("AES/CBC/PKCS5Padding");	
	        acipher.init(Cipher.DECRYPT_MODE, secretKeySpec,ivSpec);
	        byte[] arrayOfByte1  = acipher.doFinal(data);
	        return arrayOfByte1;
	 }
	 public static byte[] encrypt(byte[] key,byte[] data)throws Exception{
		 System.out.println(">>>>>>>>>KEY::"+key.length);
	        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
	        byte[] iv = new byte[16];
	        IvParameterSpec ivSpec = new IvParameterSpec(iv);
	        Cipher acipher = Cipher.getInstance("AES/CBC/PKCS5Padding");	
	        acipher.init(Cipher.ENCRYPT_MODE, secretKeySpec,ivSpec);
	        byte[] arrayOfByte1  = acipher.doFinal(data);
	        return arrayOfByte1;
	 }
	 
	 public static String createAesKey()
	    {

	        try
	        {
	            KeyGenerator localKeyGenerator = KeyGenerator.getInstance("AES");
	            localKeyGenerator.init(256);
	            SecretKey localSecretKey = localKeyGenerator.generateKey();
	            byte[] data = localSecretKey.getEncoded();
	            String str = bytesToHex(data);
	            return  str;
	        }
	        catch (Exception e)
	        {
	            e.printStackTrace();
	        }
	        return null;
	    }
	
	 final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
	    public static String bytesToHex(byte[] bytes) {
	        char[] hexChars = new char[bytes.length * 2];
	        for ( int j = 0; j < bytes.length; j++ ) {
	            int v = bytes[j] & 0xFF;
	            hexChars[j * 2] = hexArray[v >>> 4];
	            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
	        }
	        return new String(hexChars);
	    }
	    public static byte[] hexStringToByteArray(String s) {
	        byte[] b = new byte[s.length() / 2];
	        for (int i = 0; i < b.length; i++) {
	            int index = i * 2;
	            int v = Integer.parseInt(s.substring(index, index + 2), 16);
	            b[i] = (byte) v;
	        }
	        return b;
	    }
	    public static void main(String[] args) {
	    	System.out.println(Crypto.createAesKey());
		}

	public static boolean verifyCheckSum(ChecksumData collect, String checkSum, String key) {
		try {
			System.out.println("checkSumval :"+collect.getInput());
			 byte[] hash=Crypto.SHA256(collect.getInput());
			 //System.out.println(Crypto.bytesToHex(hash));
		 	 byte[] rchek=Crypto.decrypt(Crypto.hexStringToByteArray(key),Base64.decodeBase64(checkSum));
		 	// System.out.println(Crypto.bytesToHex(rchek));
		 	 
    	 if(Arrays.equals(hash, rchek)){
    		 return true;
    	 }else{
     		 return false;
      	 }
    	} catch (Exception e) {
		 	 e.printStackTrace();
	     }
    	return false;
	}
}
