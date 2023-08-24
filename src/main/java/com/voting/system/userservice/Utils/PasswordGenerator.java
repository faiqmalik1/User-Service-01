package com.voting.system.userservice.Utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.Random;

@Configuration
public class PasswordGenerator {

  public static String generateRandomPassword() {
    String allowedChars = "0123456789ABCDEFGHIJKLAMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    int passwordLength = 7;

    StringBuilder password = new StringBuilder();

    Random random = new Random();
    for (int i = 0; i < passwordLength; i++) {
      int index = random.nextInt(allowedChars.length());
      char randomChar = allowedChars.charAt(index);
      password.append(randomChar);
    }
    return password.toString();
  }
}
