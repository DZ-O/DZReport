package com.dz.eToSQL;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class EToSQLApplicationTests {

    @Test
    void contextLoads() {

    }

    public static void main(String[] args) {
        String str = ",,,,";
        String[] split = str.split(",",-1);
        System.out.println(split.length);
    }

}
