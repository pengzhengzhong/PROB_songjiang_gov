package com.enesource.jump.web.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import com.enesource.jump.web.enums.ENUM_DATA_TYPE;
import com.enesource.jump.web.utils.*;
import org.apache.commons.compress.utils.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.enesource.jump.web.annotation.JwtToken;
import com.enesource.jump.web.common.BaseAction;
import com.enesource.jump.web.common.CheckEntInfoDTOExcelVerifyHandler;
import com.enesource.jump.web.common.Result;
import com.enesource.jump.web.dao.ICommonMapper;
import com.enesource.jump.web.dao.IGovBigScreen;
import com.enesource.jump.web.dao.IGovMapper;
import com.enesource.jump.web.dao.IInsightMapper;
import com.enesource.jump.web.dto.ChargeDTO;
import com.enesource.jump.web.dto.DateColumnDTO;
import com.enesource.jump.web.dto.EconomicsinfoDTO;
import com.enesource.jump.web.dto.EnergyinfoDTO;
import com.enesource.jump.web.dto.EntInfoDTO;
import com.enesource.jump.web.dto.ExceptionLog;
import com.enesource.jump.web.dto.GovMapInfoDTO;
import com.enesource.jump.web.dto.GovMapInfoDetailDTO;
import com.enesource.jump.web.dto.InsightDTO;
import com.enesource.jump.web.dto.ParamDTO;
import com.enesource.jump.web.dto.PhotoDTO;
import com.enesource.jump.web.service.IAsyncService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

import cn.afterturn.easypoi.excel.ExcelImportUtil;
import cn.afterturn.easypoi.excel.entity.ImportParams;
import cn.afterturn.easypoi.excel.entity.result.ExcelImportResult;

@Controller
@CrossOrigin
public class InsightAction extends BaseAction {

    @Autowired
    ICommonMapper commonMapper;

    @Autowired
    IInsightMapper insightMapper;

    @Autowired
    IGovMapper govMapper;

    @Autowired
    IAsyncService asyncService;

    /**
     * ??????????????????????????????
     *
     * @param paramMap
     * @return
     */
    @JwtToken
    @RequestMapping(value = "/insight/findInsightIndex", method = RequestMethod.POST)
    @ResponseBody
    public Result findInsightIndex(@RequestBody Map<String, Object> paramMap) {
        Result result = new Result();

        try {
            String[] checkParamsMap = {"userId", "areaLabel"};
            String errorString = checkParams(paramMap, checkParamsMap, ERROR_STRING_MAP);
            if (null != errorString) {
                result.setCode(Conf.ERROR);
                result.setMsg(errorString);

                return result;
            }

            List<Map<String, Object>> valueList = insightMapper.findInsightIndex(paramMap);

            result.setData(valueList);
        } catch (Exception e) {
            logger.error("????????????????????????????????????", e);
            ExceptionLog exceptionLog = new ExceptionLog();
            exceptionLog.setType(1);
            exceptionLog.setUrl("/insight/findInsightIndex");
            exceptionLog.setName("????????????????????????????????????");
            exceptionLog.setLevel(1);
            exceptionLog.setInformation(e.getMessage());
            exceptionLog.setOperator("");

            this.saveExceptionLog(exceptionLog);

            result.setCode(Conf.ERROR);
            result.setMsg("????????????");

            return result;

        }

        return result;
    }


    /**
     * ??????????????????????????????
     *
     * @param paramMap
     * @return
     */
    @JwtToken
    @RequestMapping(value = "/insight/saveInsightIndex", method = RequestMethod.POST)
    @ResponseBody
    public Result saveInsightIndex(@RequestBody Map<String, Object> paramMap) {
        Result result = new Result();

        try {
            String[] checkParamsMap = {"userId"};
            String errorString = checkParams(paramMap, checkParamsMap, ERROR_STRING_MAP);
            if (null != errorString) {
                result.setCode(Conf.ERROR);
                result.setMsg(errorString);

                return result;
            }


            List<Map<String, Object>> valueList = (List<Map<String, Object>>) paramMap.get("valueList");

            for (Map<String, Object> map : valueList) {
                Set keySet = map.keySet();

                map.put("key", keySet.iterator().next());
                map.put("value", map.get(keySet.iterator().next()));
                map.put("areaLabel", paramMap.get("areaLabel"));

                insightMapper.saveInsightIndex(map);

            }

            // ????????????????????????????????????????????????
            asyncService.updateEntEnergyinfoCompSumStatus();

        } catch (Exception e) {
            logger.error("????????????????????????????????????", e);
            ExceptionLog exceptionLog = new ExceptionLog();
            exceptionLog.setType(1);
            exceptionLog.setUrl("/insight/saveInsightIndex");
            exceptionLog.setName("????????????????????????????????????");
            exceptionLog.setLevel(1);
            exceptionLog.setInformation(e.getMessage());
            exceptionLog.setOperator("");

            this.saveExceptionLog(exceptionLog);

            result.setCode(Conf.ERROR);
            result.setMsg("????????????");

            return result;

        }

        return result;
    }


