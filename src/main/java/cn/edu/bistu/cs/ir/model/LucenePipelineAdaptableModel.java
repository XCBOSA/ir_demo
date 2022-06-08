package cn.edu.bistu.cs.ir.model;

import org.apache.lucene.document.Document;

public interface LucenePipelineAdaptableModel {
    String getKeyField();
    Document generateDocument();
}
