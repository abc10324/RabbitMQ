package com.mq.model;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class PubSubService {
	
	@Autowired
	private RabbitTemplate rabbitTemplate;
	
	@Value("${value}")
	private String msg;
	
	
	@Scheduled(fixedRate=1000)
	public void sendMessage() {
		rabbitTemplate.convertAndSend("mq.fanout", "", new Date());
		System.out.println("message : " + msg);
		System.out.println("pub server Send Message !");
	}
	
	
	@RabbitListener(queues = "#{autoDeleteQueue1.name}")
	public void getMessage(Date msg) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		
		System.out.println("Get Message : " + sdf.format(msg) + " at sub server 1");
	}
	
	@RabbitListener(queues = "#{autoDeleteQueue2.name}")
	public void getMessage1(Date msg) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		
		System.out.println("Get Message : " + sdf.format(msg) + " at sub server 2");
	}
	
	@RabbitListener(queues = "#{autoDeleteQueue3.name}")
	public void getMessage2(Date msg) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		
		System.out.println("Get Message : " + sdf.format(msg) + " at sub server 3");
	}
	
}
