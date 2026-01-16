package com.project.core.controller.dto.response;

public final class MaskingUtil {
  private MaskingUtil() {}

  // 예: 01012345678 -> 010****5678 (길이에 따라 유연하게)
  public static String maskPhone(String phone) {
    if (phone == null || phone.isBlank()) return phone;
    String digits = phone.replaceAll("\\D", "");
    if (digits.length() < 7) return "***"; // 너무 짧으면 안전하게

    int prefix = Math.min(3, digits.length());
    int suffix = 4;
    if (digits.length() <= prefix + suffix) return "***";

    String start = digits.substring(0, prefix);
    String end = digits.substring(digits.length() - suffix);
    return start + "****" + end;
  }

  // 이메일 마스킹도 필요하면 함께 (예: e***@example.com)
  public static String maskEmail(String email) {
    if (email == null || email.isBlank()) return email;
    int at = email.indexOf('@');
    if (at <= 0) return null; // 기존 테스트처럼 '@' 없으면 null 처리

    String local = email.substring(0, at);
    String domain = email.substring(at);
    if (local.length() == 1) return local + "***" + domain;
    return local.substring(0, 1) + "***" + domain;
  }
}
