package team.moebius.disposer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DisposerApplication {

    public static void main(String[] args) {
        SpringApplication.run(DisposerApplication.class, args);
    }

}
