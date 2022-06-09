package cn.edu.bistu.cs.ir.index;

import cn.edu.bistu.cs.ir.config.Config;
import cn.edu.bistu.cs.ir.utils.StringUtil;
import com.hankcs.hanlp.corpus.occurrence.TermOccurrence;
import kotlin.Pair;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.xml.builders.RangeQueryBuilder;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * 面向<a href="https://lucene.apache.org/">Lucene</a>
 * 索引读、写的服务类
 * @author chenruoyu
 */
@Component
public class IdxService implements DisposableBean {

    private static final Logger log = LoggerFactory.getLogger(IdxService.class);

    private static final Class<? extends Analyzer> DEFAULT_ANALYZER = StandardAnalyzer.class;

    private IndexWriter writer;

    public IdxService(@Autowired Config config) throws Exception {
        Analyzer analyzer = DEFAULT_ANALYZER.getConstructor().newInstance();
        Directory index;
        try {
            index = FSDirectory.open(Paths.get(config.getIdx()));
            IndexWriterConfig writerConfig = new IndexWriterConfig(analyzer);
            writer = new IndexWriter(index, writerConfig);
            log.info("索引初始化完成，索引目录为:[{}]", config.getIdx());
        } catch (IOException e) {
            e.printStackTrace();
            log.error("无法初始化索引，请检查提供的索引目录是否可用:[{}]", config.getIdx());
            writer = null;
        }
    }

    public boolean addDocument(String idFld, String id, Document doc){
        if(writer==null||doc==null){
            log.error("Writer对象或文档对象为空，无法添加文档到索引中");
            return false;
        }
        if(StringUtil.isEmpty(idFld)||StringUtil.isEmpty(id)){
            log.error("ID字段名或ID字段值为空，无法添加文档到索引中");
            return false;
        }
        try {
            writer.updateDocument(new Term(idFld, id), doc);
            writer.commit();
            log.info("成功将ID为[{}]的文档加入索引", id);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            log.error("构建索引失败");
            return false;
        }
    }

    /**
     * 根据关键词对索引内容进行检索，并将检索结果返回
     * @param kw 待检索的关键词
     * @return 检索得到的文档列表
     */
    public List<Document> queryByKw(String kw) throws Exception{
        //打开准实时索引Reader
        DirectoryReader reader = DirectoryReader.open(writer);
        IndexSearcher searcher = new IndexSearcher(reader);
        Analyzer analyzer = DEFAULT_ANALYZER.getConstructor().newInstance();
        QueryParser parser = new QueryParser("TITLE", analyzer);
        Query query = parser.parse(kw);
        TopDocs docs = searcher.search(query, 10);
        ScoreDoc[] hits = docs.scoreDocs;
        List<Document> results = new ArrayList<>();
        for (ScoreDoc doc : hits) {
            results.add(searcher.doc(doc.doc));
        }
        return results;
    }

    //TODO 请大家在这里添加更多的检索函数，如针对发表时间的范围检索等，
    // 添加了检索函数后，还需要相应地在Controller中添加接口

    public int queryDocumentCount() throws Exception {
        DirectoryReader reader = DirectoryReader.open(writer);
        return reader.numDocs();
    }

    public List<Pair<String, String>> queryByName(String name) throws Exception {
        DirectoryReader reader = DirectoryReader.open(writer);
        IndexSearcher searcher = new IndexSearcher(reader);
        Query query = new WildcardQuery(new Term("name", "*" + name + "*"));
        TopDocs docs = searcher.search(query, 10);
        ScoreDoc[] hits = docs.scoreDocs;
        List<Document> results = new ArrayList<>();
        for (ScoreDoc doc : hits) {
            results.add(searcher.doc(doc.doc));
        }
        return results
                .stream()
                .map(t -> new Pair<>(t.get("name"), t.get("sku")))
                .toList();
    }

    public List<Pair<String, String>> queryByMoneyRange(float min, float max) throws Exception {
        DirectoryReader reader = DirectoryReader.open(writer);
        IndexSearcher searcher = new IndexSearcher(reader);
        Query query = FloatPoint.newRangeQuery("price", min, max);
        TopDocs docs = searcher.search(query, 10);
        ScoreDoc[] hits = docs.scoreDocs;
        List<Document> results = new ArrayList<>();
        for (ScoreDoc doc : hits) {
            results.add(searcher.doc(doc.doc));
        }
        return results
                .stream()
                .map(t -> new Pair<>(t.get("name"), t.get("sku")))
                .toList();
    }

    public List<Pair<String, String>> queryByNameAndDescriptionBooleanQuery(String name, String description) throws Exception {
        DirectoryReader reader = DirectoryReader.open(writer);
        IndexSearcher searcher = new IndexSearcher(reader);
        BooleanQuery booleanQuery = new BooleanQuery.Builder()
                .add(new WildcardQuery(new Term("name", "*" + name + "*")), BooleanClause.Occur.MUST)
                .add(new WildcardQuery(new Term("description", "*" + description + "*")), BooleanClause.Occur.MUST)
                .build();
        TopDocs docs = searcher.search(booleanQuery, 10);
        ScoreDoc[] hits = docs.scoreDocs;
        List<Document> results = new ArrayList<>();
        for (ScoreDoc doc : hits) {
            results.add(searcher.doc(doc.doc));
        }
        return results
                .stream()
                .map(t -> new Pair<>(t.get("name"), t.get("sku")))
                .toList();
    }

    public List<Pair<String, String>> queryByNameAndDescriptionQueryParser(String name, String description) throws Exception {
        DirectoryReader reader = DirectoryReader.open(writer);
        IndexSearcher searcher = new IndexSearcher(reader);

        Analyzer analyzer = DEFAULT_ANALYZER.getConstructor().newInstance();
        Query query = MultiFieldQueryParser.parse(new String[] { name, description }, new String[] { "name", "description" }, analyzer);

        TopDocs docs = searcher.search(query, 10);
        ScoreDoc[] hits = docs.scoreDocs;
        List<Document> results = new ArrayList<>();
        for (ScoreDoc doc : hits) {
            results.add(searcher.doc(doc.doc));
        }
        return results
                .stream()
                .map(t -> new Pair<>(t.get("name"), t.get("sku")))
                .toList();
    }

    @Override
    public void destroy(){
        if(this.writer==null){
            return;
        }
        try {
            log.info("索引关闭");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
            log.info("尝试关闭索引失败");
        }
    }
}
