package com.cleytongoncalves.centralufmt.data.local;

import com.cleytongoncalves.centralufmt.data.model.Meal;
import com.cleytongoncalves.centralufmt.data.model.MenuRu;
import com.cleytongoncalves.centralufmt.util.TimeInterval;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import timber.log.Timber;

public class MenuParser {
	private static final Charset CHARSET = Charset.forName("UTF-8");
	private static final char NBSP_CODE = '\u00a0';

	public static MenuRu parse(String pageHtml) {
		Element mealSection = getMealSection(pageHtml);
		LocalDate date = parseDate(mealSection);
		
		MenuRu.Builder menuBuilder = MenuRu.builder();
		menuBuilder.date(date);

		Elements tables = mealSection.getElementsByTag("table");
		
		final int breakfastTableIdx = 0;
		final int lunchTableIdx = 1;
		final int dinnerTableIdx = 2;
		
		List<String> breakfast;
		Meal lunch, dinner;
		try {
			breakfast = parseBreakfast(tables.get(breakfastTableIdx));
			lunch = parseMeal(tables.get(lunchTableIdx), MenuRu.HORARIO_ALMOCO);
			dinner = parseMeal(tables.get(dinnerTableIdx), MenuRu.HORARIO_JANTA);
		} catch (IndexOutOfBoundsException e) {
			Timber.e("Error parsing menu (defaulted to Empty): %s", e.getMessage());
			breakfast = Collections.emptyList();
			lunch = Meal.emptyMeal(MenuRu.HORARIO_ALMOCO);
			dinner = Meal.emptyMeal(MenuRu.HORARIO_JANTA);
		}
		
		menuBuilder.breakfast(breakfast);
		menuBuilder.lunch(lunch);
		menuBuilder.dinner(dinner);
		
		return menuBuilder.build();
	}
	
	private static Element getMealSection(String pageHtml) {
		return Jsoup.parse(pageHtml).getElementById("secao");
	}
	
	private static LocalDate parseDate(Element mealSection) {
		LocalDate date;
		final int dateTagIndex = 4;
		//TODO: DATA:/09/12/2016 6ª feira - Make it correct indepedent of typos
		//DATA:16/09/2016 6ª feira
		final int subStrStart = 5;
		final int subStrEnd = 15;
		DateTimeFormatter fmt = DateTimeFormat.forPattern("dd/MM/yyyy");
		
		String dateStr = null;
		try {
			dateStr = mealSection.child(dateTagIndex).text().substring(subStrStart, subStrEnd);
			date = fmt.parseLocalDate(dateStr);
		} catch (Exception e) {
			Timber.w("Error parsing date (defaulted to Today): %s - %s", dateStr, e.getMessage());
			date = LocalDate.now();
		}
		
		return date;
	}
	
	private static Meal parseMeal(Element mealTable, TimeInterval time) {
		Elements tdTags = mealTable.getElementsByTag("td");
		if (tdTags.isEmpty()) { return Meal.emptyMeal(time); }

		Meal.Builder mealBuilder = Meal.builder();
		mealBuilder.timeInterval(time);

		final int saladaTd = 1;
		final int misturaTd = 3;
		final int vegetarianoTd = 5;
		final int guarnicaoTd = 7;
		final int acompanhamentoTd = 9;
		final int sobremesaTd = 11;
		final int sucoTd = 13;
		
		try {
			List<String> salada = parseFood(tdTags.get(saladaTd));
			mealBuilder.salad(salada);
			List<String> mistura = parseFood(tdTags.get(misturaTd));
			mealBuilder.meat(mistura);
			List<String> vegetariano = parseFood(tdTags.get(vegetarianoTd));
			mealBuilder.vegetarian(vegetariano);
			List<String> guarnicao = parseFood(tdTags.get(guarnicaoTd));
			mealBuilder.garnishes(guarnicao);
			List<String> acompanhamento = parseFood(tdTags.get(acompanhamentoTd));
			mealBuilder.acompaniment(acompanhamento);
			List<String> sobremesa = parseFood(tdTags.get(sobremesaTd));
			mealBuilder.dessert(sobremesa);
			List<String> suco = parseFood(tdTags.get(sucoTd));
			mealBuilder.juice(suco);
		} catch (IndexOutOfBoundsException e) {
			Timber.e("Meal Parsing Error - <td> index not found: %s", e.getMessage());
		}
		
		return mealBuilder.build();
	}
	
	private static List<String> parseBreakfast(Element bfTable) {
		Elements textElements = bfTable.getElementsByTag("p");
		
		List<String> bf = new ArrayList<>();
		for (Element each : textElements) {
			bf.add(each.text().replace(String.valueOf(NBSP_CODE), "").trim());
		}
		
		return bf;
	}
	
	private static List<String> parseFood(Element element) {
		List<String> foodList = new ArrayList<>();
		
		if (element == null || ! element.hasText()) { return foodList; }
		
		Elements pTag = element.getElementsByTag("p");
		for (Element each : pTag) {
			String str = each.text();
			List<String> parsedFood = toFoodList(str);
			
			try {
				foodList.addAll(parsedFood);
			} catch (Exception e) {
				Timber.e("Error parsing food: %s - %s", parsedFood, e.getMessage());
			}
		}
		
		return foodList;
	}
	
	private static List<String> toFoodList(String str) {
		List<String> list = new ArrayList<>();
		if (str == null || str.length() <= 1) { return list; } //Empty or with NBSP
		
		StringBuilder word = new StringBuilder(str.length());
		for (int i = 0; i < str.length(); i++) {
			int currChar = str.charAt(i);
			
			if (i > 2 && str.charAt(i - 2) != ' ' && str.charAt(i - 1) != 'C' && currChar == '/') {
				list.add(word.toString().trim());
				word.delete(0, word.length());
			} else if (currChar != NBSP_CODE) {
				word.append(str.charAt(i));
			}
		}
		
		if (word.length() > 0) { list.add(word.toString().trim()); }
		
		return list;
	}
}
