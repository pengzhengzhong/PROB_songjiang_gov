package com.enesource.jump.web.service.impl;

import java.math.BigDecimal;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import cn.afterturn.easypoi.excel.ExcelImportUtil;
import cn.afterturn.easypoi.excel.entity.ImportParams;
import cn.afterturn.easypoi.excel.entity.result.ExcelImportResult;
import cn.hutool.core.util.ObjectUtil;
import com.enesource.jump.web.dao.IDataMaintenanceMapper;
import com.enesource.jump.web.dto.*;
import com.enesource.jump.web.enums.ENUM_DATA_TYPE;
import com.enesource.jump.web.interceptor.CheckExcelDayImportDTOExcelVerifyHandler;
import com.enesource.jump.web.interceptor.CheckExcelEleDayImportDTOExcelVerifyHandler;
import com.enesource.jump.web.interceptor.CheckExcelMonthFuHeImportDTOExcelVerifyHandler;
import com.enesource.jump.web.interceptor.CheckExcelMonthImportDTOExcelVerifyHandler;
import com.enesource.jump.web.utils.*;
import com.google.common.collect.Maps;
import lombok.SneakyThrows;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.enesource.jump.web.dao.IGovMapper;
import com.enesource.jump.web.service.IAsyncService;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import static java.time.temporal.ChronoUnit.DAYS;


@Service("asyncService")
@Transactional
public class AsyncServiceImpl implements IAsyncService {

    Logger logger = Logger.getLogger(this.getClass());

    @Autowired
    IGovMapper govMapper;
    @Autowired
    IDataMaintenanceMapper iDataMaintenanceMapper;
    @Autowired
    private CheckExcelEleDayImportDTOExcelVerifyHandler checkExcelEleDayImportDTOExcelVerifyHandler;

    @Override
    @Async
    public void mergeEntInfo(List<EntInfoDTO> dtol) {

        for (EntInfoDTO entity : dtol) {
            govMapper.mergeEntInfo(entity);
        }

    }

    @Override
    @Async
    public void mergeEnergyinfo(List<EnergyinfoDTO> dtol, String energyType, String areaLabel) {

        for (EnergyinfoDTO entity : dtol) {
            govMapper.mergeEnergyinfo(entity);
        }

        // ?????????????????????
        Map<String, Object> param = new HashMap<String, Object>();

        param.put("energyType", energyType);
        param.put("areaLabel", areaLabel);

        govMapper.deleteAreaStatistics(param);

        govMapper.insertAreaStatistics(param);


        if ("0".equals(energyType)) {
            // ??????????????????????????????????????????????????????
            govMapper.saveEntEnergyinfoCompSum(new HashMap<String, Object>());

            // ??????????????????
            govMapper.updateProductionStatus_0(new HashMap<String, Object>());
            govMapper.updateProductionStatus_1(new HashMap<String, Object>());
            govMapper.updateProductionStatus_2(new HashMap<String, Object>());
            govMapper.updateProductionStatus_3(new HashMap<String, Object>());
            govMapper.updateProductionStatus_4(new HashMap<String, Object>());
            govMapper.updateProductionStatus_5(new HashMap<String, Object>());

            // ???????????????
            govMapper.updateMonthRate(new HashMap<String, Object>());

            // ???????????????
            govMapper.updateYearRate(new HashMap<String, Object>());

            // ??????????????????
            govMapper.updateGrowthRates_0(new HashMap<String, Object>());
            govMapper.updateGrowthRates_1(new HashMap<String, Object>());
            govMapper.updateGrowthRates_2(new HashMap<String, Object>());
            govMapper.updateGrowthRates_3(new HashMap<String, Object>());
        }

    }

    @Override
    @Async
    public void mergeEconomicsinfo(List<EconomicsinfoDTO> dtol, String areaLabel) {

        for (EconomicsinfoDTO entity : dtol) {
            govMapper.mergeEconomicsinfo(entity);
        }

        // ?????????????????????
        Map<String, Object> param = new HashMap<String, Object>();

        param.put("energyType", "ec");
        param.put("areaLabel", areaLabel);

        govMapper.deleteAreaStatistics(param);

        govMapper.insertAreaStatisticsEC(param);

    }

    @Override
    @Async
    public void mergeGovMapInfoDTO(List<GovMapInfoDTO> dtol) {
        for (GovMapInfoDTO entity : dtol) {

            if (entity.getSiteId() == null) {
                entity.setSiteId(govMapper.getSiteId());
            }

            govMapper.mergeGovMapInfoDTO(entity);

            DateColumnDTO tempDto = entity.getDateColumn();

            Map<String, Object> tempMap = MapUtils.java2Map(tempDto);

            GovMapInfoDetailDTO dtoCurve = new GovMapInfoDetailDTO();

            dtoCurve.setSiteId(entity.getSiteId());
            dtoCurve.setSiteName(entity.getSiteName());

            if (tempMap != null) {
                Set<String> keySet = tempMap.keySet();

                for (String key : keySet) {
                    String dateStr = key.substring(5, 9) + "-" + key.substring(9, 11) + "-01 00:00:00";

                    dtoCurve.setDate(dateStr);
                    dtoCurve.setValue(tempMap.get(key).toString());

                    govMapper.mergeGovMapCurveDTO(dtoCurve);

                }

            }

        }

    }


