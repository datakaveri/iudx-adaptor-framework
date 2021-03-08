package in.org.iudx.adaptor.source;

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

public class DateFormatTest {

  @Test
  void parseA() throws InterruptedException {

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
}
