package com.xck.jdk.file.pipe;

import com.xck.jdk.file.config.*;
import com.xck.jdk.file.writer.ObjectWriterFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author xuchengkun
 * @date 2021/11/26 16:21
 **/
public class SubmitService {

    private ObjPipelines submitDataDealers;
    private Map<String, ObjPipeline> submitDataReDealers;

    public SubmitService(SubmitConfig submitConfig) {
        List<SubmitConfigItem> configItems = submitConfig.getConfigItems();
        this.submitDataDealers = ObjPipelines.create(configItems.size());
        this.submitDataReDealers = new HashMap<>(configItems.size());
        for (SubmitConfigItem item : configItems) {
            submitDataDealers.pipeline(ObjPipeline.create(4)
                    .pipe(new SubmitDataDealer(item))
                    .pipe(new ZipDealer(item, "report"))
                    .pipe(item.getObjConverter())
                    .pipe(ObjectWriterFactory.fileWriter(item.getFileFormat(), Report.class)));

            submitDataReDealers.put(item.getName(), ObjPipeline.create(4)
                    .pipe(new SubmitDataDealer(item))
                    .pipe(new ZipReRunDealer(item, "re_report"))
                    .pipe(item.getObjConverter())
                    .pipe(ObjectWriterFactory.fileWriter(item.getFileFormat(), Report.class)));
        }
    }

    public void startTask() throws Exception {
        submitDataDealers.start();
    }

    public void startTask(Object input) throws Exception {
        submitDataDealers.start(input);
    }

    public void deal(Message message) throws Exception {
        submitDataDealers.deal(message);
    }

    public void endTask() throws Exception {
        submitDataDealers.close();
    }

    public Map<String, ObjPipeline> getSubmitDataReDealers() {
        return submitDataReDealers;
    }
}
