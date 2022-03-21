package registry;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.listener.Event;
import com.alibaba.nacos.api.naming.listener.EventListener;
import com.alibaba.nacos.api.naming.listener.NamingEvent;
import com.alibaba.nacos.api.naming.pojo.Cluster;
import com.alibaba.nacos.api.naming.pojo.Instance;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * @author snowalker
 * @version 1.0
 * @date 2022/3/13 21:13
 * @className
 * @desc
 */
public class NacosRegistryConfig {

	private static final Logger logger = Logger.getLogger(NacosRegistryConfig.class.getName());

	/**Nacos服务单地址*/
	private String serverAddr;
	/**注册端口，一般就是服务暴露端口*/
	private int port;
	/**权重*/
	private double weight = 1.0;
	/**服务名*/
	private String serviceName;
	/**当前服务ip*/
	private String ip;

	public NacosRegistryConfig(String serverAddr, int port, double weight, String serviceName) {
		this.serverAddr = serverAddr;
		this.port = port;
		this.weight = weight;
		this.serviceName = serviceName;

		try {
			InetAddress inetAddress = InetAddress.getLocalHost();
			this.ip = inetAddress.getHostAddress();
		} catch (UnknownHostException e) {
			throw new RuntimeException("NacosRegistryConfig.getLocalHost failed.", e);
		}

		logger.info("NacosRegistryConfig construct done. serverAddr=[" + serverAddr +
				"],serviceName=" + serviceName +
				"],ip=[" + ip +
				"],port=[" + port +
				"],weight=[" + weight + "]");
	}

	public void register() {
		try {

			NamingService namingService = NamingFactory.createNamingService(serverAddr);

			Instance instance = new Instance();
			instance.setIp(ip);
			instance.setPort(port);
			instance.setHealthy(false);
			instance.setWeight(weight);
			instance.setInstanceId(serviceName + "-instance");

			// 自定义服务元数据
			Map<String, String> instanceMeta = new HashMap<>();
			instanceMeta.put("language", "java");
			instanceMeta.put("rpc-framework", "gRPC");
			instance.setMetadata(instanceMeta);

			Cluster cluster = new Cluster();
			cluster.setName("DEFAULT-CLUSTER");
//			Http healthChecker = new Http();
//			healthChecker.setExpectedResponseCode(400);
//			healthChecker.setCurlHost("USer-Agent|Nacos");
//			healthChecker.setCurlPath("/xxx.html");
//			cluster.setHealthChecker(healthChecker);
			Map<String, String> clusterMeta = new HashMap<>();
			clusterMeta.put("name", cluster.getName());
			cluster.setMetadata(clusterMeta);

			instance.setClusterName("DEFAULT-CLUSTER");
			// 注册服务实例
			namingService.registerInstance(serviceName, instance);


//			NamingService naming = NamingFactory.createNamingService("nacos-server:8848");
//			naming.registerInstance("admin-service-222", "11.11.11.11", 8888, "TEST1");
			namingService.subscribe(serviceName, new EventListener() {
				@Override
				public void onEvent(Event event) {
					System.out.println(((NamingEvent)event).getServiceName());
					System.out.println(((NamingEvent)event).getInstances());
				}
			});
		} catch (NacosException e) {
			throw new RuntimeException("Register Services To Nacos Failed.", e);
		}
	}
}
