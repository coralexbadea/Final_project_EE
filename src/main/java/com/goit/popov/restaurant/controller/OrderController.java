package com.goit.popov.restaurant.controller;

import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.goit.popov.restaurant.model.Dish;
import com.goit.popov.restaurant.model.Order;
import com.goit.popov.restaurant.service.DishService;
import com.goit.popov.restaurant.service.OrderService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by Andrey on 02.12.2016.
 */
@Controller
public class OrderController {

        private static final Logger logger = (Logger) LoggerFactory.getLogger(OrderController.class);

        @Autowired
        private DishService dishService;

        @Autowired
        private OrderService orderService;

        @GetMapping("/new_order")
        public ModelAndView showOrderForm(){
                logger.info("Show Order form");
                ModelAndView modelAndView = new ModelAndView("th/new_order");
                Order order = new Order();
                order.setOpened(true);
                order.setTable(0);
                order.setOpenedTimeStamp(new Date());
                modelAndView.addObject("order", order);
                List<Integer> tables = new ArrayList<>();
                for (int i = 0; i < Order.TABLE_SET.length; i++) {
                        tables.add(Order.TABLE_SET[i]);
                }
                modelAndView.addObject("allTables", tables);
                Map<Dish, Integer> orderedDishes;
                orderedDishes = orderService.getDishes(1);
                modelAndView.addObject("orderedDishes", orderedDishes);
                return modelAndView;
        }

        //------------------------------AJAX-----------------------------

        @GetMapping("/new_order_ajax")
        public ModelAndView showOrderFormAjax() throws JsonProcessingException {
                ModelAndView modelAndView = new ModelAndView("th/new_order_ajax");
                modelAndView.addObject("allTables", Order.TABLE_SET);
                List<Dish> dishes = dishService.getAll();
                ObjectMapper mapper = new ObjectMapper();
                modelAndView.addObject("dishes", mapper.writeValueAsString(dishes));
                return modelAndView;
        }

        @GetMapping("/edit_order_ajax/{id}")
        public ModelAndView editOrderAjax(@PathVariable("id") int id) throws JsonProcessingException {
                ModelAndView modelAndView = new ModelAndView("th/edit_order_ajax");
                modelAndView.addObject("allTables", Order.TABLE_SET);
                List<Dish> dishes = dishService.getAll();
                ObjectMapper mapper = new ObjectMapper();
                modelAndView.addObject("dishes", mapper.writeValueAsString(dishes));
                Map<Dish, Integer> orderedDishes;
                orderedDishes = orderService.getDishes(id);
                modelAndView.addObject("orderedDishes", orderedDishes);
                return modelAndView;
        }

        @PostMapping(value="/create_order_ajax")
        public @ResponseBody String createOrder(@Valid @RequestBody Order order, BindingResult result) {
                if (result.hasErrors()) {
                        logger.error("Validation error: "+result.getErrorCount());
                }
                logger.info("Controller Order: "+order);
                logger.info("Controller Order's dishes: "+order.getDishes());
                return "{"+"\""+"result"+"\""+":"+"\""+"success"+"\""+"}";
                //orderService.insert(order);
        }
}
