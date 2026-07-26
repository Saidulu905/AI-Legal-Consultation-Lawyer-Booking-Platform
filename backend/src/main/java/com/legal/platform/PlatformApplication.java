package com.legal.platform;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PlatformApplication {

    public static void main(String[] args) {
        // Load environment variables from .env (in current directory or parent directory)
        loadEnv(".");
        loadEnv("..");

        mapLegacyDatabaseEnv();
        configureDatabaseUrl();

        SpringApplication.run(PlatformApplication.class, args);
    }

    private static void loadEnv(String directory) {
        try {
            Dotenv dotenv = Dotenv.configure()
                    .directory(directory)
                    .ignoreIfMissing()
                    .load();
            dotenv.entries().forEach(entry -> {
                if (System.getProperty(entry.getKey()) == null && System.getenv(entry.getKey()) == null) {
                    System.setProperty(entry.getKey(), entry.getValue());
                }
            });
        } catch (Exception e) {
            // Fail silently if .env is missing or invalid
        }
    }

    private static void mapLegacyDatabaseEnv() {
        if (getEnv("SPRING_DATASOURCE_URL") == null) {
            setIfMissing("SPRING_DATASOURCE_URL", buildJdbcUrl(
                    getEnv("DB_HOST", "localhost"),
                    getEnv("DB_PORT", "5432"),
                    getEnv("DB_NAME", "legal_platform")
            ));
        }
        setIfMissing("SPRING_DATASOURCE_USERNAME", getEnv("DB_USERNAME", "postgres"));
        setIfMissing("SPRING_DATASOURCE_PASSWORD", getEnv("DB_PASSWORD", "postgres"));
    }

    private static void configureDatabaseUrl() {
        String databaseUrl = getEnv("DATABASE_URL");
        
        if (databaseUrl != null && (databaseUrl.startsWith("postgres://") || databaseUrl.startsWith("postgresql://"))) {
            try {
                String cleanedUrl = databaseUrl.replaceFirst("postgres(ql)?://", "");
                int atSignIndex = cleanedUrl.indexOf('@');
                if (atSignIndex != -1) {
                    String userInfo = cleanedUrl.substring(0, atSignIndex);
                    String hostInfo = cleanedUrl.substring(atSignIndex + 1);
                    
                    String[] userParts = userInfo.split(":");
                    String username = userParts[0];
                    String password = userParts.length > 1 ? userParts[1] : "";
                    
                    String dbUrl = "jdbc:postgresql://" + hostInfo;
                    if (hostInfo.contains("render.com") && !hostInfo.contains("sslmode")) {
                        if (hostInfo.contains("?")) {
                            dbUrl += "&sslmode=require";
                        } else {
                            dbUrl += "?sslmode=require";
                        }
                    }
                    
                    setIfMissing("SPRING_DATASOURCE_URL", dbUrl);
                    setIfMissing("SPRING_DATASOURCE_USERNAME", username);
                    setIfMissing("SPRING_DATASOURCE_PASSWORD", password);
                }
            } catch (Exception e) {
                // Fail silently
            }
        }
    }

    private static String buildJdbcUrl(String host, String port, String database) {
        return "jdbc:postgresql://" + host + ":" + port + "/" + database;
    }

    private static String getEnv(String key) {
        String value = System.getenv(key);
        if (value == null) {
            value = System.getProperty(key);
        }
        return value;
    }

    private static String getEnv(String key, String defaultValue) {
        String value = getEnv(key);
        return value != null ? value : defaultValue;
    }

    private static void setIfMissing(String key, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        if (System.getProperty(key) == null && System.getenv(key) == null) {
            System.setProperty(key, value);
        }
    }
}
