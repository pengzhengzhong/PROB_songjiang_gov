package com.enesource.jump.web.action;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.enesource.jump.web.enums.ENUM_LOGIN_TYPE;
import com.enesource.jump.web.enums.ENUM_USER_LEVEL;
import com.enesource.jump.web.utils.*;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.gson.JsonObject;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import com.alibaba.fastjson.JSONObject;
import com.enesource.jump.web.annotation.JwtToken;
import com.enesource.jump.web.common.BaseAction;
import com.enesource.jump.web.common.Result;
import com.enesource.jump.web.dao.ICommonMapper;
import com.enesource.jump.web.dao.IGovMapper;
import com.enesource.jump.web.dao.ITagMapper;
import com.enesource.jump.web.dto.TagDTO;
import com.enesource.jump.web.redis.IRedisService;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.captcha.generator.RandomGenerator;

@Controller
@CrossOrigin
public class MainAction extends BaseAction {

    @Autowired
    ICommonMapper commonMapper;

    @Autowired
    ITagMapper tagMapper;

    @Autowired
    IGovMapper govMapper;

    @Autowired
    private IRedisService redisService;

    @Autowired
    private RestTemplate restTemplate;


    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    @ResponseBody
    public Result login(@RequestBody Map<String, Object> paramMap) {
        Result result = new Result();
        result.setCode(Conf.ERROR);
        String loginType = StringUtil.getString(paramMap.get("loginType"));
        Map<String, Object> checkUser = Maps.newHashMap();
        if (ENUM_LOGIN_TYPE.PASSWORD_VALID_LOGIN.getCode().equals(loginType)) {
            String[] checkParamsMap = {"userName", "password", "webUUID", "verifyCode"};
            String errorString = checkParams(paramMap, checkParamsMap, ERROR_STRING_MAP);
            if (null != errorString) {
                result.setMsg(errorString);
                return result;
            }
            String webUUID = paramMap.get("webUUID").toString();
            String verifyCode = paramMap.get("verifyCode").toString();
            // ???????????????????????????
            if (redisService.exists(webUUID)) {
                if (!verifyCode.equals(redisService.get(webUUID))) {
                    result.setMsg("???????????????????????????????????????");
                    // TODO ??????????????????
                    redisService.del(webUUID);
                    return result;
                }
            } else {
                result.setMsg("???????????????????????????????????????");
                // TODO ??????????????????
                redisService.del(webUUID);
                return result;
            }
            
            // TODO ??????????????????
            redisService.del(webUUID);
            
            // ????????????????????????????????????
            checkUser = commonMapper.checkUserPwd(paramMap);
        } else if (ENUM_LOGIN_TYPE.SSO_LOGIN.getCode().equals(loginType)) {
            String ssoToken = StringUtil.getString(paramMap.get("token"));
            this.logger.info("ssoLogin - token=" + ssoToken);
            if (!StringUtil.isNotEmpty(ssoToken)) {
                result.setMsg("token??????????????????");
                return result;
            }
            String areaLabel = "jiashan";
            String jsSsoAuth = commonMapper.getSsoAuth(areaLabel);
            Map accessToken = restTemplate.postForEntity(jsSsoAuth, ImmutableMap.of("accessToken", ssoToken), Map.class).getBody();
            System.out.println("token ???????????? accessToken=" + accessToken);
            if (null != accessToken && "200".equals(accessToken.get("statusCode")) && "true".equals(StringUtil.getString(accessToken.get("result")))) {
                //TODO  sso ?????? ???????????????????????????????????????userID??????
                checkUser = commonMapper.checkUserByUserId("user_00000001");
            } else {
                result.setMsg("token????????????");
                return result;
            }
        } else if (ENUM_LOGIN_TYPE.PASSWORD_LOGIN.getCode().equals(loginType)) {
            //???????????????????????????????????????
            String[] checkParamsMap = {"userName", "password"};
            String errorString = checkParams(paramMap, checkParamsMap, ERROR_STRING_MAP);
            if (null != errorString) {
                result.setMsg(errorString);
                return result;
            }
            // ????????????????????????????????????
            checkUser = commonMapper.checkUserPwd(paramMap);
        } else {
            result.setMsg("??????????????????");
            return result;
        }
        if (checkUser == null) {
            result.setMsg("??????????????????????????????");
            return result;
        }
        String userId = StringUtil.getString(checkUser.get("userId"));
        Map<String, Object> resultMap = new HashMap<String, Object>();
        paramMap.put("userId", userId);
        List<Map<String, Object>> menuLevel1 = commonMapper.getMenuList(paramMap);
        if (menuLevel1 != null && menuLevel1.size() > 0) {
            for (Map<String, Object> mapMenu : menuLevel1) {
                paramMap.put("parentMenuId", mapMenu.get("menuId"));
                List<Map<String, Object>> menuLevel2 = commonMapper.getMenuList(paramMap);
                mapMenu.put("children", menuLevel2);
            }
        }
        String level = StringUtil.getString(checkUser.get("level"));
        resultMap.put("userId", checkUser.get("userId"));
        resultMap.put("userName", checkUser.get("userName"));
        resultMap.put("shortName", checkUser.get("shortName"));
        resultMap.put("areaLabel", "jiashan");
        resultMap.put("menuList", menuLevel1);
        resultMap.put("level", level);
        String token = JwtUtil.sign(userId);
        redisService.set(Conf.LOGINTOKEN + userId, token);
        redisService.expire(Conf.LOGINTOKEN + userId, 1200);
        if (ENUM_USER_LEVEL.ADMIN.getCode().equals(level)) {
            resultMap.put("defaultMenu", "AreasRA");
        } else if (ENUM_USER_LEVEL.COMPANY.getCode().equals(level)) {
            resultMap.put("defaultMenu", "archivesTabs");
            //????????????????????????
            String companyId = StringUtil.getString(checkUser.get("companyId"));
            Map<String, Object> entInfo =  govMapper.findEntBaseInfoByCompanyId(companyId);
            resultMap.put("entInfo", entInfo);
        }
        resultMap.put("token", token);
        result.setData(resultMap);
        result.setCode(Conf.SUCCESS);
        return result;
    }
    
    
    @RequestMapping(value = "/logout", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    Result logout(HttpServletResponse req, @RequestBody Map<String, Object> paramMap) {
        Result result = new Result();

        String[] checkParamsMap = {"userId"};
        String errorString = checkParams(paramMap, checkParamsMap, ERROR_STRING_MAP);
        if (null != errorString) {
            result.setCode(Conf.ERROR);
            result.setMsg(errorString);

            return result;
        }
        
        String userId = StringUtil.getString(paramMap.get("userId"));
        
        redisService.del(Conf.LOGINTOKEN + userId);

        return result;
    }


    @RequestMapping(value = "/getVerifyCode", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    Result getVerifyCode(HttpServletResponse req, @RequestBody Map<String, Object> paramMap) {
        Result result = new Result();

        String[] checkParamsMap = {"webUUID"};
        String errorString = checkParams(paramMap, checkParamsMap, ERROR_STRING_MAP);
        if (null != errorString) {
            result.setCode(Conf.ERROR);
            result.setMsg(errorString);

            return result;
        }

        RandomGenerator randomGenerator = new RandomGenerator("0123456789", 4);

        LineCaptcha lineCaptcha = CaptchaUtil.createLineCaptcha(200, 100);

        lineCaptcha.setGenerator(randomGenerator);

        String webUUID = paramMap.get("webUUID").toString();

        redisService.set(webUUID, lineCaptcha.getCode());
        redisService.expire(webUUID, 60);

        try {
            ServletOutputStream outputStream = req.getOutputStream();
            lineCaptcha.write(outputStream);
            outputStream.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return result;
    }

    //	@JwtToken
    @RequestMapping("/main")
    @ResponseBody
    Result getCurrent() {

        Result result = new Result();

        Map<String, Object> resultMap = new HashMap<String, Object>();

        result.setData(resultMap);

        return result;
    }


    @RequestMapping("/getJWT")
    @ResponseBody
    Result getJWT() {

        Result result = new Result();

        String token = JwtUtil.sign("user_00000001");

        redisService.set(Conf.LOGINTOKEN + "user_00000001", token);
        redisService.expire(Conf.LOGINTOKEN + "user_00000001", 300);

        result.setData(token);

        return result;
    }
    
    @JwtToken
    @RequestMapping("/checkJWT")
    @ResponseBody
    Result checkJWT(@RequestBody Map<String, Object> paramMap) {

        Result result = new Result();
        
        String token = paramMap.get("token").toString();

        String userId = JwtUtil.getUserId(token);

        // ?????? token
        String redisToken = redisService.get(Conf.LOGINTOKEN + "user_00004900");

        if (redisToken == null) {

            throw new RuntimeException("token ????????????????????????");
        }

        if (!redisToken.equals(token)) {

            throw new RuntimeException("token ????????????????????????");
        }

        result.setData(token);

        return result;
    }


    @RequestMapping(value = "/common/getProvinceList", method = RequestMethod.POST)
    @ResponseBody
    public Result getProvinceList(@RequestBody Map<String, Object> paramMap) {
        Result result = new Result();

//		String[] checkParamsMap = {  };
//		String errorString = checkParams(paramMap, checkParamsMap, ERROR_STRING_MAP);
//		if (null != errorString) {
//			result.setCode(Conf.ERROR);
//			result.setMsg(errorString);
//
//			return result;
//		}

        List<Map<String, Object>> el = commonMapper.getProvinceList(paramMap);

        result.setData(el);


        return result;
    }

    @RequestMapping(value = "/common/findTagAllList", method = RequestMethod.POST)
    @ResponseBody
    public Result findTagAllList(@RequestBody Map<String, Object> paramMap) {
        Result result = new Result();

        String[] checkParamsMap = {"areaLabel"};
        String errorString = checkParams(paramMap, checkParamsMap, ERROR_STRING_MAP);
        if (null != errorString) {
            result.setCode(Conf.ERROR);
            result.setMsg(errorString);

            return result;
        }

        List<TagDTO> tl = tagMapper.findTagListByArealable(paramMap);

        result.setData(tl);


        return result;
    }

    @RequestMapping(value = "/common/getStreetList", method = RequestMethod.POST)
    @ResponseBody
    public Result getStreetList(@RequestBody Map<String, Object> paramMap) {
        Result result = new Result();

//		String[] checkParamsMap = {  };
//		String errorString = checkParams(paramMap, checkParamsMap, ERROR_STRING_MAP);
//		if (null != errorString) {
//			result.setCode(Conf.ERROR);
//			result.setMsg(errorString);
//
//			return result;
//		}

        List<Map<String, Object>> el = commonMapper.getStreetList(paramMap);

        result.setData(el);


        return result;
    }

    @RequestMapping(value = "/common/getIndustryList", method = RequestMethod.POST)
    @ResponseBody
    public Result getIndustryList(@RequestBody Map<String, Object> paramMap) {
        Result result = new Result();

//		String[] checkParamsMap = {  };
//		String errorString = checkParams(paramMap, checkParamsMap, ERROR_STRING_MAP);
//		if (null != errorString) {
//			result.setCode(Conf.ERROR);
//			result.setMsg(errorString);
//
//			return result;
//		}

        List<Map<String, Object>> el = commonMapper.getIndustryList(paramMap);

        result.setData(el);


        return result;
    }

    @RequestMapping(value = "/getTableauUrlForWeb", method = RequestMethod.POST)
    @ResponseBody
    Result getTeUrl(HttpServletRequest request, @RequestBody Map<String, Object> paramMap) {
        Result result = new Result();
        String url = paramMap.get("url") == null ? null : paramMap.get("url").toString();
        // ??????????????????
        MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
        map.add("username", "tableau-admin");
        String returnStr = restTemplate.postForEntity("https://tableau.enesource.com/trusted", map, String.class).getBody();
        String tableauUrl = "/views/V1_16099996555210/sheet0?:iid=1";
        if ("capacity".equals(url)) {
            tableauUrl = "/views/V1/sheet0";
        }
        String returnUrl = "https://tableau.enesource.com/trusted" + "/" + returnStr + tableauUrl;
        result.setData(returnUrl);
        return result;
    }

    /**
     * @Author:lio
     * @Description: ????????????
     * @Date :10:22 ?????? 2021/1/31
     */
    @RequestMapping(value = "/updateUserPassword", method = RequestMethod.POST)
    @ResponseBody
    Result updateUserPassword(HttpServletRequest request, @RequestBody Map<String, Object> paramMap) {
        Result result = new Result();
        String localPassWord = StringUtil.getString(paramMap.get("localPassword"));
        String updatePassWord = StringUtil.getString(paramMap.get("updatePassword"));
        String userId = StringUtil.getString(paramMap.get("userId"));
        AssertUtil.NotBlank(localPassWord, "?????????????????????");
        AssertUtil.NotBlank(updatePassWord, "?????????????????????");
        AssertUtil.NotBlank(userId, "??????Id????????????");
        
        // TODO ??????????????????
//        if(!this.checkPas(updatePassWord)) {
//        	AssertUtil.NotBlank(updatePassWord, "??????????????????????????????????????????, ???????????????8-16???");
//        }
        
//        paramMap.put("localPassword", SHA256.getSHA256StrJava(localPassWord));
//        paramMap.put("updatePassword", SHA256.getSHA256StrJava(updatePassWord));
      paramMap.put("localPassword", localPassWord);
      paramMap.put("updatePassword", updatePassWord);
        int updateFlag = commonMapper.updateUserPassWord(paramMap);
        if (Conf.ERROR.equals(StringUtil.getString(updateFlag))) {
            result.setCode(Conf.ERROR);
            result.setMsg("???????????????????????????????????????????????????");
        }
        return result;
    }
    
    public static boolean checkPas(String pas) {
    	// ????????????????????????????????????, ???????????????8-16???
        Pattern pattern = Pattern.compile("^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).{8,16}$");
        Matcher matcher = pattern.matcher(pas);
        return matcher.matches();
    }
    
    public static void main(String[] args) {
		System.out.println(checkPas("aa1111111"));
	}


//    @RequestMapping(value = "/ssoLogin", method = RequestMethod.POST)
//    @ResponseBody
//    public Result ssoLogin(@RequestBody Map<String, Object> paramMap) {
//
//        Result result = new Result();
//
//        String[] checkParamsMap = {"userName", "password"};
//        String errorString = checkParams(paramMap, checkParamsMap, ERROR_STRING_MAP);
//        if (null != errorString) {
//            result.setCode(Conf.ERROR);
//            result.setMsg(errorString);
//
//            return result;
//        }
//
//        // ????????????????????????????????????
//        String userId = commonMapper.findByUsername(paramMap.get("userName").toString());
//
//        if (userId == null) {
//            result.setCode(Conf.ERROR);
//            result.setMsg("??????????????????????????????");
//
//            return result;
//        }
//
//        // ????????????????????????
//        Map<String, Object> checkUser = commonMapper.checkUserPwd(paramMap);
//
//        if (checkUser == null) {
//            result.setCode(Conf.ERROR);
//            result.setMsg("??????????????????????????????");
//
//            return result;
//        }
//
//        Map<String, Object> resultMap = new HashMap<String, Object>();
//
//        paramMap.put("userId", userId);
//
//        List<Map<String, Object>> menuLevel1 = commonMapper.getMenuList(paramMap);
//
//        if (menuLevel1 != null && menuLevel1.size() > 0) {
//
//            for (Map<String, Object> mapMenu : menuLevel1) {
//
//                Map<String, Object> param = new HashMap<String, Object>();
//
//                param.put("userId", userId);
//                param.put("parentMenuId", mapMenu.get("menuId"));
//
//                List<Map<String, Object>> menuLevel2 = commonMapper.getMenuList(param);
//
//                mapMenu.put("children", menuLevel2);
//
//            }
//        }
//
//        resultMap.put("userId", checkUser.get("userId"));
//        resultMap.put("userName", checkUser.get("userName"));
//        resultMap.put("shortName", checkUser.get("shortName"));
//        resultMap.put("areaLabel", checkUser.get("areaLabel"));
//        resultMap.put("menuList", menuLevel1);
//
//
//        String token = JwtUtil.sign(userId);
//
//        redisService.set(Conf.LOGINTOKEN + userId, token);
//        redisService.expire(Conf.LOGINTOKEN + userId, 1800);
//
//        resultMap.put("token", token);
//
//        result.setData(resultMap);
//
//        return result;
//    }

}
