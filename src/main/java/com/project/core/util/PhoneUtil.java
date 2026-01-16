package com.project.core.util;

import java.util.concurrent.ThreadLocalRandom;
import lombok.experimental.UtilityClass;

@UtilityClass
public class PhoneUtil {

  // 010-XXXX-XXXX 형식의 랜덤 번호 생성
  public static String generateRandomPhoneNumber() {
    int mid = ThreadLocalRandom.current().nextInt(1000, 10000);
    int last = ThreadLocalRandom.current().nextInt(1000, 10000);
    return String.format("010-%d-%d", mid, last);
  }
}
