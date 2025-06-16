package com.codefathers.util;

import com.codefathers.model.entity.Order;
import com.codefathers.model.entity.OrderItem;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import java.util.List;

@Slf4j
public final class JsonUtil {

    private static final ObjectMapper objectMapper = createObjectMapper();

    // 1. Define the Mix-in "carriers"
    //    These are abstract classes that hold the annotations for your entities.

    private abstract static class OrderMixIn {
        @JsonManagedReference
        abstract List<OrderItem> getItems(); // The annotation goes on the getter
    }

    private abstract static class OrderItemMixIn {
        @JsonBackReference
        abstract Order getOrder();
    }

    private static ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // 2. Register the Mix-ins with the ObjectMapper
        //    This tells Jackson to apply annotations from OrderMixIn to Order, etc.
        mapper.addMixIn(Order.class, OrderMixIn.class);
        mapper.addMixIn(OrderItem.class, OrderItemMixIn.class);

        // Add mix-ins for your other relationships here too to avoid future errors!
        // e.g., mapper.addMixIn(ShippingProvider.class, ShippingProviderMixIn.class);

        return mapper;
    }

    public static String toPrettyJson(Object obj) {
        if (obj == null) {
            return "null";
        }
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            log.error("Failed to serialize object to JSON", e);
            return "Error converting object to JSON: " + e.getMessage();
        }
    }
}