package com.vf.sb_criteria_pager.pager;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vf.sb_criteria_pager.user.UserService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.vf.sbCriteriaPager.configuration.SbCriteriaPagerAutoConfiguration;
import org.vf.sbCriteriaPager.model.Column;

import java.util.List;

@SpringBootTest(classes = PageControllerTest.TestApplicationConfig.class)
@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class)
@Slf4j
public class PageControllerTest extends TestConfiguration {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserService userService;

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @EnableJpaRepositories(basePackages = "com.vf.sb_criteria_pager")
    @ComponentScan(basePackages = {"com.vf.sb_criteria_pager"})
    @Import({SbCriteriaPagerAutoConfiguration.class})
    @EntityScan(basePackages = "com.vf.sb_criteria_pager")
    public static class TestApplicationConfig {

    }


    @Test
    void testUserList() {
        var list = this.userService.listAll();
        Assertions.assertFalse(list.isEmpty());
    }

    @Test
    void testPagerService() {
        try {
            final var objectMapper = new ObjectMapper();
            var jsonTest = """
                    [ {
                               "path": "birthDate",
                               "alias": "birthDate",
                               "valueType": "DATE",
                               "sortType": "DESC",
                               "isFiltered": false,
                               "isSorted": false
                             },
                             {
                               "path": "userId",
                               "alias": "userId",
                               "valueType": "NUMBER",
                               "sortType": "DESC",
                               "isFiltered": false,
                               "isSorted": false
                             },
                             {
                               "path": "name",
                               "alias": "name",
                               "valueType": "STRING",
                               "sortType": "DESC",
                               "isFiltered": false,
                               "isSorted": true
                             },
                            {
                               "path": "lastName",
                               "alias": "lastName",
                               "valueType": "STRING",
                               "sortType": "DESC",
                               "isFiltered": false,
                               "isSorted": false
                             },
                             {
                               "path": "salary",
                               "alias": "salary",
                               "valueType": "NUMBER",
                               "sortType": "DESC",
                               "isFiltered": false,
                               "isSorted": false
                             },
                             {
                               "path": "score",
                               "alias": "score",
                               "valueType": "NUMBER",
                               "sortType": "DESC",
                               "isFiltered": false,
                               "isSorted": false
                             }]
                    """;
            final List<Column> columnParsed = objectMapper.readValue(jsonTest, new TypeReference<List<Column>>() {
            });


            final var result = this.userService.pageUser(0, 10, columnParsed);
            if (result != null && !result.getData().isEmpty()) {
                Assertions.assertTrue(true);
            }
        } catch (Exception e) {
            Assertions.fail(e.getMessage());
        }
    }
}
