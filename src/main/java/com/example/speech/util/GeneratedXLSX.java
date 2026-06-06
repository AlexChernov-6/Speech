package com.example.speech.util;

import com.example.speech.model.Channel;
import com.example.speech.model.Message;
import com.example.speech.model.User;
import org.apache.poi.xddf.usermodel.chart.*;
import org.apache.poi.xssf.usermodel.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class GeneratedXLSX {

    // === Экспорт каналов ===
    public static void exportChannelsToExcel(List<Channel> channels, Path targetPath) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet("Каналы");
            // Заголовки
            String[] headers = {"ID", "Участников", "Тип канала", "Уникальное имя", "Владелец"};
            XSSFRow headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }
            // Данные
            for (int i = 0; i < channels.size(); i++) {
                Channel ch = channels.get(i);
                XSSFRow row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(ch.getChannelID());
                row.createCell(1).setCellValue(ch.getChannelCountUser());
                row.createCell(2).setCellValue(ch.getChannelType().getChannelTypeName());
                row.createCell(3).setCellValue(ch.getChannel_name_unique());
                row.createCell(4).setCellValue(ch.getOwnerUser() != null ? ch.getOwnerUser().getNameUser() : "");
            }
            // Автоширина
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Сохранение
            try (FileOutputStream fos = new FileOutputStream(targetPath.toFile())) {
                workbook.write(fos);
            }
        }
    }

    // === Экспорт пользователей ===
    public static void exportUsersToExcel(List<User> users, Path targetPath) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet("Пользователи");
            String[] headers = {"ID", "E-mail", "Имя", "Дата рождения", "Статус"};
            XSSFRow headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }
            for (int i = 0; i < users.size(); i++) {
                User u = users.get(i);
                XSSFRow row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(u.getIdUser());
                row.createCell(1).setCellValue(u.getEmailUser());
                row.createCell(2).setCellValue(u.getNameUser());
                row.createCell(3).setCellValue(u.getBirthdayUser());
                row.createCell(4).setCellValue(u.getStatusUser());
            }
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            // Можно добавить круговую диаграмму по статусам – по желанию
            try (FileOutputStream fos = new FileOutputStream(targetPath.toFile())) {
                workbook.write(fos);
            }
        }
    }

    // === Экспорт сообщений ===
    public static void exportMessagesToExcel(List<Message> messages, Path targetPath) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet("Сообщения");
            String[] headers = {"ID", "Дата/время", "Отправитель", "Канал", "Изменено", "Закреплено", "Файлов"};
            XSSFRow headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
            for (int i = 0; i < messages.size(); i++) {
                Message m = messages.get(i);
                XSSFRow row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(m.getMessageId());
                row.createCell(1).setCellValue(m.getMessageDatetime().format(dtf));
                row.createCell(2).setCellValue(m.getChannelUser().getUser().getNameUser());
                row.createCell(3).setCellValue(m.getChannelUser().getChannel().getChannel_name_unique());
                row.createCell(4).setCellValue(m.isModifiedMessage() ? "Да" : "Нет");
                row.createCell(5).setCellValue(m.getPinMessage() ? "Да" : "Нет");
                row.createCell(6).setCellValue(m.getMessageContent().size());
            }
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            try (FileOutputStream fos = new FileOutputStream(targetPath.toFile())) {
                workbook.write(fos);
            }
        }
    }
}