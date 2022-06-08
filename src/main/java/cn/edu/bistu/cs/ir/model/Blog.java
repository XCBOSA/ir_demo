package cn.edu.bistu.cs.ir.model;

import lombok.Getter;
import lombok.Setter;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;

import java.util.List;

/**
 * 面向新浪博客的模型类
 * @author chenruoyu
 */
@Getter
@Setter
public class Blog implements LucenePipelineAdaptableModel {

    /**
     * 页面的唯一ID
     */
    private String id;

    /**
     * 标题
     */
    private String title;

    /**
     * 日期
     */
    private long date;

    /**
     * 正文内容
     */
    private String content;

    /**
     * 作者
     */
    private String author;

    /**
     * 标签
     */
    private List<String> tags;

    @Override
    public String getKeyField() {
        return id;
    }

    @Override
    public Document generateDocument() {
        Document document = new Document();
        document.add(new StringField("ID", getId(), Field.Store.YES));
        document.add(new TextField("TITLE", getTitle(), Field.Store.YES));
        document.add(new TextField("CONTENT", getContent(), Field.Store.YES));
        return document;
    }
}
