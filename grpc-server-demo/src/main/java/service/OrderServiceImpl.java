package service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.snowalker.grpc.sdk.*;
import io.grpc.stub.StreamObserver;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author snowalker
 * @version 1.0
 * @date 2022/3/12 23:47
 * @className
 * @desc
 */
public class OrderServiceImpl extends OrderServiceGrpc.OrderServiceImplBase {

	private static final Logger logger = Logger.getLogger(OrderServiceImpl.class.getName());

	private static final Map<Integer, LinkedList<UserOrder>> USER_MEMORY_ORDER_BOOK = Maps.newConcurrentMap();

	/**
	 * <pre>
	 * 查询用户订单列表
	 * </pre>
	 *
	 * @param request
	 * @param responseObserver
	 */
	@Override
	public void queryUserOrders(QueryUserOrderRequest request, StreamObserver<QueryUserOrderResponse> responseObserver) {
		int userId = request.getUserId();
		// 查询订单
		List<UserOrder> orders = USER_MEMORY_ORDER_BOOK.getOrDefault(userId, Lists.newLinkedList());

		// 计算总价
		String totalPrice = calculateTotalPrice(orders);

		// 组装response
		QueryUserOrderResponse queryUserOrderResponse = QueryUserOrderResponse.newBuilder()
				.setUserId(userId)
				.addAllUserOrder(orders)
				.setTotalPrice(totalPrice)
				.build();

		logger.info("[Server] queryUserOrders, request:" + request.toString() + "\n" + "response:" + queryUserOrderResponse.toString());

		// 响应
		responseObserver.onNext(queryUserOrderResponse);
		responseObserver.onCompleted();
	}

	private String calculateTotalPrice(List<UserOrder> orders) {
		Optional<BigDecimal> count = orders.stream()
				.map(order -> new BigDecimal(order.getOrderAmount()).multiply(new BigDecimal(order.getOrderPrice())))
				.reduce(BigDecimal::add);
		return count.orElseGet(() -> BigDecimal.ZERO).toPlainString();
	}

	/**
	 * <pre>
	 * 下单
	 * </pre>
	 *
	 * @param request
	 * @param responseObserver
	 */
	@Override
	public void placeOrder(PlaceOrderRequest request, StreamObserver<PlaceOrderRequestResponse> responseObserver) {

		ThreadLocalRandom orderIdGenerator = ThreadLocalRandom.current();

		PlaceOrderRequestResponse.Builder placeOrderRequestResponse = PlaceOrderRequestResponse.newBuilder();

		int userId = request.getUserId();

		if (request.getPlaceUserOrderParamCount() <= 0) {
			placeOrderRequestResponse.setUserId(userId).setResultCode(ResultCode.FAILURE).build();
			responseObserver.onNext(placeOrderRequestResponse.build());
			responseObserver.onCompleted();
		}

		// 获取用户订单列表
		LinkedList<UserOrder> userOrderList = USER_MEMORY_ORDER_BOOK.getOrDefault(userId, Lists.newLinkedList());

		if (userOrderList.size() == 0) {
			USER_MEMORY_ORDER_BOOK.put(userId, Lists.newLinkedList());
		}

		int orderId = getOrderId(orderIdGenerator);

		// 本次订单
		List<UserOrder> userOrders = request.getPlaceUserOrderParamList().stream().map(
				param -> UserOrder.newBuilder()
						.setOrderId(orderId)
						.setOrderAmount(param.getOrderAmount())
						.setOrderPrice(param.getOrderPrice())
						.setProductId(param.getProductId())
						.build()).collect(Collectors.toList());

		// 追加订单列表
		userOrderList.addAll(userOrders);

		USER_MEMORY_ORDER_BOOK.put(userId, userOrderList);

		// 响应
		responseObserver.onNext(placeOrderRequestResponse.setUserId(userId).setResultCode(ResultCode.SUCCESS).build());
		responseObserver.onCompleted();
	}

	private int getOrderId(ThreadLocalRandom orderIdGenerator) {
		int orderId = orderIdGenerator.nextInt();
		if (orderId < 0) {
			orderId *= -1;
		}
		return orderId;
	}
}
