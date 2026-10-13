package syy.portPilotage.constant;

public class RabbitMQConstant {
    
    public static final String EXCHANGE_PILOTAGE = "exchange.pilotage";///// 交换机名称
    public static final String EXCHANGE_BILLING = "exchange.billing";/// 交换机名称
    public static final String EXCHANGE_NOTIFICATION = "exchange.notification";/// 交换机名称
    
    public static final String QUEUE_SMS = "queue.sms";/// 队列名称
    public static final String QUEUE_EMAIL = "queue.email";/// 队列名称
    public static final String QUEUE_PLAN_NOTIFY = "queue.plan.notify";/// 队列名称
    public static final String QUEUE_BILLING_CALCULATE = "queue.billing.calculate";/// 队列名称
    public static final String QUEUE_CERTIFICATE_REMINDER = "queue.certificate.reminder";/// 队列名称
    
    public static final String ROUTING_KEY_SMS = "routing.sms";/// 路由名称
    public static final String ROUTING_KEY_EMAIL = "routing.email";/// 路由名称
    public static final String ROUTING_KEY_PLAN_NOTIFY = "routing.plan.notify";/// 路由名称
    public static final String ROUTING_KEY_BILLING = "routing.billing";/// 队列名称
    public static final String ROUTING_KEY_CERTIFICATE = "routing.certificate";/// 队列名称
}