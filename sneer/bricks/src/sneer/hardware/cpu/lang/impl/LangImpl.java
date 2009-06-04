package sneer.hardware.cpu.lang.impl;

import java.io.Serializable;
import java.util.Collection;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.SerializationUtils;
import org.apache.commons.lang.StringUtils;

import sneer.hardware.cpu.lang.Lang;

class LangImpl implements Lang {

	private final Arrays _arrays = new Lang.Arrays(){
		@Override public void reverse(Object[] array) { ArrayUtils.reverse(array);}
	};
	
	private final Serialization _serialization = new Lang.Serialization(){
		@Override public byte[] serialize(Serializable obj) { return SerializationUtils.serialize(obj); }
		@Override public <T> T serialize(byte[] data) { return (T)SerializationUtils.deserialize(data); }
	};

	private Strings _strings = new Lang.Strings(){
		@Override public boolean isEmpty(String str) { return str == null || str.isEmpty();	}
		@Override public String join(Collection<?> collection, String separator) {return StringUtils.join(collection, separator); }
		@Override public String trimToNull(String str) {return StringUtils.trimToNull(str);}
		@Override public String chomp(String str, String separator) { return StringUtils.chomp(str, separator);}
		@Override public String deleteWhitespace(String str) {return StringUtils.deleteWhitespace(str);}
	};

	@Override	 public Arrays arrays() { return _arrays; }
	@Override public Serialization serialization() {	 return _serialization;}
	@Override public Strings strings() { return _strings;}
}