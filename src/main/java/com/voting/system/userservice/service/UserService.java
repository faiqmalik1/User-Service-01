package com.voting.system.userservice.service;

import CustomException.CommonException;
import com.cloudinary.Cloudinary;
import com.voting.system.userservice.Utils.EmailSender;
import com.voting.system.userservice.Utils.PasswordGenerator;
import com.voting.system.userservice.feignController.CandidateController;
import com.voting.system.userservice.feignController.ConstituencyController;
import com.voting.system.userservice.feignController.VoterController;
import com.voting.system.userservice.jwt.JwtUtils;
import com.voting.system.userservice.model.Role;
import com.voting.system.userservice.model.User;
import com.voting.system.userservice.model.UserImage;
import com.voting.system.userservice.model.UserRole;
import com.voting.system.userservice.repository.RoleRepository;
import com.voting.system.userservice.repository.UserImageRepository;
import com.voting.system.userservice.repository.UserRepository;
import com.voting.system.userservice.repository.UserRoleRepository;
import com.voting.system.userservice.resources.resposne.*;
import constants.Constants;
import constants.ReturnMessage;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.multipart.MultipartFile;
import resources.BaseService;
import resources.ResponseDTO;
import resources.candidate.CandidateRequestDTO;
import resources.candidate.PartyResponseDTO;
import resources.constituency.ConstituencyResponseDTO;
import resources.user.*;
import resources.user.LoginResponseDTO;
import resources.voter.VoterRequestDTO;
import resources.voter.VoterResponseDTO;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

import static java.lang.Long.parseLong;

@Service
@RequiredArgsConstructor
public class UserService extends BaseService {

  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final UserRoleRepository userRoleRepository;
  private final UserImageRepository userImageRepository;
  private final VoterController voterController;
  private final ConstituencyController constituencyController;
  private final CandidateController candidateController;
  private final JwtUtils jwtUtils;
  private final PasswordEncoder passwordEncoder;
  private final EmailSender emailSender;
  private final Cloudinary cloudinary;


  /**
   * retrieve all the list of roles
   *
   * @return :list of roles
   */
  public RoleListResponseDTO retrieveRoles() {
    List<Role> roleList = roleRepository.findAll();
    return ModelToResponse.parseRoleResponseListToResponse(roleList);
  }

  /**
   * create the user
   *
   * @param userInviteDTO: contains the resources to create the user
   * @return :return success if creation successful else return failure reason
   */
  @Transactional
  public ResponseDTO createUser(String token, UserInviteDTO userInviteDTO) {

    Optional<Role> optionalRole = roleRepository.findByRoleNameIgnoreCase(userInviteDTO.getUserType());
    if (optionalRole.isEmpty()) {
      return generateFailureResponse(ReturnMessage.INVALID_ROLE.getValue());
    }
    Optional<User> optionalUser = userRepository.findByUserCNIC(userInviteDTO.getCNIC());
    if (optionalUser.isPresent()) {
      return generateFailureResponse(ReturnMessage.USER_CNIC_EXISTS.getValue());
    }
    User newUser = new User();
    newUser.setCreatedAt(new Date());
    newUser.setUserCNIC(userInviteDTO.getCNIC());
    newUser.setUserName(userInviteDTO.getName());
    newUser.setEmail(userInviteDTO.getEmail());
    String password = PasswordGenerator.generateRandomPassword();
    newUser.setPassword(passwordEncoder.encode(password));
    newUser = userRepository.save(newUser);
    Role verifiedRole = optionalRole.get();
    saveUserRole(verifiedRole, newUser);
    ConstituencyResponseDTO response = constituencyController.retrieveConstituencyByName(userInviteDTO.getHalka());
    if (response == null) {
      return generateFailureResponse(ReturnMessage.INVALID_HALKA_NAME.getValue());
    }
    VoterRequestDTO voterRequestDTO = new VoterRequestDTO();
    voterRequestDTO.setUserId(newUser.getUserId());
    voterRequestDTO.setConstituencyId(response.getConstituencyId());
    voterController.createVoter(voterRequestDTO);
    if (verifiedRole.getRoleName().equalsIgnoreCase(Constants.CANDIDATE)) {
      CandidateRequestDTO candidateRequestDTO = new CandidateRequestDTO();
      candidateRequestDTO.setUserId(newUser.getUserId());
      candidateRequestDTO.setPost("MNA");
      ConstituencyResponseDTO constituencyResponseDTO = constituencyController.retrieveConstituencyById(userInviteDTO.getHalkaId());
      if (constituencyResponseDTO == null) {
        return generateFailureResponse(ReturnMessage.INVALID_HALKA_NAME.getValue());
      }
      PartyResponseDTO partyResponseDTO = candidateController.retrieveParty(userInviteDTO.getPartyId());
      if (partyResponseDTO == null) {
        return generateFailureResponse(ReturnMessage.PARTY_NOT_EXISTS.getValue());
      }
      candidateRequestDTO.setConstituencyId(constituencyResponseDTO.getConstituencyId());
      candidateRequestDTO.setPartyId(partyResponseDTO.getPartyId());
      candidateController.createCandidateByAdmin(token, candidateRequestDTO);
    }
    sendEmail(newUser, userInviteDTO.getUserType(), userInviteDTO.getHalka(), password);
    return generateSuccessResponse();
  }

