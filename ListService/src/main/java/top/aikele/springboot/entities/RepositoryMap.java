package top.aikele.springboot.entities;

import top.aikele.entities.Address;

import java.util.HashMap;

public class RepositoryMap {
    private static HashMap<String, Address> addressMap = new HashMap<>();
    private RepositoryMap() {

    }
    public static  HashMap<String,Address> getList(){
        return addressMap;
    }
    public static  boolean setData(Address address){
        if(addressMap.containsKey(address.getName())){
            System.out.println("心跳刷新成功");
        }else {
            System.out.println("添加成功");
        }
        addressMap.put(address.getName(),address);
        return true;
    }
}

