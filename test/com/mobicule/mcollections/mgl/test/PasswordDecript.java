package com.mobicule.mcollections.mgl.test;

import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.mobicule.mcollections.core.commons.Base64;

public class PasswordDecript
{
	public static String decrypt(String strToDecrypt)
	{
		try
		{
			System.out.println(strToDecrypt);
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
			AlgorithmParameterSpec paramSpec = new IvParameterSpec("k0t@km0b!culeR$y".getBytes());
			SecretKeySpec secretKey = new SecretKeySpec("k0t@km0b!culeR$y".getBytes(), "AES");
			cipher.init(2, secretKey, paramSpec);
			String decryptedString = new String(cipher.doFinal(Base64.decode(strToDecrypt)));
			return decryptedString;
		}
		catch (Exception localException)
		{
		}

		return null;
	}

	public static void main(String args[])
	{

		System.out.println("--- password is ----- " + decrypt("Zy4EOtFaF1QYiZa5zFAhQg=="));

	}

}
