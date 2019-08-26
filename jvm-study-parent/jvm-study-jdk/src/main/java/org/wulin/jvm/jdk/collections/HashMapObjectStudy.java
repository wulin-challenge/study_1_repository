package org.wulin.jvm.jdk.collections;

import java.util.HashMap;

import org.junit.Test;

public class HashMapObjectStudy {
	
	HashMap<ObjectKey,Object> objMap = new HashMap<ObjectKey,Object>();
	
	@Test
	public void putTest() {
		objMap.put(new ObjectKey("aa"), 1);
		objMap.put(new ObjectKey("bb"), 2);
		objMap.put(new ObjectKey("cc"), 3);
		objMap.put(new ObjectKey("dd"), 4);
		objMap.put(new ObjectKey("ee"), 5);
		objMap.put(new ObjectKey("ff"), 6);
		objMap.put(new ObjectKey("ff"), 7);
		objMap.put(new ObjectKey("hh"), 8);
		objMap.put(new ObjectKey("ii"), 9);
		objMap.put(new ObjectKey("jj"), 10);
	}

	private static class ObjectKey{
		private int hash;
		
		private String key;
		
		public ObjectKey(String key) {
			this(1212, key);
		}

		public ObjectKey(int hash, String key) {
			super();
			this.hash = hash;
			this.key = key;
		}

		@Override
		public int hashCode() {
			return hash;
		}

		@Override
		public String toString() {
			return key;
		}
	}
}
