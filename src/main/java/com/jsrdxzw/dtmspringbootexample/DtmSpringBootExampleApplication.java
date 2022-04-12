package com.jsrdxzw.dtmspringbootexample;

import com.jsrdxzw.dtmspringbootstarter.annotations.EnableDtm;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableDtm
public class DtmSpringBootExampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(DtmSpringBootExampleApplication.class, args);
    }

}