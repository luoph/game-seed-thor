package com.windforce.filter.firewall;

import com.windforce.core.SessionManager;
import com.windforce.core.Wsession;
import com.windforce.utils.IpUtils;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;

/**
 * IP过虑防火墙
 *
 * @author Kuang Hao
 * @since v1.0 2016-1-20
 */
@Sharable
@Component
public class IpFirewall extends ChannelInboundHandlerAdapter {

	private static final Logger logger = LoggerFactory.getLogger(IpFirewall.class);
	/**
	 * 限制ip最大连接数(白名单的除外)
	 */
	private final int MAX_IP_SESSION_COUNT = Integer.MAX_VALUE;
	/**
	 * 白名单IP集合
	 */
	private HashSet<String> allows = new HashSet<String>();
	/**
	 * 黑名单IP集合
	 */
	private HashSet<String> blocks = new HashSet<String>();
	/**
	 * 管理后台IP集合
	 */
	private HashSet<String> managements = new HashSet<String>();
	@Autowired
	private SessionManager sessionManager;
	/**
	 * ip防火墙开关
	 */
	private volatile boolean firewallOpen = false;
	/**
	 * 是否开启(用于总开关)
	 */
	private volatile boolean opened = false;

	public void open() {
		opened = true;
	}

	public void close() {
		opened = false;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		if (!opened) {
			// 没有启动完成,方式用户在程序启动过程中连接游戏
			return;
		}
		if (firewallOpen) {
			if (!allows.contains(IpUtils.getIp(ctx.channel().remoteAddress().toString()))) {
				ctx.channel().close();
				logger.info(String.format("防火墙未开启,阻止IP[%s]连接.", ctx.channel().remoteAddress()));
				return;
			}
		}
		if (blocks.contains(IpUtils.getIp(ctx.channel().remoteAddress().toString()))) {
			ctx.channel().close();
			logger.info(String.format("IP[%s]在黑名单中,断开连接.", ctx.channel().remoteAddress()));
			return;
		}
		int ipCount = sessionManager.ipSessionCount(IpUtils.getIp(ctx.channel().remoteAddress()));
		if (MAX_IP_SESSION_COUNT <= ipCount) {
			ctx.channel().close();
			logger.info(String.format("超过最大连接数[%s],IP[%s]连接数[%s].", MAX_IP_SESSION_COUNT, ctx.channel().remoteAddress(),
				ipCount));
			return;
		}

		ctx.fireChannelActive();
	}

	/**
	 * 添加黑名单
	 *
	 * @param session
	 * @param ips
	 */
	public void addBlocks(Wsession session, String... ips) {
		if (ips != null) {
			for (String ip : ips) {
				if (!IpUtils.isIpv4(ip)) {
					throw new RuntimeException(
						String.format("管理后台IP[%s]添加IP[%s]格式非法.", session.getChannel().remoteAddress(), ip));
				}
			}
			for (String ip : ips) {
				blocks.add(ip);
				logger.info(String.format("管理后台IP[%s]添加IP[%s]到黑名单.", session.getChannel().remoteAddress(), ip));
			}
		}
	}

	/**
	 * 添加白名单IP白名单
	 *
	 * @param session
	 * @param ips
	 */
	public void addAllows(Wsession session, String... ips) {
		if (ips != null) {
			for (String ip : ips) {
				if (!IpUtils.isIpv4(ip)) {
					throw new RuntimeException(
						String.format("管理后台IP[%s]添加IP[%s]格式非法.", session.getChannel().remoteAddress(), ip));
				}
			}
			for (String ip : ips) {
				allows.add(ip);
				logger.info(String.format("管理后台IP[%s]添加IP[%s]到白名单.", session.getChannel().remoteAddress(), ip));
			}
		}
	}

	/**
	 * 添加白名单IP白名单
	 *
	 * @param session
	 * @param ips
	 */
	public void addManagements(Wsession session, String... ips) {
		if (ips != null) {
			for (String ip : ips) {
				if (!IpUtils.isIpv4(ip)) {
					throw new RuntimeException(
						String.format("管理后台IP[%s]添加IP[%s]格式非法.", session.getChannel().remoteAddress(), ip));
				}
			}
			for (String ip : ips) {
				managements.add(ip);
				logger.info(String.format("管理后台IP[%s]添加IP[%s]到管理后台.", session.getChannel().remoteAddress(), ip));
				// 添加到白名单中
				addAllows(session, ip);
			}
		}
	}

	/**
	 * TODO 考虑性能问题,不再此处判断
	 */
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		// if
		// (blocks.contains(IpUtils.getIp(ctx.channel().remoteAddress().toString())))
		// {
		// ctx.channel().close();
		// logger.info(String.format("IP[%s]在黑名单中禁止通信,断开连接.",
		// ctx.channel().remoteAddress()));
		// return;
		// }
		// ctx.fireChannelRead(msg);
	}

}
