package com.example.kotlinstudy.week03;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Reader {
    static String readFirstLineFromFile(String path) throws IOException {
        try(BufferedReader br = new BufferedReader(new FileReader(path))) {
            return br.readLine();
        }
    }
}
