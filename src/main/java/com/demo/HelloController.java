package com.demo;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.net.URI;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;

@EnableEurekaClient
@RestController
@EnableCircuitBreaker
public class HelloController {
    private static final String template = "Hello, %s!";
    private final AtomicLong counter = new AtomicLong();
    
    @Autowired
    private DiscoveryClient discoveryClient;
    
    @Value("${fortune.fallbackFortune}")
	private String defaultFortune = "Things not looking so great!";

    @RequestMapping("/")
    public String index() {
        return "Greetings from KC Spring Boot demo!";
    }

    @RequestMapping(value="/hello-world", method=RequestMethod.GET)
    public @ResponseBody Greeting sayHello(@RequestParam(value="name", required=false, defaultValue="Stranger") String name) {
        return new Greeting(counter.incrementAndGet(), String.format(template, name));
    }
    
    @RequestMapping("/fortune")
    @HystrixCommand(fallbackMethod="getDefaultFortune")
    public Fortune getFortune() throws Exception {
    	URI fortuneURI = getServiceUrl();
    	String fortuneURIFull = fortuneURI + "/random";
    	RestTemplate restTemplate = new RestTemplate();
    	Fortune fortune = restTemplate.getForObject(fortuneURIFull, Fortune.class);
 
        return fortune;
    }    
    
    public URI getServiceUrl() throws Exception {    	
        List<ServiceInstance> list = discoveryClient.getInstances("fortunes");
        if (list == null || list.size() == 0 ) {
            throw new Exception("No service instances found!");
        }
        return list.get(0).getUri();
    }
    
    public Fortune getDefaultFortune(){
    	Fortune f = new Fortune();
    	f.setId(999L);
    	f.setText(defaultFortune);
    	return f;
    }

}