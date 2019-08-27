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
public class TopicService {

	@Autowired
	private RabbitTemplate rabbitTemplate;
	
	private String[] destArr = {"nervous.red.rabbit" // topic 2
							   ,"lazy.brown.dog" 	 // topic 1 2
							   ,"lazy.gold.cat"  	 // topic 2
							   ,"normal.brown.human" // topic 1
							   ,"nervous.blue.fish"};// no topic
	
	private Random random = new Random();
	
	@Scheduled(fixedRate=2000)
	public void sendMessage() {
		
		String destination = destArr[random.nextInt(destArr.length)];
		
		System.out.println("Send to Topic : " + destination);
		
		rabbitTemplate.convertAndSend("mq.topic", destination, new Date());
	}
	
	@RabbitListener(queues="#{autoDeleteQueue1.name}")
	public void getMessage(Date msg) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		
		System.out.println("Get Message : " + sdf.format(msg) + " at topic 1");
	}
	
	@RabbitListener(queues="#{autoDeleteQueue2.name}")
	public void getMessage1(Date msg) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		
		System.out.println("Get Message : " + sdf.format(msg) + " at topic 2");
		
	}
}
