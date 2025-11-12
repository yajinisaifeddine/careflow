package com.careflow.dtos.auth;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class UserDto {
   private Long id;
   private String fullName;
   private String email;
   private String role;
}
