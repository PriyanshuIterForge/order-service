package com.example.ct_order_demo.controller;

import com.commercetools.api.client.ProjectApiRoot;
import com.commercetools.api.models.customer.CustomerSignInResult;
import com.example.ct_order_demo.model.LoginRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.ct_order_demo.security.JwtService;

@RestController
@RequestMapping("/login")
public class LoginController {
    private final ProjectApiRoot apiRoot;
    private final JwtService jwtService;

    public LoginController(ProjectApiRoot apiRoot, JwtService jwtService) {
        this.apiRoot = apiRoot;
        this.jwtService = jwtService;
    }

    @PostMapping
    public String login(@RequestBody LoginRequest request){

        CustomerSignInResult result = apiRoot.login()
                .post(builder -> builder.email(request.email)
                        .password(request.password))
                .executeBlocking()
                .getBody();

        String customerId = result.getCustomer().getId();

        return jwtService.generateToken(customerId);
    }
}