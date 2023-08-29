package com.voting.system.userservice.feignController;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import resources.ResponseDTO;
import resources.candidate.*;

@FeignClient("CANDIDATE-SERVICE")
public interface CandidateController {

  /**
   * returns the list of parties available
   *
   * @param detail: to get parties detail including party image and creation date
   * @return :list of parties
   */
  @GetMapping("/candidate/party")
  public PartyListResponseDTO retrieveAllParties(@RequestParam(value = "detail", required = true) boolean detail);

  /**
   * to register the user as candidate and to update its role
   *
   * @param token             : to validate user request
   * @param candidateRequest: json containing data for creating candidate
   * @param image:            image of party if a user want to create its own party
   * @return :success response if every thing goes fine else failure response
   */
  @PostMapping("/candidate/register")
  public ResponseDTO registerUserAsCandidate(@CookieValue("Authorization") String token, @RequestParam(value = "candidate") String candidateRequest, @RequestParam(value = "image", required = false) MultipartFile image);

  /**
   * it will retrieve candidate with its details
   *
   * @param token:       token to validate request
   * @param candidateId: id of candidate
   * @return :return candidate response if candidate present else return null
   */
  @GetMapping("/candidate/{candidateId}")
  public CandidateResponseDTO retrieveCandidateDetail(@CookieValue("Authorization") String token, @PathVariable(name = "candidateId") long candidateId);

  @GetMapping("/candidate/party-management/party-management")
  public String showPartyManagementPage(Model model);

  /**
   * to retrieve the basic info of candidate
   *
   * @param candidateId: id of candidate
   * @return :return the candidate basic response else return null
   */
  @GetMapping("/candidate/{candidateId}/get")
  public CandidateBasicResponseDTO retrieveCandidate(@PathVariable(name = "candidateId") long candidateId);


  @GetMapping("/candidate/party/get")
  public PartyResponseDTO retrieveParty(@RequestParam long partyId);

  @PostMapping("/candidate/register/admin")
  public ResponseDTO createCandidateByAdmin(@CookieValue("Authorization") String token, @RequestBody CandidateRequestDTO candidateRequestDTO);

  @PostMapping("/candidate/party/create")
  public ResponseDTO createParty();

}