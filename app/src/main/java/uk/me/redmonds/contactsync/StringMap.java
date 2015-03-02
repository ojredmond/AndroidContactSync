package uk.me.redmonds.contactsync;

import java.util.HashMap;
import java.util.*;

class StringMap extends HashMap<String, Object> {
    public String get(String key)
	{
		return (String)super.get(key);
	}
	
	public Object getObject(String key)
	{
		return super.get(key);
	}
	
	public byte[] getByteArray(String key)
	{
		return (byte[])super.get(key);
	}
	
	public Boolean isByteArray (String key) {
		return super.get(key) instanceof byte[];
	}

	@Override
	public int hashCode()
	{
		int hashCode = 0;
		
		for(Map.Entry<String,Object> e: this.entrySet()) {
			hashCode += e.getKey().hashCode();
			if (e.getValue() instanceof byte[])
				hashCode += Arrays.hashCode((byte[]) e.getValue());
			else if (e.getValue() != null)
				hashCode += e.getValue().hashCode();
		}
		return hashCode;
	}
	
	public int hashCode2()
	{
		int hashCode = 0;

		for(Map.Entry<String,Object> e: this.entrySet()) {
			//hashCode += e.getKey().hashCode();
			if (e.getValue() instanceof byte[])
				hashCode += Arrays.hashCode((byte[]) e.getValue());
			else if (e.getValue() != null)
				hashCode += e.getValue().hashCode();
		}
		return hashCode;
	}
}
