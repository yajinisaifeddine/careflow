package com.careflow.dtos.auth;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OAuthResponse {

    private String token;
    private String email;
    private String userName;

}