    @Override
    @Async
    public void mergePhotoInfoDTO(List<PhotoDTO> dtol) {
        for (PhotoDTO entity : dtol) {

            if (entity.getSiteId() == null) {
                entity.setSiteId(govMapper.getSiteId());
            }

            govMapper.mergeGovMapInfoDTO(entity);

            DateColumnDTO tempDto = entity.getDateColumn();

            Map<String, Object> tempMap = MapUtils.java2Map(tempDto);

            GovMapInfoDetailDTO dtoCurve = new GovMapInfoDetailDTO();

            dtoCurve.setSiteId(entity.getSiteId());
            dtoCurve.setSiteName(entity.getSiteName());

            if (tempMap != null) {
                Set<String> keySet = tempMap.keySet();

                for (String key : keySet) {
                    String dateStr = key.substring(5, 9) + "-" + key.substring(9, 11) + "-01 00:00:00";

                    dtoCurve.setDate(dateStr);
                    dtoCurve.setValue(tempMap.get(key).toString());

                    govMapper.mergeGovMapCurveDTO(dtoCurve);

                }

            }
        }

    }

    @Override
    @Async
    public void mergeChargeInfoDTO(List<ChargeDTO> dtol) {
        for (ChargeDTO entity : dtol) {

            if (entity.getSiteId() == null) {
                entity.setSiteId(govMapper.getSiteId());
            }

            govMapper.mergeGovMapInfoDTO(entity);

            DateColumnDTO tempDto = entity.getDateColumn();

            Map<String, Object> tempMap = MapUtils.java2Map(tempDto);

            GovMapInfoDetailDTO dtoCurve = new GovMapInfoDetailDTO();

            dtoCurve.setSiteId(entity.getSiteId());
            dtoCurve.setSiteName(entity.getSiteName());

            if (tempMap != null) {
                Set<String> keySet = tempMap.keySet();

                for (String key : keySet) {
                    String dateStr = key.substring(5, 9) + "-" + key.substring(9, 11) + "-01 00:00:00";

                    dtoCurve.setDate(dateStr);
                    dtoCurve.setValue(tempMap.get(key).toString());

                    govMapper.mergeGovMapCurveDTO(dtoCurve);

                }

            }
        }

    }

    @Override
//	@Async
    public void mergeDayValueDTO(List<ImportDayValueDTO> dtol, String date) {
        //  ????????????????????????
        if (dtol == null || dtol.size() == 0) {
            return;
        }

        for (ImportDayValueDTO importDayValueDTO : dtol) {
            if (importDayValueDTO.getValue() != null) {
                govMapper.mergeEntEnergyinfoDay(importDayValueDTO);
            }
        }

        //  ????????????????????????
        govMapper.delLoseAccnumberDay(date);

        //  ??????????????????????????????
        govMapper.saveLoseAccnumberDay(date);


        //  ????????????????????????????????????
        govMapper.delLoseStatisticsDay(date);
        //  ????????????????????????????????????
        govMapper.saveLoseStatisticsDay(date);
    }

    @Override
    @Async
    public void updateEntEnergyinfoCompSumStatus() {
        // ??????????????????
        govMapper.updateProductionStatus_0(new HashMap<String, Object>());
        govMapper.updateProductionStatus_1(new HashMap<String, Object>());
        govMapper.updateProductionStatus_2(new HashMap<String, Object>());
        govMapper.updateProductionStatus_3(new HashMap<String, Object>());
        govMapper.updateProductionStatus_4(new HashMap<String, Object>());
        govMapper.updateProductionStatus_5(new HashMap<String, Object>());

        govMapper.updateProductionStatus_week_0(new HashMap<String, Object>());
        govMapper.updateProductionStatus_week_1(new HashMap<String, Object>());
        govMapper.updateProductionStatus_week_2(new HashMap<String, Object>());
        govMapper.updateProductionStatus_week_3(new HashMap<String, Object>());
        govMapper.updateProductionStatus_week_4(new HashMap<String, Object>());
        govMapper.updateProductionStatus_week_5(new HashMap<String, Object>());

        // ???????????????
        govMapper.updateMonthRate(new HashMap<String, Object>());

        // ???????????????
        govMapper.updateYearRate(new HashMap<String, Object>());

        // ??????????????????
        govMapper.updateGrowthRates_0(new HashMap<String, Object>());
        govMapper.updateGrowthRates_1(new HashMap<String, Object>());
        govMapper.updateGrowthRates_2(new HashMap<String, Object>());
        govMapper.updateGrowthRates_3(new HashMap<String, Object>());

        govMapper.updateGrowthRates_week_0(new HashMap<String, Object>());
        govMapper.updateGrowthRates_week_1(new HashMap<String, Object>());
        govMapper.updateGrowthRates_week_2(new HashMap<String, Object>());
        govMapper.updateGrowthRates_week_3(new HashMap<String, Object>());

    }

    @Override
    @Async
    public void updateProductionStatus(Map<String, Object> paramMap) {
        // ??????????????????
        govMapper.updateProductionValue(paramMap);
        // ??????????????????
        govMapper.updateProductionStatus_0(paramMap);
        govMapper.updateProductionStatus_1(paramMap);
        govMapper.updateProductionStatus_2(paramMap);
        govMapper.updateProductionStatus_3(paramMap);
        govMapper.updateProductionStatus_4(paramMap);
        govMapper.updateProductionStatus_5(paramMap);

    }

