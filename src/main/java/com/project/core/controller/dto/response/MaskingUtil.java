package com.project.core.controller.dto.response;

public final class MaskingUtil {
  private MaskingUtil() {}

  // 마스킹 010-**12-**12의 형식
  public static String maskPhone(String phone) {
    if (phone == null || phone.isBlank()) {
      return phone;
    }

    // 숫자만 추출
    String digits = phone.replaceAll("\\D", "");

    // 휴대폰 번호 길이 최소 검증 (010XXXXXXXX 기준)
    if (digits.length() != 11) {
      return "***";
    }

    String first = digits.substring(0, 3);
    String middle = digits.substring(3, 7);
    String last = digits.substring(7, 11);

    // 010-**34-**12
    return String.format("%s-**%s-**%s", first, middle.substring(2), last.substring(2));
  }

  public static String maskEmail(String email) {
    if (email == null || email.isBlank()) {
      return email;
    }

    int at = email.indexOf('@');
    if (at < 0) {
      return null;
    } // @ 없으면 null

    String local = email.substring(0, at);
    String domain = email.substring(at); // "@domain.com"

    // local 비어있으면 "***@domain.com"
    if (local.isEmpty()) {
      return "***" + domain;
    }

    // local 1글자 이상이면 "첫 글자 + *** + @domain.com"
    return local.substring(0, 1) + "***" + domain;
  }
}
