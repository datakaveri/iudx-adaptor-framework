package in.org.iudx.adaptor.codegen;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.apache.flink.api.common.typeinfo.TypeInformation;
import in.org.iudx.adaptor.datatypes.Message;


public class TypeInformationTest {

  @Test
  void testType() throws Exception {
    TypeInformation tpf =   TypeInformation.of(Message.class);
    System.out.println(tpf.getTypeClass());
  }
}
