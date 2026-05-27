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
	@FXML
	private TextArea previewArea;

	private ObservableList<Attachment> attachments = FXCollections.observableArrayList();

	// Флаги для валидации
	private boolean isRecipientPostValid = false;
	private boolean isRecipientNameValid = false;
	private boolean isOrganizationValid = false;
	private boolean isStudentNameValid = false;

	@FXML
	private void initialize() {
		attachmentsListView.setItems(attachments);

		setupValidationListeners();
		setupPreviewListeners();

		generateButton.setDisable(true);
		updatePreview();
	}

	private void setupValidationListeners() {
		recipientPostField.textProperty().addListener((obs, oldVal, newVal) -> {
			isRecipientPostValid = newVal != null && !newVal.trim().isEmpty();
			updateGenerateButtonState();
			updatePreview();
		});

		recipientNameField.textProperty().addListener((obs, oldVal, newVal) -> {
			isRecipientNameValid = newVal != null && !newVal.trim().isEmpty();
			updateGenerateButtonState();
			updatePreview();
		});

		organizationField.textProperty().addListener((obs, oldVal, newVal) -> {
			isOrganizationValid = newVal != null && !newVal.trim().isEmpty();
			updateGenerateButtonState();
			updatePreview();
		});

		studentNameField.textProperty().addListener((obs, oldVal, newVal) -> {
			isStudentNameValid = newVal != null && !newVal.trim().isEmpty();
			updateGenerateButtonState();
			updatePreview();
		});

		subjectField.textProperty().addListener((obs, oldVal, newVal) -> updatePreview());
		bodyField.textProperty().addListener((obs, oldVal, newVal) -> updatePreview());
	}

	private void setupPreviewListeners() {
		recipientPostField.textProperty().addListener((obs, oldVal, newVal) -> updatePreview());
		recipientNameField.textProperty().addListener((obs, oldVal, newVal) -> updatePreview());
		organizationField.textProperty().addListener((obs, oldVal, newVal) -> updatePreview());
		subjectField.textProperty().addListener((obs, oldVal, newVal) -> updatePreview());
		studentNameField.textProperty().addListener((obs, oldVal, newVal) -> updatePreview());
		bodyField.textProperty().addListener((obs, oldVal, newVal) -> updatePreview());

		attachments.addListener((javafx.collections.ListChangeListener<Attachment>) change -> updatePreview());
	}

	private void updateGenerateButtonState() {
		boolean allRequiredValid = isRecipientPostValid && isRecipientNameValid &&
				isOrganizationValid && isStudentNameValid;
		generateButton.setDisable(!allRequiredValid);

		if (!allRequiredValid) {
			statusLabel.setText("Заполните все обязательные поля");
			statusLabel.setStyle("-fx-text-fill: #e67e22;");
		} else if (subjectField.getText().isEmpty()) {
			statusLabel.setText("Тема письма не заполнена (рекомендуется)");
			statusLabel.setStyle("-fx-text-fill: #e67e22;");
		} else {
			statusLabel.setText("Все обязательные поля заполнены");
			statusLabel.setStyle("-fx-text-fill: green;");
		}
	}

	private void updatePreview() {
		StringBuilder preview = new StringBuilder();

		preview.append("═══════════════════════════════════════════════════════════\n");
		preview.append("                                            ПРЕДПРОСМОТР ПИСЬМА\n");
		preview.append("═══════════════════════════════════════════════════════════\n\n");

		preview.append("Кому: ").append(getTextOrDefault(recipientPostField, "[не указано]"));
		preview.append(" ").append(getTextOrDefault(recipientNameField, "[не указано]")).append("\n");
		preview.append("Организация: ").append(getTextOrDefault(organizationField, "[не указано]")).append("\n\n");

		preview.append("Тема: ").append(getTextOrDefault(subjectField, "[не указана]")).append("\n\n");

		preview.append("Текст письма:\n");
		preview.append("───────────────────────────────────────────────────────────\n");
		preview.append(getTextOrDefault(bodyField, "[текст письма не указан]")).append("\n\n");

		if (!attachments.isEmpty()) {
			preview.append("───────────────────────────────────────────────────────────\n");
			preview.append("Приложение:\n");
			for (int i = 0; i < attachments.size(); i++) {
				Attachment att = attachments.get(i);
				preview.append("  ").append(i + 1).append(". ");
				preview.append(att.getTitle());
				preview.append(" на ").append(att.getPageCount()).append(" л.\n");
			}
			preview.append("\n");
		}

		preview.append("───────────────────────────────────────────────────────────\n");
		preview.append("Студент ").append(getTextOrDefault(studentNameField, "[не указано]")).append("\n");

		preview.append("\n═══════════════════════════════════════════════════════════\n");

		previewArea.setText(preview.toString());
		previewArea.setPrefHeight(350);
	}

	private String getTextOrDefault(TextField field, String defaultValue) {
		return field.getText().isEmpty() ? defaultValue : field.getText();
	}

	private String getTextOrDefault(TextArea area, String defaultValue) {
		return area.getText().isEmpty() ? defaultValue : area.getText();
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
			updatePreview();
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
					attachmentsListView.refresh();
				}
				statusLabel.setText(attachment == null ? "Приложение добавлено" : "Приложение обновлено");
				updatePreview();
			}

		} catch (Exception e) {
			e.printStackTrace();
			showAlert("Ошибка", "Не удалось открыть диалог: " + e.getMessage());
		}
	}

	@FXML
	private void handleGenerateButtonAction() {
		if (recipientPostField.getText().isEmpty() ||
				recipientNameField.getText().isEmpty() ||
				organizationField.getText().isEmpty() ||
				studentNameField.getText().isEmpty()) {

			statusLabel.setText("❌ Ошибка: Заполните все обязательные поля!");
			statusLabel.setStyle("-fx-text-fill: red;");
			return;
		}

		if (subjectField.getText().isEmpty()) {
			Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
			confirmAlert.setTitle("Подтверждение");
			confirmAlert.setHeaderText("Тема письма не заполнена");
			confirmAlert.setContentText("Вы не указали тему письма. Продолжить генерацию?");

			if (confirmAlert.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
				return;
			}
		}

		Map<String, String> placeholders = new HashMap<>();
		placeholders.put("{RECIPIENT_POST}", recipientPostField.getText());
		placeholders.put("{RECIPIENT_NAME}", recipientNameField.getText());
		placeholders.put("{ORGANIZATION}", organizationField.getText());
		placeholders.put("{SUBJECT}", subjectField.getText().isEmpty() ? "Без темы" : subjectField.getText());
		placeholders.put("{STUDENT_NAME}", studentNameField.getText());
		placeholders.put("{LETTER_BODY}",
				bodyField.getText().isEmpty() ? "Текст письма не указан." : bodyField.getText());

		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Сохранить документ");
		fileChooser.getExtensionFilters().add(
				new FileChooser.ExtensionFilter("Word документы", "*.docx"));
		fileChooser.setInitialFileName("Письмо_с_приложениями_" + System.currentTimeMillis() + ".docx");

		File saveFile = fileChooser.showSaveDialog(generateButton.getScene().getWindow());

		if (saveFile != null) {
			try {
				String templatePath = getClass().getResource("/templates/template.docx").getPath();
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
