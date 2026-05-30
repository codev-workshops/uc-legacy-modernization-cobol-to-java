package com.mainframe.carddemo.transaction;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = {
        "com.mainframe.carddemo.transaction",
        "com.mainframe.carddemo.common"
})
@EnableFeignClients(basePackages = "com.mainframe.carddemo.common.client")
public class TransactionServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(TransactionServiceApplication.class, args);
    }
}
