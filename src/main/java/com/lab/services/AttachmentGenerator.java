package com.lab.services;

import com.lab.models.Attachment;
import org.apache.poi.xwpf.usermodel.*;

import java.util.List;

/**
 * Класс для генерации приложений в документе Word
 */
public class AttachmentGenerator {

	/**
	 * Добавление всех приложений в конец документа
	 */
	public static void addAttachments(XWPFDocument document, List<Attachment> attachments) {
		if (attachments == null || attachments.isEmpty()) {
			return;
		}

		// Добавить разделитель и заголовок
		addSeparator(document);
		addSectionTitle(document, "Приложения");

		// Добавить каждое приложение
		for (int i = 0; i < attachments.size(); i++) {
			Attachment attachment = attachments.get(i);
			int number = i + 1;

			addAttachment(document, attachment, number, attachments.size());
		}
	}

	/**
	 * Добавление разделительной линии
	 */
	private static void addSeparator(XWPFDocument document) {
		XWPFParagraph separator = document.createParagraph();
		separator.setAlignment(ParagraphAlignment.CENTER);
		XWPFRun run = separator.createRun();
		run.setText("________________________________________");
		run.setFontSize(10);

		// Добавить пустую строку
		XWPFParagraph empty = document.createParagraph();
		empty.createRun().setText("");
	}

	/**
	 * Добавление заголовка раздела
	 */
	private static void addSectionTitle(XWPFDocument document, String title) {
		XWPFParagraph titleParagraph = document.createParagraph();
		titleParagraph.setAlignment(ParagraphAlignment.CENTER);
		XWPFRun titleRun = titleParagraph.createRun();
		titleRun.setText(title);
		titleRun.setBold(true);
		titleRun.setFontSize(16);

		// Пустая строка после заголовка
		XWPFParagraph empty = document.createParagraph();
		empty.createRun().setText("");
	}

	/**
	 * Добавление одного приложения
	 */
	private static void addAttachment(XWPFDocument document, Attachment attachment,
			int number, int totalCount) {
		// 1. Слово "Приложение X" справа
		XWPFParagraph headerParagraph = document.createParagraph();
		headerParagraph.setAlignment(ParagraphAlignment.RIGHT);
		XWPFRun headerRun = headerParagraph.createRun();

		if (totalCount == 1) {
			headerRun.setText("Приложение");
		} else {
			headerRun.setText("Приложение " + number);
		}
		headerRun.setBold(true);
		headerRun.setFontSize(12);

		// 2. Заголовок приложения по центру
		XWPFParagraph titleParagraph = document.createParagraph();
		titleParagraph.setAlignment(ParagraphAlignment.CENTER);
		XWPFRun titleRun = titleParagraph.createRun();
		titleRun.setText(attachment.getTitle());
		titleRun.setBold(true);
		titleRun.setFontSize(14);
		titleRun.addBreak();

		// 3. Содержание приложения
		XWPFParagraph contentParagraph = document.createParagraph();
		contentParagraph.setAlignment(ParagraphAlignment.LEFT);
		XWPFRun contentRun = contentParagraph.createRun();
		contentRun.setText(attachment.getContent());
		contentRun.setFontSize(12);

		// Пустая строка между приложениями
		XWPFParagraph empty = document.createParagraph();
		empty.createRun().setText("");
	}

	/**
	 * Создание текстового списка приложений для вставки в тело письма
	 */
	public static String createAttachmentsListText(List<Attachment> attachments) {
		if (attachments == null || attachments.isEmpty()) {
			return "";
		}

		StringBuilder sb = new StringBuilder();
		sb.append("\n\nПриложение:\n");

		for (int i = 0; i < attachments.size(); i++) {
			Attachment att = attachments.get(i);
			sb.append(i + 1).append(". ");
			sb.append(att.getTitle());
			sb.append(" на ").append(att.getPageCount()).append(" л.\n");
		}

		return sb.toString();
	}
}
