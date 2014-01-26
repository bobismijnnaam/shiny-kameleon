package utility;

import java.util.ArrayList;
import java.util.List;

public class Utils {
	public static <T> String join(List<T> list, String delimiter) {
		StringBuilder sb = new StringBuilder(list.size());
		
		for (int i = 0; i < list.size() - 1; i++) {
			sb.append(list.get(i).toString());
			sb.append(delimiter);
		}
		
		sb.append(list.get(list.size() - 1));
		
		return sb.toString();
	}
	
	public static void main(String[] args) {
		List<String> sl = new ArrayList<String>();
		sl.add("hi");
		sl.add("lo");
		sl.add("bye");
		sl.add("YO&MAMA");
		System.out.println("\"" + Utils.join(sl, "_") + "\"");
	}
}
