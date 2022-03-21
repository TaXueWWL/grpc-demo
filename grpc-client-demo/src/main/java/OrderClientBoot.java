import agent.OrderClientAgent;
import agent.stream.DoubleStreamClient;
import com.google.common.collect.Lists;
import com.snowalker.grpc.sdk.*;
import io.grpc.ManagedChannelBuilder;
import lombok.SneakyThrows;
import regiatry.NacosRegistryConfig;

import java.util.concurrent.locks.LockSupport;
import java.util.logging.Logger;

/**
 * @author snowalker
 * @version 1.0
 * @date 2022/3/12 23:56
 * @desc 客户端启动类
 */
public class OrderClientBoot {

	private static final Logger logger = Logger.getLogger(OrderClientBoot.class.getName());
	private static final String NACOS_SERVER_ADDR = "nacos-server:8848";

	@SneakyThrows
	public static void main(String[] args) {
		// 启动阶段发现服务
		String providerServiceName = "grpc-server-demo";
		NacosRegistryConfig nacosRegistryConfig = new NacosRegistryConfig(NACOS_SERVER_ADDR, providerServiceName);

		// 端口及ip
		int port = nacosRegistryConfig.getProviderPort();
		String providerIp = nacosRegistryConfig.getProviderIp();
		OrderClientAgent orderClientAgent = new OrderClientAgent(providerIp, port);

		try {
			int userId = 10086;

			// 下单
			doPlaceOrder(orderClientAgent, userId);

			// 查订单
			doQueryOrder(orderClientAgent, userId);

			// 双向流通信
			DoubleStreamClient doubleStreamClient = new DoubleStreamClient(
					ManagedChannelBuilder.forAddress(providerIp, port)
					//使用非安全机制传输,默认情况下，通道是安全的（通过SSLTLS）
					.usePlaintext()
					.build());
			String chat = doubleStreamClient.chat("gRPC 双向流通信 ，就是牛！", userId, 10);
			System.out.println("[服务端响应：]" + chat);
		} finally {
//			orderClientAgent.shutdown();
		}

		LockSupport.park();
	}

	private static void doQueryOrder(OrderClientAgent orderClientAgent, int userId) {
		QueryUserOrderRequest queryUserOrderRequest = QueryUserOrderRequest.newBuilder()
				.setUserId(userId)
				.buildPartial();
		QueryUserOrderResponse queryUserOrderResponse = orderClientAgent.queryOrders(queryUserOrderRequest);
		logger.info("client queryOrders end. response:" + queryUserOrderResponse.toString());
	}

	private static void doPlaceOrder(OrderClientAgent orderClientAgent, int userId) {

		PlaceUserOrderParam orderParam0 = PlaceUserOrderParam.newBuilder()
				.setProductId(1)
				.setOrderAmount("15.00")
				.setOrderPrice("12.50")
				.build();

		PlaceUserOrderParam orderParam1 = PlaceUserOrderParam.newBuilder()
				.setProductId(2)
				.setOrderAmount("2.00")
				.setOrderPrice("10.00")
				.build();

		PlaceOrderRequest placeOrderRequest = PlaceOrderRequest.newBuilder()
				.setUserId(userId)
				.addAllPlaceUserOrderParam(Lists.newArrayList(orderParam0, orderParam1))
				.buildPartial();

		PlaceOrderRequestResponse placeOrderRequestResponse = orderClientAgent.placeOrder(placeOrderRequest);
		logger.info("client placeOrder end. response:" + placeOrderRequestResponse.toString() + ",resultCode:" + placeOrderRequestResponse.getResultCode());
	}
}
