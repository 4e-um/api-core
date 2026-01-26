package com.project.core.infra.entity.batch;

import java.util.Arrays;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BatchJobType {
    INVOICE_ITEM_JOB("invoiceItemJob", "INVOICE_ITEM"),
    INVOICE_JOB("invoiceJob", "INVOICE");

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
