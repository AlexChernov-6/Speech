package com.example.speech.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnection {
    //Единственный экземпляр пула на всё приложение
    //Пул-это заранее созданные подключения к БД, тоесть вместо того, чтобы каждый раз для каждого запроса и пользователя
    //Создавать новое соединение, мы берём как бы из списка уже существующих подключений и взаимодействуем с БД,
    //Данный подход сильно оптимизирует ресурсозатратность взаимодействия с БД, сводя их к минимуму
    //В пуле, созданные подключения не закрываются а передаются следующему пользователю для работы с БД
    private static HikariDataSource dataSource;

    //Статичный блок инициализации, создаётся один раз, при первом использовании класса, что исключает создание лишних
    //И ненужных подключений и пулов в том числе.
    static {
        Properties properties = new Properties();
        try (FileInputStream fileInputStream = new FileInputStream("config.properties")) {
            properties.load(fileInputStream);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load email configuration", e);
        }

        //Создаётся экземпляр конфигурация для пула
        HikariConfig config = new HikariConfig();
        //Передаём url БД, к которой хотим подключить пул, и необходимо передать параметры для соединения
        config.setJdbcUrl(properties.getProperty("database.URL"));
        config.setUsername(properties.getProperty("database.userName"));
        config.setPassword(properties.getProperty("database.userPassword"));

        //Настройки пула

        //Максимум подключений, если все 20 заняты, новые запросы будут ждать освобождения подключения
        config.setMaximumPoolSize(20);
        //HikariCP будет поддерживать минимум 5 готовых подключений для быстрого доступа
        config.setMinimumIdle(5);
        //Если все подключения заняты запрос будет ждать свободное не более 30 секунд после этого
        //исключение SQLTimeoutException
        config.setConnectionTimeout(30000);
        //Если подключение простаивает больше 10 минут, оно будет закрыто, но пул будет поддерживать минимум 5 подключений,
        //то есть бездействующие подключения закрываются для экономии трафика
        config.setIdleTimeout(600000);

        //Даже если подключение активно, через 30 минут оно будет заменено новым, данный подход предотвращает утечки памяти
        //и "устаревшие" подключения
        config.setMaxLifetime(1800000);
        //Каждая SQL транзакция(запрос) сразу выполняется
        config.setAutoCommit(true);

        //Оптимизация для PostgreSQL
        //Включаем кэширование(храним данные запроса в быстром хранилище) запросов на уровне пула, это важно для оптимизации
        //Не нужно каждый раз компилировать запрос, повторяющийся или аналогичный запрос будет взят из кэша.
        config.addDataSourceProperty("cachePrepStmts", "true");
        //Храним в кэше 250 часто используемых запросов для каждого подключения
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        //Максимальная длина кэша 2048 символов, дальше запрос не кэшируется
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        //Инициализация пула с указанной конфигурацией(создаются стартовые 5 подключений к БД)
        dataSource = new HikariDataSource(config);
    }

    //Метод, возвращающий готовое подключение из пула
    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
}