    /**
     * @Author:lio
     * @Description: ??????????????????????????????
     * ??????date = yyyy
     * ??????date = yyyy-mm
     * @Date :10:07 ?????? 2021/1/29
     */
    @Override
    @Async
    public void updateEnergyInfo(ExcelDataImportDTO dataImportDto) {
        String dataType = dataImportDto.getDataType();
        String tce = ENUM_DATA_TYPE.getTceByCode(dataImportDto.getDataType());
        if (!StringUtil.isNotEmpty(tce)) {
            tce = "1";
        }
        dataImportDto.setTce(tce);
        if (ENUM_DATA_TYPE.YONGDIAN.getCode().equals(dataType)) {
            // ???????????? t_ent_energyinfo
            //???????????????????????????????????????????????????????????????????????????
            // ???????????????????????????,?????????????????????????????????????????????
            //?????????????????????????????????????????????????????????
            govMapper.delEenergyInfo(dataImportDto);
            govMapper.insertImportEleEenergyInfo(dataImportDto);
//            //???????????????
//            //?????????????????????????????? ??????????????????????????? ???????????????????????????
//            LocalDate startDate = getLocalDateByStr(date);
//            String startDayOfWeek = StringUtil.getString(startDate.getDayOfWeek());
//            int addStartDay = getAddStartDay(startDayOfWeek);
//            String startTime = DateUtil.getTimeDayAdd("yyyy-MM-dd", DateUtil.getDateByStringDate("yyyy-MM-dd", date), addStartDay);
//            //???????????????????????????
//            String endDay = DateUtil.getEndDayByMonth("yyyy-MM-dd", DateUtil.getDateByStringDate("yyyy-MM-dd", date));
//            LocalDate endDate = getLocalDateByStr(endDay);
//            String dayOfWeek = StringUtil.getString(endDate.getDayOfWeek());
//            int addEndDay = getAddEndDay(dayOfWeek);
//            String endTime = DateUtil.getTimeDayAdd("yyyy-MM-dd", DateUtil.getDateByStringDate("yyyy-MM-dd", StringUtil.getString(endDate)), addEndDay);
//            //????????????????????????????????? ???????????????
//            // ???????????????????????????
//            Map<String, Object> weekMap = Maps.newHashMap();
//            weekMap.put("startTime", startTime);
//            weekMap.put("endTime", endTime);
//            govMapper.delWeekSum(weekMap);

            //    ????????? t_ent_energyinfo_comp_week_sum
            //???????????????
            //  govMapper.insertEntEnergyInfoWeekSum(weekMap);
            //?????????????????????
            //govMapper.uploadWeekSumRate(weekMap);
            // ??????????????????
//            govMapper.updateWeekProductionStatus_0(weekMap);
//            govMapper.updateWeekProductionStatus_1(weekMap);
//            govMapper.updateWeekProductionStatus_2(weekMap);
//            govMapper.updateWeekProductionStatus_3(weekMap);
//            govMapper.updateWeekProductionStatus_4(weekMap);
//            govMapper.updateWeekProductionStatus_5(weekMap);
//            // ??????????????????
//            govMapper.updateWeekGrowthRates_0(weekMap);
//            govMapper.updateWeekGrowthRates_1(weekMap);
//            govMapper.updateWeekGrowthRates_2(weekMap);
//            govMapper.updateWeekGrowthRates_3(weekMap);
            //?????? ????????? t_ent_energyinfo_comp_sum
            //????????????????????????
            // TODO ????????????????????????
            govMapper.deleteEntEnergyInfoComSum(dataImportDto);
            govMapper.insertEntEnergyInfoComSum(dataImportDto);
            // ??????????????????
            govMapper.updateProductionValueByImport(dataImportDto);
            govMapper.updateProductionStatusMonth_0(dataImportDto);
            govMapper.updateProductionStatusMonth_1(dataImportDto);
            govMapper.updateProductionStatusMonth_2(dataImportDto);
            govMapper.updateProductionStatusMonth_3(dataImportDto);
            govMapper.updateProductionStatusMonth_4(dataImportDto);
            govMapper.updateProductionStatusMonth_5(dataImportDto);
            // ???????????????
            govMapper.updateMonthRateByImport(dataImportDto);
            // ???????????????
            govMapper.updateYearRateByImport(dataImportDto);
            // ??????????????????
            govMapper.updateGrowthRatesMonth_0(dataImportDto);
            govMapper.updateGrowthRatesMonth_1(dataImportDto);
            govMapper.updateGrowthRatesMonth_2(dataImportDto);
            govMapper.updateGrowthRatesMonth_3(dataImportDto);

            // ????????????????????? t_ent_energyinfo_day_area
            govMapper.delEnergyinfoDayArea(dataImportDto);
            govMapper.insertEnergyinfoDayArea(dataImportDto);
            // ???????????? t_area_statistics
            govMapper.delAreaStatistics(dataImportDto);
            govMapper.insertAreaStatisticsByImport(dataImportDto);
        } else if (ENUM_DATA_TYPE.KAIPIAO.getCode().equals(dataType)) {
            //????????????
        } else if (ENUM_DATA_TYPE.DIANFEI.getCode().equals(dataType)) {
            //????????????
        } else if (ENUM_DATA_TYPE.ZUIGAOFUHE.getCode().equals(dataType)) {
            // ????????????
        } else if (ENUM_DATA_TYPE.YONGRE.getCode().equals(dataType)) {
            //?????????????????????
            govMapper.delEenergyInfo(dataImportDto);
            govMapper.insertImportReLiEenergyInfo(dataImportDto);
            // ???????????? t_area_statistics
            govMapper.delAreaStatisticsByDay(dataImportDto);
            govMapper.insertAreaStatisticsByImportDay(dataImportDto);
            // TODO ????????????????????????
            govMapper.deleteEntEnergyInfoComSum(dataImportDto);
            govMapper.insertEntEnergyInfoComSum(dataImportDto);
        } else if (ENUM_DATA_TYPE.TIANRANQI.getCode().equals(dataType) || ENUM_DATA_TYPE.YONGSHUI.getCode().equals(dataType)
                || ENUM_DATA_TYPE.MEITAN.getCode().equals(dataType) || ENUM_DATA_TYPE.QIYOU.getCode().equals(dataType)
                || ENUM_DATA_TYPE.CHAIYOU.getCode().equals(dataType) || ENUM_DATA_TYPE.SHENGWUZHI.getCode().equals(dataType)) {
            // ???????????? t_area_statistics
            govMapper.delAreaStatisticsByMonth(dataImportDto);
            govMapper.insertAreaStatisticsByImportByMonth(dataImportDto);
            // TODO ????????????????????????
            govMapper.deleteEntEnergyInfoComSumByMonth(dataImportDto);
            govMapper.insertEntEnergyInfoComSumByMonth(dataImportDto);
        }
    }

