package uk.me.redmonds.contactsync;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import android.widget.*;

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

	@Override
	public boolean equals(Object object)
	{
		StringMap compObj;
		if(object instanceof StringMap) {
			compObj = (StringMap) object;
		} else {
			return false;
		}
		
		for(Map.Entry<String,Object> e: this.entrySet()) {
			if(compObj.containsKey(e.getKey())) {
				if(e.getValue() == null || compObj.getObject(e.getKey()) == null) {
					if(e.getValue() != compObj.getObject(e.getKey()))
						return false;
				} else if(e.getValue().getClass() == compObj.getObject(e.getKey()).getClass()) {
					if (e.getValue() instanceof byte[]) {
						if(!Arrays.equals((byte[]) e.getValue(), (byte[])compObj.getObject(e.getKey()))) {
								return false;
						}
					} else if (!e.getValue().equals(compObj.getObject(e.getKey()))) {
						return false;
					}
				} else
					return false;
			} else
				return false;
		}
		
		for(Map.Entry<String,Object> e: compObj.entrySet())
			if(!this.containsKey(e.getKey()))
				return false;
		
		return true;
	}
}
