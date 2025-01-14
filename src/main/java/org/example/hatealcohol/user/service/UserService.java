package org.example.hatealcohol.user.service;

import lombok.RequiredArgsConstructor;
import org.example.hatealcohol.user.entity.User;
import org.example.hatealcohol.user.exception.UserNotFoundException;
import org.example.hatealcohol.user.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;

  public User getUserInfo(String userId) {

    Long parseUserId = Long.parseLong(userId);

    User foundUser = userRepository.findById(parseUserId)
        .orElseThrow(() -> new UserNotFoundException("사용자가 조회되지 않습니다."));

    return foundUser;
  }
}
