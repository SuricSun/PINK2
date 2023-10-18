package org.sun.pink.nat_penetration.server.runnable.inner_socket;

import org.sun.pink.nat_penetration.common.Logger;
import org.sun.pink.nat_penetration.common.Socket_util;
import org.sun.pink.nat_penetration.server.Server;

import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.RejectedExecutionException;

/**
 * @author: SuricSun
 * @date: 2022/7/25
 */
public class Server_inner_proxy_port_listener_runnable implements Runnable {

    public Server server;
    public int port;

    public Server_inner_proxy_port_listener_runnable(Server server, int port) {

        this.server = server;
        this.port = port;
    }

    @Override
    public void run() {

        ServerSocketChannel listener;
        //循环等待连接
        try {
            listener = ServerSocketChannel.open();
            listener.bind(new InetSocketAddress(this.port));
            while (true) {
                //等待连接
                SocketChannel new_conn = listener.accept();
                //配对
                try {
                    this.server.socket_pair_thread_pool.execute(
                            new Server_inner_proxy_socket_pairing_handler_runnable(this.server, new_conn)
                    );
                } catch (RejectedExecutionException e) {
                    Logger.Log("file", Logger.Log_type.Warning, "线程池满，无法处理Inner_Socket配对请求");
                    //断开连接
                    Socket_util.Close_socket(new_conn);
                    //继续下个请求
                    continue;
                }
            }
        } catch (Exception e) {
            Logger.Log("file", Logger.Log_type.Error, "服务器异常终止", Logger.Stringify(e));
            System.exit(-1);
        }
    }
}
