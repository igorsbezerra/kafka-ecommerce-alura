package br.com.alura.ecommerce;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.sql.*;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class CreateUserService {

    private final Connection connection;

    public CreateUserService() throws SQLException {
        String url = "jdbc:sqlite:service-users/target/users_database.db";
        this.connection = DriverManager.getConnection(url);
        try {
            connection.createStatement().execute("create table users (" +
                    "uuid varchar(200) primary key," +
                    "email varchar(200))");
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) throws SQLException, ExecutionException, InterruptedException {
        var createUserService = new CreateUserService();
        try (var service = new KafkaService<>(CreateUserService.class.getSimpleName(),
                "ECOMMERCE_NEW_ORDER",
                createUserService::parse,
                Map.of())) {
            service.run();
        }
    }

    private void parse(ConsumerRecord<String, Message<Order>> record) throws SQLException {
        System.out.println("------------------------------------------");
        System.out.println("Processing new order, checking for new user");
        System.out.println(record.key());
        System.out.println(record.value());
        var order = record.value();
        if (isNewUser(order.getPayload().getEmail())) {
            insertNewUser(order.getPayload().getEmail());
        }
    }

    private void insertNewUser(String email) throws SQLException {
        PreparedStatement insert = connection.prepareStatement("insert into users (uuid, email) values (?, ?)");
        insert.setString(1, UUID.randomUUID().toString());
        insert.setString(2, email);
        insert.execute();
        System.out.println("Usuario uuid e " + email + " adicionado");
    }

    private boolean isNewUser(String email) throws SQLException {
        PreparedStatement exists = connection.prepareStatement("select uuid from users where email = ? limit 1");
        exists.setString(1, email);
        ResultSet result = exists.executeQuery();
        return !result.next();
    }
}
