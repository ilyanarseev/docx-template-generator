package com.lab.controllers;

import com.lab.models.Attachment;
import com.lab.services.DocxGenerator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

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
	private ListView<Attachment> attachmentsListView;

	private ObservableList<Attachment> attachments = FXCollections.observableArrayList();

	@FXML
	private void initialize() {
		// Просто установить список - toString() сам отобразит данные
		attachmentsListView.setItems(attachments);
	}

	@FXML
	private void handleAddAttachmentAction() {
		showAttachmentDialog(null);
	}

	@FXML
	private void handleEditAttachmentAction() {
		Attachment selected = attachmentsListView.getSelectionModel().getSelectedItem();
		if (selected != null) {
			showAttachmentDialog(selected);
		} else {
			showAlert("Ошибка", "Выберите приложение для редактирования");
		}
	}

	@FXML
	private void handleRemoveAttachmentAction() {
		Attachment selected = attachmentsListView.getSelectionModel().getSelectedItem();
		if (selected != null) {
			attachments.remove(selected);
			statusLabel.setText("Приложение удалено");
		} else {
			showAlert("Ошибка", "Выберите приложение для удаления");
		}
	}

	private void showAttachmentDialog(Attachment attachment) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/attachment-dialog.fxml"));
			VBox page = loader.load();

			Stage dialogStage = new Stage();
			dialogStage.setTitle(attachment == null ? "Новое приложение" : "Редактирование приложения");
			dialogStage.initModality(Modality.WINDOW_MODAL);
			dialogStage.initOwner(generateButton.getScene().getWindow());
			Scene scene = new Scene(page, 500, 450);
			dialogStage.setScene(scene);

			AttachmentDialogController controller = loader.getController();
			controller.setDialogStage(dialogStage);
			controller.setAttachment(attachment);

			dialogStage.showAndWait();

			if (controller.isSaved()) {
				if (attachment == null) {
					attachments.add(controller.getAttachment());
				} else {
					// Обновить отображение
					attachmentsListView.refresh();
				}
				statusLabel.setText(attachment == null ? "Приложение добавлено" : "Приложение обновлено");
			}

		} catch (Exception e) {
			e.printStackTrace();
			showAlert("Ошибка", "Не удалось открыть диалог: " + e.getMessage());
		}
	}

	@FXML
	private void handleGenerateButtonAction() {
		// Валидация
		if (recipientPostField.getText().isEmpty() ||
				recipientNameField.getText().isEmpty() ||
				organizationField.getText().isEmpty() ||
				studentNameField.getText().isEmpty()) {

			statusLabel.setText("Ошибка: Заполните все обязательные поля!");
			statusLabel.setStyle("-fx-text-fill: red;");
			return;
		}

		// Формирование плейсхолдеров
		Map<String, String> placeholders = new HashMap<>();
		placeholders.put("{RECIPIENT_POST}", recipientPostField.getText());
		placeholders.put("{RECIPIENT_NAME}", recipientNameField.getText());
		placeholders.put("{ORGANIZATION}", organizationField.getText());
		placeholders.put("{SUBJECT}", subjectField.getText().isEmpty() ? "Без темы" : subjectField.getText());
		placeholders.put("{STUDENT_NAME}", studentNameField.getText());
		placeholders.put("{LETTER_BODY}",
				bodyField.getText().isEmpty() ? "Текст письма не указан." : bodyField.getText());

		// Сохранение файла
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Сохранить документ");
		fileChooser.getExtensionFilters().add(
				new FileChooser.ExtensionFilter("Word документы", "*.docx"));
		fileChooser.setInitialFileName("Письмо_с_приложениями_" + System.currentTimeMillis() + ".docx");

		File saveFile = fileChooser.showSaveDialog(generateButton.getScene().getWindow());

		if (saveFile != null) {
			try {
				String templatePath = getClass().getResource("/templates/template.docx").getPath();

				// Генерация документа
				DocxGenerator.generate(templatePath, saveFile.getAbsolutePath(), placeholders, attachments);

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

	private void showAlert(String title, String message) {
		Alert alert = new Alert(Alert.AlertType.WARNING);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
	}
}
