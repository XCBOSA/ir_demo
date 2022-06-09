package cn.edu.bistu.cs.ir.crawler;

import cn.edu.bistu.cs.ir.model.JDProduct;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.selector.Html;
import us.codecraft.webmagic.selector.JsonPathSelector;

import java.util.List;

public class JDMailCrawler implements PageProcessor {

    @Getter private Site site;

    private static final Logger log = LoggerFactory.getLogger(JDMailCrawler.class);

    public JDMailCrawler(Site site) {
        this.site = site;
    }

    @Override
    public void process(Page page) {
        String url = page.getRequest().getUrl();
        if (url.startsWith("https://list.jd.com/list.html")) {
            // 对于目录页
            List<Request> products = page.getHtml()
                    .xpath("//div[@id='J_goodsList']//ul/li/@data-sku")
                    .all()
                    .stream().filter(s -> s.length() > 0)
                    .map(s -> {
                        Request request = new Request("https://item.jd.com/" + s + ".html");
                        request.putExtra("sku", s);
                        request.putExtra("price", page.getHtml().xpath("//i[@data-price='" + s + "']/text()").get());
                        return request;
                    })
                    .toList();
            log.info("解析目录成功 {} 页", products.size());

            products.forEach(v -> page.addTargetRequest(v));
            //page.addTargetRequest(products.get(0));

            page.setSkip(true);
        } else if (url.startsWith("https://item.jd.com/")) {
            // 详情页
            log.info("解析内容页 {}", url);
            String id = url.replace("https://item.jd.com/", "").replace(".html", "");
            Html html = page.getHtml();
            String name = html.xpath("//div[@class='sku-name']/text()").get();
            String price = page.getRequest().getExtra("price");
            String description = html.xpath("//ul[@class='parameter2 p-parameter-list']/html()").get();

            JDProduct model = new JDProduct();
            model.setUrl(url);
            model.setName(name);
            model.setPrice(price);
            model.setDescription(description);
            model.setSku(page.getRequest().getExtra("sku"));
            Request request = new Request("https://club.jd.com/comment/productCommentSummaries.action?referenceIds=" + page.getRequest().getExtra("sku"));
            request.putExtra("model", model);
            page.addTargetRequest(request);
            page.setSkip(true);
        } else {
            // JS接口
            log.info("解析JSON接口 {}", url);
            JDProduct model = page.getRequest().getExtra("model");
            String commentCount = "";
            try {
                commentCount = new JsonPathSelector("$.CommentsCount[0].CommentCountStr").select(page.getRawText());
            } catch (Exception e) { }
            String rate = "";
            try {
                rate = new JsonPathSelector("$.CommentsCount[0].GoodRate").select(page.getRawText());
            } catch (Exception e) { }
            model.setTotalCommentCount(commentCount);
            model.setRate(rate);
            page.putField("MODEL", model);
        }
    }

    @Override
    public Site getSite() {
        return site;
    }
}
