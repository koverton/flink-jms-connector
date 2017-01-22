package com.solacesystems.demo;

import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.apache.flink.streaming.api.windowing.time.Time;

import javax.naming.Context;
import javax.naming.InitialContext;
import java.util.Hashtable;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

@SuppressWarnings("serial")
public class BasicTopicStreamingSample {

    public static void main(String[] args) throws Exception {

        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(4);

        final Hashtable<String, String> jmsEnv = new Hashtable<>();
        jmsEnv.put(InitialContext.INITIAL_CONTEXT_FACTORY, "com.solacesystems.jndi.SolJNDIInitialContextFactory");
        jmsEnv.put(InitialContext.PROVIDER_URL, "smf://192.168.56.101");
        jmsEnv.put(Context.SECURITY_PRINCIPAL, "test@poc_vpn");
        jmsEnv.put(Context.SECURITY_CREDENTIALS, "password");

        JMSTopicSource<String> topicSource = new JMSTopicSource<String>(jmsEnv,
                "flink_cf",
                "flink_queue",
                new JMSTextTranslator());

        env.addSource(topicSource)
        .keyBy(new KeySelector<String, String>() {
            @Override
            public String getKey(String s) throws Exception {return s;}
        })
        .timeWindow(Time.of(1000, MILLISECONDS), Time.of(500, MILLISECONDS))
        .reduce(new ConcatReducer())
        .addSink(new SinkFunction<String>() {
            @Override
            public void invoke(String value) {
                System.out.println("HI " + value);
            }
        });

        env.execute();
    }


    public static class ConcatReducer implements ReduceFunction<String> {
        @Override
        public String reduce(String v1, String v2) {
            return  v1 + " + " + v2;
        }
    }
}
