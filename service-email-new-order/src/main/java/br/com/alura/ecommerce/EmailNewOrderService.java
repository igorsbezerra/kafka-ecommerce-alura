package br.com.alura.ecommerce;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class EmailNewOrderService {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        var emailService = new EmailNewOrderService();
        try (var service = new KafkaService<>(EmailNewOrderService.class.getSimpleName(),
                "ECOMMERCE_NEW_ORDER",
                emailService::parse,
                Map.of())) {
            service.run();
        }
    }

    private final KafkaDispatcher<String> emailDispatcher = new KafkaDispatcher<>();

    private void parse(ConsumerRecord<String, Message<Order>> record) throws ExecutionException, InterruptedException {
        System.out.println("------------------------------------------");
        System.out.println("Processing new order, prepare email");
        System.out.println(record.key());
        System.out.println(record.value());

        var emailCode = "Thank you for your order! We are processing your order!";
        var order = record.value().getPayload();
        emailDispatcher.send("ECOMMERCE_SEND_EMAIL", order.getEmail(),
                record.value().getId().continueWith(EmailNewOrderService.class.getSimpleName()),
                emailCode);
    }
}
