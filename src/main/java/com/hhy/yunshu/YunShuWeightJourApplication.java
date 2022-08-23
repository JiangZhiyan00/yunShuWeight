package com.hhy.yunshu;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@MapperScan(value="com.**.mapper.**")
@EnableTransactionManagement
@SpringBootApplication
public class YunShuWeightJourApplication {

    public static void main(String[] args) {
        SpringApplication.run(YunShuWeightJourApplication.class, args);
    }

}
