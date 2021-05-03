package io.lematech.httprunner4j.cli.commands;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.URLUtil;
import com.google.common.collect.Maps;
import io.lematech.httprunner4j.cli.Command;
import io.lematech.httprunner4j.cli.har.HarUtils;
import io.lematech.httprunner4j.cli.har.model.Har;
import io.lematech.httprunner4j.cli.har.model.HarEntry;
import io.lematech.httprunner4j.cli.har.model.HarPage;
import io.lematech.httprunner4j.cli.har.model.HarRequest;
import io.lematech.httprunner4j.common.DefinedException;
import io.lematech.httprunner4j.entity.http.RequestEntity;
import io.lematech.httprunner4j.entity.testcase.Config;
import io.lematech.httprunner4j.entity.testcase.TestStep;
import io.lematech.httprunner4j.widget.log.MyLog;
import io.lematech.httprunner4j.widget.utils.FilesUtil;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author lematech@foxmail.com
 * @version 1.0.0
 * @className Har2Yml
 * @description The <code>har2case</code> command.
 * @created 2021/4/18 7:53 下午
 * @publicWechat lematech
 */
public class Har2Case extends Command {
    @Option(name = "--file", usage = "Specify the HAR file path.")
    File harFile;
    @Argument
    String format;

    @Override
    public String description() {
        return "Print har2yml command information.";
    }

    @Override
    public int execute(PrintWriter out, PrintWriter err) {
        FilesUtil.checkFileExists(harFile);
        MyLog.info("Start generating test cases,testcase format:{}", format);
        Har har;
        try {
            har = HarUtils.read(harFile);
        } catch (Exception e) {
            String exceptionMsg = String.format("Error reading HAR file:%s,Exception information:%s", FilesUtil.getCanonicalPath(harFile), e.getMessage());
            MyLog.error(exceptionMsg);
            return 1;
        }
        if (Objects.isNull(har.getLog())) {
            String exceptionMsg = String.format("HAR file %s has no pages!", FilesUtil.getCanonicalPath(harFile));
            MyLog.error(exceptionMsg);
            return 1;
        }

        Config config = new Config();
        config.setName("Testcase descritpion");
        config.setVariables(Maps.newHashMap());
        config.setVerify(false);
        List<TestStep> testSteps = new ArrayList<>();
        List<HarPage> harPages = har.getLog().getPages();
        MyLog.info("Number of pages viewed: " + harPages.size());
        for (HarPage page : harPages) {
            MyLog.info(page.toString());
            MyLog.info("Output the calls for this page: ");
            for (HarEntry entry : page.getEntries()) {
                TestStep testStep = new TestStep();
                HarRequest request = entry.getRequest();
                MyLog.info("\t" + entry);
                testStep.setName(String.format("Request api:%s", URLUtil.getPath(request.getUrl())));
                RequestEntity requestEntity = new RequestEntity();
                requestEntity.setMethod(request.getMethod());
                requestEntity.setUrl(request.getUrl());
                //requestEntity.setHeaders(request.getHeaders());
                requestEntity.setCookies(request.getCookies());
                testStep.setRequest(requestEntity);
            }
        }
        return 0;
    }


}
