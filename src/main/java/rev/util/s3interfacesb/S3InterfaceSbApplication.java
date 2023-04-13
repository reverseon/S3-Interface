package rev.util.s3interfacesb;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySources;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Slf4j
@SpringBootApplication
@PropertySources({
		@org.springframework.context.annotation.PropertySource("classpath:application.properties"),
		@org.springframework.context.annotation.PropertySource("classpath:app-s3.properties")
})
@EnableWebMvc
public class S3InterfaceSbApplication {

	public static void main(String[] args) {
		SpringApplication.run(S3InterfaceSbApplication.class, args);
	}

}
