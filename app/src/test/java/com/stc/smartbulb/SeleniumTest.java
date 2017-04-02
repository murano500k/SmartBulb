package com.stc.smartbulb;

import android.support.test.uiautomator.By;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

import static android.content.ContentValues.TAG;

/**
 * Created by artem on 3/31/17.
 */
public class SeleniumTest {


    String BASE_URL = "http://172.22.89.65/litecart/admin/";
    @Test
    public void testSelenium() {

        driver.get("http://localhost/litecart/admin/login.php");
        driver.findElement(By.name("username")).sendKeys("admin");
        driver.findElement(By.name("password")).sendKeys("admin");
        driver.findElement(By.name("login")).click();
    }
    @Test
    public void testJsoup() {

/*  <ul id="box-apps-menu" class="list-vertical">
*/
        Document doc = null;
        try {
            doc = Jsoup.connect(BASE_URL).get();
        } catch (IOException e) {
            Log.e(TAG, "testSelenium: ", e);
            System.err.println("doc: "+ e);

        }
        Assert.assertNotNull(doc);
        System.out.println("doc: "+ doc);
        Elements elements = doc.body().getAllElements();
        for(Element e: elements){
            System.out.println(e.id());

            if(e.className().contains("input-wrapper")) e.append("admin");
            else if(e.text()!=null && e.text().contains("submit"))  e.
        }


        /*
        Elements links = doc.select("a[href]");
        Elements media = doc.select("[src]");
        Elements imports = doc.select("link[href]");

        print("\nMedia: (%d)", media.size());
            for (Element src : media) {
        if (src.tagName().equals("img"))
            print(" * %s: <%s> %sx%s (%s)",
                    src.tagName(), src.attr("abs:src"), src.attr("width"), src.attr("height"),
                    trim(src.attr("alt"), 20));
        else
            print(" * %s: <%s>", src.tagName(), src.attr("abs:src"));
    }

    print("\nImports: (%d)", imports.size());
            for (Element link : imports) {
        print(" * %s <%s> (%s)", link.tagName(),link.attr("abs:href"), link.attr("rel"));
    }

    print("\nLinks: (%d)", links.size());
            for (Element link : links) {
        print(" * a: <%s>  (%s)", link.attr("abs:href"), trim(link.text(), 35));
    }*/
    }


    private static void print(String msg, Object... args) {
        System.out.println(String.format(msg, args));
    }

    private static String trim(String s, int width) {
        if (s.length() > width)
            return s.substring(0, width-1) + ".";
        else
            return s;
    }
}
