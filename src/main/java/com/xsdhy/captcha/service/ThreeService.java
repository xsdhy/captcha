package com.xsdhy.captcha.service;

import com.xsdhy.captcha.been.CoordinateInt;
import com.xsdhy.captcha.utils.ImageUtils;
import com.xsdhy.captcha.vo.CaptchaVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.axis.encoding.Base64;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Random;

/**
 * @author 唐川
 * @date 2022/2/22 14:28
 */
@Service
@Slf4j
public class ThreeService {

    /**
     * 抠图凸起圆心
     */
    private static int circleR = 22;
    /**
     * 抠图内部矩形填充大小
     */
    private static int RECTANGLE_PADDING = 32;
    /**
     * 抠图的边框宽度
     */
    private static int SLIDER_IMG_OUT_PADDING = 4;


    /**
     * 切图在原图的比例
     */
    private static double CUT_IN_SRC_SCALE = 2.78;




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

    public CaptchaVO captcha(BufferedImage srcImage) throws IOException {
        CaptchaVO captchaVO = new CaptchaVO();

        //计算|设置【滑块】的尺寸
        captchaVO.setSlideBlockHeight((int) Math.ceil(srcImage.getHeight()/CUT_IN_SRC_SCALE));
        captchaVO.setSlideBlockWidth(captchaVO.getSlideBlockHeight());

        //计算出一组【滑块】在【背景图】中的坐标
        CoordinateInt coordinateInt = this.generateCutoutCoordinates(srcImage.getWidth(), srcImage.getHeight(), captchaVO.getSlideBlockWidth(), captchaVO.getSlideBlockHeight());
        captchaVO.setSlideBlockX(coordinateInt.getX());
        captchaVO.setSlideBlockY(coordinateInt.getY());



        //创建一个切块,TYPE_4BYTE_ABGR表示具有8位RGBA颜色分量的图像
        BufferedImage markImage = new BufferedImage(captchaVO.getSlideBlockWidth(), captchaVO.getSlideBlockHeight(), BufferedImage.TYPE_4BYTE_ABGR);
        int[][] data = getBlockData(captchaVO.getSlideBlockWidth(), captchaVO.getSlideBlockHeight());



        cutImgByTemplate(srcImage, markImage, data, coordinateInt.getX(), coordinateInt.getY(),captchaVO.getSlideBlockWidth(), captchaVO.getSlideBlockHeight());
/*

        //底图
        Graphics2D bgG2 = (Graphics2D)srcImage.getGraphics();
        //创建黑色遮罩层
        BufferedImage cover = new BufferedImage(captchaVO.getSlideBlockWidth(), captchaVO.getSlideBlockHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D coverG2 = (Graphics2D)cover.getGraphics();
        coverG2.setColor(Color.BLACK);
        coverG2.fillRect(0,0, captchaVO.getSlideBlockWidth(), CUT_HEIGHT);
        coverG2.dispose();

        //开启透明度
        bgG2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 0.5f));
        //描绘
        bgG2.drawImage(cover, coordinateInt.getX(), coordinateInt.getY(), captchaVO.getSlideBlockWidth(), captchaVO.getSlideBlockHeight(), null);
        //结束透明度
        bgG2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
        bgG2.dispose();
*/

        captchaVO.setBackgroundImage(ImageUtils.imageToBase64(srcImage));
        captchaVO.setSlideBlockImage(ImageUtils.imageToBase64(markImage));
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
    private int[][] getBlockData(int cutWidth,int cutHeight) {
        int[][] data = new int[cutWidth][cutHeight];
        Random random = new Random();
        //(x-a)²+(y-b)²=r²
        //x中心位置左右5像素随机
        double x1 = RECTANGLE_PADDING + (cutWidth - 2 * RECTANGLE_PADDING) / 2.0 - 5 + random.nextInt(10);
        //y 矩形上边界半径-1像素移动
        double y1Top = RECTANGLE_PADDING - random.nextInt(3);
        double y1Bottom = cutHeight - RECTANGLE_PADDING + random.nextInt(3);
        double y1 = random.nextInt(2) == 1 ? y1Top : y1Bottom;


        double x2Right = cutWidth - RECTANGLE_PADDING - circleR + random.nextInt(2 * circleR - 4);
        double x2Left = RECTANGLE_PADDING + circleR - 2 - random.nextInt(2 * circleR - 4);
        double x2 = random.nextInt(2) == 1 ? x2Right : x2Left;
        double y2 = RECTANGLE_PADDING + (cutHeight - 2 * RECTANGLE_PADDING) / 2.0 - 4 + random.nextInt(10);

        double po = Math.pow(circleR, 2);
        for (int i = 0; i < cutWidth; i++) {
            for (int j = 0; j < cutHeight; j++) {
                //矩形区域
                boolean fill;
                if ((i >= RECTANGLE_PADDING && i < cutWidth - RECTANGLE_PADDING)
                        && (j >= RECTANGLE_PADDING && j < cutHeight - RECTANGLE_PADDING)) {
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
        for (int i = 0; i < cutWidth; i++) {
            for (int j = 0; j < cutHeight; j++) {
                //四个正方形边角处理
                for (int k = 1; k <= SLIDER_IMG_OUT_PADDING; k++) {
                    //左上、右上
                    if (i >= RECTANGLE_PADDING - k && i < RECTANGLE_PADDING
                            && ((j >= RECTANGLE_PADDING - k && j < RECTANGLE_PADDING)
                            || (j >= cutHeight - RECTANGLE_PADDING - k && j < cutHeight - RECTANGLE_PADDING + 1))) {
                        data[i][j] = 2;
                    }

                    //左下、右下
                    if (i >= cutWidth - RECTANGLE_PADDING + k - 1 && i < cutWidth - RECTANGLE_PADDING + 1) {
                        for (int n = 1; n <= SLIDER_IMG_OUT_PADDING; n++) {
                            if (((j >= RECTANGLE_PADDING - n && j < RECTANGLE_PADDING)
                                    || (j >= cutHeight - RECTANGLE_PADDING - n && j <= cutHeight - RECTANGLE_PADDING))) {
                                data[i][j] = 2;
                            }
                        }
                    }
                }

                if (data[i][j] == 1 && j - SLIDER_IMG_OUT_PADDING > 0 && data[i][j - SLIDER_IMG_OUT_PADDING] == 0) {
                    data[i][j - SLIDER_IMG_OUT_PADDING] = 2;
                }
                if (data[i][j] == 1 && j + SLIDER_IMG_OUT_PADDING > 0 && j + SLIDER_IMG_OUT_PADDING < cutHeight && data[i][j + SLIDER_IMG_OUT_PADDING] == 0) {
                    data[i][j + SLIDER_IMG_OUT_PADDING] = 2;
                }
                if (data[i][j] == 1 && i - SLIDER_IMG_OUT_PADDING > 0 && data[i - SLIDER_IMG_OUT_PADDING][j] == 0) {
                    data[i - SLIDER_IMG_OUT_PADDING][j] = 2;
                }
                if (data[i][j] == 1 && i + SLIDER_IMG_OUT_PADDING > 0 && i + SLIDER_IMG_OUT_PADDING < cutWidth && data[i + SLIDER_IMG_OUT_PADDING][j] == 0) {
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
     */
    private void cutImgByTemplate(BufferedImage oriImage, BufferedImage targetImage, int[][] blockImage, int x, int y,int cutWidth,int cutHeight) {
        for (int i = 0; i < cutWidth; i++) {
            for (int j = 0; j < cutHeight; j++) {
                int _x = x + i;
                int _y = y + j;
                int rgbFlg = blockImage[i][j];
                int rgb_ori = oriImage.getRGB(_x, _y);
                // 原图中对应位置变色处理
                if (rgbFlg == 1) {
                    //抠图上复制对应颜色值
                    targetImage.setRGB(i, j, rgb_ori);

                    //原图对应位置颜色变化
//                    oriImage.setRGB(_x, _y, Color.LIGHT_GRAY.getRGB());

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
                    oriImage.setRGB(_x, _y, p);

                } else if (rgbFlg == 2) {
                    //设置边框区域

                    Color color = new Color(0, 0, 0, 211);
//                    targetImage.setRGB(i, j,color.getRGB());
//                    oriImage.setRGB(_x, _y, color.getRGB());
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
