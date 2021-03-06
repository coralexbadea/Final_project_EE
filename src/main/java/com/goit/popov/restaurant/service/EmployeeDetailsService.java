package com.goit.popov.restaurant.service;

import ch.qos.logback.classic.Logger;
import com.goit.popov.restaurant.model.Employee;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Andrey on 1/8/2017.
 */
public class EmployeeDetailsService implements UserDetailsService {

        private static final Logger logger = (Logger) LoggerFactory.getLogger(EmployeeDetailsService.class);

        @Autowired
        private EmployeeService employeeService;

        @Override
        public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
                Employee employee = employeeService.getEmployeeByLogin(username);
                if (employee!=null) {
                        List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
                        authorities.add( new SimpleGrantedAuthority(employee.getPosition().getRole().getName()));
                        UserDetails ud  = new com.goit.popov.restaurant.service.authentification.Employee(
                                employee.getName(), employee.getPassword(),
                                true, true, true, true, authorities, employee.getId());
                        logger.info("User details: "+ud);
                        return ud;
                } else {
                        logger.error("ERROR: Employee not found!");
                        throw new UsernameNotFoundException("Employee not found!");
                }
        }
}
