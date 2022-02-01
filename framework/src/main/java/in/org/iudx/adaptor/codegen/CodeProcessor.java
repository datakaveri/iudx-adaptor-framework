package in.org.iudx.adaptor.codegen;

import com.google.auto.service.AutoService;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import java.io.IOException;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;
import org.json.JSONObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import in.org.iudx.adaptor.codegen.ProcessingException;

import java.util.Map;



@AutoService(Processor.class)
public class CodeProcessor extends AbstractProcessor {


  private Types typeUtils;
  private Elements elementUtils;
  private Filer filer;
  private Messager messager;

  private static final Logger LOGGER = LogManager.getLogger(CodeProcessor.class);


  @Override public synchronized void init(ProcessingEnvironment processingEnv) {
  super.init(processingEnv);
  typeUtils = processingEnv.getTypeUtils();
  elementUtils = processingEnv.getElementUtils();
  filer = processingEnv.getFiler();
  messager = processingEnv.getMessager();
  }

  @Override public Set<String> getSupportedAnnotationTypes() {
  Set<String> annotations = new LinkedHashSet<String>();
  annotations.add(TopologyProperty.class.getCanonicalName());
  return annotations;
  }


  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    //  String env = System.getenv("ADAPTOR_CONFIG_PATH");      
    String env = System.getProperty("ADAPTOR_CONFIG_PATH");
    TopologyConfig config;

    try {
      /* NOTE: This must be a safe and standard path */
      config = new TopologyConfig(new String(Files.readAllBytes(Paths.get(env)),
            StandardCharsets.UTF_8));
      TopologyBuilder tb = new TopologyBuilder(config, filer);
      tb.gencode();
    } catch (Exception e) {
      LOGGER.debug("Exception in loading file: "+e);
      return false;
    }
    // Send to config parser
    // Send to JPoet and generate file


    return true;
  }

  /**
   * Prints an error message
   *
   * @param e The element which has caused the error. Can be null
   * @param msg The error message
   */
  public void error(Element e, String msg) {
    messager.printMessage(Diagnostic.Kind.ERROR, msg, e);
  }

}

