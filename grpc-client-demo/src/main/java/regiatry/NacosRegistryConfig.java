package regiatry;

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
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;

/**
 * @author snowalker
 * @version 1.0
 * @date 2022/3/13 21:13
 * @className
 * @desc  实战中，应当维护一个映射，服务与对应的实例配置的关系，这里需要注意
 */
public class NacosRegistryConfig {

	private static final Logger logger = Logger.getLogger(NacosRegistryConfig.class.getName());

	/**Nacos服务单地址*/
	private String serverAddr;
	/**服务名*/
	private String providerServiceName;
	/**提供者ip*/
	private String providerIp;
	/**提供者端口*/
	private int providerPort;

	public NacosRegistryConfig(String serverAddr, String providerServiceName) {
		this.serverAddr = serverAddr;
		this.providerServiceName = providerServiceName;
		findServerList();
	}

	private void findServerList() {
		try {
			// 连接Nacos-server
			NamingService namingService = NamingFactory.createNamingService(serverAddr);
			// 获取服务提供者实例信息
			List<Instance> instances = namingService.getAllInstances(providerServiceName);

			// 随机策略  TODO 改为基于取模粘滞请求，基于userId取模
			int serverSize = instances.size();
			Random random = new Random();
			int index = random.nextInt(serverSize);
			System.out.println("serverSize:" + serverSize + "选择的机器：" + index);
			Instance instance = instances.get(index);
			// 获取ip 端口
			this.providerIp = instance.getIp();
			this.providerPort = instance.getPort();

			// TODO还需要考虑对服务列表变更的处理
			namingService.subscribe(providerServiceName, new EventListener() {
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

	public String getProviderIp() {
		return providerIp;
	}

	public int getProviderPort() {
		return providerPort;
	}
}
