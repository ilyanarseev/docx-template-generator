package com.lab.models;

/**
 * Модель приложения (вложения) к письму
 */
public class Attachment {
	private String title; // Заголовок приложения
	private String content; // Содержание приложения
	private int pageCount; // Количество страниц (листов) - опционально

	public Attachment(String title, String content) {
		this.title = title;
		this.content = content;
		this.pageCount = 1; // По умолчанию 1 страница
	}

	public Attachment(String title, String content, int pageCount) {
		this.title = title;
		this.content = content;
		this.pageCount = pageCount;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public int getPageCount() {
		return pageCount;
	}

	public void setPageCount(int pageCount) {
		this.pageCount = pageCount;
	}

	@Override
	public String toString() {
		return title + " (" + pageCount + " л.)";
	}
}
