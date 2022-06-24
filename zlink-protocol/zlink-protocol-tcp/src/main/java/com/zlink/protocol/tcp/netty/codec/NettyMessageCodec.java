package com.zlink.protocol.tcp.netty.codec;

import com.zlink.protocol.base.ProtocolUtil;
import com.zlink.protocol.tcp.netty.policy.NettyReadWritePolicy;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;

import java.io.ByteArrayOutputStream;
import java.util.List;

public class NettyMessageCodec extends ByteToMessageCodec {

    private static final int BLANK_BYTE = 0x20;
    private ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    private int bodyLength = 0;
    private NettyReadWritePolicy readPolicy;
    private NettyReadWritePolicy writePolicy;
    private String localCharset;
    private String remoteCharset;

    public NettyMessageCodec(String readPolicyStr, String writePolicyStr, String localCharset, String remoteCharset) {
        this.readPolicy = new NettyReadWritePolicy(readPolicyStr);
        this.writePolicy = new NettyReadWritePolicy(writePolicyStr);
        this.localCharset = localCharset;
        this.remoteCharset = remoteCharset;
    }

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object o, ByteBuf byteBuf) throws Exception {
        if (o instanceof byte[]) {
            byte[] sendData = ProtocolUtil.convertCharset((byte[]) o, localCharset, remoteCharset);
            int headLength = readPolicy.getHeadLength();
            int start = readPolicy.getStart();
            while (start > 0) {
                byteBuf.writeByte(BLANK_BYTE);
                start--;
            }
            if (headLength > 0) {
                byte[] heads = writePolicy.createHeadBytes(sendData.length);
                byteBuf.writeBytes(heads);
                byteBuf.writeBytes(sendData);
            } else {
                if (writePolicy.getFixLength() > 0) {
                    if (sendData.length <= writePolicy.getFixLength()) {
                        byteBuf.writeBytes(sendData);
                        int sp = writePolicy.getFixLength() - sendData.length;
                        while (sp > 0) {
                            byteBuf.writeByte(BLANK_BYTE);
                            sp--;
                        }
                    } else {
                        byteBuf.writeBytes(sendData, 0, writePolicy.getFixLength());
                    }
                } else {
                    byteBuf.writeBytes(sendData);
                }
            }

        } else {
            throw new IllegalArgumentException("Object [" + o + "] is not support");
        }
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List list) throws Exception {
        if (bodyLength == 0) {
            int headLength = readPolicy.getHeadLength();
            int start = readPolicy.getStart();
            if (headLength > 0) {
                byte[] headBytes = new byte[headLength];
                while (start > 0) {
                    byteBuf.readByte();
                    start--;
                }
                byteBuf.readBytes(headBytes);
                bodyLength = readPolicy.getBodyLength(headBytes);
            } else {
                if (readPolicy.getFixLength() > 0) {
                    bodyLength = readPolicy.getFixLength();
                } else {
                    bodyLength = byteBuf.readableBytes();
                }
            }
        }
        int len = Math.min(byteBuf.readableBytes(), bodyLength - byteArrayOutputStream.size());
        byte[] bodyBytes = new byte[len];
        byteBuf.readBytes(bodyBytes);
        byteArrayOutputStream.write(bodyBytes, 0, len);
        if (byteArrayOutputStream.size() == bodyLength) {
            bodyLength = 0;
            list.add(ProtocolUtil.convertCharset(byteArrayOutputStream.toByteArray(), remoteCharset, localCharset));
            byteArrayOutputStream = new ByteArrayOutputStream();
            int hasLen = byteBuf.readableBytes();
            while (hasLen > 0) {
                byteBuf.readByte();
                hasLen--;
            }
        }
    }
}
