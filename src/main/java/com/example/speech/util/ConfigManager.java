package com.example.speech.util;

import java.io.*;
import java.util.Properties;

public class ConfigManager {
    private static final String CONFIG_DIR = System.getProperty("user.home") + "/.speechapp/";
    private static final String CONFIG_FILE = CONFIG_DIR + "config.properties";
    private Properties props = new Properties();

    public ConfigManager() {
        File file = new File(CONFIG_FILE);
        file.getParentFile().mkdirs();
        if (file.exists()) {
            try (InputStream in = new FileInputStream(file)) {
                props.load(in);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void save() {
        File file = new File(CONFIG_FILE);
        try (OutputStream out = new FileOutputStream(file)) {
            props.store(out, "Speech App Configuration");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String get(String key, String defaultValue) {
        return props.getProperty(key, defaultValue);
    }

    public void set(String key, String value) {
        props.setProperty(key, value);
    }

    public String getUserEmail() {
        String configEmail = get("user.email", null);
        if(configEmail == null || configEmail.isEmpty())
            return null;
        return configEmail;
    }

    public void setUserEmail(String userEmail) {
        set("user.email", userEmail);
    }

    public String getUserPassword() {
        String configPassword = get("user.password", null);
        if(configPassword == null || configPassword.isEmpty())
            return null;
        return configPassword;
    }

    public void setUserPassword(String userPassword) {
        set("user.password", userPassword);
    }

    public double getWindowWidth() {
        return Double.parseDouble(get("window.width", "800"));
    }

    public void setWindowWidth(double width) {
        set("window.width", String.valueOf(width));
    }

    public double getWindowHeight() {
        return Double.parseDouble(get("window.height", "600"));
    }

    public void setWindowHeight(double height) {
        set("window.height", String.valueOf(height));
    }

    public double getWindowX() {
        return Double.parseDouble(get("window.xCor", "0.0"));
    }

    public void setWindowX(double xCor) {
        set("window.xCor", String.valueOf(xCor));
    }

    public double getWindowY() {
        return Double.parseDouble(get("window.yCor", "0.0"));
    }

    public void setWindowY(double yCor) {
        set("window.yCor", String.valueOf(yCor));
    }

    public boolean getIsFullScreen() {return Boolean.parseBoolean(get("window.isFullScreen", "false"));}

    public void setIsFullScreen(boolean isFullScreen) {set("window.isFullScreen", String.valueOf(isFullScreen));}
}