package com.voting.system.userservice.feignController;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import resources.ResponseDTO;
import resources.voter.VoterRequestDTO;
import resources.voter.VoterResponseDTO;

@FeignClient(name = "VOTER-SERVICE")
public interface VoterController {

  /**
   * to create the voter
   *
   * @param voterRequestDTO: contains the user id and constituency id
   * @return : return success if created successfully else return failure exception
   */
  @PostMapping("/voter")
  public ResponseDTO createVoter(@RequestBody VoterRequestDTO voterRequestDTO);

  /**
   * check if voter is registered in constituency or not
   *
   * @param token:   to validate request
   * @param voterId: voter id
   * @return : return the voter response if voter is present else return null
   */
  @GetMapping("voter/{voterId}")
  public VoterResponseDTO isVoterRegisteredInConstituency(@CookieValue("Authorization") String token, @PathVariable(name = "voterId") long voterId);

  /**
   * get the constituency of voter
   *
   * @param voterId: if of voter
   * @return :return the voter response if found else return null
   */
  @GetMapping("voter/{voterId}/constituency")
  public VoterResponseDTO getVoterConstituency(@PathVariable(name = "voterId") long voterId);

}
