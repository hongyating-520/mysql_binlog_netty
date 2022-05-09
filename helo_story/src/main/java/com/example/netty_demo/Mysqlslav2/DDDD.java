package com.example.netty_demo.Mysqlslav2;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.function.Function;
import java.util.function.Predicate;

/*
 * @author ZZQ
 * @Date 2022/4/29 5:44 下午
 */
public class DDDD {

    public static void main(String[] args) {


    String type = "appin";
        String 条件 = (String)OpteunEnum.getApp(type).options.apply("条件");
        System.out.println(条件);

    }
    enum OpteunEnum{
        APP_IN("appin", (Function) o -> "String no in")
        ,
        APP_PAnt("appin", (Function) o -> "String pan end ")
        ;
        private String type;
        private Function options;

        OpteunEnum(String appin, Function function) {
        }
        public static OpteunEnum getApp(String type){
            for (OpteunEnum value : OpteunEnum.values()) {
                if (value.type.equals(type)){
                    return value;
                }
            }
            return null;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public Function getOptions() {
            return options;
        }

        public void setOptions(Function options) {
            this.options = options;
        }
    }
}