    @Override
    public void importData(MultipartFile multipartFile, ExcelDataImportDTO dataImportDto, String dataTypeCode, ImportParams params) throws Exception {
//        if (ENUM_DATA_TYPE.KAIPIAO.getCode().equals(dataTypeCode)
//                || ENUM_DATA_TYPE.DIANFEI.getCode().equals(dataTypeCode)
//                || ENUM_DATA_TYPE.TIANRANQI.getCode().equals(dataTypeCode)
//                || ENUM_DATA_TYPE.YONGSHUI.getCode().equals(dataTypeCode)
//                || ENUM_DATA_TYPE.MEITAN.getCode().equals(dataTypeCode)
//                || ENUM_DATA_TYPE.QIYOU.getCode().equals(dataTypeCode)
//                || ENUM_DATA_TYPE.CHAIYOU.getCode().equals(dataTypeCode)
//                || ENUM_DATA_TYPE.SHENGWUZHI.getCode().equals(dataTypeCode)) {
//            // ????????????
//            entInfoExcelImport(multipartFile, params, dataImportDto, entInfo, dataTypeCode);
//        } else if (ENUM_DATA_TYPE.YONGDIAN.getCode().equals(dataTypeCode)) {
//            entEleExcelImport(multipartFile, params, dataImportDto, entInfo);
//        } else if (ENUM_DATA_TYPE.ZUIGAOFUHE.getCode().equals(dataTypeCode)) {
//            entFuheExcelImport(multipartFile, params, dataImportDto, entInfo);
//        } else if (ENUM_DATA_TYPE.YONGRE.getCode().equals(dataTypeCode)) {
//            entYongReExcelImport(multipartFile, params, dataImportDto, entInfo);
//        }
    }

