package com.cleytongoncalves.centralufmt.data.local;

import com.cleytongoncalves.centralufmt.data.model.Course;
import com.cleytongoncalves.centralufmt.data.model.Discipline;
import com.cleytongoncalves.centralufmt.data.model.Student;
import com.cleytongoncalves.centralufmt.util.TextUtil;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class HtmlHelper {
	private static final String TAG = HtmlHelper.class.getSimpleName();
	private static final Locale LOCALE_PTBR = new Locale("pt", "BR");
	private static final String NBSP_CODE = "\u00a0";
	//TODO: ENSURE THAT ANY FIELD CAN BE EMPTY WITHOUT TROWING EXCEPTION

	public static Student parseBasicStudent(String exacaoHtml) {
		Element body = Jsoup.parse(exacaoHtml).body();
		List<TextNode> alunoInfoNodes = body.getElementsByTag("div").get(0).textNodes();

		Course course = extractCurso(alunoInfoNodes);
		return extractStudent(alunoInfoNodes, course);
	}

	private static Course extractCurso(List<TextNode> alunoInfoNodes) {
		String infoCursoRaw = alunoInfoNodes.get(0).text();
		String[] infoCurso = infoCursoRaw.split(":")[1].split("-");

		String code = infoCurso[0].trim();
		String title = TextUtil.capsWordsFirstLetter(infoCurso[1].trim());
		String type = TextUtil.capsWordsFirstLetter(infoCurso[2].trim());

		String infoTerm = alunoInfoNodes.get(2).text();
		String[] infoTermSplit = infoTerm.split(":")[1].split(" ");
		String term = infoTermSplit[0];

		List<Discipline> emptyList = Collections.emptyList();
		return Course.of(title, code, type, term, emptyList);
	}

	private static Student extractStudent(List<TextNode> alunoInfoNodes, Course course) {
		String infoAlunoRaw = alunoInfoNodes.get(1).text();
		String[] infoAluno = infoAlunoRaw.split(":")[1].split("-");

		String rga = infoAluno[0].trim();
		String nomeCompleto = TextUtil.capsWordsFirstLetter(infoAluno[1].trim());

		return Student.of(nomeCompleto, rga, course);
	}
	
	public static List<Discipline> parseSchedule(String html) {
		Element body = Jsoup.parse(html).body();
		Elements planilha = body.getElementsByTag("form");
		Elements tables = planilha.select("table");

		//0=empty, 1=periodo, 2=curso, 3=aluno outer, 4=aluno inner, 5=horarios
		final int horariosTable = 5;
		Element horarioTable = tables.get(horariosTable);

		//0=cabecalho tabela, 1..n=displicinas
		Elements rows = horarioTable.getElementsByTag("tr");

		List<Discipline> disciplinas = new ArrayList<>();

		//0=cod. disc., 1=nome, 2=primeiro dia, 3=primeiro horario, 4=turma, 5=sala, 6=crd?,
		//7=cargaHor, 8=tipo, 9=per. oferta
		for (int i = 1; i < rows.size(); i++) {
			Element currRow = rows.get(i);
			Elements content = currRow.getElementsByTag("div");

			String nome = TextUtil.capsWordsFirstLetter(content.get(1).ownText());
			String cod = content.get(0).ownText();
			String turma = content.get(4).ownText();
			String sala = content.get(5).ownText();
			String crd = content.get(6).ownText();
			String carga = content.get(7).ownText();
			String tipo = content.get(8).ownText();
			String periodo = content.get(9).ownText();

			switch (periodo) {
				case "1":
					periodo = "1º Semestre";
					break;
				case "2":
					periodo = "2º Semestre";
					break;
				case "3":
					periodo = "Anual";
					break;
				case "4":
					periodo = "Modular";
					break;
			}

			DateTimeFormatter fmt = DateTimeFormat.forPattern("EEEEHH:mm").withLocale(LOCALE_PTBR);
			List<Interval> aulas = new ArrayList<>();
			boolean keepGoing = true;
			for (int index = i; keepGoing; index++) {
				String dia = content.get(2).ownText();
				String horario = content.get(3).getElementsByTag("span").text();
				String[] horArr = horario.split("_às_");

				String horaInicio = horArr[0].replace(",", ":");
				String horaFim = horArr[1].replace(",", ":");

				DateTime start = fmt.parseDateTime(dia + horaInicio);
				DateTime end = fmt.parseDateTime(dia + horaFim);

				Interval interval = new Interval(start, end);
				aulas.add(interval);

				if (index + 1 < rows.size()) {
					content = rows.get(index + 1).getElementsByTag("div");
					if (content.get(0).ownText().contains(NBSP_CODE)) { //is not a continuation
						i++;
					} else {
						keepGoing = false;
					}
				} else {
					keepGoing = false;
				}
			}

			//(nome, cod, turma, sala, crd, carga, tipo, periodo, aulas);
			Discipline disc =
					Discipline.of(nome, cod, turma, sala, crd, carga, tipo, periodo, aulas);
			disciplinas.add(disc);
		}
		
		return disciplinas;
	}

	/* ----- Static Helper Methods ----- */

	public static Map<String, String> createFormParams(String html) {
		Map<String, String> map = new HashMap<>();

		final String submitButton = "Submit2";
		final String loginField = "txt_login";
		final String passwordField = "txt_senha";
		final String verificationField = "valorcontrole";

		Document doc = Jsoup.parse(html);

		Element loginForm = doc.getElementsByAttributeValue("name", "form1").first();
		Element imgVerif = loginForm
				                   .getElementsByAttributeValueStarting("src", "http://siga.ufmt" +
						                                                               ".br/www-siga/dll/pdf/Imagem")

				                   .first();
		String verifNumber = imgVerif.absUrl("src").replaceAll("[^0-9]", "");

		Elements inputElements = loginForm.getElementsByTag("input");

		for (Element inputElement : inputElements) {
			String key = inputElement.attr("name");
			String value = inputElement.attr("value");

			switch (key) {
				case submitButton:
					continue;
				case loginField:
					continue;
				case passwordField:
					continue;
				case verificationField:
					value = verifNumber;
					break;
				default:
					break;
			}

			map.put(key, value);
		}

		return map;
	}

}
