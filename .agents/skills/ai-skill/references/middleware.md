# 中间件服务

## 目录

1. [定时任务 (Quartz)](#1-定时任务-quartz)
2. [消息队列](#2-消息队列)
3. [服务容错 (Sentinel)](#3-服务容错-sentinel)

---

## 1. 定时任务 (Quartz)

基于 **Quartz + MySQL** 实现分布式任务调度。

### 开发规范

```java
@Component
@TenantJob  // 可选：开启多租户遍历
public class OrderTimeoutJob implements JobHandler {
    @Override
    public String execute(String param) throws Exception {
        // 业务逻辑: 取消超时未支付订单
        return "执行成功";
    }
}
```

### 配置步骤

1. 启动项目
2. 管理后台 → 基础设施 → 定时任务
3. 新增任务，配置 Bean 名称（如 `orderTimeoutJob`）和 Cron 表达式

### 注意事项

- **环境隔离**: `application-local.yaml` 默认禁用定时任务
- 如需本地调试，手动开启 `QuartzAutoConfiguration`

---

## 2. 消息队列

通过 `yudao-spring-boot-starter-mq` 提供多种 MQ 抽象。

### 2.1 内存队列 (Spring Event)

**适用场景**: 单机应用、对消息可靠性要求不高

```java
// 消息定义
@Data
public class SmsSendMessage {
    private String mobile;
    private String content;
}

// 发送
applicationContext.publishEvent(new SmsSendMessage(...));

// 消费
@EventListener
@Async
public void onMessage(SmsSendMessage message) {
    // 处理
}
```

### 2.2 Redis 队列 (推荐)

**适用场景**: 中小型分布式系统，无需额外部署 MQ

- 基于 Redis 5.0 Stream 实现**集群消费**
- 基于 Pub/Sub 实现**广播消费**

```java
// 消息定义
@Data
public class SmsSendMessage extends AbstractRedisStreamMessage {
    private String mobile;
    private String content;
}

// 发送
@Resource
private RedisMQTemplate redisMQTemplate;

redisMQTemplate.send(new SmsSendMessage()
    .setMobile("13800138000")
    .setContent("验证码：1234"));

// 消费
@Component
public class SmsSendConsumer extends AbstractRedisStreamMessageListener<SmsSendMessage> {
    @Override
    public void onMessage(SmsSendMessage message) {
        // 处理短信发送
    }
}
```

### 2.3 RocketMQ

**适用场景**: 高并发、高可靠性的大型系统

```java
// 发送
rocketMQTemplate.syncSend(TOPIC, message);

// 消费
@RocketMQMessageListener(topic = "...", consumerGroup = "...")
public class SmsConsumer implements RocketMQListener<SmsSendMessage> {
    @Override
    public void onMessage(SmsSendMessage message) {
        // 处理
    }
}
```

### 2.4 RabbitMQ

```java
// 发送
rabbitTemplate.convertAndSend(QUEUE, message);

// 消费
@RabbitListener(queues = "...")
@RabbitHandler
public void onMessage(SmsSendMessage message) {
    // 处理
}
```

### 2.5 Kafka

**适用场景**: 极高吞吐量或大数据处理

```java
// 消息定义
@Data
public class SmsSendMessage implements Serializable {
    public static final String TOPIC = "SEND_MESSAGE_TOPIC";
    private String mobile;
}

// 发送
kafkaTemplate.send(SmsSendMessage.TOPIC, message);

// 消费
@KafkaListener(
    topics = SmsSendMessage.TOPIC, 
    groupId = SmsSendMessage.TOPIC + "_CONSUMER"  // 必须指定 groupId
)
public void onMessage(SmsSendMessage message) {
    // 处理
}
```

---

## 3. 服务容错 (Sentinel)

基于 **Sentinel** 实现流量控制、熔断降级。

### 组件

`yudao-spring-boot-starter-protection`

### 使用

```java
@SentinelResource(
    value = "getUser",
    blockHandler = "handleBlock",
    fallback = "handleFallback"
)
public User getUser(Long id) {
    return userService.getById(id);
}

public User handleBlock(Long id, BlockException ex) {
    return null; // 限流后的处理
}

public User handleFallback(Long id, Throwable ex) {
    return null; // 降级处理
}
```

### 与 @RateLimiter 的区别

| 特性 | Sentinel | @RateLimiter |
|------|----------|--------------|
| 实现 | 阿里 Sentinel | Redis |
| 侧重点 | 微服务自我保护、熔断 | 接口层限流 |
| 场景 | Feign 调用、核心接口 | 普通 API 限流 |
