package org.example.hatealcohol.user.service;

import lombok.RequiredArgsConstructor;
import org.example.hatealcohol.user.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
}
