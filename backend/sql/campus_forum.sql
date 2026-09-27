/*
 Navicat Premium Dump SQL

 Source Server         : 本地数据库
 Source Server Type    : MySQL
 Source Server Version : 80036 (8.0.36)
 Source Host           : localhost:3306
 Source Schema         : campus_forum

 Target Server Type    : MySQL
 Target Server Version : 80036 (8.0.36)
 File Encoding         : 65001

 Date: 25/05/2026 14:46:13
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for forum_board
-- ----------------------------
DROP TABLE IF EXISTS `forum_board`;
CREATE TABLE `forum_board`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '板块ID',
  `name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '板块名称',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '板块描述',
  `sort` int NULL DEFAULT 0 COMMENT '排序优先级',
  `icon` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '图标',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '板块表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of forum_board
-- ----------------------------
INSERT INTO `forum_board` VALUES (1, '校园杂谈', '聊聊学校里的新鲜事', 1, NULL, '2025-11-21 13:24:10');
INSERT INTO `forum_board` VALUES (2, '技术交流', '代码、算法、作业求助', 2, NULL, '2025-11-21 13:24:10');
INSERT INTO `forum_board` VALUES (3, '二手交易', '毕业甩卖，好物流转', 3, NULL, '2025-11-21 13:24:10');
INSERT INTO `forum_board` VALUES (5, '考试资料', '考试资料', 0, '', '2026-02-10 13:20:02');
INSERT INTO `forum_board` VALUES (6, '体育知识', '交单的体育知识交流', 6, '', '2026-05-25 14:34:05');

-- ----------------------------
-- Table structure for forum_comment
-- ----------------------------
DROP TABLE IF EXISTS `forum_comment`;
CREATE TABLE `forum_comment`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `post_id` bigint NOT NULL COMMENT '所属帖子',
  `user_id` bigint NOT NULL COMMENT '评论人',
  `content` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '评论内容',
  `parent_id` bigint NULL DEFAULT NULL COMMENT '父评论ID(用于盖楼，可空)',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `deleted` tinyint(1) NULL DEFAULT 0,
  `root_id` bigint NULL DEFAULT 0 COMMENT '根评论ID',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_post`(`post_id` ASC) USING BTREE,
  INDEX `idx_root_id`(`root_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 30 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '评论表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of forum_comment
-- ----------------------------
INSERT INTO `forum_comment` VALUES (9, 2, 1, '太贵了', 0, '2025-11-22 17:34:49', 0, 0);
INSERT INTO `forum_comment` VALUES (10, 2, 1, '回复 @ljx1230 : 死穷逼', 9, '2025-11-22 17:34:58', 0, 9);
INSERT INTO `forum_comment` VALUES (11, 3, 1, '当然可以', 0, '2025-11-22 20:55:06', 0, 0);
INSERT INTO `forum_comment` VALUES (12, 3, 1, '回复 @ljx1230 : 被裁员前的幻想罢了', 11, '2025-11-22 20:55:24', 0, 11);
INSERT INTO `forum_comment` VALUES (13, 1, 1, '测试1', 0, '2025-11-23 20:13:16', 0, 0);
INSERT INTO `forum_comment` VALUES (14, 1, 1, '测试2', 0, '2025-11-23 20:14:19', 0, 0);
INSERT INTO `forum_comment` VALUES (15, 1, 2, '测试2', 0, '2025-11-23 20:15:15', 0, 0);
INSERT INTO `forum_comment` VALUES (16, 1, 2, '再测试一下', 0, '2025-11-24 15:28:43', 0, 0);
INSERT INTO `forum_comment` VALUES (17, 1, 2, '测试一下一键清空1', 0, '2025-11-24 15:29:04', 0, 0);
INSERT INTO `forum_comment` VALUES (18, 1, 2, '测试一下一键清空2', 0, '2025-11-24 15:29:06', 0, 0);
INSERT INTO `forum_comment` VALUES (19, 1, 2, '测试一下一键清空3', 0, '2025-11-24 15:31:57', 0, 0);
INSERT INTO `forum_comment` VALUES (20, 1, 2, '测试一下一键清空4', 0, '2025-11-24 15:32:00', 0, 0);
INSERT INTO `forum_comment` VALUES (21, 1, 2, '测试一下一键清空5', 0, '2025-11-24 15:32:03', 0, 0);
INSERT INTO `forum_comment` VALUES (22, 7, 6, '您好！', 0, '2026-02-10 13:01:52', 0, 0);
INSERT INTO `forum_comment` VALUES (23, 6, 4, '哈哈哈不错哦', 0, '2026-05-25 11:18:23', 0, 0);
INSERT INTO `forum_comment` VALUES (24, 9, 4, '123', 0, '2026-05-25 11:34:53', 0, 0);
INSERT INTO `forum_comment` VALUES (25, 13, 4, '好哦', 0, '2026-05-25 11:53:26', 0, 0);
INSERT INTO `forum_comment` VALUES (26, 13, 7, '回复 @admin : 确实写的很不错哦', 25, '2026-05-25 13:39:07', 0, 25);
INSERT INTO `forum_comment` VALUES (27, 13, 7, '好哦', 0, '2026-05-25 13:39:14', 0, 0);
INSERT INTO `forum_comment` VALUES (28, 13, 4, '确实写的不错，还有吗', 0, '2026-05-25 14:30:53', 0, 0);

-- ----------------------------
-- Table structure for forum_post
-- ----------------------------
DROP TABLE IF EXISTS `forum_post`;
CREATE TABLE `forum_post`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '帖子ID',
  `board_id` int NOT NULL COMMENT '板块ID',
  `user_id` bigint NOT NULL COMMENT '作者ID',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '标题',
  `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '内容(Markdown源码)',
  `view_count` int NULL DEFAULT 0 COMMENT '浏览量',
  `reply_count` int NULL DEFAULT 0 COMMENT '回复数',
  `is_top` tinyint(1) NULL DEFAULT 0 COMMENT '是否置顶',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint(1) NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_board`(`board_id` ASC) USING BTREE,
  INDEX `idx_user`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 16 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '帖子表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of forum_post
-- ----------------------------
INSERT INTO `forum_post` VALUES (10, 2, 4, 'Vue3速成】01-npm+vue初体验+vite构建vue工程化', '一、npm常见的命令\n1、项目初始化\n生成 package.json 项目配置文件（等价于 Maven 的 pom.xml，里面有对应的依赖）\n\nnpm init\n交互式填写项目信息，一步步生成配置文件\nnpm init -y （y代表的是yes）\n快速初始化，所有选项使用默认值，直接生成 package.json\n\n2、安装依赖\n注意：只有已经有了 package.json 配置文件之后我们才可以安装依赖\n语法：\nnpm install 依赖名称 或者 npm i 依赖名称\n比如：安装vue\n\n\n下载完成之后：多了node_modules 和 package-lock.json\n其中package-lock.json存放详细的依赖信息，而node_modules 存的是你下载的依赖。\n\n2.1 常用命令\nnpm install 包名 / npm i 包名\n安装项目依赖（写入 dependencies）\nnpm install 包名@版本号\n安装指定版本的依赖\nnpm install -g 包名\n安装全局依赖，所有项目均可使用\nnpm install\n根据 package.json 安装所有依赖\n其中注意npm i在哪里使用？\n场景如下：\n\n3、升级依赖\nnpm update 包名\n将指定依赖升级到最新兼容版本\n4、卸载依赖\nnpm uninstall 包名\n卸载项目依赖，并自动更新配置文件\n5、查看依赖\nnpm ls\n查看当前项目的所有依赖\nnpm list -g\n查看全局安装的所有依赖\n6、运行脚本命令（npm run）\n语法：\nnpm run 运行脚本的键\n\n什么事运行脚本中的键呢？请看下述：\n\n然后怎么运行呢？如下所示：\n\n\n二、vue3初体验\n1、要有一个基本的标签\n\n\n2、导入vue框架\n首先我们的vue是一堆js写的，那么你想要人家写的框架，你就得导进来呀 👇\n\n\n3、创建vue对象\n\n\n4、设置值\n你创建vue对象干什么？我们就是为了设置值呀，怎么设置？\n你看好：\n\n\n然后给我在里面设置对应的值：\n\n\n那么请问：你设置这个msg的值干什么？有什么用？就只是干巴巴的设置？\n咱们肯定是要在页面展示的呀，那谁来展示？肯定是html标签来展示呀，所以你得将这个msg值返回给html标签，👇\n\n\n那么，此时，你返回就完了吗？肯定不是呀，你说让他给html标签展示，那你告诉他是哪一个标签了么？没有啊，所以我们就得告诉他你要在哪一个表情中展示，所以下一步就是：挂载\n\n5、挂载\n\n\n如何访问？\n使用{{变量}}访问👇\n\n\n结果：\n\n\n注意事项：\n\n\n\n那么后续我们学习vite构建工程化之后，我们是不需要写什么creatApp和setup以及mount的，我们直接写属性和函数就行\n\n三、使用vite构建vue前端工程化\nvite是构建工程化的脚手架，帮我们创建工程，帮我们提前设置好一些依赖\n\n1、创建工程\n\n如何使用vite构建vue项目呢？\n输入：npm create vite\n\n', 0, 0, 1, '2026-05-25 11:36:48', '2026-05-25 11:36:48', 0);
INSERT INTO `forum_post` VALUES (11, 1, 4, '珍惜最后的学校生活', '昨天听了FunPlus的宣讲会，前几天FPX战队3比0横扫G2战队，夺得2019英雄联盟全球总决赛冠军，晚上11点对面男生宿舍在那喊着牛逼，略有点小震撼，所以冲着入场精美小礼品和抽奖礼物就去了，礼品的确精美。\n\n以上是题外话，忙完工作差不多就要开始准备大论文了。\n\n接下来的半年，我可以好好做喜欢的研究、看喜欢的书，成为一个社会人以后，就没有这么纯粹的时间和精力来做喜欢的事了！\n\n慢慢地把之前开的关于机器学习、深度学习还有其他七七八八的坑，尽力而为地填上，然后再开新的坑，具体是什么还没想好。\n\n可能把前端的东西系统地捋一遍，毕竟大学的时候我可是励志考不上研就去当一名前端工程师的；或者把PS精通一下；或者学一下视频剪辑；或者系统学一下GO做做服务器端开发；devops也不错……未来嘛，有无限的可能性，碰巧我爱做梦喜欢折腾，所以拭目以待吧！\n\n只是现在研究的东西可能要放下了，内心有点不舍和难过，离开学校，没有免费的论文库、没有可以支持的设备有点难。\n\nFighting！\n\n忍不住的絮絮叨叨：深度学习的东西看的越多，越觉得迷。很多论文都拿实验数据说话，只是这个数据真真假假谁说得清楚？到底为什么要这么做又很难说清楚，开组会的时候导师也经常提到这个问题。深度学习发论文好发加上算法高价，太多硕士生一股脑转深度学习找算法，也不知道深度学习的泡沫还会持续多久？\n\n工作一年以后的碎碎念\n\n这部分是写给自己看的，于2021年10月26日 @ NanGC\n\n        没有新开一个帖子，所以可能没有人会看到，只不过还是喜欢唠唠叨叨地说些有的没的。\n\n         转眼毕业已经一年零4个月了，开始工作一年零2个月，分手一年零1个月。这一年过的可真是漫长。秉持着“低调做事”的原则，在公司不会像在学校那样锋芒毕露、高傲的不可一世了，感觉自己还是有很大的进步，很幸运碰到了一群很好的同事和两个待我很好的师父。从一个项目组换到了更大的项目组，犯了很多的错，接触了不少新东西，尽管对工作还是不够满意，只是，大概还是慢慢来吧，别再还没学会走路的时候就想着跑步前进……\n\n        去年年底到今年年初，一到难过迷惘的时候就弹尤克里里，水平也是精进了不少。暑假的时候开始织围巾，想着过年给长辈们一个小礼物。工作以后的第一次体检让我猝不及防，体检的威慑下终于办了健身卡，开始练瑜伽学古典舞，很快乐也很充实。\n\n        尽管不总是那么地尽如人意，生活总归还在正常的轨道上不急不缓地前进着。好好热爱和享受生活，珍惜眼前的人和事……我知道自己其实很平凡，但依旧相信自己会变得更好，也依旧相信自己可以成为自己想成为的样子。\n', 0, 0, 1, '2026-05-25 11:38:05', '2026-05-25 11:38:05', 0);
INSERT INTO `forum_post` VALUES (12, 1, 4, '校园招聘面试', '近期参与了几个大学的校园招聘，总体下来感觉还行，由于校园招聘需要面的人很多，差不多面试流程都形成模式了，在面试的过程中，有不少学生问过我，到底面 试的标准是什么，不过每个面试官的标准都是不同的，所以也就注定了面试是会有些不公平的，是否对面试官的胃口会起到很大的决定性因素，当然，最重要的还是 实力，很多学生会认为面试不公平，但我觉得这也算是从学校进入社会的第一课吧，工作后学生们会发现更多不公平的事，对于学生而言，无论是应届毕业的本科、 硕士，我的面试标准都差不多，考察的为Java基础、Java框架、设计模式、互联网架构的了解，当然，在最后会问一些其他的问题，例如大学学习情况呀、 一两年后对自己的期望呀、优势和不足、最近看过的技术新闻等等，在这些所有的问题的背后，考察的最重要的是基础掌握的是否扎实、学习能力、反应速度、抗压 能力以及技术兴趣。\n对于应届生而言，通常都没有太多的实际的商业项目的经验，更多要求是基础的扎实，因此第一关会是java基础的考察，在java基础的考察上通常我会考察 下学生对于protected、static等等的掌握程度，在面试的过程中，几乎所有的同学都认为自己熟悉的包是集合，其他的包都谈不上熟悉，在集合包 方面可以考察的点也很多，例如List、Set的区别；HashMap的实现方式等；在基础方面，线程、通信、远程调用、并发、GC等这些会成为加分项， 我觉的如果学生能对这些有掌握的话就更好了。\n第二关会是Java框架的考察，在这方面会考察学生对于自己认为的最熟悉的框架的掌握程度，然后会考察学生对这类框架中的核心思想的掌握情况，例如 Struts，那么就可以考察学生对于MVC思想的掌握情况，自主如何实现MVC框架；例如spring，可以考察如何自主实现一个DI框架等，这些题目 其实如果学生具备很强的举一反三和反向推理的能力，基本是可以答出来的。\n第三关会是设计模式的考察，这关我觉得基本已经属于加分环节了，设计模式方面可以让学生当场写一段自己最熟悉的模式的代码，例如singleton模式，有很多种写法，可以问下学生各种写法有什么不同。\n第四关为互联网架构的了解状况，这关纯属加分环节，如果能够对互联网的架构有所了解的话，会非常有帮助。\n第一关和第二关属于通关制，如果顺利的通过了第一关和第二关，其实基本也就过了，之后就可以聊聊在大学的学习情况、一两年后的期望、最近看过的技术新闻、大学期间做过的最有成就感的事等等，尽可能的更加全面的了解学生。\n根据整体的面试情况来看，现在应届生找工作的压力确实非常的大，而且大部分都已经是硕士了，本科生能过笔试的都不多，能过面试的就更少了，不过也会有就 是，面过一个不错的本科生，竟然对jdk的很多代码都有阅读、开源框架上spring DI那部分的代码也仔细的阅读过，并且他去阅读这些代码的原因就是他认为这些代码应该是会写的不错的，需要仔细看看，:)，这我觉得基本是典型的技术人 士，另外，还碰到过一个硕士生，他对于Java框架几乎完全不了解，但他有个很明显的特征，就是只要他用过、学过的Java包什么的，例如 ArrayList、HashMap等等，他都能做到从头到尾的掌握，可以称的上是精通了，这种我觉得很适合做专业型的基础技术，:)\n很多应届生会觉得是因为大学中没教这些，所以导致他们面试很难通过，但我觉得这还是对于技术的兴趣以及自学能力等决定的，大学中最不缺的应该就是时间，完 全有足够的时间看看一些源码什么。加强自己的知识体系，为离开学校、进入社会工作做好充足的准备，另外，在面试的时候一定要诚实，不懂的就是不懂，没什么 的，不能完全答对面试的问题并不代表就被淘汰了，希望这篇blog能给大学生们提供一点帮助，:)。\n————————————————', 1, 0, 0, '2026-05-25 11:38:51', '2026-05-25 11:38:51', 0);
INSERT INTO `forum_post` VALUES (13, 3, 4, '基于微信小程序的大学生二手物品闲置交易系统', '1.1课题背景及意义\n互联网具有传统产业所缺乏的许多固有优势。信息的传播不再受地域限制，具有高效传播的特点。闲置资源市场一直存在，但是由于缺乏交易渠道，许多仍然有价值的商品经常被丢弃或闲置，这不仅造成资源浪费，而且对环境产生一定的影响。如果将互联网与闲置市场连接起来，就可以通过互联网的交流有效地刺激对闲置商品交易的需求，从而充分利用闲置商品。 \n\n大学生消费行为的发生本质上是物质上或精神上的需要。作为一个年轻的群体，与其他群体相比，大学生的需求更加旺盛，并且能够接受新事物，这确保了大学生的消费水平。但是，作为一个年轻的群体，消费者的行为受到许多心理因素的影响，往往不能真正客观地真正满足自己的需求，容易出现冲动性消费现象，导致购买的商品不符合自身的实际需求或没有太多需求使用的机会。而大学生作为一个青年群体还没有进入社会，大部分学生的收入来自家庭供给，在很大程度上无法满足自己强大的消费需求，闲散物品具有很高的性价比，到了在一定程度上可以匹配大学生的特点。此外，一些研究表明，年轻人更喜欢互联网和在线购物，因为它们可以减少时间成本和人际关系成本。\n\n1.2 国内外研究现状\n国内离线闲置商品交易市场主要集中在学校附近，通常以临时跳蚤市场或集市的形式出现。学生可以将不常用或完全闲置的物品拿到手中，并委托他人或自己在这里进行交易。但是，这种交易通常是分散的，并且在时间和空间上都是不确定的。这通常会导致大多数学生无法满足离线市场需求的问题。通常，闲置货物的人没有机会出售，需要购买的人没有购买渠道。这种情况通常无法满足消费者群体的需求。 \n\n与线下低迷相比，中国在线闲置商品平台的开发如火如荼。自2014年6月上线休闲鱼购物平台以来，依托大型互联网企业阿里巴巴，在发展过程中获得了大量的流量和关注，逐步带动了国内闲置市场的巨大需求。 。到2018年，该平台的交易量已超过1000亿，这表明中国对闲置商品交易的需求相当大。紧随其后的是腾讯和58家转砖的支持也涉足市场，也获得了很好的关注度和收益。此外，与先玉和专撰等集成平台相比，还有许多其他专注于垂直细分的平台。爱回收是杰出的代表之一。该平台使用c2b2cm商业模式专注于电子数字领域。也就是说，用户之间没有直接交易，交易只能通过转账平台产生。该平台负责购买和提供第三方检测，可以在一定程度上反映二手商品的真实属性。这种方式有效地解决了双方无法信任的痛点，在电子和数字领域受到消费者的追捧和追捧。 \n\n目前，国内市场上的绝大多数平台都是面向社会的二手物业交易系统。这些平台以互联网为信息传播媒介，以物流系统为实物运输渠道，可以很好地满足社会对二手物业资源交易的需求。但是，国内校园的情况是有其特殊性的，但是校园建设的旧事物交易系统很少，主要包括陶尔陶，柚子校园，校一等平台。造成这种情况的原因并不是校园没有市场。相反，由于学生群体密集，购物需求旺盛，善于接受新事物，短期居住等因素，校园内有庞大的二手商品市场。每个毕业季节学生组织的校园跳蚤市场也证明了这一点。这种应用平台的功能类似，以西柚园为例，其主要功能是信息发布平台，有购买需求的学生可以在该平台上获得卖家的联系方式。但是，由于功能因素，系统中存在大量过时和无效的信息，给学生购买时带来不好的体验。另一方面，由于客观因素的限制，这种平台主要依靠学生的独立运作，导致用户群不足，交易量无法令人满意。总体而言，校园内的旧物资源市场相当庞大，但由于缺乏成熟的市场运作模式和经验，许多需求无法满足。\n\n微信是腾讯公司在 2011 年发布的一款为智能终端提供的即时通讯应用程序。微信软件的使用与下载是完全免费的，只需少量的网络流量费用。同时，其使用不受移动终端系统和运营商的限制，已成为目前亚洲地区拥有最大用户群体的移动即时通讯软件。随着互联网技术的不断发展，微信也在不断拓展着自身的功能来更好的满足人们对于软件功能的需要。比如我们现在很熟悉也很常用的钱包支付功能，以及用来推送 文章的微信公众号平台，还有一些订阅号，企业号等功能。目前微信已经被应用到我们日常生活中的各个领域，例如广播电台，旅游服务等方面。在教育领域中，关于如何将微信应用于教育中的研究也在不断深入。有学者依托微信构建一些学习交流平台，希望可以在学科教学中起到一定的助推作用，一些学校教师也通过创设学科学习微信公众号，初步构建基于微信的新型微学习模式，探讨微学习模式在优化教学课堂中的应用。也有的学者提出利用微信建立生活中的应用，提供生活的便利性。\n\n微信小程序是在 2017 年基于微信内部而开通的一款最新功能。“微信之父”张小龙曾经说过这样的一句话:“小程序是一种无需下载安装，即可使用的一种应用，小程序的出现实现了我们曾经一直以来对于应用可以实现“触手可及”的期望，用户只需通过扫一扫或者搜一搜就可以轻松快捷的打开自己所需要的功能的对应的应用，这也充分的体现了符合当今快速发展社会的其中“用完即走”的这一理念与趋势，使用小程序的用户们无需再像从前一样，经常关心其手机是否安装了太多应用的问题。2017 年 1 月 9 日,微信小程序正式开放,作为一种不需要下载、即开即使用的线上应用,它最大的好处莫过于节省手机空间。小程序可在微信中置顶或在手机桌面形成快捷方式,这样一来用户可卸载掉手机中冗杂的应用，用小程序来代替。用户不需要担心安装过多应用的问题，也不必为了节省空间而卸掉一些应用，小程序可以通过搜索获得，也可以通过扫描二维码获得，还可以在私聊，群聊界面分享。\n\n微信小程序的开发相对来说也较为简单，在开发微信小程序时，开发者可以直接调用微信自带的 api 功能接口来实现所需功能。用户在使用小程序时也十分简单,用户只需打开自己常用的微信软件，在微信中直接搜索或者直接扫一扫即可进入小程序进行使用。小程序的出现，使开发者的开发周期大大缩短，小程序一经公布，很快就被大家所认可，并且积极应用于各个领域。一些学者将微信小程序用于构建校园失领平台，以及家政服务的预约。生活领域对于微信小程序的应用研究也在不断深入。\n\n主要实现功能包括：\n一、首页：\n\n1、搜索引擎功能，商品推荐板块，发布商品功能。\n\n2、商品信息页面功能有：点赞，收藏，联系卖家，查看卖家个人主页，立即购买功能。\n\n二、订单（包括“我买入的”和“我卖出的”两部分）：\n\n1、功能包括：联系买家，确认收货，确认发货，双方评价，七天后自动收货，退货（同校交易，不用填快递单号）\n\n三、消息\n\n四、我的：\n\n1、编辑“收货信息”，包括：收货人姓名，地址，电话等。\n\n2、编辑个人信息包括：头像，昵称，性别，生日，简介，常住地，学校，兴趣\n\n3、收藏夹、关注、粉丝\n\n4、查看我的闲置\n\n5、查看我的评价\n\n6、查看个人主页\n\n个人主页：\n\n（1）详细信息（包括：昵称，性别，生日，简介，常住地，学校，兴趣）\n\n（2）可以关注他\n\n（3）显示他的所有闲置\n\n（4）查看他的交易评价\n\n7、注册、登录、退出登录。\n', 14, 4, 1, '2026-05-25 11:42:02', '2026-05-25 14:30:53', 0);
INSERT INTO `forum_post` VALUES (14, 5, 7, '收藏干货｜2026 版双非零基础入局大模型开发，RAG 与 Agent 就业上岸全攻略', '日常总能收到不少初学伙伴的私信，大家普遍都有同一个疑惑：二本及普通院校学历，零基础入门 RAG、Agent 大模型应用开发，究竟能不能顺利入职？行业后续发展前景又如何？\n\n本篇 2026 年全新内容，不空谈鸡汤、不夸大前景，也不刻意打击信心，客观剖析当下行业真实行情，梳理适配普通开发者与零基础新手的学习路线，分享实打实的求职上岸思路。\n\nPART 01 先给结论：能，但没你想的那么轻松\n很多双非同学一上来就焦虑：我学历普通，能跟名校生竞争吗？我直接把核心结论整理成表格，一看就懂：\n\n维度	真实结论\n能不能找到工作？	能，但必须用实力和项目，比名校生多证明自己一步\n有没有钱途？	有，当前Agent、RAG落地正处于行业红利期\n学历会不会卡人？	会卡部分大厂简历关，但绝对卡不死普通学历开发者\n真正核心竞争力？	能落地、能交付、能解决真实业务问题\nPART 02 先搞懂：Agent/大模型应用开发，到底是做什么的？\n首先必须纠正一个90%小白都有的误区：\n大模型应用开发 ≠ 大模型算法工程师\n\n一听到“大模型”，很多人立刻想到：预训练、RLHF、千亿参数、千卡集群、顶会论文……\n停！那是大模型研究与训练岗，基本是清北、海外名校、博士的战场，双非本科硬挤这条路，性价比极低，也完全没必要。\n\n而大模型应用开发 / Agent开发，是完全不同的赛道：\n\n不需要你从零训练大模型\n不需要你懂复杂底层算法原理\n你只需要：理解业务 → 设计Agent架构 → 工程化落地 → 持续优化效果\n它更像是传统后端开发的升级版，是工程+产品结合的岗位，而非纯科研岗。\n\n我用一张表，帮你彻底分清传统软件工程和AI Agent研发的区别：\n![Description](https://emoji.cdn.bcebos.com/yige-aigc/pc_workbench_guides/%E5%8F%98%E6%B8%85%E6%99%B0-%E6%89%A7%E8%A1%8C%E5%9B%BE1.png)\n对比维度	传统软件工程研发	AI Agent 研发\n核心目标	实现固定功能与逻辑	实现高级目标与自主行为\n思维模型	命令式：定义每一步怎么做	目标导向式：定义要达成什么结果\n主要产出物	代码、API、服务	Agent架构、Prompt、评估体系、工具链\n核心角色	后端、前端、测试、产品	Agent工程师、提示词工程师、业务专家\n研发模式	线性流程：需求→设计→编码→测试	探索迭代：原型→评估→调优→再迭代\n调试方式	断点Debug、查日志、看变量	追踪Agent思考链、复盘失败案例\n结果确定性	输入固定，输出固定	概率性输出，看重成功率与稳定性\n控制方式	if-else硬编码	LLM实时决策+工具调用\n迭代重心	修Bug、改需求、重构代码	Agent升级、Prompt调优、记忆策略优化!', 10, 0, 0, '2026-05-25 13:54:15', '2026-05-25 14:31:16', 0);
INSERT INTO `forum_post` VALUES (15, 5, 4, '免费领考试资料啦', '注意注意！！ 号外号外，大家来教室领资料了哦哈哈哈\n', 1, 0, 0, '2026-05-25 14:32:10', '2026-05-25 14:32:10', 0);

-- ----------------------------
-- Table structure for sys_message
-- ----------------------------
DROP TABLE IF EXISTS `sys_message`;
CREATE TABLE `sys_message`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `from_id` bigint NOT NULL COMMENT '发送者ID(0代表系统)',
  `to_id` bigint NOT NULL COMMENT '接收者ID',
  `type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '类型: COMMENT/CHAT/SYSTEM',
  `content` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '消息内容或关联ID',
  `is_read` tinyint(1) NULL DEFAULT 0 COMMENT '是否已读',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_to_user`(`to_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 29 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '消息通知表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_message
-- ----------------------------
INSERT INTO `sys_message` VALUES (1, 2, 1, 'COMMENT', 'POST:1:测试2', 1, '2025-11-23 20:15:15');
INSERT INTO `sys_message` VALUES (2, 1, 2, 'CHAT', '测试私信', 1, '2025-11-23 20:18:47');
INSERT INTO `sys_message` VALUES (3, 2, 1, 'CHAT', '测试未读消息1', 1, '2025-11-24 13:49:50');
INSERT INTO `sys_message` VALUES (4, 2, 1, 'CHAT', '测试未读消息2', 1, '2025-11-24 13:50:04');
INSERT INTO `sys_message` VALUES (5, 1, 2, 'CHAT', '123', 1, '2025-11-24 14:50:45');
INSERT INTO `sys_message` VALUES (6, 2, 1, 'CHAT', '测试未读消息3', 1, '2025-11-24 13:50:04');
INSERT INTO `sys_message` VALUES (7, 1, 2, 'CHAT', '222', 1, '2025-11-24 15:27:59');
INSERT INTO `sys_message` VALUES (8, 1, 2, 'CHAT', '测试一下实时性', 1, '2025-11-24 15:28:16');
INSERT INTO `sys_message` VALUES (9, 2, 1, 'COMMENT', 'POST:1:再测试一下', 1, '2025-11-24 15:28:43');
INSERT INTO `sys_message` VALUES (10, 2, 1, 'COMMENT', 'POST:1:测试一下一键清空1', 1, '2025-11-24 15:29:04');
INSERT INTO `sys_message` VALUES (11, 2, 1, 'COMMENT', 'POST:1:测试一下一键清空2', 1, '2025-11-24 15:29:06');
INSERT INTO `sys_message` VALUES (12, 2, 1, 'COMMENT', 'POST:1:测试一下一键清空3', 1, '2025-11-24 15:31:57');
INSERT INTO `sys_message` VALUES (13, 2, 1, 'COMMENT', 'POST:1:测试一下一键清空4', 1, '2025-11-24 15:32:00');
INSERT INTO `sys_message` VALUES (14, 2, 1, 'COMMENT', 'POST:1:测试一下一键清空5', 1, '2025-11-24 15:32:03');
INSERT INTO `sys_message` VALUES (16, 1, 3, 'CHAT', 'hello，可以聊一聊吗？', 1, '2025-11-26 14:36:24');
INSERT INTO `sys_message` VALUES (17, 3, 1, 'CHAT', '可以呀', 1, '2025-11-26 14:36:47');
INSERT INTO `sys_message` VALUES (18, 1, 3, 'CHAT', '??', 1, '2025-11-26 14:37:04');
INSERT INTO `sys_message` VALUES (19, 3, 1, 'CHAT', 'd(=^･ω･^=)b', 1, '2025-11-26 14:37:13');
INSERT INTO `sys_message` VALUES (20, 6, 5, 'COMMENT', 'POST:7:您好！', 0, '2026-02-10 13:01:52');
INSERT INTO `sys_message` VALUES (21, 2, 1, 'CHAT', '你好啊', 0, '2026-05-25 11:21:11');
INSERT INTO `sys_message` VALUES (22, 7, 4, 'COMMENT', 'POST:13:回复了你的评论: 确实写的很不错哦', 1, '2026-05-25 13:39:07');
INSERT INTO `sys_message` VALUES (23, 7, 4, 'COMMENT', 'POST:13:好哦', 1, '2026-05-25 13:39:14');
INSERT INTO `sys_message` VALUES (24, 7, 4, 'CHAT', 'hello，可以聊一聊吗？', 1, '2026-05-25 13:50:21');
INSERT INTO `sys_message` VALUES (25, 7, 4, 'CHAT', '你好啊', 1, '2026-05-25 13:50:29');
INSERT INTO `sys_message` VALUES (26, 4, 7, 'CHAT', '可以哦', 1, '2026-05-25 13:51:20');
INSERT INTO `sys_message` VALUES (27, 4, 7, 'COMMENT', 'POST:14:来了哦', 1, '2026-05-25 14:31:13');
INSERT INTO `sys_message` VALUES (28, 4, 7, 'CHAT', '你有什么事情吗', 1, '2026-05-25 14:33:13');

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码(加密)',
  `nickname` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '昵称',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '头像URL',
  `email` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮箱',
  `score` int NULL DEFAULT 0 COMMENT '积分',
  `role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'USER' COMMENT '角色: USER/ADMIN/MODERATOR',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) NULL DEFAULT 0 COMMENT '逻辑删除: 0正常, 1删除',
  `board_id` int NULL DEFAULT NULL COMMENT '负责的板块ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (1, '154284220', '$2a$10$N1zllEgWwS5.dmBco0oyRu0IRL3yVKgEaWZTxAikoIqqWgSi/cKgC', 'ljx1230', 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png', 'moderator@example.com', 28, 'MODERATOR', '2025-11-21 14:51:02', '2025-11-27 20:38:44', 0, 1);
INSERT INTO `sys_user` VALUES (2, '11223344', '$2a$10$/ZVowxgJMUFcr8UuMPr5GeQTOM0jX/HdfqygK4mMZUpUmPncvB6Pi', '小白的世界', 'http://localhost:8080/file/image/126a7f49-6b80-4828-be6d-09fa948749f7.png', '', 14, 'MODERATOR', '2025-11-21 16:55:30', '2025-11-24 15:32:03', 0, 3);
INSERT INTO `sys_user` VALUES (3, '3207368827', '$2a$10$akdQKYkXVCgWIbZtRiaipebyleXPe9UWrWOoVBmqiOemGEzhdnQf.', 'ljx1232', 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png', NULL, 0, 'USER', '2025-11-26 14:27:41', '2025-11-26 14:27:41', 0, NULL);
INSERT INTO `sys_user` VALUES (4, '154284221', '$2a$10$akdQKYkXVCgWIbZtRiaipebyleXPe9UWrWOoVBmqiOemGEzhdnQf.', '管理员', 'http://localhost:8080/file/image/4fb22111-c0c7-4407-a4ba-8694773fa6eb.jpg', 'admin@example.com', 90, 'ADMIN', '2025-11-26 14:27:41', '2026-05-25 14:32:10', 0, NULL);
INSERT INTO `sys_user` VALUES (5, 'zwz123456', '$2a$10$ou/oC6rasVUM/JILA4qD4e2Xm5HWOSkn.h0kUp3Q49SkEoGbSgY/K', 'zwz123456', 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png', NULL, 10, 'USER', '2026-02-10 12:45:17', '2026-02-10 12:47:07', 0, NULL);
INSERT INTO `sys_user` VALUES (6, '111111', '$2a$10$B4MztFqS/sVZM4Tb830ofuEwTQyd0ePbv208T1czLraqnD63btaOS', '111111', 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png', NULL, 12, 'MODERATOR', '2026-02-10 13:01:43', '2026-02-10 13:21:22', 0, 3);
INSERT INTO `sys_user` VALUES (7, 'zhangsan', '$2a$10$HpmUTyoGzLLKnTfpq0GbKeNNKl24.5.8XpOs5ATkQWinQpPhOloEi', '张三', 'http://localhost:8080/file/image/168d61a7-2fd3-485b-b893-96ebaef55dfc.jpg', '', 14, 'USER', '2026-05-25 13:38:13', '2026-05-25 13:54:15', 0, 5);

SET FOREIGN_KEY_CHECKS = 1;
