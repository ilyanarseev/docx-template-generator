package com.lab.controllers;

import com.lab.services.DocxGenerator;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class MainController {

	@FXML
	private TextField recipientPostField;
	@FXML
	private TextField recipientNameField;
	@FXML
	private TextField organizationField;
	@FXML
	private TextField subjectField;
	@FXML
	private TextField studentNameField;
	@FXML
	private TextArea bodyField;
	@FXML
	private Label statusLabel;
	@FXML
	private Button generateButton;

	@FXML
	private void handleGenerateButtonAction() {
		// Проверка заполнения обязательных полей
		if (recipientPostField.getText().isEmpty() ||
				recipientNameField.getText().isEmpty() ||
				organizationField.getText().isEmpty() ||
				studentNameField.getText().isEmpty()) {

			statusLabel.setText("Ошибка: Заполните все обязательные поля!");
			statusLabel.setStyle("-fx-text-fill: red;");
			return;
		}

		// Формирование карты плейсхолдеров
		Map<String, String> placeholders = new HashMap<>();
		placeholders.put("{RECIPIENT_POST}", recipientPostField.getText());
		placeholders.put("{RECIPIENT_NAME}", recipientNameField.getText());
		placeholders.put("{ORGANIZATION}", organizationField.getText());
		placeholders.put("{SUBJECT}", subjectField.getText().isEmpty() ? "Без темы" : subjectField.getText());
		placeholders.put("{STUDENT_NAME}", studentNameField.getText());

		// Тело письма с переносами строк
		String bodyText = bodyField.getText().isEmpty() ? "Текст письма не указан." : bodyField.getText();
		placeholders.put("{LETTER_BODY}", bodyText);

		// Выбор места сохранения
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Сохранить документ");
		fileChooser.getExtensionFilters().add(
				new FileChooser.ExtensionFilter("Word документы", "*.docx"));
		fileChooser.setInitialFileName("Письмо_" + System.currentTimeMillis() + ".docx");

		File saveFile = fileChooser.showSaveDialog(generateButton.getScene().getWindow());

		if (saveFile != null) {
			try {
				// Путь к шаблону (ложить template.docx в resources/templates)
				String templatePath = getClass().getResource("/templates/template.docx").getPath();

				DocxGenerator.generate(templatePath, saveFile.getAbsolutePath(), placeholders);

				statusLabel.setText("Успешно! Документ сохранён: " + saveFile.getName());
				statusLabel.setStyle("-fx-text-fill: green;");
			} catch (Exception e) {
				statusLabel.setText("Ошибка: " + e.getMessage());
				statusLabel.setStyle("-fx-text-fill: red;");
				e.printStackTrace();
			}
		} else {
			statusLabel.setText("Сохранение отменено.");
		}
	}
}
