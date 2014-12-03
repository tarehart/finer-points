package com.nodestand.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.dao.SystemWideSaltSource;
import org.springframework.security.authentication.encoding.Md5PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class PasswordEncoderService {
    @Autowired
    Md5PasswordEncoder passwordEncoder;

    @Autowired
    SystemWideSaltSource saltSource;

    public String encodePassword(String password) {
        return passwordEncoder.encodePassword(password, saltSource.getSystemWideSalt());
    }
}
