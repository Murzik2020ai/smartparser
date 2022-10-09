package com.example.smartParser;


import com.rabbitmq.client.DeliverCallback;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import java.nio.charset.StandardCharsets;

@SpringBootApplication
@Controller

public class SmartParserApplication {
	private static final Logger log = Logger.getLogger(SmartParserApplication.class.getName());
	//enum for bussiness roles
	public enum roles {
		DIR("dir"),
		BUH("buh");
		private String title;
		roles (String title) {
			this.title = title;
		}

		@Override
		public String toString() {
			return title;
		}
	}

	public static void main(String[] args) {

		SpringApplication.run(SmartParserApplication.class, args);
		log.info("app started at localhost:8080");
	}
	@GetMapping("/")
	public String homepage () {
		return "homepage";
	};
	@GetMapping("/rbc")
	public  String rbc () throws IOException {
		ParserRbc rbc = new ParserRbc("./src/main/resources/static/test_id.csv");
		boolean parsingResult = rbc.doParsing();
		if (parsingResult) {
			return "rbc";
		} else {
			return "errorwhileparsing";
		}
	}

	@GetMapping("/torabbitdir")
	public String rabbitdir() throws Exception {
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("localhost");
		try (Connection connection = factory.newConnection();
			Channel channel = connection.createChannel()) {
			channel.queueDeclare(roles.DIR.toString(),false,false,false,null);
			String message = "hello dir";
			channel.basicPublish("",roles.DIR.toString(), null,
					message.getBytes(StandardCharsets.UTF_8));
			System.out.println("send to dir "+message);
		}
		return "torabbit";
	}
	@GetMapping("/torabbitbuh")
	public String rabbitbuh() throws Exception {
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("localhost");
		try (Connection connection = factory.newConnection();
			 Channel channel = connection.createChannel()) {
			channel.queueDeclare(roles.BUH.toString(), false,false,false,null);
			String message = "hello buh";
			channel.basicPublish("",roles.BUH.toString(), null,
					message.getBytes(StandardCharsets.UTF_8));
			System.out.println("send to buh "+message);
		}
		return "torabbit";
	}

	@GetMapping("/sendtodir")
	public String sendToDir () throws Exception {
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("localhost");
		Connection connection = factory.newConnection();
		Channel channel = connection.createChannel();
		channel.queueDeclare("dir",false,false,false,null);
		System.out.println("waiting for messages. press CTRL+C to exit");
		DeliverCallback deliverCallback = (consumerTag,delivery) -> {
			String message = new String(delivery.getBody(),StandardCharsets.UTF_8);
			System.out.println("send to dir : "+message);
		};
		channel.basicConsume("dir",true,deliverCallback,consumerTag -> {});
		return "sendtodir";
	}
	@GetMapping("/sendtobuh")
	public String sendToBuh () throws Exception {
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("localhost");
		Connection connection = factory.newConnection();
		Channel channel = connection.createChannel();
		channel.queueDeclare(roles.BUH.toString(), false,false,false,null);
		System.out.println("waiting for messages. press CTRL+C to exit");
		DeliverCallback deliverCallback = (consumerTag,delivery) -> {
			String message = new String(delivery.getBody(),StandardCharsets.UTF_8);
			System.out.println("send to buh : "+message);
		};
		channel.basicConsume(roles.BUH.toString(),true,deliverCallback,consumerTag -> {});
		return "sendtobuh";
	}
}
