package com.pay.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description
 * @author: scj
 * @Date: 2019/6/25 14:36
 */
@RestController
@RequestMapping( value = "/pay" , name = "支付")
public class PayController {

    @RequestMapping(value = "/")
    public String pay(String pay){
        return pay;
    }

}
