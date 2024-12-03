package com.spring.be.user.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class S3ResponseDto {
    private String url;

    public S3ResponseDto(String url){
        this.url = url;
    }
}
