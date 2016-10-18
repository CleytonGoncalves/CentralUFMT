package com.cleytongoncalves.centralufmt.data.local;

import com.cleytongoncalves.centralufmt.data.model.Course;
import com.cleytongoncalves.centralufmt.data.model.Student;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HtmlHelper {
	private static final String NBSP_CODE = "\u00a0";

	private final Element mBodyHtml;
	private Course mCourse;

	public HtmlHelper(String html) {
		this.mBodyHtml = Jsoup.parse(html).body();
	}

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

	/**
	 * Creates the Student with a basic (essentials-only) Curso object.
	 * The proper parsing of the remaining info must be made in another thread
	 * by calling parseCompleteCourse().
	 *
	 * @return Student
	 */
	public Student parseBasicStudent() {
		List<TextNode> alunoInfoNodes = mBodyHtml.getElementsByTag("div").get(0).textNodes();

		mCourse = extractCurso(alunoInfoNodes);
		return extractStudent(alunoInfoNodes);
	}

	private Course extractCurso(List<TextNode> alunoInfoNodes) {
		String infoCursoRaw = alunoInfoNodes.get(0).text();
		String[] infoCurso = infoCursoRaw.split(":")[1].split("-");

		String id = infoCurso[0].trim();
		String name = capitalizeFirstLetter(infoCurso[1].trim());
		String type = capitalizeFirstLetter(infoCurso[2].trim());

		return new Course(name, id, type);
	}

	private Student extractStudent(List<TextNode> alunoInfoNodes) {
		String infoAlunoRaw = alunoInfoNodes.get(1).text();
		String[] infoAluno = infoAlunoRaw.split(":")[1].split("-");

		String rga = infoAluno[0].trim();
		String nomeCompleto = capitalizeFirstLetter(infoAluno[1].trim());

		String infoStatusRaw = alunoInfoNodes.get(2).text();
		String[] infoStatus = infoStatusRaw.split(":")[1].split("-");

		String currPeriodo = infoStatus[0].trim();
		boolean matriculado = infoStatus[1].trim().equalsIgnoreCase("Matriculado no Período");

		Student student = new Student(nomeCompleto, rga, mCourse);
		mCourse.setCurrentTermCode(currPeriodo);

		return student;
	}

	/* Static Helper Methods */
	private static String capitalizeFirstLetter(String text) {
		char[] newText = text.toCharArray();

		boolean makeLowerCase = false;
		for (int i = 0; i < newText.length; i++) {
			char currChar = newText[i];
			if (currChar == ' ') {
				makeLowerCase = i < newText.length - 3 &&
						                ((newText[i + 3] == ' ') || (newText[i + 2] == ' '));
			} else if (makeLowerCase) {
				newText[i] = Character.toLowerCase(currChar);
			} else {
				makeLowerCase = true;
			}
		}
		return String.valueOf(newText);
	}

	/**
	 * Parses the remaining course info.
	 * Should be called preferentially in a background thread.
	 */
	public void parseCompleteCourse() {
		Elements tables = mBodyHtml.getElementsByTag("table");
		parseCourseLoad(tables.get(0));
		parseTakenCourseLoad(tables.get(1));
	}

	private void parseCourseLoad(Element table) {
		Elements tds = table.getElementsByTag("td");

		//Tempo Medio e Maximo para Integralizacao do Curso
		String[] splitFirstTd = tds.get(0).text().split(NBSP_CODE);
		//[2] = Tempo Media, [5] Tempo Maximo
		String tempoMedioIntegralizar = splitFirstTd[2].trim();
		String tempoMaxIntegralizar = splitFirstTd[5].trim();

		String cargaHorTotal = tds.get(6).text();
		String cargaHorObg = tds.get(7).text();
		String cargaHorOpt = tds.get(8).text();
		String cargaHorComplem = tds.get(9).text();

		mCourse.setAvgFinishYears(tempoMedioIntegralizar);
		mCourse.setMaxFinishYears(tempoMaxIntegralizar);

		mCourse.setObligatoryCourseLoad(cargaHorObg);
		mCourse.setOptionalCourseLoad(cargaHorOpt);
		mCourse.setComplementaryCourseLoad(cargaHorComplem);
	}

	private void parseTakenCourseLoad(Element table) {
		Elements tds = table.getElementsByTag("td");

		String[] secTdSplit = tds.get(1).text().split(NBSP_CODE);
		String tempoIntegralizado = secTdSplit[2].trim().substring(0, 3);

		//Student completed course load
		String cargaHorTotal = tds.get(9).text();
		String cargaHorObg = tds.get(10).text();
		String cargaHorOpt = tds.get(11).text();
		String cargaHorCompl = tds.get(12).text();
		String cargaHorElet = tds.get(13).text();
		String cargaHorMatric = tds.get(14).text();

		mCourse.setFinishedYears(tempoIntegralizado);
		mCourse.setTakenObligatoryCourseLoad(cargaHorObg);
		mCourse.setTakenOptionalCourseLoad(cargaHorOpt);
		mCourse.setTakenComplementaryCourseLoad(cargaHorCompl);
		mCourse.setTakenElectiveCourseLoad(cargaHorElet);
		mCourse.setEnrolledCourseLoad(cargaHorMatric);
	}
}
