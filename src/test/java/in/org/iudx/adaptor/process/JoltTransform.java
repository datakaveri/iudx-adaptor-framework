package in.org.iudx.adaptor.process;


import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.HashMap;

import com.bazaarvoice.jolt.Chainr;
import com.bazaarvoice.jolt.JsonUtils;

import java.io.IOException;
import java.util.List;

public class JoltTransform {

  @Test
  void transform() throws InterruptedException {

    // How to access the test artifacts, i.e. JSON files
    //  JsonUtils.classpathToList : assumes you put the test artifacts in your class path
    //  JsonUtils.filepathToList : you can use an absolute path to specify the files

    List chainrSpecJSON = JsonUtils.filepathToList("src/test/java/in/org/iudx/adaptor/process/spec.json");
    Chainr chainr = Chainr.fromSpec( chainrSpecJSON );

    Object inputJSON = JsonUtils.filepathToObject("src/test/java/in/org/iudx/adaptor/process/input.json" );

    Object transformedOutput = chainr.transform( inputJSON );
    System.out.println("\n");
    System.out.println( JsonUtils.toJsonString( transformedOutput ) );
    System.out.println("\n");
  }
}
