package com.cleytongoncalves.centralufmt.data.local;

import com.cleytongoncalves.centralufmt.data.model.Course;
import com.cleytongoncalves.centralufmt.data.model.Student;
import com.cleytongoncalves.centralufmt.data.model.Subject;
import com.cleytongoncalves.centralufmt.data.model.SubjectClass;
import com.cleytongoncalves.centralufmt.util.Pair;
import com.cleytongoncalves.centralufmt.util.TextUtil;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class HtmlHelper {
	private static final Locale LOCALE_PTBR = new Locale("pt", "BR");
	private static final String NBSP_CODE = "\u00a0";
	
	//TODO: ENSURE THAT ANY FIELD CAN FAIL WITHOUT CRASHING
	
	private HtmlHelper() {
	}
	
	public static Course parseCourse(String exacaoHtml) throws Exception {
		Element body = Jsoup.parse(exacaoHtml).body();
		List<TextNode> alunoInfoNodes = body.getElementsByTag("div").get(0).textNodes();
		
		String infoCursoRaw = alunoInfoNodes.get(0).text();
		String[] infoCurso = infoCursoRaw.split(":")[1].split("-");
		
		Long code = Long.parseLong(infoCurso[0].trim());
		String title = TextUtil.capsWordsFirstLetter(infoCurso[1].trim());
		String type = TextUtil.capsWordsFirstLetter(infoCurso[2].trim());
		
		String infoTerm = alunoInfoNodes.get(2).text();
		
		Matcher matcher = Pattern.compile("\\d(?!.*\\d)").matcher(infoTerm);
		String term = "";
		if (matcher.find()) { term = matcher.group(); }
		
		return new Course(code, title, type, term);
	}
	
	public static Student parseStudent(String exacaoHtml) throws Exception {
		Element body = Jsoup.parse(exacaoHtml).body();
		List<TextNode> alunoInfoNodes = body.getElementsByTag("div").get(0).textNodes();
		
		String infoAlunoRaw = alunoInfoNodes.get(1).text();
		String[] infoAluno = infoAlunoRaw.split(":")[1].split("-");
		
		Long rga = Long.parseLong(infoAluno[0].trim());
		String fullName = TextUtil.capsWordsFirstLetter(infoAluno[1].trim());
		
		return new Student(rga, fullName);
	}
	
	public static List<Subject> parseCurriculum(String exacaoHtml) throws Exception {
		Element body = Jsoup.parse(exacaoHtml).body();
		List<Subject> curriculum = new ArrayList<>();
		
		//1st Tb = Carga horaria curso, 2nd = carga horaria cursada, 3rd = grade
		Element curriculumTb = body.getElementsByTag("table").get(2);
		
		//1st tr = header, depois cada 'tr' = cada disciplina; 'td' = coluna
		Elements rowList = curriculumTb.getElementsByTag("tr");

		for (int i = 1, rowListSize = rowList.size(); i < rowListSize; i++) {
			Element disciplineRow = rowList.get(i);
			Elements columnList = disciplineRow.getElementsByTag("td");

			final int codeAndTitleCol = 0;
			//final int obligatoryCol = 1; optional -> term = 99
			final int termCol = 2;
			final int courseLoadCol = 3;
			//final int periodTakenCol = 4;
			final int statusCol = 5;

			//Em ordem:
			//Cod - Titulo; Tipo (obg, opt); Semestre, cg. hor.; periodo cursou; estado (cg h.,
			// matric., falta cursar);
			String codeAndTitleText[] = columnList.get(codeAndTitleCol)
			                                      .text()
			                                      .replace(NBSP_CODE, "")
			                                      .trim()
			                                      .split(" - ");
			Long code = Long.parseLong(codeAndTitleText[0].trim());
			String title = codeAndTitleText[1].trim();

			String term = columnList.get(termCol).text().replace(NBSP_CODE, "").trim();
			String courseLoad = columnList.get(courseLoadCol).text().replace(NBSP_CODE, "").trim();
			String statusText = columnList.get(statusCol).text().replace(NBSP_CODE, "").trim();
			
			int status;
			if ("FALTA CURSAR".equals(statusText)) {
				status = Subject.FUTURE;
			} else if ("Matriculada".equals(statusText)) {
				status = Subject.ENROLLED;
			} else if (statusText.isEmpty()) {
				status = Subject.OPTIONAL;
			} else {
				status = Subject.PAST;
			}
			
			Subject subject = new Subject(code, title, courseLoad, term, status);
			curriculum.add(subject);
		}
		
		return curriculum;
	}
	
	public static List<SubjectClass> parseSchedule(String html) throws Exception {
		Element body = Jsoup.parse(html).body();
		
		Elements borderElements = body.getElementsByAttributeValue("border", "1");
		
		//0=tabela horarios
		final int scheduleTable = 0;
		Element horarioTable = borderElements.get(scheduleTable);
		Elements rows = horarioTable.getElementsByTag("tr");
		
		List<SubjectClass> classes = new ArrayList<>();
		
		//0=header, 1..n=classes
		for (int i = 1, rowsSize = rows.size(); i < rowsSize; i++) {
			Element currRow = rows.get(i);
			Elements content = currRow.children();
			
			//0="cod-nome disc", 1=primeiro dia, 2=hora inicio, 3=hora fim, 4=turma, 5=sala,
			//6=crd?, 7=cargaHor, 8=tipo, 9=per. oferta
			String titleAndCode[] = content.get(0).ownText().split("-");
			Long subjectCode = Long.parseLong(titleAndCode[0]);
			//String title = TextUtil.capsWordsFirstLetter(titleAndCode[1]);
			
			String group = content.get(4).ownText();
			String room = content.get(5).ownText();
			String crd = content.get(6).ownText();
			//String courseLoad = content.get(7).ownText();
			String type = content.get(8).ownText();
			
			//String periodo = content.get(9).ownText();
			
			Pair<List<Interval>, Integer> pair = parseClassTimes(content, rows, i);
			List<Interval> aulas = pair.getItem1(); //Lista de aulas
			i = pair.getItem2(); //Outras materias a partir daqui
			
			SubjectClass subjectClass =
					new SubjectClass(subjectCode, group, room, crd, type, aulas);
			
			classes.add(subjectClass);
		}
		
		return classes;
	}
	
	private static Pair<List<Interval>, Integer> parseClassTimes(Elements content, Elements rows,
	                                                             int pos) throws Exception {
		List<Interval> aulas = new ArrayList<>();
		
		DateTimeFormatter fmt = DateTimeFormat.forPattern("EEEEHH:mm").withLocale(LOCALE_PTBR);
		boolean keepGoing = true;
		for (int index = pos; keepGoing; index++) {
			String dia = content.get(1).ownText();
			String horaInicio = content.get(2).ownText();
			String horaFim = content.get(3).ownText();
			
			//Format WED14:30
			DateTime start = fmt.parseDateTime(dia + horaInicio);
			DateTime end = fmt.parseDateTime(dia + horaFim);
			
			Interval interval = new Interval(start, end);
			aulas.add(interval);
			
			if (index + 1 < rows.size()) {
				content = rows.get(index + 1).children();
				if (content.get(0).ownText().contains(NBSP_CODE)) { //is a continuation
					pos++;
				} else {
					keepGoing = false;
				}
			} else {
				keepGoing = false;
			}
		}
		
		return new Pair<>(aulas, pos);
	}
	
	/* ----- Static Helper Methods ----- */
	
	public static Map<String, String> parseSigaFormParams(String html) throws Exception {
		if (html == null || html.isEmpty()) {
			throw new IllegalArgumentException("Invalid Siga HTML");
		}
		
		Element body = Jsoup.parse(html).body();
		
		String bodyText = body.text();
		if (bodyText.contains("ERRO")) {
			throw new IllegalArgumentException("Siga HTML might have errors: " + bodyText);
		}
		
		Map<String, String> map = new HashMap<>();
		
		final String verifImgUrl = "http://academico-siga.ufmt.br/www-siga/dll/pdf/Imagem";
		final String submitButton = "Submit2";
		final String loginField = "txt_login";
		final String passwordField = "txt_senha";
		final String verificationField = "valorcontrole";
		
		Element loginForm = body.getElementsByAttributeValue("name", "form1").first();
		Element imgVerif = loginForm
				                   .getElementsByAttributeValueStarting("src", verifImgUrl)
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
