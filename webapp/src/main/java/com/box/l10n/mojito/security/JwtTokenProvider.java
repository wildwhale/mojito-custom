package com.box.l10n.mojito.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final String RESOLVE_HEADER = "X-EXTEND-AUTH-TOKEN";

    @Value("${l10n.security.extend.secret}")
    private String secretKey;

    /*
     * token 생성 로직에 변경이 있을 경우
     * tokengenerator 모듈의 TokenGenerator도 수정 해줘야함..
     * 나중에 리펙토링..
     */
    // unused
    public String createToken(String userName) {
        return createToken(this.secretKey, userName);
    }

    // unused
    public String createToken(String secretKey, String userName) {
        Claims claims = Jwts.claims().setSubject(userName);
        Date now = new Date();
        return Jwts.builder()
                .setClaims(claims) // 데이터
                .setIssuedAt(now) // 토큰 발행일자
                .signWith(SignatureAlgorithm.HS256, secretKey) // 암호화 알고리즘, secret값 세팅
                .compact();
    }

    public String resolveToken(HttpServletRequest req) {
        return req.getHeader(RESOLVE_HEADER);
    }

    public boolean validateToken(String jwtToken) {
        try {
            Jwts.parser().setSigningKey(secretKey).parseClaimsJws(jwtToken);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}