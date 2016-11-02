package com.cleytongoncalves.centralufmt.util;

import java.util.StringTokenizer;

public final class TextUtil {
	private static final String TAG = TextUtil.class.getSimpleName();

	private TextUtil() {
		throw new RuntimeException("TextUtil is not instantiable");
	}

	public static String capsMeaningfulWords(String text) {
		final String[] toIgnore = {"Para", "Com", "E", "De", "Da", "Do"};

		StringTokenizer tokenizer = new StringTokenizer(text);
		StringBuilder sb = new StringBuilder();

		while (tokenizer.hasMoreTokens()) {
			String currWord = tokenizer.nextToken().toLowerCase();


			boolean toCaps = true;
			if (currWord.length() < 5) {
				for (int i = 0, length = toIgnore.length; i < length && toCaps; i++) {
					toCaps = ! currWord.equalsIgnoreCase(toIgnore[i]);
				}
			}

			if (toCaps) {
				currWord = Character.toUpperCase(currWord.charAt(0)) + currWord.substring(1);
			}

			sb.append(currWord);
			if (tokenizer.hasMoreTokens()) {
				sb.append(" ");
			}
		}

		return sb.toString();
	}

	public static String ellipsizeString(String text, int maxSize) {
		if (text.length() > maxSize) {
			final char ellipse = '\u2026'; //Even though it is only 1 char, it occupies ~2 spaces
			text = text.substring(0, maxSize - 2) + ellipse;
		}

		return text;
	}
}
