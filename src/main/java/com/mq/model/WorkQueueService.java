package com.mq.model;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

//@Service
public class WorkQueueService {

	@Autowired
	private RabbitTemplate rabbitTemplate;
	
	@Scheduled(fixedRate=1000)
	public void sendMessage() {
		System.out.println("Send message");
		rabbitTemplate.convertAndSend("myQueue", new Date());
	}
	
	@RabbitListener(queues="myQueue")
	public void getMessage(Date msg) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		
		System.out.println("Get Message : " + sdf.format(msg) + " at server 1");
	}
	
	@RabbitListener(queues="myQueue")
	public void getMessage1(Date msg) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		
		System.out.println("Get Message : " + sdf.format(msg) + " at server 2");
		
	}
	
}
