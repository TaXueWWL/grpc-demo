package agent.stream;

import com.snowalker.grpc.sdk.stream.ChatRequest;
import com.snowalker.grpc.sdk.stream.ChatResponse;
import com.snowalker.grpc.sdk.stream.DoubleStreamServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import java.util.logging.Logger;

/**
 * @author snowalker
 * @version 1.0
 * @date 2022/3/16 23:49
 * @className
 * @desc
 */
public class DoubleStreamClient {

	private static final Logger logger = Logger.getLogger(DoubleStreamClient.class.getName());

	private final DoubleStreamServiceGrpc.DoubleStreamServiceStub doubleStreamServiceStub;

	public DoubleStreamClient(ManagedChannel channel) {
		doubleStreamServiceStub = DoubleStreamServiceGrpc.newStub(channel);
	}

	public String chat(String msg, int user, int count) {
		ExtendResponseObserver<ChatResponse> chatResponseStreamObserver = new ExtendResponseObserver<ChatResponse>() {

			@Override
			public String getExtra() {
				return stringBuilder.toString();
			}

			// 用stringBuilder保存所有来自服务端的响应
			private StringBuilder stringBuilder = new StringBuilder();

			@Override
			public void onNext(ChatResponse chatResponse) {
				logger.info("[DoubleStreamClient] onNext.....");
				// 放入匿名类的成员变量中
				System.out.println(chatResponse.getMsg());
				stringBuilder.append(String.format("服务端响应:%s<br>, 用户:%d" , chatResponse.getMsg(), chatResponse.getUserId()));
			}

			@Override
			public void onError(Throwable throwable) {
				logger.warning("[DoubleStreamClient] gRPC request error");
				stringBuilder.append("[DoubleStreamClient]chat gRPC error, " + throwable.getMessage());
			}

			@Override
			public void onCompleted() {
				logger.info("[DoubleStreamClient] onCompleted");
			}
		};

		// 重点！！！！ RPC调用发起
		StreamObserver<ChatRequest> chatRequestStreamObserver = doubleStreamServiceStub.chat(chatResponseStreamObserver);

		for(int i = 0; i < count; i++) {
			// 每次执行onNext都会发送一笔数据到服务端，
			// 服务端的onNext方法都会被执行一次
			ChatRequest chatRequest = ChatRequest.newBuilder()
					.setUserId(user)
					.setMsg("这是一条来自客户端的消息: 你好，" + user + new SimpleDateFormat("yyyy-MM-dd HH:mm:ssSSS").format(new Date()))
					.buildPartial();
			LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(100));
			chatRequestStreamObserver.onNext(chatRequest);
		}

		// 客户端告诉服务端：数据已经发完了
		chatRequestStreamObserver.onCompleted();

		logger.info("service finish");

		return chatResponseStreamObserver.getExtra();
	}
}