    /**
     * ??????????????????????????????
     *
     * @param paramMap
     * @return
     */
    @JwtToken
    @RequestMapping(value = "/insight/findInsightStatistics", method = RequestMethod.POST)
    @ResponseBody
    public Result findInsightStatistics(@RequestBody Map<String, Object> paramMap) {
        Result result = new Result();

        try {
            String[] checkParamsMap = {"userId", "areaLabel"};
            String errorString = checkParams(paramMap, checkParamsMap, ERROR_STRING_MAP);
            if (null != errorString) {
                result.setCode(Conf.ERROR);
                result.setMsg(errorString);

                return result;
            }

            Map<String, Object> m = insightMapper.findInsightStatistics(paramMap);


            if (paramMap.get("companyId") != null) {
                // ??????????????????
                Double lastsametimeElevalue = insightMapper.getLastsametimeElevalue(paramMap);

                if (lastsametimeElevalue == null) {
                    lastsametimeElevalue = 0d;
                }

                m.put("lastsametimeElevalue", lastsametimeElevalue);

                // ?????????????????????
                List<Map<String, Object>> eleValueList = govMapper.findEleValueByCompanyId(paramMap);

                m.put("eleValueList", eleValueList);
            }

            result.setData(m);
        } catch (Exception e) {
            logger.error("????????????????????????????????????", e);
            ExceptionLog exceptionLog = new ExceptionLog();
            exceptionLog.setType(1);
            exceptionLog.setUrl("/insight/findInsightStatistics");
            exceptionLog.setName("????????????????????????????????????");
            exceptionLog.setLevel(1);
            exceptionLog.setInformation(e.getMessage());
            exceptionLog.setOperator("");

            this.saveExceptionLog(exceptionLog);

            result.setCode(Conf.ERROR);
            result.setMsg("????????????");

            return result;

        }

        return result;
    }

    /**
     * ????????????????????????????????????
     *
     * @param paramMap
     * @return
     */
    @JwtToken
    @RequestMapping(value = "/insight/findInsightWeekStatistics", method = RequestMethod.POST)
    @ResponseBody
    public Result findInsightWeekStatistics(@RequestBody Map<String, Object> paramMap) {
        Result result = new Result();

        try {
            String[] checkParamsMap = {"userId", "areaLabel"};
            String errorString = checkParams(paramMap, checkParamsMap, ERROR_STRING_MAP);
            if (null != errorString) {
                result.setCode(Conf.ERROR);
                result.setMsg(errorString);

                return result;
            }

            Map<String, Object> m = insightMapper.findInsightWeekStatistics(paramMap);


            if (paramMap.get("companyId") != null) {
                // TODO
                Double lastsametimeElevalue = insightMapper.getLastsametimeElevalue(paramMap);

                if (lastsametimeElevalue == null) {
                    lastsametimeElevalue = 0d;
                }

                m.put("lastsametimeElevalue", lastsametimeElevalue);

                // ?????????????????????
                List<Map<String, Object>> eleValueList = govMapper.findEleValueByCompanyId(paramMap);

                m.put("eleValueList", eleValueList);
            }

            result.setData(m);
        } catch (Exception e) {
            logger.error("??????????????????????????????????????????", e);
            ExceptionLog exceptionLog = new ExceptionLog();
            exceptionLog.setType(1);
            exceptionLog.setUrl("/insight/findInsightWeekStatistics");
            exceptionLog.setName("??????????????????????????????????????????");
            exceptionLog.setLevel(1);
            exceptionLog.setInformation(e.getMessage());
            exceptionLog.setOperator("");

            this.saveExceptionLog(exceptionLog);

            result.setCode(Conf.ERROR);
            result.setMsg("????????????");

            return result;

        }

        return result;
    }


