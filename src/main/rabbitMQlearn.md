% rabbitMQ learn
% qijun
% 19/01/2018
###  mq 的一些概念
***
- mq:  mq 是一个message broker （消息中介）
- AMQP （Advanced Message Queue ） 一个消息队列标准协议
- RabbitMQ是一个由erlang开发的AMQP（Advanced Message Queue ）的开源实现
### rabbit mq 的一些概念
***
### rabbit  mq 的适用场景架构图
![image.png](http://upload-images.jianshu.io/upload_images/5641667-dd863d2f33956cbc.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
* Client A &Client B  为消息的producer 消息由payload 和 label 组成，label是exchange的名字或者说是一个tag，它描述了payload，而且RabbitMQ也是通过这个label来决定把这个Message发给哪个Consumer  
* client 1 & client2 & client3  消息的consumer, 消息的接受者 接收到的消息是去除label 的消息，紧包含消息的内容，消费者通过订阅队列获取消息。
* 中间是的 rabbit server 由 交换器,routingKey 和queue 组成，交换器和queue 通过routingKey 绑定，消息通过交换器和routingKey  路由到相应的queue
- Connection： 就是一个TCP的连接。Producer和Consumer都是通过TCP连接到RabbitMQ Server的。程序的起始处就是建立这个TCP连接。
- Channels： 虚拟连接。它建立在上述的TCP连接中。数据流动都是在Channel中进行的。也就是说，一般情况是程序起始建立TCP连接，第二步就是建立这个Channel。

### 四种交换器
***
由上面可知，消息通过交换器，通过对应的routekey 路由到queue, 交换器的类型一共有三种
1. direct   如果 routing key 匹配, 那么Message就会被传递到相应的queue中 
2. fanout  广播到所有绑定的queue(假设你有一个消息需要发送给a和b,如果现在还需要发送给c，使用fanout  交换器，只需要在c的代码中创建一个队列，然后绑定到fanout 交换器即可)
3. topic     对key进行模式匹配，比如ab.1,ab.2都可以传递到所有routingkey 为ab*的queue   
            基于topic类型交换器的routing key不是唯一的，而是一系列词，基于点区分。
            例如："stock.usd.nyse", "nyse.vmw", "quick.orange.rabbit"
            binding key也是.*表示只匹配一个关键字 #可以匹配0或者多个关键字
4. header  header交换器和 direct几乎一样，性能更差，基本不会用到


```
    rabbitTemplate.convertAndSend("fanout","","fanout 交换机");
    rabbitTemplate.convertAndSend("direct","temp.queue.1","direct 交换机");
    rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, "aaa.orange.bbb", "hello,world1 2", new CorrelationData(uuid1));

    rabbitAdmin.declareBinding(BindingBuilder.bind(tempQueue).to(fanoutExchange));
    rabbitAdmin.declareBinding(BindingBuilder.bind(tempQueue2).to(fanoutExchange));
    rabbitAdmin.declareBinding(BindingBuilder.bind(tempQueue1).to(directExchange).withQueueName());
    rabbitAdmin.declareBinding(BindingBuilder.bind(firstQueue).to(topicExchange).with(ROUTER_KEY_1));
```
### 匿名交换器（默认）
***
事实上，你在代码中不创建交换器也是可以通过rabbit mq 发送消息的，因为rabbit 提供了默认的交换器。

![image.png](http://upload-images.jianshu.io/upload_images/5641667-e159c2aa09611a7f.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

如图中空白字符串名字的交换器为默认的交换器，类型为direct
本质上所有的消息发送都要送往exchange（可以没有队列，但不能没有交换机，没有队列时消息直接被丢弃）。
RabbitMQ提供了一种直接向Queue发送消息的快捷方法：直接使用未命名的exchange，不用绑定routing_key，直接用它指定队列名。
```
  channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        // 发送消息
        String message = "Hello World!";
        // basicPublish(String exchange, String routingKey, BasicProperties props, byte[] body)
        // 参数1 exchange ：交换器
        // 参数2 routingKey ： 路由键
        // 参数3 props ： 消息的其他参数
        // 参数4 body ： 消息体
        channel.basicPublish("", QUEUE_NAME, null, message.getBytes());
```
### 消息的确认和拒绝
  使用ack确认Message的正确传递 
   默认情况下，如果Message 已经被某个Consumer正确的接收到了，那么该Message就会被从queue中移除。当然也可以让同一个Message发送到很多的Consumer
    如果一个queue没被任何的Consumer Subscribe（订阅），那么，如果这个queue有数据到达，那么这个数据会被cache，不会被丢弃。当有Consumer时，这个数据会被立即发送到这个Consumer，这个数据被Consumer正确收到时，这个数据就被从queue中删除。
那么什么是正确收到呢？通过ack。每个Message都要被acknowledged（确认，ack）。我们可以显示的在程序中去ack，也可以自动的ack。
如果在收到数据后处理数据时程序发生错误，无法正确处理数据，而是被reject。reject 参数设为true时RabbitMQ Server会把这个信息发送到下一个Consumer，设为false也可以从队列中把这条消息删除。
如果这个app有bug，忘记了ack，那么RabbitMQ Server不会再发送数据给它，因为Server认为这个Consumer处理能力有限。
 而且ack的机制可以起到限流的作用（Benefitto throttling）：在Consumer处理完成数据后发送ack，甚至在额外的延时后发送ack，将有效的balance Consumer的load。

### 在什么地方创建queue

Consumer和Procuder都可以通过 queue.declare 创建queue。对于某个Channel来说，Consumer不能declare一个queue，却订阅其他的queue。当然也可以创建私有的queue。这样只有app本身才可以使用这个queue。queue也可以自动删除，被标为auto-delete的queue在最后一个Consumer unsubscribe后就会被自动删除。那么如果是创建一个已经存在的queue呢？那么不会有任何的影响。需要注意的是没有任何的影响，也就是说第二次创建如果参数和第一次不一样，那么该操作虽然成功，但是queue的属性并不会被修改。

那么谁应该负责创建这个queue呢？是Consumer，还是Producer？

如果queue不存在，当然Consumer不会得到任何的Message。但是如果queue不存在，那么Producer Publish的Message会被丢弃。所以，还是为了数据不丢失，Consumer和Producer都try to create the queue！反正不管怎么样，这个接口都不会出问题。
queue对load balance的处理是完美的。对于多个Consumer来说，RabbitMQ 使用循环的方式（round-robin）的方式均衡的发送给不同的Consumer。

### VirtualHost

在RabbitMQ中可以虚拟消息服务器VirtualHost，每个VirtualHost相当月一个相对独立的RabbitMQ服务器，每个VirtualHost之间是相互隔离的。exchange、queue、message不能互通。 
在RabbitMQ中无法通过AMQP创建VirtualHost，可以通过以下命令来创建。
rabbitmqctl add_vhost [vhostname]

## windows下如何安装rabbit mq

1. rabbit mq 运行于erlang之上，需要先安装erlang http://www.erlang.org/downloads 下载，并使用管理员运行安装
2. 安装rabbit mq https://www.rabbitmq.com/download.html
3. 新增环境变量 ERLANG_HOME=EC:\Program Files\erl9.2 
                           RABBITMQ_SERVER = C:\Program Files\RabbitMQ Server\rabbitmq_server-3.7.2
    配置环境变量
    Path=%ERLANG_HOME%\bin;%RABBITMQ_SERVER%\sbin
4. 替换 erlang cookie
    拷贝C:\WINDOWS 下的.erlang.cookie (还有可能在C:\Windows\System32\config\systemprofile)文件替换 C:\Users\%USERNAME%.erlang.cookie 或者 C:\Documents and 
   Settings\%USERNAME%.erlang.cookie
5. 通过startMenu 启动erlang 服务 和停止 rabbit mq 可以以服务的方式和按进程的方式启动，建议使用服务方式启动,然后在rabbit mq的命令行（RabbitMQ Command Prompt 开始菜单中)  执行 rabbitmq-plugins enable rabbitmq_management
最后就可以通过 http://localhost:15672/   账号guest 密码guest  访问rabbit mq的控制台  /是默认的VirtualHost

### 常用命令

停止 broker abbitmqctl stop
查询 broker  状态 rabbitmqctl status
更多的命令请查阅 https://www.rabbitmq.com/man/rabbitmqctl.8.html


## 实战
下面会通过两个例子，演示如何使用rabbitmq,第一个原生的java api 使用direct 交换器演示 routing，第二个例子使用topic 交换器。spring mvc，spring boot 中的注解和接口本质上是对原生接口的包装，spring 会隐藏一些操作，对理解rabbit mq的工作流程会造成阻碍，先使用原生api做演示一般的工作流程，而后结合springboot 演示在项目中如何使用rabbit mq。
### rabbitmq 消费者和生产者两端的在处理消息时经历的步骤
 1. 创建连接工厂ConnectionFactory
 2. 通过连接获取通信通道Channel
 3. 声明交换机Exchange(可选)
 4. 申明队列（可选）
 5. 绑定交换机和队列（可选）
 之后生产者通过channel发送消息，消费者获取并处理消息
### rabbitmq comsumer 消息获取方式
rabbitMQ中consumer通过建立到queue的连接，创建channel对象，通过channel通道获取message,
Consumer可以声明式的以API轮询poll的方式主动从queue的获取消息，也可以通过订阅的方式被动的从Queue中消费消息。
### 使用原生rabbitmq api 的例子
代码发送三种类型的日志到交换器，交换器通过routingkey 分发到不同的queue

#### maven 依赖
```
   <dependency>
            <groupId>com.rabbitmq</groupId>
            <artifactId>amqp-client</artifactId>
            <version>3.6.3</version>
        </dependency>
```
## 消息发送
```
public class EmitLogDirect {
    private static final String EXCHANGE_NAME = "direct_logs";
    private static final String[] LOG_LEVEL_ARR = {"debug", "info", "error"};

    public static void main(String[] args) throws IOException, TimeoutException {
        // 创建连接
        ConnectionFactory factory = new ConnectionFactory();
        // 设置 RabbitMQ 的主机名
        factory.setHost("localhost");
        // 创建一个连接
        Connection connection = factory.newConnection();
        // 创建一个通道
        Channel channel = connection.createChannel();
        // 指定一个交换器
        channel.exchangeDeclare(EXCHANGE_NAME, "direct");
        // 发送消息
        for (int i = 0; i < 10; i++)  {
            int rand = new Random().nextInt(3);
            String severity  = LOG_LEVEL_ARR[rand];
            String message = "Qijun-MSG log : [" +severity+ "]" + UUID.randomUUID().toString();
            // 发布消息至交换器
            channel.basicPublish(EXCHANGE_NAME, severity, null, message.getBytes());
            System.out.println(" [x] Sent '" + message + "'");
        }
        // 关闭频道和连接
        channel.close();
        connection.close();
    }
}
```
## 消息接收
```
public class ReceiveLogsDirect {
    private static final String EXCHANGE_NAME = "direct_logs";
    private static final String[] LOG_LEVEL_ARR = {"debug", "info", "error"};

    public static void main(String[] args) throws IOException, TimeoutException {
        // 创建连接
        ConnectionFactory factory = new ConnectionFactory();
        // 设置 RabbitMQ 的主机名
        factory.setHost("localhost");
        // 创建一个连接
        Connection connection = factory.newConnection();
        // 创建一个通道
        Channel channel = connection.createChannel();
        // 指定一个交换器
        channel.exchangeDeclare(EXCHANGE_NAME, "direct");
        // 设置日志级别
        int rand = new Random().nextInt(3);

        // 创建三个非持久的、唯一的、自动删除的队列，分别接收不同的日志信息
        String debugQueueName = channel.queueDeclare().getQueue();
        String InfoQueueName = channel.queueDeclare().getQueue();
        String ErrorQueueName = channel.queueDeclare().getQueue();
        // 绑定交换器和队列
        // queueBind(String queue, String exchange, String routingKey)
        // 参数1 queue ：队列名
        // 参数2 exchange ：交换器名
        // 参数3 routingKey ：路由键名
        channel.queueBind(debugQueueName, EXCHANGE_NAME, LOG_LEVEL_ARR[0]);
        channel.queueBind(InfoQueueName, EXCHANGE_NAME, LOG_LEVEL_ARR[1]);
        channel.queueBind(ErrorQueueName, EXCHANGE_NAME, LOG_LEVEL_ARR[2]);

        // rabbit mq 消息的推送支持poll 也支持订阅，先创建一个poll 方式的comsumer
        QueueingConsumer pollConsumer = new QueueingConsumer(channel);
        channel.basicConsume(ErrorQueueName, true, pollConsumer);

        // 创建订阅类型的消费者
        final Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope,
                                       AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                System.out.println("Received '" + message + "' from "+envelope.getRoutingKey()+ " by subscribe" );
            }
        };
        channel.basicConsume(debugQueueName, true, consumer);
        channel.basicConsume(InfoQueueName, true, consumer);

        // 通过 循环poll 获取队列中的所有消息  
        while (true) {
            QueueingConsumer.Delivery delivery = null;
            try {
                delivery = pollConsumer.nextDelivery();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            String message = new String(delivery.getBody());
            String routingKey = delivery.getEnvelope().getRoutingKey();


            System.out.println("Received '" + message + "' from "+routingKey +" by poll");
        }

    }
}
```
[源码](https://github.com/qijun4tian/java_rabbit_mq)

### springboot 中使用rabbit mq 的例子
### maven 依赖
```
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-amqp</artifactId>
</dependency>
```

### ConnectionFactory配置
```
// 项目中可通过配置文件读取来获取 connect 参数
 @Bean
    public CachingConnectionFactory rabbitConnectionFactory() {
        CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory();
        cachingConnectionFactory.setHost("localhost");
        cachingConnectionFactory.setPort(5672);
        cachingConnectionFactory.setUsername("guest");
        cachingConnectionFactory.setPassword("guest");
        cachingConnectionFactory.setVirtualHost("/");
        return cachingConnectionFactory;
    }
```
CachingConnectionFactory  内部通过com.rabbitmq.client.ConnectionFactory 去设置 connect的参数
```
public abstract class AbstractConnectionFactory implements ConnectionFactory, DisposableBean, BeanNameAware {
    private static final String BAD_URI = "setUri() was passed an invalid URI; it is ignored";
    protected final Log logger = LogFactory.getLog(this.getClass());
    private final com.rabbitmq.client.ConnectionFactory rabbitConnectionFactory;
```
### 通过 RabbitAdmin 配置队列，交换机和binding
``` 
   public static final String  ROUTER_KEY_1 = "*.orange.*";
 @Bean
    public RabbitAdmin rabbitAdmin() {
        RabbitAdmin rabbitAdmin = new RabbitAdmin(rabbitConnectionFactory());
       //申明一个 一个topic类型的交换机，routingkey 使用通配符
        TopicExchange topicExchange =(TopicExchange)ExchangeBuilder.topicExchange(QUEUE_EXCHANGE_NAME).durable(true).build();
        rabbitAdmin.declareExchange(topicExchange);
        Queue firstQueue = new Queue(QUEUE_NAME);
        rabbitAdmin.declareQueue(firstQueue);
        rabbitAdmin.declareBinding(BindingBuilder.bind(firstQueue).to(topicExchange).with(ROUTER_KEY_1));
        return rabbitAdmin;
    }
```
### 消息消费的两种方法（推荐使用第二种，更灵活）
1. 通过SimpleMessageListenerContainer 绑定特定的messageListener
```
@Bean
    MessageListenerAdapter listenerAdapter(Receiver receiver) {
        return new MessageListenerAdapter(receiver, "receive2");
    }
```
```
 @Bean
    SimpleMessageListenerContainer container(MessageListenerAdapter messageListenerAdapter) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(rabbitConnectionFactory());
        container.setQueueNames(QUEUE_NAME);
        container.setMessageListener(messageListenerAdapter);
        return container;
    }
```

```
@Service
public class Receiver {

    public void receiveMessage(String message) {
        System.out.println("Received<" + message + ">");
    }

    public void receive2(String in) throws InterruptedException {
        System.out.println("in message"+in);
    }
}
```

2. 使用 SimpleRabbitListenerContainerFactory 和 @RabbitListener 方式接收mq 的消息
```
  @Bean
    public SimpleRabbitListenerContainerFactory myContainerFactory(
            SimpleRabbitListenerContainerFactoryConfigurer configurer,
            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        //设置了每个消费者再不回ack的情况下最大可接收消息的条数
        factory.setPrefetchCount(100);
        configurer.configure(factory, connectionFactory);
        return factory;
    }
```
```
/**
 * @author 祁军
 * 使用 SimpleRabbitListenerContainerFactory 和 @RabbitListener 方式接收mq 的消息
 */
@Service
public class Receiver1 {
    @RabbitListener(queues = "${rabbitConfiguration.queue}", containerFactory = "myContainerFactory")
    public void processMessage(String msg){
        System.out.println("Receiver1 got message" + msg);
    }
}

```
### sender
```
@Service
public class Sender {
    private RabbitTemplate rabbitTemplate;

    @Autowired
    public Sender(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void send() {
       // 发送两次routing key不同 由于 是topic exchange routing key 为通配符可达到同一队列
        System.out.println("sender is sending message");
        rabbitTemplate.convertAndSend(RabbitMQConfig.QUEUE_EXCHANGE_NAME,"aaa.orange.bbb", "hello,world1");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        rabbitTemplate.convertAndSend(RabbitMQConfig.QUEUE_EXCHANGE_NAME,"aaa.orange.ccc", "hello,world2");
    }
}

```


### 测试
```
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Application.class)
public class RabbitMQTest {

    @Autowired
    private Sender sender;

    @Test
    public void send() throws Exception {
        sender.send();
    }

}
```
[源码](https://github.com/qijun4tian/spring_boot_learn)

### rabbitmq 消息的可靠性
1. 到达exchange的可靠性 
   1. 发送端的comfirm 机制，通过注册回调，我们可以知道消息是否已经发送到exchange 
   2. 事务机制，通过事务机制去确保消息已经到达exchange
2. 确认是否到达queue
   设置basic.publish的mandatory 是true，可以在消息没有到达指定队列时产生一个回调(在正确到达时不会回调)
比如发送告警，或者重发来应对。
2. 消息的持久化，通过交换机，队列和消息的持久化来实现
4. rabbitmq 从queue 发消息给消费者，如果消费者选择no ack（或者说自动ack） 则queue每发一条消息，rabbitmq 就会把消息删除，如果cosumer 由于某种问题消费消息出错，rabbitmq也会把消息删除。
   我们需要在comsumer 关闭自动ack，使用basic ack 手工应答保证消息被正确消费，如果消费失败，basic nack 可以删除队列消息或者重新入原队列，可能导致死循环
   如果不希望把有问题的消息删除或者重新入原来的队列，可以指定一个死信队列，错误的消息重新入死信对列，然后再次被消费。
   我们可以把消息放入死信队列，然后通过死信队列去消费错误的消息。

#### 如何配置事务
```
   配置通道为事务模式
 //rabbitTemplate.setChannelTransacted(true);
 try {
            rabbitTemplate.send(RabbitMQConfig.EXCHANGE_NAME, "1111111", new Message(messageBody, MessagePropertiesBuilder.newInstance().setCorrelationIdString(uuid3).
                            setMessageId(uuid3).setContentType("text/x-json").build()),
                    new CorrelationData(uuid3)
            );
        }catch (Exception e){
            log.info("commit error");
        }
```

#### comfirm 模式
rabbitmq提供了确认ack机制，可以用来确认消息是否有返回。

```
// 配置
 cachingConnectionFactory.setPublisherReturns(true);
 rabbitTemplate.setMandatory(true);

/**confirmcallback用来确认消息是否到达broker*/     
rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
    if (!ack) {
        //log error
    } else {
        //maybe delete msg in db
    }
});
```
**千万不要在confirm模式下使用不存在的excharge名发消息会造成严重的问题**

#### mandatory标志位的设置和回调
```
rabbitTemplate.setMandatory(true);
 /**若消息不能正确的达到指定的队列会调用 */
rabbitTemplate.setReturnCallback((message, replyCode, replyText, tmpExchange, tmpRoutingKey) -> {
    log.info("send message failed: " + replyCode + " " + replyText);
    // resend message
   
});
```
#### 消息的持久化
```
// 交换机的持久化
// 参数1 name ：交互器名
// 参数2 durable ：是否持久化
// 参数3 autoDelete ：当所有消费客户端连接断开后，是否自动删除队列
new TopicExchange(name, durable, autoDelete)

// 队列是持久化
// 参数1 name ：队列名
// 参数2 durable ：是否持久化
// 参数3 exclusive ：仅创建者可以使用的私有队列，断开后自动删除
// 参数4 autoDelete : 当所有消费客户端连接断开后，是否自动删除队列
new Queue(name, durable, exclusive, autoDelete);

springAMQP  的消息持久化是默认的
```
#### 消费者端的手工确认
如果一直不回ack，mq会block 这个消费者
```
      @Bean
    SimpleMessageListenerContainer container() {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(rabbitConnectionFactory());
        container.setQueueNames(QUEUE_NAME);
        //设定单次可分发给消费者的消息个数
        container.setPrefetchCount(1);
        //设定这个container的最大消费者个数
        container.setMaxConcurrentConsumers(1);
        container.setConcurrentConsumers(1);
        container.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        container.setMessageListener(new ChannelAwareMessageListener() {

            @Override
            public void onMessage(Message message, Channel channel) throws Exception {
                byte[] body = message.getBody();
                try {
                    log.info("receive msg: " + new String(body));
                    //do something
                } catch (Exception e) {
                } finally {
//                    channel.basicAck(message.getMessageProperties().getDeliveryTag(), false); //确认消息成功消费
                }

            }

        });
        return container;
    }

```
##### springAMQP 提供的确认方式
很明显上述代码提供的手工确认方式(使用ChannelAwareMessageListener)很不优雅，你需要创建多个bean 然后绑定queue。
spring AMQP 当factory.setDefaultRequeueRejected(true);  (默认情况下)，如果消息被正常消费，container 会ack，然后队列删除消息，如果消费者抛出异常，container会reject这个消息，然后这个消息会requeue,在某些情况下，如果业务一直处在这个异常情况下，requeue的消息会再次回到消费者，然后死循环，这种情况不是我们想要的，spring 提供了其他的方式 如果listener抛出AmqpRejectAndDontRequeueException，则这个消息会被抛弃，或者进入死信队列，Listener抛出AmqpRejectAndDontRequeueException还可以通过配置factory 的ErrorHandler 把你抛出的特定异常 转换为AmqpRejectAndDontRequeueException,如下式例，如果你的listener 抛出了XMLException  则这个消息会被discard（在没有配置死信队列的情况下）。
```
factory.setErrorHandler(new ConditionalRejectingErrorHandler(
                t -> t instanceof ListenerExecutionFailedException && t.getCause() instanceof XMLException));
```
factory.setDefaultRequeueRejected(false); 则只要listener 抛出异常，message就会被discard或者转入死信队列，如果需要针对不同的异常（比如可短时间内恢复的异常，需要重入原队列，不可恢复的异常discard 或者入死信队列）建议设置成true，然后配置ErrorHandler 如上

##### springAMQP 如何配置死信队列
```
@Bean
TopicExchange exchange()
{
    return new TopicExchange(DEFAULT_EXCHANGE);
}

@Bean
Queue deadLetterQueue()
{
    return new Queue(DEAD_LETTER_QUEUE,true);
}

@Bean
Queue queue()
{
    // 通过args参数为当前队列绑定一个死信队列
    Map<String, Object> args = new HashMap<String, Object>();
    args.put("x-dead-letter-exchange", DEFAULT_EXCHANGE);
    args.put("x-dead-letter-routing-key", DEAD_LETTER_QUEUE);
    return new Queue(WORKORDER_QUEUE,true,false,false,args);
}
@Bean
Binding binding(Queue queue, TopicExchange exchange)
{
    return BindingBuilder.bind(queue).to(exchange).with(WORKORDER_QUEUE);
}

@Bean
Binding bindingDeadLetter(Queue deadLetterQueue, TopicExchange exchange)
{
    return BindingBuilder.bind(deadLetterQueue).to(exchange).with(DEAD_LETTER_QUEUE);
}



```

消费者抛出AmqpRejectAndDontRequeueException 异常时则会进入死信队列
```

  @RabbitListener(queues = RabbitConfig.WORKORDER_QUEUE)
    public void processMessage(String msg) throws Exception
    {
        
            throw new AmqpRejectAndDontRequeueException("to dead-letter");
        
    }

```
死信队列的消费者
```
@Service
public class ErrorHandler {
    @RabbitListener(queues = "dead_queue", containerFactory = "myContainerFactory")
    public void handleError(Object message){
        System.out.println("XXXXXXX"+message);
    }
}

```


## rabbit mq 的其他应用场景
### working queue
当有Consumer需要大量的运算时，RabbitMQ Server需要一定的分发机制来balance每个Consumer的load。试想一下，对于web application来说，在一个很多的HTTP request里是没有时间来处理复杂的运算的，只能通过后台的一些工作线程来完成。应用场景就是RabbitMQ Server会将queue的Message分发给不同的Consumer以处理计算密集型的任务。

![image.png](http://upload-images.jianshu.io/upload_images/5641667-093c8b127c890010.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
### RPC
MQ本身是基于异步的消息处理，前面的示例中所有的生产者（P）将消息发送到RabbitMQ后不会知道消费者（C）处理成功或者失败（甚至连有没有消费者来处理这条消息都不知道）。
但实际的应用场景中，我们很可能需要一些同步处理，需要同步等待服务端将我的消息处理完成后再进行下一步处理。这相当于RPC（Remote Procedure Call，远程过程调用）。在RabbitMQ中也支持RPC。

![image.png](http://upload-images.jianshu.io/upload_images/5641667-62cebdedc05c491e.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

RabbitMQ中实现RPC的机制是：
1. 客户端发送请求（消息）时，在消息的属性（MessageProperties，在AMQP协议中定义了14中properties，这些属性会随着消息一起发送）中设置两个值replyTo（一个Queue名称，用于告诉服务器处理完成后将通知我的消息发送到这个Queue中）和correlationId（此次请求的标识号，服务器处理完成后需要将此属性返还，客户端将根据这个id了解哪条请求被成功执行了或执行失败）
2. 服务器端收到消息并处理
3. 服务器端处理完消息后，将生成一条应答消息到replyTo指定的Queue，同时带上correlationId属性
4. 客户端之前已订阅replyTo指定的Queue，从中收到服务器的应答消息后，根据其中的correlationId属性分析哪条请求被执行了，根据执行结果进行后续业务处理


### 什么时候使用MQ
***
#### 什么时候不使用MQ？
上游实时关注执行结果

#### 什么时候使用MQ？
1. 数据驱动的任务依赖
2. 上游不关心多下游执行结果
3. 异步返回执行时间长

更多参阅 https://mp.weixin.qq.com/s/Brd-j3IcljcY7BV01r712Q

### 其他高级主题
rabbit mq集群
### 参考
https://www.rabbitmq.com/getstarted.html
https://github.com/rabbitmq/rabbitmq-tutorials/tree/master/spring-amqp
https://docs.spring.io/spring-amqp/reference/html/
http://blog.720ui.com/2017/springboot_06_mq_rabbitmq/
http://www.cnblogs.com/xingzc/p/5945030.html
https://www.cnblogs.com/diegodu/p/4971586.html
http://blog.csdn.net/column/details/rabbitmq.html
http://blog.csdn.net/u013256816/article/category/6532725/1