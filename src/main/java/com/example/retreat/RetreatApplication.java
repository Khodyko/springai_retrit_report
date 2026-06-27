package com.example.retreat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Точка входа демо-приложения «Ассистент ретрита».
 */
@SpringBootApplication
public class RetreatApplication {

    /**
     * Запускает Spring Boot приложение.
     *
     * @param args аргументы командной строки
     */
    public static void main(String[] args) {
        SpringApplication.run(RetreatApplication.class, args);
    }
}
