package com.casestudy.orderservice.service;

import com.casestudy.orderservice.dto.MenuItemDto;
import com.casestudy.orderservice.dto.OrderRequestDto;
import com.casestudy.orderservice.dto.OrderResponseDto;
import com.casestudy.orderservice.dto.OrderedItemsDto;
import com.casestudy.orderservice.entity.Order;
import com.casestudy.orderservice.entity.OrderedItem;
import com.casestudy.orderservice.entity.Payment;
import com.casestudy.orderservice.exception.OrderServiceException;
import com.casestudy.orderservice.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestOperations;

import java.util.List;
import java.util.Random;
import java.util.UUID;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderedItemService orderedItemService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    RestOperations restTemplate;

    private static final String ENDPOINT_URL = "http://localhost:9192/restaurant/menu/item/";

    public Order placeOrder(OrderRequestDto orderRequestDto) throws OrderServiceException {

        double totalPrice = 0;
        Order order = new Order(orderRequestDto.getOrderId(), orderRequestDto.getCustomerId(),
                orderRequestDto.getRestaurantId(), "Created", totalPrice);

        Order savedOrder = orderRepository.save(order);
        List<OrderedItemsDto> itemsOrdered = orderRequestDto.getItems();
        for(OrderedItemsDto itemsDto : itemsOrdered) {

            MenuItemDto menuItem = restTemplate.getForObject(ENDPOINT_URL + itemsDto.getItemId(), MenuItemDto.class);

            totalPrice = totalPrice + (menuItem.getPrice() * itemsDto.getQuantity());

            if(itemsDto.getQuantity() <= 0) {
                orderRepository.delete(order);
                throw new OrderServiceException("Quantity of the item cant be 0");
            }

            OrderedItem orderedItem = new OrderedItem(0L, menuItem.getName(), itemsDto.getQuantity(),
                    menuItem.getPrice(), menuItem.getMenuItemId(), savedOrder);

            orderedItemService.saveItem(orderedItem);
        }
        order.setTotalPrice(totalPrice);
        orderRepository.save(order);

        Payment payment = orderRequestDto.getPayment();
        payment.setOrderId(orderRequestDto.getOrderId());
        payment.setAmount(totalPrice);
        payment.setTransactionId(UUID.randomUUID().toString());

        if(paymentProcessing().equalsIgnoreCase("Success")){
            payment.setPaymentStatus(paymentProcessing());
            payment.setOrder(order);
            payment.setCustomerId(orderRequestDto.getCustomerId());
            paymentService.savePayment(payment);
        }
        else {
            throw new OrderServiceException("Payment Failed , Something wrong in Payment ApI");
        }
        return savedOrder;
    }

    public String paymentProcessing() {
        return new Random().nextBoolean()? "Success" : "Failure";
    }

    public boolean cancelOrder(Long orderId) throws OrderServiceException {
        Order order = orderRepository.findByOrderId(orderId);
        if(order != null) {
            if(order.getStatus().equalsIgnoreCase("Cancelled")) {
                throw new OrderServiceException("This Order is already Cancelled");
            }
            order.setStatus("Cancelled");
            orderRepository.save(order);
            return true;
        }
        else {
            throw new OrderServiceException("No records available for the specified id");
        }
    }

    public OrderResponseDto getOrderById(Long orderId) throws OrderServiceException {
        Order order =  orderRepository.findByOrderId(orderId);
        if(order != null){
            OrderResponseDto orderResponseDto = new OrderResponseDto();
            orderResponseDto.setOrder(order);
            Payment payment = paymentService.getPaymentByOrderId(orderId);
            orderResponseDto.setPayment(payment);
            List<OrderedItem> itemsOrdered = orderedItemService.findByOrderId(orderId);
            orderResponseDto.setItems(itemsOrdered);
            return orderResponseDto;
        } else {
            throw new OrderServiceException("No records available for the specified id");
        }
    }

    public double getOrderAmountByOrderId(Long orderId) {
        Order order = orderRepository.findByOrderId(orderId);
        if(order != null) {
            List<OrderedItem> itemsOrdered = orderedItemService.findByOrderId(orderId);
            double totalPrice = 0;
            for(OrderedItem items : itemsOrdered) {
                totalPrice = totalPrice + (items.getPrice()*items.getQuantity());
            }
            return totalPrice;
        }
        else {
            return 0;
        }
    }
}
