package io.lematech.httprunner4j.core.processor;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.JsonNode;
import io.lematech.httprunner4j.common.Constant;
import io.lematech.httprunner4j.common.DefinedException;
import io.lematech.httprunner4j.entity.http.ResponseEntity;
import io.lematech.httprunner4j.widget.log.MyLog;
import io.lematech.httprunner4j.widget.utils.JsonUtil;
import io.lematech.httprunner4j.widget.utils.RegExpUtil;

import java.util.*;

/**
 * @author lematech@foxmail.com
 * @version 1.0.0
 */
public class DataExtractor {

    private ExpProcessor expProcessor;
    private Map testContextVariable;

    public DataExtractor(ExpProcessor expProcessor) {
        this.expProcessor = expProcessor;
    }

    public DataExtractor(ExpProcessor expProcessor, Map testContextVariable) {
        this.expProcessor = expProcessor;
        this.testContextVariable = testContextVariable;
    }

    /**
     * Support regular, JSONPATH, JMESPATH, expression and other ways to extract parameters
     *
     * @param exp            expression language
     * @param responseEntity response entity
     * @return The object after processing
     */
    public Object handleExpDataExtractor(Object exp, ResponseEntity responseEntity) {
        if (!(exp instanceof String) || Objects.isNull(exp)) {
            return exp;
        }
        String expStr = String.valueOf(exp);
        if (StrUtil.isEmpty(expStr)) {
            return "";
        }
        Object dataExtractorValue;
        String responseStr = JSON.toJSONString(responseEntity);


        if (RegExpUtil.isExp(expStr)) {
            dataExtractorValue = expProcessor.handleStringExp(expStr);
        } else if (expStr.startsWith(Constant.DATA_EXTRACTOR_REGEX_START) && expStr.endsWith(Constant.DATA_EXTRACTOR_REGEX_END)) {
            String expression = expStr.substring(1, expStr.length() - 1);
            dataExtractorValue = RegExpUtil.findString(expression, responseStr);
        } else if (expStr.startsWith(Constant.DATA_EXTRACTOR_JSONPATH_START)) {
            dataExtractorValue = JsonUtil.getJsonPathResult(expStr, responseStr);
        } else {
            dataExtractorValue = JsonUtil.getJmesPathResult(expStr, responseStr);
        }
        return dataExtractorValue;
    }


    /**
     * Extract data according to the extraction rules
     * @param extracts extracted data
     * @param responseEntity The response entity
     * @param variable variable map
     */
    public void extractVariables(Object extracts, ResponseEntity responseEntity, Map<String, Object> variable) {
        if (Objects.isNull(extracts)) {
            return;
        }
        Class clz = extracts.getClass();
        MyLog.debug("Data extractor type : {}", clz);
        if (clz == ArrayList.class) {
            List<Map<String, String>> extractList = (List<Map<String, String>>) extracts;
            for (Map extractMap : extractList) {
                extractMap(responseEntity, extractMap, variable);
            }
        } else if (clz == Map.class || clz == LinkedHashMap.class) {
            Map extractMap = (Map) extracts;
            extractMap(responseEntity, extractMap, variable);
        } else {
            String exceptionMsg = String.format("The current extractor of this type %s is not supported", clz);
            throw new DefinedException(exceptionMsg);
        }
    }

    /**
     * Extract as a key-value pair
     *
     * @param responseEntity
     * @param extractMap
     * @param testStepConfigVariable
     */
    private void extractMap(ResponseEntity responseEntity, Map<String, Object> extractMap, Map<String, Object> testStepConfigVariable) {
        for (Map.Entry<String, Object> entry : extractMap.entrySet()) {
            String key = entry.getKey();
            if (Objects.isNull(entry.getValue())) {
                String exceptionMsg = String.format("The data extraction rule cannot be empty");
                throw new DefinedException(exceptionMsg);
            }
            Object expValue = entry.getValue();
            String extractValue = (String) handleExpDataExtractor(expValue, responseEntity);
            if (extractValue.equals(expValue)) {
                String exceptionMsg = String.format("By extracting the data that the rule %s does not match to the rulel", expValue);
                throw new DefinedException(exceptionMsg);
            }
            MyLog.debug("Extract rule %s, the extracted data value:%s", expValue, extractValue);
            testContextVariable.put(key, extractValue);
        }

    }
}
