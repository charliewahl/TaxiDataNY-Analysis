package javaintro.ws21.taxidata.ors;

import java.util.Arrays;

public class Main {

	public static void main(String[] args) {
		int[] ints = new int[2];
		Integer[] ints2 = new Integer[2];
		System.out.println(Arrays.toString(ints2));
		
		for(int i=0; i< ints2.length; i++) {
			ints2[i] = new Integer(0);
		}
		
		for(int i=0; i< ints2.length; i++) {
			ints2[i] += 1;
		}
		System.out.println(Arrays.toString(ints2));

	}

}
