package com.qring.qring_backend.auth.service;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/** JDK 내장 JNDI DNS 프로바이더로 시스템 리졸버에 MX 레코드를 질의한다. */
@Slf4j
@Component
public class JndiDnsMxResolver implements DnsMxResolver {

    /** 1회 질의 타임아웃(ms). JNDI 기본값(1s × 4회 재시도, 배수 증가)은 최악 15초라 짧게 고정한다. */
    @Setter
    @Value("${qring.email.dns-check.timeout-ms:2000}")
    private int timeoutMs = 2000;

    @Setter
    @Value("${qring.email.dns-check.retries:1}")
    private int retries = 1;

    @Override
    public Result lookupMx(String domain) {
        Hashtable<String, String> env = new Hashtable<>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.dns.DnsContextFactory");
        env.put(Context.PROVIDER_URL, "dns:");   // 시스템(OS) 리졸버 사용
        env.put("com.sun.jndi.dns.timeout.initial", String.valueOf(timeoutMs));
        env.put("com.sun.jndi.dns.timeout.retries", String.valueOf(retries));

        DirContext ctx = null;
        try {
            ctx = new InitialDirContext(env);
            Attributes attrs = ctx.getAttributes(domain, new String[] {"MX"});
            Attribute mx = attrs.get("MX");
            if (mx == null || mx.size() == 0) {
                return Result.NO_MX;
            }
            // 값 형식: "10 mx1.naver.com." — 마지막 토큰이 "." 이면 널 MX(RFC 7505, 메일 수신 거부 선언)
            NamingEnumeration<?> values = mx.getAll();
            while (values.hasMore()) {
                String[] parts = String.valueOf(values.next()).trim().split("\\s+");
                String host = parts[parts.length - 1];
                if (!".".equals(host)) {
                    return Result.HAS_MX;
                }
            }
            return Result.NO_MX;
        } catch (NameNotFoundException e) {
            return Result.NO_MX;   // NXDOMAIN: 도메인 자체가 없음
        } catch (NamingException e) {
            log.warn("DNS MX lookup failed for {}: {}", domain, e.getMessage());
            return Result.LOOKUP_FAILED;
        } finally {
            if (ctx != null) {
                try { ctx.close(); } catch (NamingException ignored) { }
            }
        }
    }
}
