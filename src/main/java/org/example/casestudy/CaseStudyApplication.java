package org.example.casestudy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
@EntityScan(basePackages = "org.example.casestudy.entities")
public class CaseStudyApplication {

    public static void main(String[] args) {
        SpringApplication.run(CaseStudyApplication.class, args);
    }

}
