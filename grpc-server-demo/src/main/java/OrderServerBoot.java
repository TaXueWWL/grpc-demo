import io.grpc.Server;
import io.grpc.ServerBuilder;
import lombok.SneakyThrows;
import registry.NacosRegistryConfig;
import service.OrderServiceImpl;
import service.stream.DoubleStreamServiceImpl;

import java.util.logging.Logger;

/**
 * @author snowalker
 * @version 1.0
 * @date 2022/3/12 23:46
 * @desc 服务端启动类
 */
public class OrderServerBoot {

	private static final Logger logger = Logger.getLogger(OrderServerBoot.class.getName());

	private Server server;

	private static final String NACOS_SERVER_ADDR = "nacos-server:8848";

	@SneakyThrows
	private void startServer() {
		int serverPort = 10881;
		server = ServerBuilder.forPort(serverPort)
				.addService(new OrderServiceImpl())
				.addService(new DoubleStreamServiceImpl())
				.build();
		server.start();

		logger.info("OrderServerBoot started, listening on:" + serverPort);

		// 优雅停机
		addGracefulShowdownHook();

		// 服务注册
		NacosRegistryConfig nacosRegistryConfig = new NacosRegistryConfig(NACOS_SERVER_ADDR, serverPort, 1.0, "grpc-server-demo");
		nacosRegistryConfig.register();
	}

	private void addGracefulShowdownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			// Use stderr here since the logger may have been reset by its JVM shutdown hook.
			System.err.println("*** shutting down gRPC server since JVM is shutting down");
			OrderServerBoot.this.stop();
			System.err.println("*** server shut down");
		}));
	}

	/**
	 * 服务关闭
	 */
	private void stop() {
		if (server != null) {
			server.shutdown();
		}
	}

	/**
	 * 由于 grpc 库使用守护线程，因此在主线程上等待终止。
	 */
	private void blockUntilShutdown() throws InterruptedException {
		if (server != null) {
			server.awaitTermination();
		}
	}

	@SneakyThrows
	public static void main(String[] args) {
		OrderServerBoot boot = new OrderServerBoot();
		// 启动服务
		boot.startServer();
		// 主线程等待终止
		boot.blockUntilShutdown();
	}
}
