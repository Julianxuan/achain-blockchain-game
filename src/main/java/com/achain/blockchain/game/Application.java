package com.achain.blockchain.game;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import java.util.Arrays;

/**
 * @author yujian
 */
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        ApplicationContext ctx = SpringApplication.run(Application.class, args);
        String[] activeProfiles = ctx.getEnvironment().getActiveProfiles();
        System.out.println("##################################################");
        System.out.println("start success: " + Arrays.toString(activeProfiles));
        System.out.println("##################################################");
    }
}
