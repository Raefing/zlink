package com.test.zlink;

import com.test.zlink.configuration.TestConfiguration;
import com.zlink.service.api.annotation.ServiceScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@ServiceScan
@SpringBootApplication
public class Starter {
    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(Starter.class, args);
        TestConfiguration configuration = context.getBean(TestConfiguration.class);
        configuration.doInit();
    }

    public static void mains(String[] args) {
        int index = 0;
        while (true) {
            int[] data = LEDData.bolang(index++);
            for (int i = 0; i < data.length; i++) {
                System.err.print(hex(data[i]) + " ");
            }
            if (index > 47) {
                index = 0;
            }
            System.err.println();
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }


    public static String hex(int d) {
        if (d == 0x80) {
            return "  ";
        }
        return Integer.toHexString(d);
    }
}
