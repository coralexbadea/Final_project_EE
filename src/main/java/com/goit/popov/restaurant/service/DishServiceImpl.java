package com.goit.popov.restaurant.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.goit.popov.restaurant.dao.DishDAO;
import com.goit.popov.restaurant.model.Dish;
import com.goit.popov.restaurant.model.Ingredient;
import com.goit.popov.restaurant.service.dataTables.DataTablesInputExtendedDTO;
import com.goit.popov.restaurant.service.dataTables.DataTablesOutputDTOUniversal;
import com.goit.popov.restaurant.service.dataTables.service.DishServerSideProcessing;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import java.util.Map;

/**
 * Created by Andrey on 02.12.2016.
 */
public class DishServiceImpl implements DishService {

        @Autowired
        private DishDAO dishDAO;

        @Autowired
        private DishServerSideProcessing dishServerSideProcessing;

        @Override
        public Long insert(Dish dish) {
                return dishDAO.insert(dish);
        }

        @Override
        public void update(Dish dish) {
                dishDAO.update(dish);
        }

        @Override
        public List<Dish> getAll() {
                return dishDAO.getAll();
        }

        @Override
        public Dish getById(Long id) {
                return dishDAO.getById(id);
        }

        @Override
        public void delete(Dish dish) {
                dishDAO.delete(dish);
        }

        @Override
        public void deleteById(Long id) {
                dishDAO.deleteById(id);
        }

        @Override
        public Long count() {
                return dishDAO.count();
        }

        @Override
        public List<Dish> getAllItems(DataTablesInputExtendedDTO dt) {
                return dishDAO.getAllItems(dt);
        }

        @Override
        public void updateDishsIngredients(Long dishId, Map<Ingredient, Double> ingredients) {
                Dish updatedDish = getById(dishId);
                updatedDish.setIngredients(ingredients);
                update(updatedDish);
        }

        @Override
        public ArrayNode toJSON(Map<Ingredient, Double> ingredients) {
                ObjectMapper mapper = new ObjectMapper();
                ArrayNode ana = mapper.createArrayNode();
                for (Map.Entry<Ingredient, Double> ingredient : ingredients.entrySet()) {
                        ObjectNode a = mapper.createObjectNode();
                        a.put("id", ingredient.getKey().getId());
                        a.put("name", ingredient.getKey().getName());
                        a.put("quantity", ingredient.getValue());
                        a.put("unit", ingredient.getKey().getUnit().getName());
                        ana.add(a);
                }
                return ana;
        }

        @Override
        public DataTablesOutputDTOUniversal<Dish> getAll(DataTablesInputExtendedDTO dt) {
                return dishServerSideProcessing.getAll(dt);
        }
}
