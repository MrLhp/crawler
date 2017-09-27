package controller;

import model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import repository.UserRepository;

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
}
