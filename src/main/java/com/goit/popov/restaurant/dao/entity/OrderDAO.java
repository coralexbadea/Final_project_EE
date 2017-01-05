package com.goit.popov.restaurant.dao.entity;

import com.goit.popov.restaurant.model.Dish;
import com.goit.popov.restaurant.model.Order;
import com.goit.popov.restaurant.model.Waiter;
import com.goit.popov.restaurant.service.dataTables.DataTablesInputExtendedDTO;

import java.util.List;

/**
 * Created by Andrey on 10/14/2016.
 */
public interface OrderDAO extends GenericDAO<Order> {
        void addDish(Order order, Dish dish, int quantity);
        void deleteDish(Order order, Dish dish, int quantity);
        void delete(int id);
        void close(Order order);
        List<Order> getAllClosed();
        List<Order> getAllOpened();
        long count();
        long countWaiter(Waiter waiter);
        List<Order> getAll(DataTablesInputExtendedDTO dt);

        List<Order> getAllWaiterToday(int waiterId);
        List<Order> getAllWaiterArchive(int waiterId, DataTablesInputExtendedDTO dt);
}
