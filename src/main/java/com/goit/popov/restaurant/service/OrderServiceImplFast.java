package com.goit.popov.restaurant.service;

import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.goit.popov.restaurant.dao.OrderDAO;
import com.goit.popov.restaurant.model.*;
import com.goit.popov.restaurant.service.dataTables.DataTablesInputExtendedDTO;
import com.goit.popov.restaurant.service.dataTables.DataTablesOutputDTOUniversal;
import com.goit.popov.restaurant.service.dataTables.service.OrderByWaiterServerSideProcessing;
import com.goit.popov.restaurant.service.dataTables.service.OrderServerSideProcessing;
import com.goit.popov.restaurant.service.exceptions.NotEnoughIngredientsException;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by Andrey on 25.01.2017.
 */

public class OrderServiceImplFast implements OrderService {

        private static final Logger logger = (Logger) LoggerFactory.getLogger(OrderServiceImplFast.class);

        @Autowired
        private OrderDAO orderDAO;

        @Autowired
        private StockService stockService;

        @Autowired
        private Service service;

        @Autowired
        private OrderServerSideProcessing orderServerSideProcessing;

        @Autowired
        private OrderByWaiterServerSideProcessing orderByWaiterServerSideProcessing;

        @Override
        public List<Order> getAll() {
                return orderDAO.getAll();
        }

        @Override
        public Long insert(Order order) {
                return orderDAO.insert(order);
        }

        @Override
        public void update(Order order) {
                orderDAO.update(order);
        }

        @Override
        public Order getById(Long id) {
                return orderDAO.getById(id);
        }

        @Override
        public void delete(Order order) {
                orderDAO.delete(order);
        }

        @Override
        public void deleteById(Long orderId) {
                orderDAO.delete(getById(orderId));
        }

        @Override
        public List<Order> getAllWaiterToday(Long waiterId) {
                return orderDAO.getAllWaiterToday(waiterId);
        }

        @Override
        public List<Order> getAll(DataTablesInputExtendedDTO dt) {
                return orderDAO.getAll(dt);
        }

        @Override
        public List<Order> getAllToday() {
                return orderDAO.getAllToday();
        }

        @Override
        public Integer[] getTables() {
                return Order.TABLE_SET;
        }

        @Override
        public void cancelOrder(Long id) {
                Order order = getById(id);
                order.setCancelled(true);
                update(order);
        }

        @Override
        public Long count() {
                return orderDAO.count();
        }

        @Override
        public Long countWaiter(Employee waiter) {
                return orderDAO.countWaiter(waiter);
        }

        @Override
        public void closeOrder(Long orderId) {
                Order order = getById(orderId);
                if (order.isCancelled()) throw
                        new UnsupportedOperationException("You cannot close a cancelled order!");
                if (!order.isOpened()) throw
                        new IllegalStateException("This order has already been closed!");
                order.setOpened(false);
                order.setClosedTimeStamp(new Date());
                update(order);
        }

        /**
         * Validation Algorithm:
         * 1. Create a Map<Ingredient, Double> of ingredients needed to fulfill the current Order;
         * 2. For each Ingredient get its quantity from the DB;
         * 3. Compare the quantity with the corresponding value in the map;
         *      If the value in the map is greater - return false and do rollback transaction,
         *      at the end - return true, insert of update Order and decrease the quantity from Stock
         * @param order Order to be validated
         */
        @Transactional
        @Override
        public void processOrder(Order order) throws NotEnoughIngredientsException {
                if (!order.isOpened() || order.hasPreparedDishes())
                        throw new UnsupportedOperationException();
                final Long orderId = order.getId();
                if (orderId != 0) returnIngredients(order);
                Map<Dish, Integer> orderedDishes = order.getDishes();
                Map<Ingredient, Double> requiredIngredients = service.getIngredients(orderedDishes);
                if (!validateIngredients(requiredIngredients))
                        throw new NotEnoughIngredientsException();
                if (orderId != 0) {
                        update(order);
                } else {
                        insert(order);
                }
                stockService.decreaseIngredients(requiredIngredients);
        }

        private void returnIngredients(Order order) {
                stockService.increaseIngredients(service.getIngredients(order.getPreviousDishes()));
        }

        private boolean validateIngredients(Map<Ingredient, Double> requiredIngredients) {
                for (Map.Entry<Ingredient, Double> ingredient : requiredIngredients.entrySet()) {
                        Double stockQuantity = stockService.getQuantityByIngredient(ingredient.getKey());
                        Double requiredQuantity = ingredient.getValue();
                        if (stockQuantity < requiredQuantity)
                                return false;
                }
                return true;
        }

        @Override
        public List<Order> getAllOrdersByWaiter(DataTablesInputExtendedDTO dt, String[] params) {
                return orderDAO.getAllOrdersByWaiter(dt, params);
        }

        @Override
        public ArrayNode toJSON(Map<Dish, Integer> dishes) {
                ObjectMapper mapper = new ObjectMapper();
                ArrayNode ana = mapper.createArrayNode();
                for (Map.Entry<Dish, Integer> dish : dishes.entrySet()) {
                        ObjectNode a = mapper.createObjectNode();
                        a.put("id", dish.getKey().getId());
                        a.put("name", dish.getKey().getName());
                        a.put("price", dish.getKey().getPrice());
                        a.put("quantity", dish.getValue());
                        ana.add(a);
                }
                return ana;
        }

        @Override
        public DataTablesOutputDTOUniversal<Order> getAllOrders(DataTablesInputExtendedDTO dt) {
                return orderServerSideProcessing.getAll(dt);
        }

        @Override
        public DataTablesOutputDTOUniversal<Order> getAllOrdersByWaiter(DataTablesInputExtendedDTO dt, Long waiterId) {
                String[] params = new String[1];
                params[0] = waiterId+"";
                return orderByWaiterServerSideProcessing.getAll(dt, params);
        }

}

