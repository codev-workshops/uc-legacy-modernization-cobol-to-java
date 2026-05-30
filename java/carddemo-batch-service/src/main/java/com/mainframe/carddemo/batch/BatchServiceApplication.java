package com.mainframe.carddemo.batch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = {
        "com.mainframe.carddemo.batch",
        "com.mainframe.carddemo.common"
})
@EnableFeignClients(basePackages = "com.mainframe.carddemo.common.client")
public class BatchServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(BatchServiceApplication.class, args);
    }
}
