package in.org.iudx.adaptor.codegen;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.HashMap;

import java.nio.file.*;
import java.net.URISyntaxException;
import java.io.IOException;
import java.lang.ClassLoader;

public class TopologyBuilderTest {

  @Test
  void singleContainerTopology() throws Exception {

    
    TopologyConfig top = new TopologyConfig(
            new String(
                    Files.readAllBytes(
                            Paths.get(
                                    "src/test/java/in/org/iudx/adaptor/codegen/single.json"))));

    TopologyBuilder builder = new TopologyBuilder(top);
    builder.gencode();

  }

  @Test
  void buildJSTopology() throws Exception {

    
    TopologyConfig top = new TopologyConfig(
                          new String(
                            Files.readAllBytes(
                              Paths.get(
                                "src/test/java/in/org/iudx/adaptor/codegen/jstop.json"))));

    TopologyBuilder builder = new TopologyBuilder(top);
    builder.gencode();

  }

  @Test
  void headersTop() throws Exception {

    
    TopologyConfig top = new TopologyConfig(
                          new String(
                            Files.readAllBytes(
                              Paths.get(
                                "src/test/java/in/org/iudx/adaptor/codegen/headersTop.json"))));

    TopologyBuilder builder = new TopologyBuilder(top);
    builder.gencode();

  }

  @Test
  void urlScripts() throws Exception {

    
    TopologyConfig top = new TopologyConfig(
                          new String(
                            Files.readAllBytes(
                              Paths.get(
                                "src/test/java/in/org/iudx/adaptor/codegen/url.json"))));

    TopologyBuilder builder = new TopologyBuilder(top);
    builder.gencode();

  }

  @Test
  void trickle() throws Exception {


    TopologyConfig top = new TopologyConfig(
            new String(
                    Files.readAllBytes(
                            Paths.get(
                                    "src/test/java/in/org/iudx/adaptor/codegen/trickle.json"))));

    TopologyBuilder builder = new TopologyBuilder(top);
    builder.gencode();

  }

  @Test
  void boundedJob() throws Exception {

    TopologyConfig top = new TopologyConfig(
            new String(
                    Files.readAllBytes(
                            Paths.get(
                                    "src/test/java/in/org/iudx/adaptor/codegen/bounded.json"))));

    TopologyBuilder builder = new TopologyBuilder(top);
    builder.gencode();

  }

  @Test
  void temp() throws Exception {

    TopologyConfig top = new TopologyConfig(
            new String(
                    Files.readAllBytes(
                            Paths.get(
                                    "src/test/java/in/org/iudx/adaptor/codegen/t.json"))));

    TopologyBuilder builder = new TopologyBuilder(top);
    builder.gencode();

  }
}
