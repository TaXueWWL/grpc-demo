package service.stream;

import com.snowalker.grpc.sdk.stream.ChatRequest;
import com.snowalker.grpc.sdk.stream.ChatResponse;
import com.snowalker.grpc.sdk.stream.DoubleStreamServiceGrpc;
import io.grpc.stub.StreamObserver;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

/**
 * @author snowalker
 * @version 1.0
 * @date 2022/3/16 23:45
 * @className
 * @desc 双向流demo
 */
public class DoubleStreamServiceImpl extends DoubleStreamServiceGrpc.DoubleStreamServiceImplBase {

	private static final Logger logger = Logger.getLogger(DoubleStreamServiceImpl.class.getName());

	/**
	 * @param responseObserver
	 * @return
	 */
	@Override
	public StreamObserver<ChatRequest> chat(StreamObserver<ChatResponse> responseObserver) {
		return new StreamObserver<ChatRequest>() {
			@Override
			public void onNext(ChatRequest chatRequest) {

				int userId = chatRequest.getUserId();
				String msg = chatRequest.getMsg();

				logger.info("[DoubleStreamServiceImpl] 服务端处理开始....");
				logger.info("[DoubleStreamServiceImpl] 客户端说: [" + msg + "]");

				responseObserver.onNext(ChatResponse.newBuilder()
						.setUserId(chatRequest.getUserId())
						.setMsg("这是一条来自[服务端]的消息: 你好，收到了-" + userId + " 的消息. " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ssSSS").format(new Date()) + "\n")
						.build());
			}

			@Override
			public void onError(Throwable throwable) {
				logger.warning("[DoubleStreamServiceImpl] gRPC dealing error");
			}

			@Override
			public void onCompleted() {
				responseObserver.onCompleted();
			}
		};
	}
}
