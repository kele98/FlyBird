package top.aikele.springboot.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.aikele.entities.Address;
import top.aikele.springboot.entities.RepositoryMap;

import java.util.HashMap;
@RestController
public class ListController {
    @RequestMapping("/get")
    @CrossOrigin
    public HashMap<String, Address> get(){
       return RepositoryMap.getList();
    }
}
