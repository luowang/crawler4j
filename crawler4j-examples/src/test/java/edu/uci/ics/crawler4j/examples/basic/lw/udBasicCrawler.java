/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.uci.ics.crawler4j.examples.basic.lw;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;
import org.apache.http.Header;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * @author Yasser Ganjisaffar
 */
public class udBasicCrawler extends WebCrawler {

    private static final Pattern IMAGE_EXTENSIONS = Pattern.compile(".*\\.(bmp|gif|jpg|png)$");

    /**
     * You should implement this function to specify whether the given url
     * should be crawled or not (based on your crawling logic).
     */
    @Override
    public boolean shouldVisit(Page referringPage, WebURL url) {
        String href = url.getURL().toLowerCase();
        // Ignore the url if it has an extension that matches our defined set of image extensions.
        if (IMAGE_EXTENSIONS.matcher(href).matches()|| !href.startsWith("http://www.91ud.com/app")) {
            return false;
        }

        // Only accept the url if it is in the "www.ics.uci.edu" domain and protocol is "http".
        return href.startsWith("http://www.91ud.com/");
    }

    /**
     * This function is called when a page is fetched and ready to be processed
     * by your program.
     */
    @Override
    public void visit(Page page) {
        final String url = page.getWebURL().getURL();
        logger.debug("-----------爬取路径：" + url);

        if (page.getParseData() instanceof HtmlParseData) {
            HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
            String html = htmlParseData.getHtml();
            final Document doc = Jsoup.parse(html);
            ArrayList<String> strings = new ArrayList<>();
            Element element1 = doc.select("div[class=intro clearfix]").first();
            // 图片logo地址
            String imgStr = element1.select("img[class=avatar]").first().attr("src");
            strings.add(imgStr);
            // 名字
            String nameStr = element1.select("div[class=name]").first().select("h1").first().text();
            strings.add(nameStr);
            // 标签
            StringBuffer sb = new StringBuffer();
            Elements tagsElements = element1.select("div[class=tags]").first().select("a");
            for(Element element:tagsElements){
                String text1 = element.text();
                sb.append(text1).append(";");
            }
            String tagStr = sb.toString();
            strings.add(tagStr);
            // 二维码地址
            String qrcodeStr = doc.select("div[class=qrcode]").first().select("img").first().attr("src");
            strings.add(qrcodeStr);
            Elements lis = doc.select("ul[class=info]").first().select("li");
                Elements spans = lis.get(0).select("span");
                    // 分类
                    String categoryStr=spans.get(0).select("strong").first().select("a").text();
                    strings.add(categoryStr);
                    // 上传时间
                    String uploadTime=spans.get(1).select("strong").first().text();
                     strings.add(uploadTime);
                Elements spans1 = lis.get(1).select("span");
                    // 作者
                    String authorStr= spans1.get(0).select("strong").first().text();
            strings.add(authorStr);
                    // 查看要求
                    String reqStr=spans1.get(1).select("strong").first().text();
            strings.add(reqStr);
            // 截图
            StringBuffer sb2 = new StringBuffer();
            Elements lis2 = doc.select("div[class=screenshot]").first().select("ul[class=screenshot-list clearfix]").first().select("li");
            for(Element element:lis2){
                String href = element.select("a").attr("href");
                sb2.append(href).append(";");
            }
            String screenStr=sb2.toString();
            strings.add(screenStr);
            // 简介
            String introductStr = doc.select("div[class=description marked]").first().select("p").text();
            strings.add(introductStr);
            // 来源
            String sourceUrl = page.getWebURL().getURL();
            strings.add(sourceUrl);
            // 上传到数据库

            //写excel文件

            String[] title = strings.toArray(new String[strings.size()]);
            createExcel("E:/test3.xls","xcx_data",title);
        }
        logger.debug("=============");
    }
    /**
     * 创建新excel.
     * @param fileDir  excel的路径
     * @param sheetName 要创建的表格索引
     * @param titleRow excel的第一行即表格头
     */
    public void createExcel(String fileDir,String sheetName,String titleRow[]){
        //新建文件
        FileOutputStream out = null;
        try {
        FileInputStream fs=new FileInputStream(fileDir);  //获取d://test.xls
        POIFSFileSystem ps=new POIFSFileSystem(fs);  //使用POI提供的方法得到excel的信息
        HSSFWorkbook wb=new HSSFWorkbook(ps);
        HSSFSheet sheet=wb.getSheetAt(0);  //获取到工作表，因为一个excel可能有多个工作表
        HSSFRow row=sheet.getRow(0);  //获取第一行（excel中的行默认从0开始，所以这就是为什么，一个excel必须有字段列头），即，字段列头，便于赋值
//        System.out.println(sheet.getLastRowNum()+" "+row.getLastCellNum());  //分别得到最后一行的行号，和一条记录的最后一个单元格
        out=new FileOutputStream(fileDir);  //向d://test.xls中写数据
        row=sheet.createRow((short)(sheet.getLastRowNum()+1)); //在现有行号后追加数据
            for(short i = 0;i < titleRow.length;i++){
                row.createCell(i).setCellValue(titleRow[i]); //设置第一个（从0开始）单元格的数据
            }
            out.flush();
            wb.write(out);
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
