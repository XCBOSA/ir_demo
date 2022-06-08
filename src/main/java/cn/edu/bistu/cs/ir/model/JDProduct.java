package cn.edu.bistu.cs.ir.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JDProduct implements LucenePipelineAdaptableModel {
    private String sku;
    private String name;
    private String price;
    private String totalCommentCount;
    private String rate;
    private String description;
    private String url;

    @Override
    public String getKeyField() {
        return sku;
    }

    @Override
    public Document generateDocument() {
        Document document = new Document();
        document.add(new StringField("sku", sku, Field.Store.YES));
        document.add(new StringField("name", name, Field.Store.YES));
        document.add(new StringField("price", price, Field.Store.YES));
        document.add(new StringField("totalCommentCount", totalCommentCount, Field.Store.YES));
        document.add(new StringField("rate", rate, Field.Store.YES));
        document.add(new StringField("description", description, Field.Store.YES));
        document.add(new StringField("url", url, Field.Store.YES));
        return document;
    }
}