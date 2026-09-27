package com.ljx.forum;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;

import java.nio.file.Paths;
import java.util.Collections;

/**
 * @Author: ljx
 * @Date: 2025/11/21 14:02
 */
public class CodeGenerator {

    public static void main(String[] args) {
        // 1. 数据库配置
        String url = "jdbc:mysql://localhost:3306/campus_forum?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai";
        String username = "root";
        String password = "root";

        FastAutoGenerator.create(url, username, password)

                // 2. 全局配置
                .globalConfig(builder -> {
                    builder.author("ljx") // 设置作者
                            .enableSpringdoc()
                            .outputDir(Paths.get(System.getProperty("user.dir")) + "/src/main/java") // 指定输出目录
                            .disableOpenDir(); // 禁止打开输出目录
                })

                // 3. 包配置
                .packageConfig(builder -> {
                    builder.parent("com.ljx.forum") // 设置父包名
                            .entity("entity")
                            .service("service")
                            .serviceImpl("service.impl")
                            .mapper("mapper")
                            .xml("mapper.xml")
                            .controller("controller")
                            .pathInfo(Collections.singletonMap(OutputFile.xml, Paths.get(System.getProperty("user.dir")) + "/src/main/resources/mapper")); // 设置mapperXml生成路径
                })

                // 4. 策略配置
                .strategyConfig(builder -> {
                    builder.addInclude("sys_user", "forum_board", "forum_post", "forum_comment", "sys_message") // 设置需要生成的表名
                            .addTablePrefix("sys_", "forum_") // 设置过滤表前缀 (例如 sys_user -> User)

                            // Entity 策略
                            .entityBuilder()
                            .enableLombok() // 开启 Lombok
                            .enableTableFieldAnnotation() // 开启字段注解

                            // Controller 策略
                            .controllerBuilder()
                            .enableRestStyle(); // 开启 @RestController
                })

                // 5. 模板引擎
                .templateEngine(new FreemarkerTemplateEngine())

                // 6. 执行
                .execute();

        System.out.println("代码生成完毕！");
    }
}
