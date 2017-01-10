package com.goit.popov.restaurant.controller;

import ch.qos.logback.classic.Logger;
import com.goit.popov.restaurant.service.EmployeeService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;


/**
 * Created by Andrey on 1/7/2017.
 */
@Controller
public class LoginController {

    private static final Logger logger = (Logger) LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private EmployeeService employeeService;

    @GetMapping("/login")
    public String showLoginForm() {
        return "th/login";
    }

    @GetMapping("/access_denied")
    public ModelAndView showAccessDeniedPage(ModelAndView modelAndView) {
        modelAndView.addObject("error","Error");
        modelAndView.setViewName("th/login");
        return modelAndView;
    }

    /*@GetMapping(value="/perform_logout")
    public String logoutPage (HttpServletRequest request, HttpServletResponse response) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null){
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }
        //You can redirect wherever you want, but generally it's a good practice to show login screen again.
        return "redirect:/login";
    }*/
}