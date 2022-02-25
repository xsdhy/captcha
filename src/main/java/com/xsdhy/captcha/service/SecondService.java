package com.xsdhy.captcha.service;

import com.xsdhy.captcha.utils.ImageUtils;
import com.xsdhy.captcha.vo.CaptchaVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.axis.encoding.Base64;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author 唐川
 * @date 2022/2/22 14:28
 */
@Service
@Slf4j
public class SecondService {

    /**
     * 源文件宽度
     */
    private static int ORI_WIDTH = 680;
    /**
     * 源文件高度
     */
    private static int ORI_HEIGHT = 390;
    /**
     * 模板图宽度
     */
    private static int CUT_WIDTH = 100;
    /**
     * 模板图高度
     */
    private static int CUT_HEIGHT = 100;
    /**
     * 抠图凸起圆心
     */
    private static int circleR = 10;
    /**
     * 抠图内部矩形填充大小
     */
    private static int RECTANGLE_PADDING = 15;
    /**
     * 抠图的边框宽度
     */
    private static int SLIDER_IMG_OUT_PADDING = 2;



    public CaptchaVO captcha(BufferedImage srcImage) throws IOException {

        int locationX = CUT_WIDTH + new Random().nextInt(srcImage.getWidth() - CUT_WIDTH * 3);
        int locationY = CUT_HEIGHT + new Random().nextInt(srcImage.getHeight() - CUT_HEIGHT) / 2;



        //创建一个切块,TYPE_4BYTE_ABGR表示具有8位RGBA颜色分量的图像
        BufferedImage markImage = new BufferedImage(CUT_WIDTH, CUT_HEIGHT, BufferedImage.TYPE_4BYTE_ABGR);
        int[][] data = getBlockData();

        cutImgByTemplate(srcImage, markImage, data, locationX, locationY);




        CaptchaVO captchaVO = new CaptchaVO();
        captchaVO.setBackgroundImage(ImageUtils.imageToBase64(srcImage));
        captchaVO.setSlideBlockImage(ImageUtils.imageToBase64(markImage));
        captchaVO.setSlideBlockX(locationX);
        captchaVO.setSlideBlockY(locationY);
        captchaVO.setSlideBlockWidth(CUT_WIDTH);
        captchaVO.setSlideBlockHeight(CUT_HEIGHT);

        return captchaVO;
    }

    /**
     * 生成随机滑块形状
     * <p>
     * 0 透明像素
     * 1 滑块像素
     * 2 阴影像素
     *
     * @return int[][]
     */
    private int[][] getBlockData() {
        int[][] data = new int[CUT_WIDTH][CUT_HEIGHT];
        Random random = new Random();
        //(x-a)²+(y-b)²=r²
        //x中心位置左右5像素随机
        double x1 = RECTANGLE_PADDING + (CUT_WIDTH - 2 * RECTANGLE_PADDING) / 2.0 - 5 + random.nextInt(10);
        //y 矩形上边界半径-1像素移动
        double y1_top = RECTANGLE_PADDING - random.nextInt(3);
        double y1_bottom = CUT_HEIGHT - RECTANGLE_PADDING + random.nextInt(3);
        double y1 = random.nextInt(2) == 1 ? y1_top : y1_bottom;


        double x2_right = CUT_WIDTH - RECTANGLE_PADDING - circleR + random.nextInt(2 * circleR - 4);
        double x2_left = RECTANGLE_PADDING + circleR - 2 - random.nextInt(2 * circleR - 4);
        double x2 = random.nextInt(2) == 1 ? x2_right : x2_left;
        double y2 = RECTANGLE_PADDING + (CUT_HEIGHT - 2 * RECTANGLE_PADDING) / 2.0 - 4 + random.nextInt(10);

        double po = Math.pow(circleR, 2);
        for (int i = 0; i < CUT_WIDTH; i++) {
            for (int j = 0; j < CUT_HEIGHT; j++) {
                //矩形区域
                boolean fill;
                if ((i >= RECTANGLE_PADDING && i < CUT_WIDTH - RECTANGLE_PADDING)
                        && (j >= RECTANGLE_PADDING && j < CUT_HEIGHT - RECTANGLE_PADDING)) {
                    data[i][j] = 1;
                    fill = true;
                } else {
                    data[i][j] = 0;
                    fill = false;
                }
                //凸出区域
                double d3 = Math.pow(i - x1, 2) + Math.pow(j - y1, 2);
                if (d3 < po) {
                    data[i][j] = 1;
                } else {
                    if (!fill) {
                        data[i][j] = 0;
                    }
                }
                //凹进区域
                double d4 = Math.pow(i - x2, 2) + Math.pow(j - y2, 2);
                if (d4 < po) {
                    data[i][j] = 0;
                }
            }
        }
        //边界阴影
        for (int i = 0; i < CUT_WIDTH; i++) {
            for (int j = 0; j < CUT_HEIGHT; j++) {
                //四个正方形边角处理
                for (int k = 1; k <= SLIDER_IMG_OUT_PADDING; k++) {
                    //左上、右上
                    if (i >= RECTANGLE_PADDING - k && i < RECTANGLE_PADDING
                            && ((j >= RECTANGLE_PADDING - k && j < RECTANGLE_PADDING)
                            || (j >= CUT_HEIGHT - RECTANGLE_PADDING - k && j < CUT_HEIGHT - RECTANGLE_PADDING + 1))) {
                        data[i][j] = 2;
                    }

                    //左下、右下
                    if (i >= CUT_WIDTH - RECTANGLE_PADDING + k - 1 && i < CUT_WIDTH - RECTANGLE_PADDING + 1) {
                        for (int n = 1; n <= SLIDER_IMG_OUT_PADDING; n++) {
                            if (((j >= RECTANGLE_PADDING - n && j < RECTANGLE_PADDING)
                                    || (j >= CUT_HEIGHT - RECTANGLE_PADDING - n && j <= CUT_HEIGHT - RECTANGLE_PADDING))) {
                                data[i][j] = 2;
                            }
                        }
                    }
                }

                if (data[i][j] == 1 && j - SLIDER_IMG_OUT_PADDING > 0 && data[i][j - SLIDER_IMG_OUT_PADDING] == 0) {
                    data[i][j - SLIDER_IMG_OUT_PADDING] = 2;
                }
                if (data[i][j] == 1 && j + SLIDER_IMG_OUT_PADDING > 0 && j + SLIDER_IMG_OUT_PADDING < CUT_HEIGHT && data[i][j + SLIDER_IMG_OUT_PADDING] == 0) {
                    data[i][j + SLIDER_IMG_OUT_PADDING] = 2;
                }
                if (data[i][j] == 1 && i - SLIDER_IMG_OUT_PADDING > 0 && data[i - SLIDER_IMG_OUT_PADDING][j] == 0) {
                    data[i - SLIDER_IMG_OUT_PADDING][j] = 2;
                }
                if (data[i][j] == 1 && i + SLIDER_IMG_OUT_PADDING > 0 && i + SLIDER_IMG_OUT_PADDING < CUT_WIDTH && data[i + SLIDER_IMG_OUT_PADDING][j] == 0) {
                    data[i + SLIDER_IMG_OUT_PADDING][j] = 2;
                }
            }
        }
        return data;
    }

