package com.mobicule.mcollections.integration.commons;

import java.util.UUID;

public class GenerateUUID {

	public static final void main(String... aArgs) 
	{
		// generate random UUIDs
		UUID idOne = UUID.randomUUID();
		String oneString = idOne.toString();
		oneString = "KMBMKCBG" + oneString.replaceAll("-", "");
		UUID idTwo = UUID.randomUUID();
		String twoString = idTwo.toString();
		twoString = "ABC" + twoString.replaceAll("-", "");
		// log("UUID TXN ID One: " + oneString);
		log("UUID TXN ID One: " + oneString.substring(0, 35));
		// log("UUID TXN ID Two: " + twoString);
	}

	private static void log(Object aObject) 
	{
		System.out.println(String.valueOf(aObject));
	}

	public String GenerateUUIDValue() 
	{
		UUID idOne = UUID.randomUUID();
		String oneString = idOne.toString();
		oneString = "KMBMKCBG" + oneString.replaceAll("-", "");
		UUID idTwo = UUID.randomUUID();
		String twoString = idTwo.toString();
		twoString = "ABC" + twoString.replaceAll("-", "");
		
		return oneString.substring(0, 35);
	}
}
