package org.seckill.enums;

/**
 * 使用枚举表述我们的常量数据字段 不希望那些常量在程序中有很多
 * @author: sjswh
 * @date: 2019-08-18 17:26:41
 * @description:
 **/
public enum SeckillStatEnum {

    SUCCESS(1, "秒杀成功"),
    END(0, "秒杀结束"),
    REPEAT_KILL(-1, "重复秒杀"),
    INNER_ERROR(-2, "系统异常"),
    DATA_REWRITE(-3, "数据篡改");

    private int state;

    private String stateInfo;

    SeckillStatEnum(int state, String stateInfo) {
        this.state = state;
        this.stateInfo = stateInfo;
    }

    //生成get方法
    public int getState() {
        return state;
    }


    public String getStateInfo() {
        return stateInfo;
    }


    public static SeckillStatEnum stateOf(int index){
        for(SeckillStatEnum state: values()){
            if(state.getState() == index){
                return state;
            }
        }
        return null;
    }

}