  /**
   * to save user role
   *
   * @param role: role of user
   * @param user: user reference object
   */
  private void saveUserRole(Role role, User user) {
    UserRole newUserRole = new UserRole();
    newUserRole.setUserId(user);
    newUserRole.setRole(role);
    userRoleRepository.save(newUserRole);
    if (role.getRoleName().equalsIgnoreCase(Constants.CANDIDATE)) {
      UserRole voterRole = new UserRole();
      voterRole.setUserId(user);
      voterRole.setRole(roleRepository.findByRoleNameIgnoreCase(Constants.VOTER).get());
      userRoleRepository.save(voterRole);
    }
  }

  /**
   * upload the user image
   *
   * @param token:to        validate user
   * @param image:multipart image
   * @return :return success message if image saved else throw failure exception
   */
  @Transactional
  public ResponseDTO setUserImage(String token, MultipartFile image) {
    String userid = jwtUtils.getUserIdFromToken(token);
    Optional<User> optionalUser = userRepository.findById(parseLong(userid));
    if (optionalUser.isEmpty()) {
      throw new CommonException(ReturnMessage.USER_NOT_EXISTS.getValue(), HttpStatus.BAD_REQUEST);
    }
    User verifiedUser = optionalUser.get();
    Optional<UserImage> optionalUserImage = userImageRepository.findByUser(verifiedUser);
    UserImage newImage = new UserImage();
    if (optionalUserImage.isPresent()) {
      newImage = optionalUserImage.get();
    }
    newImage.setUser(verifiedUser);
    try {
      newImage.setImage(saveImage(image));
    } catch (Exception ex) {
      throw new CommonException(ReturnMessage.IMAGE_UPLOAD_FAIL.getValue(), HttpStatus.BAD_REQUEST);
    }
    userImageRepository.save(newImage);
    return ModelToResponse.generateSccessResponse();
  }


  /**
   * retrieve user by id
   *
   * @param id:id of user
   * @return :return user response else return null
   */
  public UserResponseDTO retrieveUser(Long id) {
    Optional<User> optionalUser = userRepository.findById(id);
    String halka = "No Found";
    if (optionalUser.isEmpty()) {
      return null;
    }
    User verifiedUser = optionalUser.get();
    VoterResponseDTO voterResponseDTO = voterController.getVoterConstituency(verifiedUser.getUserId());
    if (voterResponseDTO != null) {
      ConstituencyResponseDTO constituencyResponseDTO = constituencyController.retrieveConstituencyById(voterResponseDTO.getConstitutionId());
      if (constituencyResponseDTO != null) {
        halka = constituencyResponseDTO.getHalkaName();
      }
    }
    return ModelToResponse.parseUserToUserResponse(cloudinary, verifiedUser, halka);
  }

