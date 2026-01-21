package com.project.core.infra.entity.batch;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum BatchJobType {

    INVOICE_ITEM_JOB("invoiceItemJob", "청구 내역 생성"),
    INVOICE_JOB("invoiceJob", "청구서 생성");

    private final String jobName;
    private final String title;

    public static String getTitleByJobName(String jobName) {
        return Arrays.stream(values())
                .filter(type -> type.jobName.equals(jobName))
                .findFirst()
                .map(BatchJobType::getTitle)
                .orElse(jobName);
    }
}
