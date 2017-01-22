package com.solacesystems.demo;

import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.util.serialization.SimpleStringSchema;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import java.util.Hashtable;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

@SuppressWarnings("serial")
public class BasicQueueStreamingSample {

    public static void main(String[] args) throws Exception {

        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(4);

        env.addSource(new SourceFunction<String>() {
            @Override
            public void run(SourceContext<String> sourceContext) throws Exception {
                final Hashtable<String, String> jmsEnv = new Hashtable<>();
                jmsEnv.put(InitialContext.INITIAL_CONTEXT_FACTORY, "com.solacesystems.jndi.SolJNDIInitialContextFactory");
                jmsEnv.put(InitialContext.PROVIDER_URL, "smf://192.168.56.101");
                jmsEnv.put(Context.SECURITY_PRINCIPAL, "test@poc_vpn");
                jmsEnv.put(Context.SECURITY_CREDENTIALS, "password");
                InitialContext _jndiContext = new InitialContext(jmsEnv);
                ConnectionFactory factory = (ConnectionFactory) _jndiContext.lookup("flink_cf");
                Connection connection = factory.createConnection();
                Session session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
                MessageConsumer consumer = session.createConsumer(session.createQueue("flink_queue"));
                connection.start();
                while(true) {
                    TextMessage msg = (TextMessage) consumer.receive();
                    sourceContext.collect(msg.getText());
                    msg.acknowledge();
                }
            }
            @Override
            public void cancel() {}
        })
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
        public String reduce(String value1, String value2) {
            return value1 + " + " + value2;
        }
    }
}
