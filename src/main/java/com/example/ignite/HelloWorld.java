package com.example.ignite;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteException;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.client.ClientAddressFinder;
import org.apache.ignite.client.ClientCache;
import org.apache.ignite.client.ClientCacheConfiguration;
import org.apache.ignite.client.ClientException;
import org.apache.ignite.client.IgniteClient;
import org.apache.ignite.configuration.ClientConfiguration;
import org.apache.ignite.configuration.ClientConnectorConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.configuration.ThinClientConfiguration;

public class HelloWorld {

    private static final String CACHE_NAME = "myCache";


    public static void main(String[] args) throws IgniteException {

        System.setProperty("-Djava.net.preferIPv4Stack", "true");

        ClientAddressFinder finder = () -> new String[]{"127.0.0.1:10800"}; //this can be more complex


        ClientConfiguration cfg = new ClientConfiguration()
//                .setAddresses("127.0.0.1:10800")
                .setAddressesFinder(finder)
                //                .setTransactionConfiguration(new ClientTransactionConfiguration().setDefaultTxTimeout(10000)
                //                                                                                 .setDefaultTxConcurrency(TransactionConcurrency.OPTIMISTIC)
                //                                                                                 .setDefaultTxIsolation(TransactionIsolation.REPEATABLE_READ));
                //                .setPartitionAwarenessEnabled(true) // better set to true and add all the servers
                ;

        ClientCacheConfiguration ccf = new ClientCacheConfiguration();
        ccf.setName(CACHE_NAME);
        ccf.setCacheMode(CacheMode.REPLICATED);

        try (IgniteClient client = Ignition.startClient(cfg)) {
            ClientCache<Integer, String> cache =
                    //                    client.cache("mixas cache"); // fails if cache is not there
                    //                    client.getOrCreateCache("mixas cache"); //does not fail
                    client.getOrCreateCache(ccf); //same with second but with configuration

            /*
                Basic ops
             */
            // bulk load
            Map<Integer, String> data = IntStream.rangeClosed(1, 100).boxed()
                                                 .collect(Collectors.toMap(i -> i, i -> i + " in cache"));
            cache.putAll(data);

            System.out.println(cache.get(1));
            System.out.println(cache.get(100));

            // replace
            cache.replace(1, "new one value");
            System.out.println(cache.get(1));


            // remove
            cache.removeAll(data.keySet());
            System.out.println("Should be null: " + cache.get(1));


        } catch (ClientException e) {
            System.err.println(e.getMessage());
        }

        System.out.println("over");
    }


}