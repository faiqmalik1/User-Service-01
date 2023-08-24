package com.voting.system.userservice.resources.resposne;

import com.voting.system.userservice.model.Role;
import com.voting.system.userservice.model.User;
import constants.Constants;
import org.springframework.data.domain.Page;
import resources.ResponseDTO;
import resources.user.RoleResponseDTO;
import resources.user.UserResponseDTO;

import java.util.ArrayList;
import java.util.List;

public class ModelToResponse {

  public static RoleResponseDTO parseRoleToRoleResponse(Role role) {
    RoleResponseDTO newResponse = new RoleResponseDTO();
    newResponse.setRoleId(role.getRoleId());
    newResponse.setRoleName(role.getRoleName());
    return newResponse;
  }

  public static RoleListResponseDTO parseRoleResponseListToResponse(List<Role> roleResponseList) {
    List<RoleResponseDTO> roleResponsDTOS = new ArrayList<>();
    for (Role role : roleResponseList) {
      roleResponsDTOS.add(parseRoleToRoleResponse(role));
    }
    return new RoleListResponseDTO(roleResponsDTOS);
  }

  public static UserResponseDTO parseUserToUserResponse(User user, String halka) {
    UserResponseDTO userResponseDTO = new UserResponseDTO();
    userResponseDTO.setName(user.getUserName());
    userResponseDTO.setCNIC(user.getUserCNIC());
    userResponseDTO.setId(user.getUserId());
    userResponseDTO.setEmail(user.getEmail());
    userResponseDTO.setHalka(halka);
    if (user.getUserImage() != null) {
      userResponseDTO.setImg(user.getUserImage().getImage());
    }
    userResponseDTO.setResponseCode(Constants.SUCCESS_CODE);
    return userResponseDTO;
  }

  public static Page<UserResponseDTO> parseUserToUserPageResponse(Page<User> users) {
    return users.map(user -> {
      return ModelToResponse.parseUserToUserResponse(user, null);
    });
  }

  public static ResponseDTO generateSccessResponse() {
    ResponseDTO responseDTO = new ResponseDTO();
    responseDTO.setResponseCode(Constants.SUCCESS_CODE);
    responseDTO.setErrorMessage("");
    return responseDTO;
  }

  public static ResponseDTO generateLoginSccessResponse(String role) {
    LoginResponseDTO response = new LoginResponseDTO();
    response.setUserRole(role);
    response.setResponseCode(Constants.SUCCESS_CODE);
    return response;
  }

  public static ResponseDTO generateFailureResponse(String errorMessage) {
    ResponseDTO responseDTO = new ResponseDTO();
    responseDTO.setResponseCode(Constants.FAILURE_CODE);
    responseDTO.setErrorMessage(errorMessage);
    return responseDTO;
  }

}