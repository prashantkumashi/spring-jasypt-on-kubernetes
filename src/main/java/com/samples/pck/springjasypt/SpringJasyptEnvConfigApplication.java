package com.samples.pck.springjasypt;

import org.jasypt.encryption.pbe.config.EnvironmentStringPBEConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@SpringBootApplication
@EnableWebMvc
public class SpringJasyptEnvConfigApplication {

   public static void main(String[] args) {
      SpringApplication.run(SpringJasyptEnvConfigApplication.class, args);
   }

   @Autowired
   Environment env;

   public String getProperty(String pPropertyKey) {
      return env.getProperty(pPropertyKey);
   }
    
   @Bean
   public EnvironmentStringPBEConfig environmentStringPBEConfig() {
       EnvironmentStringPBEConfig config = new EnvironmentStringPBEConfig();
       config.setAlgorithm("jasypt.encryptor.algorithm");
       config.setPasswordEnvName("jasypt.encryptor.password");
       return config;
   }
}
