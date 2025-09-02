package com.vf.sb_criteria_pager;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.Assert;
import org.vf.sbCriteriaPager.model.Column;
import org.vf.sbCriteriaPager.model.filter.NumberFilter;

import java.util.List;

@ExtendWith(MockitoExtension.class)
@Slf4j
class SbCriteriaPagerApplicationTests {

//!todo probar dezerializacion en caso de fiultro nulo

    @Test
    public void testNoFitler() throws JsonProcessingException {
        final var objectMapper = new ObjectMapper();

        var jsonTest = """
                {
                          "path": "userId",
                          "alias": "userId",
                          "valueType": "NUMBER",
                          "criteriaFilter": { },
                          "sortType": "DESC",
                          "isFiltered": true,
                          "isSorted": true
                        }
                """;
        final Column columnParsed = objectMapper.readValue(jsonTest, Column.class);
        Assert.isTrue(columnParsed.getAlias().equals("userId"), "alias must to be UserId");
    }
    //test deserializacion del json
    @Test
    public void testDeserializationSingleNumberCriteriaFilter() throws JsonProcessingException {
        final var objectMapper = new ObjectMapper();

        var jsonTest = """
                {
                          "path": "userId",
                          "alias": "userId",
                          "valueType": "NUMBER",
                          "criteriaFilter": {
                            "filterType": "EQUALS",
                            "valueType": "NUMBER",
                            "value": 1,
                            "minValue": null,
                            "maxValue": null
                          },
                          "sortType": "DESC",
                          "isFiltered": true,
                          "isSorted": true
                        }
                """;
        final Column columnParsed = objectMapper.readValue(jsonTest, Column.class);
        Assert.isTrue(columnParsed.getAlias().equals("userId"), "alias must to be UserId");
    }

    @Test
    public void testDeserializationBetweenNumberCriteriaFilter() throws JsonProcessingException {
        final var objectMapper = new ObjectMapper();

        var jsonTest = """
                {
                          "path": "userId",
                          "alias": "userId",
                          "valueType": "NUMBER",
                          "criteriaFilter": {
                            "filterType": "BETWEEN",
                            "valueType": "NUMBER",
                            "value": 1,
                            "minValue": 0,
                            "maxValue": 100
                          },
                          "sortType": "DESC",
                          "isFiltered": true,
                          "isSorted": true
                        }
                """;
        final Column columnParsed = objectMapper.readValue(jsonTest, Column.class);
        var filter = (NumberFilter) columnParsed.getCriteriaFilter();
        Assert.isTrue(filter.getMinValue() == 0 && filter.getMaxValue() == 100, "min and max value must be 0 and 100");
    }
    @Test
    public void testDeserializationSingleStringFilters() throws JsonProcessingException {
        final var objectMapper = new ObjectMapper();

        var jsonTest = """
               [ {
                          "path": "name",
                          "alias": "name",
                          "valueType": "STRING",
                          "criteriaFilter": {
                            "filterType": "EQUALS",
                            "valueType": "STRING",
                            "value": "Alan",
                            "regex" : "null"
                          },
                          "sortType": "DESC",
                          "isFiltered": true,
                          "isSorted": true
                        }, {
                          "path": "name",
                          "alias": "name",
                          "valueType": "STRING",
                          "criteriaFilter": {
                            "filterType": "START_WITH",
                            "valueType": "STRING",
                            "value": "Alan",
                            "regex" : "null"
                          },
                          "sortType": "DESC",
                          "isFiltered": true,
                          "isSorted": true
                        },
                        {
                          "path": "name",
                          "alias": "name",
                          "valueType": "STRING",
                          "criteriaFilter": {
                            "filterType": "END_WITH",
                            "valueType": "STRING",
                            "value": "Alan",
                            "regex" : "null"
                          },
                          "sortType": "DESC",
                          "isFiltered": true,
                          "isSorted": true
                        },
                        {
                          "path": "name",
                          "alias": "name",
                          "valueType": "STRING",
                          "criteriaFilter": {
                            "filterType": "LIKE",
                            "valueType": "STRING",
                            "value": "Alan",
                            "regex" : "null"
                          },
                          "sortType": "DESC",
                          "isFiltered": true,
                          "isSorted": true
                        }, {
                          "path": "name",
                          "alias": "name",
                          "valueType": "STRING",
                          "criteriaFilter": {
                            "filterType": "NOT_START",
                            "valueType": "STRING",
                            "value": "Alan",
                            "regex" : "null"
                          },
                          "sortType": "DESC",
                          "isFiltered": true,
                          "isSorted": true
                        },
                        {
                          "path": "name",
                          "alias": "name",
                          "valueType": "STRING",
                          "criteriaFilter": {
                            "filterType": "NOT_END",
                            "valueType": "STRING",
                            "value": "Alan",
                            "regex" : "null"
                          },
                          "sortType": "DESC",
                          "isFiltered": true,
                          "isSorted": true
                        },{
                          "path": "name",
                          "alias": "name",
                          "valueType": "STRING",
                          "criteriaFilter": {
                            "filterType": "REGEX",
                            "valueType": "STRING",
                            "value": "",
                            "regex" : "[a-zA-Z]{0,9}"
                          },
                          "sortType": "DESC",
                          "isFiltered": true,
                          "isSorted": true
                        }]
                """;
        final List<Column> columnParsed = objectMapper.readValue(jsonTest, new TypeReference<List<Column>>() {});

        Assertions.assertEquals(7, columnParsed.size());
    }

    @Test
    public void testDeserializationDateFilters() throws JsonProcessingException {
        final var objectMapper = new ObjectMapper();

        var jsonTest = """
               [ {
                          "path": "birthdate",
                          "alias": "birthdate",
                          "valueType": "DATE",
                          "criteriaFilter": {
                            "filterType": "EQUALS",
                            "valueType": "DATE",
                            "value": "1995-10-02",
                            "startDate" : "",
                            "endDate" : ""
                          },
                          "sortType": "DESC",
                          "isFiltered": true,
                          "isSorted": true
                        }, {
                          "path": "birthdate",
                          "alias": "birthdate",
                          "valueType": "DATE",
                          "criteriaFilter": {
                            "filterType": "BEFORE",
                            "valueType": "DATE",
                            "value": "1995-10-02",
                            "startDate" : "",
                            "endDate" : ""
                          },
                          "sortType": "DESC",
                          "isFiltered": true,
                          "isSorted": true
                        },
                        {
                          "path": "birthdate",
                          "alias": "birthdate",
                          "valueType": "DATE",
                          "criteriaFilter": {
                            "filterType": "AFTER",
                            "valueType": "DATE",
                            "value": "1995-10-02",
                            "startDate" : "",
                            "endDate" : ""
                          },
                          "sortType": "DESC",
                          "isFiltered": true,
                          "isSorted": true
                        },
                        {
                          "path": "birthdate",
                          "alias": "birthdate",
                          "valueType": "DATE",
                          "criteriaFilter": {
                            "filterType": "BETWEEN",
                            "valueType": "DATE",
                            "value": "null",
                            "startDate" : "1995-01-01",
                            "endDate" : "1995-12-31"
                          },
                          "sortType": "DESC",
                          "isFiltered": true,
                          "isSorted": true
                        }]
                """;
        final List<Column> columnParsed = objectMapper.readValue(jsonTest, new TypeReference<List<Column>>() {});

        Assertions.assertEquals(4, columnParsed.size());
    }

}
