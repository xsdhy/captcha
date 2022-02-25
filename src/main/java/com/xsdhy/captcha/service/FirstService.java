package com.xsdhy.captcha.service;


import com.xsdhy.captcha.been.CoordinateInt;
import com.xsdhy.captcha.utils.ImageUtils;
import com.xsdhy.captcha.vo.CaptchaVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.axis.encoding.Base64;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Random;

/**
 * @author 唐川
 * @date 2022/2/22 14:28
 */
@Service
@Slf4j
public class FirstService {


    public CaptchaVO captcha(BufferedImage originImage) {


        InputStream templateImageFileAsStream = this.getClass().getClassLoader().getResourceAsStream("static/template.png");
        InputStream borderImageFileAsStream = this.getClass().getClassLoader().getResourceAsStream("static/border.png");


        try {
            BufferedImage borderImage = ImageIO.read(borderImageFileAsStream);
            BufferedImage templateImage = ImageIO.read(templateImageFileAsStream);

            //获取原图感兴趣区域坐标
            CoordinateInt coordinateInt = generateCutoutCoordinates(originImage.getWidth(), originImage.getHeight(), templateImage.getWidth(), templateImage.getHeight());

            //  根据原图生成切块图(矩形)
            BufferedImage interestArea = originImage.getSubimage(coordinateInt.getX(), coordinateInt.getY(),
                    templateImage.getWidth(), templateImage.getHeight());

            //根据模板图尺寸创建一张透明图片
            BufferedImage cutoutImage = new BufferedImage(templateImage.getWidth(), templateImage.getHeight(), templateImage.getType());
            //根据模板图片切图
            cutoutImageByTemplateImage(interestArea, templateImage, cutoutImage);


            //  原图生成遮罩
            BufferedImage shadeImage = generateShadeByTemplateImage(originImage, templateImage, coordinateInt.getX(), coordinateInt.getY());


            //把边框图加到切片图上
            //没有处理的特别好，看看这篇文章再优化：https://blog.csdn.net/weixin_39911056/article/details/114249468
            //BufferedImage png = cutoutImageEdge(cutoutImage, borderImage);




            CaptchaVO captchaVO = new CaptchaVO();
            captchaVO.setBackgroundImage(ImageUtils.imageToBase64(shadeImage));
            captchaVO.setSlideBlockImage(ImageUtils.imageToBase64(cutoutImage));

            captchaVO.setSlideBlockX(coordinateInt.getX());
            captchaVO.setSlideBlockY(coordinateInt.getY());
            captchaVO.setSlideBlockWidth(borderImage.getWidth());
            captchaVO.setSlideBlockHeight(borderImage.getHeight());

            return captchaVO;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 生成切图的坐标
     *
     * @param oW
     * @param oH
     * @param tW
     * @param tH
     * @return
     */
    public CoordinateInt generateCutoutCoordinates(int oW, int oH, int tW, int tH) {
        CoordinateInt coordinateInt = new CoordinateInt();

        Random random = new Random(System.currentTimeMillis());

        //  取范围内坐标数据，坐标抠图一定要落在原图中，否则会导致程序错误
        coordinateInt.setX(random.nextInt(oW - tW) % (oW - tW - tW + 1) + tW);
        coordinateInt.setY(random.nextInt(oH - tW) % (oH - tW - tW + 1) + tW);
        if (tH - oH >= 0) {
            coordinateInt.setY(random.nextInt(10));
        }
        return coordinateInt;
    }


    /**
     * 根据模板图生成遮罩图
     *
     * @param originImage   源图
     * @param templateImage 模板图
     * @param x             感兴趣区域X轴
     * @param y             感兴趣区域Y轴
     * @return 遮罩图
     * @throws IOException 数据转换异常
     */
    private BufferedImage generateShadeByTemplateImage(BufferedImage originImage, BufferedImage templateImage, int x, int y) throws IOException {
        //  根据原图，创建支持alpha通道的rgb图片
//        BufferedImage shadeImage = new BufferedImage(originImage.getWidth(), originImage.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
        BufferedImage shadeImage = new BufferedImage(originImage.getWidth(), originImage.getHeight(), BufferedImage.TYPE_INT_ARGB);

        //  原图片矩阵
        int[][] originImageMatrix = getMatrix(originImage);
        //  模板图片矩阵
        int[][] templateImageMatrix = getMatrix(templateImage);

        //  将原图的像素拷贝到遮罩图
        for (int i = 0; i < originImageMatrix.length; i++) {
            for (int j = 0; j < originImageMatrix[0].length; j++) {
                int rgb = originImage.getRGB(i, j);
                //  获取rgb色度
                int r = (0xff & rgb);
                int g = (0xff & (rgb >> 8));
                int b = (0xff & (rgb >> 16));
                //  无透明处理
                rgb = r + (g << 8) + (b << 16) + (255 << 24);
                shadeImage.setRGB(i, j, rgb);
            }
        }

        //  对遮罩图根据模板像素进行处理
        for (int i = 0; i < templateImageMatrix.length; i++) {
            for (int j = 0; j < templateImageMatrix[0].length; j++) {
                int rgb = templateImage.getRGB(i, j);

                //对源文件备份图像(x+i,y+j)坐标点进行透明处理
                if (rgb != 16777215 && rgb < 0) {
                    int rgb_ori = shadeImage.getRGB(x + i, y + j);
                    int r = (0xff & rgb_ori);
                    int g = (0xff & (rgb_ori >> 8));
                    int b = (0xff & (rgb_ori >> 16));


                    //45,40,26

                    rgb_ori = r + (g << 8) + (b << 16) + (140 << 24);
                    //  对遮罩透明处理
                    shadeImage.setRGB(x + i, y + j, rgb_ori);
                }
            }
        }
        return shadeImage;
    }

    /**
     * 根据模板图抠图
     *
     * @param interestArea  感兴趣区域图
     * @param templateImage 模板图
     * @param cutoutImage   裁剪图
     * @return 裁剪图
     */
    private BufferedImage cutoutImageByTemplateImage(BufferedImage interestArea, BufferedImage templateImage, BufferedImage cutoutImage) {
        //  获取兴趣区域图片矩阵
        int[][] interestAreaMatrix = getMatrix(interestArea);
        //  获取模板图片矩阵
        int[][] templateImageMatrix = getMatrix(templateImage);

        //  将模板图非透明像素设置到剪切图中
        for (int i = 0; i < templateImageMatrix.length; i++) {
            for (int j = 0; j < templateImageMatrix[0].length; j++) {
                int rgb = templateImageMatrix[i][j];
                if (rgb != 16777215 && rgb < 0) {
                    cutoutImage.setRGB(i, j, interestArea.getRGB(i, j));
                }
            }
        }

        return cutoutImage;
    }


    /**
     * 切块图描边
     *
     * @param cutoutImage 图片容器
     * @param borderImage 描边图
     * @return 图片容器
     */
    public BufferedImage cutoutImageEdge(BufferedImage cutoutImage, BufferedImage borderImage) throws Exception {
        try {

            //  获取模板边框矩阵， 并进行颜色处理
            int[][] borderImageMatrix = getMatrix(cutoutImage);
            for (int i = 0; i < borderImageMatrix.length; i++) {
                for (int j = 0; j < borderImageMatrix[0].length; j++) {
                    int rgb = borderImage.getRGB(i, j);
                    if (rgb < 0) {
                        cutoutImage.setRGB(i, j, -7237488);
                    }
                }
            }

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ImageIO.write(cutoutImage, "png", byteArrayOutputStream);
            return cutoutImage;
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new Exception(e);
        }
    }


    /**
     * 图片生成图像矩阵
     *
     * @param bufferedImage 图片源
     * @return 图片矩阵
     */
    private int[][] getMatrix(BufferedImage bufferedImage) {
        int[][] matrix = new int[bufferedImage.getWidth()][bufferedImage.getHeight()];
        for (int i = 0; i < bufferedImage.getWidth(); i++) {
            for (int j = 0; j < bufferedImage.getHeight(); j++) {
                matrix[i][j] = bufferedImage.getRGB(i, j);
            }
        }
        return matrix;
    }
}