  public UserPageResponseDTO retrieveAllUsers(Pageable page) {
    Page<User> userPage = userRepository.findAll(page);
    if (userPage.isEmpty()) {
      new UserPageResponseDTO(null);
    }
    Page<UserResponseDTO> userResponsePage = ModelToResponse.parseUserToUserPageResponse(cloudinary, userPage);
    return new UserPageResponseDTO(userResponsePage);
  }


  /**
   * retrieve all users who are voter
   *
   * @param page: to give custom size to pageable object
   * @return :return pageable object of voters
   */
  public UserPageResponseDTO retrieveAllVoters(Pageable page) {
    Page<User> userPage = userRepository.findAllByRoleName(Constants.VOTER, page);
    if (userPage.isEmpty()) {
      new UserPageResponseDTO(null);
    }
    Page<UserResponseDTO> userResponsePage = ModelToResponse.parseUserToUserPageResponse(cloudinary, userPage);
    return new UserPageResponseDTO(userResponsePage);
  }

  /**
   * login user
   *
   * @param loginRequestDTO: contains the resources to login user
   * @return :if found return user if and roles else throw failure reason exception
   */
  public String loginUser(LoginRequestDTO loginRequestDTO) {
    if (loginRequestDTO.getUserCNIC() != null && loginRequestDTO.getUserPassword() != null) {
      Optional<User> optionalUser = userRepository.findByUserCNIC(loginRequestDTO.getUserCNIC());
      if (optionalUser.isEmpty()) {
        throw new CommonException(ReturnMessage.USER_NOT_EXISTS.getValue(), HttpStatus.BAD_REQUEST);
      }
      User verifiedUser = optionalUser.get();
      if (!verifiedUser.getPassword().equals(loginRequestDTO.getUserPassword())) {
        throw new CommonException(ReturnMessage.INVALID_USER_NAME_AND_PASSWORD.getValue(), HttpStatus.BAD_REQUEST);
      }
      List<UserRole> userRoleOptional = userRoleRepository.findAllByUserIdUserId(verifiedUser.getUserId());
      if (userRoleOptional.isEmpty()) {
        return null;
      }
      List<String> distinctRoleNames = new ArrayList<>(userRoleOptional
              .stream()
              .map(userRole -> userRole.getRole().getRoleName())
              .distinct()
              .toList());
      distinctRoleNames.add(Long.toString(verifiedUser.getUserId()));
      return String.join(":", distinctRoleNames);
    }
    throw new CommonException(ReturnMessage.INVALID_USER_NAME_AND_PASSWORD.getValue(), HttpStatus.BAD_REQUEST);
  }

  /**
   * retrieve all the users from the userId
   *
   * @param userListRequestDTO: contains the list of ids for users
   * @return :return the list of users
   */
  public UserListResponseDTO findAllUsers(UserListRequestDTO userListRequestDTO) {
    List<UserResponseDTO> list = new ArrayList<>();
    for (User user : userRepository.findAllById(userListRequestDTO.getUserIdList())) {
      UserResponseDTO userResponseDTO = ModelToResponse.parseUserToUserResponse(cloudinary, user, null);
      list.add(userResponseDTO);
    }
    return new UserListResponseDTO(list);
  }

  /**
   * checks if user exists or not
   *
   * @param userId: id for user
   * @return : return true if user found else false
   */
  public boolean isUserExists(long userId) {
    return userRepository.findById(userId).isPresent();
  }

  /**
   * find user role
   *
   * @param userId: id of user
   * @return :role of user if found else return null
   */
  public String findUserRole(long userId) {
    List<UserRole> userRoleOptional = userRoleRepository.findAllByUserIdUserId(userId);
    if (userRoleOptional.isEmpty()) {
      return null;
    }
    List<String> distinctRoleNames = userRoleOptional
            .stream()
            .map(userRole -> userRole.getRole().getRoleName())
            .distinct()
            .toList();
    return String.join(":", distinctRoleNames);
  }

