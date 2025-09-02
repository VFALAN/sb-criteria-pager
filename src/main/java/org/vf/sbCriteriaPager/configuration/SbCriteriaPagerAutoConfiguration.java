package org.vf.sbCriteriaPager.configuration;

import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.vf.sbCriteriaPager.services.PageQueryService;

@AutoConfiguration
@ConditionalOnClass(PageQueryService.class)
@Slf4j
public class SbCriteriaPagerAutoConfiguration {


    @Bean
    @ConditionalOnMissingBean
    public PageQueryService pageQueryService(EntityManager entityManager) {
        log.info("Staring Page Server Bean");
        return new PageQueryService(entityManager);
    }
}
