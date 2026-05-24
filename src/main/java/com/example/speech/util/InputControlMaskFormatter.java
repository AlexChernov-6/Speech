package com.example.speech.util;

import javafx.scene.control.*;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class InputControlMaskFormatter {

    private static final char PLACEHOLDER = '_';
    public static final String MASK_DATE = "__.__.____";
    private static final String MASK_PASSPORT = "____ ______";
    private static final String MASK_PHONE = "+7(___)___-__-__";
    private MaskContext context;
    private IndexRange oldRange;
    private Label hintLB;
    private String nodePromptText;

    public enum MaskContext {
        DATE_MASK,
        PASSPORT_MASK,
        PHONE_MASK
    }

    public void apply(TextInputControl textField, MaskContext context) {
        this.context = context;
        this.hintLB = ((Label) textField.getUserData());
        this.nodePromptText = textField.getPromptText();

        textField.setTextFormatter(new TextFormatter<>(change -> {
            String newTextControl = change.getControlNewText();
            String newText = change.getText();
            String oldText = change.getControlText();

            if(newTextControl.isEmpty() && !change.getControl().isFocused() && !oldText.isEmpty())
                return change;

            if (!change.getControl().isFocused())
                return change;


            if ((oldText == null || oldText.isEmpty()) && change.getControl().isFocused()) {
                if(context == MaskContext.DATE_MASK) {
                    change.setText(MASK_DATE);
                    change.setCaretPosition(0);
                    change.setAnchor(0);
                    System.out.println("1");
                }
                else if (context == MaskContext.PASSPORT_MASK) {
                    change.setText(MASK_PASSPORT);
                    change.setCaretPosition(0);
                    change.setAnchor(0);
                }
                else if (context == MaskContext.PHONE_MASK) {
                    change.setText(MASK_PHONE);
                    change.setCaretPosition(3);
                    change.setAnchor(3);
                }
                hintLB.setText("Поле обязательно для заполнения");
                return change;
            }

            if (change.isAdded() || change.isReplaced()) {
                if (oldRange != null && oldRange.getLength() >= 1) {
                    StringBuilder stringBuilder = new StringBuilder(oldText);
                    for (int i = oldRange.getStart(); i < oldRange.getEnd(); i++) {
                        char c = stringBuilder.charAt(i);
                        if (c == '.' || c == ' ' || c == '-' || c == '(' || c == ')' || c == '+' || (i == 1 && context == MaskContext.PHONE_MASK))
                            continue;
                        stringBuilder.setCharAt(i, '_');
                    }
                    change.setRange(0, oldText.length());
                    oldRange = change.getSelection();

                    return handleDigitInsert(change, stringBuilder.toString());
                } else
                    return handleDigitInsert(change, oldText);
            }

            if (newText.isEmpty() && change.getControlNewText().length() < oldText.length()) {
                change.getControl().setStyle("");
                if(context == MaskContext.DATE_MASK) hintLB.setText("Укажите дату полностью");
                else hintLB.setText("");

                if (oldRange != null && oldRange.getLength() >= 1) {
                    StringBuilder stringBuilder = new StringBuilder(oldText);
                    for (int i = oldRange.getStart(); i < oldRange.getEnd(); i++) {
                        char c = stringBuilder.charAt(i);
                        if (c == '.' || c == ' ' || c == '-' || c == '(' || c == ')' || c == '+' || (context == MaskContext.PHONE_MASK && i == 1))
                            continue;
                        stringBuilder.setCharAt(i, '_');
                    }
                    change.setText(stringBuilder.toString());
                    change.setRange(0, oldText.length());
                    oldRange = change.getSelection();
                    return change;
                } else {
                    oldRange = change.getSelection();
                    return handleDeletion(change, oldText);
                }
            }

            if (newText.isEmpty() && change.getControlNewText().equals(oldText)) {
                oldRange = change.getSelection();
                return change;
            }

            if (change.getSelection().getLength() >= 1) {
                oldRange = change.getSelection();
                return change;
            }

            return null;
        }));

        // Обработка фокуса
        textField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                if (textField.getText().equals(MASK_DATE) || textField.getText().equals(MASK_PASSPORT)
                        || textField.getText().equals(MASK_PHONE)) {
                    textField.setText("");
                }
            }
        });
    }

    private TextFormatter.Change handleDigitInsert(TextFormatter.Change change, String oldText) {
        String insStr = change.getText();

        StringBuilder digit = new StringBuilder();
        for (int i = 0; i < insStr.length(); i++) {
            if (Character.isDigit(insStr.charAt(i)))
                digit.append(insStr.charAt(i));
        }
        String resultCasting = digit.toString();
        if (resultCasting.isEmpty()) {
            hintLB.setText("Допускаются только цифры");
            return null;
        }

        int countPlaceholder = 0;
        for (int i = 0; i < oldText.length(); i++) {
            if (oldText.charAt(i) == PLACEHOLDER)
                countPlaceholder++;
        }

        if (countPlaceholder == 0) {
            return null;
        }

        StringBuilder sb = new StringBuilder(oldText);
        int targetPos = -1;
        for (int j = 0; j < Math.min(countPlaceholder, resultCasting.length()); j++) {
            for (int i = 0; i < oldText.length(); i++) {
                if (sb.charAt(i) == PLACEHOLDER) {
                    targetPos = i;
                    break;
                }
            }
            if (targetPos == -1) return null;

            sb.setCharAt(targetPos, resultCasting.charAt(j));
        }
        String newText = sb.toString();

        change.setText(newText);
        change.setRange(0, oldText.length());

        if(context == MaskContext.DATE_MASK && !change.getControlNewText().contains("_")) {
            try {
                boolean clearHint = true;
                LocalDate insertDate = LocalDate.parse(change.getControlNewText(), DateTimeFormatter.ofPattern("dd.MM.yyyy"));
                if(change.getControl() instanceof TextField) {
                    if(nodePromptText.equals("ДАТА РОЖДЕНИЯ")) {
                        if(insertDate.isAfter(LocalDate.now())) {
                            hintLB.setText("Некорректная дата: кажется гость ещё не родился...");
                            clearHint = false;
                        }
                        else if(insertDate.isBefore(LocalDate.now().minusYears(123))) {
                            hintLB.setText("Некорректная дата: люди столько не живут...");
                            clearHint = false;
                        }
                    } else {
                        if(insertDate.isBefore(LocalDate.now())) {
                            hintLB.setText("Некорректная дата: кажется ваш документ просрочен");
                            clearHint = false;
                        }
                    }
                }
                if(clearHint)
                    hintLB.setText(nodePromptText);
            } catch (DateTimeException e) {
                hintLB.setText("Некорректная дата");
            }
        } else hintLB.setText("Укажите дату полностью");

        int newCaretPos = targetPos + 1;
        change.setCaretPosition(newCaretPos);
        change.setAnchor(newCaretPos);
        return change;
    }

    private TextFormatter.Change handleDeletion(TextFormatter.Change change, String oldText) {
        // Находим последнюю введённую цифру
        int lastDigitPos = -1;
        for (int i = oldText.length() - 1; i >= 0; i--) {
            char c = oldText.charAt(i);
            if(context == MaskContext.PHONE_MASK) {
                if (c != PLACEHOLDER && c != '.' && c != ' ' && c != '-' && c != '(' && c != ')' && c != '+' && i != 1) {
                    lastDigitPos = i;
                    break;
                }
            } else {
                if (c != PLACEHOLDER && c != '.' && c != ' ' && c != '-' && c != '(' && c != ')' && c != '+') {
                    lastDigitPos = i;
                    break;
                }
            }
        }
        if (lastDigitPos == -1) return null;

        StringBuilder sb = new StringBuilder(oldText);
        sb.setCharAt(lastDigitPos, PLACEHOLDER);
        String newText = sb.toString();

        change.setText(newText);
        change.setRange(0, oldText.length());
        change.setCaretPosition(lastDigitPos);
        change.setAnchor(lastDigitPos);
        return change;
    }
}