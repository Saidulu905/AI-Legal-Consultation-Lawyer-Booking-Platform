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

        // Auto-configure Spring Data Source from DATABASE_URL if present (useful for Render/Heroku)
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

    private static void configureDatabaseUrl() {
        String databaseUrl = System.getenv("DATABASE_URL");
        if (databaseUrl == null) {
            databaseUrl = System.getProperty("DATABASE_URL");
        }
        
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
                    
                    System.setProperty("spring.datasource.url", dbUrl);
                    System.setProperty("spring.datasource.username", username);
                    System.setProperty("spring.datasource.password", password);
                }
            } catch (Exception e) {
                // Fail silently
            }
        }
    }
}
