package uk.me.redmonds.contactsync;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

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
}
