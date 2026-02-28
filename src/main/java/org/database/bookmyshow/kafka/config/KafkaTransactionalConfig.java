package org.database.bookmyshow.kafka.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.transaction.KafkaTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
public class KafkaTransactionalConfig {

//    @Bean(name = "kafkaTransactionManager")
//    @Primary
//    public KafkaTransactionManager<String, String> kafkaTransactionManager(
//            ProducerFactory<String, String> producerFactory) {
//
//        return new KafkaTransactionManager<>(producerFactory);
//    }
}
