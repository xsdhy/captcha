package com.xsdhy.captcha.been;

import lombok.Data;

/**
 * @author 唐川
 * @date 2022/2/18 16:28
 */
@Data
public class CoordinateInt {

    private int x;
    private int y;

    public CoordinateInt(){

    }

    public CoordinateInt(int x, int y){
        this.x=x;
        this.y=y;
    }
}
