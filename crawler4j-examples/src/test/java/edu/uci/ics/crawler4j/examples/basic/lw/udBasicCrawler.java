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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

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
            Element element1 = doc.select("div[class=intro clearfix]").first();
            // 图片logo地址
            String imgStr = element1.select("img[class=avatar]").first().attr("src");
            // 名字
            String nameStr = element1.select("div[class=name]").first().select("h1").first().text();
            // 标签
            StringBuffer sb = new StringBuffer();
            Elements tagsElements = element1.select("div[class=tags]").first().select("a");
            for(Element element:tagsElements){
                String text1 = element.text();
                sb.append(text1).append(";");
            }
            String tagStr = sb.toString();
            // 二维码地址
            String qrcodeStr = doc.select("div[class=qrcode]").first().select("img").first().attr("src");

            Elements lis = doc.select("ul[class=info]").first().select("li");
                Elements spans = lis.get(0).select("span");
                    // 分类
                    String categoryStr=spans.get(0).select("strong").first().select("a").text();
                    // 上传时间
                    String uploadTime=spans.get(1).select("strong").first().select("a").text();
                Elements spans1 = lis.get(1).select("span");
                    // 作者
                    String authorStr= spans1.get(0).select("strong").first().select("a").text();
                    // 查看要求
                    String reqStr=spans1.get(1).select("strong").first().select("a").text();
            // 截图
            StringBuffer sb2 = new StringBuffer();
            Elements lis2 = doc.select("div[class=screenshot]").first().select("ul[class=screenshot-list clearfix]").first().select("li");
            for(Element element:lis2){
                String href = element.select("a").attr("href");
                sb2.append(href).append(";");
            }
            String screenStr=sb2.toString();
            // 简介
            String introductStr = doc.select("div[class=description marked]").first().select("p").text();
            // 来源
            String sourceUrl = page.getWebURL().getURL();

            // 上传到数据库



        }


        logger.debug("=============");
    }
}
