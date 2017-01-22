# flink-jms-connector

A simple example JMS consumer library for Flink apps. Has basic SourceFunction<OUT> instances for JMS queues and topics.

The library is designed to allow you to plugin your own function to translate inbound JMS Message objects to a target type that is consumed by Flink. This is the bulk of the code you should have to write to use this library.

Here's an example program:

```java
public class BasicTopicStreamingSample {

    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(4);

        final Hashtable<String, String> jmsEnv = new Hashtable<>();
        jmsEnv.put(InitialContext.INITIAL_CONTEXT_FACTORY, "com.solacesystems.jndi.SolJNDIInitialContextFactory");
        jmsEnv.put(InitialContext.PROVIDER_URL, "smf://192.168.56.101");
        jmsEnv.put(Context.SECURITY_PRINCIPAL, "test@poc_vpn");
        jmsEnv.put(Context.SECURITY_CREDENTIALS, "password");

        env.addSource(new JMSTopicSource<String>(
                jmsEnv,
                "flink_cf",
                "flink_queue",
                new JMSTextTranslator())
        ).print();

        env.execute();
    }
```
