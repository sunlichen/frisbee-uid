package com.sunlichen.frisbee.service;

import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.*;

/**
 * @author me@sunlichen.com
 */
@Service
public class NtpTimeService {
    private static final Logger log =  org.slf4j.LoggerFactory.getLogger(NtpTimeService.class);

    @Value("${frisbeeUid.ntpServices}")
    private String[] ntpServices ;

    public volatile long offset = 0L;

    /**
     * 更新本地时间与NTP服务时间差值，取全部可用节点的差值做平均
     */
    public void updateNtpTimeOffset() {

        final NTPUDPClient client = new NTPUDPClient();
        client.setDefaultTimeout(2000);
        try {
            client.open();
            List<Long> list = new ArrayList<>();
            for (final String arg : ntpServices)
            {
                try {
                    final InetAddress hostAddress = InetAddress.getByName(arg);
                    log.info("NTP服务地址：{} / {} " , hostAddress.getHostName(), hostAddress.getHostAddress());
                    final TimeInfo info = client.getTime(hostAddress);
                    info.computeDetails();

                    list.add(info.getOffset());
                } catch (final IOException ioe) {
                    log.warn("获取NTP服务时间异常",ioe);
                }
            }

            if ( list.size() > 0 ){
                long ret = 0;
                for (Long a : list ) {
                    ret += a;
                }
                offset =  ret / list.size();
            }
            log.info("与本地时间差异: {}毫秒",offset);

        } catch (final SocketException e) {
            log.error("获取NTP服务时间异常，无法更新时间差值，已当前现有差值为准，等待下次更新:",e);
        }

        client.close();
    }


}
