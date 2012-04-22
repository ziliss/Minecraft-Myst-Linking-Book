package net.minecraft.src.mystlinkingbook;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

/**
 * Inspired by: http://stackoverflow.com/questions/54295/how-to-write-java-util-properties-to-xml-with-sorted-keys
 * 
 * @author ziliss
 * @since 0.9b
 */
public class Properties extends java.util.Properties {
	
	protected boolean sorted = false;
	
	public Properties() {
	}
	
	public Properties(java.util.Properties defaults) {
		super(defaults);
	}
	
	@Override
	public Set<Entry<Object, Object>> entrySet() {
		return sorted ? Collections.unmodifiableSet(new TreeSet<Entry<Object, Object>>(super.entrySet())) : super.entrySet();
	}
	
	@Override
	public synchronized Enumeration<Object> keys() {
		return sorted ? Collections.enumeration(new TreeSet<Object>(super.keySet())) : super.keys();
	}
	
	@Override
	public Set<Object> keySet() {
		return sorted ? Collections.unmodifiableSet(new TreeSet<Object>(super.keySet())) : super.keySet();
	}
	
	@Override
	public Enumeration<?> propertyNames() {
		return sorted ? Collections.enumeration(new TreeSet(super.stringPropertyNames())) : super.propertyNames();
	}
	
	@Override
	public Set<String> stringPropertyNames() {
		return sorted ? Collections.unmodifiableSet(new TreeSet<String>(super.stringPropertyNames())) : super.stringPropertyNames();
	}
	
	public boolean isSorted() {
		return sorted;
	}
	
	public void setSorted(boolean sorted) {
		this.sorted = sorted;
	}
}
