package com.vf.sb_criteria_pager.user;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.vf.sbCriteriaPager.exception.InvalidArgumentException;
import org.vf.sbCriteriaPager.model.Column;
import org.vf.sbCriteriaPager.model.PageQueryResponse;

import java.util.List;

@RestController("/api/user")
public class UserController {
    private final UserService userService;

    public UserController(final UserService pUserService) {
        userService = pUserService;
    }


    @PostMapping("/page")
    public ResponseEntity<PageQueryResponse<UserResponseDTO>> page(@RequestParam(name = "page", defaultValue = "0") int page,
                                                                   @RequestParam(name = "size", defaultValue = "10") int size,
                                                                   @RequestBody List<Column> pColumns
    ) throws InvalidArgumentException {
        final var body = this.userService.pageUser(page, size, pColumns);
        return ResponseEntity.ok(body);
    }
}
