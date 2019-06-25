package com.pay.service.impl;

import java.io.InputStream;
import java.util.*;

/**
 * @Description
 * @author: scj
 * @Date: 2019/6/25 18:01
 */
public class PayServiceImpl {

    /**
     * 微信-APP支付
     *
     * @param content
     * @return
     * @throws Exception
     */
    @SuppressWarnings({ "static-access" })
    public static byte[] weixin_app_pay(String content) throws Exception {
        try {
            if (Util.debugLog.isDebugEnabled()) {
                Util.debugLog.debug("微信支付参数：content=" + content);
            }

            if (content == null || "".equals(content)) {
                return ReturnUtils.jsonReturnToString("0", "content不合法", "");
            }

            String order_price = ""; // 支付金额
            String body = ""; // 商品名称
            String out_trade_no = ""; // 订单号
            String serviceType = "";//	1：服务2:商品,3:家庭护士
            String CREATE_IP = ""; // 生成订单的ip地址
            try {
                JSONObject contentjson = JSONObject.fromObject(content);
                if (!contentjson.containsKey("price") || StringUtils.isEmpty(contentjson.getString("price"))) {
                    return ReturnUtils.jsonReturnToString("0", "参数不完整", "");
                }
                order_price = contentjson.getString("price");
                if (Util.debugLog.isDebugEnabled()) {
                    Util.debugLog.debug("微信支付参数：price=" + order_price);
                }
                if (!contentjson.containsKey("body") || StringUtils.isEmpty(contentjson.getString("body"))) {
                    return ReturnUtils.jsonReturnToString("0", "参数不完整", "");
                }
                body = contentjson.getString("body");
                if (Util.debugLog.isDebugEnabled()) {
                    Util.debugLog.debug("微信支付参数：body=" + body);
                }
                if (!contentjson.containsKey("out_trade_no")
                        || StringUtils.isEmpty(contentjson.getString("out_trade_no"))) {
                    return ReturnUtils.jsonReturnToString("0", "参数不完整", "");
                }
                out_trade_no = contentjson.getString("out_trade_no");
                if (!contentjson.containsKey("CREATE_IP") || StringUtils.isEmpty(contentjson.getString("CREATE_IP"))) {
                    return ReturnUtils.jsonReturnToString("0", "参数不完整", "");
                }
                if (!contentjson.containsKey("serviceType") || StringUtils.isEmpty(contentjson.getString("serviceType"))) {
                    return ReturnUtils.jsonReturnToString("0", "参数不完整", "");
                }
                serviceType = contentjson.getString("serviceType");
                if (Util.debugLog.isDebugEnabled()) {
                    Util.debugLog.debug("微信支付参数：serviceType=" + serviceType);
                }
                CREATE_IP = contentjson.getString("CREATE_IP");
                if (Util.debugLog.isDebugEnabled()) {
                    Util.debugLog.debug("微信支付参数：out_trade_no=" + out_trade_no);
                }
            } catch (Exception e1) {
                if (Util.debugLog.isDebugEnabled()) {
                    Util.debugLog.debug("微信支付异常：e1=" + e1);
                }
                return ReturnUtils.jsonReturnToString("0", "content不合法", "");
            }

            String APP_ID = "";
            String MCH_ID = "";
            String API_KEY = "";
            String NOTIFY_URL = "";
            String APP_SECRET = "";
            String UFDODER_URL = "";
            String nonce_str = "";
            String trade_type = "APP";// 请求二维码url
            try {
                // 读取配置文件类
                Properties pps = new Properties();
                InputStream in = PropertiesUtil.class.getResourceAsStream("/config/wechatConfig.properties");
                pps.load(in);
                // 商户平台APPID
                APP_ID = pps.getProperty("APP_ID_APP");// appid
                // 公众号key秘钥
                // String appsecret = PayConfigUtil.APP_SECRET; // appsecret
                // 商户号
                MCH_ID = pps.getProperty("MCH_ID_APP"); //
                // 商户key秘钥
                API_KEY = pps.getProperty("API_KEY_APP"); // key
                // APP_SECRET
                APP_SECRET = pps.getProperty("APP_SECRET"); // key
                // 异步回调地址
                NOTIFY_URL = pps.getProperty("NOTIFY_URL_APP");
                // 微信
                UFDODER_URL = pps.getProperty("UFDODER_URL_APP");

                String currTime = PayCommonUtil.getCurrTime();
                String strTime = currTime.substring(8, currTime.length());
                String strRandom = PayCommonUtil.buildRandom(4) + "";
                nonce_str = strTime + strRandom;

                if (Util.debugLog.isDebugEnabled()) {
                    Util.debugLog.debug("微信支付，获取账户配置信息：APP_ID=" + APP_ID + ",MCH_ID=" + MCH_ID + ",API_KEY=" + API_KEY
                            + ",CREATE_IP=" + CREATE_IP + ",CREATE_IP=" + CREATE_IP + ",NOTIFY_URL=" + NOTIFY_URL
                            + ",UFDODER_URL=" + UFDODER_URL + ",nonce_str=" + nonce_str);
                }

                if (APP_ID == null || "".equals(APP_ID) || MCH_ID == null || "".equals(MCH_ID) || API_KEY == null
                        || "".equals(API_KEY) || CREATE_IP == null || "".equals(CREATE_IP) || NOTIFY_URL == null
                        || "".equals(NOTIFY_URL) || UFDODER_URL == null || "".equals(UFDODER_URL)) {
                    return ReturnUtils.jsonReturnToString("0", "获取配置文件信息失败！", "");
                }
            } catch (Exception e) {
                if (Util.debugLog.isDebugEnabled()) {
                    Util.debugLog.debug("微信支付，获取账户配置信息：e=" + e);
                }
                return ReturnUtils.jsonReturnToString("0", "获取账户配置信息失败！", "");
            }

            try {
                JSONObject attach_obj = new JSONObject();
                attach_obj.put("payType", serviceType);//	支付类型1：服务；2：商品

                SortedMap<Object, Object> packageParams = new TreeMap<Object, Object>();
                packageParams.put("appid", APP_ID);
                packageParams.put("mch_id", MCH_ID);
                packageParams.put("nonce_str", nonce_str);
                packageParams.put("attach", attach_obj.toString());
                packageParams.put("body", body);
                packageParams.put("out_trade_no", out_trade_no);
                packageParams.put("total_fee", order_price);
                packageParams.put("spbill_create_ip", CREATE_IP);
                packageParams.put("notify_url", NOTIFY_URL);
                packageParams.put("trade_type", trade_type);
                RequestHandler reqHandler = new RequestHandler(null, null);
                reqHandler.init(APP_ID, APP_SECRET, API_KEY);
                String sign = reqHandler.createSign(packageParams);
                packageParams.put("sign", sign);
                String allParameters = "";
                try {
                    allParameters = reqHandler.genPackage(packageParams);
                } catch (Exception e) {
                    if (Util.debugLog.isDebugEnabled()) {
                        Util.debugLog.debug("微信支付，生成签名异常：e=" + e + ";allParameters=" + allParameters);
                    }
                    return ReturnUtils.jsonReturnToString("0", "生成签名失败！", "");
                }
                String prepay_id = "";// 预支付订单返回结果
                try {
                    prepay_id = new GetWxOrderno().getPayNo(UFDODER_URL, XMLUtil.parseXML(packageParams));
                    if (prepay_id.equals("")) {
                        if (Util.debugLog.isDebugEnabled()) {
                            Util.debugLog.debug("统一支付接口获取预支付订单出错：prepay_id=" + prepay_id);
                        }
                        return ReturnUtils.jsonReturnToString("0", "统一支付接口获取预支付订单出错1！", "");
                    }
                } catch (Exception e1) {
                    if (Util.debugLog.isDebugEnabled()) {
                        Util.debugLog.debug("统一支付接口获取预支付订单出错：prepay_id=" + prepay_id + ";e1" + e1);
                    }
                    return ReturnUtils.jsonReturnToString("0", "统一支付接口获取预支付订单出错2！", "");
                }
                // 获取到prepayid后对以下字段进行签名最终发送给app
                SortedMap<Object, Object> finalpackage = new TreeMap<Object, Object>();
                String timestamp = TenpayUtil.getTimeStamp();
                finalpackage.put("appid", APP_ID);
                finalpackage.put("timestamp", timestamp);
                finalpackage.put("noncestr", nonce_str);
                finalpackage.put("partnerid", MCH_ID);
                finalpackage.put("package", "Sign=WXPay");
                finalpackage.put("prepayid", prepay_id);
                String finalsign = reqHandler.createSign(finalpackage);
                Map<String, String> map = new HashMap<String, String>();// 使用Map集合
                map.put("appId", APP_ID);
                map.put("timeStamp", timestamp);
                map.put("nonceStr", nonce_str);
                map.put("package", "Sign=WXPay");
                map.put("signType", "MD5");
                map.put("total", out_trade_no);
                map.put("sign", finalsign);
                map.put("prepayid", prepay_id);
                if (Util.debugLog.isDebugEnabled()) {
                    Util.debugLog.debug("生成签名结果;map=" + new JSONObject().fromObject(map));
                }
                return ReturnUtils.jsonReturnToString("1", "完成！", new JSONObject().fromObject(map).toString());
            } catch (Exception e) {
                if (Util.debugLog.isDebugEnabled()) {
                    Util.debugLog.debug("微信支付，生成url失败-异常：e=" + e);
                }
                return ReturnUtils.jsonReturnToString("0", "生成url失败！", "");
            }
        } catch (Exception e5) {
            if (Util.debugLog.isDebugEnabled()) {
                Util.debugLog.debug("微信支付，生成url失败-异常：e=5" + e5);
            }
        }
        return ReturnUtils.jsonReturnToString("0", "异常失败", "");
    }

}