  /**
   * login function for user which creates the jwt token and return token
   *
   * @param loginRequestDTO: contains the resources for login
   * @return : return the jwt token else return failure exception
   */
  public LoginResponseDTO loginUserJwt(LoginRequestDTO loginRequestDTO) {
    if (userRoleRepository.findAll().isEmpty()) {
      generateAdminAndRoles();
    }
    LoginResponseDTO loginResponseDTO = new LoginResponseDTO();
    if (loginRequestDTO.getUserCNIC() != null && loginRequestDTO.getUserPassword() != null) {
      Optional<User> optionalUser = userRepository.findByUserCNIC(loginRequestDTO.getUserCNIC());
      if (optionalUser.isEmpty()) {
        loginResponseDTO.setErrorMessage(ReturnMessage.USER_NOT_EXISTS.getValue());
        loginResponseDTO.setResponseCode(Constants.FAILURE_CODE);
        return loginResponseDTO;
      }
      User verifiedUser = optionalUser.get();
      if (!passwordEncoder.matches(loginRequestDTO.getUserPassword(), verifiedUser.getPassword())) {
        loginResponseDTO.setErrorMessage(ReturnMessage.INVALID_USER_NAME_AND_PASSWORD.getValue());
        loginResponseDTO.setResponseCode(Constants.FAILURE_CODE);
        return loginResponseDTO;
      }
      List<UserRole> userRoleOptional = userRoleRepository.findAllByUserIdUserId(verifiedUser.getUserId());
      if (userRoleOptional.isEmpty()) {
        loginResponseDTO.setErrorMessage(ReturnMessage.USER_NOT_EXISTS.getValue());
        loginResponseDTO.setResponseCode(Constants.FAILURE_CODE);
        return loginResponseDTO;
      }
      loginResponseDTO.setToken(JwtUtils.generateJwtToken(verifiedUser));
      loginResponseDTO.setResponseCode(Constants.SUCCESS_CODE);
      return loginResponseDTO;
    }
    loginResponseDTO.setErrorMessage(ReturnMessage.INVALID_USER_NAME_AND_PASSWORD.getValue());
    loginResponseDTO.setResponseCode(Constants.FAILURE_CODE);
    return loginResponseDTO;
  }

  /**
   * function to validate the user token
   *
   * @param token: token to be validated containing user id
   * @return :return the user response containing the role, name and id of user if user found else return null
   */
  public ValidateResponseDTO validateToken(String token) {
    String[] tokens = token.split(",");
    token = tokens[0];
    String userId = this.jwtUtils.getUserIdFromToken(token);
    if (userId == null) {
      return null;
    }
    long user = parseLong(userId);
    Optional<User> optionalUser = userRepository.findById(user);
    User verifiedUser = optionalUser.get();
    ValidateResponseDTO validateResponseDTO = new ValidateResponseDTO();
    validateResponseDTO.setUserId(verifiedUser.getUserId());
    validateResponseDTO.setUserName(verifiedUser.getUsername());
    if (verifiedUser.getUserImage() != null && (verifiedUser.getUserImage().getImage() != null)) {
      validateResponseDTO.setProfile(getImageFromCloud(verifiedUser.getUserImage().getImage()));
    } else {
      validateResponseDTO.setProfile(null);
    }
    validateResponseDTO.setUserRole(getHighestPriorityRole(verifiedUser.getUserRoles()));
    return validateResponseDTO;
  }

  public String getHighestPriorityRole(Set<UserRole> userRoles) {
    boolean hasCandidate = false;
    boolean hasVoter = false;
    boolean hasAdmin = false;
    for (UserRole userRole : userRoles) {
      String roleName = userRole.getRole().getRoleName();
      if (roleName.equalsIgnoreCase(Constants.CANDIDATE)) {
        hasCandidate = true;
      } else if (roleName.equalsIgnoreCase(Constants.VOTER)) {
        hasVoter = true;
      } else if (roleName.equalsIgnoreCase(Constants.ADMIN)) {
        hasAdmin = true;
      }
    }
    if (hasCandidate) {
      return Constants.CANDIDATE;
    } else if (hasVoter && hasAdmin) {
      return Constants.ADMIN;
    } else if (hasVoter) {
      return Constants.VOTER;
    } else if (hasAdmin) {
      return Constants.ADMIN;
    }
    return Constants.VOTER;
  }


