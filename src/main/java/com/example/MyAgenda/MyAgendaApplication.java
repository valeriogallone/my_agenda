package com.example.MyAgenda;

import com.example.MyAgenda.frame.MyAgendaFrame;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import javax.swing.*;

@SpringBootApplication
public class MyAgendaApplication {

	public static void main(String[] args) {
		SpringApplicationBuilder builder = new SpringApplicationBuilder(MyAgendaApplication.class);
		builder.headless(false);
		ConfigurableApplicationContext context = builder.run(args);

		SwingUtilities.invokeLater(() -> {
			MyAgendaFrame frame = context.getBean(MyAgendaFrame.class);
			frame.initialize();
		});
	}
}


