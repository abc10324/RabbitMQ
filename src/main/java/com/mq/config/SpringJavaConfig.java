package com.mq.config;

import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.AnonymousQueue;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@ComponentScan(basePackages="com.mq")
@EnableScheduling
@EnableRabbit
//@PropertySource(value="classpath:prop.properties",encoding="UTF-8")
public class SpringJavaConfig {
	
	@Bean
    public ConnectionFactory connectionFactory() {
        return new CachingConnectionFactory("localhost");
    }

    @Bean
    public AmqpAdmin amqpAdmin() {
        return new RabbitAdmin(connectionFactory());
    }

    @Bean
    public RabbitTemplate rabbitTemplate() {
        return new RabbitTemplate(connectionFactory());
    }

    @Bean
    public Queue autoDeleteQueue1() {
        return new AnonymousQueue();
    }

    @Bean
    public Queue autoDeleteQueue2() {
        return new AnonymousQueue();
    }
    
    @Bean
    public Queue autoDeleteQueue3() {
    	return new AnonymousQueue();
    }
    
    @Bean
    public Queue myQueue() {
       return new Queue("myQueue",false);
    }
    
    @Bean
    public FanoutExchange fanout() {
        return new FanoutExchange("mq.fanout");
    }
    
    
    @Bean
    public Binding fanoutBinding1(FanoutExchange fanout,
    							     Queue autoDeleteQueue1) {
        return BindingBuilder.bind(autoDeleteQueue1).to(fanout);
    }

    @Bean
    public Binding fanoutBinding2(FanoutExchange fanout,
    								 	   Queue autoDeleteQueue2) {
        return BindingBuilder.bind(autoDeleteQueue2).to(fanout);
    }
    
    @Bean
    public Binding fanoutBinding3(FanoutExchange fanout,
    							  Queue autoDeleteQueue3) {
    	return BindingBuilder.bind(autoDeleteQueue3).to(fanout);
    }
    
    @Bean
    public DirectExchange direct() {
    	return new DirectExchange("mq.direct");
    }
    
    @Bean
    public Binding directBinding1(DirectExchange direct,
    							   Queue autoDeleteQueue1) {
    	return BindingBuilder.bind(autoDeleteQueue1)
    						 .to(direct)
    						 .with("black");
    }
    
    @Bean
    public Binding directBinding2(DirectExchange direct,
    		Queue autoDeleteQueue2) {
    	return BindingBuilder.bind(autoDeleteQueue2)
    			.to(direct)
    			.with("yellow");
    }
    
    @Bean
    public Binding directBinding3(DirectExchange direct,
    		Queue autoDeleteQueue2) {
    	return BindingBuilder.bind(autoDeleteQueue2)
    			.to(direct)
    			.with("red");
    }
    
    @Bean
    public TopicExchange topic() {
    	return new TopicExchange("mq.topic");
    }
    
    @Bean
    public Binding topicBinding1(TopicExchange topic,
    							   Queue autoDeleteQueue1) {
    	return BindingBuilder.bind(autoDeleteQueue1)
    						 .to(topic)
    						 .with("*.brown.*");
    }
    
    @Bean
    public Binding topicBinding2(TopicExchange topic,
    		Queue autoDeleteQueue2) {
    	return BindingBuilder.bind(autoDeleteQueue2)
    			.to(topic)
    			.with("*.*.rabbit");
    }
    
    @Bean
    public Binding topicBinding3(TopicExchange topic,
    		Queue autoDeleteQueue2) {
    	return BindingBuilder.bind(autoDeleteQueue2)
    			.to(topic)
    			.with("lazy.#");
    }
    
    @Bean
    public Binding rpcBinding(DirectExchange direct,
		   Queue autoDeleteQueue1) {
		return BindingBuilder.bind(autoDeleteQueue1)
							 .to(direct)
							 .with("rpc");
	}
    
    // listener setting
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory() {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory());
        factory.setConcurrentConsumers(3);
        factory.setMaxConcurrentConsumers(10);
        return factory;
    }
}
