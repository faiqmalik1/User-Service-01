package com.voting.system.userservice.resources.resposne;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import resources.ResponseDTO;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class LoginResponseDTO extends ResponseDTO {

  private String userRole;

}