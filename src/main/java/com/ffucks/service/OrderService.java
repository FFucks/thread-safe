package com.ffucks.service;

import com.ffucks.entity.Order;
import com.ffucks.repository.OrderRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Transactional
    public Order createOrder(String code) {
        try {
            return orderRepository.save(new Order(code));
        } catch (DataIntegrityViolationException e) {
            return orderRepository.findByCode(code).orElseThrow(() -> e);
        }
    }

    @Transactional
    public Order updateWithOptimisticLock(Long id, java.util.function.Consumer<Order> change) {
        var ent = orderRepository.findById(id).orElseThrow();
        change.accept(ent);
        try {
            return orderRepository.save(ent);
        } catch (OptimisticLockingFailureException ex) {
            throw ex;
        }
    }

}
