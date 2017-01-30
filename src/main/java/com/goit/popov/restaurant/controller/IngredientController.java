package com.goit.popov.restaurant.controller;

import ch.qos.logback.classic.Logger;
import com.goit.popov.restaurant.model.Ingredient;
import com.goit.popov.restaurant.model.Unit;
import com.goit.popov.restaurant.service.IngredientService;
import com.goit.popov.restaurant.service.UnitService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Andrey on 09.11.2016.
 */
@Controller
public class IngredientController {

        static Logger logger = (Logger) LoggerFactory.getLogger(IngredientController.class);

        @Autowired
        private IngredientService ingredientService;

        @Autowired
        private UnitService unitService;

        // Populate units
        @ModelAttribute("units")
        public Map<String, String> populatePositions() {
                List<Unit> units = unitService.getAll();
                Map<String, String> unitsList = new HashMap<>();
                unitsList.put("0", "--Select--");
                for (Unit unit : units) {
                        unitsList.put(Integer.toString(unit.getId()), unit.getName());
                }
                return unitsList;
        }

        @RequestMapping("/admin/new_ingredient")
        public ModelAndView showIngredientForm(){
                return new ModelAndView("jsp/new_ingredient","ingredient",new Ingredient());
        }

        @RequestMapping(value = "/admin/ingredients", method = RequestMethod.GET)
        public String ingredients(Map<String, Object> model) {
                model.put("ingredients", ingredientService.getAll());
                return "th/manager/ingredients";
        }

        @RequestMapping(value="/admin/save_ingredient",method = RequestMethod.POST)
        public String saveIngredient(@Valid @ModelAttribute("ingredient") Ingredient ingredient, BindingResult result){
                if (result.hasErrors()) {
                        logger.error("Errors: during creating!");
                        return "jsp/new_ingredient";
                }
                ingredientService.save(ingredient);
                return "redirect:/admin/ingredients";
        }

        @RequestMapping(value="/admin/edit_ingredient/{id}", method = RequestMethod.GET)
        public ModelAndView edit(@PathVariable int id){
                Ingredient ing=ingredientService.getIngredientById(id);
                return new ModelAndView("jsp/update_ingredient","ingredient",ing);
        }

        @RequestMapping(value="/admin/update_ingredient/{id}", method = RequestMethod.POST)
        public String editSave(@Valid @ModelAttribute Ingredient ingredient, BindingResult result, @PathVariable Integer id){
                if (result.hasErrors()) {
                        logger.error("Errors: during updating!");
                        return "jsp/update_ingredient";
                }
                ingredientService.update(ingredient);
                return "redirect:/admin/ingredients";
        }

        @RequestMapping(value="/admin/delete_ingredient/{id}",method = RequestMethod.GET)
        public String delete(@PathVariable int id, RedirectAttributes ra){
                try {
                        ingredientService.deleteById(id);
                } catch (Exception e) {
                        ra.addFlashAttribute("status", HttpStatus.FORBIDDEN);
                        ra.addFlashAttribute("error", "Constraint violation exception deleting ingredient!");
                        ra.addFlashAttribute("message", "Error: failed to delete the ingredient #" +id);
                        return "redirect:/error";
                }
                return "redirect:/admin/ingredients";
        }
}
