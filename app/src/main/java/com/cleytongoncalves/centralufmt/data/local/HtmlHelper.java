package com.cleytongoncalves.centralufmt.data.local;

import com.cleytongoncalves.centralufmt.data.model.Course;
import com.cleytongoncalves.centralufmt.data.model.Discipline;
import com.cleytongoncalves.centralufmt.data.model.Student;

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
		String title = capitalizeFirstLetterOnSentence(infoCurso[1].trim());
		String type = capitalizeFirstLetterOnSentence(infoCurso[2].trim());

		String infoTerm = alunoInfoNodes.get(2).text();
		String[] infoTermSplit = infoTerm.split(":")[1].split(" ");
		String term = infoTermSplit[0];

		return new Course(title, code, type, term);
	}

	private static Student extractStudent(List<TextNode> alunoInfoNodes, Course course) {
		String infoAlunoRaw = alunoInfoNodes.get(1).text();
		String[] infoAluno = infoAlunoRaw.split(":")[1].split("-");

		String rga = infoAluno[0].trim();
		String nomeCompleto = capitalizeFirstLetterOnSentence(infoAluno[1].trim());

		Student student = new Student(nomeCompleto, rga, course);

		return student;
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

			String nome = capitalizeFirstLetterOnSentence(content.get(1).ownText());
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

			Discipline disc = new Discipline(nome, cod, turma, sala, crd,
					                                carga, tipo, periodo, aulas);
			disciplinas.add(disc);
		}
		
		return disciplinas;
	}

	/* ----- Static Helper Methods ----- */

	private static String capitalizeFirstLetterOnSentence(String text) {
		char[] newText = text.toUpperCase().toCharArray();

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

	/* TEMPORARY */
	public static String getScheduleHtml() {
		return "\n" +
				       "<html>\n" +
				       "\n" +
				       "<head>\n" +
				       "    <meta http-equiv=\"Content-Type\" content=\"text/html; " +
				       "charset=windows-1252\">\n" +
				       "    <title>\n" +
				       "    </title>\n" +
				       "    <style>\n" +
				       "        .CAMPO {\n" +
				       "            BACKGROUND-COLOR: #FFFFFF;\n" +
				       "            font-family: Verdana, Arial, Helvetica, sans-serif;\n" +
				       "            font-size: 11px;\n" +
				       "            font-style: normal;\n" +
				       "            cursor: text;\n" +
				       "            page-break-before: auto;\n" +
				       "            page-break-after: auto;\n" +
				       "            border: auto #000000 none\" }\n" +
				       "\n" +
				       "    </style>\n" +
				       "</head>\n" +
				       "\n" +
				       "<body>\n" +
				       "    <div align=\"left\">\n" +
				       "        <table width=\"78%\" style=\"border-collapse: collapse\" " +
				       "bordercolor=\"#111111\" cellpadding=\"0\" cellspacing=\"0\" " +
				       "height=\"80\">\n" +
				       "            <tbody>\n" +
				       "                <tr>\n" +
				       "                    <td width=\"30%\" height=\"83\"> <img src=\"" +
				       "./HorarioAluno_files/Ufmt.gif\" align=\"left\" width=\"50\" " +
				       "height=\"60\">\n" +
				       "                        <div align=\"left\">\n" +
				       "                            <table width=\"86%\" style=\"border-collapse:" +
				       " " +
				       "collapse\" bordercolor=\"#111111\" cellpadding=\"0\" " +
				       "cellspacing=\"0\">\n" +
				       "                                <tbody>\n" +
				       "                                    <tr>\n" +
				       "                                        <td class=\"CAMPO\" width=\"400\"" +
				       " " +
				       "align=\"left\"><font size=\"1\">FUNDAÇÃO\n" +
				       "                    UNIVERSIDADE FEDERAL DE MATO GROSSO </font>\n" +
				       "                                        </td>\n" +
				       "                                    </tr>\n" +
				       "                                    <tr>\n" +
				       "                                        <td class=\"CAMPO\" width=\"400\"" +
				       " " +
				       "align=\"left\"><font size=\"1\">SISTEMA\n" +
				       "                    DE INFORMAÇÕES GERENCIAIS ACADÊMICAS </font>\n" +
				       "                                        </td>\n" +
				       "                                    </tr>\n" +
				       "                                    <tr>\n" +
				       "                                        <td class=\"CAMPO\" width=\"400\"" +
				       " " +
				       "align=\"left\"><font size=\"1\">COORDENAÇÃO\n" +
				       "                    DE PROCESSAMENTO DE DADOS - PROPLAN </font>\n" +
				       "                                        </td>\n" +
				       "                                    </tr>\n" +
				       "                                    <tr>\n" +
				       "                                        <td class=\"CAMPO\" width=\"400\"" +
				       " " +
				       "align=\"left\"><font size=\"1\">COORDENAÇÃO \n" +
				       "                    DE ADMINISTRAÇÃO ESCOLAR - PROEG </font>\n" +
				       "                                        </td>\n" +
				       "                                    </tr>\n" +
				       "                                </tbody>\n" +
				       "                            </table>\n" +
				       "                        </div>\n" +
				       "                    </td>\n" +
				       "                </tr>\n" +
				       "            </tbody>\n" +
				       "        </table>\n" +
				       "    </div>\n" +
				       "    <table>\n" +
				       "        <tbody>\n" +
				       "            <tr>\n" +
				       "                <td>\n" +
				       "                    <br>\n" +
				       "                </td>\n" +
				       "            </tr>\n" +
				       "            <tr>\n" +
				       "                <td><font face=\"Verdana, Arial, Helvetica, sans-serif\"" +
				       " " +
				       "size=\"2\">\n" +
				       "    <font color=\"#003399\"> <b>PLANILHA DE HORÁRIO DO " +
				       "ALUNO</b></font></font>\n" +
				       "                </td>\n" +
				       "            </tr>\n" +
				       "        </tbody>\n" +
				       "    </table>\n" +
				       "\n" +
				       "\n" +
				       "\n" +
				       "\n" +
				       "    <form name=\"AdapterForm1\" method=\"post\" action=\"http://sia.ufmt" +
				       ".br/www-siga/WebSnap/C_PlanilhaHorario/planilhaHorariodoAluno" +
				       ".dll/HorarioAluno\">\n" +
				       "        <input type=\"hidden\" name=\"__act\">\n" +
				       "        <table style=\"background-color: #F8F8F8;\">\n" +
				       "            <tbody>\n" +
				       "                <tr>\n" +
				       "                    <td>\n" +
				       "\n" +
				       "                    </td>\n" +
				       "                </tr>\n" +
				       "                <tr>\n" +
				       "                    <td>\n" +
				       "                        <table style=\"font-family: &#39;Verdana&#39;; " +
				       "font-size: 8pt; font-weight: bold; color:#000000;" +
				       "background-color:#FFFFFF;" +
				       " \">\n" +
				       "\n" +
				       "                            <tbody>\n" +
				       "                                <tr>\n" +
				       "                                    <td>HORÁRIO DO ALUNO NO PERÍODO DE " +
				       "-</td>\n" +
				       "                                    <td><span style=\"font-family: &#39;" +
				       "Verdana&#39;; font-size: 9pt; font-weight: bold; color:#003399; \">20161" +
				       " " +
				       "</span>\n" +
				       "                                    </td>\n" +
				       "                                </tr>\n" +
				       "\n" +
				       "                            </tbody>\n" +
				       "                        </table>\n" +
				       "                    </td>\n" +
				       "                    <input type=\"hidden\" name=\"__am" +
				       ".DS_AdptHorarioAluno\" value=\"\">\n" +
				       "\n" +
				       "\n" +
				       "                </tr>\n" +
				       "                <tr>\n" +
				       "                    <td>\n" +
				       "                        <table style=\"font-family: &#39;Verdana&#39;; " +
				       "font-size: 8pt; font-weight: bold; color:#000000;" +
				       "background-color:#FFFFFF;" +
				       " \">\n" +
				       "\n" +
				       "                            <tbody>\n" +
				       "                                <tr>\n" +
				       "                                    <td>CURSO DE -</td>\n" +
				       "                                    <td><span style=\"font-family: &#39;" +
				       "Verdana&#39;; font-size: 9pt; font-weight: bold; color:#003399; " +
				       "\">CIENCIA" +
				       " DA COMPUTACAO - BACHARELADO </span>\n" +
				       "                                    </td>\n" +
				       "                                </tr>\n" +
				       "\n" +
				       "                            </tbody>\n" +
				       "                        </table>\n" +
				       "                    </td>\n" +
				       "                    <input type=\"hidden\" name=\"__am" +
				       ".DS_AdptHorarioAluno\" value=\"\">\n" +
				       "\n" +
				       "\n" +
				       "                </tr>\n" +
				       "                <tr>\n" +
				       "                    <td>\n" +
				       "                        <table style=\"font-family: &#39;Verdana&#39;; " +
				       "font-size: 8pt; font-weight: bold; color:#000000;" +
				       "background-color:#FFFFFF;" +
				       " \">\n" +
				       "                            <tbody>\n" +
				       "                                <tr>\n" +
				       "                                    <td style=\"font-family: &#39;" +
				       "Verdana&#39;; font-size: 8pt; font-weight: bold; color:#000000;" +
				       "background-color:#FFFFFF; \">\n" +
				       "\n" +
				       "\n" +
				       "                                        <table style=\"font-family: " +
				       "&#39;" +
				       "Verdana&#39;; font-size: 8pt; font-weight: bold; color:#000000;" +
				       "background-color:#FFFFFF; \">\n" +
				       "                                            <tbody>\n" +
				       "                                                <tr>\n" +
				       "                                                    <td>ALUNO (A) " +
				       "-</td>\n" +
				       "                                                    <td><span " +
				       "style=\"font-family: &#39;Verdana&#39;; font-size: 9pt; font-weight: " +
				       "bold;" +
				       " color:#003399; \">201611310023 </span>\n" +
				       "                                                    </td>\n" +
				       "\n" +
				       "\n" +
				       "                                                    <td>- </td>\n" +
				       "                                                    <td><span " +
				       "style=\"font-family: &#39;Verdana&#39;; font-size: 9pt; font-weight: " +
				       "bold;" +
				       " color:#003399; \">CLEYTON CAMPOS GONCALVES </span>\n" +
				       "                                                    </td>\n" +
				       "\n" +
				       "                                                </tr>\n" +
				       "                                            </tbody>\n" +
				       "                                        </table>\n" +
				       "                                    </td>\n" +
				       "                                </tr>\n" +
				       "                            </tbody>\n" +
				       "                        </table>\n" +
				       "                    </td>\n" +
				       "                    <input type=\"hidden\" name=\"__am" +
				       ".DS_AdptHorarioAluno\" value=\"\">\n" +
				       "\n" +
				       "\n" +
				       "                </tr>\n" +
				       "                <tr>\n" +
				       "                    <td>\n" +
				       "                        <table cellspacing=\"1\" cellpadding=\"4\" " +
				       "border=\"1\">\n" +
				       "                            <tbody>\n" +
				       "                                <tr style=\"font-family: &#39;" +
				       "Verdana&#39;" +
				       "; font-size: 8pt; font-weight: bold; color:#000000;" +
				       "background-color:#59ACFF; \">\n" +
				       "\n" +
				       "                                    <th>CÓDIGO</th>\n" +
				       "\n" +
				       "\n" +
				       "                                    <th>DISCIPLINAS MATRICULADAS</th>\n" +
				       "\n" +
				       "\n" +
				       "                                    <th>DIA</th>\n" +
				       "\n" +
				       "\n" +
				       "                                    <th>HORÁRIO</th>\n" +
				       "\n" +
				       "\n" +
				       "                                    <th>TURMA</th>\n" +
				       "\n" +
				       "\n" +
				       "                                    <th>SALA</th>\n" +
				       "\n" +
				       "\n" +
				       "                                    <th>CRD</th>\n" +
				       "\n" +
				       "\n" +
				       "                                    <th>C.H.</th>\n" +
				       "\n" +
				       "\n" +
				       "                                    <th>TIPO AULA</th>\n" +
				       "\n" +
				       "\n" +
				       "                                    <th>PERÍODO OFERTA</th>\n" +
				       "\n" +
				       "                                </tr>\n" +
				       "\n" +
				       "                                <tr style=\"font-family: &#39;Arial&#39;;" +
				       " " +
				       "font-size: 8pt;  color:#000000;\">\n" +
				       "\n" +
				       "                                    <td>\n" +
				       "                                        <div>30326950</div>\n" +
				       "                                    </td>\n" +
				       "\n" +
				       "\n" +
				       "                                    <td>\n" +
				       "                                        <div>FÍSICA PARA COMPUTAÇÃO " +
				       "I</div>\n" +
				       "                                    </td>\n" +
				       "\n" +
				       "\n" +
				       "                                    <td align=\"center\">\n" +
				       "                                        <div>TER</div>\n" +
				       "                                    </td>\n" +
				       "\n" +
				       "\n" +
				       "                                    <td>\n" +
				       "                                        <div><span style=\"font-size: " +
				       "9pt;" +
				       "\">13,30_às_15,30</span>\n" +
				       "                                        </div>\n" +
				       "                                    </td>\n" +
				       "\n" +
				       "\n" +
				       "                                    <td align=\"center\">\n" +
				       "                                        <div>CC </div>\n" +
				       "                                    </td>\n" +
				       "\n" +
				       "\n" +
				       "                                    <td align=\"center\">\n" +
				       "                                        <div> </div>\n" +
				       "                                    </td>\n" +
				       "\n" +
				       "\n" +
				       "                                    <td align=\"center\">\n" +
				       "                                        <div><span 99=\"\">4</span>\n" +
				       "                                        </div>\n" +
				       "                                    </td>\n" +
				       "\n" +
				       "\n" +
				       "                                    <td align=\"center\">\n" +
				       "                                        <div>60</div>\n" +
				       "                                    </td>\n" +
				       "\n" +
				       "\n" +
				       "                                    <td align=\"center\">\n" +
				       "                                        <div>T</div>\n" +
				       "                                    </td>\n" +
				       "\n" +
				       "\n" +
				       "                                    <td align=\"center\">\n" +
				       "                                        <div>1</div>\n" +
				       "                                    </td>\n" +
				       "\n" +
				       "                                </tr>\n" +
				       "\n" +
				       "                                <tr style=\"font-family: &#39;Arial&#39;;" +
				       " " +
				       "font-size: 8pt;  color:#000000;\">\n" +
				       "\n" +
				       "                                    <td>\n" +
				       "                                        <div>&nbsp;</div>\n" +
				       "                                    </td>\n" +
				       "\n" +
				       "\n" +
				       "                                    <td>\n" +
				       "                                        <div>&nbsp;</div>\n" +
				       "                                    </td>\n" +
				       "\n" +
				       "\n" +
				       "                                    <td align=\"center\">\n" +
				       "                                        <div>QUI</div>\n" +
				       "                                    </td>\n" +
				       "\n" +
				       "\n" +
				       "                                    <td>\n" +
				       "                                        <div><span style=\"font-size: " +
				       "9pt;" +
				       "\">13,30_às_15,30</span>\n" +
				       "                                        </div>\n" +
				       "                                    </td>\n" +
				       "\n" +
				       "\n" +
				       "                                    <td align=\"center\">\n" +
				       "                                        <div>&nbsp;</div>\n" +
				       "                                    </td>\n" +
				       "\n" +
				       "\n" +
				       "                                    <td align=\"center\">\n" +
				       "                                        <div> </div>\n" +
				       "                                    </td>\n" +
				       "\n" +
				       "\n" +
				       "                                    <td align=\"center\">\n" +
				       "                                        <div><span 99=\"\">&nbsp;" +
				       "</span>\n" +
				       "                                        </div>\n" +
				       "                                    </td>\n" +
				       "\n" +
				       "\n" +
				       "                                    <td align=\"center\">\n" +
				       "                                        <div>&nbsp;</div>\n" +
				       "                                    </td>\n" +
				       "\n" +
				       "\n" +
				       "                                    <td align=\"center\">\n" +
				       "                                        <div>T</div>\n" +
				       "                                    </td>\n" +
				       "\n" +
				       "\n" +
				       "                                    <td align=\"center\">\n" +
				       "                                        <div>1</div>\n" +
				       "                                    </td>\n" +
				       "\n" +
				       "                                </tr>\n" +
				       "\n" +
				       "                                <tr style=\"font-family: &#39;Arial&#39;;" +
				       " " +
				       "font-size: 8pt;  color:#000000;\">\n" +
				       "\n" +
				       "                                    <td>\n" +
				       "                                        <div>30401291</div>\n" +
				       "                                    </td>\n" +
				       "\n" +
				       "\n" +
				       "                                    <td>\n" +
				       "                                        <div>CALCULO I</div>\n" +
				       "                                    </td>\n" +
				       "\n" +
				       "\n" +
				       "                                    <td align=\"center\">\n" +
				       "                                        <div>SEG</div>\n" +
				       "                                    </td>\n" +
				       "\n" +
				       "\n" +
				       "                                    <td>\n" +
				       "                                        <div><span style=\"font-size: " +
				       "9pt;" +
				       "\">13,30_às_15,30</span>\n" +
				       "                                        </div>\n" +
				       "                                    </td>\n" +
				       "\n" +
				       "\n" +
				       "                                    <td align=\"center\">\n" +
				       "                                        <div>CC </div>\n" +
				       "                                    </td>\n" +
				       "\n" +
				       "\n" +
				       "                                    <td align=\"center\">\n" +
				       "                                        <div> </div>\n" +
				       "                                    </td>\n" +
				       "\n" +
				       "\n" +
				       "                                    <td align=\"center\">\n" +
				       "                                        <div><span 99=\"\">6</span>\n" +
				       "                                        </div>\n" +
				       "                                    </td>\n" +
				       "\n" +
				       "\n" +
				       "                                    <td align=\"center\">\n" +
				       "                                        <div>90</div>\n" +
				       "                                    </td>\n" +
				       "\n" +
				       "\n" +
				       "                                    <td align=\"center\">\n" +
				       "                                        <div>T</div>\n" +
				       "                                    </td>\n" +
				       "\n" +
				       "\n" +
				       "                                    <td align=\"center\">\n" +
				       "                                        <div>1</div>\n" +
				       "                                    </td>\n" +
				       "\n" +
				       "                                </tr>\n" +
				       "\n" +
				       "                                <tr style=\"font-family: &#39;Arial&#39;;" +
				       " " +
				       "font-size: 8pt;  color:#000000;\">\n" +
				       "\n" +
				       "                                    <td>\n" +
				       "                                        <div>&nbsp;</div>\n" +
				       "                                    </td>\n" +
				       "\n" +
				       "\n" +
				       "                                    <td>\n" +
				       "                                        <div>&nbsp;</div>\n" +
				       "                                    </td>\n" +
				       "\n" +
				       "\n" +
				       "                                    <td align=\"center\">\n" +
				       "                                        <div>QUA</div>\n" +
				       "                                    </td>\n" +
				       "\n" +
				       "\n" +
				       "                                    <td>\n" +
				       "                                        <div><span style=\"font-size: " +
				       "9pt;" +
				       "\">13,30_às_15,30</span>\n" +
				       "                                        </div>\n" +
				       "                                    </td>\n" +
				       "\n" +
				       "\n" +
				       "                                    <td align=\"center\">\n" +
				       "                                        <div>&nbsp;</div>\n" +
				       "                                    </td>\n" +
				       "\n" +
				       "\n" +
				       "                                    <td align=\"center\">\n" +
				       "                                        <div> </div>\n" +
				       "                                    </td>\n" +
				       "\n" +
				       "\n" +
				       "                                    <td align=\"center\">\n" +
				       "                                        <div><span 99=\"\">&nbsp;" +
				       "</span>\n" +
				       "                                        </div>\n" +
				       "                                    </td>\n" +
				       "\n" +
				       "\n" +
				       "                                    <td align=\"center\">\n" +
				       "                                        <div>&nbsp;</div>\n" +
				       "                                    </td>\n" +
				       "\n" +
				       "\n" +
				       "                                    <td align=\"center\">\n" +
				       "                                        <div>T</div>\n" +
				       "                                    </td>\n" +
				       "\n" +
				       "\n" +
				       "                                    <td align=\"center\">\n" +
				       "                                        <div>1</div>\n" +
				       "                                    </td>\n" +
				       "\n" +
				       "                                </tr>\n" +
				       "\n" +
				       "                                <tr style=\"font-family: &#39;Arial&#39;;" +
				       " " +
				       "font-size: 8pt;  color:#000000;\">\n" +
				       "\n" +
				       "                                    <td>\n" +
				       "                                        <div>&nbsp;</div>\n" +
				       "                                    </td>\n" +
				       "\n" +
				       "\n" +
				       "                                    <td>\n" +
				       "                                        <div>&nbsp;</div>\n" +
				       "                                    </td>\n" +
				       "\n" +
				       "\n" +
				       "                                    <td align=\"center\">\n" +
				       "                                        <div>SEX</div>\n" +
				       "                                    </td>\n" +
				       "\n" +
				       "\n" +
				       "                                    <td>\n" +
				       "                                        <div><span style=\"font-size: " +
				       "9pt;" +
				       "\">13,30_às_15,30</span>\n" +
				       "                                        </div>\n" +
				       "                                    </td>\n" +
				       "\n" +
				       "\n" +
				       "                                    <td align=\"center\">\n" +
				       "                                        <div>&nbsp;</div>\n" +
				       "                                    </td>\n" +
				       "\n" +
				       "\n" +
				       "                                    <td align=\"center\">\n" +
				       "                                        <div> </div>\n" +
				       "                                    </td>\n" +
				       "\n" +
				       "\n" +
				       "                                    <td align=\"center\">\n" +
				       "                                        <div><span 99=\"\">&nbsp;" +
				       "</span>\n" +
				       "                                        </div>\n" +
				       "                                    </td>\n" +
				       "\n" +
				       "\n" +
				       "                                    <td align=\"center\">\n" +
				       "                                        <div>&nbsp;</div>\n" +
				       "                                    </td>\n" +
				       "\n" +
				       "\n" +
				       "                                    <td align=\"center\">\n" +
				       "                                        <div>T</div>\n" +
				       "                                    </td>\n" +
				       "\n" +
				       "\n" +
				       "                                    <td align=\"center\">\n" +
				       "                                        <div>1</div>\n" +
				       "                                    </td>\n" +
				       "\n" +
				       "                                </tr>\n" +
				       "\n" +
				       "                                <tr style=\"font-family: &#39;Arial&#39;;" +
				       " " +
				       "font-size: 8pt;  color:#000000;\">\n" +
				       "\n" +
				       "                                    <td>\n" +
				       "                                        <div>30412846</div>\n" +
				       "                                    </td>\n" +
				       "\n" +
				       "\n" +
				       "                                    <td>\n" +
				       "                                        <div>VETORES E GEOMETRIA " +
				       "ANALITICA</div>\n" +
				       "                                    </td>\n" +
				       "\n" +
				       "\n" +
				       "                                    <td align=\"center\">\n" +
				       "                                        <div>SEG</div>\n" +
				       "                                    </td>\n" +
				       "\n" +
				       "\n" +
				       "                                    <td>\n" +
				       "                                        <div><span style=\"font-size: " +
				       "9pt;" +
				       "\">15,30_às_17,30</span>\n" +
				       "                                        </div>\n" +
				       "                                    </td>\n" +
				       "\n" +
				       "\n" +
				       "                                    <td align=\"center\">\n" +
				       "                                        <div>CC </div>\n" +
				       "                                    </td>\n" +
				       "\n" +
				       "\n" +
				       "                                    <td align=\"center\">\n" +
				       "                                        <div> </div>\n" +
				       "                                    </td>\n" +
				       "\n" +
				       "\n" +
				       "                                    <td align=\"center\">\n" +
				       "                                        <div><span 99=\"\">6</span>\n" +
				       "                                        </div>\n" +
				       "                                    </td>\n" +
				       "\n" +
				       "\n" +
				       "                                    <td align=\"center\">\n" +
				       "                                        <div>90</div>\n" +
				       "                                    </td>\n" +
				       "\n" +
				       "\n" +
				       "                                    <td align=\"center\">\n" +
				       "                                        <div>T</div>\n" +
				       "                                    </td>\n" +
				       "\n" +
				       "\n" +
				       "                                    <td align=\"center\">\n" +
				       "                                        <div>1</div>\n" +
				       "                                    </td>\n" +
				       "\n" +
				       "                                </tr>\n" +
				       "\n" +
				       "                                <tr style=\"font-family: &#39;Arial&#39;;" +
				       " " +
				       "font-size: 8pt;  color:#000000;\">\n" +
				       "\n" +
				       "                                    <td>\n" +
				       "                                        <div>&nbsp;</div>\n" +
				       "                                    </td>\n" +
				       "\n" +
				       "\n" +
				       "                                    <td>\n" +
				       "                                        <div>&nbsp;</div>\n" +
				       "                                    </td>\n" +
				       "\n" +
				       "\n" +
				       "                                    <td align=\"center\">\n" +
				       "                                        <div>QUA</div>\n" +
				       "                                    </td>\n" +
				       "\n" +
				       "\n" +
				       "                                    <td>\n" +
				       "                                        <div><span style=\"font-size: " +
				       "9pt;" +
				       "\">15,30_às_17,30</span>\n" +
				       "                                        </div>\n" +
				       "                                    </td>\n" +
				       "\n" +
				       "\n" +
				       "                                    <td align=\"center\">\n" +
				       "                                        <div>&nbsp;</div>\n" +
				       "                                    </td>\n" +
				       "\n" +
				       "\n" +
				       "                                    <td align=\"center\">\n" +
				       "                                        <div> </div>\n" +
				       "                                    </td>\n" +
				       "\n" +
				       "\n" +
				       "                                    <td align=\"center\">\n" +
				       "                                        <div><span 99=\"\">&nbsp;" +
				       "</span>\n" +
				       "                                        </div>\n" +
				       "                                    </td>\n" +
				       "\n" +
				       "\n" +
				       "                                    <td align=\"center\">\n" +
				       "                                        <div>&nbsp;</div>\n" +
				       "                                    </td>\n" +
				       "\n" +
				       "\n" +
				       "                                    <td align=\"center\">\n" +
				       "                                        <div>T</div>\n" +
				       "                                    </td>\n" +
				       "\n" +
				       "\n" +
				       "                                    <td align=\"center\">\n" +
				       "                                        <div>1</div>\n" +
				       "                                    </td>\n" +
				       "\n" +
				       "                                </tr>\n" +
				       "\n" +
				       "                                <tr style=\"font-family: &#39;Arial&#39;;" +
				       " " +
				       "font-size: 8pt;  color:#000000;\">\n" +
				       "\n" +
				       "                                    <td>\n" +
				       "                                        <div>&nbsp;</div>\n" +
				       "                                    </td>\n" +
				       "\n" +
				       "\n" +
				       "                                    <td>\n" +
				       "                                        <div>&nbsp;</div>\n" +
				       "                                    </td>\n" +
				       "\n" +
				       "\n" +
				       "                                    <td align=\"center\">\n" +
				       "                                        <div>SEX</div>\n" +
				       "                                    </td>\n" +
				       "\n" +
				       "\n" +
				       "                                    <td>\n" +
				       "                                        <div><span style=\"font-size: " +
				       "9pt;" +
				       "\">15,30_às_17,30</span>\n" +
				       "                                        </div>\n" +
				       "                                    </td>\n" +
				       "\n" +
				       "\n" +
				       "                                    <td align=\"center\">\n" +
				       "                                        <div>&nbsp;</div>\n" +
				       "                                    </td>\n" +
				       "\n" +
				       "\n" +
				       "                                    <td align=\"center\">\n" +
				       "                                        <div> </div>\n" +
				       "                                    </td>\n" +
				       "\n" +
				       "\n" +
				       "                                    <td align=\"center\">\n" +
				       "                                        <div><span 99=\"\">&nbsp;" +
				       "</span>\n" +
				       "                                        </div>\n" +
				       "                                    </td>\n" +
				       "\n" +
				       "\n" +
				       "                                    <td align=\"center\">\n" +
				       "                                        <div>&nbsp;</div>\n" +
				       "                                    </td>\n" +
				       "\n" +
				       "\n" +
				       "                                    <td align=\"center\">\n" +
				       "                                        <div>T</div>\n" +
				       "                                    </td>\n" +
				       "\n" +
				       "\n" +
				       "                                    <td align=\"center\">\n" +
				       "                                        <div>1</div>\n" +
				       "                                    </td>\n" +
				       "\n" +
				       "                                </tr>\n" +
				       "\n" +
				       "                                <tr style=\"font-family: &#39;Arial&#39;;" +
				       " " +
				       "font-size: 8pt;  color:#000000;\">\n" +
				       "\n" +
				       "                                    <td>\n" +
				       "                                        <div>30829290</div>\n" +
				       "                                    </td>\n" +
				       "\n" +
				       "\n" +
				       "                                    <td>\n" +
				       "                                        <div>FUNDAMENTOS DA " +
				       "COMPUTAÇÃO</div>\n" +
				       "                                    </td>\n" +
				       "\n" +
				       "\n" +
				       "                                    <td align=\"center\">\n" +
				       "                                        <div>QUA</div>\n" +
				       "                                    </td>\n" +
				       "\n" +
				       "\n" +
				       "                                    <td>\n" +
				       "                                        <div><span style=\"font-size: " +
				       "9pt;" +
				       "\">19,00_às_23,00</span>\n" +
				       "                                        </div>\n" +
				       "                                    </td>\n" +
				       "\n" +
				       "\n" +
				       "                                    <td align=\"center\">\n" +
				       "                                        <div>CO </div>\n" +
				       "                                    </td>\n" +
				       "\n" +
				       "\n" +
				       "                                    <td align=\"center\">\n" +
				       "                                        <div>CB03 </div>\n" +
				       "                                    </td>\n" +
				       "\n" +
				       "\n" +
				       "                                    <td align=\"center\">\n" +
				       "                                        <div><span 99=\"\">3</span>\n" +
				       "                                        </div>\n" +
				       "                                    </td>\n" +
				       "\n" +
				       "\n" +
				       "                                    <td align=\"center\">\n" +
				       "                                        <div>60</div>\n" +
				       "                                    </td>\n" +
				       "\n" +
				       "\n" +
				       "                                    <td align=\"center\">\n" +
				       "                                        <div>T</div>\n" +
				       "                                    </td>\n" +
				       "\n" +
				       "\n" +
				       "                                    <td align=\"center\">\n" +
				       "                                        <div>1</div>\n" +
				       "                                    </td>\n" +
				       "\n" +
				       "                                </tr>\n" +
				       "\n" +
				       "                                <tr style=\"font-family: &#39;Arial&#39;;" +
				       " " +
				       "font-size: 8pt;  color:#000000;\">\n" +
				       "\n" +
				       "                                    <td>\n" +
				       "                                        <div>30829840</div>\n" +
				       "                                    </td>\n" +
				       "\n" +
				       "\n" +
				       "                                    <td>\n" +
				       "                                        <div>LÓGICA</div>\n" +
				       "                                    </td>\n" +
				       "\n" +
				       "\n" +
				       "                                    <td align=\"center\">\n" +
				       "                                        <div>SEX</div>\n" +
				       "                                    </td>\n" +
				       "\n" +
				       "\n" +
				       "                                    <td>\n" +
				       "                                        <div><span style=\"font-size: " +
				       "9pt;" +
				       "\">19,00_às_21,00</span>\n" +
				       "                                        </div>\n" +
				       "                                    </td>\n" +
				       "\n" +
				       "\n" +
				       "                                    <td align=\"center\">\n" +
				       "                                        <div>CC </div>\n" +
				       "                                    </td>\n" +
				       "\n" +
				       "\n" +
				       "                                    <td align=\"center\">\n" +
				       "                                        <div> </div>\n" +
				       "                                    </td>\n" +
				       "\n" +
				       "\n" +
				       "                                    <td align=\"center\">\n" +
				       "                                        <div><span 99=\"\">4</span>\n" +
				       "                                        </div>\n" +
				       "                                    </td>\n" +
				       "\n" +
				       "\n" +
				       "                                    <td align=\"center\">\n" +
				       "                                        <div>60</div>\n" +
				       "                                    </td>\n" +
				       "\n" +
				       "\n" +
				       "                                    <td align=\"center\">\n" +
				       "                                        <div>T</div>\n" +
				       "                                    </td>\n" +
				       "\n" +
				       "\n" +
				       "                                    <td align=\"center\">\n" +
				       "                                        <div>1</div>\n" +
				       "                                    </td>\n" +
				       "\n" +
				       "                                </tr>\n" +
				       "\n" +
				       "                                <tr style=\"font-family: &#39;Arial&#39;;" +
				       " " +
				       "font-size: 8pt;  color:#000000;\">\n" +
				       "\n" +
				       "                                    <td>\n" +
				       "                                        <div>&nbsp;</div>\n" +
				       "                                    </td>\n" +
				       "\n" +
				       "\n" +
				       "                                    <td>\n" +
				       "                                        <div>&nbsp;</div>\n" +
				       "                                    </td>\n" +
				       "\n" +
				       "\n" +
				       "                                    <td align=\"center\">\n" +
				       "                                        <div>SEX</div>\n" +
				       "                                    </td>\n" +
				       "\n" +
				       "\n" +
				       "                                    <td>\n" +
				       "                                        <div><span style=\"font-size: " +
				       "9pt;" +
				       "\">21,00_às_23,00</span>\n" +
				       "                                        </div>\n" +
				       "                                    </td>\n" +
				       "\n" +
				       "\n" +
				       "                                    <td align=\"center\">\n" +
				       "                                        <div>&nbsp;</div>\n" +
				       "                                    </td>\n" +
				       "\n" +
				       "\n" +
				       "                                    <td align=\"center\">\n" +
				       "                                        <div> </div>\n" +
				       "                                    </td>\n" +
				       "\n" +
				       "\n" +
				       "                                    <td align=\"center\">\n" +
				       "                                        <div><span 99=\"\">&nbsp;" +
				       "</span>\n" +
				       "                                        </div>\n" +
				       "                                    </td>\n" +
				       "\n" +
				       "\n" +
				       "                                    <td align=\"center\">\n" +
				       "                                        <div>&nbsp;</div>\n" +
				       "                                    </td>\n" +
				       "\n" +
				       "\n" +
				       "                                    <td align=\"center\">\n" +
				       "                                        <div>T</div>\n" +
				       "                                    </td>\n" +
				       "\n" +
				       "\n" +
				       "                                    <td align=\"center\">\n" +
				       "                                        <div>1</div>\n" +
				       "                                    </td>\n" +
				       "\n" +
				       "                                </tr>\n" +
				       "                                <input type=\"hidden\" name=\"__am" +
				       ".DS_AdptHorarioAluno\" value=\"\">\n" +
				       "\n" +
				       "\n" +
				       "\n" +
				       "                            </tbody>\n" +
				       "                        </table>\n" +
				       "                    </td>\n" +
				       "                </tr>\n" +
				       "            </tbody>\n" +
				       "        </table>\n" +
				       "    </form>\n" +
				       "\n" +
				       "    <table align=\"center\">\n" +
				       "        <tbody>\n" +
				       "            <tr>\n" +
				       "                <td align=\"right\">\n" +
				       "                    <font face=\"Verdana, Arial, Helvetica, sans-serif\"" +
				       " " +
				       "color=\"#003399\" size=\"1\">\n" +
				       "   <b>TOTAL DE CRÉDITOS..........23</b>\n" +
				       "   </font>\n" +
				       "                </td>\n" +
				       "            </tr>\n" +
				       "            <tr>\n" +
				       "                <td align=\"right\">\n" +
				       "                    <font face=\"Verdana, Arial, Helvetica, sans-serif\"" +
				       " " +
				       "color=\"#003399\" size=\"1\">\n" +
				       "   <b>TOTAL DE CARGA HORÁRIA....360 </b>\n" +
				       "   </font>\n" +
				       "                </td>\n" +
				       "            </tr>\n" +
				       "            <tr>\n" +
				       "                <td>\n" +
				       "                    <br>\n" +
				       "                    <font face=\"Courier New, Arial, Helvetica, " +
				       "sans-serif\" color=\"#003399\" size=\"1\">\n" +
				       " <b>PERÍODO OFERTA  (1 = 1º SEMESTRE 1) (2 = 2ºSEMESTRE ) (3 = ANUAL) (4 " +
				       "=" +
				       " MODULAR)  </b>\n" +
				       " </font>\n" +
				       "                </td>\n" +
				       "            </tr>\n" +
				       "            <tr bgcolor=\"#F8F8F8\">\n" +
				       "                <td colspan=\"2\" align=\"left\">\n" +
				       "                    <font face=\"Verdana\" size=\"1\" " +
				       "color=\"#0000CC\">Copyright\n" +
				       "    © 2003 Coordenação de Processamento de Dados- CPD/UFMT\n" +
				       "    </font>\n" +
				       "                </td>\n" +
				       "            </tr>\n" +
				       "        </tbody>\n" +
				       "    </table>\n" +
				       "    <table align=\"center\">\n" +
				       "        <tbody>\n" +
				       "            <tr bgcolor=\"#CCCCCC\">\n" +
				       "                <td>\n" +
				       "                    <font color=\"#FF0000\">\n" +
				       "\n" +
				       "</font>\n" +
				       "                </td>\n" +
				       "            </tr>\n" +
				       "        </tbody>\n" +
				       "    </table>\n" +
				       "\n" +
				       "    <script aria-hidden=\"true\" type=\"application/x-lastpass\" " +
				       "id=\"hiddenlpsubmitdiv\" style=\"display: none;\"></script>\n" +
				       "    <script>\n" +
				       "        try {\n" +
				       "            (function () {\n" +
				       "                for (var lastpass_iter = 0; lastpass_iter < document" +
				       ".forms" +
				       ".length; lastpass_iter++) {\n" +
				       "                    var lastpass_f = document.forms[lastpass_iter];\n" +
				       "                    if (typeof (lastpass_f.lpsubmitorig2) == " +
				       "\"undefined\") {\n" +
				       "                        lastpass_f.lpsubmitorig2 = lastpass_f.submit;\n" +
				       "                        if (typeof (lastpass_f.lpsubmitorig2) == " +
				       "'object')" +
				       " {\n" +
				       "                            continue;\n" +
				       "                        }\n" +
				       "                        lastpass_f.submit = function () {\n" +
				       "                            var form = this;\n" +
				       "                            var customEvent = document.createEvent" +
				       "(\"Event\");\n" +
				       "                            customEvent.initEvent(\"lpCustomEvent\", " +
				       "true," +
				       " true);\n" +
				       "                            var d = document.getElementById" +
				       "(\"hiddenlpsubmitdiv\");\n" +
				       "                            if (d) {\n" +
				       "                                for (var i = 0; i < document.forms" +
				       ".length;" +
				       " i++) {\n" +
				       "                                    if (document.forms[i] == form) {\n" +
				       "                                        if (typeof (d.innerText) != " +
				       "'undefined') {\n" +
				       "                                            d.innerText = i.toString();" +
				       "\n" +
				       "                                        } else {\n" +
				       "                                            d.textContent = i.toString()" +
				       ";" +
				       "\n" +
				       "                                        }\n" +
				       "                                    }\n" +
				       "                                }\n" +
				       "                                d.dispatchEvent(customEvent);\n" +
				       "                            }\n" +
				       "                            form.lpsubmitorig2();\n" +
				       "                        }\n" +
				       "                    }\n" +
				       "                }\n" +
				       "            })()\n" +
				       "        } catch (e) {}\n" +
				       "\n" +
				       "    </script>\n" +
				       "</body>\n" +
				       "\n" +
				       "</html>\n" +
				       "\n";
	}
}
