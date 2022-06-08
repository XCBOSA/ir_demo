package cn.edu.bistu.cs.ir.index;

import cn.edu.bistu.cs.ir.crawler.SinaBlogCrawler;
import cn.edu.bistu.cs.ir.model.Blog;
import cn.edu.bistu.cs.ir.model.LucenePipelineAdaptableModel;
import org.apache.lucene.document.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

/**
 * 基于Lucene的WebMagic Pipeline,
 * 用于将抓取的数据写入本地的Lucene索引
 * @author ruoyuchen
 */
public class LucenePipeline implements Pipeline {

    private static final Logger log = LoggerFactory.getLogger(LucenePipeline.class);

    private final IdxService idxService;
    public LucenePipeline(IdxService idxService){
        log.info("初始化LucenePipeline模块");
        this.idxService = idxService;
    }

    @Override
    public void process(ResultItems resultItems, Task task) {
        LucenePipelineAdaptableModel model = resultItems.get(SinaBlogCrawler.RESULT_ITEM_KEY);
        if(model==null){
            log.error("无法从爬取的结果中提取到Blog对象");
            return;
        }
        String id = model.getKeyField();
        Document doc = model.generateDocument();
        boolean result = idxService.addDocument("ID", id, doc);
        if(!result){
            log.error("无法将ID为[{}]的博客内容写入索引", id);
        }
    }
}
