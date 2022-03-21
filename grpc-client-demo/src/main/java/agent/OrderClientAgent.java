package agent;

import com.snowalker.grpc.sdk.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * @author snowalker
 * @version 1.0
 * @date 2022/3/12 23:57
 * @className
 * @desc
 */
public class OrderClientAgent {

	private static final Logger logger = Logger.getLogger(OrderClientAgent.class.getName());

	private final ManagedChannel channel;

	// 客户端请求服务端的桩
	private final OrderServiceGrpc.OrderServiceBlockingStub orderServiceBlockingStub;

	public OrderClientAgent(String host, int port) {
		this(ManagedChannelBuilder.forAddress(host, port)
				//使用非安全机制传输,默认情况下，通道是安全的（通过SSLTLS）
				.usePlaintext()
				.build());
	}

	OrderClientAgent(ManagedChannel channel) {
		this.channel = channel;
		orderServiceBlockingStub = OrderServiceGrpc.newBlockingStub(channel);
	}

	public void shutdown() throws InterruptedException {
		channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
	}

	/**
	 * 下单
	 * @param request
	 */
	public PlaceOrderRequestResponse placeOrder(PlaceOrderRequest request) {
		logger.info("client placeOrder start. request:" + request.toString());
		PlaceOrderRequestResponse placeOrderRequestResponse;
		try {
			placeOrderRequestResponse = orderServiceBlockingStub.placeOrder(request);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return placeOrderRequestResponse;
	}

	/**
	 * 订单查询
	 * @param request
	 * @return
	 */
	public QueryUserOrderResponse queryOrders(QueryUserOrderRequest request) {
		logger.info("client queryOrders start. request:" + request.toString());
		QueryUserOrderResponse queryUserOrderResponse;
		try {
			queryUserOrderResponse = orderServiceBlockingStub.queryUserOrders(request);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return queryUserOrderResponse;
	}
}
