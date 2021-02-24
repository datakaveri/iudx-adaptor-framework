package in.org.iudx.adaptor.codegen;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.HashMap;

import in.org.iudx.adaptor.datatypes.Message;
import in.org.iudx.adaptor.codegen.ApiConfig;
import in.org.iudx.adaptor.codegen.SimpleATestParser;


import javax.annotation.processing.Processor;
import javax.tools.DiagnosticCollector;
import javax.tools.DiagnosticListener;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class CompilerTest {


  private static File createTempDir() throws IOException {
    File dir = Files.createTempDirectory("/tmp/codegen").toFile();
    // dir.deleteOnExit();
    return dir;
  }

  @Test
  void compile() throws InterruptedException {
    
    try {
      File dir = createTempDir();

    } catch (Exception e) {
    }
  }
}
