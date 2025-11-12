package com.careflow.dtos.auth;

import com.careflow.models.User;
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
    public UserDto(User user){
        this.email = user.getEmail();
        this.fullName = user.getFullName();
        this.id = user.getId();
        this.role = user.getRole().getName();
    }
}
