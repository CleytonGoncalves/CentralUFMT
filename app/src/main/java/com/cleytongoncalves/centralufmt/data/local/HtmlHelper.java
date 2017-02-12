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
	private static final Locale LOCALE_PTBR = new Locale("pt", "BR");
	private static final String NBSP_CODE = "\u00a0";
	
	private static final String ACCESS_DENIED_ERROR = "Acesso Negado";
	
	//TODO: ENSURE THAT ANY FIELD CAN FAIL WITHOUT CRASHING
	
	private HtmlHelper() {
	}
	
	public static Student parseStudent(String exacaoHtml) throws Exception {
		Element body = Jsoup.parse(exacaoHtml).body();
		List<TextNode> alunoInfoNodes = body.getElementsByTag("div").get(0).textNodes();
		
		Course course = extractCurso(alunoInfoNodes);
		return extractStudent(alunoInfoNodes, course);
	}
	
	private static Course extractCurso(List<TextNode> alunoInfoNodes) throws Exception {
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
	
	private static Student extractStudent(List<TextNode> alunoInfoNodes, Course course) throws Exception {
		String infoAlunoRaw = alunoInfoNodes.get(1).text();
		String[] infoAluno = infoAlunoRaw.split(":")[1].split("-");
		
		String rga = infoAluno[0].trim();
		String nomeCompleto = TextUtil.capsWordsFirstLetter(infoAluno[1].trim());
		
		return Student.of(nomeCompleto, rga, course);
	}
	
	private static void parseCurriculum(Element body) throws Exception {
		List<Discipline> curriculum = new ArrayList<>();
		
		//1st Tb = Carga horaria curso, 2nd = carga horaria cursada, 3rd = grade
		Element curriculumTb = body.getElementsByTag("table").get(2);
		//1st tr = header, depois cada 'tr' = cada disciplina; 'td' = coluna
		
		Elements rowList = curriculumTb.getElementsByTag("tr");
		
		for (int i = 1, rowListSize = rowList.size(); i < rowListSize; i++) {
			Element disciplineRow = rowList.get(i);
			Elements columnList = disciplineRow.getElementsByTag("td");
			
			final int codeAndTitleCol = 0;
			final int obligatoryCol = 1;
			final int termCol = 2;
			final int courseLoadCol = 3;
			final int periodTakenCol = 4;
			final int statusCol = 5;
			
			//Em ordem:
			//Cod - Titulo; Tipo (obg, opt); Semestre, cg. hor.; periodo cursou; estado (cg h.,
			// matric., falta cursar);
			String codeAndTitleText[] = columnList.get(codeAndTitleCol)
			                                      .text()
			                                      .replace(NBSP_CODE, "")
			                                      .trim()
			                                      .split(" - ");
			String code = codeAndTitleText[0].trim();
			String title = codeAndTitleText[1].trim();
			
			String obligatory = columnList.get(obligatoryCol).text().replace(NBSP_CODE, "").trim();
			String term = columnList.get(termCol).text().replace(NBSP_CODE, "").trim();
			String courseLoad = columnList.get(courseLoadCol).text().replace(NBSP_CODE, "").trim();
			String periodTaken =
					columnList.get(periodTakenCol).text().replace(NBSP_CODE, "").trim();
			
			String statusText = columnList.get(statusCol).text().replace(NBSP_CODE, "").trim();
		}
	}
	
	public static List<Discipline> parseSchedule(String html) throws Exception {
		Element body = Jsoup.parse(html).body();
		
		if (body.text().contains(ACCESS_DENIED_ERROR)) {
			throw new UnsupportedOperationException("Siga Access Denied");
		}
		
		Elements tables = body.getElementsByTag("table");
		
		//17=tabela horarios, 18=total creditos/carga horaria
		final int scheduleTable = 17;
		Element horarioTable = tables.get(scheduleTable);
		Elements rows = horarioTable.getElementsByTag("tr");
		
		List<Discipline> disciplinas = new ArrayList<>();
		
		//0=header, 1..n=disciplinas
		for (int i = 1, rowsSize = rows.size(); i < rowsSize; i++) {
			Element currRow = rows.get(i);
			Elements content = currRow.children();
			
			Discipline.Builder discBuilder = Discipline.builder();
			
			//0="cod-nome disc", 1=primeiro dia, 2=hora inicio, 3=hora fim, 4=turma, 5=sala, 6=crd?, 7=cargaHor,
			//8=tipo, 9=per. oferta
			String titleAndCode[] = content.get(0).ownText().split("-");
			discBuilder.code(titleAndCode[0]);
			discBuilder.title(TextUtil.capsWordsFirstLetter(titleAndCode[1]));
			
			discBuilder.group(content.get(4).ownText());
			discBuilder.room(content.get(5).ownText());
			discBuilder.crd(content.get(6).ownText());
			discBuilder.courseLoad(content.get(7).ownText());
			discBuilder.type(content.get(8).ownText());
			
			String periodo = content.get(9).ownText(); //TODO PERIODO != TERM?
			switch (periodo) {
				case "1":
					discBuilder.term("1º Semestre");
					break;
				case "2":
					discBuilder.term("2º Semestre");
					break;
				case "3":
					discBuilder.term("Anual");
					break;
				case "4":
					discBuilder.term("Modular");
					break;
			}
			
			List<Interval> aulas = new ArrayList<>();
			
			DateTimeFormatter fmt = DateTimeFormat.forPattern("EEEEHH:mm").withLocale(LOCALE_PTBR);
			boolean keepGoing = true;
			for (int index = i; keepGoing; index++) {
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
						i++;
					} else {
						keepGoing = false;
					}
				} else {
					keepGoing = false;
				}
			}
			
			discBuilder.classTimes(aulas);
			disciplinas.add(discBuilder.build());
		}
		
		return disciplinas;
	}

	/* ----- Static Helper Methods ----- */
	
	public static Map<String, String> parseSigaFormParams(String html) {
		Map<String, String> map = new HashMap<>();
		
		final String verifImgUrl = "http://academico-siga.ufmt.br/www-siga/dll/pdf/Imagem";
		final String submitButton = "Submit2";
		final String loginField = "txt_login";
		final String passwordField = "txt_senha";
		final String verificationField = "valorcontrole";
		
		Document doc = Jsoup.parse(html);
		
		Element loginForm = doc.getElementsByAttributeValue("name", "form1").first();
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
