package com.lab.services;

import com.lab.models.Attachment;
import org.apache.poi.xwpf.usermodel.*;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Map;

public class DocxGenerator {
	private static final String ATTACHMENTS_PLACEHOLDER = "{ATTACHMENTS_LIST}";

	/**
	 * Генерация документа из шаблона с заменой плейсхолдеров
	 * 
	 * @param templatePath путь к шаблону .docx
	 * @param outputPath   путь для сохранения результата
	 * @param placeholders карта {плейсхолдер -> значение}
	 */
	public static void generate(String templatePath, String outputPath,
			Map<String, String> placeholders,
			List<Attachment> attachments) throws Exception {

		try (FileInputStream fis = new FileInputStream(templatePath);
				XWPFDocument document = new XWPFDocument(fis)) {

			// 1. Замена в основном тексте документа
			replaceInParagraphs(document.getParagraphs(), placeholders);

			// 2. Замена в таблицах
			replaceInTables(document, placeholders);

			// 3. Замена в колонтитулах
			replaceInHeadersAndFooters(document, placeholders);

			// 4. Обработка приложений
			if (attachments != null && !attachments.isEmpty()) {
				processAttachments(document, attachments);
			}

			// 5. Сохранение
			try (FileOutputStream fos = new FileOutputStream(outputPath)) {
				document.write(fos);
			}
		}
	}

	/**
	 * Обработка приложений в документе
	 */
	private static void processAttachments(XWPFDocument document,
			List<Attachment> attachments) {
		// Заменить плейсхолдер на текстовый список приложений
		replaceAttachmentsPlaceholder(document, attachments);

		// Добавить полные тексты приложений в конец документа
		AttachmentGenerator.addAttachments(document, attachments);
	}

	/**
	 * Замена плейсхолдера {ATTACHMENTS_LIST} на текстовый список приложений
	 */
	private static void replaceAttachmentsPlaceholder(XWPFDocument document,
			List<Attachment> attachments) {

		// Перебрать все параграфы
		for (int i = 0; i < document.getParagraphs().size(); i++) {
			XWPFParagraph paragraph = document.getParagraphs().get(i);
			StringBuilder fullText = new StringBuilder();

			// Склеить текст параграфа
			for (XWPFRun run : paragraph.getRuns()) {
				String runText = run.getText(0);
				if (runText != null) {
					fullText.append(runText);
				}
			}

			String paragraphText = fullText.toString();

			// Если плейсхолдер найден
			if (paragraphText.contains(ATTACHMENTS_PLACEHOLDER)) {
				// Текст ДО плейсхолдера (если есть)
				String beforePlaceholder = paragraphText.substring(0, paragraphText.indexOf(ATTACHMENTS_PLACEHOLDER));
				// Текст ПОСЛЕ плейсхолдера (если есть)
				String afterPlaceholder = paragraphText
						.substring(paragraphText.indexOf(ATTACHMENTS_PLACEHOLDER) + ATTACHMENTS_PLACEHOLDER.length());

				// Очистить параграф
				while (paragraph.getRuns().size() > 0) {
					paragraph.removeRun(0);
				}

				// Вставить текст до плейсхолдера (если есть)
				if (!beforePlaceholder.isEmpty()) {
					paragraph.createRun().setText(beforePlaceholder);
				}

				// Вставить список приложений (каждый элемент на новой строке)
				if (attachments != null && !attachments.isEmpty()) {
					// Заголовок "Приложение:"
					XWPFRun titleRun = paragraph.createRun();
					titleRun.setText("\nПриложение:");
					titleRun.addBreak(); // Добавить перенос строки

					// Каждое приложение на отдельной строке
					for (int j = 0; j < attachments.size(); j++) {
						Attachment att = attachments.get(j);
						XWPFRun itemRun = paragraph.createRun();
						String itemText = (j + 1) + ". " + att.getTitle() + " на " + att.getPageCount() + " л.";
						itemRun.setText(itemText);
						itemRun.addBreak(); // Перенос строки после каждого элемента
					}
				}

				// Если есть текст после плейсхолдера, создать новый параграф для него
				if (!afterPlaceholder.isEmpty()) {
					XWPFParagraph afterParagraph = document.createParagraph();
					afterParagraph.createRun().setText(afterPlaceholder);
				}

				break;
			}
		}
	}

	private static void replaceInParagraphs(List<XWPFParagraph> paragraphs,
			Map<String, String> placeholders) {
		for (XWPFParagraph paragraph : paragraphs) {
			replaceInParagraph(paragraph, placeholders);
		}
	}

	/**
	 * Замена в одном параграфе с учётом склейки Run'ов
	 * (Word может разбить {PLACEHOLDER} на несколько кусков)
	 */
	private static void replaceInParagraph(XWPFParagraph paragraph,
			Map<String, String> placeholders) {
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
	private static void replaceInHeadersAndFooters(XWPFDocument document,
			Map<String, String> placeholders) {
		// Верхние колонтитулы
		for (XWPFHeader header : document.getHeaderList()) {
			replaceInParagraphs(header.getParagraphs(), placeholders);
		}
		// Нижние колонтитулы
		for (XWPFFooter footer : document.getFooterList()) {
			replaceInParagraphs(footer.getParagraphs(), placeholders);
		}
	}
}
