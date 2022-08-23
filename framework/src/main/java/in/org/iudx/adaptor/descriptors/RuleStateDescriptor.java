package in.org.iudx.adaptor.descriptors;

import in.org.iudx.adaptor.datatypes.Rule;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.typeinfo.BasicTypeInfo;
import org.apache.flink.api.common.typeinfo.TypeInformation;

public class RuleStateDescriptor {
    public static final MapStateDescriptor<String, Rule> ruleMapStateDescriptor = new MapStateDescriptor<>("RulesBroadcastState", BasicTypeInfo.STRING_TYPE_INFO, TypeInformation.of(Rule.class));
}
