package com.devgang.marketduck.openapi.auth.dto;


import com.devgang.marketduck.auth.entity.VerifyNumEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class VerifyNumDto {

    private String email;

    private String verifyNum;

    private LocalDateTime createdAt;

    public VerifyNumDto(VerifyNumEntity entity) {
        this.email = entity.getEmail();
        this.verifyNum = entity.getVerifyNum();
        this.createdAt = entity.getCreatedAt();
    }

    public static VerifyNumDto of(VerifyNumEntity entity) {
        return new VerifyNumDto(entity);
    }

}
