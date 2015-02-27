package uk.me.redmonds.contactsync;

import java.util.HashMap;

public class StringMap extends HashMap<String,Object>
{
	@Override
	public String get(String key)
	{
		return (String)super.get(key);
	}
	
	public byte[] getByteArray(String key)
	{
		return (byte[])super.get(key);
	}
}