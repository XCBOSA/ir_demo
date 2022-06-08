package cn.edu.bistu.cs.ir.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JDProduct {
    private String name;
    private String price;
    private String totalCommentCount;
    private String rate;
    private String description;
    private String url;
}
