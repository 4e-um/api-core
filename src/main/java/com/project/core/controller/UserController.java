package com.project.core.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.core.controller.dto.Request.FindUserRequest;
import com.project.core.infra.entity.user.User;
import com.project.core.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
  private final UserService userService;

  @GetMapping
  public Long save(@RequestBody FindUserRequest request) {
      User user = userService.loadByContactEnc(request.contactEnc());
      return user.getUserId();
  }
  
}
