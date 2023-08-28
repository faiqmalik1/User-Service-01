package com.voting.system.userservice.resources.resposne;

import com.cloudinary.Cloudinary;
import com.voting.system.userservice.model.Role;
import com.voting.system.userservice.model.User;
import com.voting.system.userservice.service.UserService;
import constants.Constants;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import resources.ResponseDTO;
import resources.user.RoleResponseDTO;
import resources.user.UserResponseDTO;

import java.io.InputStream;
import java.net.URL;
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

  public static UserResponseDTO parseUserToUserResponse(Cloudinary cloudinary, User user, String halka) {
    UserResponseDTO userResponseDTO = new UserResponseDTO();
    userResponseDTO.setName(user.getUserName());
    userResponseDTO.setCNIC(user.getUserCNIC());
    userResponseDTO.setId(user.getUserId());
    userResponseDTO.setEmail(user.getEmail());
    userResponseDTO.setHalka(halka);
    if (user.getUserImage() != null) {
      userResponseDTO.setImg(getImageFromCloud(cloudinary, user.getUserImage().getImage()));
    }
    userResponseDTO.setResponseCode(Constants.SUCCESS_CODE);
    return userResponseDTO;
  }

  public static Page<UserResponseDTO> parseUserToUserPageResponse(Cloudinary cloudinary, Page<User> users) {
    return users.map(user -> {
      return ModelToResponse.parseUserToUserResponse(cloudinary, user, null);
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

  public static byte[] getImageFromCloud(Cloudinary cloudinary, String publicID) {

    String cloudUrl = cloudinary.url()
            .publicId(publicID)
            .generate();
    try {
      URL url = new URL(cloudUrl);
      InputStream inputStream = url.openStream();
      byte[] out = IOUtils.toByteArray(inputStream);
      ByteArrayResource resource = new ByteArrayResource(out);
      return resource.getByteArray();

    } catch (Exception ex) {
      return null;
    }
  }

}