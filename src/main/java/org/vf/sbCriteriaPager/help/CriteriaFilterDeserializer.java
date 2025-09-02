package org.vf.sbCriteriaPager.help;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import lombok.extern.slf4j.Slf4j;
import org.vf.sbCriteriaPager.common.FilterType;
import org.vf.sbCriteriaPager.common.QueryStep;
import org.vf.sbCriteriaPager.common.ValueType;
import org.vf.sbCriteriaPager.exception.InvalidArgumentException;
import org.vf.sbCriteriaPager.model.filter.*;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class CriteriaFilterDeserializer extends StdDeserializer<CriteriaFilter> {
    private static final String FILTER_TYPE = "filterType";
    private static final String VALUE_TYPE = "valueType";
    private static final String VALUE = "value";
    private static final String MIN_VALUE = "minValue";
    private static final String MAX_VALUE = "maxValue";
    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    public CriteriaFilterDeserializer() {
        super(CharSequence.class);
    }

    @Override
    public CriteriaFilter deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
        final var mObjectMapper = (ObjectMapper) jsonParser.getCodec();
        final JsonNode mNodeTree = mObjectMapper.readTree(jsonParser);
        if (mNodeTree.isEmpty()) {
            return null;
        }
        final var mJsonNodeValueType = mNodeTree.get(VALUE_TYPE);
        final var mValueTypeEnum = ValueType.valueOf(mJsonNodeValueType.asText());
        final var mMapCommonProperties = this.getCommonValues(mNodeTree);
        //!todo review if the column config is filtrable
        if (hasProperties(mMapCommonProperties)) {
            return switch (mValueTypeEnum) {
                case BOOLEAN -> parseToBooleanFilter(mNodeTree);
                case STRING -> parseToStringFilter(mNodeTree);
                case DATE -> parseToDateFilter(mNodeTree);
                case NUMBER -> parseToNumberFilter(mNodeTree);
                default ->
                        throw new IllegalArgumentException(String.format("Invalid Value Type:  %s", mJsonNodeValueType.asText()));
            };
        } else {
            log.error("Error in object");
            mMapCommonProperties.forEach((key, value) -> log.error("{}:{}", key, value));
            throw new IllegalArgumentException("No deserializable Object");
        }


    }

    private BooleanFilter parseToBooleanFilter(JsonNode pJsonNode) {
        final var mValueTypeEnum = pJsonNode.get(VALUE_TYPE) != null ? ValueType.valueOf(pJsonNode.get(VALUE_TYPE).asText()) : null;
        final var mFilterTypeEnum = pJsonNode.get(FILTER_TYPE) != null ? FilterType.valueOf(pJsonNode.get(FILTER_TYPE).asText()) : null;
        final var mValueBln = pJsonNode.get(VALUE) != null ? pJsonNode.get(VALUE).asBoolean() : null;
        return BooleanFilter.builder()
                .pValue(mValueBln)
                .filterType(mFilterTypeEnum)
                .valueType(mValueTypeEnum)
                .build();
    }

    private StringFilter parseToStringFilter(JsonNode pJsonNode) {
        final var mValueTypeEnum = pJsonNode.get(VALUE_TYPE) != null ? ValueType.valueOf(pJsonNode.get(VALUE_TYPE).asText()) : null;
        final var mFilterTypeEnum = pJsonNode.get(FILTER_TYPE) != null ? FilterType.valueOf(pJsonNode.get(FILTER_TYPE).asText()) : null;
        final var mValueStr = pJsonNode.get(VALUE) != null ? pJsonNode.get(VALUE).asText() : null;
        final var mRegexStr = pJsonNode.get("regex") != null ? pJsonNode.get("regex").asText() : null;
        return StringFilter.builder()
                .pValueStr(mValueStr)
                .pRegexStr(mRegexStr)
                .pFilterType(mFilterTypeEnum)
                .pValueType(mValueTypeEnum)
                .build();
    }

    private DateFilter parseToDateFilter(JsonNode pJsonNode) {
        try {
            Date mEndDate = null;
            Date mStartDate = null;
            Date mValueDate = null;
            final var mFilterTypeEnum = pJsonNode.get(FILTER_TYPE) != null ? FilterType.valueOf(pJsonNode.get(FILTER_TYPE).asText()) : null;
            final var mValueTypeEnum = pJsonNode.get(VALUE_TYPE) != null ? ValueType.valueOf(pJsonNode.get(VALUE_TYPE).asText()) : null;

            if (mFilterTypeEnum != null && (mFilterTypeEnum.equals(FilterType.AFTER) || mFilterTypeEnum.equals(FilterType.BEFORE) || mFilterTypeEnum.equals(FilterType.EQUALS))) {
                final var mValueDateStr = pJsonNode.get("value").asText();
                if (mValueDateStr == null) {
                    throw new IllegalArgumentException("Value field does not exist in Date filter");
                }
                mValueDate = SIMPLE_DATE_FORMAT.parse(mValueDateStr);
            } else if (mFilterTypeEnum != null && mFilterTypeEnum.equals(FilterType.BETWEEN)) {
                final var mStartDateStr = pJsonNode.get("startDate").asText();
                final var mEndDateStr = pJsonNode.get("endDate").asText();
                if ((mStartDateStr.equals("null") && mStartDateStr == null) || mEndDateStr.equals("null") && mStartDateStr == null) {
                    throw new IllegalArgumentException("startDAte or EndDate field does not exist in Date filter");
                }
                mEndDate = SIMPLE_DATE_FORMAT.parse(mEndDateStr);
                mStartDate = SIMPLE_DATE_FORMAT.parse(mStartDateStr);
            }
            //   final var mStartDate = pJsonNode.get("startDate") != null ? SIMPLE_DATE_FORMAT.parse(pJsonNode.get("startDate").asText()) : null;
            //    final var mEndDate = pJsonNode.get("endDate") != null ? SIMPLE_DATE_FORMAT.parse(pJsonNode.get("endDate").asText()) : null;
            //   final var mValueDate = pJsonNode.get(VALUE) != null ? SIMPLE_DATE_FORMAT.parse(pJsonNode.get(VALUE).asText()) : null;
            return DateFilter.builder()
                    .pValueDate(mValueDate)
                    .pEndDate(mEndDate)
                    .pStartDate(mStartDate)
                    .pFilterType(mFilterTypeEnum)
                    .pValueType(mValueTypeEnum)
                    .build();
        } catch (ParseException e) {
            log.error("Parse exception in a DateFilter");
            log.error("Object Structure:  \n {}", pJsonNode.asText());
            return null;
        }
    }

    private NumberFilter parseToNumberFilter(JsonNode pJsonNode) {
        final var mValueInt = Integer.valueOf(pJsonNode.get(VALUE).asText());
        final var mMinValueInt = pJsonNode.has(MIN_VALUE) ? pJsonNode.get(MIN_VALUE).asInt(0) : null;
        final var mMaxValueInt = pJsonNode.has(MAX_VALUE) ? pJsonNode.get(MAX_VALUE).asInt(0) : null;
        return NumberFilter.builder()
                .pFilterType(FilterType.valueOf(pJsonNode.get(FILTER_TYPE).asText()))
                .pValueType(ValueType.valueOf(pJsonNode.get(VALUE_TYPE).asText()))
                .pValueInt(mValueInt)
                .pMaxValue(mMaxValueInt)
                .pMinValue(mMinValueInt)
                .build();

    }

    private boolean hasProperties(Map<String, String> pPropertiesMap) {
        var hasAll = true;
        for (final var item : pPropertiesMap.entrySet()) {
            if (item.getValue() == null) {
                hasAll = false;
                break;
            }

        }
        return hasAll;
    }

    private Map<String, String> getCommonValues(JsonNode pJsonNode) {
        final var mFilterTypeStr = pJsonNode.get(FILTER_TYPE) != null ? pJsonNode.get(FILTER_TYPE).asText() : null;
        final var mValueTypeStr = pJsonNode.get(VALUE_TYPE) != null ? pJsonNode.get(VALUE_TYPE).asText() : null;
        final var mPropertiesMap = new HashMap<String, String>();
        mPropertiesMap.put(FILTER_TYPE, mFilterTypeStr);
        mPropertiesMap.put(VALUE_TYPE, mValueTypeStr);
        return mPropertiesMap;
    }
}
