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
                "flink/topic",
                new JMSTextTranslator())
        ).print();

        env.execute();
    }
```

This uses a sample `JMSTranslator` instance for JMS TextMessages. For a custom payload you should implement your own `JMSTranslator`, which would be the bulk of the work for integrating any JMS consumer. I elected not to use Flink's existing serialization approach as it only deals with primitive types and arrays, where you'd really prefer serialization from the full inbound message to access headers.
