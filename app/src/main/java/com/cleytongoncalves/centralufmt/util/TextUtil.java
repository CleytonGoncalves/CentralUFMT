package com.cleytongoncalves.centralufmt.util;

import java.util.StringTokenizer;

public final class TextUtil {
	private TextUtil() {
		throw new AssertionError("TextUtil is not instantiable");
	}

	public static String capsWordsFirstLetter(String text) {
		final String[] toIgnore = {"Para", "Com", "E", "De", "Da", "Do"};
		final String[] alwaysCaps = {"I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"};

		StringTokenizer tokenizer = new StringTokenizer(text);
		StringBuilder sb = new StringBuilder();

		while (tokenizer.hasMoreTokens()) {
			String currWord = tokenizer.nextToken();

			boolean toCapsFirstLetter = true;
			boolean toCapsAll = false;
			if (currWord.length() < 5) {
				for (int i = 0, length = alwaysCaps.length; i < length && ! toCapsAll; i++) {
					toCapsAll = currWord.equalsIgnoreCase(alwaysCaps[i]);
				}

				if (! toCapsAll) {
					for (int i = 0, length = toIgnore.length; i < length && toCapsFirstLetter;
					     i++) {
						toCapsFirstLetter = ! currWord.equalsIgnoreCase(toIgnore[i]);
					}
				}
			}

			if (toCapsAll) {
				currWord = currWord.toUpperCase();
			} else if (toCapsFirstLetter) {
				currWord = Character.toUpperCase(currWord.charAt(0)) + currWord.substring(1)
				                                                               .toLowerCase();
			} else {
				currWord = currWord.toLowerCase();
			}

			sb.append(currWord);
			if (tokenizer.hasMoreTokens()) {
				sb.append(" ");
			}
		}

		return sb.toString();
	}

	public static String capsSentenceFirstLetter(String text) {
		final String[] toIgnore = {"Para", "Com", "E", "De", "Da", "Do"};
		final String[] alwaysCaps = {"I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"};

		StringTokenizer tokenizer = new StringTokenizer(text);
		StringBuilder sb = new StringBuilder();

		boolean toCapsFirstLetter = true;
		while (tokenizer.hasMoreTokens()) {
			String currWord = tokenizer.nextToken();

			boolean toCapsAll = false;
			if (currWord.length() < 5) {
				for (int i = 0, length = alwaysCaps.length; i < length && ! toCapsAll; i++) {
					toCapsAll = currWord.equalsIgnoreCase(alwaysCaps[i]);
				}

				if (! toCapsAll) {
					for (int i = 0, length = toIgnore.length; i < length && toCapsFirstLetter;
					     i++) {
						toCapsFirstLetter = ! currWord.equalsIgnoreCase(toIgnore[i]);
					}
				}
			}

			if (toCapsAll) {
				currWord = currWord.toUpperCase();
			} else if (toCapsFirstLetter) {
				currWord = Character.toUpperCase(currWord.charAt(0)) + currWord.substring(1)
				                                                               .toLowerCase();
				toCapsFirstLetter = false;
			} else {
				currWord = currWord.toLowerCase();
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
