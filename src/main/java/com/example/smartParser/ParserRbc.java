package com.example.smartParser;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
public class ParserRbc  {
    private String fileName;
    ParserRbc (String name) {
        this.fileName = name;
    };
    public boolean doParsing () throws IOException {
        List <String> records = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(
                new FileReader(fileName)
        )) {
            String line; int cnt = 0;
            while ((line = br.readLine()) != null) {
                String [] values = line.split(",");
                Document doc = Jsoup.connect(
                        "https://www.rbc.ru/rbcfreenews/"+
                                values[0].substring(0,24)
                ).get();
                String title = doc.title();
                String body = doc.getElementsByClass("article__text__overview").text();
                records.add(values[0]+","+title+","+body+"");
                cnt ++;
                if (cnt > 50) break;
            }
        }
        File csvOutputfile = new File("./src/main/resources/static/test_id_txt.csv");
        try (PrintWriter pw = new PrintWriter(csvOutputfile)) {
            records.stream()
                    .forEach(pw::println);
        }
        return true;
    }
}
