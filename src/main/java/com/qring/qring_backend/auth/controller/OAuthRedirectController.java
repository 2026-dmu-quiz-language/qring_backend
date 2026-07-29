package com.qring.qring_backend.auth.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/** OAuth 제공자의 리다이렉트를 받아 state에 담긴 앱 딥링크로 인증 결과를 전달하는 브릿지 페이지. */
@RestController
public class OAuthRedirectController {

    private static final String BRIDGE_HTML = """
        <!DOCTYPE html>
        <html>
        <head><meta charset="utf-8"><title>Qring 로그인</title></head>
        <body style="font-family: sans-serif; text-align: center; padding-top: 80px;">
        <p>앱으로 돌아가는 중입니다...</p>
        <p><a id="returnLink" href="#" style="display: none; padding: 14px 28px; background: #6F9F63;
          color: #fff; border-radius: 12px; text-decoration: none; font-weight: bold;">앱으로 돌아가기</a></p>
        <script>
        (function () {
          var merged = new URLSearchParams(window.location.search);
          var frag = new URLSearchParams(window.location.hash.replace('#', ''));
          frag.forEach(function (v, k) { if (!merged.has(k)) merged.append(k, v); });
          merged.set('provider', window.location.pathname.split('/').pop());

          var returnUrl = merged.get('state') || '';
          var allowedPrefixes = ['exp://', 'exps://', 'qring://', 'http://localhost', 'https://localhost'];
          var allowed = allowedPrefixes.some(function (p) { return returnUrl.indexOf(p) === 0; });
          if (!allowed) {
            document.body.textContent = '잘못된 접근입니다.';
            return;
          }
          var target = returnUrl + (returnUrl.indexOf('?') >= 0 ? '&' : '?') + merged.toString();

          // 인앱 브라우저가 자동 이동을 차단하는 경우를 대비한 수동 버튼
          var link = document.getElementById('returnLink');
          link.href = target;
          link.style.display = 'inline-block';

          window.location.replace(target);
        })();
        </script>
        </body>
        </html>
        """;

    /** 소셜 로그인 브릿지: 쿼리(code)와 프래그먼트(id_token) 파라미터를 앱 딥링크로 전달한다. */
    @GetMapping(value = "/oauth/{provider}", produces = MediaType.TEXT_HTML_VALUE + ";charset=UTF-8")
    public String bridge(@PathVariable String provider) {
        return BRIDGE_HTML;
    }
}
