package com.goit.popov.restaurant.dao.impl;

import com.goit.popov.restaurant.dao.MenuDAO;
import com.goit.popov.restaurant.model.Menu;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/**
 * Created by Andrey on 28.10.2016.
 */
@Transactional
public class MenuDAOImplJPA implements MenuDAO {

        @Autowired
        private SessionFactory sessionFactory;

        @Override
        public Long insert(Menu menu) {
                return (Long) sessionFactory.getCurrentSession().save(menu);
        }

        @Override
        public void update(Menu menu) {
                sessionFactory.getCurrentSession().update(menu);
        }

        @Override
        public List<Menu> getAll() {
                return sessionFactory.getCurrentSession().createQuery("select m from Menu m").list();
        }

        @Override
        public Menu getById(Long id) {
                return sessionFactory.getCurrentSession().get(Menu.class, id);
        }

        @Override
        public void delete(Menu menu) {
                sessionFactory.getCurrentSession().delete(menu);
        }

}
