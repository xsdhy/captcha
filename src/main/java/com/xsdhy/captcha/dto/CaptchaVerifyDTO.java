package com.xsdhy.captcha.dto;

import lombok.Data;

/**
 * @author 唐川
 * @date 2022/2/23 14:42
 */
@Data
public class CaptchaVerifyDTO {

    private String ticket;
    private Integer blockX;
    private String moveTrack;
}
