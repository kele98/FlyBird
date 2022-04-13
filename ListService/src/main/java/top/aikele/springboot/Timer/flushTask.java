package top.aikele.springboot.Timer;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import top.aikele.entities.Address;
import top.aikele.springboot.entities.RepositoryMap;

@Component
@EnableScheduling
public class flushTask {
    @Scheduled(initialDelay = 10000,fixedDelay = 5000)
    public void flush(){
        System.out.println("刷新心跳");
        for (String s : RepositoryMap.getList().keySet()) {
            Address address = RepositoryMap.getList().get(s);
            if(address.getTime()==0){
                RepositoryMap.getList().remove(s);
            }else {
                address.setTime(address.getTime()-5);
                RepositoryMap.getList().replace(s,address);
            }

        }

    }
}
