package com;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.websocket.Test;


@SpringBootApplication
//@EnableCaching
public class Application /*implements CommandLineRunner*/{
	
	@Autowired
	private Test test;
	
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

//	@Override
//	public void run(String... args) throws Exception {
//		
//		while(true) {
//			test.sendMessage();
//			Thread.sleep(3000);
//		}
//		
//	}
    
    


}
