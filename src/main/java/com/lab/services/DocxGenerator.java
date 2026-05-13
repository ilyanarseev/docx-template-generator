package com.lab.services;

import org.apache.poi.xwpf.usermodel.*;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Map;

public class DocxGenerator {

	/**
	 * Генерация документа из шаблона с заменой плейсхолдеров
	 * 
	 * @param templatePath путь к шаблону .docx
	 * @param outputPath   путь для сохранения результата
	 * @param placeholders карта {плейсхолдер -> значение}
	 */
	public static void generate(String templatePath, String outputPath, Map<String, String> placeholders)
			throws Exception {
		try (FileInputStream fis = new FileInputStream(templatePath);
				XWPFDocument document = new XWPFDocument(fis)) {

			// 1. Замена в основном тексте документа
			replaceInParagraphs(document.getParagraphs(), placeholders);

			// 2. Замена в таблицах (если есть)
			replaceInTables(document, placeholders);

			// 3. Замена в колонтитулах
			replaceInHeadersAndFooters(document, placeholders);

			// Сохранение
			try (FileOutputStream fos = new FileOutputStream(outputPath)) {
				document.write(fos);
			}
		}
	}

	/**
	 * Замена плейсхолдеров в списке параграфов
	 */
	private static void replaceInParagraphs(List<XWPFParagraph> paragraphs, Map<String, String> placeholders) {
		for (XWPFParagraph paragraph : paragraphs) {
			replaceInParagraph(paragraph, placeholders);
		}
	}

	/**
	 * Замена в одном параграфе с учётом склейки Run'ов
	 * (Word может разбить {PLACEHOLDER} на несколько кусков)
	 */
	private static void replaceInParagraph(XWPFParagraph paragraph, Map<String, String> placeholders) {
		// Склеивание всего текста параграфа
		StringBuilder fullTextBuilder = new StringBuilder();
		for (XWPFRun run : paragraph.getRuns()) {
			String runText = run.getText(0);
			if (runText != null) {
				fullTextBuilder.append(runText);
			}
		}

		String fullText = fullTextBuilder.toString();
		boolean modified = false;

		// Проверка наличия плейсхолдеров
		for (Map.Entry<String, String> entry : placeholders.entrySet()) {
			if (fullText.contains(entry.getKey())) {
				fullText = fullText.replace(entry.getKey(), entry.getValue());
				modified = true;
			}
		}

		// Если были замены — перезаписать параграф
		if (modified) {
			// Удаление всех существующих Run
			while (paragraph.getRuns().size() > 0) {
				paragraph.removeRun(0);
			}
			// Создание нового Run с итоговым текстом
			XWPFRun newRun = paragraph.createRun();
			newRun.setText(fullText);
		}
	}

	/**
	 * Замена в таблицах документа
	 */
	private static void replaceInTables(XWPFDocument document, Map<String, String> placeholders) {
		for (XWPFTable table : document.getTables()) {
			for (XWPFTableRow row : table.getRows()) {
				for (XWPFTableCell cell : row.getTableCells()) {
					replaceInParagraphs(cell.getParagraphs(), placeholders);
				}
			}
		}
	}

	/**
	 * Замена в колонтитулах
	 */
	private static void replaceInHeadersAndFooters(XWPFDocument document, Map<String, String> placeholders) {
		// Верхние колонтитулы
		for (XWPFHeader header : document.getHeaderList()) {
			replaceInParagraphs(header.getParagraphs(), placeholders);
			replaceInTablesInHeaderFooter(header);
		}

		// Нижние колонтитулы
		for (XWPFFooter footer : document.getFooterList()) {
			replaceInParagraphs(footer.getParagraphs(), placeholders);
			replaceInTablesInHeaderFooter(footer);
		}
	}

	/**
	 * Замена в таблицах внутри колонтитулов
	 */
	private static void replaceInTablesInHeaderFooter(XWPFHeader header) {
		for (XWPFTable table : header.getTables()) {
			for (XWPFTableRow row : table.getRows()) {
				for (XWPFTableCell cell : row.getTableCells()) {
					replaceInParagraphs(cell.getParagraphs(), Map.of());
				}
			}
		}
	}

	private static void replaceInTablesInHeaderFooter(XWPFFooter footer) {
		for (XWPFTable table : footer.getTables()) {
			for (XWPFTableRow row : table.getRows()) {
				for (XWPFTableCell cell : row.getTableCells()) {
					replaceInParagraphs(cell.getParagraphs(), Map.of());
				}
			}
		}
	}
}
