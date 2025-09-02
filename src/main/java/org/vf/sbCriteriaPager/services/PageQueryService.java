package org.vf.sbCriteriaPager.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.*;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.vf.sbCriteriaPager.common.QueryStep;
import org.vf.sbCriteriaPager.common.SortType;
import org.vf.sbCriteriaPager.exception.InvalidArgumentException;
import org.vf.sbCriteriaPager.model.Column;
import org.vf.sbCriteriaPager.model.PageQueryResponse;
import org.vf.sbCriteriaPager.model.filter.BooleanFilter;
import org.vf.sbCriteriaPager.model.filter.DateFilter;
import org.vf.sbCriteriaPager.model.filter.NumberFilter;
import org.vf.sbCriteriaPager.model.filter.StringFilter;
import org.vf.sbCriteriaPager.utils.FieldsUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class PageQueryService {
    private final static int CERO_RECORDS = 0;
    private final EntityManager entityManager;
    private final ObjectMapper objectMapper;
    private final SimpleDateFormat simpleDateFormat;


    @Builder
    public PageQueryService(final EntityManager pEntityManager) {

        entityManager = pEntityManager;
        objectMapper = new ObjectMapper();
        simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
    }

    public <T> PageQueryResponse<T> search(Class entitySource, Class<T> targetClass, int page, int size, List<Column> columnList) throws InvalidArgumentException {
        if (size <= CERO_RECORDS) {
            throw new InvalidArgumentException(QueryStep.VALIDATING_PARAMETERS, entitySource.getName(), "records size invalid");
        }
        if (page < CERO_RECORDS) {
            throw new InvalidArgumentException(QueryStep.VALIDATING_PARAMETERS, entitySource.getName(), "page invalid");
        }
        if (!validAllPaths(columnList, entitySource)) {
            throw new InvalidArgumentException(QueryStep.CREATING_PATH, entitySource.getName(), "Path of some property wrong");
        }
        final var totalRecords = countRecords(columnList, entitySource);
        int mTotalPages = 0;
        List<T> data = null;

        if (totalRecords > 0) {

            final var tupleData = getTupleData(columnList, page, size, entitySource);
            data = parseTupleDataToMapObject(tupleData, columnList, targetClass);
            mTotalPages = (int) Math.ceil((double) totalRecords / size);
        }

        return PageQueryResponse.
                <T>builder()
                .data(data)
                .totalPages(mTotalPages)
                .totalRecords(totalRecords)
                .pageNumber(page)
                .pageSize(size)
                .build();
    }

    private <T> List<T> parseTupleDataToMapObject(List<Tuple> resultList, List<Column> pColumnList, Class<T> targetClass) {
        final ArrayNode mJsonArrayNode = objectMapper.createArrayNode();
        resultList.forEach(t -> {
            final var mTempJsonNode = objectMapper.createObjectNode();
            pColumnList.forEach(c -> {
                switch (c.getValueType()) {
                    case NUMBER -> mTempJsonNode.put(c.getAlias(), t.get(c.getAlias(), Integer.class));
                    case STRING -> mTempJsonNode.put(c.getAlias(), t.get(c.getAlias(), String.class));
                    case DATE ->
                            mTempJsonNode.put(c.getAlias(), simpleDateFormat.format(t.get(c.getAlias(), Date.class)));
                    case BOOLEAN -> mTempJsonNode.put(c.getAlias(), t.get(c.getAlias(), Boolean.class));
                    default -> {
                        log.warn("No type for tuple");
                        log.debug("type searched: {} for alias: {} path property: {}", c.getValueType(), c.getAlias(), c.getPath());
                    }
                }
            });
            mJsonArrayNode.add(mTempJsonNode);
        });
        try {
            return objectMapper.readerForListOf(targetClass).readValue(mJsonArrayNode);
        } catch (Exception e) {
            log.error("Error parsing result to target class: {} , {}", targetClass.getName(), e.getMessage(), e);
            throw new RuntimeException("Failed to convert JSON to " + targetClass.getName() + " list", e);

        }
    }

    public List<Tuple> getTupleData(List<Column> pColumnList, int page, int size, Class pEntityClass) {
        final var mCriteriaBuilder = entityManager.getCriteriaBuilder();
        final var mTupleQuery = mCriteriaBuilder.createTupleQuery();
        final var mRoot = mTupleQuery.from(pEntityClass);
        final var firstRow = page > 0 ? page * size : 0;
        final var mMultipleSelectTupleQuery = buildMainQuery(mRoot, mCriteriaBuilder, pColumnList, mTupleQuery);
        final var mFitlerList = buildPredicateList(mRoot, mCriteriaBuilder, pColumnList);
        final var mOrderList = buildOrders(mRoot, mCriteriaBuilder, pColumnList);
        mMultipleSelectTupleQuery.where(mFitlerList.toArray(new Predicate[0])).orderBy(mOrderList);
        return entityManager.createQuery(mMultipleSelectTupleQuery)
                .setFirstResult(firstRow)
                .setMaxResults(size)
                .getResultList();

    }

    private List<Order> buildOrders(Root mRoot, CriteriaBuilder mCriteriaBuilder, List<Column> pColumnList) {
        final var mOrderList = new ArrayList<Order>();
        pColumnList.forEach(c -> {
            if (c.getIsSorted()) {
                final var path = getPropertyPath(mRoot, c.getPath());
                if (SortType.ASC == c.getSortType()) {
                    mOrderList.add(mCriteriaBuilder.asc(path));
                } else {
                    mOrderList.add(mCriteriaBuilder.desc(path));
                }
            }
        });
        return mOrderList;
    }

    private CriteriaQuery<Tuple> buildMainQuery(Root mRoot, CriteriaBuilder mCriteriaBuilder, List<Column> pColumnList, CriteriaQuery<Tuple> mTupleQuery) {
        final List<Selection<?>> mSelectionList = new ArrayList<>();
        pColumnList.forEach(c -> {
            final Path<?> path = getPropertyPath(mRoot, c.getPath());
            mSelectionList.add(path.alias(c.getAlias()));
        });
        return mTupleQuery.multiselect(mSelectionList);
    }

    private boolean validAllPaths(List<Column> columns, Class clazz) {
        log.info("@--> validating all properties");
        boolean hasAllPaths = true;
        for (var filter : columns) {
            if (!FieldsUtils.validIdClassHasField(clazz, filter.getPath())) {

                hasAllPaths = false;
                log.info("path : {} for class: {} no valid", filter.getPath(), clazz.getName());
            }
        }
        return hasAllPaths;
    }


    private Long countRecords(List<Column> pColumns, Class pClass) {
        final var mCriteriaBuilder = entityManager.getCriteriaBuilder();
        final var mCriteriaQueryLong = mCriteriaBuilder.createQuery(Long.class);
        final Root rootEntity = (Root) mCriteriaQueryLong.from(pClass);
        final var predicates = buildPredicateList(rootEntity, mCriteriaBuilder, pColumns);
        mCriteriaQueryLong.select(mCriteriaBuilder.count(rootEntity)).where(predicates.toArray(new Predicate[0]));
        return entityManager.createQuery(mCriteriaQueryLong).getSingleResult();
    }

    private List<Predicate> buildPredicateList(Root pRoot, CriteriaBuilder pCriteriaBuilder, List<Column> pColumnList) {
        final var mPredicatesList = new ArrayList<Predicate>();
        pColumnList.forEach(columnItem -> {
            if (columnItem.getIsFiltered()) {
                final var predicate = buildPredicate(pRoot, pCriteriaBuilder, columnItem);
                if (predicate != null) {
                    mPredicatesList.add(predicate);
                }
            }
        });
        return mPredicatesList;
    }

    private Predicate buildPredicate(Root pRoot, CriteriaBuilder pCriteriaBuilder, Column pColumn) {
        if (FieldsUtils.validIdClassHasField(pRoot.getModel().getJavaType(), pColumn.getPath())) {
            return switch (pColumn.getCriteriaFilter().getValueType()) {
                case DATE -> createDateFilter(pRoot, pCriteriaBuilder, pColumn);

                case NUMBER -> createNumberFilter(pRoot, pCriteriaBuilder, pColumn);

                case STRING -> createStringFilter(pRoot, pCriteriaBuilder, pColumn);

                case BOOLEAN -> createBooleanFilter(pRoot, pCriteriaBuilder, pColumn);
            };
        }
        return null;
    }

    private Predicate createStringFilter(Root pRootEntity, CriteriaBuilder pCriteriaBuilder, Column pColumn) {
        final var mStringFilter = (StringFilter) pColumn.getCriteriaFilter();
        final Path<String> mPath = (Path<String>) this.getPropertyPath(pRootEntity, pColumn.getPath());
        return switch (mStringFilter.getFilterType()) {
            case EQUALS -> pCriteriaBuilder.equal(mPath, mStringFilter.getValue());
            case LIKE -> pCriteriaBuilder.like(mPath, "%" + mStringFilter.getValue() + "%");
            case END_WITH -> pCriteriaBuilder.like(mPath, "%" + mStringFilter.getValue());
            case START_WITH -> pCriteriaBuilder.like(mPath, mStringFilter.getValue() + "%");
            case NOT_END -> pCriteriaBuilder.notLike(mPath, "%" + mStringFilter.getValue());
            case NOT_START -> pCriteriaBuilder.notLike(mPath, mStringFilter.getValue() + "%");
            case DIFFERENT -> pCriteriaBuilder.notEqual(mPath, mStringFilter.getValue());
            default -> {
                log.error("no search type for String filter: {} in field: {}", mStringFilter.getFilterType(), pColumn.getAlias());
                yield null;
            }
        };
    }

    private Predicate createBooleanFilter(Root pRootUserEntity,
                                          CriteriaBuilder pCriteriaBuilder, Column pColumnData) {
        //final FilterCriteria<Boolean>
        final var mBooleanFilter = (BooleanFilter) pColumnData.getCriteriaFilter();
        final Path<Boolean> mPath = (Path<Boolean>) getPropertyPath(pRootUserEntity, pColumnData.getPath());
        return switch (mBooleanFilter.getFilterType()) {
            case IS_TRUE -> pCriteriaBuilder.isTrue(mPath);
            case IS_FALSE -> pCriteriaBuilder.isFalse(mPath);
            default -> {
                log.error("no search type for Boolean filter: {} in field: {}", mBooleanFilter.getFilterType(), pColumnData.getAlias());
                yield null;
            }
        };

    }

    private Predicate createNumberFilter(Root pRootUserEntity,
                                         CriteriaBuilder pCriteriaBuilder, Column pColumnData) {

        final var mNumberFilter = (NumberFilter) pColumnData.getCriteriaFilter();
        final Path<Integer> mPath = (Path<Integer>) getPropertyPath(pRootUserEntity, pColumnData.getPath());
        return switch (mNumberFilter.getFilterType()) {
            case EQUALS -> pCriteriaBuilder.equal(mPath, mNumberFilter.getValue());
            case DIFFERENT -> pCriteriaBuilder.notEqual(mPath, mNumberFilter.getValue());
            case LESS_THAN -> pCriteriaBuilder.lessThan(mPath, mNumberFilter.getValue());
            case BIGGER_THAN -> pCriteriaBuilder.greaterThan(mPath, mNumberFilter.getValue());
            case LESS_OR_EQUALS -> pCriteriaBuilder.lessThanOrEqualTo(mPath, mNumberFilter.getValue());
            case BIGGER_OR_EQUALS -> pCriteriaBuilder.greaterThanOrEqualTo(mPath, mNumberFilter.getValue());
            default -> {
                log.error("Filter type not compatible with Number Filter field:{} , filterType: {}", pColumnData.getAlias(), pColumnData.getPath());
                yield null;
            }
        };

    }

    private Predicate createDateFilter(Root pRootUserEntity,
                                       CriteriaBuilder pCriteriaBuilder, Column pColumnData) {
        Predicate predicate = null;
        final DateFilter mFilterDate = (DateFilter) pColumnData.getCriteriaFilter();
        final Path<Date> mPath = (Path<Date>) getPropertyPath(pRootUserEntity, pColumnData.getPath());
        switch (mFilterDate.getFilterType()) {
            case EQUALS -> predicate = pCriteriaBuilder.equal(mPath, mFilterDate.getValue());
            case BETWEEN ->
                    predicate = pCriteriaBuilder.between(mPath, mFilterDate.getStartDate(), mFilterDate.getEndDate());
            case AFTER -> predicate = pCriteriaBuilder.greaterThan(mPath, mFilterDate.getValue());
            case BEFORE -> predicate = pCriteriaBuilder.lessThan(mPath, mFilterDate.getValue());
            case DIFFERENT -> predicate = pCriteriaBuilder.notEqual(mPath, mFilterDate.getValue());
            default -> log.warn("No Filter Type Found for Date Object in field: {}", pColumnData.getAlias());
        }
        return predicate;
    }

    private Path<?> getPropertyPath(Root pRootEntity, String pPathStr) {
        final var levels = pPathStr.split("\\.");
        Path<?> tempPath = pRootEntity;
        for (final var level : levels) {
            tempPath = tempPath.get(level);
        }
        return tempPath;
    }


}
