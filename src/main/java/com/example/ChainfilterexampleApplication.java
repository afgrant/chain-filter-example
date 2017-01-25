package com.example;

import java.io.File;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.InboundChannelAdapter;
import org.springframework.integration.annotation.Poller;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.file.FileReadingMessageSource;
import org.springframework.integration.file.filters.AcceptOnceFileListFilter;
import org.springframework.integration.file.filters.LastModifiedFileListFilter;
import org.springframework.integration.file.filters.SimplePatternFileListFilter;
import org.springframework.messaging.MessageHandler;

@SpringBootApplication
public class ChainfilterexampleApplication {

	public static void main(String[] args) {
		SpringApplication.run(ChainfilterexampleApplication.class, args);
		System.out.println("Polling directory: " + Paths.get(".").toAbsolutePath());
	}

	@Bean
	@InboundChannelAdapter(channel = "txtFilesIn", poller = @Poller(fixedDelay = "2000", maxMessagesPerPoll = "10"))
	public MessageSource<File> deviceSettingsFileReader() {
		FileReadingMessageSource source = new FileReadingMessageSource();
		source.setDirectory(Paths.get(".").toFile());

		// accept only *.txt files
		ChainFileListFilter<File> filters = new ChainFileListFilter<>();
		filters.addFilter(new SimplePatternFileListFilter("*.txt"));

		// only pull in a file if it's been unmodified for at least 5 seconds
		LastModifiedFileListFilter lastModifiedFilter = new LastModifiedFileListFilter();
		lastModifiedFilter.setAge(5, TimeUnit.SECONDS);
		filters.addFilter(lastModifiedFilter);

		// only accept each file once
		filters.addFilter(new AcceptOnceFileListFilter<File>());

		source.setFilter(filters);
		return source;
	}

	@Bean
	@ServiceActivator(inputChannel = "txtFilesIn")
	public MessageHandler handler() {
		return message -> {
			System.out.println("Received message: " + message);
		};
	}

}
