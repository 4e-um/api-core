package com.project.core.controller;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.verify;

import com.project.core.service.BatchScheduleService;
import com.project.core.util.BatchTriggerClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;


@WebMvcTest(InvoiceBatchAdminController.class)
class InvoiceBatchAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BatchTriggerClient batchTriggerClient;

    @MockitoBean
    private BatchScheduleService scheduleService;

    @Test
    @DisplayName("스케줄 수정 트리거 성공")
    void updateSchedule_should_update_db_and_trigger_cf() throws Exception {
        // given
        String body = """
            {
              "jobName": "invoiceJob",
              "cron": "0 0 2 * * ?"
            }
        """;

        // when & then
        mockMvc.perform(
                        put("/admin/batch/invoice/schedule")
                                .contentType("application/json")
                                .content(body)
                )
                .andExpect(status().isOk());

        // then (행위 검증)
        verify(scheduleService)
                .updateSchedule("invoiceJob", "0 0 2 * * ?");

        verify(batchTriggerClient)
                .schedule("invoiceJob", "0 0 2 * * ?");
    }

    @Test
    @DisplayName("배치 작업 즉시 실행 성공")
    void runNow_should_trigger_batch() throws Exception {

        mockMvc.perform(
                        post("/admin/batch/invoice/run-now")
                                .param("jobName", "invoiceJob")
                                .param("invMonth", "202601")
                )
                .andExpect(status().isAccepted());

        verify(batchTriggerClient)
                .trigger("invoiceJob", "202601");
    }
}