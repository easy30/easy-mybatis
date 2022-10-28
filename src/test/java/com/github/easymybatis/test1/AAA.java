package com.github.easymybatis.test1;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Tolerate;

@Data

public class AAA {
    private BBB bbb;//=new BBB();

    @Data
    @Builder
    public static class BBB{
        @Tolerate
        public BBB(){

        }
        private String name;
    }
}
