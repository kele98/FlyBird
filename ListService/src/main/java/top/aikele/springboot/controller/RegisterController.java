package top.aikele.springboot.controller;


import org.springframework.web.bind.annotation.*;
import top.aikele.entities.Address;
import top.aikele.springboot.entities.RepositoryMap;

@RestController
public class RegisterController {
    @RequestMapping("/set")
    @CrossOrigin
    public void setData( @RequestBody Address address){
        boolean is = RepositoryMap.setData(address);

    }
}
