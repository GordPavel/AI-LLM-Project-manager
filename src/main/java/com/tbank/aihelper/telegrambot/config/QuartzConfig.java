package com.tbank.aihelper.telegrambot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import com.tbank.aihelper.AutowiringSpringBeanJobFactory;

@Configuration
public class QuartzConfig {
    @Bean
    public AutowiringSpringBeanJobFactory autowiringSpringBeanJobFactory() {
        return new AutowiringSpringBeanJobFactory();
    }

    @Bean
    public SchedulerFactoryBean schedulerFactoryBean() {
        SchedulerFactoryBean factory = new SchedulerFactoryBean();
        factory.setJobFactory(autowiringSpringBeanJobFactory());
        return factory;
    }
}
