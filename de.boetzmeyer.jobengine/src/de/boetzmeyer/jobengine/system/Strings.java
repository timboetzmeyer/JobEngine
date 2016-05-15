package de.boetzmeyer.jobengine.system;

public final class Strings {

	public static final String EMPTY = "";

	private Strings() {
	}

	public static boolean isEmpty(final String inStr) {
		return inStr == null || inStr.isEmpty();
	}

	public static boolean isNotEmpty(final String inStr) {
		return inStr != null && !inStr.isEmpty();
	}

	public static String concat(Object... inArgs) {
		StringBuilder s = new StringBuilder();
		for (Object o : inArgs) {
			if (o != null) {
				s.append(o);
			}
		}
		return s.toString();
	}

	public static int indexOf(final String inString, final String[] inStrings) {
		if (inString != null && inStrings != null) {
			for (int i = 0; i < inStrings.length; i++) {
				if ((inStrings[i] != null) && (inStrings[i].equalsIgnoreCase(inString))) {
					return i;
				}
			}
		}
		return -1;
	}

	public static boolean isInArray(final String inString, final String[] inArray) {
		return indexOf(inString, inArray) >= 0;
	}

}
