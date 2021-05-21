package br.com.desenv.ecommerce;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;


public class FraudDetectorService {

    public static void main(String[] args) {
        var fraudService = new FraudDetectorService();
        try (var service = new KafkaService<>(
                FraudDetectorService.class.getSimpleName(),
                "ECOMMERCE_NEW_ORDER",
                fraudService::parse,
                Order.class,
                new HashMap<>())) {
            service.run();
        }
    }

    private void parse(ConsumerRecord<String, Order> record) throws ExecutionException, InterruptedException {
        System.out.println("-----------------------------------------");
        System.out.println("Processing new order, checking for frauds");
        System.out.println(record.key());
        System.out.println(record.value());
        System.out.println(record.partition());
        System.out.println(record.offset());
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            //ignoring
            e.printStackTrace();

        }
        var order = (Order) record.value();
        if (isFraud(order)) {
            // pretending that the fraud happens when the amount is >= 4500
            System.out.println("Order is a fraud!!!");
            orderDispatcher.send("ECOMMERCE_ORDER_REJECTED", order.getEmail(), order);
        } else {
            System.out.println("Approved: " + order.toString());
            orderDispatcher.send("ECOMMERCE_ORDER_APPROVED", order.getEmail(), order);

        }

    }

    private final KafkaDispatcher<Order> orderDispatcher = new KafkaDispatcher<>();

    private boolean isFraud(Order order) {
        return order.getAmount().compareTo(new BigDecimal("4500")) >= 0;
    }

}
