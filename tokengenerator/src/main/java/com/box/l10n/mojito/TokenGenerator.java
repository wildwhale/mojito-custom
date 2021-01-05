package com.box.l10n.mojito;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

@SpringBootApplication
public class TokenGenerator implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(TokenGenerator.class, args);
    }

    @Override
    public void run(String... strings) throws Exception {
        if (strings.length < 2) {
            System.out.println("required secretKey and username");
            return ;
        }
        String secretKey = strings[0];
        String userName = strings[1];
        System.out.println(String.format("secretKey : %s / username : %s", secretKey, userName));
        Claims claims = Jwts.claims().setSubject(userName);
        Date now = new Date();
        String compact = Jwts.builder()
                .setClaims(claims) // 데이터
                .setIssuedAt(now) // 토큰 발행일자
                .signWith(SignatureAlgorithm.HS256, secretKey) // 암호화 알고리즘, secret값 세팅
                .compact();
        Path path = Paths.get("token.txt");
        try {
            Files.write(path, compact.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(String.format("[JWT TOKEN] >>>  %s <<< ",compact));
    }
}
