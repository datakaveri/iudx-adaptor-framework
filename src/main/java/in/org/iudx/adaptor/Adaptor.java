package in.org.iudx.adaptor;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class Adaptor {

	public static void main(String[] args) throws Exception {

		final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

		env.execute("Streaming WordCount");
	}
}
