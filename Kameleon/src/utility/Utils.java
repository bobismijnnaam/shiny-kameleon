package utility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Utils {
	public static String join(int[] ints, String delimiter) {
		ArrayList<Integer> intList = new ArrayList<Integer>(ints.length);
		for (int i : ints) {
			intList.add(new Integer(i));
		}
		
		return join(intList, delimiter);
	}
	
	public static String join(String[] strings) {
		return join(strings, " ");
	}
	
	public static String join(String[] strings, String delimiter) {
		return join(Arrays.asList(strings), delimiter);
	}
	
	public static <T> String join(List<T> list, String delimiter) {
		StringBuilder sb = new StringBuilder(list.size());
		
		if (list.size() == 0) {
			return "";
		} else if (list.size() == 1) {
			return String.valueOf(list.get(0));
		} else {
			for (int i = 0; i < list.size() - 1; i++) {
				sb.append(list.get(i).toString());
				sb.append(delimiter);
			}
	
			sb.append(list.get(list.size() - 1));
		
			return sb.toString();
		}
	}
	
	public static void disp(String pre, String[] args) {
		System.out.println(pre + ": " + join(Arrays.asList(args), "|"));
	}
	
	public static void main(String[] args) {
		List<String> sl = new ArrayList<String>();
		sl.add("hi");
		sl.add("lo");
		sl.add("bye");
		sl.add("YO&MAMA");
		System.out.println("\"" + Utils.join(sl, "_") + "\"");
		
		int[] ints = new int[64];
		for (int i = 0; i < 64; i++) {
			ints[i] = i;
		}
		
		System.out.println(Utils.join(ints, " "));
	}
}
