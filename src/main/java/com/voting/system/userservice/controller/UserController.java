package com.voting.system.userservice.controller;

import com.voting.system.userservice.jwt.JwtUtils;
import com.voting.system.userservice.resources.resposne.RoleListResponseDTO;
import com.voting.system.userservice.service.UserService;
import feign.Headers;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import resources.ResponseDTO;
import resources.user.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

  private final UserService userService;
  private final AuthenticationManager authenticationManager;
  private final JwtUtils jwtHelper;


  /**
   * retrieve all the list of roles
   *
   * @return :list of roles
   */
  @GetMapping("/roles")
  public RoleListResponseDTO retrieveRoles() {
    return userService.retrieveRoles();
  }

  /**
   * create the user
   *
   * @param userInviteDTO: contains the resources to create the user
   * @return :return success if creation successful else return failure reason
   */
  @PostMapping("")
  public ResponseDTO createUser(@CookieValue("Authorization") String token, @RequestBody UserInviteDTO userInviteDTO) {
    return userService.createUser(token, userInviteDTO);
  }

  /**
   * upload the user image
   *
   * @param token:to        validate user
   * @param image:multipart image
   * @return :return success message if image saved else throw failure exception
   */
  @PostMapping("/profile")
  public ResponseDTO uploadProfile(@CookieValue("Authorization") String token, @RequestParam("image") MultipartFile image) {
    return userService.setUserImage(token, image);
  }

  /**
   * retrieve user by id
   *
   * @param id:id of user
   * @return :return user response else return null
   */
  @GetMapping("/{id}")
  public UserResponseDTO getUser(@PathVariable("id") long id) {
    return userService.retrieveUser(id);
  }

  /**
   * retrieve all users who are voter
   *
   * @param pageable: to give custom size to pageable object
   * @return :return pageable object of voters
   */
  @GetMapping("/voters")
  public UserPageResponseDTO getAllVoters(Pageable pageable) {
    return userService.retrieveAllVoters(pageable);
  }

  /**
   * login user
   *
   * @param loginRequestDTO: contains the resources to login user
   * @return :if found return user if and roles else throw failure reason exception
   */
  @PutMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
  public String login(@RequestBody LoginRequestDTO loginRequestDTO) {
    return userService.loginUser(loginRequestDTO);
  }

  /**
   * retrieve all the users from the userId
   *
   * @param userListRequestDTO: contains the list of ids for users
   * @return :return the list of users
   */
  @PostMapping("/find/users")
  public UserListResponseDTO findUsers(@CookieValue("Authorization") String token, @RequestBody UserListRequestDTO userListRequestDTO) {
    return userService.findAllUsers(userListRequestDTO);
  }

  /**
   * checks if user exists or not
   *
   * @param userId: id for user
   * @return : return true if user found else false
   */
  @GetMapping("/user/{userId}")
  public boolean isUserExists(@PathVariable long userId) {
    return userService.isUserExists(userId);
  }

  /**
   * find user role
   *
   * @param userId: id of user
   * @return :role of user if found else return null
   */
  @GetMapping("/user/{userId}/role")
  public String userRole(@PathVariable long userId) {
    return userService.findUserRole(userId);
  }

  /**
   * login function for user which creates the jwt token and return token
   *
   * @param loginRequestDTO: contains the resources for login
   * @return : return the jwt token else return failure exception
   */
  @PostMapping(value = "/login")
  public LoginResponseDTO loginUser(@RequestBody LoginRequestDTO loginRequestDTO) {
    return userService.loginUserJwt(loginRequestDTO);
  }

  /**
   * function to validate the user token
   *
   * @param token: token to be validated containing user id
   * @return :return the user response containing the role, name and id of user if user found else return null
   */
  @GetMapping(value = "/validate")
  @Headers("Content-Type: application/json")
  public ValidateResponseDTO validateToken(@CookieValue("Authorization") String token) {
    return userService.validateToken(token);
  }

  /**
   * to update the user role
   *
   * @param token:    token of user to be validates
   * @param userId:   id of user whose role is to be updated
   * @param roleName: role name
   * @return : success response of role updated successfully else failure exception
   */
  @PostMapping(value = "/{userId}/role/{roleName}")
  public ResponseDTO updateUserRole(@CookieValue("Authorization") String token, @PathVariable long userId, @PathVariable String roleName) {
    return userService.updateUserRole(token, userId, roleName);
  }

  @PostMapping(value = "/email")
  public ResponseDTO sendEmail(@CookieValue("Authorization") String token, @RequestParam String emailBody) {
    return userService.sendEmail(token, emailBody);
  }

}