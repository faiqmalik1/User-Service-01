package com.voting.system.userservice.feignController;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import resources.constituency.ConstituencyListResponseDTO;
import resources.constituency.ConstituencyResponseDTO;

@FeignClient(name = "CONSTITUENCY-SERVICE")
public interface ConstituencyController {

  /**
   * to retrieve constituency by name
   *
   * @param halkaName: name of constituency
   * @return :return constituency if found else return null
   */
  @GetMapping("/constituency/name/{halkaName}")
  public ConstituencyResponseDTO retrieveConstituencyByName(@PathVariable(name = "halkaName") String halkaName);

  /**
   * to retrieve constituency by id
   *
   * @param constituencyId:id of constituency
   * @return :return constituency if found else return null
   */
  @GetMapping("/constituency/id/{constituencyId}")
  public ConstituencyResponseDTO retrieveConstituencyById(@PathVariable(name = "constituencyId") long constituencyId);

  /**
   * to retrieve all the constituencies
   *
   * @return :return the list of available constituencies if found else return null
   */
  @GetMapping("/constituency")
  public ConstituencyListResponseDTO retrieveConstituencies();


}