    /**
     * ?????????????????????????????????
     *
     * @param paramMap
     * @return
     */
    @JwtToken
    @RequestMapping(value = "/insight/findInsightEleList", method = RequestMethod.POST)
    @ResponseBody
    public Result findInsightEleList(@RequestBody Map<String, Object> paramMap) {
        Result result = new Result();
        try {
            String energyType = StringUtil.getString(paramMap.get("energyType"));
            List<Map<String, Object>> valueList = Lists.newArrayList();
            if (ENUM_DATA_TYPE.YONGDIAN.getCode().equals(energyType) || StringUtil.isEmpty(energyType)) {
                valueList = insightMapper.findInsightEleList(paramMap);
            } else if (ENUM_DATA_TYPE.YONGRE.getCode().equals(energyType)) {
                valueList = insightMapper.findInsightYongReList(paramMap);
            }
            double sumValue = 0;
            if (valueList != null && valueList.size() > 0) {
                for (Map<String, Object> map : valueList) {
                    sumValue += Double.valueOf(map.get("value").toString());
                }
            }
            Map<String, Object> resuleMap = new HashMap<String, Object>();
            resuleMap.put("valueList", valueList);
            resuleMap.put("sumValue", sumValue);
            result.setData(valueList);
        } catch (Exception e) {
            logger.error("???????????????????????????????????????", e);
            ExceptionLog exceptionLog = new ExceptionLog();
            exceptionLog.setType(1);
            exceptionLog.setUrl("/insight/findInsightEleList");
            exceptionLog.setName("???????????????????????????????????????");
            exceptionLog.setLevel(1);
            exceptionLog.setInformation(e.getMessage());
            exceptionLog.setOperator("");

            this.saveExceptionLog(exceptionLog);

            result.setCode(Conf.ERROR);
            result.setMsg("????????????");

            return result;

        }

        return result;
    }

    /**
     * ?????????????????????????????????????????????
     *
     * @param paramMap
     * @return
     */
    @JwtToken
    @RequestMapping(value = "/insight/findInsightWeekEleList", method = RequestMethod.POST)
    @ResponseBody
    public Result findInsightWeekEleList(@RequestBody Map<String, Object> paramMap) {
        Result result = new Result();

        try {
            String[] checkParamsMap = {"userId", "areaLabel"};
            String errorString = checkParams(paramMap, checkParamsMap, ERROR_STRING_MAP);
            if (null != errorString) {
                result.setCode(Conf.ERROR);
                result.setMsg(errorString);

                return result;
            }

            // TODO
            List<Map<String, Object>> valueList = insightMapper.findInsightEleListWeek(paramMap);

            double sumValue = 0;

            if (valueList != null && valueList.size() > 0) {
                for (Map<String, Object> map : valueList) {
                    sumValue += Double.valueOf(map.get("value").toString());
                }
            }

            List<Map<String, Object>> lastValueList = insightMapper.findInsightEleListLastWeek(paramMap);


            Map<String, Object> resuleMap = new HashMap<String, Object>();

            resuleMap.put("valueList", valueList);
            resuleMap.put("lastValueList", lastValueList);
            resuleMap.put("sumValue", sumValue);

            result.setData(resuleMap);
        } catch (Exception e) {
            logger.error("???????????????????????????????????????????????????", e);
            ExceptionLog exceptionLog = new ExceptionLog();
            exceptionLog.setType(1);
            exceptionLog.setUrl("/insight/findInsightWeekEleList");
            exceptionLog.setName("???????????????????????????????????????????????????");
            exceptionLog.setLevel(1);
            exceptionLog.setInformation(e.getMessage());
            exceptionLog.setOperator("");

            this.saveExceptionLog(exceptionLog);

            result.setCode(Conf.ERROR);
            result.setMsg("????????????");

            return result;

        }

        return result;
    }


