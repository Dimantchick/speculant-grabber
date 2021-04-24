package tk.dimantchick.speculant.grabber;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class GrabberApplication {

    public static void main(String[] args) {
        SpringApplication.run(GrabberApplication.class, args);
    }

}
