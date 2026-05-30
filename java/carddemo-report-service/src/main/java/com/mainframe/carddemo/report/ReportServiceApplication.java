package com.mainframe.carddemo.report;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = {
        "com.mainframe.carddemo.report",
        "com.mainframe.carddemo.common"
})
@EnableFeignClients(basePackages = "com.mainframe.carddemo.common.client")
public class ReportServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReportServiceApplication.class, args);
    }
}