    /**
     * ??????????????????????????????
     *
     * @param paramMap
     * @return
     */
    @JwtToken
    @RequestMapping(value = "/insight/findInsightCompanyList", method = RequestMethod.POST)
    @ResponseBody
    public Result findInsightCompanyList(@RequestBody Map<String, Object> paramMap) {
        Result result = new Result();

        try {
            String[] checkParamsMap = {"userId", "areaLabel"};
            String errorString = checkParams(paramMap, checkParamsMap, ERROR_STRING_MAP);
            if (null != errorString) {
                result.setCode(Conf.ERROR);
                result.setMsg(errorString);
                return result;
            }
            InsightDTO dto = (InsightDTO) MapUtils.map2JavaBean(InsightDTO.class, paramMap);
            String date = dto.getDate();
            AssertUtil.NotBlank(date,"??????????????????");
            dto.setYear(date.split("-")[0]);
            if (dto.getPage() != null) {
                PageHelper.startPage(dto.getPage(), dto.getPageSize());
            }
            List<Map<String, Object>> valueList = insightMapper.findInsightCompanyList(dto);
            result.setData(new PageInfo(valueList));
        } catch (Exception e) {
            logger.error("????????????????????????????????????", e);
            ExceptionLog exceptionLog = new ExceptionLog();
            exceptionLog.setType(1);
            exceptionLog.setUrl("/insight/findInsightCompanyList");
            exceptionLog.setName("????????????????????????????????????");
            exceptionLog.setLevel(1);
            exceptionLog.setInformation(e.getMessage());
            exceptionLog.setOperator("");

            this.saveExceptionLog(exceptionLog);

            result.setCode(Conf.ERROR);
            result.setMsg("????????????");

            return result;

        }

        return result;
    }


    /**
     * ??????????????????????????????????????????
     *
     * @param paramMap
     * @return
     */
    @JwtToken
    @RequestMapping(value = "/insight/findInsightWeekCompanyList", method = RequestMethod.POST)
    @ResponseBody
    public Result findInsightWeekCompanyList(@RequestBody Map<String, Object> paramMap) {
        Result result = new Result();

        try {
            String[] checkParamsMap = {"userId", "areaLabel"};
            String errorString = checkParams(paramMap, checkParamsMap, ERROR_STRING_MAP);
            if (null != errorString) {
                result.setCode(Conf.ERROR);
                result.setMsg(errorString);

                return result;
            }

            InsightDTO dto = (InsightDTO) MapUtils.map2JavaBean(InsightDTO.class, paramMap);

            if (dto.getPage() != null) {
                PageHelper.startPage(dto.getPage(), dto.getPageSize());
            }

            // TODO
            List<Map<String, Object>> valueList = insightMapper.findInsightWeekCompanyList(dto);

            result.setData(new PageInfo(valueList));
        } catch (Exception e) {
            logger.error("????????????????????????????????????????????????", e);
            ExceptionLog exceptionLog = new ExceptionLog();
            exceptionLog.setType(1);
            exceptionLog.setUrl("/insight/findInsightWeekCompanyList");
            exceptionLog.setName("????????????????????????????????????????????????");
            exceptionLog.setLevel(1);
            exceptionLog.setInformation(e.getMessage());
            exceptionLog.setOperator("");

            this.saveExceptionLog(exceptionLog);

            result.setCode(Conf.ERROR);
            result.setMsg("????????????");

            return result;

        }

        return result;
    }


    /**
     * ????????????????????????
     *
     * @param paramMap
     * @return
     */
    @JwtToken
    @RequestMapping(value = "/insight/updateCompanyUnitEle", method = RequestMethod.POST)
    @ResponseBody
    public Result updateCompanyUnitEle(@RequestBody Map<String, Object> paramMap) {
        Result result = new Result();

        try {
            String[] checkParamsMap = {"userId", "companyId", "areaLabel"};
            String errorString = checkParams(paramMap, checkParamsMap, ERROR_STRING_MAP);
            if (null != errorString) {
                result.setCode(Conf.ERROR);
                result.setMsg(errorString);

                return result;
            }

            insightMapper.updateCompanyUnitEle(paramMap);

            // ??????????????????????????????????????????????????????
            asyncService.updateProductionStatus(paramMap);

        } catch (Exception e) {
            logger.error(" ??????????????????????????????", e);
            ExceptionLog exceptionLog = new ExceptionLog();
            exceptionLog.setType(1);
            exceptionLog.setUrl("/insight/updateCompanyUnitEle");
            exceptionLog.setName(" ??????????????????????????????");
            exceptionLog.setLevel(1);
            exceptionLog.setInformation(e.getMessage());
            exceptionLog.setOperator("");

            this.saveExceptionLog(exceptionLog);

            result.setCode(Conf.ERROR);
            result.setMsg("????????????");

            return result;

        }

        return result;
    }

}
