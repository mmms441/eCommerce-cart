package com.wisely.e_commercecard.service.order;

import com.wisely.e_commercecard.dto.OrderDto;
import com.wisely.e_commercecard.enums.OrderStatus;
import com.wisely.e_commercecard.exception.ResourceNotFoundException;
import com.wisely.e_commercecard.model.Cart;
import com.wisely.e_commercecard.model.Order;
import com.wisely.e_commercecard.model.OrderItem;
import com.wisely.e_commercecard.model.Product;
import com.wisely.e_commercecard.repository.CartRepository;
import com.wisely.e_commercecard.repository.OrderRepository;
import com.wisely.e_commercecard.repository.ProductRepository;
import com.wisely.e_commercecard.service.cart.CartService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService implements IOrderService {
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final ModelMapper modelMapper;
    private final CartService cartService;


    @Override
    public Order placeOrder(Long userId) {
        Cart cart = cartService.getCartByUserId(userId);

        Order order =createOrder(cart);
        List<OrderItem> orderItemList =createOrderItems(order,cart);
        order.setOrderItems(new HashSet<>(orderItemList));
        order.setTotalAmount(calculateTotalAmount(orderItemList));
        Order savedOrder = orderRepository.save(order);
        cartService.clearCart(cart.getId());
        return savedOrder;
    }

    public Order createOrder(Cart cart) {
        Order order = new Order();
        order.setUser(cart.getUser());
        order.setOrderStatus(OrderStatus.PENDING);
        order.setOrder_date(LocalDate.now());
        return order;
    }


    public BigDecimal calculateTotalAmount(List<OrderItem> orderItems) {
      return  orderItems.stream().
                map(orderItem->orderItem.getPrice()
                        .multiply(new BigDecimal(orderItem.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

    }


public List<OrderItem> createOrderItems(Order orders , Cart cart) {
        return cart.getItems().stream().map(orderItem ->{
            Product product = orderItem.getProduct();
            product.setInventory(product.getInventory()- orderItem.getQuantity());
            productRepository.save(product);
            return new OrderItem(
                    orders,
                    product,
                    orderItem.getQuantity(),
                    orderItem.getUnitPrice()
            );
        }).toList();
}


    @Override
    public OrderDto getOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .map(this::convertToDto).orElseThrow(()->new ResourceNotFoundException("Order not found"));
    }

    @Override
    public List<OrderDto> getUserOrders(Long userId) {
        return orderRepository.findByUserId(userId).stream().map(
                this::convertToDto
        ).toList();
    }

    private OrderDto convertToDto(Order order) {
        return modelMapper.map(order, OrderDto.class);
    }
}
