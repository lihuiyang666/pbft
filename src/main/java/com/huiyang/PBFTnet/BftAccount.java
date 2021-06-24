package com.huiyang.PBFTnet;

import com.huiyang.consensusEngine.Account;

import java.util.Objects;

public class BftAccount implements Account {

    public BftAccount() {
    }

    public BftAccount(String ip, Integer host) {
        Ip = ip;
        this.host = host;
    }

    // 账户IP
    public String Ip;
    // 账户端口号

    public Integer host;

    @Override
    public String toString() {
        return "BftAccount{" +
                "Ip='" + Ip + '\'' +
                ", host=" + host +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BftAccount that = (BftAccount) o;
        return Objects.equals(Ip, that.Ip) && Objects.equals(host, that.host);
    }

    @Override
    public int hashCode() {
        return Objects.hash(Ip, host);
    }
}
