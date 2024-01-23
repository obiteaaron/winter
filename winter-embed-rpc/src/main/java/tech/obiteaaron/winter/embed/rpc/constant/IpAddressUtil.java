package tech.obiteaaron.winter.embed.rpc.constant;

import tech.obiteaaron.winter.common.tools.cache.LocalCacheFactory;

import java.net.*;
import java.util.Enumeration;
import java.util.List;

public class IpAddressUtil {

    public static String getLocalIpv4ByNetCard() {
        return LocalCacheFactory.get("IpAddressUtil", "IpAddressUtil", key -> IpAddressUtil.getLocalIpv4ByNetCard0());
    }

    public static String getLocalIpv4ByNetCard0() {
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                    continue;
                }
                List<InterfaceAddress> interfaceAddresses = networkInterface.getInterfaceAddresses();
                for (InterfaceAddress interfaceAddress : interfaceAddresses) {
                    if (interfaceAddress.getAddress() instanceof Inet4Address) {
                        Inet4Address inet4Address = (Inet4Address) interfaceAddress.getAddress();
                        String hostAddress = inet4Address.getHostAddress();
                        // TODO 做IP前缀过滤
                        return hostAddress;
                    }
                }
            }
            return Inet4Address.getLocalHost().getHostAddress();
        } catch (SocketException | UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }
}
