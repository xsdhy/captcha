package com.xsdhy.captcha.service;

import com.xsdhy.captcha.utils.SecureUtils;
import com.xsdhy.captcha.vo.CaptchaVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Random;
import java.util.UUID;

/**
 * @author 唐川
 * @date 2022/2/18 9:58
 */
@Service
@Slf4j
public class CaptchaService {


    @Resource
    private FirstService firstService;

    @Resource
    private SecondService secondService;

    @Resource
    private ThreeService threeService;

    private String key = "asdhjkauiewrwkjhi32huisahfuisdhf23";


    public static void main(String[] args) {
        CaptchaService captchaService = new CaptchaService();
        CaptchaVO captcha = captchaService.captcha();
    }


    public CaptchaVO captcha() {
        int originImageNo = new Random().nextInt(10);
        InputStream originImageFileAsStream = this.getClass().getClassLoader().getResourceAsStream("static/template/" + originImageNo + ".jpg");

        try {
            BufferedImage originImage = ImageIO.read(originImageFileAsStream);
            CaptchaVO verifyImage = this.threeService.captcha(originImage);
            verifyImage.setBackgroundWidth(originImage.getWidth());
            verifyImage.setBackgroundHeight(originImage.getHeight());
            verifyImage.setTicket(this.generateTicket(verifyImage.getSlideBlockX()));

            return verifyImage;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    public String generateTicket(int x) {
        String ticket = String.format("%s.%s.%s", UUID.randomUUID(), System.currentTimeMillis(), x);
        return SecureUtils.encrypt(this.key, ticket);
    }

    public boolean verifyTicket(String ticket, Integer requestX) {
        String decrypt = SecureUtils.decrypt(this.key, ticket);
        if (null == decrypt) {
            return false;
        }
        //分隔明文
        String[] split = decrypt.split("\\.");
        if (3 != split.length) {
            return false;
        }


        long captchaTime = Long.parseLong(split[1]);
        int x = Integer.parseInt(split[2]);


        //校验有效期，有效期2分钟
        long nowTime = System.currentTimeMillis();
        if (nowTime - captchaTime > 120 * 1000) {
            return false;
        }

        //校验滑动位置是否正确
        return Math.abs(requestX - x) < 10;
    }

}
