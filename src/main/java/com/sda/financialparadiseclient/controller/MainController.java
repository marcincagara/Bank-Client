package com.sda.financialparadiseclient.controller;

import com.sda.financialparadiseclient.dto.Customer;
import com.sda.financialparadiseclient.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.security.Principal;

@Controller
public class MainController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private CustomerService customerService;

    @RequestMapping("/")
    public String mainPage(HttpServletRequest httpServletRequest) {
        Principal userPrincipal = httpServletRequest.getUserPrincipal();
        if(userPrincipal == null){
            return "mainPage";
        }
        String user = userPrincipal.getName();
        if(!user.equals("admin")) {
            return "redirect:/customers/panel";
        }
        return "redirect:/admin/panel";
    }

    @GetMapping("/show-my-login-page")
    public String showMyLoginPage(){
        return "fancy-login";
    }

    @GetMapping("/register")
    public String showForForAdd(Model model) {
        Customer customer = new Customer();
        model.addAttribute("customer", customer);
        return "customer-register";
    }

    @PostMapping("/register")
    public String saveCustomer(@Valid @ModelAttribute("customer") Customer customer,
                               BindingResult bindingResult) throws Exception {
        if (bindingResult.hasErrors()) {
            return "customer-register";
        }

        String hashedPassword = hashPassword(customer.getPassword());
        customer.setPassword(hashedPassword);
        customerService.addCustomer(customer);
        insertRoles(customer.getEmail(), hashedPassword);
        return "redirect:/";
    }

    private void insertRoles(String email, String password) {
        String sqluser =
                String.format("INSERT INTO users (email, password, enabled) VALUES ('%s', '%s', true)",
                        email, password);
        jdbcTemplate.execute(sqluser);
        String sqlrole =
                String.format("INSERT INTO user_roles (email, role) VALUES ('%s', '%s')",
                        email, "ROLE_USER");
        jdbcTemplate.execute(sqlrole);
    }

    private String hashPassword(String password){
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String hashedPassword = passwordEncoder.encode(password);
        return hashedPassword;
    }
}
