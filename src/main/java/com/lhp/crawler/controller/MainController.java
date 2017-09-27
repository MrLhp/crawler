package com.lhp.crawler.controller;

import com.lhp.crawler.model.User;
import com.lhp.crawler.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(path = "/demo")
public class MainController {
    @Autowired
    UserRepository userRepository;

    @GetMapping("/add")
    public @ResponseBody
    String addNewUser(@RequestParam String name, @RequestParam String age) {
        User u = new User();
        u.setAge(age);
        u.setName(name);
        System.out.println(u.toString()+"\t"+name+"\t"+age);
        userRepository.save(u);
        return "success saved";
    }

    @GetMapping("/all")
    public @ResponseBody
    Iterable<User> getAllUsers() {
        return userRepository.findAll();
    }

    @GetMapping("/s")
    public @ResponseBody
    String string() {
        return "123";
    }
}
