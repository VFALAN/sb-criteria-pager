package com.vf.sb_criteria_pager.user;

import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.vf.sbCriteriaPager.exception.InvalidArgumentException;
import org.vf.sbCriteriaPager.model.Column;
import org.vf.sbCriteriaPager.model.PageQueryResponse;
import org.vf.sbCriteriaPager.services.PageQueryService;

import java.util.List;

@Service
@Slf4j
public class UserService {

    private final EntityManager entityManager;
    private final PageQueryService pageQueryService;
    private final IUserRepository iUserRepository;

    public UserService(EntityManager pEntityManager, IUserRepository pIUserRepository) {
        entityManager = pEntityManager;
        iUserRepository = pIUserRepository;
        pageQueryService = new PageQueryService(entityManager);
    }

    public List<UserEntity> listAll() {
        return iUserRepository.findAll();
    }

    public PageQueryResponse<UserResponseDTO> pageUser(int page, int size, List<Column> pColumn) throws InvalidArgumentException {
        final PageQueryResponse<UserResponseDTO> response = pageQueryService.search(UserEntity.class,
                UserResponseDTO.class,
                page, size, pColumn);
        return response;
    }

}
