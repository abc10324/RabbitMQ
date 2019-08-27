package com.mq.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

//@Service
public class RoutingService {

	@Autowired
	private RabbitTemplate rabbitTemplate;
	
	private String[] colorArr = {"black","red","yellow"};
	
	private Random random = new Random();
	
	@Scheduled(fixedRate=1000)
	public void sendMessage() {
		
		String destination = colorArr[random.nextInt(colorArr.length)];
		
		System.out.println("Direct Send to " + destination);
		
		rabbitTemplate.convertAndSend("mq.direct", destination, new Date());
	}
	
	@RabbitListener(queues="#{autoDeleteQueue1.name}")
	public void getMessage(Date msg) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		
		System.out.println("Get Message : " + sdf.format(msg) + " at direct channel 1");
	}
	
	@RabbitListener(queues="#{autoDeleteQueue2.name}")
	public void getMessage1(Date msg) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		
		System.out.println("Get Message : " + sdf.format(msg) + " at direct channel 2");
		
	}
}
