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

        configureDatabaseUrl();
        mapLegacyDatabaseEnv();

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
                    getEnv("DB_PORT", "3306"),
                    getEnv("DB_NAME", "legal_platform")
            ));
        }
        setIfMissing("SPRING_DATASOURCE_USERNAME", getEnv("DB_USERNAME", "root"));
        setIfMissing("SPRING_DATASOURCE_PASSWORD", getEnv("DB_PASSWORD", ""));
    }

    private static void configureDatabaseUrl() {
        String databaseUrl = getEnv("DATABASE_URL");
        // Also inspect SPRING_DATASOURCE_URL in case it was passed without jdbc: prefix
        if (databaseUrl == null || databaseUrl.isBlank()) {
            String springUrl = getEnv("SPRING_DATASOURCE_URL");
            if (springUrl != null && !springUrl.startsWith("jdbc:")) {
                databaseUrl = springUrl;
            }
        }

        String jdbcUrl = getEnv("JDBC_DATABASE_URL");
        String dbUser = getEnv("DB_USER");
        String dbPassword = getEnv("DB_PASSWORD");
        
        System.out.println("=== Database Configuration ===");
        System.out.println("DATABASE_URL: " + (databaseUrl != null ? databaseUrl.substring(0, Math.min(50, databaseUrl.length())) + "..." : "null"));
        System.out.println("JDBC_DATABASE_URL: " + (jdbcUrl != null ? jdbcUrl.substring(0, Math.min(50, jdbcUrl.length())) + "..." : "null"));
        System.out.println("DB_USER: " + dbUser);
        System.out.println("DB_PASSWORD: " + (dbPassword != null ? "***" : "null"));
        
        // Try JDBC_DATABASE_URL first (Render-specific)
        if (jdbcUrl != null && !jdbcUrl.isBlank()) {
            System.out.println("Using JDBC_DATABASE_URL");
            System.setProperty("SPRING_DATASOURCE_URL", jdbcUrl);
            if (dbUser != null) System.setProperty("SPRING_DATASOURCE_USERNAME", dbUser);
            if (dbPassword != null) System.setProperty("SPRING_DATASOURCE_PASSWORD", dbPassword);
        }
        // Fall back to DATABASE_URL parsing (handles postgres://, postgresql://, mysql://)
        else if (databaseUrl != null && (databaseUrl.startsWith("postgres://") || databaseUrl.startsWith("postgresql://") || databaseUrl.startsWith("mysql://"))) {
            try {
                boolean isMysql = databaseUrl.startsWith("mysql://");
                String cleanedUrl = databaseUrl.replaceFirst("(postgres(ql)?|mysql)://", "");
                int atSignIndex = cleanedUrl.indexOf('@');
                if (atSignIndex != -1) {
                    String userInfo = cleanedUrl.substring(0, atSignIndex);
                    String hostInfo = cleanedUrl.substring(atSignIndex + 1);
                    
                    String[] userParts = userInfo.split(":");
                    String username = userParts[0];
                    String password = userParts.length > 1 ? userParts[1] : "";
                    
                    String dbUrl = (isMysql ? "jdbc:mysql://" : "jdbc:postgresql://") + hostInfo;
                    if (isMysql) {
                        if (!dbUrl.contains("?")) {
                            dbUrl += "?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
                        }
                    } else if (hostInfo.contains("render.com") && !hostInfo.contains("sslmode")) {
                        if (hostInfo.contains("?")) {
                            dbUrl += "&sslmode=require";
                        } else {
                            dbUrl += "?sslmode=require";
                        }
                    }
                    
                    System.out.println("Parsed JDBC URL: " + dbUrl.substring(0, Math.min(50, dbUrl.length())) + "...");
                    System.out.println("Username: " + username);
                    
                    System.setProperty("SPRING_DATASOURCE_URL", dbUrl);
                    System.setProperty("SPRING_DATASOURCE_USERNAME", username);
                    System.setProperty("SPRING_DATASOURCE_PASSWORD", password);
                } else {
                    String dbUrl = "jdbc:" + databaseUrl;
                    System.setProperty("SPRING_DATASOURCE_URL", dbUrl);
                }
            } catch (Exception e) {
                System.err.println("Error parsing DATABASE_URL: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("No database URL found, using defaults");
        }
        
        System.out.println("Final SPRING_DATASOURCE_URL: " + getEnv("SPRING_DATASOURCE_URL"));
        System.out.println("Final SPRING_DATASOURCE_USERNAME: " + getEnv("SPRING_DATASOURCE_USERNAME"));
        System.out.println("=============================");
    }

    private static String buildJdbcUrl(String host, String port, String database) {
        return "jdbc:mysql://" + host + ":" + port + "/" + database + "?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    }

    private static String getEnv(String key) {
        String value = System.getProperty(key);
        if (value == null) {
            value = System.getenv(key);
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
