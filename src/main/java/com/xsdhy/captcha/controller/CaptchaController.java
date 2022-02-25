package com.xsdhy.captcha.controller;

import com.xsdhy.captcha.service.CaptchaService;
import com.xsdhy.captcha.vo.CaptchaResultVO;
import com.xsdhy.captcha.vo.CaptchaVO;
import com.xsdhy.captcha.vo.CaptchaVerifyDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author 唐川
 * @date 2022/2/23 15:45
 */


@RestController
@Slf4j
public class CaptchaController {


    @Resource
    private CaptchaService captchaService;


    @GetMapping("/captcha")
    public CaptchaVO getCaptcha(){
        return this.captchaService.captcha();
    }

    @PostMapping("/captcha/verify")
    public CaptchaResultVO verifyCaptcha(@RequestBody CaptchaVerifyDTO captchaVerifyDTO){
        CaptchaResultVO captchaResultVO = new CaptchaResultVO();
        if (this.captchaService.verifyTicket(captchaVerifyDTO.getTicket(),captchaVerifyDTO.getBlockX())){
            captchaResultVO.setCode(2000);
        }else {
            captchaResultVO.setCode(5000);
        }
        return captchaResultVO;
    }
}