    /**
     * @Author:lio
     * @Description:??????????????????
     * @Date :4:54 ?????? 2021/2/3
     */
    @Async
    public void entYongReExcelImport(MultipartFile multipartFile, ImportParams params, ExcelDataImportDTO dataImportDto, ExcelImportResult<ExcelDayImport> excelData) {
        //??????????????????????????????
        List<Map<String, Object>> localInfo = iDataMaintenanceMapper.getEntYongReInfoByImport(dataImportDto);
        //????????????????????????
        List<Map<String, Object>> entInfo = govMapper.getAllEntInfo();
        String date = dataImportDto.getDate();
        String msg = "";
        String userId = dataImportDto.getUserId();
        String dataTypeCode = dataImportDto.getDataType();
        //????????????????????????
        String nextMonth = date + "-01 00:00:00";
        String endDayByMonth = DateUtil.getEndDayByMonth("", DateUtil.getDateByStringDate("", nextMonth));
        // ????????????????????????????????? ??????????????????
        if (!excelData.isVerfiyFail()) {
            List<Map<String, Object>> insertEntInfoDto = Lists.newArrayList();
            List<Map<String, Object>> finalLocalInfo = localInfo;
            excelData.getList().stream().forEach(k -> {
                Map<String, Object> tempMap = MapUtils.java2Map(k);
                Set<String> keySet = tempMap.keySet();
                String entName = k.getEntName();
                String companyId = "";
                String accNumber = "";
                String areaCode = "";
                for (int i = 0; i < entInfo.size(); i++) {
                    if (entName.equals(StringUtil.getString(entInfo.get(i).get("entName")))) {
                        companyId = StringUtil.getString(entInfo.get(i).get("companyId"));
                        if (dataTypeCode.equals(StringUtil.getString(entInfo.get(i).get("type")))) {
                            accNumber = StringUtil.getString(entInfo.get(i).get("accNumber"));
                            areaCode = StringUtil.getString(entInfo.get(i).get("areaCode"));
                        }
                    }
                }
                if (StringUtil.isNotEmpty(companyId, entName)) {
                    for (String key : keySet) {
                        if (key.contains("_Data")) {
                            String finalCompanyId = companyId;
                            String value = tempMap.get(key).toString();
                            String valueDate = "";
                            if (ENUM_DATA_TYPE.YONGRE.getCode().equals(dataTypeCode)) {
                                valueDate = date + "-" + key.replace("_Data", "") + " 00:00:00";
                            }
                            if (DateUtil.getBetweenDate(DateUtil.getDateByStringDate("yyyy-MM-dd HH:mm:ss", endDayByMonth)
                                    , DateUtil.getDateByStringDate("yyyy-MM-dd HH:mm:ss", valueDate), "yyyy-MM-dd HH:mm:ss") < 0) {
                                continue;
                            }
                            //??????????????????
                            try {
                                if (DateUtil.getBetweenDate(DateUtil.getDateByStringDate("yyyy-MM-dd HH:mm:ss", DateUtil.getStringDateByDate("yyyy-MM-dd HH:mm:ss", new Date())), DateUtil.getDateByStringDate("yyyy-MM-dd HH:mm:ss", valueDate), "yyyy-MM-dd HH:mm:ss") < 0) {
                                    continue;
                                }
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            Map<String, Object> resMap = Maps.newHashMap();
                            resMap.put("date", valueDate);
                            resMap.put("value", StringUtil.getStringZero(value));
                            resMap.put("companyId", finalCompanyId);
                            resMap.put("energyType", dataTypeCode);
                            resMap.put("accnumber", accNumber);
                            resMap.put("entName", entName);
                            resMap.put("userId", userId);
                            resMap.put("areaCode", areaCode);
                            insertEntInfoDto.add(resMap);
                        }
                    }
                }
            });
            // ????????????????????????????????????????????????
            if (!CollectionUtils.isEmpty(insertEntInfoDto)) {
                batchInserInfo(insertEntInfoDto, "yongre");
            }
        } else {
            List<ExcelDayImport> failList = excelData.getFailList();
            if (!CollectionUtils.isEmpty(failList)) {
                for (ExcelDayImport entity : failList) {
                    msg += "???" + entity.getRowNum() + "??????????????????" + entity.getErrorMsg() + "\n";
                }
                AssertUtil.ThrowSystemErr(msg);
            }
        }
        //??????????????????
        updateEnergyInfo(dataImportDto);
    }


    /**
     * @Author:lio
     * @Description: ????????????
     * @Date :11:30 ?????? 2021/1/8
     */
    @Async
    public void entInfoExcelImport(MultipartFile multipartFile, ImportParams params, ExcelDataImportDTO dataImportDto, String dataTypeCode, ExcelImportResult<ExcelMonthImport> excelData) {
        //????????????????????????
        List<Map<String, Object>> entInfo = govMapper.getAllEntInfo();
        // ???????????????????????????
        List<Map<String, Object>> localInfo = Lists.newArrayList();
        String msg = "";
        String userId = dataImportDto.getUserId();
        if (ENUM_DATA_TYPE.KAIPIAO.getCode().equals(dataTypeCode)) {
            localInfo = iDataMaintenanceMapper.getEntTicketInfoByImport(dataImportDto);
        } else if (ENUM_DATA_TYPE.DIANFEI.getCode().equals(dataTypeCode)) {
            localInfo = iDataMaintenanceMapper.getEntDianFeiInfoByImport(dataImportDto);
        } else if (ENUM_DATA_TYPE.TIANRANQI.getCode().equals(dataTypeCode)
                || ENUM_DATA_TYPE.YONGSHUI.getCode().equals(dataTypeCode)
                || ENUM_DATA_TYPE.MEITAN.getCode().equals(dataTypeCode)
                || ENUM_DATA_TYPE.CHAIYOU.getCode().equals(dataTypeCode)
                || ENUM_DATA_TYPE.SHENGWUZHI.getCode().equals(dataTypeCode)
                || ENUM_DATA_TYPE.QIYOU.getCode().equals(dataTypeCode)) {
            localInfo = iDataMaintenanceMapper.getEntEnergyInfoByImport(dataImportDto);
        }
        String date = dataImportDto.getDate();
        // ????????????????????????????????? ??????????????????
        if (!excelData.isVerfiyFail() && !CollectionUtils.isEmpty(excelData.getList())) {
            List<Map<String, Object>> insertEntInfoDto = Lists.newArrayList();
            List<Map<String, Object>> finalLocalInfo = localInfo;
            for (ExcelMonthImport k : excelData.getList()) {
                Map<String, Object> tempMap = MapUtils.java2Map(k);
                Set<String> keySet = tempMap.keySet();
                String entName = k.getEntName();
                String companyId = "";
                String accNumber = "";
                String areaCode = "";
                for (int i = 0; i < entInfo.size(); i++) {
                    if (entName.equals(StringUtil.getString(entInfo.get(i).get("entName")))) {
                        companyId = StringUtil.getString(entInfo.get(i).get("companyId"));
                        if (dataTypeCode.equals(StringUtil.getString(entInfo.get(i).get("type")))) {
                            accNumber = StringUtil.getString(entInfo.get(i).get("accNumber"));
                            areaCode = StringUtil.getString(entInfo.get(i).get("areaCode"));
                        }
                    }
                }
                if (StringUtil.isNotEmpty(companyId, entName)) {
                    for (String key : keySet) {
                        if (key.contains("_Data")) {
                            String finalCompanyId = companyId;
                            String value = tempMap.get(key).toString();
                            String valueDate = "";
                            String updateMonth = key.replace("_Data", "");
                            valueDate = date + "-" + key.replace("_Data", "") + "-01 00:00:00";
                            // ????????????????????????????????????
                            LocalDate localDate = LocalDate.now();
                            int monthValue = localDate.getMonthValue();
                            int yearValue = localDate.getYear();
                            if (Integer.parseInt(date) > yearValue || (Integer.parseInt(date) == yearValue && Integer.parseInt(updateMonth) > monthValue)) {
                                continue;
                            }
                            // ??????tce
                            String tce = "0";
                            if (StringUtil.isNotEmpty(value) && StringUtil.isNotEmpty(ENUM_DATA_TYPE.getTceByCode(dataTypeCode))) {
                                if (ENUM_DATA_TYPE.SHENGWUZHI.getCode().equals(dataTypeCode)) {
                                    tce = StringUtil.getStringZero(value);
                                } else {
                                    tce = BigDecimalUtil.multiply(value, StringUtil.getString(ENUM_DATA_TYPE.getTceByCode(dataTypeCode)));
                                }
                            }
                            Map<String, Object> resMap = Maps.newHashMap();
                            resMap.put("tce", tce);
                            resMap.put("date", valueDate);
                            resMap.put("value", StringUtil.getStringZero(value));
                            resMap.put("companyId", finalCompanyId);
                            resMap.put("energyType", dataTypeCode);
                            resMap.put("accnumber", accNumber);
                            resMap.put("entName", entName);
                            resMap.put("userId", userId);
                            resMap.put("areaCode", areaCode);
                            insertEntInfoDto.add(resMap);
                        }
                    }
                }
            }
            // ????????????????????????????????????????????????
            if (!CollectionUtils.isEmpty(insertEntInfoDto)) {
                if (ENUM_DATA_TYPE.KAIPIAO.getCode().equals(dataTypeCode)) {
                    batchInserInfo(insertEntInfoDto, "kaipiao");
                } else if (ENUM_DATA_TYPE.DIANFEI.getCode().equals(dataTypeCode)) {
                    batchInserInfo(insertEntInfoDto, "dianfei");
                } else if (ENUM_DATA_TYPE.TIANRANQI.getCode().equals(dataTypeCode)
                        || ENUM_DATA_TYPE.YONGSHUI.getCode().equals(dataTypeCode)
                        || ENUM_DATA_TYPE.MEITAN.getCode().equals(dataTypeCode)
                        || ENUM_DATA_TYPE.CHAIYOU.getCode().equals(dataTypeCode)
                        || ENUM_DATA_TYPE.QIYOU.getCode().equals(dataTypeCode)
                        || ENUM_DATA_TYPE.SHENGWUZHI.getCode().equals(dataTypeCode)) {
                    batchInserInfo(insertEntInfoDto, "energy");
                }
            }
        } else {
            List<ExcelMonthImport> failList = excelData.getFailList();
            if (!CollectionUtils.isEmpty(failList)) {
                for (ExcelMonthImport entity : failList) {
                    msg += "???" + entity.getRowNum() + "??????????????????" + entity.getErrorMsg() + "\n";
                }
                AssertUtil.ThrowSystemErr(msg);
            }
        }
        //??????????????????
        updateEnergyInfo(dataImportDto);
    }

    /**
     * @Author:lio
     * @Description: ????????????
     * @Date :9:30 ?????? 2021/2/1
     */
    private void batchInserInfo(List<Map<String, Object>> entInfoList, String batchType) {
        // ??????????????????
        int pointsDataLimit = 3000;//????????????
        Integer size = entInfoList.size();
        if (pointsDataLimit < size) {
            int part = size / pointsDataLimit + 1;//?????????
            for (int i = 0; i < part; i++) {
                //1000???
                int startLimit = i * pointsDataLimit;
                int endLimit = (i + 1) * pointsDataLimit - 1;
                if (endLimit + 1 > size) {
                    endLimit = size;
                }
                List<Map<String, Object>> listPage = entInfoList.subList(startLimit, endLimit);
                updateMapper(listPage, batchType);
            }
        } else {
            updateMapper(entInfoList, batchType);
        }
    }

    private void updateMapper(List<Map<String, Object>> listPage, String batchType) {
        if (batchType.equals("insert")) {
            iDataMaintenanceMapper.insertEleInfo(listPage);
        } else if (batchType.equals("updateFeng")) {
            iDataMaintenanceMapper.updateFengEleInfo(listPage);
        } else if (batchType.equals("updateJian")) {
            iDataMaintenanceMapper.updateJianEleInfo(listPage);
        } else if (batchType.equals("updateGu")) {
            iDataMaintenanceMapper.updateGuEleInfo(listPage);
        } else if (batchType.equals("kaipiao")) {
            iDataMaintenanceMapper.insertEntTicketInfo(listPage);
        } else if (batchType.equals("dianfei")) {
            iDataMaintenanceMapper.insertDianFeiInfo(listPage);
        } else if (batchType.equals("fuhe")) {
            iDataMaintenanceMapper.insertFuHeInfo(listPage);
        } else if (batchType.equals("energy")) {
            iDataMaintenanceMapper.insertEnergyInfo(listPage);
        } else if (batchType.equals("yongre")) {
            iDataMaintenanceMapper.insertYongReInfo(listPage);
        }
    }


    /**
     * @Author:lio
     * @Description:??????????????????
     * @Date :4:29 ?????? 2021/1/17
     */
    @Async
    @Override
    public void entEleExcelImport(MultipartFile multipartFile, ImportParams params, ExcelDataImportDTO dataImportDto, List<ExcelDayEleImport> readExcelList) {
        //??????????????????????????????
        List<Map<String, Object>> entInfo = govMapper.getAllEntInfo();
        // ????????????????????????????????? ??????????????????
        String date = dataImportDto.getDate();
        //????????????????????????
        String nextMonth = date + "-01 00:00:00";
        String endDayByMonth = DateUtil.getEndDayByMonth("", DateUtil.getDateByStringDate("", nextMonth));
            List<Map<String, Object>> insertEntInfoDto = Lists.newArrayList();
            Map<String, BigDecimal> entRcvElecCapsMap = checkExcelEleDayImportDTOExcelVerifyHandler.getEntRcvElecCapsMap();
            for (ExcelDayEleImport k : readExcelList) {
                Map<String, Object> tempMap = MapUtils.java2Map(k);
                Set<String> keySet = tempMap.keySet();
                String entName = k.getEntName().replace(" ", "");
                String companyId = "", areaCode = "", accNumber = k.getAccNumber();
                // ?????????????????????????????????
                if (!entRcvElecCapsMap.containsKey(accNumber)) {
                    continue;
                }
                for (int i = 0; i < entInfo.size(); i++) {
                    if (entName.equals(entInfo.get(i).get("entName"))) {
                        companyId = StringUtil.getString(entInfo.get(i).get("companyId"));
                        areaCode = StringUtil.getString(entInfo.get(i).get("areaCode"));
                        break;
                    }
                }
                if (StringUtil.isNotEmpty(companyId, entName)) {
                    // ????????????????????????
                    LocalDate monthLastDay = LocalDate.parse(endDayByMonth, DateTimeFormatter.ofPattern(DateUtil.DEFAULT_TIME_PATTERN));
                    // ????????????
                    LocalDate now = LocalDate.now();
                    for (String key : keySet) {
                        if (key.contains("_Data")) {
                            String value = tempMap.get(key).toString();
                            String valueDate = date + "-" + key.replace("_Data", "");
                            valueDate = valueDate.substring(0, valueDate.length() - 1) + " 00:00:00";
                            // ???excel???????????????
                            LocalDate excelValueDate = LocalDate.parse(valueDate, DateTimeFormatter.ofPattern(DateUtil.DEFAULT_TIME_PATTERN));
                            // ?????????????????????????????? && ??????????????????
                            if (excelValueDate.isAfter(monthLastDay) || excelValueDate.isAfter(now)) {
                                continue;
                            }
                            Map<String, Object> resMap = Maps.newHashMap();
                            resMap.put("date", valueDate);
                            resMap.put("companyId", companyId);
                            resMap.put("entName", entName);
                            resMap.put("areaCode", areaCode);
                            resMap.put("accnumber", accNumber);
                            resMap.put("energyType", "0");
                            resMap.put("userId", dataImportDto.getUserId());
                            //??????????????????
                            String jian = "1", feng = "2", gu = "3";
                            String endKey = key.substring(key.length() - 1);
                            if (jian.equals(endKey)) {
                                resMap.put("jian", value == null ? null : value.trim());
                            } else if (feng.equals(endKey)) {
                                resMap.put("feng", value == null ? null : value.trim());
                            } else if (gu.equals(endKey)) {
                                resMap.put("gu", value == null ? null : value.trim());
                            }
                            insertEntInfoDto.add(resMap);
                        }
                    }
                }
            }
            //???????????????????????????????????????
            List<Map<String, Object>> insertEntInfo = changeData(insertEntInfoDto);
            // ??????????????????????????????
            if (!CollectionUtils.isEmpty(insertEntInfo)) {
                batchInserInfo(insertEntInfo, "insert");
            }
            //??????value
            // iDataMaintenanceMapper.updateValueEleInfo(dataImportDto);
        //??????????????????
        updateEnergyInfo(dataImportDto);
    }


    /**
     * @Author:lio
     * @Description: ??????????????????
     * @Date :9:52 ?????? 2021/2/1
     */
    @Async
    public void entFuheExcelImport(MultipartFile multipartFile, ImportParams params, ExcelDataImportDTO dataImportDto, ExcelImportResult<ExcelFuHeMonthImport> excelData) {
        List<Map<String, Object>> localInfo = iDataMaintenanceMapper.getEntFuHeByImport(dataImportDto);
        List<Map<String, Object>> entInfo = govMapper.getAllEntInfo();
        String date = dataImportDto.getDate();
        String msg = "";
        String dataTypeCode = ENUM_DATA_TYPE.ZUIGAOFUHE.getCode();
        String userId = dataImportDto.getUserId();
        // ????????????????????????????????? ??????????????????
        if (!excelData.isVerfiyFail()) {
            List<Map<String, Object>> insertEntInfoDto = Lists.newArrayList();
            List<Map<String, Object>> finalLocalInfo = localInfo;
            excelData.getList().stream().forEach(k -> {
                Map<String, Object> tempMap = MapUtils.java2Map(k);
                Set<String> keySet = tempMap.keySet();
                String entName = k.getEntName();
                String companyId = "";
                String accNumber = k.getAccnumber();
                String areaCode = "";
                for (int i = 0; i < entInfo.size(); i++) {
                    if (entName.equals(entInfo.get(i).get("entName"))) {
                        companyId = StringUtil.getString(entInfo.get(i).get("companyId"));
                        if (dataTypeCode.equals(entInfo.get(i).get("type"))) {
                            areaCode = StringUtil.getString(entInfo.get(i).get("areaCode"));
                        }
                    }
                }
                if (StringUtil.isNotEmpty(companyId, entName)) {
                    for (String key : keySet) {
                        if (key.contains("_Data")) {
                            String finalCompanyId = companyId;
                            String value = tempMap.get(key).toString();
                            String valueDate = "";
                            valueDate = date + "-" + key.replace("_Data", "") + "-01 00:00:00";

                            // ????????????????????????????????????
                            LocalDate localDate = LocalDate.now();
                            int monthValue = localDate.getMonthValue();
                            int yearValue = localDate.getYear();
                            String updateMonth = key.replace("_Data", "");
                            if (Integer.parseInt(date) > yearValue || (Integer.parseInt(date) == yearValue && Integer.parseInt(updateMonth) > monthValue)) {
                                continue;
                            }
                            Map<String, Object> resMap = Maps.newHashMap();
                            resMap.put("date", valueDate);
                            resMap.put("value", StringUtil.getStringZero(value));
                            resMap.put("companyId", finalCompanyId);
                            resMap.put("energyType", dataTypeCode);
                            resMap.put("accNumber", accNumber);
                            resMap.put("entName", entName);
                            resMap.put("userId", userId);
                            resMap.put("areaCode", areaCode);
                            insertEntInfoDto.add(resMap);
                        }
                    }
                }
            });
            // ????????????????????????????????????????????????
            if (!CollectionUtils.isEmpty(insertEntInfoDto)) {
                batchInserInfo(insertEntInfoDto, "fuhe");
            }
        } else {
            List<ExcelFuHeMonthImport> failList = excelData.getFailList();
            if (!CollectionUtils.isEmpty(failList)) {
                for (ExcelFuHeMonthImport entity : failList) {
                    msg += "???" + entity.getRowNum() + "??????????????????" + entity.getErrorMsg() + "\n";
                }
                AssertUtil.ThrowSystemErr(msg);
            }
        }
        //??????????????????
        updateEnergyInfo(dataImportDto);
    }

    private List<Map<String, Object>> changeData(List<Map<String, Object>> infoList) {
        List<Map<String, Object>> mapList = Lists.newArrayList();
        if (!CollectionUtils.isEmpty(infoList)) {
            //????????????????????????
            Map<Object, List<Map<String, Object>>> accNumberList = infoList.stream().collect(Collectors.groupingBy(m -> m.get("accnumber")));
            accNumberList.forEach((number, list) -> {
                //???????????????????????????????????????
                Map<Object, List<Map<String, Object>>> dateList = list.stream().collect(Collectors.groupingBy(m -> m.get("date")));
                dateList.forEach((time, timeList) -> {
                    //??????????????????????????????????????????
                    Map<String, Object> insMap = Maps.newHashMap();
                    insMap.put("companyId", timeList.get(0).get("companyId"));
                    insMap.put("entName", timeList.get(0).get("entName"));
                    insMap.put("accnumber", number);
                    insMap.put("date", time);
                    insMap.put("userId", timeList.get(0).get("userId"));
                    insMap.put("areaCode", timeList.get(0).get("areaCode"));
                    insMap.put("rcvElecCap", checkExcelEleDayImportDTOExcelVerifyHandler.getEntRcvElecCapsMap().get(number));
                    for (int i = 0; i < timeList.size(); i++) {
                        Object feng = timeList.get(i).get("feng"), jian = timeList.get(i).get("jian"), gu = timeList.get(i).get("gu");
                        // ???????????????,0???????????????
                        if (ObjectUtil.isNotEmpty(feng) && !StringUtils.equals("null", feng.toString())) {
                            insMap.put("feng", feng);
                        }
                        if (ObjectUtil.isNotEmpty(jian) && !StringUtils.equals("null", jian.toString())) {
                            insMap.put("jian", jian);
                        }
                        if (ObjectUtil.isNotEmpty(gu) && !StringUtils.equals("null", gu.toString())) {
                            insMap.put("gu", gu);
                        }
                    }
                    // ??????date???????????????????????????
                    if (!(insMap.get("feng") == null && insMap.get("jian") == null && insMap.get("gu") == null)) {
                        // ??????????????????????????????(????????????,????????????) eg.
                        // 27??????	27??????	27??????
                        // 18920
                        insMap.put("feng", ObjectUtils.defaultIfNull(insMap.get("feng"), "0"));
                        insMap.put("jian", ObjectUtils.defaultIfNull(insMap.get("jian"), "0"));
                        insMap.put("gu", ObjectUtils.defaultIfNull(insMap.get("gu"), "0"));
                        mapList.add(insMap);
                    }
                });
            });
        }
        return mapList;
    }


    private int getAddEndDay(String dayOfWeek) {
        int addDay = 0;
        if ("MONDAY".equals(dayOfWeek)) {
            addDay = 6;
        } else if ("TUESDAY".equals(dayOfWeek)) {
            addDay = 5;
        } else if ("WEDNESDAY".equals(dayOfWeek)) {
            addDay = 4;
        } else if ("THURSDAY".equals(dayOfWeek)) {
            addDay = 3;
        } else if ("FRIDAY".equals(dayOfWeek)) {
            addDay = 2;
        } else if ("SATURDAY".equals(dayOfWeek)) {
            addDay = 1;
        }
        return addDay;
    }

    private int getAddStartDay(String dayOfWeek) {
        int addDay = 0;
        if ("TUESDAY".equals(dayOfWeek)) {
            addDay = -1;
        } else if ("WEDNESDAY".equals(dayOfWeek)) {
            addDay = -2;
        } else if ("THURSDAY".equals(dayOfWeek)) {
            addDay = -3;
        } else if ("FRIDAY".equals(dayOfWeek)) {
            addDay = -4;
        } else if ("SATURDAY".equals(dayOfWeek)) {
            addDay = -5;
        } else if ("SUNDAY".equals(dayOfWeek)) {
            addDay = -6;
        }
        return addDay;
    }

    public static void main(String[] args) {
        System.out.println(DateUtil.getEndDayByMonth("yyyy-MM-dd", DateUtil.getDateByStringDate("yyyy-MM-dd", "2020-01-02")));
    }


    public static LocalDate getLocalDateByStr(String str) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return LocalDate.parse(str, formatter);
    }


}
