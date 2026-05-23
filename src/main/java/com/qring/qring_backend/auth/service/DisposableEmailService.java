package com.qring.qring_backend.auth.service;

import org.springframework.stereotype.Service;

import java.util.Set;

/** 일회용/임시 이메일 도메인을 차단하기 위한 화이트리스트 기반 판별기. */
@Service
public class DisposableEmailService {

    private static final Set<String> DISPOSABLE_DOMAINS = Set.of(
        "mailinator.com", "guerrillamail.com", "guerrillamail.net", "guerrillamail.org",
        "tempmail.com", "temp-mail.org", "temp-mail.io", "throwaway.email",
        "yopmail.com", "yopmail.fr", "sharklasers.com", "guerrillamailblock.com",
        "grr.la", "dispostable.com", "trashmail.com", "trashmail.me", "trashmail.net",
        "mailnesia.com", "maildrop.cc", "discard.email", "fakeinbox.com",
        "tempail.com", "tempr.email", "tempmailaddress.com", "tempmailo.com",
        "getairmail.com", "mailcatch.com", "meltmail.com", "harakirimail.com",
        "mailnator.com", "binkmail.com", "bobmail.info", "chammy.info",
        "devnullmail.com", "emailisvalid.com", "emailwarden.com", "gishpuppy.com",
        "mailexpire.com", "mailmoat.com", "mytempemail.com", "nomail.xl.cx",
        "spamfree24.org", "trashymail.com", "trashymail.net", "yopmail.net",
        "10minutemail.com", "10minutemail.net", "20minutemail.com",
        "guerrillamail.de", "guerrillamail.biz", "tempinbox.com",
        "burnermail.io", "mailsac.com", "mohmal.com", "getnada.com",
        "emailondeck.com", "mintemail.com", "tempmails.net",
        "tmpmail.net", "tmpmail.org", "emailfake.com", "crazymailing.com",
        "mailtemp.net", "moakt.com", "tmail.ws", "tempmailer.com",
        "throwam.com", "wegwerfmail.de", "wegwerfmail.net", "byom.de",
        "trash-mail.com", "mailzilla.com", "armyspy.com", "cuvox.de",
        "dayrep.com", "einrot.com", "fleckens.hu", "gustr.com",
        "jourrapide.com", "rhyta.com", "superrito.com", "teleworm.us",
        "tempe.ml", "tempsky.com", "1secmail.com", "1secmail.net", "1secmail.org"
    );

    /** 이메일의 도메인이 일회용 도메인 목록에 속하면 true. */
    public boolean isDisposable(String email) {
        if (email == null || !email.contains("@")) return false;
        String domain = email.substring(email.lastIndexOf('@') + 1).toLowerCase().trim();
        return DISPOSABLE_DOMAINS.contains(domain);
    }
}
