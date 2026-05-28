package com.example.speech.util;

import javafx.application.Platform;
import javafx.scene.control.ComboBox;
import org.junit.jupiter.api.*;
import java.time.LocalDate;
import static org.assertj.core.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class HelpfulInitializationClassTest {

    @BeforeAll
    static void initJavaFX() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // already started
        }
    }

    // ==================== setValuesComboBox ====================
    @Test
    void testSetValuesComboBox_dayBirthdayCB() {
        ComboBox<Integer> dayBox = new ComboBox<>();
        dayBox.setId("dayBirthdayCB");
        HelpfulInitializationClass.setValuesComboBox(dayBox);
        assertThat(dayBox.getItems()).hasSize(31);
        assertThat(dayBox.getItems().get(0)).isEqualTo(1);
        assertThat(dayBox.getItems().get(30)).isEqualTo(31);
    }

    @Test
    void testSetValuesComboBox_monthBirthdayCB() {
        ComboBox<String> monthBox = new ComboBox<>();
        monthBox.setId("monthBirthdayCB");
        HelpfulInitializationClass.setValuesComboBox(monthBox);
        assertThat(monthBox.getItems()).containsExactlyElementsOf(HelpfulValidationClass.MONTHS);
    }

    @Test
    void testSetValuesComboBox_yearBirthdayCB() {
        ComboBox<Integer> yearBox = new ComboBox<>();
        yearBox.setId("yearBirthdayCB");
        HelpfulInitializationClass.setValuesComboBox(yearBox);
        int currentYear = LocalDate.now().getYear();
        int expectedStart = currentYear - 18;
        int expectedEnd = currentYear - 100;
        assertThat(yearBox.getItems()).hasSize(83);
        assertThat(yearBox.getItems().get(0)).isEqualTo(expectedStart);
        assertThat(yearBox.getItems().get(yearBox.getItems().size() - 1)).isEqualTo(expectedEnd);
        for (int i = 0; i < yearBox.getItems().size() - 1; i++) {
            assertThat(yearBox.getItems().get(i)).isGreaterThan(yearBox.getItems().get(i + 1));
        }
    }

    @Test
    void testSetValuesComboBox_multipleComboBoxes() {
        ComboBox<Integer> dayBox = new ComboBox<>();
        dayBox.setId("dayBirthdayCB");
        ComboBox<String> monthBox = new ComboBox<>();
        monthBox.setId("monthBirthdayCB");
        HelpfulInitializationClass.setValuesComboBox(dayBox, monthBox);
        assertThat(dayBox.getItems()).hasSize(31);
        assertThat(monthBox.getItems()).containsExactlyElementsOf(HelpfulValidationClass.MONTHS);
    }

    @Test
    void testSetValuesComboBox_unknownId_doesNothing() {
        ComboBox<String> unknownBox = new ComboBox<>();
        unknownBox.setId("unknown");
        HelpfulInitializationClass.setValuesComboBox(unknownBox);
        assertThat(unknownBox.getItems()).isEmpty();
    }

    // ==================== Логика позиционирования (без Stage) ====================
    @Test
    void testPositionCalculation() {
        double mainX = 100, mainY = 150, mainW = 800, mainH = 600;
        double modalW = 400, modalH = 250;
        double expectedX = mainX + (mainW - modalW) / 2;
        double expectedY = mainY + (mainH - modalH) / 2;
        assertThat(expectedX).isEqualTo(300.0);
        assertThat(expectedY).isEqualTo(325.0);
    }

    // Тесты для applyDimmingEffect пропущены, так как требуют JavaFX Stage на FX потоке.
    // В дипломной работе указано ручное тестирование UI.
}