  /**
   * to update the user role
   *
   * @param token:    token of user to be validates
   * @param userId:   id of user whose role is to be updated
   * @param roleName: role name
   * @return : success response of role updated successfully else failure exception
   */
  public ResponseDTO updateUserRole(String token, long userId, String roleName) {
    Optional<User> optionalUser = userRepository.findById(userId);
    if (optionalUser.isEmpty()) {
      throw new CommonException(ReturnMessage.USER_NOT_EXISTS.getValue(), HttpStatus.BAD_REQUEST);
    }
    Optional<Role> optionalRole = roleRepository.findByRoleNameIgnoreCase(roleName);
    if (optionalRole.isEmpty()) {
      throw new CommonException(ReturnMessage.INVALID_ROLE.getValue(), HttpStatus.BAD_REQUEST);
    }
    UserRole userRole = new UserRole();
    userRole.setRole(optionalRole.get());
    userRole.setUserId(optionalUser.get());
    userRoleRepository.save(userRole);
    return generateSuccessResponse();
  }

  private void sendEmail(User user, String role, String halka, String password) {
    String body = "Greetings " + user.getUserName() + "\nA very warm welcome to Voting System. You are invited as " + role + "\nFollowing are your credentials for logIn\n CNIC: " + user.getUserCNIC() + "\nPassword: " + password +"\n You can login from here: http://18.212.158.163:3000/ui/login "+ "\n\nRegards\nVoting Team";
    SimpleMailMessage message = new SimpleMailMessage();
    message.setTo(user.getEmail());
    message.setSubject("Invitation From Voting Service!!");
    message.setText(body);
    emailSender.getJavaMailSender().send(message);
  }

  public ResponseDTO sendEmail(String token, String emailBody) {
    ValidateResponseDTO validateResponseDTO = validateToken(token);
    Optional<User> optionalUser = userRepository.findById(validateResponseDTO.getUserId());
    if (optionalUser.isEmpty()) {
      return generateFailureResponse(ReturnMessage.USER_NOT_EXISTS.getValue());
    }
    SimpleMailMessage message = new SimpleMailMessage();
    message.setTo(optionalUser.get().getEmail());
    message.setSubject("Thank You For Casting Vote");
    message.setText(emailBody);
    emailSender.getJavaMailSender().send(message);
    return generateSuccessResponse();
  }

  @Transactional
  public ResponseDTO generateAdminAndRoles() {
    Role role = new Role();
    role.setRoleName(Constants.VOTER);
    roleRepository.save(role);
    Role role2 = new Role();
    role2.setRoleName(Constants.ADMIN);
    role2 = roleRepository.save(role2);
    Role role3 = new Role();
    role3.setRoleName(Constants.CANDIDATE);
    roleRepository.save(role3);

    User user = new User();
    user.setUserName("Faiq Malik");
    user.setPassword(passwordEncoder.encode(Constants.DEFAULT_CODE));
    user.setUserCNIC("35202-4074847-5");
    user.setEmail("faiq.ijaz@devsinc.com");
    user.setCreatedAt(new Date());
    user = userRepository.save(user);
    UserRole userRole = new UserRole();
    userRole.setUserId(user);
    userRole.setRole(role2);
    userRoleRepository.save(userRole);

    candidateController.createParty();
    ConstituencyResponseDTO constituencyResponseDTO = constituencyController.createConstituency();
    VoterRequestDTO voterRequestDTO = new VoterRequestDTO();
    voterRequestDTO.setConstituencyId(constituencyResponseDTO.getConstituencyId());
    voterRequestDTO.setUserId(user.getUserId());
    voterController.createVoter(voterRequestDTO);
    return generateSuccessResponse();
  }

  public String saveImage(MultipartFile image) throws IOException {
    Map<String, String> uploadResult = cloudinary.uploader().upload(image.getBytes(), Map.of());
    return uploadResult.get("public_id");
  }

  public byte[] getImageFromCloud(String publicID) {

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