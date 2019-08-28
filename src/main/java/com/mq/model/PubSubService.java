package com.mq.model;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeoutException;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

//@Service
public class PubSubService {
	
//	@Autowired
//	private RabbitTemplate rabbitTemplate;
//	
//	@Autowired
//	private Environment env;
//	
//	@Value("${key}")
//	private String msg;
//	
//	
//	@Scheduled(fixedRate=1000)
//	public void sendMessage() {
//		rabbitTemplate.convertAndSend("mq.fanout", "", new Date());
////		System.out.println("message : " + msg);
////		System.out.println("message : " + env.getProperty("company"));
//		System.out.println("pub server Send Message !");
//	}
//	
//	
//	@RabbitListener(queues = "#{autoDeleteQueue1.name}")
//	public void getMessage(Date msg) {
//		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
//		
//		System.out.println("Get Message : " + sdf.format(msg) + " at sub server 1");
//	}
//	
//	@RabbitListener(queues = "#{autoDeleteQueue2.name}")
//	public void getMessage1(Date msg) {
//		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
//		
//		System.out.println("Get Message : " + sdf.format(msg) + " at sub server 2");
//	}
//	
//	@RabbitListener(queues = "#{autoDeleteQueue3.name}")
//	public void getMessage2(Date msg) {
//		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
//		
//		System.out.println("Get Message : " + sdf.format(msg) + " at sub server 3");
//	}
	
	private List<Connection> connectionList = new ArrayList<Connection>();
	private List<Channel> channelList = new ArrayList<Channel>();
	
	@Scheduled(fixedRate=1000)
	public void sendMessage() {
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("localhost");
		
		Connection connection = null;
		Channel channel = null;
		
		try{
			connection = factory.newConnection();
			channel = connection.createChannel();
			
			channel.queueDeclare("myQueue", false, false, false, null);
			String message = "Hello World!";
			
			
			channel.basicPublish("", "myQueue", null, message.getBytes());
			System.out.println(" [x] Sent '" + message + "'");
			
		} catch (IOException | TimeoutException e) {
			e.printStackTrace();
		} finally {
			try {
				if(channel != null)
					channel.close();
				if(connection != null)
					connection.close();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (TimeoutException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	// start consumer at system start point
	@EventListener(ContextRefreshedEvent.class)
	public void getMessage() {
		initConsumer(1,5);
		initConsumer(2,0);
	}
	
	// close consumer's connection
	@EventListener(ContextClosedEvent.class)
	public void stopConsumer() {
		try {
			for(Channel channel : channelList)
				channel.close();
			
			for(Connection connection : connectionList)
				connection.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TimeoutException e) {
			e.printStackTrace();
		}
		
	}

	
	// initial consumer
	public void initConsumer(int serverNo,int sleepSecond) {
		ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        
        Connection connection = null;
		Channel channel = null;
		
		try{
			connection = factory.newConnection();
			channel = connection.createChannel();
			
			connectionList.add(connection);
			channelList.add(channel);
			
			channel.queueDeclare("myQueue", false, false, false, null);
	        channel.basicQos(1);
			System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
	        
	        Consumer consumer = new DefaultConsumer(channel) {
	        	@Override
	            public void handleDelivery(String consumerTag,
	                                       Envelope envelope,
	                                       AMQP.BasicProperties properties,
	                                       byte[] body)
	                throws IOException
	            {
	        		
	        		try {
						Thread.sleep(sleepSecond * 1000);
						System.out.println("Recieve from server " + serverNo + " : " + new String(body));
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
	        		
	        		getChannel().basicAck(envelope.getDeliveryTag(), false);
	            }
	        };
	        
	        channel.basicConsume("myQueue", false, consumer);
			
		} catch (IOException | TimeoutException e) {
			e.printStackTrace();
		} 
		
	}
	
}
