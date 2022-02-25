package com.xsdhy.captcha.controller;

import com.xsdhy.captcha.common.Resp;
import com.xsdhy.captcha.service.CaptchaService;
import com.xsdhy.captcha.vo.CaptchaResultVO;
import com.xsdhy.captcha.vo.CaptchaVO;
import com.xsdhy.captcha.dto.CaptchaVerifyDTO;
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
    public Resp<CaptchaVO> getCaptcha() {
        return Resp.ok(this.captchaService.captcha());
    }

    @PostMapping("/captcha/verify")
    public Resp<Void> verifyCaptcha(@RequestBody CaptchaVerifyDTO captchaVerifyDTO) {
        if (this.captchaService.verifyTicket(captchaVerifyDTO)) {
            return Resp.ok();
        } else {
            return Resp.error("验证失败");
        }
    }
}
