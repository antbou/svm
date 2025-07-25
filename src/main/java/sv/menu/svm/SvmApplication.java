package sv.menu.svm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SvmApplication {

    public static void main(String[] args) {
        SpringApplication.run(SvmApplication.class, args);
    }
}
