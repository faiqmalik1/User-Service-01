package com.voting.system.userservice.resources.resposne;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import resources.ResponseDTO;
import resources.user.RoleResponseDTO;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Setter
public class RoleListResponseDTO extends ResponseDTO {

  private List<RoleResponseDTO> roleResponseDTOList;

}