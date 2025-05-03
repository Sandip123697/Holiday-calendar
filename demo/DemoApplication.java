package com.example.calendar.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import javax.swing.SwingUtilities;

@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) {
		System.setProperty("java.awt.headless", "false");

		SpringApplication.run(DemoApplication.class, args);
		

		SwingUtilities.invokeLater(() -> {
			new SimpleCalendarApp();
		});
	}

}
