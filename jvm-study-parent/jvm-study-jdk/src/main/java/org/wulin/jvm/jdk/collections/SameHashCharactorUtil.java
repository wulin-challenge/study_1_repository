package org.wulin.jvm.jdk.collections;

import org.junit.Test;

/**
 * 得到相同hash值的字符
 * @author ThinkPad
 *
 */
public class SameHashCharactorUtil {

	/**
	 * 得到相同hash 的字符
	 */
	@Test
	public void getSameHashCharactor() {
		
		int[] charSizes = new int[] {-1};
		int currentIndex = 0;
		for (int j = 0; j < 1000; j++) {
			Pair<int[], Integer> setChar = setChar(charSizes, 52, currentIndex);
			String chars = getChars(setChar.x);
			
			int[] charSizes2 = new int[] {-1};
			int currentIndex2 = 0;
			for (int k = 0; k < 10000; k++) {
				Pair<int[], Integer> setChar2 = setChar(charSizes2, 52, currentIndex2);
				String chars2 = getChars(setChar2.x);
				charSizes2 = setChar2.x;
				currentIndex2 = setChar2.y;
				
				if(chars.hashCode() == chars2.hashCode() && !chars.equals(chars2)) {
					System.out.println(chars+":"+chars2+":"+chars.hashCode());
				}
				
			}
			charSizes = setChar.x;
			currentIndex = setChar.y;
		}
		
	}
	
	
	private Pair<int[],Integer> setChar(int[] charSizes,int maxValue,int currentIndex) {
		//设置值成功标记
		boolean setFlag = false;
		for (int i = currentIndex; i > -1; i--) {
			int value = charSizes[i];
			
			if(value+1>=maxValue) {
				continue;
			}else {
				setFlag = true;
				charSizes[i] = value+1;
				break;
			}
		}
		
		if(!setFlag) {
			charSizes = new int[charSizes.length+1];
			currentIndex++;
		}
		
		Pair<int[],Integer> result = new Pair<int[],Integer>(charSizes,currentIndex);
		return result;
	}
	
	
	private String getChars(int[] charSize) {
		String[] chars = new String[] {"a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","q","r","s","t","u","v","w","x","y","z","A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z"};
		
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < charSize.length; i++) {
			int index = charSize[i];
			if(index+1 > chars.length) {
				
				index = index%chars.length;
			}
			result.append(chars[index]);
		}
		return result.toString();
	}
	
	private static class Pair<X,Y>{
		private X x;
		private Y y;
		public Pair(X x, Y y) {
			super();
			this.x = x;
			this.y = y;
		}
	}
	
}
