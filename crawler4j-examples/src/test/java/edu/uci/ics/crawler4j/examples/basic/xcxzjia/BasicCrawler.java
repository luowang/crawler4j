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

package edu.uci.ics.crawler4j.examples.basic.xcxzjia;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 * @author Yasser Ganjisaffar
 */
public class BasicCrawler extends WebCrawler {

    private static final Pattern IMAGE_EXTENSIONS = Pattern.compile(".*\\.(bmp|gif|jpg|png)$");

    /**
     * You should implement this function to specify whether the given url
     * should be crawled or not (based on your crawling logic).
     */
    @Override
    public boolean shouldVisit(Page referringPage, WebURL url) {
        String href = url.getURL().toLowerCase();
        // Ignore the url if it has an extension that matches our defined set of image extensions.
        if (IMAGE_EXTENSIONS.matcher(href).matches()|| !href.startsWith("http://www.xcxzjia.com/shop/app")
                || !href.startsWith("http://www.xcxzjia.com/shop/app") ) {
            return false;
        }

        // Only accept the url if it is in the "www.ics.uci.edu" domain and protocol is "http".
        return href.startsWith("http://www.xcxzjia.com/");
    }

    /**
     * This function is called when a page is fetched and ready to be processed
     * by your program.
     */
    @Override
    public void visit(Page page) {
        final String url = page.getWebURL().getURL();
        logger.debug("-----------爬取路径：" + url);
        String domain = "http://www.xcxzjia.com";
        if (page.getParseData() instanceof HtmlParseData) {
            HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
            String html = htmlParseData.getHtml();
            final Document doc = Jsoup.parse(html);
            ArrayList<String> strings = new ArrayList<>();
            // 图片logo地址
            String imgStr = doc.select("div[class=img fl]").first().select("img").first().attr("src");
            strings.add(domain +imgStr);
            // 名字
            String nameStr = doc.select("h3[class=fl]").first().text();
            strings.add(nameStr);
            // 分类
            StringBuffer sb = new StringBuffer();
            Elements tagsElements = doc.select("div[class=dataList fl]").select("p");
                Elements label1 = tagsElements.get(1).select("label");
            for(int i=0;i<label1.size();i++){
                String tag1 = label1.get(i).select("a").text();
                sb.append(tag1).append(";");
            }
            strings.add(sb.toString());
            StringBuffer sb2 = new StringBuffer();
            // 标签
                Elements label2 = tagsElements.get(2).select("span");
            for(int i=0;i<label2.size();i++){
                String tag1 = label2.get(i).select("a").text();
                sb2.append(tag1).append(";");
            }
                strings.add(sb2.toString());
            // 二维码地址
            String qrcodeStr = doc.select("div[class=lite-code fr]").first().select("img").first().attr("src");
            strings.add(domain+qrcodeStr);
            // 上传时间
            String uploadTime = doc.select("div[class=lite-text fl]").first().select("div[class=fl]").get(1).select("p").get(1).text();
            strings.add(uploadTime);
            // 作者
            String authorStr = doc.select("div[class=lite-text fl]").first().select("div[class=fl]").get(1).select("p").get(0).text();
            strings.add(authorStr);
            // 查看要求
            String reqStr = doc.select("div[class=lite-text fl]").first().select("div[class=fl]").get(1).select("p").get(2).text();
            strings.add(reqStr);
            // 截图
            StringBuffer sb3 = new StringBuffer();
            Elements lis2 = doc.select("ul[class=imgsroll clear]").select("li");
            for(Element element:lis2){
                String href = element.select("img").attr("src");
                sb3.append(domain+href).append(";");
            }
            strings.add(sb3.toString());
            // 简介
            String introductStr = doc.select("div[class=detaltext]").select("p").get(7).text();
            strings.add(introductStr);
            // 来源
            String sourceUrl = page.getWebURL().getURL();
            strings.add(sourceUrl);
            // 上传到数据库

            //写excel文件

            String[] title = strings.toArray(new String[strings.size()]);
            createExcel("E:\\data\\crawl\\xcx\\xcxzjia2.xls","xcx_data",title);
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
