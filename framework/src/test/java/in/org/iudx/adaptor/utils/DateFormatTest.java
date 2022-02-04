package in.org.iudx.adaptor.utils;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.HashMap;


import java.time.Instant;

import java.text.SimpleDateFormat;
import java.text.DateFormat;
import java.util.TimeZone;
import java.util.Date;

public class DateFormatTest {


  DateFormat fromFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

  DateFormat toFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");

  @Test
  void getDate() throws InterruptedException {

  toFormat.setTimeZone(TimeZone.getTimeZone("IST"));

    try {
      System.out.println(toFormat.format(new Date()));
    } catch (Exception e) {
      System.out.println(e);
    }

  }


  @Test
  void parseA() throws InterruptedException {

    fromFormat.setTimeZone(TimeZone.getTimeZone("IST"));
    toFormat.setTimeZone(TimeZone.getTimeZone("IST"));

    String testTime = "2021-03-01 13:10:10";

    DateFormat fromFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    fromFormat.setTimeZone(TimeZone.getTimeZone("IST"));

    DateFormat toFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
    toFormat.setTimeZone(TimeZone.getTimeZone("IST"));

    try {
      System.out.println(toFormat.format(fromFormat.parse(testTime)));
    } catch (Exception e) {
      System.out.println(e);
    }

  }

  @Test
  void parseB() throws InterruptedException {

    fromFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SS");
    fromFormat.setTimeZone(TimeZone.getTimeZone("IST"));
    toFormat.setTimeZone(TimeZone.getTimeZone("IST"));

    String testTime = "2022-02-02T11:28:18";

    fromFormat.setTimeZone(TimeZone.getTimeZone("IST"));

    DateFormat toFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
    toFormat.setTimeZone(TimeZone.getTimeZone("IST"));

    try {
      System.out.println(toFormat.format(fromFormat.parse(testTime)));
    } catch (Exception e) {
      System.out.println(e);
    }

  }
}
