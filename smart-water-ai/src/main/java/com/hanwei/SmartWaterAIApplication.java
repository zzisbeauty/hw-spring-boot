package com.hanwei;

import com.hanwei.core.util.oConvertUtils;
import com.hanwei.process.server.server.WebSocketServer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.web.socket.config.annotation.EnableWebSocket;

import java.net.InetAddress;
import java.net.UnknownHostException;

/*
1. @SpringBootApplication 这是最关键的注解，它是以下三个注解的组合：
    @Configuration：说明该类是配置类；
    @EnableAutoConfiguration：启用 Spring Boot 的自动配置机制；
    @ComponentScan：自动扫描同级和子包下的 @Component、@Service、@Controller 等注解。  (basePackages = "com.hanwei") ---> bean
*/
@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
@Slf4j // 日志
@EnableScheduling // 启用 Spring 的定时任务功能。
@EnableDiscoveryClient // 启用服务发现客户端功能（通常用于微服务架构）。 ---> 注册微服务客户端
@EnableWebSocket // 启用 WebSocket 支持。
public class SmartWaterAIApplication extends SpringBootServletInitializer {
    // SpringBootServletInitializer
    //      作用是支持将应用打包为 WAR 文件并部署到外部 Servlet 容器（如 Tomcat）。
    //      如果你只使用内嵌容器（如 Jar 包运行），这行不是必须的，但保留也无害。

    // 启动 Spring Boot 应用，返回 Spring 容器上下文
    public static void main(String[] args) throws UnknownHostException {
        // 启动 spring 容器
        ConfigurableApplicationContext application = SpringApplication.run(SmartWaterAIApplication.class, args);
        //解决WebSocket不能注入的问题
        WebSocketServer.setApplicationContext(application);
        Environment env = application.getEnvironment();
        String ip = InetAddress.getLocalHost().getHostAddress();
        String port = env.getProperty("server.port");
        String path = oConvertUtils.getString(env.getProperty("server.servlet.context-path"));
        log.info("\n----------------------------------------------------------\n\t" +
                "Application SmartWaterAIApplication is running! Access URLs:\n\t" +
                "Local: \t\thttp://localhost:" + port + path + "/\n\t" +
                "External: \thttp://" + ip + ":" + port + path + "/\n\t" +
                "Swagger文档: \thttp://" + ip + ":" + port + path + "/doc.html\n" +
                "----------------------------------------------------------");
    }
}
