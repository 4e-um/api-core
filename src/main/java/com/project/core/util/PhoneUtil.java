package com.project.core.util;

import java.util.Random;

public class PhoneUtil {
	private static final Random random = new Random();

	// 010-XXXX-XXXX 형식의 랜덤 번호 생성
	public static String generateRandomPhoneNumber() {
		int mid = random.nextInt(9000) + 1000;	// 1000 ~ 9999
		int last = random.nextInt(9000) + 1000;
		return String.format("010-%d-%d", mid, last);
	}
}