    /**
     * 裁剪区块
     * 根据生成的滑块形状，对原图和裁剪块进行变色处理
     *
     * @param oriImage    原图
     * @param targetImage 裁剪图
     * @param blockImage  滑块
     * @param x           裁剪点x
     * @param y           裁剪点y
     * https://www.cnblogs.com/xpvincent/archive/2012/12/17/2821665.html
     */
    private void cutImgByTemplate(BufferedImage oriImage, BufferedImage targetImage, int[][] blockImage, int x, int y) {
        for (int i = 0; i < CUT_WIDTH; i++) {
            for (int j = 0; j < CUT_HEIGHT; j++) {
                int _x = x + i;
                int _y = y + j;
                int rgbFlg = blockImage[i][j];
                int rgb_ori = oriImage.getRGB(_x, _y);
                // 原图中对应位置变色处理
                if (rgbFlg == 1) {
                    //抠图上复制对应颜色值
                    targetImage.setRGB(i, j, rgb_ori);

                    //原图对应位置颜色变化
                    oriImage.setRGB(_x, _y, Color.LIGHT_GRAY.getRGB());

                    Color color = new Color(0, 0, 0, 211);
//                    oriImage.setRGB(_x, _y, color.getRGB());


//                    int r = (0xff & rgb_ori);
//                    int g = (0xff & (rgb_ori >> 8));
//                    int b = (0xff & (rgb_ori >> 16));
//                    rgb_ori = r + (g << 8) + (b << 16) + (140 << 24);
//                    oriImage.setRGB(_x, _y, rgb_ori);


                    int a = (rgb_ori >> 24) & 0xff;
                    int r = (rgb_ori >> 16) & 0xff;
                    int g = (rgb_ori >> 8) & 0xff;
                    int b = rgb_ori & 0xff;

                    int avg = (r + g + b) / 4;

                    int p = (a << 24) | (avg << 16) | (avg << 8) | avg;
//                    oriImage.setRGB(_x, _y, p);

                } else if (rgbFlg == 2) {
                    //设置边框区域
                    targetImage.setRGB(i, j, Color.WHITE.getRGB());
                    oriImage.setRGB(_x, _y, Color.GRAY.getRGB());
                } else if (rgbFlg == 0) {
                    //int alpha = 0;
                    targetImage.setRGB(i, j, rgb_ori & 0x00ffffff);
                }
            }

        }
    }



}
