package com.xsdhy.captcha.vo;

import lombok.Data;

/**
 * @author 唐川
 * @date 2022/2/22 15:07
 */
@Data
public class CaptchaVO {

    private Integer backgroundWidth;
    private Integer backgroundHeight;
    private String backgroundImage;

    private String slideBlockImage;
    private Integer slideBlockWidth;
    private Integer slideBlockHeight;

    private Integer slideBlockX;
    private Integer slideBlockY;
    private String ticket;
}
