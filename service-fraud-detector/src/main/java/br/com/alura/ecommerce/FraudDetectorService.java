package br.com.alura.ecommerce;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class FraudDetectorService {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        var fraudService = new FraudDetectorService();
        try (var service = new KafkaService<>(FraudDetectorService.class.getSimpleName(),
                "ECOMMERCE_NEW_ORDER",
                fraudService::parse,
                Map.of())) {
            service.run();
        }
    }

    private final KafkaDispatcher<Order> orderDispatcher = new KafkaDispatcher<>();

    private void parse(ConsumerRecord<String, Message<Order>> record) throws ExecutionException, InterruptedException {
        System.out.println("------------------------------------------");
        System.out.println("Processing new order, checking for fraud");
        System.out.println(record.key());
        System.out.println(record.value());
        System.out.println(record.partition());
        System.out.println(record.offset());
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            // ignoring
            e.printStackTrace();
        }
        var message = record.value();
        if (isFraud(message.getPayload())) {
            System.out.println("Order ir as fraud!!! " + message.getPayload());
            orderDispatcher.send("ECOMMERCE_ORDER_REJECTED", message.getPayload().getEmail(), message.getId().continueWith(FraudDetectorService.class.getSimpleName()), message.getPayload());
        } else {
            System.out.println("Approved: " + message.getPayload());
            orderDispatcher.send("ECOMMERCE_ORDER_APPROVED", message.getPayload().getEmail(), message.getId().continueWith(FraudDetectorService.class.getSimpleName()), message.getPayload());
        }
    }

    private static boolean isFraud(Order order) {
        return order.getAmount().compareTo(new BigDecimal("4500")) >= 0;
    }

}
