package com.digitalfix.workorders.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String DIRECT_EXCHANGE = "cmd.direct";
    public static final String TOPIC_EXCHANGE = "cmd.topic";
    public static final String DEAD_LETTER_EXCHANGE = "cmd.dead.dlx";

    public static final String EMAIL_QUEUE = "q.cmd.email";
    public static final String DISPATCH_QUEUE = "q.cmd.dispatch";
    public static final String REPORT_QUEUE = "q.cmd.report";

    public static final String EMAIL_DLQ = "q.cmd.email.dlq";
    public static final String DISPATCH_DLQ = "q.cmd.dispatch.dlq";
    public static final String REPORT_DLQ = "q.cmd.report.dlq";

    public static final String EMAIL_ROUTING_KEY = "email.send";
    public static final String DISPATCH_ROUTING_KEY = "dispatch.ticket";
    public static final String REPORT_ROUTING_KEY = "report.gen";

    private static final int MESSAGE_TTL_MS = 24 * 60 * 60 * 1000;
    private static final int DLQ_TTL_MS = 7 * 24 * 60 * 60 * 1000;
    private static final int MAX_LENGTH = 10000;
    private static final int DLQ_MAX_LENGTH = 5000;

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public DirectExchange directExchange() {
        return new DirectExchange(DIRECT_EXCHANGE, true, false);
    }

    @Bean
    public TopicExchange topicExchange() {
        return new TopicExchange(TOPIC_EXCHANGE, true, false);
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(DEAD_LETTER_EXCHANGE, true, false);
    }

    @Bean
    public Queue emailQueue() {
        return workQueue(EMAIL_QUEUE, EMAIL_DLQ);
    }

    @Bean
    public Queue dispatchQueue() {
        return workQueue(DISPATCH_QUEUE, DISPATCH_DLQ);
    }

    @Bean
    public Queue reportQueue() {
        return workQueue(REPORT_QUEUE, REPORT_DLQ);
    }

    @Bean
    public Queue emailDlq() {
        return deadLetterQueue(EMAIL_DLQ);
    }

    @Bean
    public Queue dispatchDlq() {
        return deadLetterQueue(DISPATCH_DLQ);
    }

    @Bean
    public Queue reportDlq() {
        return deadLetterQueue(REPORT_DLQ);
    }

    @Bean
    public Binding emailDirectBinding(Queue emailQueue, DirectExchange directExchange) {
        return BindingBuilder.bind(emailQueue).to(directExchange).with(EMAIL_ROUTING_KEY);
    }

    @Bean
    public Binding dispatchDirectBinding(Queue dispatchQueue, DirectExchange directExchange) {
        return BindingBuilder.bind(dispatchQueue).to(directExchange).with(DISPATCH_ROUTING_KEY);
    }

    @Bean
    public Binding reportDirectBinding(Queue reportQueue, DirectExchange directExchange) {
        return BindingBuilder.bind(reportQueue).to(directExchange).with(REPORT_ROUTING_KEY);
    }

    @Bean
    public Binding emailTopicBinding(Queue emailQueue, TopicExchange topicExchange) {
        return BindingBuilder.bind(emailQueue).to(topicExchange).with("email.*");
    }

    @Bean
    public Binding dispatchTopicBinding(Queue dispatchQueue, TopicExchange topicExchange) {
        return BindingBuilder.bind(dispatchQueue).to(topicExchange).with("dispatch.#");
    }

    @Bean
    public Binding reportTopicBinding(Queue reportQueue, TopicExchange topicExchange) {
        return BindingBuilder.bind(reportQueue).to(topicExchange).with("report.*");
    }

    @Bean
    public Binding emailDlqBinding(Queue emailDlq, DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(emailDlq).to(deadLetterExchange).with(EMAIL_DLQ);
    }

    @Bean
    public Binding dispatchDlqBinding(Queue dispatchDlq, DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(dispatchDlq).to(deadLetterExchange).with(DISPATCH_DLQ);
    }

    @Bean
    public Binding reportDlqBinding(Queue reportDlq, DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(reportDlq).to(deadLetterExchange).with(REPORT_DLQ);
    }

    private Queue workQueue(String name, String dlqName) {
        return QueueBuilder.durable(name)
                .ttl(MESSAGE_TTL_MS)
                .maxLength(MAX_LENGTH)
                .deadLetterExchange(DEAD_LETTER_EXCHANGE)
                .deadLetterRoutingKey(dlqName)
                .build();
    }

    private Queue deadLetterQueue(String name) {
        return QueueBuilder.durable(name)
                .ttl(DLQ_TTL_MS)
                .maxLength(DLQ_MAX_LENGTH)
                .build();
    }
}
