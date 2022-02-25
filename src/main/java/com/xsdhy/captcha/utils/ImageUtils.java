package com.xsdhy.captcha.utils;

import org.apache.axis.encoding.Base64;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;

/**
 * @author 唐川
 * @date 2022/2/24 10:13
 */
public class ImageUtils {

    /**
     * 将IMG输出为文件
     *
     * @param image
     * @param file
     * @throws Exception
     */
    public static void imageToFile(BufferedImage image, String file) {
        try {
            byte[] imagedata = null;
            ByteArrayOutputStream bao = new ByteArrayOutputStream();
            ImageIO.write(image, "png", bao);
            imagedata = bao.toByteArray();
            FileOutputStream out = null;
            out = new FileOutputStream(new File(file));
            out.write(imagedata);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 将图片转换为BASE64
     *
     * @param image
     * @return
     * @throws IOException
     */
    public static String imageToBase64(BufferedImage image) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(image, "png", out);
        return Base64.encode(out.toByteArray());
    }
}
