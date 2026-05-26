package com.lab.controllers;

import com.lab.models.Attachment;
import javafx.fxml.FXML;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class AttachmentDialogController {

	@FXML
	private TextField titleField;
	@FXML
	private TextArea contentField;
	@FXML
	private Spinner<Integer> pageCountSpinner;

	private Stage dialogStage;
	private Attachment attachment;
	private boolean saved = false;

	public void setDialogStage(Stage dialogStage) {
		this.dialogStage = dialogStage;
	}

	public void setAttachment(Attachment attachment) {
		this.attachment = attachment;

		if (attachment != null) {
			titleField.setText(attachment.getTitle());
			contentField.setText(attachment.getContent());
			pageCountSpinner.getValueFactory().setValue(attachment.getPageCount());
		}
	}

	public boolean isSaved() {
		return saved;
	}

	public Attachment getAttachment() {
		return attachment;
	}

	@FXML
	private void handleSaveAction() {
		if (titleField.getText().isEmpty()) {
			return;
		}

		if (attachment == null) {
			attachment = new Attachment(titleField.getText(), contentField.getText(),
					pageCountSpinner.getValue());
		} else {
			attachment.setTitle(titleField.getText());
			attachment.setContent(contentField.getText());
			attachment.setPageCount(pageCountSpinner.getValue());
		}

		saved = true;
		dialogStage.close();
	}

	@FXML
	private void handleCancelAction() {
		dialogStage.close();
	}
}
