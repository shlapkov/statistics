package com.n26;

import com.n26.api.TransactionManager;
import com.n26.model.TransactionManagerImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfig {

    @Bean
    public TransactionManager getTransactionManager() {
        return new TransactionManagerImpl();
    }

}