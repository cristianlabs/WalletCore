package br.com.User.walletcore.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.config.PageableHandlerMethodArgumentResolverCustomizer;

@Configuration
public class WebConfig {

    // Caps page size regardless of what the client requests, so "?size=999999999"
    // can't reintroduce the unbounded-query problem pagination is meant to fix.
    @Bean
    public PageableHandlerMethodArgumentResolverCustomizer pageableCustomizer() {
        return resolver -> resolver.setMaxPageSize(100);
    }
}
